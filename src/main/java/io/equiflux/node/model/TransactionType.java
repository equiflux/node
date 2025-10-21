package io.equiflux.node.model;

/**
 * 交易类型枚举
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public enum TransactionType {
    
    /**
     * 转账交易
     */
    TRANSFER("transfer", "转账"),
    
    /**
     * 质押交易
     */
    STAKE("stake", "质押"),
    
    /**
     * 解质押交易
     */
    UNSTAKE("unstake", "解质押"),
    
    /**
     * 投票交易
     */
    VOTE("vote", "投票");
    
    private final String code;
    private final String description;
    
    TransactionType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取类型代码
     * 
     * @return 类型代码
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取类型描述
     * 
     * @return 类型描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取类型
     * 
     * @param code 类型代码
     * @return 交易类型
     */
    public static TransactionType fromCode(String code) {
        for (TransactionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
