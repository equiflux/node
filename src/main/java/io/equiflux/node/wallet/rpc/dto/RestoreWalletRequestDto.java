package io.equiflux.node.wallet.rpc.dto;

import io.equiflux.node.wallet.model.WalletBackup;

/**
 * 恢复钱包请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class RestoreWalletRequestDto {
    
    private WalletBackup backup;
    private String password;
    
    public RestoreWalletRequestDto() {}
    
    public RestoreWalletRequestDto(WalletBackup backup, String password) {
        this.backup = backup;
        this.password = password;
    }
    
    // Getters and Setters
    public WalletBackup getBackup() {
        return backup;
    }
    
    public void setBackup(WalletBackup backup) {
        this.backup = backup;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
