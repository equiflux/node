package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

/**
 * 链状态信息DTO
 * 
 * <p>用于RPC接口返回链状态信息，包含当前高度、轮次、总供应量等。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class ChainStateDto {
    
    @NotNull(message = "Current height is required")
    @Positive(message = "Current height must be positive")
    @JsonProperty("currentHeight")
    private Long currentHeight;
    
    @JsonProperty("currentRound")
    private Long currentRound;
    
    @JsonProperty("totalSupply")
    private Long totalSupply;
    
    @JsonProperty("currentDifficulty")
    private String currentDifficulty;
    
    @JsonProperty("blockTime")
    private Long blockTime;
    
    @JsonProperty("lastBlockHash")
    private String lastBlockHash;
    
    @JsonProperty("lastBlockTimestamp")
    private Long lastBlockTimestamp;
    
    @JsonProperty("superNodeCount")
    private Integer superNodeCount;
    
    @JsonProperty("coreNodeCount")
    private Integer coreNodeCount;
    
    @JsonProperty("rotateNodeCount")
    private Integer rotateNodeCount;
    
    @JsonProperty("rewardedTopX")
    private Integer rewardedTopX;
    
    @JsonProperty("consensusVersion")
    private String consensusVersion;
    
    @JsonProperty("networkId")
    private String networkId;
    
    @JsonProperty("chainId")
    private String chainId;
    
    /**
     * 默认构造函数
     */
    public ChainStateDto() {
    }
    
    /**
     * 构造函数
     * 
     * @param currentHeight 当前高度
     * @param currentRound 当前轮次
     * @param totalSupply 总供应量
     */
    public ChainStateDto(Long currentHeight, Long currentRound, Long totalSupply) {
        this.currentHeight = currentHeight;
        this.currentRound = currentRound;
        this.totalSupply = totalSupply;
    }
    
    // Getters and Setters
    
    public Long getCurrentHeight() {
        return currentHeight;
    }
    
    public void setCurrentHeight(Long currentHeight) {
        this.currentHeight = currentHeight;
    }
    
    public Long getCurrentRound() {
        return currentRound;
    }
    
    public void setCurrentRound(Long currentRound) {
        this.currentRound = currentRound;
    }
    
    public Long getTotalSupply() {
        return totalSupply;
    }
    
    public void setTotalSupply(Long totalSupply) {
        this.totalSupply = totalSupply;
    }
    
    public String getCurrentDifficulty() {
        return currentDifficulty;
    }
    
    public void setCurrentDifficulty(String currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }
    
    public Long getBlockTime() {
        return blockTime;
    }
    
    public void setBlockTime(Long blockTime) {
        this.blockTime = blockTime;
    }
    
    public String getLastBlockHash() {
        return lastBlockHash;
    }
    
    public void setLastBlockHash(String lastBlockHash) {
        this.lastBlockHash = lastBlockHash;
    }
    
    public Long getLastBlockTimestamp() {
        return lastBlockTimestamp;
    }
    
    public void setLastBlockTimestamp(Long lastBlockTimestamp) {
        this.lastBlockTimestamp = lastBlockTimestamp;
    }
    
    public Integer getSuperNodeCount() {
        return superNodeCount;
    }
    
    public void setSuperNodeCount(Integer superNodeCount) {
        this.superNodeCount = superNodeCount;
    }
    
    public Integer getCoreNodeCount() {
        return coreNodeCount;
    }
    
    public void setCoreNodeCount(Integer coreNodeCount) {
        this.coreNodeCount = coreNodeCount;
    }
    
    public Integer getRotateNodeCount() {
        return rotateNodeCount;
    }
    
    public void setRotateNodeCount(Integer rotateNodeCount) {
        this.rotateNodeCount = rotateNodeCount;
    }
    
    public Integer getRewardedTopX() {
        return rewardedTopX;
    }
    
    public void setRewardedTopX(Integer rewardedTopX) {
        this.rewardedTopX = rewardedTopX;
    }
    
    public String getConsensusVersion() {
        return consensusVersion;
    }
    
    public void setConsensusVersion(String consensusVersion) {
        this.consensusVersion = consensusVersion;
    }
    
    public String getNetworkId() {
        return networkId;
    }
    
    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
    
    public String getChainId() {
        return chainId;
    }
    
    public void setChainId(String chainId) {
        this.chainId = chainId;
    }
    
    @Override
    public String toString() {
        return "ChainStateDto{" +
                "currentHeight=" + currentHeight +
                ", currentRound=" + currentRound +
                ", totalSupply=" + totalSupply +
                ", lastBlockHash='" + lastBlockHash + '\'' +
                ", superNodeCount=" + superNodeCount +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChainStateDto that = (ChainStateDto) obj;
        return Objects.equals(currentHeight, that.currentHeight) &&
               Objects.equals(currentRound, that.currentRound) &&
               Objects.equals(totalSupply, that.totalSupply) &&
               Objects.equals(currentDifficulty, that.currentDifficulty) &&
               Objects.equals(blockTime, that.blockTime) &&
               Objects.equals(lastBlockHash, that.lastBlockHash) &&
               Objects.equals(lastBlockTimestamp, that.lastBlockTimestamp) &&
               Objects.equals(superNodeCount, that.superNodeCount) &&
               Objects.equals(coreNodeCount, that.coreNodeCount) &&
               Objects.equals(rotateNodeCount, that.rotateNodeCount) &&
               Objects.equals(rewardedTopX, that.rewardedTopX) &&
               Objects.equals(consensusVersion, that.consensusVersion) &&
               Objects.equals(networkId, that.networkId) &&
               Objects.equals(chainId, that.chainId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(currentHeight, currentRound, totalSupply, currentDifficulty, 
                           blockTime, lastBlockHash, lastBlockTimestamp, superNodeCount, 
                           coreNodeCount, rotateNodeCount, rewardedTopX, consensusVersion, 
                           networkId, chainId);
    }
}
