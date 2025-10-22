package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 区块摘要DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BlockSummaryDto {
    
    @JsonProperty("height")
    private long height;
    
    @JsonProperty("hash")
    private String hash;
    
    @JsonProperty("previous_hash")
    private String previousHash;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("round")
    private long round;
    
    @JsonProperty("proposer")
    private String proposer;
    
    @JsonProperty("transaction_count")
    private int transactionCount;
    
    @JsonProperty("difficulty_target")
    private String difficultyTarget;
    
    // Getters and Setters
    public long getHeight() { return height; }
    public void setHeight(long height) { this.height = height; }
    
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public long getRound() { return round; }
    public void setRound(long round) { this.round = round; }
    
    public String getProposer() { return proposer; }
    public void setProposer(String proposer) { this.proposer = proposer; }
    
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    
    public String getDifficultyTarget() { return difficultyTarget; }
    public void setDifficultyTarget(String difficultyTarget) { this.difficultyTarget = difficultyTarget; }
}
