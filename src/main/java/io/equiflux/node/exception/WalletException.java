package io.equiflux.node.exception;

/**
 * 钱包异常
 * 
 * <p>钱包服务相关的异常类。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class WalletException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造钱包异常
     * 
     * @param message 异常消息
     */
    public WalletException(String message) {
        super(message);
    }
    
    /**
     * 构造钱包异常
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public WalletException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造钱包异常
     * 
     * @param cause 原因异常
     */
    public WalletException(Throwable cause) {
        super(cause);
    }
}
