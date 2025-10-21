package io.equiflux.node.wallet.model;

/**
 * 交易状态枚举
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public enum TransactionState {
    
    /**
     * 待处理
     */
    PENDING("pending", "待处理"),
    
    /**
     * 已确认
     */
    CONFIRMED("confirmed", "已确认"),
    
    /**
     * 失败
     */
    FAILED("failed", "失败"),
    
    /**
     * 已丢弃
     */
    DROPPED("dropped", "已丢弃");
    
    private final String code;
    private final String description;
    
    TransactionState(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取状态代码
     * 
     * @return 状态代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取状态
     * 
     * @param code 状态代码
     * @return 交易状态
     */
    public static TransactionState fromCode(String code) {
        for (TransactionState state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown transaction state code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
