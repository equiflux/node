package io.equiflux.node.exception;

/**
 * 密码学相关异常
 * 
 * <p>用于处理密码学操作中的各种异常情况，如：
 * <ul>
 *   <li>密钥生成失败</li>
 *   <li>签名生成失败</li>
 *   <li>签名验证失败</li>
 *   <li>哈希计算失败</li>
 *   <li>VRF计算失败</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class CryptoException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造密码学异常
     * 
     * @param message 异常消息
     */
    public CryptoException(String message) {
        super(message);
    }
    
    /**
     * 构造密码学异常
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
