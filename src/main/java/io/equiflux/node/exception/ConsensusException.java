package io.equiflux.node.exception;

/**
 * 共识相关异常
 * 
 * <p>用于处理共识过程中的各种异常情况，如：
 * <ul>
 *   <li>VRF收集失败</li>
 *   <li>区块验证失败</li>
 *   <li>出块者选择失败</li>
 *   <li>共识超时</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class ConsensusException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 构造共识异常
     * 
     * @param message 异常消息
     */
    public ConsensusException(String message) {
        super(message);
    }
    
    /**
     * 构造共识异常
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ConsensusException(String message, Throwable cause) {
        super(message, cause);
    }
}
