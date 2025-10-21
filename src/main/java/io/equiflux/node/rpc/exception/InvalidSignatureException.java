package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;

/**
 * 无效签名异常
 * 
 * <p>当签名验证失败时抛出此异常。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class InvalidSignatureException extends RpcException {
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public InvalidSignatureException(String message) {
        super(RpcError.INVALID_SIGNATURE, message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因异常
     */
    public InvalidSignatureException(String message, Throwable cause) {
        super(RpcError.INVALID_SIGNATURE, message, cause);
    }
}
