package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC异常处理器
 * 
 * <p>全局处理RPC相关异常，将异常转换为标准的RPC错误响应。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@RestControllerAdvice
public class RpcExceptionHandler {
    
    /**
     * 处理RPC异常
     * 
     * @param ex RPC异常
     * @return RPC错误响应
     */
    @ExceptionHandler(RpcException.class)
    public ResponseEntity<Map<String, Object>> handleRpcException(RpcException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");
        
        // 将RpcError转换为Map结构
        Map<String, Object> error = new HashMap<>();
        error.put("code", ex.getRpcError().getCode());
        error.put("message", ex.getRpcError().getMessage());
        if (ex.getRpcError().getData() != null) {
            error.put("data", ex.getRpcError().getData());
        }
        
        response.put("error", error);
        response.put("id", null); // 对于异常情况，ID可能为null
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    /**
     * 处理区块未找到异常
     * 
     * @param ex 区块未找到异常
     * @return RPC错误响应
     */
    @ExceptionHandler(BlockNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBlockNotFoundException(BlockNotFoundException ex) {
        return handleRpcException(ex);
    }
    
    /**
     * 处理交易未找到异常
     * 
     * @param ex 交易未找到异常
     * @return RPC错误响应
     */
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionNotFoundException(TransactionNotFoundException ex) {
        return handleRpcException(ex);
    }
    
    /**
     * 处理账户未找到异常
     * 
     * @param ex 账户未找到异常
     * @return RPC错误响应
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotFoundException(AccountNotFoundException ex) {
        return handleRpcException(ex);
    }
    
    /**
     * 处理余额不足异常
     * 
     * @param ex 余额不足异常
     * @return RPC错误响应
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        return handleRpcException(ex);
    }
    
    /**
     * 处理无效签名异常
     * 
     * @param ex 无效签名异常
     * @return RPC错误响应
     */
    @ExceptionHandler(InvalidSignatureException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidSignatureException(InvalidSignatureException ex) {
        return handleRpcException(ex);
    }
    
    /**
     * 处理方法参数验证异常 (JSON-RPC 2.0规范：验证错误应返回HTTP 200 + error对象)
     *
     * @param ex 方法参数验证异常
     * @return RPC错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
            .map(error -> error.getDefaultMessage())
            .findFirst()
            .orElse("Invalid request parameters");

        // 尝试从请求对象中获取ID
        Object requestId = null;
        try {
            Object target = ex.getBindingResult().getTarget();
            if (target instanceof io.equiflux.node.rpc.dto.RpcRequest) {
                requestId = ((io.equiflux.node.rpc.dto.RpcRequest) target).getId();
            }
        } catch (Exception ignored) {
            // 如果无法获取ID，保持为null
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jsonrpc", "2.0");

        Map<String, Object> error = new HashMap<>();
        error.put("code", RpcError.INVALID_REQUEST);
        error.put("message", errorMessage);

        response.put("error", error);
        response.put("id", requestId);

        return ResponseEntity.status(org.springframework.http.HttpStatus.OK).body(response);
    }

    /**
     * 处理HTTP消息不可读异常 (如JSON格式错误)
     *
     * @param ex HTTP消息不可读异常
     * @return RPC错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        RpcException rpcEx = new RpcException(RpcError.PARSE_ERROR, "Invalid JSON format");
        return handleRpcException(rpcEx);
    }

    /**
     * 处理通用异常
     *
     * @param ex 通用异常
     * @return RPC错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        RpcException rpcEx = new RpcException(RpcError.INTERNAL_ERROR,
                                            "Internal server error: " + ex.getMessage(), ex);
        return handleRpcException(rpcEx);
    }
}
