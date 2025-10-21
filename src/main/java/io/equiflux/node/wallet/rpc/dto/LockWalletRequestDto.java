package io.equiflux.node.wallet.rpc.dto;

/**
 * 锁定钱包请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class LockWalletRequestDto {
    
    private String publicKeyHex;
    
    public LockWalletRequestDto() {}
    
    public LockWalletRequestDto(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
    
    public String getPublicKeyHex() {
        return publicKeyHex;
    }
    
    public void setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
}
