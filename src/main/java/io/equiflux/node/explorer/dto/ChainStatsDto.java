package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 链统计信息DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class ChainStatsDto {
    
    @JsonProperty("current_height")
    private long currentHeight;
    
    @JsonProperty("current_round")
    private long currentRound;
    
    @JsonProperty("total_supply")
    private long totalSupply;
    
    @JsonProperty("current_difficulty")
    private String currentDifficulty;
    
    @JsonProperty("block_time")
    private long blockTime;
    
    @JsonProperty("super_node_count")
    private int superNodeCount;
    
    @JsonProperty("core_node_count")
    private int coreNodeCount;
    
    @JsonProperty("rotate_node_count")
    private int rotateNodeCount;
    
    @JsonProperty("rewarded_top_x")
    private int rewardedTopX;
    
    @JsonProperty("consensus_version")
    private String consensusVersion;
    
    @JsonProperty("network_id")
    private String networkId;
    
    @JsonProperty("chain_id")
    private String chainId;
    
    // Getters and Setters
    public long getCurrentHeight() { return currentHeight; }
    public void setCurrentHeight(long currentHeight) { this.currentHeight = currentHeight; }
    
    public long getCurrentRound() { return currentRound; }
    public void setCurrentRound(long currentRound) { this.currentRound = currentRound; }
    
    public long getTotalSupply() { return totalSupply; }
    public void setTotalSupply(long totalSupply) { this.totalSupply = totalSupply; }
    
    public String getCurrentDifficulty() { return currentDifficulty; }
    public void setCurrentDifficulty(String currentDifficulty) { this.currentDifficulty = currentDifficulty; }
    
    public long getBlockTime() { return blockTime; }
    public void setBlockTime(long blockTime) { this.blockTime = blockTime; }
    
    public int getSuperNodeCount() { return superNodeCount; }
    public void setSuperNodeCount(int superNodeCount) { this.superNodeCount = superNodeCount; }
    
    public int getCoreNodeCount() { return coreNodeCount; }
    public void setCoreNodeCount(int coreNodeCount) { this.coreNodeCount = coreNodeCount; }
    
    public int getRotateNodeCount() { return rotateNodeCount; }
    public void setRotateNodeCount(int rotateNodeCount) { this.rotateNodeCount = rotateNodeCount; }
    
    public int getRewardedTopX() { return rewardedTopX; }
    public void setRewardedTopX(int rewardedTopX) { this.rewardedTopX = rewardedTopX; }
    
    public String getConsensusVersion() { return consensusVersion; }
    public void setConsensusVersion(String consensusVersion) { this.consensusVersion = consensusVersion; }
    
    public String getNetworkId() { return networkId; }
    public void setNetworkId(String networkId) { this.networkId = networkId; }
    
    public String getChainId() { return chainId; }
    public void setChainId(String chainId) { this.chainId = chainId; }
}
