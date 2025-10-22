package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 区块详细信息DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BlockDetailDto {
    
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
    
    @JsonProperty("vrf_output")
    private String vrfOutput;
    
    @JsonProperty("vrf_proof")
    private String vrfProof;
    
    @JsonProperty("merkle_root")
    private String merkleRoot;
    
    @JsonProperty("nonce")
    private long nonce;
    
    @JsonProperty("difficulty_target")
    private String difficultyTarget;
    
    @JsonProperty("transaction_count")
    private int transactionCount;
    
    @JsonProperty("all_vrf_announcements")
    private List<VrfAnnouncementDto> allVrfAnnouncements;
    
    @JsonProperty("transactions")
    private List<TransactionSummaryDto> transactions;
    
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
    
    public String getVrfOutput() { return vrfOutput; }
    public void setVrfOutput(String vrfOutput) { this.vrfOutput = vrfOutput; }
    
    public String getVrfProof() { return vrfProof; }
    public void setVrfProof(String vrfProof) { this.vrfProof = vrfProof; }
    
    public String getMerkleRoot() { return merkleRoot; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    
    public long getNonce() { return nonce; }
    public void setNonce(long nonce) { this.nonce = nonce; }
    
    public String getDifficultyTarget() { return difficultyTarget; }
    public void setDifficultyTarget(String difficultyTarget) { this.difficultyTarget = difficultyTarget; }
    
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    
    public List<VrfAnnouncementDto> getAllVrfAnnouncements() { return allVrfAnnouncements; }
    public void setAllVrfAnnouncements(List<VrfAnnouncementDto> allVrfAnnouncements) { this.allVrfAnnouncements = allVrfAnnouncements; }
    
    public List<TransactionSummaryDto> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionSummaryDto> transactions) { this.transactions = transactions; }
}
