package io.equiflux.node.wallet.rpc.dto;

/**
 * 备份钱包请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BackupWalletRequestDto {
    
    private String publicKeyHex;
    private String password;
    
    public BackupWalletRequestDto() {}
    
    public BackupWalletRequestDto(String publicKeyHex, String password) {
        this.publicKeyHex = publicKeyHex;
        this.password = password;
    }
    
    // Getters and Setters
    public String getPublicKeyHex() {
        return publicKeyHex;
    }
    
    public void setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
