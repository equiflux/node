package io.equiflux.node.wallet.rpc.dto;

/**
 * 转账交易请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransferTransactionRequestDto {
    
    private String fromPublicKeyHex;
    private String toPublicKeyHex;
    private long amount;
    private long fee;
    private String password;
    
    public TransferTransactionRequestDto() {}
    
    public TransferTransactionRequestDto(String fromPublicKeyHex, String toPublicKeyHex, 
                                       long amount, long fee, String password) {
        this.fromPublicKeyHex = fromPublicKeyHex;
        this.toPublicKeyHex = toPublicKeyHex;
        this.amount = amount;
        this.fee = fee;
        this.password = password;
    }
    
    // Getters and Setters
    public String getFromPublicKeyHex() {
        return fromPublicKeyHex;
    }
    
    public void setFromPublicKeyHex(String fromPublicKeyHex) {
        this.fromPublicKeyHex = fromPublicKeyHex;
    }
    
    public String getToPublicKeyHex() {
        return toPublicKeyHex;
    }
    
    public void setToPublicKeyHex(String toPublicKeyHex) {
        this.toPublicKeyHex = toPublicKeyHex;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
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
