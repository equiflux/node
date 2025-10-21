package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;

/**
 * 交易未找到异常
 * 
 * <p>当请求的交易不存在时抛出此异常。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionNotFoundException extends RpcException {
    
    /**
     * 构造函数
     * 
     * @param hash 交易哈希
     */
    public TransactionNotFoundException(String hash) {
        super(RpcError.TRANSACTION_NOT_FOUND, hash);
    }
    
    /**
     * 构造函数
     * 
     * @param hash 交易哈希
     * @param cause 原因异常
     */
    public TransactionNotFoundException(String hash, Throwable cause) {
        super(RpcError.TRANSACTION_NOT_FOUND, hash, cause);
    }
}
