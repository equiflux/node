package io.equiflux.node.wallet.rpc.dto;

/**
 * 余额响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BalanceResponseDto {
    
    private boolean success;
    private String message;
    private String publicKeyHex;
    private long balance;
    
    public BalanceResponseDto() {
        this.success = true;
    }
    
    public static BalanceResponseDto error(String message) {
        BalanceResponseDto dto = new BalanceResponseDto();
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
    
    public long getBalance() {
        return balance;
    }
    
    public void setBalance(long balance) {
        this.balance = balance;
    }
}
