package io.equiflux.node.storage;

import io.equiflux.node.model.Transaction;
import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.exception.StorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 交易存储管理服务
 * 
 * <p>负责交易的存储、检索和管理，支持按哈希、发送者、接收者等多种方式查询交易。
 * 
 * <p>存储结构：
 * <ul>
 *   <li>按哈希存储：transaction:hash -> Transaction</li>
 *   <li>按发送者存储：tx_sender:publicKey -> Set<txHash></li>
 *   <li>按接收者存储：tx_receiver:publicKey -> Set<txHash></li>
 *   <li>交易池存储：tx_pool:hash -> Transaction</li>
 *   <li>交易索引：tx_index:height:index -> hash</li>
 * </ul>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>交易的存储和检索</li>
 *   <li>按哈希、发送者、接收者查询</li>
 *   <li>交易池管理</li>
 *   <li>交易索引维护</li>
 *   <li>批量操作支持</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class TransactionStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionStorageService.class);
    
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    
    // 缓存
    private final Map<String, Transaction> transactionCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> senderToTxCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> receiverToTxCache = new ConcurrentHashMap<>();
    
    // 线程安全
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 缓存配置
    private final int maxCacheSize = 5000;
    
    public TransactionStorageService(StorageService storageService) {
        this.storageService = storageService;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 存储交易
     * 
     * @param transaction 交易
     * @throws StorageException 存储异常
     */
    public void storeTransaction(Transaction transaction) throws StorageException {
        lock.writeLock().lock();
        try {
            String hash = transaction.getHashHex();
            String senderHex = transaction.getSenderPublicKeyHex();
            String receiverHex = transaction.getReceiverPublicKeyHex();
            
            // 序列化交易
            byte[] txBytes = objectMapper.writeValueAsBytes(transaction);
            StorageValue txValue = new StorageValue(txBytes, "Transaction");
            
            // 存储交易（按哈希）
            StorageKey txKey = StorageKey.transactionKey(hash);
            storageService.put(txKey, txValue);
            
            // 更新发送者索引
            updateSenderIndex(senderHex, hash);
            
            // 更新接收者索引
            updateReceiverIndex(receiverHex, hash);
            
            // 更新缓存
            transactionCache.put(hash, transaction);
            updateSenderCache(senderHex, hash);
            updateReceiverCache(receiverHex, hash);
            evictCacheIfNeeded();
            
            logger.debug("Stored transaction: hash={}, sender={}, receiver={}", 
                        hash, senderHex, receiverHex);
            
        } catch (Exception e) {
            logger.error("Failed to store transaction: hash={}", transaction.getHashHex(), e);
            throw new StorageException("Failed to store transaction: " + transaction.getHashHex(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 按哈希获取交易
     * 
     * @param hash 交易哈希
     * @return 交易，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public Transaction getTransactionByHash(String hash) throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            Transaction cachedTx = transactionCache.get(hash);
            if (cachedTx != null) {
                return cachedTx;
            }
            
            // 从存储中获取
            StorageKey txKey = StorageKey.transactionKey(hash);
            StorageValue txValue = storageService.get(txKey);
            
            if (txValue == null) {
                return null;
            }
            
            Transaction transaction = objectMapper.readValue(txValue.getData(), Transaction.class);
            
            // 更新缓存
            transactionCache.put(hash, transaction);
            evictCacheIfNeeded();
            
            logger.debug("Retrieved transaction by hash: {}", hash);
            
            return transaction;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve transaction by hash: {}", hash, e);
            throw new StorageException("Failed to retrieve transaction by hash: " + hash, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 按发送者获取交易
     * 
     * @param senderPublicKey 发送者公钥
     * @return 交易列表
     * @throws StorageException 存储异常
     */
    public List<Transaction> getTransactionsBySender(PublicKey senderPublicKey) throws StorageException {
        lock.readLock().lock();
        try {
            String senderHex = io.equiflux.node.crypto.HashUtils.toHexString(senderPublicKey.getEncoded());
            
            // 先检查缓存
            Set<String> cachedTxHashes = senderToTxCache.get(senderHex);
            if (cachedTxHashes != null) {
                List<Transaction> transactions = new ArrayList<>();
                for (String hash : cachedTxHashes) {
                    Transaction tx = transactionCache.get(hash);
                    if (tx != null) {
                        transactions.add(tx);
                    }
                }
                return transactions;
            }
            
            // 从存储中获取
            StorageKey senderKey = new StorageKey("tx_sender", senderHex);
            StorageValue senderValue = storageService.get(senderKey);
            
            if (senderValue == null) {
                return new ArrayList<>();
            }
            
            Set<String> txHashes = deserializeStringSet(senderValue.getData());
            List<Transaction> transactions = new ArrayList<>();
            
            for (String hash : txHashes) {
                Transaction tx = getTransactionByHash(hash);
                if (tx != null) {
                    transactions.add(tx);
                }
            }
            
            // 更新缓存
            senderToTxCache.put(senderHex, txHashes);
            
            logger.debug("Retrieved {} transactions by sender: {}", transactions.size(), senderHex);
            
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions by sender", e);
            throw new StorageException("Failed to retrieve transactions by sender", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 按接收者获取交易
     * 
     * @param receiverPublicKey 接收者公钥
     * @return 交易列表
     * @throws StorageException 存储异常
     */
    public List<Transaction> getTransactionsByReceiver(PublicKey receiverPublicKey) throws StorageException {
        lock.readLock().lock();
        try {
            String receiverHex = io.equiflux.node.crypto.HashUtils.toHexString(receiverPublicKey.getEncoded());
            
            // 先检查缓存
            Set<String> cachedTxHashes = receiverToTxCache.get(receiverHex);
            if (cachedTxHashes != null) {
                List<Transaction> transactions = new ArrayList<>();
                for (String hash : cachedTxHashes) {
                    Transaction tx = transactionCache.get(hash);
                    if (tx != null) {
                        transactions.add(tx);
                    }
                }
                return transactions;
            }
            
            // 从存储中获取
            StorageKey receiverKey = new StorageKey("tx_receiver", receiverHex);
            StorageValue receiverValue = storageService.get(receiverKey);
            
            if (receiverValue == null) {
                return new ArrayList<>();
            }
            
            Set<String> txHashes = deserializeStringSet(receiverValue.getData());
            List<Transaction> transactions = new ArrayList<>();
            
            for (String hash : txHashes) {
                Transaction tx = getTransactionByHash(hash);
                if (tx != null) {
                    transactions.add(tx);
                }
            }
            
            // 更新缓存
            receiverToTxCache.put(receiverHex, txHashes);
            
            logger.debug("Retrieved {} transactions by receiver: {}", transactions.size(), receiverHex);
            
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions by receiver", e);
            throw new StorageException("Failed to retrieve transactions by receiver", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 检查交易是否存在
     * 
     * @param hash 交易哈希
     * @return true如果存在，false否则
     * @throws StorageException 存储异常
     */
    public boolean transactionExists(String hash) throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            if (transactionCache.containsKey(hash)) {
                return true;
            }
            
            // 检查存储
            StorageKey txKey = StorageKey.transactionKey(hash);
            return storageService.exists(txKey);
            
        } catch (Exception e) {
            logger.error("Failed to check transaction existence: hash={}", hash, e);
            throw new StorageException("Failed to check transaction existence: " + hash, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 批量存储交易
     * 
     * @param transactions 交易列表
     * @throws StorageException 存储异常
     */
    public void storeTransactions(List<Transaction> transactions) throws StorageException {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            Map<StorageKey, StorageValue> entries = new HashMap<>();
            Map<String, Set<String>> senderUpdates = new HashMap<>();
            Map<String, Set<String>> receiverUpdates = new HashMap<>();
            
            for (Transaction transaction : transactions) {
                String hash = transaction.getHashHex();
                String senderHex = transaction.getSenderPublicKeyHex();
                String receiverHex = transaction.getReceiverPublicKeyHex();
                
                // 序列化交易
                byte[] txBytes = objectMapper.writeValueAsBytes(transaction);
                StorageValue txValue = new StorageValue(txBytes, "Transaction");
                
                // 存储交易（按哈希）
                StorageKey txKey = StorageKey.transactionKey(hash);
                entries.put(txKey, txValue);
                
                // 更新发送者索引
                senderUpdates.computeIfAbsent(senderHex, k -> new HashSet<>()).add(hash);
                
                // 更新接收者索引
                receiverUpdates.computeIfAbsent(receiverHex, k -> new HashSet<>()).add(hash);
                
                // 更新缓存
                transactionCache.put(hash, transaction);
            }
            
            // 批量存储交易
            storageService.putBatch(entries);
            
            // 批量更新索引
            updateSenderIndices(senderUpdates);
            updateReceiverIndices(receiverUpdates);
            
            evictCacheIfNeeded();
            
            logger.info("Batch stored {} transactions", transactions.size());
            
        } catch (Exception e) {
            logger.error("Failed to batch store {} transactions", transactions.size(), e);
            throw new StorageException("Failed to batch store transactions", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 存储交易到交易池
     * 
     * @param transaction 交易
     * @throws StorageException 存储异常
     */
    public void storeToTransactionPool(Transaction transaction) throws StorageException {
        lock.writeLock().lock();
        try {
            String hash = transaction.getHashHex();
            
            // 序列化交易
            byte[] txBytes = objectMapper.writeValueAsBytes(transaction);
            StorageValue txValue = new StorageValue(txBytes, "Transaction");
            
            // 存储到交易池
            StorageKey poolKey = StorageKey.transactionPoolKey(hash);
            storageService.put(poolKey, txValue);
            
            logger.debug("Stored transaction to pool: hash={}", hash);
            
        } catch (Exception e) {
            logger.error("Failed to store transaction to pool: hash={}", transaction.getHashHex(), e);
            throw new StorageException("Failed to store transaction to pool: " + transaction.getHashHex(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 从交易池获取交易
     * 
     * @param hash 交易哈希
     * @return 交易，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public Transaction getFromTransactionPool(String hash) throws StorageException {
        lock.readLock().lock();
        try {
            StorageKey poolKey = StorageKey.transactionPoolKey(hash);
            StorageValue poolValue = storageService.get(poolKey);
            
            if (poolValue == null) {
                return null;
            }
            
            Transaction transaction = objectMapper.readValue(poolValue.getData(), Transaction.class);
            
            logger.debug("Retrieved transaction from pool: hash={}", hash);
            
            return transaction;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve transaction from pool: hash={}", hash, e);
            throw new StorageException("Failed to retrieve transaction from pool: " + hash, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 从交易池删除交易
     * 
     * @param hash 交易哈希
     * @throws StorageException 存储异常
     */
    public void removeFromTransactionPool(String hash) throws StorageException {
        lock.writeLock().lock();
        try {
            StorageKey poolKey = StorageKey.transactionPoolKey(hash);
            storageService.delete(poolKey);
            
            logger.debug("Removed transaction from pool: hash={}", hash);
            
        } catch (Exception e) {
            logger.error("Failed to remove transaction from pool: hash={}", hash, e);
            throw new StorageException("Failed to remove transaction from pool: " + hash, e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取交易池中的所有交易
     * 
     * @return 交易列表
     * @throws StorageException 存储异常
     */
    public List<Transaction> getAllFromTransactionPool() throws StorageException {
        lock.readLock().lock();
        try {
            Map<StorageKey, StorageValue> poolEntries = storageService.getByNamespace("tx_pool");
            List<Transaction> transactions = new ArrayList<>();
            
            for (StorageValue value : poolEntries.values()) {
                Transaction transaction = objectMapper.readValue(value.getData(), Transaction.class);
                transactions.add(transaction);
            }
            
            logger.debug("Retrieved {} transactions from pool", transactions.size());
            
            return transactions;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve all transactions from pool", e);
            throw new StorageException("Failed to retrieve all transactions from pool", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 清空交易池
     * 
     * @throws StorageException 存储异常
     */
    public void clearTransactionPool() throws StorageException {
        lock.writeLock().lock();
        try {
            Map<StorageKey, StorageValue> poolEntries = storageService.getByNamespace("tx_pool");
            
            for (StorageKey key : poolEntries.keySet()) {
                storageService.delete(key);
            }
            
            logger.info("Cleared transaction pool: {} transactions removed", poolEntries.size());
            
        } catch (Exception e) {
            logger.error("Failed to clear transaction pool", e);
            throw new StorageException("Failed to clear transaction pool", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        lock.writeLock().lock();
        try {
            transactionCache.clear();
            senderToTxCache.clear();
            receiverToTxCache.clear();
            logger.info("Transaction cache cleared");
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
            stats.put("transactionCacheSize", transactionCache.size());
            stats.put("senderToTxCacheSize", senderToTxCache.size());
            stats.put("receiverToTxCacheSize", receiverToTxCache.size());
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 更新发送者索引
     */
    private void updateSenderIndex(String senderHex, String txHash) throws StorageException {
        StorageKey senderKey = new StorageKey("tx_sender", senderHex);
        StorageValue senderValue = storageService.get(senderKey);
        
        Set<String> txHashes;
        if (senderValue != null) {
            txHashes = deserializeStringSet(senderValue.getData());
        } else {
            txHashes = new HashSet<>();
        }
        
        txHashes.add(txHash);
        
        byte[] txHashesBytes = serializeStringSet(txHashes);
        StorageValue newSenderValue = new StorageValue(txHashesBytes, "StringSet");
        storageService.put(senderKey, newSenderValue);
    }
    
    /**
     * 更新接收者索引
     */
    private void updateReceiverIndex(String receiverHex, String txHash) throws StorageException {
        StorageKey receiverKey = new StorageKey("tx_receiver", receiverHex);
        StorageValue receiverValue = storageService.get(receiverKey);
        
        Set<String> txHashes;
        if (receiverValue != null) {
            txHashes = deserializeStringSet(receiverValue.getData());
        } else {
            txHashes = new HashSet<>();
        }
        
        txHashes.add(txHash);
        
        byte[] txHashesBytes = serializeStringSet(txHashes);
        StorageValue newReceiverValue = new StorageValue(txHashesBytes, "StringSet");
        storageService.put(receiverKey, newReceiverValue);
    }
    
    /**
     * 批量更新发送者索引
     */
    private void updateSenderIndices(Map<String, Set<String>> senderUpdates) throws StorageException {
        Map<StorageKey, StorageValue> entries = new HashMap<>();
        
        for (Map.Entry<String, Set<String>> entry : senderUpdates.entrySet()) {
            String senderHex = entry.getKey();
            Set<String> newTxHashes = entry.getValue();
            
            StorageKey senderKey = new StorageKey("tx_sender", senderHex);
            StorageValue senderValue = storageService.get(senderKey);
            
            Set<String> txHashes;
            if (senderValue != null) {
                txHashes = deserializeStringSet(senderValue.getData());
            } else {
                txHashes = new HashSet<>();
            }
            
            txHashes.addAll(newTxHashes);
            
            byte[] txHashesBytes = serializeStringSet(txHashes);
            StorageValue newSenderValue = new StorageValue(txHashesBytes, "StringSet");
            entries.put(senderKey, newSenderValue);
        }
        
        storageService.putBatch(entries);
    }
    
    /**
     * 批量更新接收者索引
     */
    private void updateReceiverIndices(Map<String, Set<String>> receiverUpdates) throws StorageException {
        Map<StorageKey, StorageValue> entries = new HashMap<>();
        
        for (Map.Entry<String, Set<String>> entry : receiverUpdates.entrySet()) {
            String receiverHex = entry.getKey();
            Set<String> newTxHashes = entry.getValue();
            
            StorageKey receiverKey = new StorageKey("tx_receiver", receiverHex);
            StorageValue receiverValue = storageService.get(receiverKey);
            
            Set<String> txHashes;
            if (receiverValue != null) {
                txHashes = deserializeStringSet(receiverValue.getData());
            } else {
                txHashes = new HashSet<>();
            }
            
            txHashes.addAll(newTxHashes);
            
            byte[] txHashesBytes = serializeStringSet(txHashes);
            StorageValue newReceiverValue = new StorageValue(txHashesBytes, "StringSet");
            entries.put(receiverKey, newReceiverValue);
        }
        
        storageService.putBatch(entries);
    }
    
    /**
     * 更新发送者缓存
     */
    private void updateSenderCache(String senderHex, String txHash) {
        senderToTxCache.computeIfAbsent(senderHex, k -> new HashSet<>()).add(txHash);
    }
    
    /**
     * 更新接收者缓存
     */
    private void updateReceiverCache(String receiverHex, String txHash) {
        receiverToTxCache.computeIfAbsent(receiverHex, k -> new HashSet<>()).add(txHash);
    }
    
    /**
     * 序列化字符串集合
     */
    private byte[] serializeStringSet(Set<String> stringSet) throws StorageException {
        try {
            return objectMapper.writeValueAsBytes(stringSet);
        } catch (Exception e) {
            throw new StorageException("Failed to serialize string set", e);
        }
    }
    
    /**
     * 反序列化字符串集合
     */
    @SuppressWarnings("unchecked")
    private Set<String> deserializeStringSet(byte[] data) throws StorageException {
        try {
            return objectMapper.readValue(data, Set.class);
        } catch (Exception e) {
            throw new StorageException("Failed to deserialize string set", e);
        }
    }
    
    /**
     * 缓存淘汰
     */
    private void evictCacheIfNeeded() {
        if (transactionCache.size() > maxCacheSize) {
            // 简单的LRU淘汰策略
            Iterator<Map.Entry<String, Transaction>> iterator = transactionCache.entrySet().iterator();
            int toRemove = transactionCache.size() - maxCacheSize + 100;
            
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                iterator.next();
                iterator.remove();
            }
            
            logger.debug("Evicted {} entries from transaction cache", toRemove);
        }
    }
}
