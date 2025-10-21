package io.equiflux.node.exception;

/**
 * 验证相关异常
 * 
 * <p>用于处理各种验证过程中的异常情况，如：
 * <ul>
 *   <li>区块验证失败</li>
 *   <li>交易验证失败</li>
 *   <li>VRF验证失败</li>
 *   <li>签名验证失败</li>
 *   <li>格式验证失败</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class ValidationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造验证异常
     * 
     * @param message 异常消息
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * 构造验证异常
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
