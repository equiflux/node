package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;

/**
 * 账户未找到异常
 * 
 * <p>当请求的账户不存在时抛出此异常。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class AccountNotFoundException extends RpcException {
    
    /**
     * 构造函数
     * 
     * @param publicKey 公钥
     */
    public AccountNotFoundException(String publicKey) {
        super(RpcError.ACCOUNT_NOT_FOUND, publicKey);
    }
    
    /**
     * 构造函数
     * 
     * @param publicKey 公钥
     * @param cause 原因异常
     */
    public AccountNotFoundException(String publicKey, Throwable cause) {
        super(RpcError.ACCOUNT_NOT_FOUND, publicKey, cause);
    }
}
