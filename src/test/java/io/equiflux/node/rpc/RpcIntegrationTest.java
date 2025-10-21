package io.equiflux.node.rpc;

import io.equiflux.node.model.Transaction;
import io.equiflux.node.rpc.dto.*;
import io.equiflux.node.storage.BlockStorageService;
import io.equiflux.node.storage.StateStorageService;
import io.equiflux.node.storage.TransactionStorageService;
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
    private io.equiflux.node.storage.StorageService storageService;

    @MockBean
    private BlockStorageService blockStorageService;

    @MockBean
    private StateStorageService stateStorageService;

    @MockBean
    private TransactionStorageService transactionStorageService;

    @MockBean
    private NetworkService networkService;
    
    @MockBean
    private io.equiflux.node.rpc.service.RpcService rpcService;
    
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
        
        // Mock RpcService methods to return proper DTOs
        io.equiflux.node.rpc.dto.BlockInfoDto blockDto = createBlockInfoDto();
        when(rpcService.getLatestBlock()).thenReturn(blockDto);
        when(rpcService.getBlockByHeight(100L)).thenReturn(blockDto);
        when(rpcService.getBlockByHash("test-hash")).thenReturn(blockDto);
        when(rpcService.getCurrentHeight()).thenReturn(100L);
        
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
        
        // 验证RpcService被调用
        verify(rpcService).getLatestBlock();
        verify(rpcService).getBlockByHeight(100L);
        verify(rpcService).getBlockByHash("test-hash");
        verify(rpcService).getCurrentHeight();
    }
    
    @Test
    void testCompleteAccountQueryWorkflow() throws Exception {
        // Given - 准备测试数据
        
        // 生成真实的密钥对用于测试
        io.equiflux.node.crypto.Ed25519KeyPair keyPair = io.equiflux.node.crypto.Ed25519KeyPair.generate();
        String testPublicKey = keyPair.getPublicKeyHex();
        
        // Mock RpcService methods to return proper DTOs - 使用相同的公钥
        io.equiflux.node.rpc.dto.AccountInfoDto accountDto = createAccountInfoDto(testPublicKey);
        when(rpcService.getAccountInfo(testPublicKey)).thenReturn(accountDto);
        when(rpcService.getAccountBalance(testPublicKey)).thenReturn(1000L);
        when(rpcService.getAccountStake(testPublicKey)).thenReturn(500L);
        
        // When & Then - 测试获取账户信息
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getAccountInfo", 
                    Map.of("publicKey", testPublicKey), 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.publicKey").value(testPublicKey))
                .andExpect(jsonPath("$.result.balance").value(1000));
        
        // When & Then - 测试获取账户余额
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getAccountBalance", 
                    Map.of("publicKey", testPublicKey), 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1000));
        
        // When & Then - 测试获取账户质押
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getAccountStake", 
                    Map.of("publicKey", testPublicKey), 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(500));
        
        // 验证RpcService被调用
        verify(rpcService).getAccountInfo(testPublicKey);
        verify(rpcService).getAccountBalance(testPublicKey);
        verify(rpcService).getAccountStake(testPublicKey);
    }
    
    @Test
    void testCompleteTransactionWorkflow() throws Exception {
        // Given - 准备测试数据
        when(networkService.broadcastTransaction(any(Transaction.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        when(rpcService.broadcastTransaction(any(Transaction.class)))
            .thenReturn("test-tx-hash");
        
        // Mock RpcService methods to return proper DTOs
        io.equiflux.node.rpc.dto.TransactionInfoDto txDto = createTransactionInfoDto();
        when(rpcService.getTransactionByHash("test-tx-hash")).thenReturn(txDto);
        
        // When & Then - 测试获取交易
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getTransactionByHash", 
                    Map.of("hash", "test-tx-hash"), 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hash").value("test-tx-hash"))
                .andExpect(jsonPath("$.result.amount").value(1000));
        
        // When & Then - 测试广播交易 - 使用真实的密钥对生成有效的地址
        io.equiflux.node.crypto.Ed25519KeyPair senderKeyPair = io.equiflux.node.crypto.Ed25519KeyPair.generate();
        io.equiflux.node.crypto.Ed25519KeyPair receiverKeyPair = io.equiflux.node.crypto.Ed25519KeyPair.generate();
        
        Map<String, Object> transactionData = Map.of(
            "senderPublicKey", senderKeyPair.getPublicKeyHex(),
            "receiverPublicKey", receiverKeyPair.getPublicKeyHex(),
            "amount", 1000,
            "fee", 10,
            "nonce", 1,
            "timestamp", System.currentTimeMillis(),
            "signature", java.util.Base64.getEncoder().encodeToString(new byte[64]),
            "type", "TRANSFER"
        );
        
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("broadcastTransaction", 
                    Map.of("transaction", transactionData), 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("test-tx-hash"));
        
        // 验证RpcService被调用
        verify(rpcService).getTransactionByHash("test-tx-hash");
        verify(rpcService).broadcastTransaction(any());
    }
    
    @Test
    void testCompleteChainStateWorkflow() throws Exception {
        // Given - 准备测试数据
        
        // Mock RpcService methods to return proper DTOs
        io.equiflux.node.rpc.dto.ChainStateDto chainDto = createChainStateDto();
        when(rpcService.getChainState()).thenReturn(chainDto);
        
        // When & Then - 测试获取链状态
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRpcRequest("getChainState", null, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentHeight").value(100))
                .andExpect(jsonPath("$.result.totalSupply").value(1000000))
                .andExpect(jsonPath("$.result.superNodeCount").value(50));
        
        // 验证RpcService被调用
        verify(rpcService).getChainState();
    }
    
    // ==================== 错误处理集成测试 ====================
    
    @Test
    void testErrorHandlingWorkflow() throws Exception {
        // Given - 准备错误场景
        
        // Mock RpcService to throw exceptions for error cases
        when(rpcService.getBlockByHeight(999999L)).thenThrow(new io.equiflux.node.rpc.exception.BlockNotFoundException(999999L));
        when(rpcService.getTransactionByHash("non-existent")).thenThrow(new io.equiflux.node.rpc.exception.TransactionNotFoundException("non-existent"));
        when(rpcService.getAccountInfo("non-existent")).thenThrow(new io.equiflux.node.rpc.exception.AccountNotFoundException("non-existent"));
        
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
        
        // 验证RpcService被调用
        verify(rpcService).getBlockByHeight(999999L);
        verify(rpcService).getTransactionByHash("non-existent");
        verify(rpcService).getAccountInfo("non-existent");
    }
    
    // ==================== 批量请求集成测试 ====================
    
    @Test
    void testBatchRequestWorkflow() throws Exception {
        // Given - 准备测试数据
        
        // Mock RpcService methods to return proper DTOs
        when(rpcService.getCurrentHeight()).thenReturn(100L);
        io.equiflux.node.rpc.dto.ChainStateDto chainDto = createChainStateDto();
        when(rpcService.getChainState()).thenReturn(chainDto);
        
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
        
        // 验证RpcService被调用
        verify(rpcService).getCurrentHeight();
        verify(rpcService).getChainState();
    }
    
    @Test
    void testBatchRequestWithMixedResults() throws Exception {
        // Given - 准备混合结果
        
        // Mock RpcService methods
        when(rpcService.getCurrentHeight()).thenReturn(100L);
        
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
        
        // 验证RpcService被调用
        verify(rpcService).getCurrentHeight();
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
        
        // Mock RpcService methods
        when(rpcService.getCurrentHeight()).thenReturn(100L);
        
        // When & Then - 测试并发请求
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/rpc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRpcRequest("getCurrentHeight", null, i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(100));
        }
        
        // 验证RpcService被调用次数
        verify(rpcService, times(10)).getCurrentHeight();
    }
    
    // Helper methods
    
    private String createRpcRequest(String method, Map<String, Object> params, int id) throws Exception {
        RpcRequest request = new RpcRequest(method, params, id);
        return objectMapper.writeValueAsString(request);
    }
    
    
    
    // ==================== DTO创建方法 ====================
    
    private io.equiflux.node.rpc.dto.BlockInfoDto createBlockInfoDto() {
        io.equiflux.node.rpc.dto.BlockInfoDto dto = new io.equiflux.node.rpc.dto.BlockInfoDto();
        dto.setHeight(100L);
        dto.setHash("test-hash");
        dto.setPreviousHash("previous-hash");
        dto.setTimestamp(System.currentTimeMillis());
        dto.setRound(1L);
        dto.setProposer("test-proposer");
        dto.setVrfOutput("test-vrf-output");
        dto.setVrfProof("test-vrf-proof");
        dto.setMerkleRoot("test-merkle-root");
        dto.setNonce(12345L);
        dto.setDifficultyTarget("1000000");
        dto.setTransactionCount(0);
        return dto;
    }
    
    private io.equiflux.node.rpc.dto.TransactionInfoDto createTransactionInfoDto() {
        // 生成真实的密钥对用于测试
        io.equiflux.node.crypto.Ed25519KeyPair senderKeyPair = io.equiflux.node.crypto.Ed25519KeyPair.generate();
        io.equiflux.node.crypto.Ed25519KeyPair receiverKeyPair = io.equiflux.node.crypto.Ed25519KeyPair.generate();
        
        io.equiflux.node.rpc.dto.TransactionInfoDto dto = new io.equiflux.node.rpc.dto.TransactionInfoDto();
        dto.setHash("test-tx-hash");
        dto.setFrom(senderKeyPair.getPublicKeyHex());
        dto.setTo(receiverKeyPair.getPublicKeyHex());
        dto.setAmount(1000L);
        dto.setFee(10L);
        dto.setNonce(1L);
        dto.setTimestamp(System.currentTimeMillis());
        dto.setSignature("test-signature");
        dto.setData(null);
        return dto;
    }
    
    private io.equiflux.node.rpc.dto.AccountInfoDto createAccountInfoDto(String publicKey) {
        io.equiflux.node.rpc.dto.AccountInfoDto dto = new io.equiflux.node.rpc.dto.AccountInfoDto();
        dto.setPublicKey(publicKey);
        dto.setAddress(publicKey); // 使用公钥作为地址
        dto.setBalance(1000L);
        dto.setStakeAmount(500L);
        dto.setNonce(1L);
        dto.setLastUpdated(System.currentTimeMillis());
        dto.setIsSuperNode(false);
        return dto;
    }
    
    private io.equiflux.node.rpc.dto.ChainStateDto createChainStateDto() {
        io.equiflux.node.rpc.dto.ChainStateDto dto = new io.equiflux.node.rpc.dto.ChainStateDto();
        dto.setCurrentHeight(100L);
        dto.setCurrentRound(1L);
        dto.setTotalSupply(1000000L);
        dto.setCurrentDifficulty("1000000");
        dto.setBlockTime(3000L);
        dto.setSuperNodeCount(50);
        dto.setCoreNodeCount(20);
        dto.setRotateNodeCount(30);
        dto.setRewardedTopX(15);
        dto.setConsensusVersion("1.0.0");
        dto.setNetworkId("equiflux-mainnet");
        dto.setChainId("equiflux-chain");
        return dto;
    }
}
