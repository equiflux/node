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
import io.equiflux.node.rpc.service.RpcService;
import io.equiflux.node.rpc.controller.RpcController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RPC模块性能测试
 * 
 * <p>测试RPC模块的性能表现，包括响应时间、并发处理能力等。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class RpcPerformanceTest {
    
    @Mock
    private BlockStorageService blockStorageService;
    
    @Mock
    private StateStorageService stateStorageService;
    
    @Mock
    private TransactionStorageService transactionStorageService;
    
    @Mock
    private NetworkService networkService;
    
    private RpcService rpcService;
    private RpcController rpcController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        rpcService = new RpcService(blockStorageService, stateStorageService, 
                                  transactionStorageService, networkService);
        rpcController = new RpcController(rpcService);
        mockMvc = MockMvcBuilders.standaloneSetup(rpcController).build();
        objectMapper = new ObjectMapper();
        
        // 设置默认的mock行为
        setupDefaultMocks();
    }
    
    private void setupDefaultMocks() {
        // 区块相关mock
        when(blockStorageService.getLatestBlock()).thenReturn(createMockBlock());
        when(blockStorageService.getBlockByHeight(anyLong())).thenReturn(createMockBlock());
        when(blockStorageService.getBlockByHash(anyString())).thenReturn(createMockBlock());
        when(blockStorageService.getCurrentHeight()).thenReturn(100L);
        when(blockStorageService.getBlocks(anyLong(), anyLong())).thenReturn(Arrays.asList(createMockBlock()));
        when(blockStorageService.getRecentBlocks(anyInt())).thenReturn(Arrays.asList(createMockBlock()));
        
        // 交易相关mock
        when(transactionStorageService.getTransactionByHash(anyString())).thenReturn(createMockTransaction());
        when(networkService.broadcastTransaction(any(Transaction.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        // 账户相关mock
        when(stateStorageService.getAccountStateByPublicKeyHex(anyString())).thenReturn(createMockAccountState());
        
        // 链状态相关mock
        when(stateStorageService.getChainState()).thenReturn(createMockChainState());
    }
    
    // ==================== 响应时间测试 ====================
    
    @Test
    void testResponseTime_SingleRequest() throws Exception {
        // Given
        int iterations = 100;
        long totalTime = 0;
        
        // When
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            
            mockMvc.perform(post("/rpc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRpcRequest("getCurrentHeight", null, i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(100));
            
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }
        
        // Then
        double averageTimeMs = (totalTime / iterations) / 1_000_000.0;
        System.out.printf("Average response time: %.2f ms%n", averageTimeMs);
        
        // 验证平均响应时间小于10ms
        assert averageTimeMs < 10.0 : "Average response time should be less than 10ms";
        
        verify(blockStorageService, times(iterations)).getCurrentHeight();
    }
    
    @Test
    void testResponseTime_DifferentMethods() throws Exception {
        // Given
        String[] methods = {
            "getCurrentHeight",
            "getLatestBlock", 
            "getChainState",
            "getNetworkStats"
        };
        
        // When & Then
        for (String method : methods) {
            long startTime = System.nanoTime();
            
            mockMvc.perform(post("/rpc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRpcRequest(method, null, 1)))
                    .andExpect(status().isOk());
            
            long endTime = System.nanoTime();
            double responseTimeMs = (endTime - startTime) / 1_000_000.0;
            
            System.out.printf("Method %s response time: %.2f ms%n", method, responseTimeMs);
            
            // 验证每个方法的响应时间都小于20ms
            assert responseTimeMs < 20.0 : "Response time for " + method + " should be less than 20ms";
        }
    }
    
    // ==================== 并发性能测试 ====================
    
    @Test
    void testConcurrentRequests_Performance() throws Exception {
        // Given
        int threadCount = 50;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        mockMvc.perform(post("/rpc")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRpcRequest("getCurrentHeight", null, threadId * 1000 + j)))
                                .andExpect(status().isOk());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("Request failed: " + e.getMessage());
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalRequests = threadCount * requestsPerThread;
        
        // Then
        System.out.printf("Concurrent test results:%n");
        System.out.printf("Total requests: %d%n", totalRequests);
        System.out.printf("Successful requests: %d%n", successCount.get());
        System.out.printf("Failed requests: %d%n", errorCount.get());
        System.out.printf("Total time: %d ms%n", totalTime);
        System.out.printf("Requests per second: %.2f%n", (double) totalRequests / totalTime * 1000);
        
        // 验证成功率大于95%
        double successRate = (double) successCount.get() / totalRequests;
        assert successRate > 0.95 : "Success rate should be greater than 95%";
        
        // 验证吞吐量大于100 RPS
        double rps = (double) totalRequests / totalTime * 1000;
        assert rps > 100 : "Requests per second should be greater than 100";
        
        verify(blockStorageService, times(successCount.get())).getCurrentHeight();
    }
    
    @Test
    void testConcurrentBatchRequests_Performance() throws Exception {
        // Given
        int threadCount = 20;
        int batchRequestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < batchRequestsPerThread; j++) {
                    try {
                        String batchRequest = String.format("[%s, %s]", 
                            createRpcRequest("getCurrentHeight", null, threadId * 1000 + j * 2),
                            createRpcRequest("getChainState", null, threadId * 1000 + j * 2 + 1));
                        
                        mockMvc.perform(post("/rpc/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(batchRequest))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(2));
                        
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        System.err.println("Batch request failed: " + e.getMessage());
                    }
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalBatchRequests = threadCount * batchRequestsPerThread;
        
        // Then
        System.out.printf("Batch concurrent test results:%n");
        System.out.printf("Total batch requests: %d%n", totalBatchRequests);
        System.out.printf("Successful batch requests: %d%n", successCount.get());
        System.out.printf("Total time: %d ms%n", totalTime);
        System.out.printf("Batch requests per second: %.2f%n", (double) totalBatchRequests / totalTime * 1000);
        
        // 验证成功率大于90%
        double successRate = (double) successCount.get() / totalBatchRequests;
        assert successRate > 0.90 : "Batch request success rate should be greater than 90%";
        
        verify(blockStorageService, times(successCount.get())).getCurrentHeight();
        verify(stateStorageService, times(successCount.get())).getChainState();
    }
    
    // ==================== 内存使用测试 ====================
    
    @Test
    void testMemoryUsage_LargeDataSets() throws Exception {
        // Given - 创建大量数据
        List<Block> largeBlockList = Arrays.asList(new Block[1000]);
        for (int i = 0; i < 1000; i++) {
            largeBlockList.set(i, createMockBlock());
        }
        
        when(blockStorageService.getBlocks(anyLong(), anyLong())).thenReturn(largeBlockList);
        
        // 记录初始内存使用
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // 强制垃圾回收
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // When - 执行大量数据请求
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/rpc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRpcRequest("getBlocks", 
                        Map.of("startHeight", 1, "endHeight", 1000), i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(1000));
        }
        
        // 记录最终内存使用
        runtime.gc(); // 强制垃圾回收
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Then
        System.out.printf("Memory usage test:%n");
        System.out.printf("Initial memory: %d bytes%n", initialMemory);
        System.out.printf("Final memory: %d bytes%n", finalMemory);
        System.out.printf("Memory increase: %d bytes%n", memoryIncrease);
        
        // 验证内存增长在合理范围内（小于100MB）
        assert memoryIncrease < 100 * 1024 * 1024 : "Memory increase should be less than 100MB";
        
        verify(blockStorageService, times(10)).getBlocks(anyLong(), anyLong());
    }
    
    // ==================== 错误处理性能测试 ====================
    
    @Test
    void testErrorHandlingPerformance() throws Exception {
        // Given - 设置错误场景
        when(blockStorageService.getBlockByHeight(999999L)).thenReturn(null);
        
        int errorRequests = 100;
        long totalTime = 0;
        
        // When
        for (int i = 0; i < errorRequests; i++) {
            long startTime = System.nanoTime();
            
            mockMvc.perform(post("/rpc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRpcRequest("getBlockByHeight", 
                        Map.of("height", 999999L), i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.error.code").value(RpcError.BLOCK_NOT_FOUND));
            
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }
        
        // Then
        double averageTimeMs = (totalTime / errorRequests) / 1_000_000.0;
        System.out.printf("Average error response time: %.2f ms%n", averageTimeMs);
        
        // 验证错误处理响应时间小于15ms
        assert averageTimeMs < 15.0 : "Error response time should be less than 15ms";
        
        verify(blockStorageService, times(errorRequests)).getBlockByHeight(999999L);
    }
    
    // ==================== 压力测试 ====================
    
    @Test
    void testStressTest_HighLoad() throws Exception {
        // Given
        int totalRequests = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(100);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        // When - 高负载测试
        for (int i = 0; i < totalRequests; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    // 随机选择不同的RPC方法
                    String method = requestId % 4 == 0 ? "getCurrentHeight" :
                                  requestId % 4 == 1 ? "getLatestBlock" :
                                  requestId % 4 == 2 ? "getChainState" : "getNetworkStats";
                    
                    mockMvc.perform(post("/rpc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createRpcRequest(method, null, requestId)))
                            .andExpect(status().isOk());
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Then
        System.out.printf("Stress test results:%n");
        System.out.printf("Total requests: %d%n", totalRequests);
        System.out.printf("Successful requests: %d%n", successCount.get());
        System.out.printf("Failed requests: %d%n", errorCount.get());
        System.out.printf("Total time: %d ms%n", totalTime);
        System.out.printf("Throughput: %.2f RPS%n", (double) totalRequests / totalTime * 1000);
        
        // 验证成功率大于90%
        double successRate = (double) successCount.get() / totalRequests;
        assert successRate > 0.90 : "Stress test success rate should be greater than 90%";
        
        // 验证吞吐量大于50 RPS
        double throughput = (double) totalRequests / totalTime * 1000;
        assert throughput > 50 : "Stress test throughput should be greater than 50 RPS";
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
