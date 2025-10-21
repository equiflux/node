package io.equiflux.node.rpc.service;

import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.rpc.dto.*;
import io.equiflux.node.rpc.exception.*;
import io.equiflux.node.storage.BlockStorageService;
import io.equiflux.node.storage.StateStorageService;
import io.equiflux.node.storage.TransactionStorageService;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.storage.model.ChainState;
import io.equiflux.node.network.NetworkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * RPC服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class RpcServiceTest {
    
    @Mock
    private BlockStorageService blockStorageService;
    
    @Mock
    private StateStorageService stateStorageService;
    
    @Mock
    private TransactionStorageService transactionStorageService;
    
    @Mock
    private NetworkService networkService;
    
    private RpcService rpcService;
    
    @BeforeEach
    void setUp() {
        rpcService = new RpcService(blockStorageService, stateStorageService, 
                                  transactionStorageService, networkService);
    }
    
    @Test
    void testGetLatestBlock_Success() throws Exception {
        // Given
        Block mockBlock = createMockBlock();
        when(blockStorageService.getLatestBlock()).thenReturn(mockBlock);
        
        // When
        BlockInfoDto result = rpcService.getLatestBlock();
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.getHeight());
        assertEquals("test-hash", result.getHash());
        verify(blockStorageService).getLatestBlock();
    }
    
    @Test
    void testGetLatestBlock_NotFound() throws Exception {
        // Given
        when(blockStorageService.getLatestBlock()).thenReturn(null);
        
        // When & Then
        assertThrows(BlockNotFoundException.class, () -> rpcService.getLatestBlock());
        verify(blockStorageService).getLatestBlock();
    }
    
    @Test
    void testGetBlockByHeight_Success() throws Exception {
        // Given
        Block mockBlock = createMockBlock();
        when(blockStorageService.getBlockByHeight(100L)).thenReturn(mockBlock);
        
        // When
        BlockInfoDto result = rpcService.getBlockByHeight(100L);
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.getHeight());
        assertEquals("test-hash", result.getHash());
        verify(blockStorageService).getBlockByHeight(100L);
    }
    
    @Test
    void testGetBlockByHeight_NotFound() throws Exception {
        // Given
        when(blockStorageService.getBlockByHeight(100L)).thenReturn(null);
        
        // When & Then
        assertThrows(BlockNotFoundException.class, () -> rpcService.getBlockByHeight(100L));
        verify(blockStorageService).getBlockByHeight(100L);
    }
    
    @Test
    void testGetBlockByHash_Success() throws Exception {
        // Given
        Block mockBlock = createMockBlock();
        when(blockStorageService.getBlockByHash("test-hash")).thenReturn(mockBlock);
        
        // When
        BlockInfoDto result = rpcService.getBlockByHash("test-hash");
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.getHeight());
        assertEquals("test-hash", result.getHash());
        verify(blockStorageService).getBlockByHash("test-hash");
    }
    
    @Test
    void testGetBlockByHash_NotFound() throws Exception {
        // Given
        when(blockStorageService.getBlockByHash("test-hash")).thenReturn(null);
        
        // When & Then
        assertThrows(BlockNotFoundException.class, () -> rpcService.getBlockByHash("test-hash"));
        verify(blockStorageService).getBlockByHash("test-hash");
    }
    
    @Test
    void testGetBlocks_Success() throws Exception {
        // Given
        List<Block> mockBlocks = Arrays.asList(createMockBlock(), createMockBlock());
        when(blockStorageService.getBlocks(100L, 101L)).thenReturn(mockBlocks);
        
        // When
        List<BlockInfoDto> result = rpcService.getBlocks(100L, 101L);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(blockStorageService).getBlocks(100L, 101L);
    }
    
    @Test
    void testGetRecentBlocks_Success() throws Exception {
        // Given
        List<Block> mockBlocks = Arrays.asList(createMockBlock(), createMockBlock());
        when(blockStorageService.getRecentBlocks(5)).thenReturn(mockBlocks);
        
        // When
        List<BlockInfoDto> result = rpcService.getRecentBlocks(5);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(blockStorageService).getRecentBlocks(5);
    }
    
    @Test
    void testGetCurrentHeight_Success() throws Exception {
        // Given
        when(blockStorageService.getCurrentHeight()).thenReturn(100L);
        
        // When
        long result = rpcService.getCurrentHeight();
        
        // Then
        assertEquals(100L, result);
        verify(blockStorageService).getCurrentHeight();
    }
    
    @Test
    void testGetTransactionByHash_Success() throws Exception {
        // Given
        Transaction mockTransaction = createMockTransaction();
        when(transactionStorageService.getTransactionByHash("test-tx-hash")).thenReturn(mockTransaction);
        
        // When
        TransactionInfoDto result = rpcService.getTransactionByHash("test-tx-hash");
        
        // Then
        assertNotNull(result);
        assertEquals("test-tx-hash", result.getHash());
        assertEquals(1000L, result.getAmount());
        verify(transactionStorageService).getTransactionByHash("test-tx-hash");
    }
    
    @Test
    void testGetTransactionByHash_NotFound() throws Exception {
        // Given
        when(transactionStorageService.getTransactionByHash("test-tx-hash")).thenReturn(null);
        
        // When & Then
        assertThrows(TransactionNotFoundException.class, () -> rpcService.getTransactionByHash("test-tx-hash"));
        verify(transactionStorageService).getTransactionByHash("test-tx-hash");
    }
    
    @Test
    void testGetAccountInfo_Success() throws Exception {
        // Given
        AccountState mockAccountState = createMockAccountState();
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenReturn(mockAccountState);
        
        // When
        AccountInfoDto result = rpcService.getAccountInfo("test-public-key");
        
        // Then
        assertNotNull(result);
        assertEquals(1000L, result.getBalance());
        assertEquals(500L, result.getStakeAmount());
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetAccountInfo_NotFound() throws Exception {
        // Given
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenReturn(null);
        
        // When & Then
        assertThrows(AccountNotFoundException.class, () -> rpcService.getAccountInfo("test-public-key"));
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetAccountBalance_Success() throws Exception {
        // Given
        AccountState mockAccountState = createMockAccountState();
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenReturn(mockAccountState);
        
        // When
        long result = rpcService.getAccountBalance("test-public-key");
        
        // Then
        assertEquals(1000L, result);
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetAccountBalance_NotFound() throws Exception {
        // Given
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenReturn(null);
        
        // When
        long result = rpcService.getAccountBalance("test-public-key");
        
        // Then
        assertEquals(0L, result); // 账户不存在时返回0
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetAccountStake_Success() throws Exception {
        // Given
        AccountState mockAccountState = createMockAccountState();
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenReturn(mockAccountState);
        
        // When
        long result = rpcService.getAccountStake("test-public-key");
        
        // Then
        assertEquals(500L, result);
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetAccountStake_NotFound() throws Exception {
        // Given
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenReturn(null);
        
        // When
        long result = rpcService.getAccountStake("test-public-key");
        
        // Then
        assertEquals(0L, result); // 账户不存在时返回0
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetChainState_Success() throws Exception {
        // Given
        ChainState mockChainState = createMockChainState();
        when(stateStorageService.getChainState()).thenReturn(mockChainState);
        
        // When
        ChainStateDto result = rpcService.getChainState();
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.getCurrentHeight());
        assertEquals(1L, result.getCurrentRound());
        assertEquals(1000000L, result.getTotalSupply());
        verify(stateStorageService).getChainState();
    }
    
    @Test
    void testGetNetworkStats_Success() throws Exception {
        // When
        NetworkStatsDto result = rpcService.getNetworkStats();
        
        // Then
        assertNotNull(result);
        assertEquals(50, result.getTotalPeers());
        assertEquals(25, result.getConnectedPeers());
        assertEquals(20, result.getActivePeers());
        assertEquals("1.0.0", result.getNetworkVersion());
    }
    
    // ==================== 错误处理和边界情况测试 ====================
    
    @Test
    void testGetLatestBlock_StorageException() throws Exception {
        // Given
        when(blockStorageService.getLatestBlock()).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getLatestBlock());
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get latest block"));
        verify(blockStorageService).getLatestBlock();
    }
    
    @Test
    void testGetBlockByHeight_StorageException() throws Exception {
        // Given
        when(blockStorageService.getBlockByHeight(100L)).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getBlockByHeight(100L));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get block by height: 100"));
        verify(blockStorageService).getBlockByHeight(100L);
    }
    
    @Test
    void testGetBlockByHash_StorageException() throws Exception {
        // Given
        when(blockStorageService.getBlockByHash("test-hash")).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getBlockByHash("test-hash"));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get block by hash: test-hash"));
        verify(blockStorageService).getBlockByHash("test-hash");
    }
    
    @Test
    void testGetBlocks_StorageException() throws Exception {
        // Given
        when(blockStorageService.getBlocks(100L, 101L)).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getBlocks(100L, 101L));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get blocks from 100 to 101"));
        verify(blockStorageService).getBlocks(100L, 101L);
    }
    
    @Test
    void testGetRecentBlocks_StorageException() throws Exception {
        // Given
        when(blockStorageService.getRecentBlocks(5)).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getRecentBlocks(5));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get recent blocks"));
        verify(blockStorageService).getRecentBlocks(5);
    }
    
    @Test
    void testGetCurrentHeight_StorageException() throws Exception {
        // Given
        when(blockStorageService.getCurrentHeight()).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getCurrentHeight());
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get current height"));
        verify(blockStorageService).getCurrentHeight();
    }
    
    @Test
    void testGetTransactionByHash_StorageException() throws Exception {
        // Given
        when(transactionStorageService.getTransactionByHash("test-tx-hash")).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getTransactionByHash("test-tx-hash"));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get transaction by hash: test-tx-hash"));
        verify(transactionStorageService).getTransactionByHash("test-tx-hash");
    }
    
    @Test
    void testGetAccountInfo_StorageException() throws Exception {
        // Given
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getAccountInfo("test-public-key"));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get account info: test-public-key"));
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetAccountBalance_StorageException() throws Exception {
        // Given
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getAccountBalance("test-public-key"));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get account balance: test-public-key"));
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetAccountStake_StorageException() throws Exception {
        // Given
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getAccountStake("test-public-key"));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get account stake: test-public-key"));
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetChainState_StorageException() throws Exception {
        // Given
        when(stateStorageService.getChainState()).thenThrow(new RuntimeException("Storage error"));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getChainState());
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to get chain state"));
        verify(stateStorageService).getChainState();
    }
    
    // ==================== 边界情况测试 ====================
    
    @Test
    void testGetBlocks_EmptyResult() throws Exception {
        // Given
        when(blockStorageService.getBlocks(100L, 101L)).thenReturn(Arrays.asList());
        
        // When
        List<BlockInfoDto> result = rpcService.getBlocks(100L, 101L);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blockStorageService).getBlocks(100L, 101L);
    }
    
    @Test
    void testGetRecentBlocks_EmptyResult() throws Exception {
        // Given
        when(blockStorageService.getRecentBlocks(5)).thenReturn(Arrays.asList());
        
        // When
        List<BlockInfoDto> result = rpcService.getRecentBlocks(5);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blockStorageService).getRecentBlocks(5);
    }
    
    @Test
    void testGetBlocks_NullResult() throws Exception {
        // Given
        when(blockStorageService.getBlocks(100L, 101L)).thenReturn(null);
        
        // When & Then - null结果会抛出异常
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getBlocks(100L, 101L));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        verify(blockStorageService).getBlocks(100L, 101L);
    }
    
    @Test
    void testGetRecentBlocks_NullResult() throws Exception {
        // Given
        when(blockStorageService.getRecentBlocks(5)).thenReturn(null);
        
        // When & Then - null结果会抛出异常
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.getRecentBlocks(5));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        verify(blockStorageService).getRecentBlocks(5);
    }
    
    @Test
    void testGetCurrentHeight_Zero() throws Exception {
        // Given
        when(blockStorageService.getCurrentHeight()).thenReturn(0L);
        
        // When
        long result = rpcService.getCurrentHeight();
        
        // Then
        assertEquals(0L, result);
        verify(blockStorageService).getCurrentHeight();
    }
    
    @Test
    void testGetAccountBalance_Zero() throws Exception {
        // Given
        AccountState mockAccountState = mock(AccountState.class);
        when(mockAccountState.getBalance()).thenReturn(0L);
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenReturn(mockAccountState);
        
        // When
        long result = rpcService.getAccountBalance("test-public-key");
        
        // Then
        assertEquals(0L, result);
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    @Test
    void testGetAccountStake_Zero() throws Exception {
        // Given
        AccountState mockAccountState = mock(AccountState.class);
        when(mockAccountState.getStakeAmount()).thenReturn(0L);
        when(stateStorageService.getAccountStateByPublicKeyHex("test-public-key")).thenReturn(mockAccountState);
        
        // When
        long result = rpcService.getAccountStake("test-public-key");
        
        // Then
        assertEquals(0L, result);
        verify(stateStorageService).getAccountStateByPublicKeyHex("test-public-key");
    }
    
    // ==================== 缺失方法测试 ====================
    
    @Test
    void testBroadcastTransaction_Success() throws Exception {
        // Given
        Transaction mockTransaction = createMockTransaction();
        when(networkService.broadcastTransaction(any(Transaction.class))).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
        
        // When
        String result = rpcService.broadcastTransaction(mockTransaction);
        
        // Then
        assertNotNull(result);
        assertEquals("test-tx-hash", result);
        verify(networkService).broadcastTransaction(mockTransaction);
        verify(transactionStorageService).storeTransaction(mockTransaction);
    }
    
    @Test
    void testBroadcastTransaction_NullTransaction() throws Exception {
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.broadcastTransaction(null));
        assertEquals(RpcError.INVALID_PARAMS, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Transaction cannot be null"));
    }
    
    @Test
    void testBroadcastTransaction_InvalidAmount() throws Exception {
        // Given
        Transaction mockTransaction = createMockTransaction();
        when(mockTransaction.getAmount()).thenReturn(0L);
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.broadcastTransaction(mockTransaction));
        assertEquals(RpcError.INVALID_PARAMS, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Transaction amount must be positive"));
    }
    
    @Test
    void testBroadcastTransaction_NegativeFee() throws Exception {
        // Given
        Transaction mockTransaction = createMockTransaction();
        when(mockTransaction.getFee()).thenReturn(-1L);
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.broadcastTransaction(mockTransaction));
        assertEquals(RpcError.INVALID_PARAMS, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Transaction fee cannot be negative"));
    }
    
    @Test
    void testBroadcastTransaction_NetworkError() throws Exception {
        // Given
        Transaction mockTransaction = createMockTransaction();
        when(networkService.broadcastTransaction(any(Transaction.class)))
            .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException("Network error")));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.broadcastTransaction(mockTransaction));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to broadcast transaction"));
    }
    
    @Test
    void testBroadcastTransaction_StorageError() throws Exception {
        // Given
        Transaction mockTransaction = createMockTransaction();
        when(networkService.broadcastTransaction(any(Transaction.class))).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
        doThrow(new RuntimeException("Storage error")).when(transactionStorageService).storeTransaction(any(Transaction.class));
        
        // When & Then
        RpcException exception = assertThrows(RpcException.class, () -> rpcService.broadcastTransaction(mockTransaction));
        assertEquals(RpcError.INTERNAL_ERROR, exception.getRpcError().getCode());
        assertTrue(exception.getMessage().contains("Failed to broadcast transaction"));
    }
    
    // ==================== 参数验证测试 ====================
    
    @Test
    void testGetBlockByHeight_NegativeHeight() throws Exception {
        // Given
        when(blockStorageService.getBlockByHeight(-1L)).thenReturn(null);
        
        // When & Then
        assertThrows(BlockNotFoundException.class, () -> rpcService.getBlockByHeight(-1L));
        verify(blockStorageService).getBlockByHeight(-1L);
    }
    
    @Test
    void testGetBlocks_InvalidRange() throws Exception {
        // Given
        when(blockStorageService.getBlocks(101L, 100L)).thenReturn(Arrays.asList());
        
        // When
        List<BlockInfoDto> result = rpcService.getBlocks(101L, 100L);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blockStorageService).getBlocks(101L, 100L);
    }
    
    @Test
    void testGetRecentBlocks_ZeroCount() throws Exception {
        // Given
        when(blockStorageService.getRecentBlocks(0)).thenReturn(Arrays.asList());
        
        // When
        List<BlockInfoDto> result = rpcService.getRecentBlocks(0);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blockStorageService).getRecentBlocks(0);
    }
    
    @Test
    void testGetRecentBlocks_NegativeCount() throws Exception {
        // Given
        when(blockStorageService.getRecentBlocks(-1)).thenReturn(Arrays.asList());
        
        // When
        List<BlockInfoDto> result = rpcService.getRecentBlocks(-1);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blockStorageService).getRecentBlocks(-1);
    }
    
    @Test
    void testGetAccountInfo_EmptyPublicKey() throws Exception {
        // When & Then - 空公钥会直接抛出异常，不会调用存储服务
        assertThrows(AccountNotFoundException.class, () -> rpcService.getAccountInfo(""));
        verify(stateStorageService, never()).getAccountStateByPublicKeyHex("");
    }
    
    @Test
    void testGetAccountBalance_EmptyPublicKey() throws Exception {
        // When - 空公钥会直接返回0，不会调用存储服务
        long result = rpcService.getAccountBalance("");
        
        // Then
        assertEquals(0L, result);
        verify(stateStorageService, never()).getAccountStateByPublicKeyHex("");
    }
    
    @Test
    void testGetAccountStake_EmptyPublicKey() throws Exception {
        // When - 空公钥会直接返回0，不会调用存储服务
        long result = rpcService.getAccountStake("");
        
        // Then
        assertEquals(0L, result);
        verify(stateStorageService, never()).getAccountStateByPublicKeyHex("");
    }
    
    // ==================== 转换方法测试 ====================
    
    @Test
    void testConvertToBlockInfoDto_WithVRFAnnouncements() throws Exception {
        // Given
        Block mockBlock = createMockBlockWithVRFAnnouncements();
        when(blockStorageService.getLatestBlock()).thenReturn(mockBlock);
        
        // When
        BlockInfoDto result = rpcService.getLatestBlock();
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getAllVrfAnnouncements());
        assertEquals(2, result.getAllVrfAnnouncements().size());
        assertEquals("test-vrf-output", result.getVrfOutput());
        verify(blockStorageService).getLatestBlock();
    }
    
    @Test
    void testConvertToBlockInfoDto_WithTransactions() throws Exception {
        // Given
        Block mockBlock = createMockBlockWithTransactions();
        when(blockStorageService.getLatestBlock()).thenReturn(mockBlock);
        
        // When
        BlockInfoDto result = rpcService.getLatestBlock();
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getTransactions());
        assertEquals(2, result.getTransactions().size());
        assertEquals(2, result.getTransactionCount());
        verify(blockStorageService).getLatestBlock();
    }
    
    @Test
    void testConvertToAccountInfoDto_SuperNode() throws Exception {
        // Given
        AccountState mockAccountState = createMockSuperNodeAccountState();
        when(stateStorageService.getAccountStateByPublicKeyHex("super-node-key")).thenReturn(mockAccountState);
        
        // When
        AccountInfoDto result = rpcService.getAccountInfo("super-node-key");
        
        // Then
        assertNotNull(result);
        assertTrue(result.getIsSuperNode());
        assertEquals(200000L, result.getStakeAmount());
        verify(stateStorageService).getAccountStateByPublicKeyHex("super-node-key");
    }
    
    @Test
    void testConvertToAccountInfoDto_RegularNode() throws Exception {
        // Given
        AccountState mockAccountState = createMockAccountState();
        when(stateStorageService.getAccountStateByPublicKeyHex("regular-key")).thenReturn(mockAccountState);
        
        // When
        AccountInfoDto result = rpcService.getAccountInfo("regular-key");
        
        // Then
        assertNotNull(result);
        assertFalse(result.getIsSuperNode());
        assertEquals(500L, result.getStakeAmount());
        verify(stateStorageService).getAccountStateByPublicKeyHex("regular-key");
    }
    
    @Test
    void testConvertToTransactionInfoDto_WithSignature() throws Exception {
        // Given
        Transaction mockTransaction = createMockTransactionWithSignature();
        when(transactionStorageService.getTransactionByHash("test-tx-hash")).thenReturn(mockTransaction);
        
        // When
        TransactionInfoDto result = rpcService.getTransactionByHash("test-tx-hash");
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getSignature());
        assertEquals("test-tx-hash", result.getHash());
        verify(transactionStorageService).getTransactionByHash("test-tx-hash");
    }
    
    @Test
    void testConvertToChainStateDto_CompleteData() throws Exception {
        // Given
        ChainState mockChainState = createMockChainState();
        when(stateStorageService.getChainState()).thenReturn(mockChainState);
        
        // When
        ChainStateDto result = rpcService.getChainState();
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.getCurrentHeight());
        assertEquals(1L, result.getCurrentRound());
        assertEquals(1000000L, result.getTotalSupply());
        assertEquals("1000000", result.getCurrentDifficulty());
        assertEquals(3000L, result.getBlockTime());
        assertEquals(50, result.getSuperNodeCount());
        assertEquals(20, result.getCoreNodeCount());
        assertEquals(30, result.getRotateNodeCount());
        assertEquals(15, result.getRewardedTopX());
        assertEquals("1.0.0", result.getConsensusVersion());
        assertEquals("equiflux-mainnet", result.getNetworkId());
        assertEquals("equiflux-chain", result.getChainId());
        verify(stateStorageService).getChainState();
    }
    
    @Test
    void testGetNetworkStats_CompleteData() throws Exception {
        // When
        NetworkStatsDto result = rpcService.getNetworkStats();
        
        // Then
        assertNotNull(result);
        assertEquals(50, result.getTotalPeers());
        assertEquals(25, result.getConnectedPeers());
        assertEquals(20, result.getActivePeers());
        assertEquals("1.0.0", result.getNetworkVersion());
        assertEquals("1.0.0", result.getProtocolVersion());
        assertTrue(result.getUptime() > 0);
    }
    
    // Helper methods
    
    private Block createMockBlock() {
        Block block = mock(Block.class);
        when(block.getHeight()).thenReturn(100L);
        when(block.getHashHex()).thenReturn("test-hash");
        when(block.getPreviousHashHex()).thenReturn("previous-hash");
        when(block.getTimestamp()).thenReturn(System.currentTimeMillis());
        when(block.getRound()).thenReturn(1);
        when(block.getProposerHex()).thenReturn("test-proposer");
        when(block.getVrfOutputHex()).thenReturn("test-vrf-output");
        when(block.getVrfProof()).thenReturn(mock(io.equiflux.node.model.VRFProof.class));
        when(block.getMerkleRootHex()).thenReturn("test-merkle-root");
        when(block.getNonce()).thenReturn(12345L);
        when(block.getDifficultyTarget()).thenReturn(java.math.BigInteger.valueOf(1000000));
        when(block.getTransactions()).thenReturn(Arrays.asList());
        when(block.getAllVRFAnnouncements()).thenReturn(Arrays.asList());
        return block;
    }
    
    private Transaction createMockTransaction() {
        Transaction transaction = mock(Transaction.class, RETURNS_DEEP_STUBS);
        lenient().when(transaction.getHashHex()).thenReturn("test-tx-hash");
        lenient().when(transaction.getSenderPublicKey()).thenReturn(new byte[32]);
        lenient().when(transaction.getReceiverPublicKey()).thenReturn(new byte[32]);
        lenient().when(transaction.getAmount()).thenReturn(1000L);
        lenient().when(transaction.getFee()).thenReturn(10L);
        lenient().when(transaction.getNonce()).thenReturn(1L);
        lenient().when(transaction.getTimestamp()).thenReturn(System.currentTimeMillis());
        lenient().when(transaction.getSignature()).thenReturn(new byte[64]);
        return transaction;
    }
    
    private AccountState createMockAccountState() {
        AccountState accountState = mock(AccountState.class);
        when(accountState.getPublicKeyHex()).thenReturn("test-public-key");
        when(accountState.getBalance()).thenReturn(1000L);
        when(accountState.getStakeAmount()).thenReturn(500L);
        when(accountState.getNonce()).thenReturn(1L);
        // AccountState doesn't have getLastUpdated method, removing this line
        return accountState;
    }
    
    private ChainState createMockChainState() {
        ChainState chainState = mock(ChainState.class);
        when(chainState.getCurrentHeight()).thenReturn(100L);
        when(chainState.getCurrentRound()).thenReturn(1L);
        when(chainState.getTotalSupply()).thenReturn(1000000L);
        when(chainState.getCurrentDifficulty()).thenReturn(java.math.BigInteger.valueOf(1000000));
        return chainState;
    }
    
    private Block createMockBlockWithVRFAnnouncements() {
        Block block = createMockBlock();
        
        // 创建VRF公告
        VRFAnnouncement vrf1 = mock(VRFAnnouncement.class);
        when(vrf1.getPublicKeyHex()).thenReturn("node1");
        when(vrf1.getVrfOutput()).thenReturn(mock(io.equiflux.node.model.VRFOutput.class));
        when(vrf1.getVrfProof()).thenReturn(mock(io.equiflux.node.model.VRFProof.class));
        when(vrf1.getRound()).thenReturn(1L);
        when(vrf1.getTimestamp()).thenReturn(System.currentTimeMillis());
        when(vrf1.getScore()).thenReturn(0.8);
        
        VRFAnnouncement vrf2 = mock(VRFAnnouncement.class);
        when(vrf2.getPublicKeyHex()).thenReturn("node2");
        when(vrf2.getVrfOutput()).thenReturn(mock(io.equiflux.node.model.VRFOutput.class));
        when(vrf2.getVrfProof()).thenReturn(mock(io.equiflux.node.model.VRFProof.class));
        when(vrf2.getRound()).thenReturn(1L);
        when(vrf2.getTimestamp()).thenReturn(System.currentTimeMillis());
        when(vrf2.getScore()).thenReturn(0.9);
        
        when(block.getAllVRFAnnouncements()).thenReturn(Arrays.asList(vrf1, vrf2));
        when(block.getVrfOutputHex()).thenReturn("test-vrf-output");
        
        return block;
    }
    
    private Block createMockBlockWithTransactions() {
        Block block = createMockBlock();
        
        Transaction tx1 = createMockTransaction();
        Transaction tx2 = createMockTransaction();
        when(tx2.getHashHex()).thenReturn("test-tx-hash-2");
        
        when(block.getTransactions()).thenReturn(Arrays.asList(tx1, tx2));
        
        return block;
    }
    
    private AccountState createMockSuperNodeAccountState() {
        AccountState accountState = mock(AccountState.class);
        when(accountState.getPublicKeyHex()).thenReturn("super-node-key");
        when(accountState.getBalance()).thenReturn(1000000L);
        when(accountState.getStakeAmount()).thenReturn(200000L); // 超过100000，应该是超级节点
        when(accountState.getNonce()).thenReturn(1L);
        when(accountState.getLastUpdateTimestamp()).thenReturn(System.currentTimeMillis());
        return accountState;
    }
    
    private Transaction createMockTransactionWithSignature() {
        Transaction transaction = createMockTransaction();
        when(transaction.getSignature()).thenReturn(new byte[64]); // 64字节签名
        return transaction;
    }
}
