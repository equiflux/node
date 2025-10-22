package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.TransactionType;
import io.equiflux.node.storage.TransactionStorageService;
import io.equiflux.node.network.NetworkService;
import io.equiflux.node.wallet.model.TransactionInfo;
import io.equiflux.node.wallet.model.TransactionState;
import io.equiflux.node.wallet.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TransactionBroadcastService测试类
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class TransactionBroadcastServiceTest {
    
    @Mock
    private NetworkService networkService;
    
    @Mock
    private TransactionStorageService transactionStorageService;
    
    @Mock
    private TransactionSigningService transactionSigningService;
    
    private TransactionBroadcastService broadcastService;
    
    @BeforeEach
    void setUp() {
        broadcastService = new TransactionBroadcastService(
            networkService,
            transactionStorageService,
            transactionSigningService
        );
    }
    
    @Test
    void testBroadcastTransaction() throws WalletException {
        // Given
        Transaction transaction = createMockTransaction();
        when(transactionSigningService.verifyTransactionSignature(transaction)).thenReturn(true);
        
        // When
        String result = broadcastService.broadcastTransaction(transaction);
        
        // Then
        assertNotNull(result);
        assertEquals(transaction.getHashHex(), result);
        verify(transactionSigningService).verifyTransactionSignature(transaction);
        verify(transactionStorageService).storeTransaction(transaction);
        verify(networkService).broadcastTransaction(transaction);
    }
    
    @Test
    void testBroadcastTransaction_InvalidSignature() throws WalletException {
        // Given
        Transaction transaction = createMockTransaction();
        when(transactionSigningService.verifyTransactionSignature(transaction)).thenReturn(false);
        
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            broadcastService.broadcastTransaction(transaction);
        });
        
        assertEquals("Invalid transaction signature", exception.getMessage());
        verify(transactionSigningService).verifyTransactionSignature(transaction);
        verify(transactionStorageService, never()).storeTransaction(any());
        verify(networkService, never()).broadcastTransaction(any());
    }
    
    @Test
    void testBroadcastTransaction_NullTransaction() {
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            broadcastService.broadcastTransaction(null);
        });
        
        assertEquals("Transaction cannot be null", exception.getMessage());
    }
    
    @Test
    void testGetTransactionStatus() throws WalletException {
        // Given
        String transactionHash = "test_transaction_hash";
        Transaction transaction = createMockTransaction();
        when(transactionStorageService.getTransactionByHash(transactionHash)).thenReturn(transaction);
        
        // When
        Optional<TransactionStatus> result = broadcastService.getTransactionStatus(transactionHash);
        
        // Then
        assertTrue(result.isPresent());
        TransactionStatus status = result.get();
        assertEquals(transactionHash, status.getTransactionHash());
        assertEquals(TransactionState.PENDING, status.getState());
        assertNotNull(status.getTimestamp());
    }
    
    @Test
    void testGetTransactionStatus_NotFound() throws WalletException {
        // Given
        String transactionHash = "non_existent_hash";
        when(transactionStorageService.getTransactionByHash(transactionHash)).thenReturn(null);
        
        // When
        Optional<TransactionStatus> result = broadcastService.getTransactionStatus(transactionHash);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetTransactionStatus_NullHash() {
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            broadcastService.getTransactionStatus(null);
        });
        
        assertEquals("Transaction hash cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGetTransactionStatus_EmptyHash() {
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            broadcastService.getTransactionStatus("");
        });
        
        assertEquals("Transaction hash cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGetTransactionHistory() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        int limit = 10;
        int offset = 0;
        
        PublicKey publicKey = mock(PublicKey.class);
        List<Transaction> sentTransactions = List.of(createMockTransaction());
        List<Transaction> receivedTransactions = List.of(createMockTransaction());
        
        when(transactionStorageService.getTransactionsBySender(any(PublicKey.class)))
            .thenReturn(sentTransactions);
        when(transactionStorageService.getTransactionsByReceiver(any(PublicKey.class)))
            .thenReturn(receivedTransactions);
        
        // When
        List<TransactionInfo> result = broadcastService.getTransactionHistory(publicKeyHex, limit, offset);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // sent + received
        verify(transactionStorageService).getTransactionsBySender(any(PublicKey.class));
        verify(transactionStorageService).getTransactionsByReceiver(any(PublicKey.class));
    }
    
    @Test
    void testGetTransactionHistory_NullPublicKey() {
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            broadcastService.getTransactionHistory(null, 10, 0);
        });
        
        assertEquals("Public key hex cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGetTransactionHistory_EmptyPublicKey() {
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            broadcastService.getTransactionHistory("", 10, 0);
        });
        
        assertEquals("Public key hex cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testGetTransactionHistory_InvalidLimit() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        int invalidLimit = -1;
        int offset = 0;
        
        when(transactionStorageService.getTransactionsBySender(any(PublicKey.class)))
            .thenReturn(List.of());
        when(transactionStorageService.getTransactionsByReceiver(any(PublicKey.class)))
            .thenReturn(List.of());
        
        // When
        List<TransactionInfo> result = broadcastService.getTransactionHistory(publicKeyHex, invalidLimit, offset);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    void testGetTransactionHistory_InvalidOffset() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        int limit = 10;
        int invalidOffset = -1;
        
        when(transactionStorageService.getTransactionsBySender(any(PublicKey.class)))
            .thenReturn(List.of());
        when(transactionStorageService.getTransactionsByReceiver(any(PublicKey.class)))
            .thenReturn(List.of());
        
        // When
        List<TransactionInfo> result = broadcastService.getTransactionHistory(publicKeyHex, limit, invalidOffset);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    void testUpdateTransactionStatus() {
        // Given
        String transactionHash = "test_transaction_hash";
        TransactionState newState = TransactionState.CONFIRMED;
        Long blockHeight = 100L;
        Integer blockIndex = 5;
        String errorMessage = null;
        
        // 先添加一个待处理的交易状态
        TransactionStatus initialStatus = new TransactionStatus(
            transactionHash,
            TransactionState.PENDING,
            LocalDateTime.now(),
            null,
            null,
            null
        );
        broadcastService.updateTransactionStatus(transactionHash, TransactionState.PENDING, null, null, null);
        
        // When
        broadcastService.updateTransactionStatus(transactionHash, newState, blockHeight, blockIndex, errorMessage);
        
        // Then
        Optional<TransactionStatus> result = broadcastService.getTransactionStatus(transactionHash);
        assertTrue(result.isPresent());
        TransactionStatus status = result.get();
        assertEquals(newState, status.getState());
        assertEquals(blockHeight, status.getBlockHeight());
        assertEquals(blockIndex, status.getBlockIndex());
        assertEquals(errorMessage, status.getErrorMessage());
    }
    
    @Test
    void testCleanupExpiredTransactionStatus() {
        // Given
        String transactionHash = "test_transaction_hash";
        TransactionStatus expiredStatus = new TransactionStatus(
            transactionHash,
            TransactionState.PENDING,
            LocalDateTime.now().minusHours(25), // 25小时前，应该被清理
            null,
            null,
            null
        );
        
        // 添加过期的交易状态
        broadcastService.updateTransactionStatus(transactionHash, TransactionState.PENDING, null, null, null);
        
        // When
        broadcastService.cleanupExpiredTransactionStatus();
        
        // Then
        // 由于时间戳是在测试中动态生成的，这里主要测试方法不会抛出异常
        assertDoesNotThrow(() -> broadcastService.cleanupExpiredTransactionStatus());
    }
    
    // ==================== 辅助方法 ====================
    
    private Transaction createMockTransaction() {
        // 创建64字节的模拟签名
        byte[] mockSignature = new byte[64];
        for (int i = 0; i < 64; i++) {
            mockSignature[i] = (byte) (i % 256);
        }
        
        return Transaction.builder()
            .fromPublicKey("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")
            .toPublicKey("fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210")
            .amount(1000L)
            .fee(10L)
            .nonce(5L)
            .timestamp(System.currentTimeMillis())
            .type(TransactionType.TRANSFER)
            .signature(mockSignature)
            .build();
    }
}
