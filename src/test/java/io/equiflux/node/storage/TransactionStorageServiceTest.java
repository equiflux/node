package io.equiflux.node.storage;

import io.equiflux.node.model.Transaction;
import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 交易存储服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class TransactionStorageServiceTest {
    
    @Mock
    private StorageService storageService;
    
    private TransactionStorageService transactionStorageService;
    
    @BeforeEach
    void setUp() {
        transactionStorageService = new TransactionStorageService(storageService);
    }
    
    @Test
    void testStoreTransaction() throws StorageException {
        // 准备测试数据
        Transaction transaction = createTestTransaction();
        
        // 执行测试
        transactionStorageService.storeTransaction(transaction);
        
        // 验证结果
        verify(storageService).put(eq(StorageKey.transactionKey(transaction.getHashHex())), any(StorageValue.class));
        verify(storageService).put(eq(new StorageKey("tx_sender", transaction.getSenderPublicKeyHex())), any(StorageValue.class));
        verify(storageService).put(eq(new StorageKey("tx_receiver", transaction.getReceiverPublicKeyHex())), any(StorageValue.class));
    }
    
    @Test
    void testGetTransactionByHash() throws StorageException {
        // 准备测试数据
        Transaction transaction = createTestTransaction();
        StorageValue txValue = new StorageValue(serializeTransaction(transaction), "Transaction");
        
        when(storageService.get(StorageKey.transactionKey(transaction.getHashHex()))).thenReturn(txValue);
        
        // 执行测试
        Transaction result = transactionStorageService.getTransactionByHash(transaction.getHashHex());
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getHashHex()).isEqualTo(transaction.getHashHex());
        assertThat(result.getAmount()).isEqualTo(transaction.getAmount());
    }
    
    @Test
    void testGetTransactionByHashNotFound() throws StorageException {
        // 准备测试数据
        String hash = "testhash";
        when(storageService.get(StorageKey.transactionKey(hash))).thenReturn(null);
        
        // 执行测试
        Transaction result = transactionStorageService.getTransactionByHash(hash);
        
        // 验证结果
        assertThat(result).isNull();
    }
    
    @Test
    void testGetTransactionsBySender() throws StorageException {
        // 准备测试数据
        Transaction transaction = createTestTransaction();
        Set<String> txHashes = Set.of(transaction.getHashHex());
        StorageValue senderValue = new StorageValue(serializeStringSet(txHashes), "StringSet");
        StorageValue txValue = new StorageValue(serializeTransaction(transaction), "Transaction");
        
        when(storageService.get(new StorageKey("tx_sender", transaction.getSenderPublicKeyHex()))).thenReturn(senderValue);
        when(storageService.get(StorageKey.transactionKey(transaction.getHashHex()))).thenReturn(txValue);
        
        // 执行测试
        List<Transaction> result = transactionStorageService.getTransactionsBySender(createPublicKeyFromBytes(transaction.getSenderPublicKey()));
        
        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHashHex()).isEqualTo(transaction.getHashHex());
    }
    
    @Test
    void testGetTransactionsByReceiver() throws StorageException {
        // 准备测试数据
        Transaction transaction = createTestTransaction();
        Set<String> txHashes = Set.of(transaction.getHashHex());
        StorageValue receiverValue = new StorageValue(serializeStringSet(txHashes), "StringSet");
        StorageValue txValue = new StorageValue(serializeTransaction(transaction), "Transaction");
        
        when(storageService.get(new StorageKey("tx_receiver", transaction.getReceiverPublicKeyHex()))).thenReturn(receiverValue);
        when(storageService.get(StorageKey.transactionKey(transaction.getHashHex()))).thenReturn(txValue);
        
        // 执行测试
        List<Transaction> result = transactionStorageService.getTransactionsByReceiver(createPublicKeyFromBytes(transaction.getReceiverPublicKey()));
        
        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHashHex()).isEqualTo(transaction.getHashHex());
    }
    
    @Test
    void testTransactionExists() throws StorageException {
        // 准备测试数据
        String hash = "testhash";
        when(storageService.exists(StorageKey.transactionKey(hash))).thenReturn(true);
        
        // 执行测试
        boolean exists = transactionStorageService.transactionExists(hash);
        
        // 验证结果
        assertThat(exists).isTrue();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testStoreTransactions() throws StorageException {
        // 准备测试数据
        List<Transaction> transactions = Arrays.asList(
            createTestTransaction(),
            createTestTransaction(),
            createTestTransaction()
        );
        
        // 执行测试
        transactionStorageService.storeTransactions(transactions);
        
        // 验证结果
        verify(storageService, times(3)).putBatch(any(Map.class));
    }
    
    @Test
    void testStoreToTransactionPool() throws StorageException {
        // 准备测试数据
        Transaction transaction = createTestTransaction();
        
        // 执行测试
        transactionStorageService.storeToTransactionPool(transaction);
        
        // 验证结果
        verify(storageService).put(eq(StorageKey.transactionPoolKey(transaction.getHashHex())), any(StorageValue.class));
    }
    
    @Test
    void testGetFromTransactionPool() throws StorageException {
        // 准备测试数据
        Transaction transaction = createTestTransaction();
        StorageValue poolValue = new StorageValue(serializeTransaction(transaction), "Transaction");
        
        when(storageService.get(StorageKey.transactionPoolKey(transaction.getHashHex()))).thenReturn(poolValue);
        
        // 执行测试
        Transaction result = transactionStorageService.getFromTransactionPool(transaction.getHashHex());
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getHashHex()).isEqualTo(transaction.getHashHex());
    }
    
    @Test
    void testRemoveFromTransactionPool() throws StorageException {
        // 准备测试数据
        String hash = "testhash";
        
        // 执行测试
        transactionStorageService.removeFromTransactionPool(hash);
        
        // 验证结果
        verify(storageService).delete(StorageKey.transactionPoolKey(hash));
    }
    
    @Test
    void testGetAllFromTransactionPool() throws StorageException {
        // 准备测试数据
        Transaction transaction1 = createTestTransaction();
        Transaction transaction2 = createTestTransaction();
        
        Map<StorageKey, StorageValue> poolEntries = new HashMap<>();
        poolEntries.put(StorageKey.transactionPoolKey(transaction1.getHashHex()), 
                       new StorageValue(serializeTransaction(transaction1), "Transaction"));
        poolEntries.put(StorageKey.transactionPoolKey(transaction2.getHashHex()), 
                       new StorageValue(serializeTransaction(transaction2), "Transaction"));
        
        when(storageService.getByNamespace("tx_pool")).thenReturn(poolEntries);
        
        // 执行测试
        List<Transaction> result = transactionStorageService.getAllFromTransactionPool();
        
        // 验证结果
        assertThat(result).hasSize(2);
    }
    
    @Test
    void testClearTransactionPool() throws StorageException {
        // 准备测试数据
        Transaction transaction = createTestTransaction();
        Map<StorageKey, StorageValue> poolEntries = new HashMap<>();
        poolEntries.put(StorageKey.transactionPoolKey(transaction.getHashHex()), 
                       new StorageValue(serializeTransaction(transaction), "Transaction"));
        
        when(storageService.getByNamespace("tx_pool")).thenReturn(poolEntries);
        
        // 执行测试
        transactionStorageService.clearTransactionPool();
        
        // 验证结果
        verify(storageService).delete(StorageKey.transactionPoolKey(transaction.getHashHex()));
    }
    
    @Test
    void testClearCache() {
        // 执行测试
        transactionStorageService.clearCache();
        
        // 验证结果
        Map<String, Object> stats = transactionStorageService.getCacheStats();
        assertThat(stats.get("transactionCacheSize")).isEqualTo(0);
        assertThat(stats.get("senderToTxCacheSize")).isEqualTo(0);
        assertThat(stats.get("receiverToTxCacheSize")).isEqualTo(0);
    }
    
    @Test
    void testGetCacheStats() {
        // 执行测试
        Map<String, Object> stats = transactionStorageService.getCacheStats();
        
        // 验证结果
        assertThat(stats).containsKeys("transactionCacheSize", "senderToTxCacheSize", "receiverToTxCacheSize");
        assertThat(stats.get("transactionCacheSize")).isInstanceOf(Integer.class);
        assertThat(stats.get("senderToTxCacheSize")).isInstanceOf(Integer.class);
        assertThat(stats.get("receiverToTxCacheSize")).isInstanceOf(Integer.class);
    }
    
    @Test
    void testStoreTransactionException() throws StorageException {
        // 准备测试数据
        Transaction transaction = createTestTransaction();
        doThrow(new StorageException("Test exception")).when(storageService).put(any(StorageKey.class), any(StorageValue.class));
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> transactionStorageService.storeTransaction(transaction))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to store transaction");
    }
    
    @Test
    void testGetTransactionByHashException() throws StorageException {
        // 准备测试数据
        String hash = "testhash";
        when(storageService.get(StorageKey.transactionKey(hash))).thenThrow(new StorageException("Test exception"));
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> transactionStorageService.getTransactionByHash(hash))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to retrieve transaction by hash");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testStoreTransactionsEmptyList() throws StorageException {
        // 执行测试
        transactionStorageService.storeTransactions(new ArrayList<>());
        
        // 验证结果 - 不应该调用存储服务
        verify(storageService, never()).putBatch(any(Map.class));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testStoreTransactionsNull() throws StorageException {
        // 执行测试
        transactionStorageService.storeTransactions(null);
        
        // 验证结果 - 不应该调用存储服务
        verify(storageService, never()).putBatch(any(Map.class));
    }
    
    /**
     * 创建测试交易
     */
    private Transaction createTestTransaction() {
        try {
            KeyPair senderKeyPair = java.security.KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            KeyPair receiverKeyPair = java.security.KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            
            byte[] senderPublicKey = senderKeyPair.getPublic().getEncoded();
            byte[] receiverPublicKey = receiverKeyPair.getPublic().getEncoded();
            
            byte[] signature = new byte[64];
            Arrays.fill(signature, (byte) 1);
            
            return new Transaction(senderPublicKey, receiverPublicKey, 1000L, 10L, 
                                 System.currentTimeMillis(), 1L, signature, new byte[32],
                                 io.equiflux.node.model.TransactionType.TRANSFER);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test transaction", e);
        }
    }
    
    /**
     * 序列化交易
     */
    private byte[] serializeTransaction(Transaction transaction) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsBytes(transaction);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize transaction", e);
        }
    }
    
    /**
     * 序列化字符串集合
     */
    private byte[] serializeStringSet(Set<String> stringSet) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsBytes(stringSet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize string set", e);
        }
    }
    
    /**
     * 从字节数组创建PublicKey
     */
    private PublicKey createPublicKeyFromBytes(byte[] publicKeyBytes) {
        try {
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(publicKeyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("Ed25519");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PublicKey from bytes", e);
        }
    }
}
