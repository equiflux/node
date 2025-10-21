package io.equiflux.node.wallet.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 钱包信息
 * 
 * <p>包含钱包的基本信息和状态。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class WalletInfo {
    
    private final String publicKeyHex;
    private final String address;
    private final String name;
    private final WalletStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastUsedAt;
    private final boolean isEncrypted;
    
    /**
     * 构造钱包信息
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param address 钱包地址
     * @param name 钱包名称
     * @param status 钱包状态
     * @param createdAt 创建时间
     * @param lastUsedAt 最后使用时间
     * @param isEncrypted 是否加密
     */
    public WalletInfo(String publicKeyHex, String address, String name, 
                     WalletStatus status, LocalDateTime createdAt, 
                     LocalDateTime lastUsedAt, boolean isEncrypted) {
        this.publicKeyHex = Objects.requireNonNull(publicKeyHex, "Public key hex cannot be null");
        this.address = Objects.requireNonNull(address, "Address cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.lastUsedAt = lastUsedAt;
        this.isEncrypted = isEncrypted;
    }
    
    /**
     * 获取公钥十六进制字符串
     * 
     * @return 公钥十六进制字符串
     */
    public String getPublicKeyHex() {
        return publicKeyHex;
    }
    
    /**
     * 获取钱包地址
     * 
     * @return 钱包地址
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * 获取钱包名称
     * 
     * @return 钱包名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取钱包状态
     * 
     * @return 钱包状态
     */
    public WalletStatus getStatus() {
        return status;
    }
    
    /**
     * 获取创建时间
     * 
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * 获取最后使用时间
     * 
     * @return 最后使用时间
     */
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    /**
     * 是否加密
     * 
     * @return true如果加密，false否则
     */
    public boolean isEncrypted() {
        return isEncrypted;
    }
    
    /**
     * 创建新的钱包信息（更新最后使用时间）
     * 
     * @return 新的钱包信息
     */
    public WalletInfo updateLastUsedAt() {
        return new WalletInfo(publicKeyHex, address, name, status, 
                            createdAt, LocalDateTime.now(), isEncrypted);
    }
    
    /**
     * 创建新的钱包信息（更新状态）
     * 
     * @param newStatus 新状态
     * @return 新的钱包信息
     */
    public WalletInfo updateStatus(WalletStatus newStatus) {
        return new WalletInfo(publicKeyHex, address, name, newStatus, 
                            createdAt, lastUsedAt, isEncrypted);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WalletInfo that = (WalletInfo) obj;
        return Objects.equals(publicKeyHex, that.publicKeyHex);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(publicKeyHex);
    }
    
    @Override
    public String toString() {
        return "WalletInfo{" +
               "publicKeyHex='" + publicKeyHex + '\'' +
               ", address='" + address + '\'' +
               ", name='" + name + '\'' +
               ", status=" + status +
               ", createdAt=" + createdAt +
               ", lastUsedAt=" + lastUsedAt +
               ", isEncrypted=" + isEncrypted +
               '}';
    }
}
