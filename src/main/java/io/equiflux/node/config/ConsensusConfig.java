package io.equiflux.node.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * 共识配置
 * 
 * <p>使用Spring Boot的@ConfigurationProperties实现配置管理，支持：
 * <ul>
 *   <li>超级节点配置</li>
 *   <li>时间配置</li>
 *   <li>奖励配置</li>
 *   <li>PoW配置</li>
 *   <li>质押配置</li>
 *   <li>性能配置</li>
 * </ul>
 * 
 * <p>配置来源：
 * <ul>
 *   <li>application.yml</li>
 *   <li>环境变量</li>
 *   <li>命令行参数</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
@ConfigurationProperties(prefix = "equiflux.consensus")
public class ConsensusConfig {
    
    // 超级节点配置
    private int superNodeCount = 50;
    private int coreNodeCount = 20;
    private int rotateNodeCount = 30;
    
    // 时间配置
    private int blockTimeSeconds = 3;
    private int vrfCollectionTimeoutMs = 3000;
    private int blockProductionTimeoutMs = 5000;
    
    // 奖励配置
    private int rewardedTopX = 15;
    
    // PoW配置
    private BigInteger powBaseDifficulty = BigInteger.valueOf(2500000);
    private int powTargetTimeSeconds = 3;
    
    // 质押配置
    private long minStakeCore = 100000L;
    private long minStakeRotate = 50000L;
    
    // 性能配置
    private int maxTransactionsPerBlock = 1000;
    private int maxBlockSizeMb = 2;
    
    /**
     * 获取超级节点总数
     * 
     * @return 超级节点总数
     */
    public int getSuperNodeCount() {
        return superNodeCount;
    }
    
    /**
     * 设置超级节点总数
     * 
     * @param superNodeCount 超级节点总数
     */
    public void setSuperNodeCount(int superNodeCount) {
        if (superNodeCount <= 0) {
            throw new IllegalArgumentException("Super node count must be positive");
        }
        this.superNodeCount = superNodeCount;
    }
    
    /**
     * 获取核心节点数量
     * 
     * @return 核心节点数量
     */
    public int getCoreNodeCount() {
        return coreNodeCount;
    }
    
    /**
     * 设置核心节点数量
     * 
     * @param coreNodeCount 核心节点数量
     */
    public void setCoreNodeCount(int coreNodeCount) {
        if (coreNodeCount < 0) {
            throw new IllegalArgumentException("Core node count cannot be negative");
        }
        this.coreNodeCount = coreNodeCount;
    }
    
    /**
     * 获取轮换节点数量
     * 
     * @return 轮换节点数量
     */
    public int getRotateNodeCount() {
        return rotateNodeCount;
    }
    
    /**
     * 设置轮换节点数量
     * 
     * @param rotateNodeCount 轮换节点数量
     */
    public void setRotateNodeCount(int rotateNodeCount) {
        if (rotateNodeCount < 0) {
            throw new IllegalArgumentException("Rotate node count cannot be negative");
        }
        this.rotateNodeCount = rotateNodeCount;
    }
    
    /**
     * 获取出块时间（秒）
     * 
     * @return 出块时间（秒）
     */
    public int getBlockTimeSeconds() {
        return blockTimeSeconds;
    }
    
    /**
     * 设置出块时间（秒）
     * 
     * @param blockTimeSeconds 出块时间（秒）
     */
    public void setBlockTimeSeconds(int blockTimeSeconds) {
        if (blockTimeSeconds <= 0) {
            throw new IllegalArgumentException("Block time must be positive");
        }
        this.blockTimeSeconds = blockTimeSeconds;
    }
    
    /**
     * 获取VRF收集超时时间（毫秒）
     * 
     * @return VRF收集超时时间（毫秒）
     */
    public int getVrfCollectionTimeoutMs() {
        return vrfCollectionTimeoutMs;
    }
    
    /**
     * 设置VRF收集超时时间（毫秒）
     * 
     * @param vrfCollectionTimeoutMs VRF收集超时时间（毫秒）
     */
    public void setVrfCollectionTimeoutMs(int vrfCollectionTimeoutMs) {
        if (vrfCollectionTimeoutMs <= 0) {
            throw new IllegalArgumentException("VRF collection timeout must be positive");
        }
        this.vrfCollectionTimeoutMs = vrfCollectionTimeoutMs;
    }
    
    /**
     * 获取区块生产超时时间（毫秒）
     * 
     * @return 区块生产超时时间（毫秒）
     */
    public int getBlockProductionTimeoutMs() {
        return blockProductionTimeoutMs;
    }
    
    /**
     * 设置区块生产超时时间（毫秒）
     * 
     * @param blockProductionTimeoutMs 区块生产超时时间（毫秒）
     */
    public void setBlockProductionTimeoutMs(int blockProductionTimeoutMs) {
        if (blockProductionTimeoutMs <= 0) {
            throw new IllegalArgumentException("Block production timeout must be positive");
        }
        this.blockProductionTimeoutMs = blockProductionTimeoutMs;
    }
    
    /**
     * 获取奖励前X名
     * 
     * @return 奖励前X名
     */
    public int getRewardedTopX() {
        return rewardedTopX;
    }
    
    /**
     * 设置奖励前X名
     * 
     * @param rewardedTopX 奖励前X名
     */
    public void setRewardedTopX(int rewardedTopX) {
        if (rewardedTopX <= 0) {
            throw new IllegalArgumentException("Rewarded top X must be positive");
        }
        this.rewardedTopX = rewardedTopX;
    }
    
