package io.equiflux.node.exception;

/**
 * 存储异常
 * 
 * <p>表示存储操作过程中发生的异常，包括数据库连接失败、数据序列化错误、存储空间不足等。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class StorageException extends RuntimeException {
    
    /**
     * 构造存储异常
     * 
     * @param message 异常消息
     */
    public StorageException(String message) {
        super(message);
    }
    
    /**
     * 构造存储异常
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造存储异常
     * 
     * @param cause 原因异常
     */
    public StorageException(Throwable cause) {
        super(cause);
    }
}
