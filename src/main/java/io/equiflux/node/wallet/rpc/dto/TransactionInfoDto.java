package io.equiflux.node.wallet.rpc.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易信息DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionInfoDto {
    
    private String transactionHash;
    private String state;
    private LocalDateTime timestamp;
    private Long blockHeight;
    private Integer blockIndex;
    private String errorMessage;
    private String fromPublicKeyHex;
    private String toPublicKeyHex;
    private long amount;
    private long fee;
    private String type;
    
    public TransactionInfoDto() {}
    
    // Getters and Setters
    public String getTransactionHash() {
        return transactionHash;
    }
    
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getBlockHeight() {
        return blockHeight;
    }
    
    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }
    
    public Integer getBlockIndex() {
        return blockIndex;
    }
    
    public void setBlockIndex(Integer blockIndex) {
        this.blockIndex = blockIndex;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
