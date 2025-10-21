package io.equiflux.node.wallet.model;

/**
 * 钱包状态枚举
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public enum WalletStatus {
    
    /**
     * 已创建但未解锁
     */
    CREATED("created", "已创建"),
    
    /**
     * 已解锁，可以使用
     */
    UNLOCKED("unlocked", "已解锁"),
    
    /**
     * 已锁定，需要密码解锁
     */
    LOCKED("locked", "已锁定"),
    
    /**
     * 已禁用
     */
    DISABLED("disabled", "已禁用");
    
    private final String code;
    private final String description;
    
    WalletStatus(String code, String description) {
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
     * @return 钱包状态
     */
    public static WalletStatus fromCode(String code) {
        for (WalletStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown wallet status code: " + code);
    }
    
    @Override
    public String toString() {
        return code;
    }
}