    /**
     * 获取PoW基础难度
     * 
     * @return PoW基础难度
     */
    public BigInteger getPowBaseDifficulty() {
        return powBaseDifficulty;
    }
    
    /**
     * 设置PoW基础难度
     * 
     * @param powBaseDifficulty PoW基础难度
     */
    public void setPowBaseDifficulty(BigInteger powBaseDifficulty) {
        if (powBaseDifficulty == null || powBaseDifficulty.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("PoW base difficulty must be positive");
        }
        this.powBaseDifficulty = powBaseDifficulty;
    }
    
    /**
     * 获取PoW目标时间（秒）
     * 
     * @return PoW目标时间（秒）
     */
    public int getPowTargetTimeSeconds() {
        return powTargetTimeSeconds;
    }
    
    /**
     * 设置PoW目标时间（秒）
     * 
     * @param powTargetTimeSeconds PoW目标时间（秒）
     */
    public void setPowTargetTimeSeconds(int powTargetTimeSeconds) {
        if (powTargetTimeSeconds <= 0) {
            throw new IllegalArgumentException("PoW target time must be positive");
        }
        this.powTargetTimeSeconds = powTargetTimeSeconds;
    }
    
    /**
     * 获取核心节点最小质押
     * 
     * @return 核心节点最小质押
     */
    public long getMinStakeCore() {
        return minStakeCore;
    }
    
    /**
     * 设置核心节点最小质押
     * 
     * @param minStakeCore 核心节点最小质押
     */
    public void setMinStakeCore(long minStakeCore) {
        if (minStakeCore < 0) {
            throw new IllegalArgumentException("Min stake core cannot be negative");
        }
        this.minStakeCore = minStakeCore;
    }
    
    /**
     * 获取轮换节点最小质押
     * 
     * @return 轮换节点最小质押
     */
    public long getMinStakeRotate() {
        return minStakeRotate;
    }
    
    /**
     * 设置轮换节点最小质押
     * 
     * @param minStakeRotate 轮换节点最小质押
     */
    public void setMinStakeRotate(long minStakeRotate) {
        if (minStakeRotate < 0) {
            throw new IllegalArgumentException("Min stake rotate cannot be negative");
        }
        this.minStakeRotate = minStakeRotate;
    }
    
    /**
     * 获取每个区块最大交易数
     * 
     * @return 每个区块最大交易数
     */
    public int getMaxTransactionsPerBlock() {
        return maxTransactionsPerBlock;
    }
    
    /**
     * 设置每个区块最大交易数
     * 
     * @param maxTransactionsPerBlock 每个区块最大交易数
     */
    public void setMaxTransactionsPerBlock(int maxTransactionsPerBlock) {
        if (maxTransactionsPerBlock <= 0) {
            throw new IllegalArgumentException("Max transactions per block must be positive");
        }
        this.maxTransactionsPerBlock = maxTransactionsPerBlock;
    }
    
    /**
     * 获取最大区块大小（MB）
     * 
     * @return 最大区块大小（MB）
     */
    public int getMaxBlockSizeMb() {
        return maxBlockSizeMb;
    }
    
    /**
     * 设置最大区块大小（MB）
     * 
     * @param maxBlockSizeMb 最大区块大小（MB）
     */
    public void setMaxBlockSizeMb(int maxBlockSizeMb) {
        if (maxBlockSizeMb <= 0) {
            throw new IllegalArgumentException("Max block size must be positive");
        }
        this.maxBlockSizeMb = maxBlockSizeMb;
    }
    
    /**
     * 获取最小VRF数量要求
     * 
     * @return 最小VRF数量要求
     */
    public int getMinVrfRequired() {
        return (superNodeCount * 2) / 3;
    }
    
    /**
     * 获取最大区块大小（字节）
     * 
     * @return 最大区块大小（字节）
     */
    public long getMaxBlockSizeBytes() {
        return maxBlockSizeMb * 1024L * 1024L;
    }
    
    /**
     * 验证配置的有效性
     * 
     * @throws IllegalArgumentException 如果配置无效
     */
    public void validate() {
        if (coreNodeCount + rotateNodeCount != superNodeCount) {
            throw new IllegalArgumentException("Core nodes + rotate nodes must equal super nodes");
        }
        
        if (rewardedTopX > superNodeCount) {
            throw new IllegalArgumentException("Rewarded top X cannot exceed super node count");
        }
        
        if (vrfCollectionTimeoutMs >= blockProductionTimeoutMs) {
            throw new IllegalArgumentException("VRF collection timeout must be less than block production timeout");
        }
        
        if (blockTimeSeconds * 1000 < blockProductionTimeoutMs) {
            throw new IllegalArgumentException("Block time must be greater than or equal to block production timeout");
        }
    }
    
    @Override
    public String toString() {
        return "ConsensusConfig{" +
               "superNodeCount=" + superNodeCount +
               ", coreNodeCount=" + coreNodeCount +
               ", rotateNodeCount=" + rotateNodeCount +
               ", blockTimeSeconds=" + blockTimeSeconds +
               ", vrfCollectionTimeoutMs=" + vrfCollectionTimeoutMs +
               ", blockProductionTimeoutMs=" + blockProductionTimeoutMs +
               ", rewardedTopX=" + rewardedTopX +
               ", powBaseDifficulty=" + powBaseDifficulty +
               ", powTargetTimeSeconds=" + powTargetTimeSeconds +
               ", minStakeCore=" + minStakeCore +
               ", minStakeRotate=" + minStakeRotate +
               ", maxTransactionsPerBlock=" + maxTransactionsPerBlock +
               ", maxBlockSizeMb=" + maxBlockSizeMb +
               '}';
    }
}
