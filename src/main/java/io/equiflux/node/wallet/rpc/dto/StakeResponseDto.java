package io.equiflux.node.wallet.rpc.dto;

/**
 * 质押响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class StakeResponseDto {
    
    private boolean success;
    private String message;
    private String publicKeyHex;
    private long stakeAmount;
    
    public StakeResponseDto() {
        this.success = true;
    }
    
    public static StakeResponseDto error(String message) {
        StakeResponseDto dto = new StakeResponseDto();
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
    
    public long getStakeAmount() {
        return stakeAmount;
    }
    
    public void setStakeAmount(long stakeAmount) {
        this.stakeAmount = stakeAmount;
    }
}
