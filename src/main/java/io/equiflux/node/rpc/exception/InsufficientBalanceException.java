package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;

/**
 * 余额不足异常
 * 
 * <p>当账户余额不足以执行操作时抛出此异常。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class InsufficientBalanceException extends RpcException {
    
    /**
     * 构造函数
     * 
     * @param publicKey 公钥
     * @param required 需要的金额
     * @param available 可用金额
     */
    public InsufficientBalanceException(String publicKey, long required, long available) {
        super(RpcError.INSUFFICIENT_BALANCE, 
              String.format("Insufficient balance: required %d, available %d", 
                           required, available),
              new BalanceInfo(required, available));
    }
    
    /**
     * 构造函数
     * 
     * @param publicKey 公钥
     * @param required 需要的金额
     * @param available 可用金额
     * @param cause 原因异常
     */
    public InsufficientBalanceException(String publicKey, long required, long available, Throwable cause) {
        super(RpcError.INSUFFICIENT_BALANCE, 
              String.format("Insufficient balance: required %d, available %d", 
                           required, available),
              new BalanceInfo(required, available),
              cause);
    }
    
    /**
     * 余额信息
     */
    public static class BalanceInfo {
        private final long required;
        private final long available;
        
        public BalanceInfo(long required, long available) {
            this.required = required;
            this.available = available;
        }
        
        public long getRequired() {
            return required;
        }
        
        public long getAvailable() {
            return available;
        }
        
        public long getShortage() {
            return required - available;
        }
    }
}
