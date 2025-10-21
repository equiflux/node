package io.equiflux.node.wallet.rpc.dto;

/**
 * 修改密码请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class ChangePasswordRequestDto {
    
    private String publicKeyHex;
    private String oldPassword;
    private String newPassword;
    
    public ChangePasswordRequestDto() {}
    
    public ChangePasswordRequestDto(String publicKeyHex, String oldPassword, String newPassword) {
        this.publicKeyHex = publicKeyHex;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
    
    // Getters and Setters
    public String getPublicKeyHex() {
        return publicKeyHex;
    }
    
    public void setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
    
    public String getOldPassword() {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
