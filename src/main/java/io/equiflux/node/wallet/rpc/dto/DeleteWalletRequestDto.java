package io.equiflux.node.wallet.rpc.dto;

/**
 * 删除钱包请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class DeleteWalletRequestDto {
    
    private String publicKeyHex;
    private String password;
    
    public DeleteWalletRequestDto() {}
    
    public DeleteWalletRequestDto(String publicKeyHex, String password) {
        this.publicKeyHex = publicKeyHex;
        this.password = password;
    }
    
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
