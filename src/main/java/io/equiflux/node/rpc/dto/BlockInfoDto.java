package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Objects;

/**
 * 区块信息DTO
 * 
 * <p>用于RPC接口返回区块信息，包含区块的基本信息和交易列表。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BlockInfoDto {
    
    @NotNull(message = "Block height is required")
    @Positive(message = "Block height must be positive")
    @JsonProperty("height")
    private Long height;
    
    @JsonProperty("hash")
    private String hash;
    
    @JsonProperty("previousHash")
    private String previousHash;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("round")
    private Long round;
    
    @JsonProperty("proposer")
    private String proposer;
    
    @JsonProperty("vrfOutput")
    private String vrfOutput;
    
    @JsonProperty("vrfProof")
    private String vrfProof;
    
    @JsonProperty("allVrfAnnouncements")
    private List<VrfAnnouncementDto> allVrfAnnouncements;
    
    @JsonProperty("rewardedNodes")
    private List<String> rewardedNodes;
    
    @JsonProperty("transactions")
    private List<TransactionInfoDto> transactions;
    
    @JsonProperty("merkleRoot")
    private String merkleRoot;
    
    @JsonProperty("nonce")
    private Long nonce;
    
    @JsonProperty("difficultyTarget")
    private String difficultyTarget;
    
    @JsonProperty("signatures")
    private List<String> signatures;
    
    @JsonProperty("size")
    private Integer size;
    
    @JsonProperty("transactionCount")
    private Integer transactionCount;
    
    /**
     * 默认构造函数
     */
    public BlockInfoDto() {
    }
    
    /**
     * 构造函数
     * 
     * @param height 区块高度
     * @param hash 区块哈希
     * @param previousHash 前一个区块哈希
     * @param timestamp 时间戳
     * @param round 轮次
     * @param proposer 提议者
     */
    public BlockInfoDto(Long height, String hash, String previousHash, Long timestamp, 
                       Long round, String proposer) {
        this.height = height;
        this.hash = hash;
        this.previousHash = previousHash;
        this.timestamp = timestamp;
        this.round = round;
        this.proposer = proposer;
    }
    
    // Getters and Setters
    
    public Long getHeight() {
        return height;
    }
    
    public void setHeight(Long height) {
        this.height = height;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public String getPreviousHash() {
        return previousHash;
    }
    
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getRound() {
        return round;
    }
    
    public void setRound(Long round) {
        this.round = round;
    }
    
    public String getProposer() {
        return proposer;
    }
    
    public void setProposer(String proposer) {
        this.proposer = proposer;
    }
    
    public String getVrfOutput() {
        return vrfOutput;
    }
    
    public void setVrfOutput(String vrfOutput) {
        this.vrfOutput = vrfOutput;
    }
    
    public String getVrfProof() {
        return vrfProof;
    }
    
    public void setVrfProof(String vrfProof) {
        this.vrfProof = vrfProof;
    }
    
    public List<VrfAnnouncementDto> getAllVrfAnnouncements() {
        return allVrfAnnouncements;
    }
    
    public void setAllVrfAnnouncements(List<VrfAnnouncementDto> allVrfAnnouncements) {
        this.allVrfAnnouncements = allVrfAnnouncements;
    }
    
    public List<String> getRewardedNodes() {
        return rewardedNodes;
    }
    
    public void setRewardedNodes(List<String> rewardedNodes) {
        this.rewardedNodes = rewardedNodes;
    }
    
    public List<TransactionInfoDto> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<TransactionInfoDto> transactions) {
        this.transactions = transactions;
    }
    
    public String getMerkleRoot() {
        return merkleRoot;
    }
    
    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }
    
    public Long getNonce() {
        return nonce;
    }
    
    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }
    
    public String getDifficultyTarget() {
        return difficultyTarget;
    }
    
    public void setDifficultyTarget(String difficultyTarget) {
        this.difficultyTarget = difficultyTarget;
    }
    
    public List<String> getSignatures() {
        return signatures;
    }
    
    public void setSignatures(List<String> signatures) {
        this.signatures = signatures;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
    
    @Override
    public String toString() {
        return "BlockInfoDto{" +
                "height=" + height +
                ", hash='" + hash + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", timestamp=" + timestamp +
                ", round=" + round +
                ", proposer='" + proposer + '\'' +
                ", transactionCount=" + transactionCount +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BlockInfoDto that = (BlockInfoDto) obj;
        return Objects.equals(height, that.height) &&
               Objects.equals(hash, that.hash) &&
               Objects.equals(previousHash, that.previousHash) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(round, that.round) &&
               Objects.equals(proposer, that.proposer) &&
               Objects.equals(vrfOutput, that.vrfOutput) &&
               Objects.equals(vrfProof, that.vrfProof) &&
               Objects.equals(allVrfAnnouncements, that.allVrfAnnouncements) &&
               Objects.equals(rewardedNodes, that.rewardedNodes) &&
               Objects.equals(transactions, that.transactions) &&
               Objects.equals(merkleRoot, that.merkleRoot) &&
               Objects.equals(nonce, that.nonce) &&
               Objects.equals(difficultyTarget, that.difficultyTarget) &&
               Objects.equals(signatures, that.signatures) &&
               Objects.equals(size, that.size) &&
               Objects.equals(transactionCount, that.transactionCount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(height, hash, previousHash, timestamp, round, proposer, 
                           vrfOutput, vrfProof, allVrfAnnouncements, rewardedNodes, 
                           transactions, merkleRoot, nonce, difficultyTarget, 
                           signatures, size, transactionCount);
    }
}
