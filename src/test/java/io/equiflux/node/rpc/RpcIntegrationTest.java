package io.equiflux.node.rpc;

import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.rpc.dto.*;
import io.equiflux.node.storage.BlockStorageService;
import io.equiflux.node.storage.StateStorageService;
import io.equiflux.node.storage.TransactionStorageService;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.storage.model.ChainState;
import io.equiflux.node.network.NetworkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RPC模块集成测试
 * 
 * <p>测试RPC模块的完整功能，包括服务层和控制器层的集成。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RpcIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @MockBean
    private BlockStorageService blockStorageService;
    
    @MockBean
    private StateStorageService stateStorageService;
    
    @MockBean
    private TransactionStorageService transactionStorageService;
    
    @MockBean
    private NetworkService networkService;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }
    
    // ==================== 完整业务流程测试 ====================
    
    @Test
    void testCompleteBlockQueryWorkflow() throws Exception {
        // Given - 准备测试数据
        Block mockBlock = createMockBlock();
        when(blockStorageService.getLatestBlock()).thenReturn(mockBlock);
        when(blockStorageService.getBlockByHeight(100L)).thenReturn(mockBlock);
        when(blockStorageService.getBlockByHash("test-hash")).thenReturn(mockBlock);
        when(blockStorageService.getCurrentHeight()).thenReturn(100L);
        
        // When & Then - 测试获取最新区块
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getLatestBlock", null, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.height").value(100))
                .andExpect(jsonPath("$.result.hash").value("test-hash"));
        
        // When & Then - 测试按高度获取区块
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getBlockByHeight", 
                    Map.of("height", 100), 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.height").value(100));
        
        // When & Then - 测试按哈希获取区块
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getBlockByHash", 
                    Map.of("hash", "test-hash"), 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hash").value("test-hash"));
        
        // When & Then - 测试获取当前高度
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getCurrentHeight", null, 4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(100));
        
        // 验证所有服务调用
        verify(blockStorageService).getLatestBlock();
        verify(blockStorageService).getBlockByHeight(100L);
        verify(blockStorageService).getBlockByHash("test-hash");
        verify(blockStorageService).getCurrentHeight();
    }
    
    @Test
    void testCompleteAccountQueryWorkflow() throws Exception {
        // Given - 准备测试数据
        AccountState mockAccount = createMockAccountState();
        when(stateStorageService.getAccountStateByPublicKeyHex("test-key")).thenReturn(mockAccount);
        
        // When & Then - 测试获取账户信息
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getAccountInfo", 
                    Map.of("publicKey", "test-key"), 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.publicKey").value("test-key"))
                .andExpect(jsonPath("$.result.balance").value(1000));
        
        // When & Then - 测试获取账户余额
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getAccountBalance", 
                    Map.of("publicKey", "test-key"), 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1000));
        
        // When & Then - 测试获取账户质押
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getAccountStake", 
                    Map.of("publicKey", "test-key"), 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(500));
        
        // 验证服务调用
        verify(stateStorageService, times(3)).getAccountStateByPublicKeyHex("test-key");
    }
    
    @Test
    void testCompleteTransactionWorkflow() throws Exception {
        // Given - 准备测试数据
        Transaction mockTransaction = createMockTransaction();
        when(transactionStorageService.getTransactionByHash("test-tx-hash")).thenReturn(mockTransaction);
        when(networkService.broadcastTransaction(any(Transaction.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        // When & Then - 测试获取交易
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getTransactionByHash", 
                    Map.of("hash", "test-tx-hash"), 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hash").value("test-tx-hash"))
                .andExpect(jsonPath("$.result.amount").value(1000));
        
        // When & Then - 测试广播交易
        Map<String, Object> transactionData = Map.of(
            "from", "test-from",
            "to", "test-to", 
            "amount", 1000,
            "fee", 10,
            "nonce", 1,
            "signature", "test-signature"
        );
        
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("broadcastTransaction", 
                    Map.of("transaction", transactionData), 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("test-tx-hash"));
        
        // 验证服务调用
        verify(transactionStorageService).getTransactionByHash("test-tx-hash");
        verify(networkService).broadcastTransaction(any(Transaction.class));
        verify(transactionStorageService).storeTransaction(any(Transaction.class));
    }
    
    @Test
    void testCompleteChainStateWorkflow() throws Exception {
        // Given - 准备测试数据
        ChainState mockChainState = createMockChainState();
        when(stateStorageService.getChainState()).thenReturn(mockChainState);
        
        // When & Then - 测试获取链状态
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getChainState", null, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentHeight").value(100))
                .andExpect(jsonPath("$.result.totalSupply").value(1000000))
                .andExpect(jsonPath("$.result.superNodeCount").value(50));
        
        // 验证服务调用
        verify(stateStorageService).getChainState();
    }
    
    // ==================== 错误处理集成测试 ====================
    
    @Test
    void testErrorHandlingWorkflow() throws Exception {
        // Given - 准备错误场景
        when(blockStorageService.getBlockByHeight(999999L)).thenReturn(null);
        when(transactionStorageService.getTransactionByHash("non-existent")).thenReturn(null);
        when(stateStorageService.getAccountStateByPublicKeyHex("non-existent")).thenReturn(null);
        
        // When & Then - 测试区块不存在错误
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getBlockByHeight", 
                    Map.of("height", 999999L), 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.BLOCK_NOT_FOUND));
        
        // When & Then - 测试交易不存在错误
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getTransactionByHash", 
                    Map.of("hash", "non-existent"), 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.TRANSACTION_NOT_FOUND));
        
        // When & Then - 测试账户不存在错误
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getAccountInfo", 
                    Map.of("publicKey", "non-existent"), 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.ACCOUNT_NOT_FOUND));
        
        // 验证服务调用
        verify(blockStorageService).getBlockByHeight(999999L);
        verify(transactionStorageService).getTransactionByHash("non-existent");
        verify(stateStorageService).getAccountStateByPublicKeyHex("non-existent");
    }
    
    // ==================== 批量请求集成测试 ====================
    
    @Test
    void testBatchRequestWorkflow() throws Exception {
        // Given - 准备测试数据
        when(blockStorageService.getCurrentHeight()).thenReturn(100L);
        when(stateStorageService.getChainState()).thenReturn(createMockChainState());
        
        // 创建批量请求
        String batchRequest = String.format("[%s, %s]", 
            createRpcRequest("getCurrentHeight", null, 1),
            createRpcRequest("getChainState", null, 2));
        
        // When & Then - 测试批量请求
        mockMvc.perform(post("/rpc/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].result").value(100))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].result.currentHeight").value(100))
                .andExpect(jsonPath("$[1].id").value(2));
        
        // 验证服务调用
        verify(blockStorageService).getCurrentHeight();
        verify(stateStorageService).getChainState();
    }
    
    @Test
    void testBatchRequestWithMixedResults() throws Exception {
        // Given - 准备混合结果
        when(blockStorageService.getCurrentHeight()).thenReturn(100L);
        
        // 创建包含成功和失败的批量请求
        String batchRequest = String.format("[%s, %s]", 
            createRpcRequest("getCurrentHeight", null, 1),
            createRpcRequest("unknownMethod", null, 2));
        
        // When & Then - 测试混合结果
        mockMvc.perform(post("/rpc/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].result").value(100))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].error.code").value(RpcError.METHOD_NOT_FOUND))
                .andExpect(jsonPath("$[1].id").value(2));
        
        // 验证服务调用
        verify(blockStorageService).getCurrentHeight();
    }
    
    // ==================== 健康检查和元数据测试 ====================
    
    @Test
    void testHealthAndMetadataEndpoints() throws Exception {
        // When & Then - 测试健康检查
        mockMvc.perform(get("/rpc/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Equiflux RPC"));
        
        // When & Then - 测试支持的方法
        mockMvc.perform(get("/rpc/methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockMethods").isArray())
                .andExpect(jsonPath("$.transactionMethods").isArray())
                .andExpect(jsonPath("$.accountMethods").isArray())
                .andExpect(jsonPath("$.chainMethods").isArray())
                .andExpect(jsonPath("$.networkMethods").isArray());
    }
    
    // ==================== 性能相关测试 ====================
    
    @Test
    void testConcurrentRequests() throws Exception {
        // Given - 准备测试数据
        when(blockStorageService.getCurrentHeight()).thenReturn(100L);
        
        // When & Then - 测试并发请求
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/rpc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRpcRequest("getCurrentHeight", null, i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(100));
        }
        
        // 验证服务调用次数
        verify(blockStorageService, times(10)).getCurrentHeight();
    }
    
    // Helper methods
    
    private String createRpcRequest(String method, Map<String, Object> params, int id) throws Exception {
        RpcRequest request = new RpcRequest(method, params, id);
        return objectMapper.writeValueAsString(request);
    }
    
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
        when(block.getDifficultyTarget()).thenReturn(BigInteger.valueOf(1000000));
        when(block.getTransactions()).thenReturn(Arrays.asList());
        when(block.getAllVRFAnnouncements()).thenReturn(Arrays.asList());
        return block;
    }
    
    private Transaction createMockTransaction() {
        Transaction transaction = mock(Transaction.class);
        when(transaction.getHashHex()).thenReturn("test-tx-hash");
        when(transaction.getSenderPublicKey()).thenReturn(new byte[32]);
        when(transaction.getReceiverPublicKey()).thenReturn(new byte[32]);
        when(transaction.getAmount()).thenReturn(1000L);
        when(transaction.getFee()).thenReturn(10L);
        when(transaction.getNonce()).thenReturn(1L);
        when(transaction.getTimestamp()).thenReturn(System.currentTimeMillis());
        when(transaction.getSignature()).thenReturn(new byte[64]);
        return transaction;
    }
    
    private AccountState createMockAccountState() {
        AccountState accountState = mock(AccountState.class);
        when(accountState.getPublicKeyHex()).thenReturn("test-key");
        when(accountState.getBalance()).thenReturn(1000L);
        when(accountState.getStakeAmount()).thenReturn(500L);
        when(accountState.getNonce()).thenReturn(1L);
        when(accountState.getLastUpdateTimestamp()).thenReturn(System.currentTimeMillis());
        return accountState;
    }
    
    private ChainState createMockChainState() {
        ChainState chainState = mock(ChainState.class);
        when(chainState.getCurrentHeight()).thenReturn(100L);
        when(chainState.getCurrentRound()).thenReturn(1L);
        when(chainState.getTotalSupply()).thenReturn(1000000L);
        when(chainState.getCurrentDifficulty()).thenReturn(BigInteger.valueOf(1000000));
        return chainState;
    }
}
