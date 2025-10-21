package io.equiflux.node.wallet.rpc.dto;

import io.equiflux.node.wallet.model.WalletBackup;
import java.time.LocalDateTime;

/**
 * 钱包备份响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class WalletBackupResponseDto {
    
    private boolean success;
    private String message;
    private String publicKeyHex;
    private String walletName;
    private LocalDateTime backupTime;
    private String version;
    private String checksum;
    
    public WalletBackupResponseDto() {
        this.success = true;
    }
    
    public static WalletBackupResponseDto error(String message) {
        WalletBackupResponseDto dto = new WalletBackupResponseDto();
        dto.success = false;
        dto.message = message;
        return dto;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPublicKeyHex() {
        return publicKeyHex;
    }
    
    public void setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
    
    public String getWalletName() {
        return walletName;
    }
    
    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
    
    public LocalDateTime getBackupTime() {
        return backupTime;
    }
    
    public void setBackupTime(LocalDateTime backupTime) {
        this.backupTime = backupTime;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
