package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 交易详细信息DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionDetailDto {
    
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
    
    @JsonProperty("nonce")
    private long nonce;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("signature")
    private String signature;
    
    @JsonProperty("data")
    private String data;
    
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
    
    public long getNonce() { return nonce; }
    public void setNonce(long nonce) { this.nonce = nonce; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
