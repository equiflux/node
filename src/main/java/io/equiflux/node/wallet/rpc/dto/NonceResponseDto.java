package io.equiflux.node.wallet.rpc.dto;

/**
 * Nonce响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class NonceResponseDto {
    
    private boolean success;
    private String message;
    private String publicKeyHex;
    private long nonce;
    
    public NonceResponseDto() {
        this.success = true;
    }
    
    public static NonceResponseDto error(String message) {
        NonceResponseDto dto = new NonceResponseDto();
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
    
    public long getNonce() {
        return nonce;
    }
    
    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
