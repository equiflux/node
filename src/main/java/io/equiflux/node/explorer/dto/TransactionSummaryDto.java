package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 交易摘要DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionSummaryDto {
    
    @JsonProperty("hash")
    private String hash;
    
    @JsonProperty("from")
    private String from;
    
    @JsonProperty("to")
    private String to;
    
    @JsonProperty("amount")
    private long amount;
    
    @JsonProperty("fee")
    private long fee;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    // Getters and Setters
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    
    public long getFee() { return fee; }
    public void setFee(long fee) { this.fee = fee; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
