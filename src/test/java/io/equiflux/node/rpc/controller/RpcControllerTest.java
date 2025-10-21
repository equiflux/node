package io.equiflux.node.rpc.controller;

import io.equiflux.node.rpc.dto.RpcRequest;
import io.equiflux.node.rpc.service.RpcService;
import io.equiflux.node.rpc.exception.RpcException;
import io.equiflux.node.rpc.dto.RpcError;
import io.equiflux.node.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.equiflux.node.rpc.exception.BlockNotFoundException;
import io.equiflux.node.rpc.exception.TransactionNotFoundException;
import io.equiflux.node.rpc.exception.AccountNotFoundException;

/**
 * RPC控制器测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class RpcControllerTest {
    
    @Mock
    private RpcService rpcService;
    
    @InjectMocks
    private RpcController rpcController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rpcController).build();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testHandleRpcRequest_Success() throws Exception {
        // Given
        RpcRequest request = new RpcRequest("getCurrentHeight", null, 1);
        long expectedHeight = 100L;
        
        when(rpcService.getCurrentHeight()).thenReturn(expectedHeight);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.result").value(expectedHeight))
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService).getCurrentHeight();
    }
    
    @Test
    void testHandleRpcRequest_WithParams() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("height", 100L);
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 2);
        
        when(rpcService.getBlockByHeight(100L)).thenReturn(createMockBlockInfo());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.id").value(2));
        
        verify(rpcService).getBlockByHeight(100L);
    }
    
    @Test
    void testHandleRpcRequest_MethodNotFound() throws Exception {
        // Given
        RpcRequest request = new RpcRequest("unknownMethod", null, 3);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.METHOD_NOT_FOUND))
                .andExpect(jsonPath("$.error.message").value("Method not found: unknownMethod"))
                .andExpect(jsonPath("$.id").value(3));
        
        verify(rpcService, never()).getCurrentHeight();
    }
    
    @Test
    void testHandleRpcRequest_InvalidParams() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("invalidParam", "value");
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 4);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_PARAMS))
                .andExpect(jsonPath("$.error.message").value("Missing required parameter: height"))
                .andExpect(jsonPath("$.id").value(4));
        
        verify(rpcService, never()).getBlockByHeight(anyLong());
    }
    
    @Test
    void testHandleRpcRequest_ServiceException() throws Exception {
        // Given
        RpcRequest request = new RpcRequest("getCurrentHeight", null, 5);
        
        when(rpcService.getCurrentHeight()).thenThrow(new RpcException(RpcError.INTERNAL_ERROR, "Service error"));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INTERNAL_ERROR))
                .andExpect(jsonPath("$.error.message").value("Service error"))
                .andExpect(jsonPath("$.id").value(5));
        
        verify(rpcService).getCurrentHeight();
    }
    
    @Test
    void testHandleBatchRpcRequest() throws Exception {
        // Given
        RpcRequest request1 = new RpcRequest("getCurrentHeight", null, 1);
        RpcRequest request2 = new RpcRequest("getCurrentHeight", null, 2);
        RpcRequest[] requests = {request1, request2};
        
        when(rpcService.getCurrentHeight()).thenReturn(100L);
        
        // When & Then
        mockMvc.perform(post("/rpc/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].result").value(100L))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].result").value(100L))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(rpcService, times(2)).getCurrentHeight();
    }
    
    @Test
    void testHealthEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/rpc/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Equiflux RPC"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void testGetSupportedMethods() throws Exception {
        // When & Then
        mockMvc.perform(get("/rpc/methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockMethods").isArray())
                .andExpect(jsonPath("$.transactionMethods").isArray())
                .andExpect(jsonPath("$.accountMethods").isArray())
                .andExpect(jsonPath("$.chainMethods").isArray())
                .andExpect(jsonPath("$.networkMethods").isArray());
    }
    
    @Test
    void testGetBlockByHeight() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("height", 100);
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 1);
        
        when(rpcService.getBlockByHeight(100L)).thenReturn(createMockBlockInfo());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.height").value(100))
                .andExpect(jsonPath("$.result.hash").value("test-hash"));
        
        verify(rpcService).getBlockByHeight(100L);
    }
    
    @Test
    void testGetAccountInfo() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("publicKey", "test-public-key");
        RpcRequest request = new RpcRequest("getAccountInfo", params, 1);
        
        when(rpcService.getAccountInfo("test-public-key")).thenReturn(createMockAccountInfo());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.publicKey").value("test-public-key"))
                .andExpect(jsonPath("$.result.balance").value(1000));
        
        verify(rpcService).getAccountInfo("test-public-key");
    }
    
    // ==================== 更多RPC方法测试 ====================
    
    @Test
    void testGetBlockByHash() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("hash", "test-hash");
        RpcRequest request = new RpcRequest("getBlockByHash", params, 1);
        
        when(rpcService.getBlockByHash("test-hash")).thenReturn(createMockBlockInfo());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hash").value("test-hash"))
                .andExpect(jsonPath("$.result.height").value(100));
        
        verify(rpcService).getBlockByHash("test-hash");
    }
    
    @Test
    void testGetBlocks() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("startHeight", 100);
        params.put("endHeight", 102);
        RpcRequest request = new RpcRequest("getBlocks", params, 1);
        
        when(rpcService.getBlocks(100L, 102L)).thenReturn(Arrays.asList(createMockBlockInfo(), createMockBlockInfo()));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2));
        
        verify(rpcService).getBlocks(100L, 102L);
    }
    
    @Test
    void testGetRecentBlocks() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("count", 5);
        RpcRequest request = new RpcRequest("getRecentBlocks", params, 1);
        
        when(rpcService.getRecentBlocks(5)).thenReturn(Arrays.asList(createMockBlockInfo(), createMockBlockInfo()));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2));
        
        verify(rpcService).getRecentBlocks(5);
    }
    
    @Test
    void testGetTransactionByHash() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("hash", "test-tx-hash");
        RpcRequest request = new RpcRequest("getTransactionByHash", params, 1);
        
        when(rpcService.getTransactionByHash("test-tx-hash")).thenReturn(createMockTransactionInfo());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.hash").value("test-tx-hash"))
                .andExpect(jsonPath("$.result.amount").value(1000));
        
        verify(rpcService).getTransactionByHash("test-tx-hash");
    }
    
    @Test
    void testGetAccountBalance() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("publicKey", "test-public-key");
        RpcRequest request = new RpcRequest("getAccountBalance", params, 1);
        
        when(rpcService.getAccountBalance("test-public-key")).thenReturn(1000L);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1000));
        
        verify(rpcService).getAccountBalance("test-public-key");
    }
    
    @Test
    void testGetAccountStake() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("publicKey", "test-public-key");
        RpcRequest request = new RpcRequest("getAccountStake", params, 1);
        
        when(rpcService.getAccountStake("test-public-key")).thenReturn(500L);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(500));
        
        verify(rpcService).getAccountStake("test-public-key");
    }
    
    @Test
    void testGetChainState() throws Exception {
        // Given
        RpcRequest request = new RpcRequest("getChainState", null, 1);
        
        when(rpcService.getChainState()).thenReturn(createMockChainState());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentHeight").value(100))
                .andExpect(jsonPath("$.result.totalSupply").value(1000000));
        
        verify(rpcService).getChainState();
    }
    
    @Test
    void testGetNetworkStats() throws Exception {
        // Given
        RpcRequest request = new RpcRequest("getNetworkStats", null, 1);
        
        when(rpcService.getNetworkStats()).thenReturn(createMockNetworkStats());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalPeers").value(100))
                .andExpect(jsonPath("$.result.connectedPeers").value(50));
        
        verify(rpcService).getNetworkStats();
    }
    
    // ==================== 更多RPC方法测试 ====================
    
    @Test
    void testGetLatestBlock() throws Exception {
        // Given
        RpcRequest request = new RpcRequest("getLatestBlock", null, 1);
        
        when(rpcService.getLatestBlock()).thenReturn(createMockBlockInfo());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.height").value(100))
                .andExpect(jsonPath("$.result.hash").value("test-hash"));
        
        verify(rpcService).getLatestBlock();
    }
    
    @Test
    void testBroadcastTransaction() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("from", "test-from");
        transactionData.put("to", "test-to");
        transactionData.put("amount", 1000);
        transactionData.put("fee", 10);
        transactionData.put("nonce", 1);
        transactionData.put("signature", "test-signature");
        params.put("transaction", transactionData);
        
        RpcRequest request = new RpcRequest("broadcastTransaction", params, 1);
        
        when(rpcService.broadcastTransaction(any(Transaction.class))).thenReturn("test-tx-hash");
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("test-tx-hash"));
        
        verify(rpcService).broadcastTransaction(any(Transaction.class));
    }
    
    @Test
    void testBroadcastTransaction_InvalidTransaction() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("amount", 0); // 无效金额
        params.put("transaction", transactionData);
        
        RpcRequest request = new RpcRequest("broadcastTransaction", params, 1);
        
        when(rpcService.broadcastTransaction(any(Transaction.class)))
            .thenThrow(new RpcException(RpcError.INVALID_PARAMS, "Transaction amount must be positive"));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_PARAMS))
                .andExpect(jsonPath("$.error.message").value("Transaction amount must be positive"));
        
        verify(rpcService).broadcastTransaction(any(Transaction.class));
    }
    
    @Test
    void testBroadcastTransaction_MissingTransaction() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        // 故意不提供transaction参数
        RpcRequest request = new RpcRequest("broadcastTransaction", params, 1);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_PARAMS))
                .andExpect(jsonPath("$.error.message").value("Missing required parameter: transaction"));
        
        verify(rpcService, never()).broadcastTransaction(any(Transaction.class));
    }
    
    // ==================== 错误处理测试 ====================
    
    @Test
    void testHandleRpcRequest_BlockNotFoundException() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("height", 999999L);
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 1);
        
        when(rpcService.getBlockByHeight(999999L)).thenThrow(new BlockNotFoundException(999999L));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.BLOCK_NOT_FOUND))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService).getBlockByHeight(999999L);
    }
    
    @Test
    void testHandleRpcRequest_TransactionNotFoundException() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("hash", "non-existent-hash");
        RpcRequest request = new RpcRequest("getTransactionByHash", params, 1);
        
        when(rpcService.getTransactionByHash("non-existent-hash")).thenThrow(new TransactionNotFoundException("non-existent-hash"));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.TRANSACTION_NOT_FOUND))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService).getTransactionByHash("non-existent-hash");
    }
    
    @Test
    void testHandleRpcRequest_AccountNotFoundException() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("publicKey", "non-existent-key");
        RpcRequest request = new RpcRequest("getAccountInfo", params, 1);
        
        when(rpcService.getAccountInfo("non-existent-key")).thenThrow(new AccountNotFoundException("non-existent-key"));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.ACCOUNT_NOT_FOUND))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService).getAccountInfo("non-existent-key");
    }
    
    @Test
    void testHandleRpcRequest_InternalError() throws Exception {
        // Given
        RpcRequest request = new RpcRequest("getCurrentHeight", null, 1);
        
        when(rpcService.getCurrentHeight()).thenThrow(new RuntimeException("Database connection failed"));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INTERNAL_ERROR))
                .andExpect(jsonPath("$.error.message").value("Internal server error: Database connection failed"))
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService).getCurrentHeight();
    }
    
    // ==================== 参数验证测试 ====================
    
    @Test
    void testHandleRpcRequest_MissingRequiredParam() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        // 故意不提供height参数
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 1);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_PARAMS))
                .andExpect(jsonPath("$.error.message").value("Missing required parameter: height"))
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService, never()).getBlockByHeight(anyLong());
    }
    
    @Test
    void testHandleRpcRequest_InvalidParamType() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("height", "not-a-number");
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 1);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_PARAMS))
                .andExpect(jsonPath("$.error.message").value("Invalid parameter type: height must be a number"))
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService, never()).getBlockByHeight(anyLong());
    }
    
    @Test
    void testHandleRpcRequest_EmptyMethod() throws Exception {
        // Given
        RpcRequest request = new RpcRequest("", null, 1);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_REQUEST))
                .andExpect(jsonPath("$.error.message").value("Method name cannot be empty"))
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService, never()).getCurrentHeight();
    }
    
    @Test
    void testHandleRpcRequest_NullMethod() throws Exception {
        // Given
        RpcRequest request = new RpcRequest(null, null, 1);
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_REQUEST))
                .andExpect(jsonPath("$.error.message").value("Method name is required"))
                .andExpect(jsonPath("$.id").value(1));
        
        verify(rpcService, never()).getCurrentHeight();
    }
    
    // ==================== 批量请求测试 ====================
    
    @Test
    void testHandleBatchRpcRequest_MixedResults() throws Exception {
        // Given
        RpcRequest request1 = new RpcRequest("getCurrentHeight", null, 1);
        RpcRequest request2 = new RpcRequest("unknownMethod", null, 2);
        RpcRequest[] requests = {request1, request2};
        
        when(rpcService.getCurrentHeight()).thenReturn(100L);
        
        // When & Then
        mockMvc.perform(post("/rpc/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].result").value(100L))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].error.code").value(RpcError.METHOD_NOT_FOUND))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(rpcService).getCurrentHeight();
    }
    
    @Test
    void testHandleBatchRpcRequest_EmptyArray() throws Exception {
        // Given
        RpcRequest[] requests = {};
        
        // When & Then
        mockMvc.perform(post("/rpc/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(rpcService, never()).getCurrentHeight();
    }
    
    @Test
    void testHandleBatchRpcRequest_NullArray() throws Exception {
        // Given
        RpcRequest[] requests = null;
        
        // When & Then
        mockMvc.perform(post("/rpc/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(rpcService, never()).getCurrentHeight();
    }
    
    // ==================== JSON解析测试 ====================
    
    @Test
    void testHandleRpcRequest_InvalidJson() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
        
        verify(rpcService, never()).getCurrentHeight();
    }
    
    @Test
    void testHandleRpcRequest_MissingJsonrpc() throws Exception {
        // Given
        String jsonWithoutJsonrpc = "{\"method\":\"getCurrentHeight\",\"id\":1}";
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithoutJsonrpc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_REQUEST))
                .andExpect(jsonPath("$.error.message").value("JSON-RPC version is required"));
        
        verify(rpcService, never()).getCurrentHeight();
    }
    
    @Test
    void testHandleRpcRequest_WrongJsonrpcVersion() throws Exception {
        // Given
        String jsonWithWrongVersion = "{\"jsonrpc\":\"1.0\",\"method\":\"getCurrentHeight\",\"id\":1}";
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithWrongVersion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.error.code").value(RpcError.INVALID_REQUEST))
                .andExpect(jsonPath("$.error.message").value("Unsupported JSON-RPC version: 1.0"));
        
        verify(rpcService, never()).getCurrentHeight();
    }
    
    // ==================== 更多边界情况测试 ====================
    
    @Test
    void testHandleRpcRequest_MaximumHeight() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("height", Long.MAX_VALUE);
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 1);
        
        when(rpcService.getBlockByHeight(Long.MAX_VALUE)).thenThrow(new BlockNotFoundException(Long.MAX_VALUE));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.BLOCK_NOT_FOUND));
        
        verify(rpcService).getBlockByHeight(Long.MAX_VALUE);
    }
    
    @Test
    void testHandleRpcRequest_ZeroHeight() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("height", 0);
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 1);
        
        when(rpcService.getBlockByHeight(0L)).thenReturn(createMockBlockInfo());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.height").value(100));
        
        verify(rpcService).getBlockByHeight(0L);
    }
    
    @Test
    void testHandleRpcRequest_VeryLongHash() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        String longHash = "a".repeat(1000); // 1000个字符的哈希
        params.put("hash", longHash);
        RpcRequest request = new RpcRequest("getBlockByHash", params, 1);
        
        when(rpcService.getBlockByHash(longHash)).thenThrow(new BlockNotFoundException(longHash));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.BLOCK_NOT_FOUND));
        
        verify(rpcService).getBlockByHash(longHash);
    }
    
    @Test
    void testHandleRpcRequest_EmptyHash() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("hash", "");
        RpcRequest request = new RpcRequest("getBlockByHash", params, 1);
        
        when(rpcService.getBlockByHash("")).thenThrow(new BlockNotFoundException(""));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.BLOCK_NOT_FOUND));
        
        verify(rpcService).getBlockByHash("");
    }
    
    @Test
    void testHandleRpcRequest_SpecialCharactersInPublicKey() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        String specialKey = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        params.put("publicKey", specialKey);
        RpcRequest request = new RpcRequest("getAccountInfo", params, 1);
        
        when(rpcService.getAccountInfo(specialKey)).thenThrow(new AccountNotFoundException(specialKey));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.ACCOUNT_NOT_FOUND));
        
        verify(rpcService).getAccountInfo(specialKey);
    }
    
    @Test
    void testHandleRpcRequest_UnicodeCharacters() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        String unicodeKey = "测试公钥🔑";
        params.put("publicKey", unicodeKey);
        RpcRequest request = new RpcRequest("getAccountInfo", params, 1);
        
        when(rpcService.getAccountInfo(unicodeKey)).thenThrow(new AccountNotFoundException(unicodeKey));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(RpcError.ACCOUNT_NOT_FOUND));
        
        verify(rpcService).getAccountInfo(unicodeKey);
    }
    
    @Test
    void testHandleRpcRequest_LargeBlockRange() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("startHeight", 1);
        params.put("endHeight", 10000);
        RpcRequest request = new RpcRequest("getBlocks", params, 1);
        
        when(rpcService.getBlocks(1L, 10000L)).thenReturn(Arrays.asList(createMockBlockInfo()));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray());
        
        verify(rpcService).getBlocks(1L, 10000L);
    }
    
    @Test
    void testHandleRpcRequest_LargeRecentBlocksCount() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("count", 1000);
        RpcRequest request = new RpcRequest("getRecentBlocks", params, 1);
        
        when(rpcService.getRecentBlocks(1000)).thenReturn(Arrays.asList(createMockBlockInfo()));
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray());
        
        verify(rpcService).getRecentBlocks(1000);
    }
    
    @Test
    void testHandleRpcRequest_NegativeRecentBlocksCount() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("count", -1);
        RpcRequest request = new RpcRequest("getRecentBlocks", params, 1);
        
        when(rpcService.getRecentBlocks(-1)).thenReturn(Arrays.asList());
        
        // When & Then
        mockMvc.perform(post("/rpc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray());
        
        verify(rpcService).getRecentBlocks(-1);
    }
    
    // Helper methods
    
    private io.equiflux.node.rpc.dto.BlockInfoDto createMockBlockInfo() {
        io.equiflux.node.rpc.dto.BlockInfoDto blockInfo = new io.equiflux.node.rpc.dto.BlockInfoDto();
        blockInfo.setHeight(100L);
        blockInfo.setHash("test-hash");
        blockInfo.setPreviousHash("previous-hash");
        blockInfo.setTimestamp(System.currentTimeMillis());
        blockInfo.setRound(1L);
        blockInfo.setProposer("test-proposer");
        blockInfo.setTransactionCount(5);
        return blockInfo;
    }
    
    private io.equiflux.node.rpc.dto.AccountInfoDto createMockAccountInfo() {
        io.equiflux.node.rpc.dto.AccountInfoDto accountInfo = new io.equiflux.node.rpc.dto.AccountInfoDto();
        accountInfo.setPublicKey("test-public-key");
        accountInfo.setBalance(1000L);
        accountInfo.setStakeAmount(500L);
        accountInfo.setNonce(1L);
        accountInfo.setIsSuperNode(false);
        return accountInfo;
    }
    
    private io.equiflux.node.rpc.dto.TransactionInfoDto createMockTransactionInfo() {
        io.equiflux.node.rpc.dto.TransactionInfoDto transactionInfo = new io.equiflux.node.rpc.dto.TransactionInfoDto();
        transactionInfo.setHash("test-tx-hash");
        transactionInfo.setFrom("test-from");
        transactionInfo.setTo("test-to");
        transactionInfo.setAmount(1000L);
        transactionInfo.setFee(10L);
        transactionInfo.setNonce(1L);
        transactionInfo.setTimestamp(System.currentTimeMillis());
        transactionInfo.setStatus("confirmed");
        return transactionInfo;
    }
    
    private io.equiflux.node.rpc.dto.ChainStateDto createMockChainState() {
        io.equiflux.node.rpc.dto.ChainStateDto chainState = new io.equiflux.node.rpc.dto.ChainStateDto();
        chainState.setCurrentHeight(100L);
        chainState.setCurrentRound(1L);
        chainState.setTotalSupply(1000000L);
        chainState.setCurrentDifficulty("1000000");
        chainState.setSuperNodeCount(50);
        chainState.setCoreNodeCount(20);
        chainState.setRotateNodeCount(30);
        return chainState;
    }
    
    private io.equiflux.node.rpc.dto.NetworkStatsDto createMockNetworkStats() {
        io.equiflux.node.rpc.dto.NetworkStatsDto networkStats = new io.equiflux.node.rpc.dto.NetworkStatsDto();
        networkStats.setTotalPeers(100);
        networkStats.setConnectedPeers(50);
        networkStats.setActivePeers(25);
        networkStats.setNetworkVersion("1.0.0");
        networkStats.setUptime(3600000L);
        networkStats.setBytesReceived(1000000L);
        networkStats.setBytesSent(500000L);
        networkStats.setMessagesReceived(10000L);
        networkStats.setMessagesSent(8000L);
        return networkStats;
    }
}
