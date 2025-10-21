package io.equiflux.node.storage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 链状态
 * 
 * <p>表示区块链的全局状态信息，包括当前高度、总供应量、共识参数等。
 * 
 * <p>链状态结构：
 * <ul>
 *   <li>当前区块高度</li>
 *   <li>当前轮次</li>
 *   <li>总供应量</li>
 *   <li>活跃超级节点数量</li>
 *   <li>当前PoW难度</li>
 *   <li>最后更新时间戳</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class ChainState {
    
    private final long currentHeight;
    private final long currentRound;
    private final long totalSupply;
    private final int activeSuperNodes;
    private final java.math.BigInteger currentDifficulty;
    private final long lastUpdateTimestamp;
    
    /**
     * 构造链状态
     * 
     * @param currentHeight 当前区块高度
     * @param currentRound 当前轮次
     * @param totalSupply 总供应量
     * @param activeSuperNodes 活跃超级节点数量
     * @param currentDifficulty 当前PoW难度
     * @param lastUpdateTimestamp 最后更新时间戳
     */
    @JsonCreator
    public ChainState(@JsonProperty("currentHeight") long currentHeight,
                      @JsonProperty("currentRound") long currentRound,
                      @JsonProperty("totalSupply") long totalSupply,
                      @JsonProperty("activeSuperNodes") int activeSuperNodes,
                      @JsonProperty("currentDifficulty") java.math.BigInteger currentDifficulty,
                      @JsonProperty("lastUpdateTimestamp") long lastUpdateTimestamp) {
        this.currentHeight = currentHeight;
        this.currentRound = currentRound;
        this.totalSupply = totalSupply;
        this.activeSuperNodes = activeSuperNodes;
        this.currentDifficulty = Objects.requireNonNull(currentDifficulty, "Current difficulty cannot be null");
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        
        // 验证参数
        if (currentHeight < 0) {
            throw new IllegalArgumentException("Current height cannot be negative");
        }
        if (currentRound < 0) {
            throw new IllegalArgumentException("Current round cannot be negative");
        }
        if (totalSupply < 0) {
            throw new IllegalArgumentException("Total supply cannot be negative");
        }
        if (activeSuperNodes < 0) {
            throw new IllegalArgumentException("Active super nodes cannot be negative");
        }
        if (currentDifficulty.compareTo(java.math.BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Current difficulty must be positive");
        }
        if (lastUpdateTimestamp <= 0) {
            throw new IllegalArgumentException("Last update timestamp must be positive");
        }
    }
    
    /**
     * 获取当前区块高度
     * 
     * @return 当前区块高度
     */
    public long getCurrentHeight() {
        return currentHeight;
    }
    
    /**
     * 获取当前轮次
     * 
     * @return 当前轮次
     */
    public long getCurrentRound() {
        return currentRound;
    }
    
    /**
     * 获取总供应量
     * 
     * @return 总供应量
     */
    public long getTotalSupply() {
        return totalSupply;
    }
    
    /**
     * 获取活跃超级节点数量
     * 
     * @return 活跃超级节点数量
     */
    public int getActiveSuperNodes() {
        return activeSuperNodes;
    }
    
    /**
     * 获取当前PoW难度
     * 
     * @return 当前PoW难度
     */
    public java.math.BigInteger getCurrentDifficulty() {
        return currentDifficulty;
    }
    
    /**
     * 获取最后更新时间戳
     * 
     * @return 最后更新时间戳
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
    
    /**
     * 创建更新后的链状态
     * 
     * @param newHeight 新区块高度
     * @param newRound 新轮次
     * @param newTotalSupply 新总供应量
     * @param newActiveSuperNodes 新活跃超级节点数量
     * @param newDifficulty 新PoW难度
     * @return 更新后的链状态
     */
    public ChainState update(long newHeight, long newRound, long newTotalSupply, 
                            int newActiveSuperNodes, java.math.BigInteger newDifficulty) {
        return new ChainState(newHeight, newRound, newTotalSupply, newActiveSuperNodes, 
                             newDifficulty, System.currentTimeMillis());
    }
    
    /**
     * 创建高度更新后的链状态
     * 
     * @return 更新后的链状态
     */
    public ChainState incrementHeight() {
        return new ChainState(currentHeight + 1, currentRound, totalSupply, activeSuperNodes, 
                             currentDifficulty, System.currentTimeMillis());
    }
    
    /**
     * 创建轮次更新后的链状态
     * 
     * @return 更新后的链状态
     */
    public ChainState incrementRound() {
        return new ChainState(currentHeight, currentRound + 1, totalSupply, activeSuperNodes, 
                             currentDifficulty, System.currentTimeMillis());
    }
    
    /**
     * 创建供应量更新后的链状态
     * 
     * @param supplyChange 供应量变化
     * @return 更新后的链状态
     */
    public ChainState updateSupply(long supplyChange) {
        long newTotalSupply = totalSupply + supplyChange;
        if (newTotalSupply < 0) {
            throw new IllegalArgumentException("Total supply cannot be negative");
        }
        return new ChainState(currentHeight, currentRound, newTotalSupply, activeSuperNodes, 
                             currentDifficulty, System.currentTimeMillis());
    }
    
    /**
     * 创建难度更新后的链状态
     * 
     * @param newDifficulty 新PoW难度
     * @return 更新后的链状态
     */
    public ChainState updateDifficulty(java.math.BigInteger newDifficulty) {
        return new ChainState(currentHeight, currentRound, totalSupply, activeSuperNodes, 
                             newDifficulty, System.currentTimeMillis());
    }
    
    /**
     * 创建超级节点数量更新后的链状态
     * 
     * @param newActiveSuperNodes 新活跃超级节点数量
     * @return 更新后的链状态
     */
    public ChainState updateActiveSuperNodes(int newActiveSuperNodes) {
        if (newActiveSuperNodes < 0) {
            throw new IllegalArgumentException("Active super nodes cannot be negative");
        }
        return new ChainState(currentHeight, currentRound, totalSupply, newActiveSuperNodes, 
                             currentDifficulty, System.currentTimeMillis());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChainState that = (ChainState) obj;
        return currentHeight == that.currentHeight &&
               currentRound == that.currentRound &&
               totalSupply == that.totalSupply &&
               activeSuperNodes == that.activeSuperNodes &&
               lastUpdateTimestamp == that.lastUpdateTimestamp &&
               Objects.equals(currentDifficulty, that.currentDifficulty);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(currentHeight, currentRound, totalSupply, activeSuperNodes, 
                           currentDifficulty, lastUpdateTimestamp);
    }
    
    @Override
    public String toString() {
        return "ChainState{" +
               "currentHeight=" + currentHeight +
               ", currentRound=" + currentRound +
               ", totalSupply=" + totalSupply +
               ", activeSuperNodes=" + activeSuperNodes +
               ", currentDifficulty=" + currentDifficulty +
               ", lastUpdateTimestamp=" + lastUpdateTimestamp +
               '}';
    }
}
