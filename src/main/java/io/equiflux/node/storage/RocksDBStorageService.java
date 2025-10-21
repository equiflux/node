package io.equiflux.node.storage;

import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.storage.model.StorageStats;
import io.equiflux.node.exception.StorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * RocksDB存储服务
 * 
 * <p>基于RocksDB的高性能键值存储服务，用于区块链数据的持久化存储。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>键值对的存储、检索和删除</li>
 *   <li>批量操作支持</li>
 *   <li>事务支持</li>
 *   <li>统计信息收集</li>
 *   <li>缓存机制</li>
 * </ul>
 * 
 * <p>存储结构：
 * <ul>
 *   <li>键格式：namespace:key:version</li>
 *   <li>值格式：JSON序列化的StorageValue</li>
 *   <li>索引：支持按命名空间查询</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class RocksDBStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(RocksDBStorageService.class);
    
    // RocksDB实例
    private RocksDB rocksDB;
    private Options options;
    private String dbPath;
    
    // 缓存
    private final Map<String, StorageValue> cache = new ConcurrentHashMap<>();
    private final int maxCacheSize = 10000;
    
    // 统计信息
    private volatile StorageStats stats = new StorageStats();
    
    // 线程安全
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // JSON序列化
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 初始化RocksDB
     */
    @PostConstruct
    public void initialize() {
        try {
            // 设置数据库路径
            dbPath = System.getProperty("user.home") + "/.equiflux/rocksdb";
            File dbDir = new File(dbPath);
            if (!dbDir.exists()) {
                boolean created = dbDir.mkdirs();
                if (!created) {
                    throw new StorageException("Failed to create database directory: " + dbPath);
                }
            }
            
            // 配置RocksDB选项
            options = new Options();
            options.setCreateIfMissing(true);
            options.setMaxOpenFiles(1000);
            options.setWriteBufferSize(64 * 1024 * 1024); // 64MB
            options.setMaxWriteBufferNumber(3);
            options.setTargetFileSizeBase(64 * 1024 * 1024); // 64MB
            options.setMaxBytesForLevelBase(256 * 1024 * 1024); // 256MB
            options.setCompressionType(CompressionType.SNAPPY_COMPRESSION);
            options.setCompactionStyle(CompactionStyle.LEVEL);
            
            // 打开数据库
            rocksDB = RocksDB.open(options, dbPath);
            
            logger.info("RocksDB initialized successfully at: {}", dbPath);
            
            // 加载统计信息
            loadStats();
            
        } catch (RocksDBException e) {
            logger.error("Failed to initialize RocksDB", e);
            throw new StorageException("Failed to initialize RocksDB", e);
        }
    }
    
    /**
     * 关闭RocksDB
     */
    @PreDestroy
    public void shutdown() {
        try {
            if (rocksDB != null) {
                rocksDB.close();
            }
            if (options != null) {
                options.close();
            }
            logger.info("RocksDB shutdown successfully");
        } catch (Exception e) {
            logger.error("Error during RocksDB shutdown", e);
        }
    }
    
    /**
     * 存储键值对
     * 
     * @param key 存储键
     * @param value 存储值
     * @throws StorageException 存储异常
     */
    public void put(StorageKey key, StorageValue value) throws StorageException {
        lock.writeLock().lock();
        try {
            String keyStr = key.getFullKey();
            byte[] valueBytes = serializeValue(value);
            
            rocksDB.put(keyStr.getBytes(StandardCharsets.UTF_8), valueBytes);
            
            // 更新缓存
            cache.put(keyStr, value);
            evictCacheIfNeeded();
            
            // 更新统计信息
            stats = stats.incrementWriteOperations()
                        .updateKeys(1)
                        .updateDataSize(value.getDataSize());
            
            logger.debug("Stored key: {}, dataSize: {}", keyStr, value.getDataSize());
            
        } catch (Exception e) {
            logger.error("Failed to store key: {}", key.getFullKey(), e);
            throw new StorageException("Failed to store key: " + key.getFullKey(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取存储值
     * 
     * @param key 存储键
     * @return 存储值，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public StorageValue get(StorageKey key) throws StorageException {
        lock.readLock().lock();
        try {
            String keyStr = key.getFullKey();
            
            // 先检查缓存
            StorageValue cachedValue = cache.get(keyStr);
            if (cachedValue != null) {
                stats = stats.incrementReadOperations();
                return cachedValue;
            }
            
            // 从数据库读取
            byte[] valueBytes = rocksDB.get(keyStr.getBytes(StandardCharsets.UTF_8));
            if (valueBytes == null) {
                stats = stats.incrementReadOperations();
                return null;
            }
            
            StorageValue value = deserializeValue(valueBytes);
            
            // 更新缓存
            cache.put(keyStr, value);
            evictCacheIfNeeded();
            
            // 更新统计信息
            stats = stats.incrementReadOperations();
            
            logger.debug("Retrieved key: {}, dataSize: {}", keyStr, value.getDataSize());
            
            return value;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve key: {}", key.getFullKey(), e);
            throw new StorageException("Failed to retrieve key: " + key.getFullKey(), e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 删除存储键值对
     * 
     * @param key 存储键
     * @throws StorageException 存储异常
     */
    public void delete(StorageKey key) throws StorageException {
        lock.writeLock().lock();
        try {
            String keyStr = key.getFullKey();
            
            // 先获取值以更新统计信息
            StorageValue value = get(key);
            if (value != null) {
                rocksDB.delete(keyStr.getBytes(StandardCharsets.UTF_8));
                
                // 从缓存中移除
                cache.remove(keyStr);
                
                // 更新统计信息
                stats = stats.incrementDeleteOperations()
                            .updateKeys(-1)
                            .updateDataSize(-value.getDataSize());
                
                logger.debug("Deleted key: {}", keyStr);
            }
            
        } catch (Exception e) {
            logger.error("Failed to delete key: {}", key.getFullKey(), e);
            throw new StorageException("Failed to delete key: " + key.getFullKey(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 检查键是否存在
     * 
     * @param key 存储键
     * @return true如果存在，false否则
     * @throws StorageException 存储异常
     */
    public boolean exists(StorageKey key) throws StorageException {
        lock.readLock().lock();
        try {
            String keyStr = key.getFullKey();
            
            // 先检查缓存
            if (cache.containsKey(keyStr)) {
                return true;
            }
            
            // 检查数据库
            byte[] valueBytes = rocksDB.get(keyStr.getBytes(StandardCharsets.UTF_8));
            return valueBytes != null;
            
        } catch (Exception e) {
            logger.error("Failed to check existence of key: {}", key.getFullKey(), e);
            throw new StorageException("Failed to check existence of key: " + key.getFullKey(), e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 批量存储
     * 
     * @param entries 键值对映射
     * @throws StorageException 存储异常
     */
    public void putBatch(Map<StorageKey, StorageValue> entries) throws StorageException {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            WriteBatch batch = new WriteBatch();
            int totalDataSize = 0;
            
            for (Map.Entry<StorageKey, StorageValue> entry : entries.entrySet()) {
                StorageKey key = entry.getKey();
                StorageValue value = entry.getValue();
                
                String keyStr = key.getFullKey();
                byte[] valueBytes = serializeValue(value);
                
                batch.put(keyStr.getBytes(StandardCharsets.UTF_8), valueBytes);
                
                // 更新缓存
                cache.put(keyStr, value);
                
                totalDataSize += value.getDataSize();
            }
            
            WriteOptions writeOptions = new WriteOptions();
            rocksDB.write(writeOptions, batch);
            
            evictCacheIfNeeded();
            
            // 更新统计信息
            stats = stats.incrementWriteOperations()
                        .updateKeys(entries.size())
                        .updateDataSize(totalDataSize);
            
            logger.debug("Batch stored {} entries, totalDataSize: {}", entries.size(), totalDataSize);
            
        } catch (Exception e) {
            logger.error("Failed to batch store {} entries", entries.size(), e);
            throw new StorageException("Failed to batch store entries", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 批量获取
     * 
     * @param keys 存储键列表
     * @return 键值对映射
     * @throws StorageException 存储异常
     */
    public Map<StorageKey, StorageValue> getBatch(List<StorageKey> keys) throws StorageException {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        
        lock.readLock().lock();
        try {
            Map<StorageKey, StorageValue> result = new HashMap<>();
            
            for (StorageKey key : keys) {
                StorageValue value = get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
            
            logger.debug("Batch retrieved {} values from {} keys", result.size(), keys.size());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to batch retrieve {} keys", keys.size(), e);
            throw new StorageException("Failed to batch retrieve keys", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 按命名空间查询
     * 
     * @param namespace 命名空间
     * @return 该命名空间下的所有键值对
     * @throws StorageException 存储异常
     */
    public Map<StorageKey, StorageValue> getByNamespace(String namespace) throws StorageException {
        lock.readLock().lock();
        try {
            Map<StorageKey, StorageValue> result = new HashMap<>();
            String prefix = namespace + ":";
            
            RocksIterator iterator = rocksDB.newIterator();
            iterator.seek(prefix.getBytes(StandardCharsets.UTF_8));
            
            while (iterator.isValid()) {
                byte[] keyBytes = iterator.key();
                String keyStr = new String(keyBytes, StandardCharsets.UTF_8);
                
                if (!keyStr.startsWith(prefix)) {
                    break;
                }
                
                byte[] valueBytes = iterator.value();
                StorageValue value = deserializeValue(valueBytes);
                StorageKey key = StorageKey.fromFullKey(keyStr);
                
                result.put(key, value);
                iterator.next();
            }
            
            iterator.close();
            
            logger.debug("Retrieved {} entries from namespace: {}", result.size(), namespace);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve entries from namespace: {}", namespace, e);
            throw new StorageException("Failed to retrieve entries from namespace: " + namespace, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取统计信息
     * 
     * @return 存储统计信息
     */
    public StorageStats getStats() {
        return stats;
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        cache.clear();
        logger.info("Cache cleared");
    }
    
    /**
     * 获取缓存大小
     * 
     * @return 缓存大小
     */
    public int getCacheSize() {
        return cache.size();
    }
    
    /**
     * 序列化存储值
     * 
     * @param value 存储值
     * @return 序列化后的字节数组
     * @throws StorageException 序列化异常
     */
    private byte[] serializeValue(StorageValue value) throws StorageException {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new StorageException("Failed to serialize value", e);
        }
    }
    
    /**
     * 反序列化存储值
     * 
     * @param valueBytes 序列化后的字节数组
     * @return 存储值
     * @throws StorageException 反序列化异常
     */
    private StorageValue deserializeValue(byte[] valueBytes) throws StorageException {
        try {
            return objectMapper.readValue(valueBytes, StorageValue.class);
        } catch (Exception e) {
            throw new StorageException("Failed to deserialize value", e);
        }
    }
    
    /**
     * 缓存淘汰
     */
    private void evictCacheIfNeeded() {
        if (cache.size() > maxCacheSize) {
            // 简单的LRU淘汰策略
            Iterator<Map.Entry<String, StorageValue>> iterator = cache.entrySet().iterator();
            int toRemove = cache.size() - maxCacheSize + 100; // 多移除一些避免频繁淘汰
            
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                iterator.next();
                iterator.remove();
            }
            
            logger.debug("Evicted {} entries from cache", toRemove);
        }
    }
    
    /**
     * 加载统计信息
     */
    private void loadStats() {
        try {
            StorageKey statsKey = new StorageKey("system", "stats");
            StorageValue statsValue = get(statsKey);
            
            if (statsValue != null) {
                stats = objectMapper.readValue(statsValue.getData(), StorageStats.class);
                logger.info("Loaded storage stats: {}", stats);
            } else {
                logger.info("No existing stats found, using default stats");
            }
        } catch (Exception e) {
            logger.warn("Failed to load stats, using default stats", e);
        }
    }
    
    /**
     * 保存统计信息
     */
    public void saveStats() {
        try {
            StorageKey statsKey = new StorageKey("system", "stats");
            byte[] statsBytes = objectMapper.writeValueAsBytes(stats);
            StorageValue statsValue = new StorageValue(statsBytes, "StorageStats");
            
            put(statsKey, statsValue);
            logger.debug("Saved storage stats: {}", stats);
        } catch (Exception e) {
            logger.error("Failed to save stats", e);
        }
    }
}
