package io.equiflux.node.rpc.controller;

import io.equiflux.node.model.Transaction;
import io.equiflux.node.rpc.dto.*;
import io.equiflux.node.rpc.service.RpcService;
import io.equiflux.node.rpc.exception.RpcException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RPC控制器
 * 
 * <p>实现JSON-RPC 2.0规范的RPC接口，提供区块链数据的查询和操作功能。
 * 
 * <p>支持的RPC方法：
 * <ul>
 *   <li>区块相关：getLatestBlock, getBlockByHeight, getBlockByHash, getBlocks, getRecentBlocks</li>
 *   <li>交易相关：getTransactionByHash, broadcastTransaction</li>
 *   <li>账户相关：getAccountInfo, getAccountBalance, getAccountStake</li>
 *   <li>链状态相关：getChainState, getCurrentHeight</li>
 *   <li>网络相关：getNetworkStats</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/rpc")
@CrossOrigin(origins = "*")
public class RpcController {
    
    private static final Logger logger = LoggerFactory.getLogger(RpcController.class);
    
    private final RpcService rpcService;
    
    public RpcController(RpcService rpcService) {
        this.rpcService = rpcService;
    }
    
    /**
     * 处理RPC请求
     * 
     * @param request RPC请求
     * @return RPC响应
     */
    @PostMapping
    public ResponseEntity<RpcResponse> handleRpcRequest(@Valid @RequestBody RpcRequest request) {
        try {
            logger.debug("Received RPC request: method={}, id={}", request.getMethod(), request.getId());

            // 验证 JSON-RPC 版本
            if (request.getJsonrpc() == null || request.getJsonrpc().trim().isEmpty()) {
                throw new RpcException(RpcError.INVALID_REQUEST, "JSON-RPC version is required");
            }
            if (!"2.0".equals(request.getJsonrpc())) {
                throw new RpcException(RpcError.INVALID_REQUEST, "Unsupported JSON-RPC version: " + request.getJsonrpc());
            }

            RpcResponse response = processRpcMethod(request.getMethod(), request.getParamsAsMap(), request.getId());

            logger.debug("RPC request processed successfully: method={}, id={}", request.getMethod(), request.getId());
            return ResponseEntity.ok(response);

        } catch (RpcException e) {
            logger.warn("RPC request failed: method={}, error={}", request.getMethod(), e.getMessage());
            return ResponseEntity.ok(e.toRpcResponse(request.getId()));
        } catch (Exception e) {
            logger.error("Unexpected error processing RPC request: method={}", request.getMethod(), e);
            RpcResponse response = RpcResponse.error(RpcError.INTERNAL_ERROR,
                                                   "Internal server error: " + e.getMessage(),
                                                   request.getId());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 处理批量RPC请求
     * 
     * @param requests RPC请求列表
     * @return RPC响应列表
     */
    @PostMapping("/batch")
    public ResponseEntity<List<RpcResponse>> handleBatchRpcRequest(@Valid @RequestBody List<RpcRequest> requests) {
        try {
            logger.debug("Received batch RPC request with {} requests", requests.size());
            
            List<RpcResponse> responses = requests.stream()
                    .map(request -> {
                        try {
                            return processRpcMethod(request.getMethod(), request.getParamsAsMap(), request.getId());
                        } catch (RpcException e) {
                            return e.toRpcResponse(request.getId());
                        } catch (Exception e) {
                            return RpcResponse.error(RpcError.INTERNAL_ERROR, 
                                                  "Internal server error: " + e.getMessage(), 
                                                  request.getId());
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            logger.debug("Batch RPC request processed successfully");
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Unexpected error processing batch RPC request", e);
            throw new RuntimeException("Failed to process batch RPC request", e);
        }
    }
    
    /**
     * 处理RPC方法调用
     * 
     * @param method 方法名
     * @param params 参数
     * @return 方法结果
     * @throws RpcException RPC异常
     */
    private RpcResponse processRpcMethod(String method, Map<String, Object> params, Object requestId) throws RpcException {
        switch (method) {
            // 区块相关方法
            case "getLatestBlock":
                return RpcResponse.success(rpcService.getLatestBlock(), requestId);
                
            case "getBlockByHeight":
                Long height = getLongParam(params, "height");
                return RpcResponse.success(rpcService.getBlockByHeight(height), requestId);
                
            case "getBlockByHash":
                String hash = getStringParam(params, "hash");
                return RpcResponse.success(rpcService.getBlockByHash(hash), requestId);
                
            case "getBlocks":
                Long startHeight = getLongParam(params, "startHeight");
                Long endHeight = getLongParam(params, "endHeight");
                return RpcResponse.success(rpcService.getBlocks(startHeight, endHeight), requestId);
                
            case "getRecentBlocks":
                Integer count = getIntegerParam(params, "count");
                return RpcResponse.success(rpcService.getRecentBlocks(count), requestId);
                
            case "getCurrentHeight":
                return RpcResponse.success(rpcService.getCurrentHeight(), requestId);
                
            // 交易相关方法
            case "getTransactionByHash":
                String txHash = getStringParam(params, "hash");
                return RpcResponse.success(rpcService.getTransactionByHash(txHash), requestId);
                
            case "broadcastTransaction":
                Map<String, Object> txData = getMapParam(params, "transaction");
                if (txData == null) {
                    throw new RpcException(RpcError.INVALID_PARAMS, "Missing required parameter: transaction");
                }
                try {
                    // 使用 ObjectMapper 将 Map 转换为 Transaction 对象
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    String jsonStr = mapper.writeValueAsString(txData);
                    Transaction tx = mapper.readValue(jsonStr, io.equiflux.node.model.Transaction.class);
                    String broadcastTxHash = rpcService.broadcastTransaction(tx);
                    return RpcResponse.success(broadcastTxHash, requestId);
                } catch (RpcException e) {
                    // 直接重新抛出 RpcException，不要包装
                    throw e;
                } catch (Exception e) {
                    logger.error("Failed to parse transaction data", e);
                    throw new RpcException(RpcError.INVALID_PARAMS, "Invalid transaction data: " + e.getMessage());
                }
                
            // 账户相关方法
            case "getAccountInfo":
                String publicKey = getStringParam(params, "publicKey");
                return RpcResponse.success(rpcService.getAccountInfo(publicKey), requestId);
                
            case "getAccountBalance":
                String balancePublicKey = getStringParam(params, "publicKey");
                return RpcResponse.success(rpcService.getAccountBalance(balancePublicKey), requestId);
                
            case "getAccountStake":
                String stakePublicKey = getStringParam(params, "publicKey");
                return RpcResponse.success(rpcService.getAccountStake(stakePublicKey), requestId);
                
            // 链状态相关方法
            case "getChainState":
                return RpcResponse.success(rpcService.getChainState(), requestId);
                
            // 网络相关方法
            case "getNetworkStats":
                return RpcResponse.success(rpcService.getNetworkStats(), requestId);
                
            // 默认情况
            default:
                throw new RpcException(RpcError.METHOD_NOT_FOUND, "Method not found: " + method);
        }
    }
    
    /**
     * 获取字符串参数
     */
    private String getStringParam(Map<String, Object> params, String key) throws RpcException {
        Object value = params.get(key);
        if (value == null) {
            throw new RpcException(RpcError.INVALID_PARAMS, "Missing required parameter: " + key);
        }
        if (!(value instanceof String)) {
            throw new RpcException(RpcError.INVALID_PARAMS, "Parameter " + key + " must be a string");
        }
        return (String) value;
    }
    
    /**
     * 获取长整型参数
     */
    private Long getLongParam(Map<String, Object> params, String key) throws RpcException {
        Object value = params.get(key);
        if (value == null) {
            throw new RpcException(RpcError.INVALID_PARAMS, "Missing required parameter: " + key);
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new RpcException(RpcError.INVALID_PARAMS, "Invalid parameter type: " + key + " must be a number");
            }
        }
        throw new RpcException(RpcError.INVALID_PARAMS, "Invalid parameter type: " + key + " must be a number");
    }
    
    /**
     * 获取整型参数
     */
    private Integer getIntegerParam(Map<String, Object> params, String key) throws RpcException {
        Object value = params.get(key);
        if (value == null) {
            throw new RpcException(RpcError.INVALID_PARAMS, "Missing required parameter: " + key);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new RpcException(RpcError.INVALID_PARAMS, "Parameter " + key + " must be a valid integer");
            }
        }
        throw new RpcException(RpcError.INVALID_PARAMS, "Parameter " + key + " must be an integer");
    }
    
    /**
     * 获取Map参数
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapParam(Map<String, Object> params, String key) throws RpcException {
        Object value = params.get(key);
        if (value == null) {
            return null; // 允许为空，由调用者检查
        }
        if (!(value instanceof Map)) {
            throw new RpcException(RpcError.INVALID_PARAMS, "Parameter " + key + " must be an object");
        }
        return (Map<String, Object>) value;
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "Equiflux RPC",
            "version", "1.0.0",
            "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.ok(health);
    }
    
    /**
     * 获取支持的RPC方法列表
     */
    @GetMapping("/methods")
    public ResponseEntity<Map<String, Object>> getSupportedMethods() {
        Map<String, Object> methods = Map.of(
            "blockMethods", List.of(
                "getLatestBlock",
                "getBlockByHeight",
                "getBlockByHash", 
                "getBlocks",
                "getRecentBlocks",
                "getCurrentHeight"
            ),
            "transactionMethods", List.of(
                "getTransactionByHash",
                "broadcastTransaction"
            ),
            "accountMethods", List.of(
                "getAccountInfo",
                "getAccountBalance",
                "getAccountStake"
            ),
            "chainMethods", List.of(
                "getChainState"
            ),
            "networkMethods", List.of(
                "getNetworkStats"
            )
        );
        return ResponseEntity.ok(methods);
    }
}
