package io.equiflux.node.wallet.rpc.dto;

import java.time.LocalDateTime;

/**
 * 账户状态响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class AccountStateResponseDto {
    
    private boolean success;
    private String message;
    private String publicKeyHex;
    private long balance;
    private long nonce;
    private long stakeAmount;
    private LocalDateTime lastUpdated;
    
    public AccountStateResponseDto() {
        this.success = true;
    }
    
    public static AccountStateResponseDto error(String message) {
        AccountStateResponseDto dto = new AccountStateResponseDto();
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
    
    public long getNonce() {
        return nonce;
    }
    
    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
    
    public long getStakeAmount() {
        return stakeAmount;
    }
    
    public void setStakeAmount(long stakeAmount) {
        this.stakeAmount = stakeAmount;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
