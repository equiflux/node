package io.equiflux.node.wallet.rpc.dto;

import java.time.LocalDateTime;

/**
 * 交易响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionResponseDto {
    
    private boolean success;
    private String message;
    private String transactionHash;
    private String fromPublicKeyHex;
    private String toPublicKeyHex;
    private long amount;
    private long fee;
    private long nonce;
    private LocalDateTime timestamp;
    private String type;
    private String signature;
    
    public TransactionResponseDto() {
        this.success = true;
    }
    
    public static TransactionResponseDto error(String message) {
        TransactionResponseDto dto = new TransactionResponseDto();
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
    
    public String getTransactionHash() {
        return transactionHash;
    }
    
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }
    
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
    
    public long getNonce() {
        return nonce;
    }
    
    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
