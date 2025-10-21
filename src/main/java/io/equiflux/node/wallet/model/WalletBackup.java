package io.equiflux.node.wallet.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 钱包备份
 * 
 * <p>包含钱包的完整备份信息，用于钱包恢复。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class WalletBackup {
    
    private final String publicKeyHex;
    private final String encryptedPrivateKey;
    private final String walletName;
    private final LocalDateTime backupTime;
    private final String version;
    private final String checksum;
    
    /**
     * 构造钱包备份
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param encryptedPrivateKey 加密的私钥
     * @param walletName 钱包名称
     * @param backupTime 备份时间
     * @param version 版本
     * @param checksum 校验和
     */
    public WalletBackup(String publicKeyHex, String encryptedPrivateKey, 
                       String walletName, LocalDateTime backupTime, 
                       String version, String checksum) {
        this.publicKeyHex = Objects.requireNonNull(publicKeyHex, "Public key hex cannot be null");
        this.encryptedPrivateKey = Objects.requireNonNull(encryptedPrivateKey, "Encrypted private key cannot be null");
        this.walletName = Objects.requireNonNull(walletName, "Wallet name cannot be null");
        this.backupTime = Objects.requireNonNull(backupTime, "Backup time cannot be null");
        this.version = Objects.requireNonNull(version, "Version cannot be null");
        this.checksum = Objects.requireNonNull(checksum, "Checksum cannot be null");
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
     * 获取加密的私钥
     * 
     * @return 加密的私钥
     */
    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }
    
    /**
     * 获取钱包名称
     * 
     * @return 钱包名称
     */
    public String getWalletName() {
        return walletName;
    }
    
    /**
     * 获取备份时间
     * 
     * @return 备份时间
     */
    public LocalDateTime getBackupTime() {
        return backupTime;
    }
    
    /**
     * 获取版本
     * 
     * @return 版本
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 获取校验和
     * 
     * @return 校验和
     */
    public String getChecksum() {
        return checksum;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WalletBackup that = (WalletBackup) obj;
        return Objects.equals(publicKeyHex, that.publicKeyHex) &&
               Objects.equals(backupTime, that.backupTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(publicKeyHex, backupTime);
    }
    
    @Override
    public String toString() {
        return "WalletBackup{" +
               "publicKeyHex='" + publicKeyHex + '\'' +
               ", walletName='" + walletName + '\'' +
               ", backupTime=" + backupTime +
               ", version='" + version + '\'' +
               '}';
    }
}
