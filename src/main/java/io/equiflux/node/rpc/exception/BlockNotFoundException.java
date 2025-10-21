package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;

/**
 * 区块未找到异常
 * 
 * <p>当请求的区块不存在时抛出此异常。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BlockNotFoundException extends RpcException {
    
    /**
     * 构造函数
     * 
     * @param height 区块高度
     */
    public BlockNotFoundException(long height) {
        super(RpcError.BLOCK_NOT_FOUND, String.valueOf(height));
    }
    
    /**
     * 构造函数
     * 
     * @param hash 区块哈希
     */
    public BlockNotFoundException(String hash) {
        super(RpcError.BLOCK_NOT_FOUND, hash);
    }
    
    /**
     * 构造函数
     * 
     * @param height 区块高度
     * @param cause 原因异常
     */
    public BlockNotFoundException(long height, Throwable cause) {
        super(RpcError.BLOCK_NOT_FOUND, String.valueOf(height), cause);
    }
    
    /**
     * 构造函数
     * 
     * @param hash 区块哈希
     * @param cause 原因异常
     */
    public BlockNotFoundException(String hash, Throwable cause) {
        super(RpcError.BLOCK_NOT_FOUND, hash, cause);
    }
}
