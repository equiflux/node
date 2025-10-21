package io.equiflux.node.wallet.rpc.dto;

/**
 * 解质押交易请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class UnstakeTransactionRequestDto {
    
    private String publicKeyHex;
    private long unstakeAmount;
    private long fee;
    private String password;
    
    public UnstakeTransactionRequestDto() {}
    
    public UnstakeTransactionRequestDto(String publicKeyHex, long unstakeAmount, long fee, String password) {
        this.publicKeyHex = publicKeyHex;
        this.unstakeAmount = unstakeAmount;
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
    
    public long getUnstakeAmount() {
        return unstakeAmount;
    }
    
    public void setUnstakeAmount(long unstakeAmount) {
        this.unstakeAmount = unstakeAmount;
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
