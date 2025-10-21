package io.equiflux.node.storage;

import io.equiflux.node.model.Block;
import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.exception.StorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 区块存储管理服务
 * 
 * <p>负责区块的存储、检索和管理，支持按高度、哈希等多种方式查询区块。
 * 
 * <p>存储结构：
 * <ul>
 *   <li>按高度存储：block:height -> Block</li>
 *   <li>按哈希存储：block_hash:hash -> height</li>
 *   <li>最新区块：block:latest -> Block</li>
 *   <li>区块索引：block_index:height -> hash</li>
 * </ul>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>区块的存储和检索</li>
 *   <li>按高度和哈希查询</li>
 *   <li>最新区块管理</li>
 *   <li>区块索引维护</li>
 *   <li>批量操作支持</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class BlockStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockStorageService.class);
    
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    
    // 缓存
    private final Map<Long, Block> blockCache = new ConcurrentHashMap<>();
    private final Map<String, Long> hashToHeightCache = new ConcurrentHashMap<>();
    private volatile Block latestBlock;
    
    // 线程安全
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 缓存配置
    private final int maxCacheSize = 1000;
    
    public BlockStorageService(StorageService storageService) {
        this.storageService = storageService;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 存储区块
     * 
     * @param block 区块
     * @throws StorageException 存储异常
     */
    public void storeBlock(Block block) throws StorageException {
        lock.writeLock().lock();
        try {
            long height = block.getHeight();
            String hash = block.getHashHex();
            
            // 序列化区块
            byte[] blockBytes = objectMapper.writeValueAsBytes(block);
            StorageValue blockValue = new StorageValue(blockBytes, "Block");
            
            // 存储区块（按高度）
            StorageKey blockKey = StorageKey.blockKey(height);
            storageService.put(blockKey, blockValue);
            
            // 存储区块哈希映射
            StorageKey hashKey = StorageKey.blockHashKey(hash);
            byte[] heightBytes = String.valueOf(height).getBytes();
            StorageValue heightValue = new StorageValue(heightBytes, "Height");
            storageService.put(hashKey, heightValue);
            
            // 存储区块索引
            StorageKey indexKey = new StorageKey("block_index", String.valueOf(height));
            byte[] hashBytes = hash.getBytes();
            StorageValue hashValue = new StorageValue(hashBytes, "Hash");
            storageService.put(indexKey, hashValue);
            
            // 更新最新区块
            if (latestBlock == null || height > latestBlock.getHeight()) {
                latestBlock = block;
                StorageKey latestKey = new StorageKey("block", "latest");
                storageService.put(latestKey, blockValue);
            }
            
            // 更新缓存
            blockCache.put(height, block);
            hashToHeightCache.put(hash, height);
            evictCacheIfNeeded();
            
            logger.info("Stored block: height={}, hash={}", height, hash);
            
        } catch (Exception e) {
            logger.error("Failed to store block: height={}", block.getHeight(), e);
            throw new StorageException("Failed to store block: " + block.getHeight(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 按高度获取区块
     * 
     * @param height 区块高度
     * @return 区块，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public Block getBlockByHeight(long height) throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            Block cachedBlock = blockCache.get(height);
            if (cachedBlock != null) {
                return cachedBlock;
            }
            
            // 从存储中获取
            StorageKey blockKey = StorageKey.blockKey(height);
            StorageValue blockValue = storageService.get(blockKey);
            
            if (blockValue == null) {
                return null;
            }
            
            Block block = objectMapper.readValue(blockValue.getData(), Block.class);
            
            // 更新缓存
            blockCache.put(height, block);
            evictCacheIfNeeded();
            
            logger.debug("Retrieved block by height: {}", height);
            
            return block;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve block by height: {}", height, e);
            throw new StorageException("Failed to retrieve block by height: " + height, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 按哈希获取区块
     * 
     * @param hash 区块哈希
     * @return 区块，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public Block getBlockByHash(String hash) throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            Long cachedHeight = hashToHeightCache.get(hash);
            if (cachedHeight != null) {
                Block cachedBlock = blockCache.get(cachedHeight);
                if (cachedBlock != null) {
                    return cachedBlock;
                }
            }
            
            // 从存储中获取高度
            StorageKey hashKey = StorageKey.blockHashKey(hash);
            StorageValue heightValue = storageService.get(hashKey);
            
            if (heightValue == null) {
                return null;
            }
            
            long height = Long.parseLong(new String(heightValue.getData()));
            
            // 按高度获取区块
            Block block = getBlockByHeight(height);
            
            logger.debug("Retrieved block by hash: {}", hash);
            
            return block;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve block by hash: {}", hash, e);
            throw new StorageException("Failed to retrieve block by hash: " + hash, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取最新区块
     * 
     * @return 最新区块，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public Block getLatestBlock() throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            if (latestBlock != null) {
                return latestBlock;
            }
            
            // 从存储中获取
            StorageKey latestKey = new StorageKey("block", "latest");
            StorageValue latestValue = storageService.get(latestKey);
            
            if (latestValue == null) {
                return null;
            }
            
            Block block = objectMapper.readValue(latestValue.getData(), Block.class);
            latestBlock = block;
            
            logger.debug("Retrieved latest block: height={}", block.getHeight());
            
            return block;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve latest block", e);
            throw new StorageException("Failed to retrieve latest block", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取当前区块高度
     * 
     * @return 当前区块高度，如果没有区块返回-1
     * @throws StorageException 存储异常
     */
    public long getCurrentHeight() throws StorageException {
        Block latest = getLatestBlock();
        return latest != null ? latest.getHeight() : -1;
    }
    
    /**
     * 检查区块是否存在
     * 
     * @param height 区块高度
     * @return true如果存在，false否则
     * @throws StorageException 存储异常
     */
    public boolean blockExists(long height) throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            if (blockCache.containsKey(height)) {
                return true;
            }
            
            // 检查存储
            StorageKey blockKey = StorageKey.blockKey(height);
            return storageService.exists(blockKey);
            
        } catch (Exception e) {
            logger.error("Failed to check block existence: height={}", height, e);
            throw new StorageException("Failed to check block existence: " + height, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 检查区块是否存在（按哈希）
     * 
     * @param hash 区块哈希
     * @return true如果存在，false否则
     * @throws StorageException 存储异常
     */
    public boolean blockExistsByHash(String hash) throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            if (hashToHeightCache.containsKey(hash)) {
                return true;
            }
            
            // 检查存储
            StorageKey hashKey = StorageKey.blockHashKey(hash);
            return storageService.exists(hashKey);
            
        } catch (Exception e) {
            logger.error("Failed to check block existence by hash: {}", hash, e);
            throw new StorageException("Failed to check block existence by hash: " + hash, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 批量存储区块
     * 
     * @param blocks 区块列表
     * @throws StorageException 存储异常
     */
    public void storeBlocks(List<Block> blocks) throws StorageException {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            Map<StorageKey, StorageValue> entries = new HashMap<>();
            Block newLatestBlock = null;
            
            for (Block block : blocks) {
                long height = block.getHeight();
                String hash = block.getHashHex();
                
                // 序列化区块
                byte[] blockBytes = objectMapper.writeValueAsBytes(block);
                StorageValue blockValue = new StorageValue(blockBytes, "Block");
                
                // 存储区块（按高度）
                StorageKey blockKey = StorageKey.blockKey(height);
                entries.put(blockKey, blockValue);
                
                // 存储区块哈希映射
                StorageKey hashKey = StorageKey.blockHashKey(hash);
                byte[] heightBytes = String.valueOf(height).getBytes();
                StorageValue heightValue = new StorageValue(heightBytes, "Height");
                entries.put(hashKey, heightValue);
                
                // 存储区块索引
                StorageKey indexKey = new StorageKey("block_index", String.valueOf(height));
                byte[] hashBytes = hash.getBytes();
                StorageValue hashValue = new StorageValue(hashBytes, "Hash");
                entries.put(indexKey, hashValue);
                
                // 更新最新区块
                if (newLatestBlock == null || height > newLatestBlock.getHeight()) {
                    newLatestBlock = block;
                }
                
                // 更新缓存
                blockCache.put(height, block);
                hashToHeightCache.put(hash, height);
            }
            
            // 批量存储
            storageService.putBatch(entries);
            
            // 更新最新区块
            if (newLatestBlock != null && (latestBlock == null || newLatestBlock.getHeight() > latestBlock.getHeight())) {
                latestBlock = newLatestBlock;
                StorageKey latestKey = new StorageKey("block", "latest");
                byte[] latestBytes = objectMapper.writeValueAsBytes(latestBlock);
                StorageValue latestValue = new StorageValue(latestBytes, "Block");
                storageService.put(latestKey, latestValue);
            }
            
            evictCacheIfNeeded();
            
            logger.info("Batch stored {} blocks", blocks.size());
            
        } catch (Exception e) {
            logger.error("Failed to batch store {} blocks", blocks.size(), e);
            throw new StorageException("Failed to batch store blocks", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取区块范围
     * 
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @return 区块列表
     * @throws StorageException 存储异常
     */
    public List<Block> getBlocks(long startHeight, long endHeight) throws StorageException {
        if (startHeight > endHeight) {
            throw new IllegalArgumentException("Start height cannot be greater than end height");
        }
        
        lock.readLock().lock();
        try {
            List<Block> blocks = new ArrayList<>();
            
            for (long height = startHeight; height <= endHeight; height++) {
                Block block = getBlockByHeight(height);
                if (block != null) {
                    blocks.add(block);
                }
            }
            
            logger.debug("Retrieved {} blocks from height {} to {}", blocks.size(), startHeight, endHeight);
            
            return blocks;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve blocks from height {} to {}", startHeight, endHeight, e);
            throw new StorageException("Failed to retrieve blocks from height " + startHeight + " to " + endHeight, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取最近的区块
     * 
     * @param count 区块数量
     * @return 区块列表
     * @throws StorageException 存储异常
     */
    public List<Block> getRecentBlocks(int count) throws StorageException {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        
        lock.readLock().lock();
        try {
            long currentHeight = getCurrentHeight();
            if (currentHeight < 0) {
                return new ArrayList<>();
            }
            
            long startHeight = Math.max(0, currentHeight - count + 1);
            return getBlocks(startHeight, currentHeight);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve recent {} blocks", count, e);
            throw new StorageException("Failed to retrieve recent blocks", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        lock.writeLock().lock();
        try {
            blockCache.clear();
            hashToHeightCache.clear();
            latestBlock = null;
            logger.info("Block cache cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        lock.readLock().lock();
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("blockCacheSize", blockCache.size());
            stats.put("hashToHeightCacheSize", hashToHeightCache.size());
            stats.put("latestBlockHeight", latestBlock != null ? latestBlock.getHeight() : -1);
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 缓存淘汰
     */
    private void evictCacheIfNeeded() {
        if (blockCache.size() > maxCacheSize) {
            // 简单的LRU淘汰策略
            Iterator<Map.Entry<Long, Block>> iterator = blockCache.entrySet().iterator();
            int toRemove = blockCache.size() - maxCacheSize + 100;
            
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                Map.Entry<Long, Block> entry = iterator.next();
                iterator.remove();
                
                // 同时从哈希缓存中移除
                String hash = entry.getValue().getHashHex();
                hashToHeightCache.remove(hash);
            }
            
            logger.debug("Evicted {} entries from block cache", toRemove);
        }
    }
}
