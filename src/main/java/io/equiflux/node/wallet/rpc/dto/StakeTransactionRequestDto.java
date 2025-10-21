package io.equiflux.node.wallet.rpc.dto;

/**
 * 质押交易请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class StakeTransactionRequestDto {
    
    private String publicKeyHex;
    private long stakeAmount;
    private long fee;
    private String password;
    
    public StakeTransactionRequestDto() {}
    
    public StakeTransactionRequestDto(String publicKeyHex, long stakeAmount, long fee, String password) {
        this.publicKeyHex = publicKeyHex;
        this.stakeAmount = stakeAmount;
        this.fee = fee;
        this.password = password;
    }
    
    // Getters and Setters
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
    
    public long getFee() {
        return fee;
    }
    
    public void setFee(long fee) {
        this.fee = fee;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
