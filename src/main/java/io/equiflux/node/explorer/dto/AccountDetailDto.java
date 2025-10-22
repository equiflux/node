package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 账户详细信息DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class AccountDetailDto {
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("public_key")
    private String publicKey;
    
    @JsonProperty("balance")
    private long balance;
    
    @JsonProperty("stake_amount")
    private long stakeAmount;
    
    @JsonProperty("nonce")
    private long nonce;
    
    @JsonProperty("last_updated")
    private long lastUpdated;
    
    @JsonProperty("is_super_node")
    private boolean isSuperNode;
    
    // Getters and Setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
    
    public long getStakeAmount() { return stakeAmount; }
    public void setStakeAmount(long stakeAmount) { this.stakeAmount = stakeAmount; }
    
    public long getNonce() { return nonce; }
    public void setNonce(long nonce) { this.nonce = nonce; }
    
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public boolean isSuperNode() { return isSuperNode; }
    public void setIsSuperNode(boolean superNode) { isSuperNode = superNode; }
}
