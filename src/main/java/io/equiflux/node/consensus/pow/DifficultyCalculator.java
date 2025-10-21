package io.equiflux.node.consensus.pow;

import io.equiflux.node.model.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Objects;

/**
 * 难度计算器
 * 
 * <p>负责计算PoW难度，实现动态难度调整机制。
 * 
 * <p>难度调整策略：
 * <ul>
 *   <li>基础难度：根据网络状况设定</li>
 *   <li>动态调整：根据实际挖矿时间调整</li>
 *   <li>惩罚机制：根据违规情况增加难度</li>
 * </ul>
 * 
 * <p>难度调整公式：
 * <pre>
 * new_difficulty = base_difficulty × adjustment_factor × penalty_factor
 * </pre>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class DifficultyCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(DifficultyCalculator.class);
    
    // 基础难度（2.5M，约2-3秒挖矿时间）
    private static final BigInteger BASE_DIFFICULTY = BigInteger.valueOf(2_500_000);
    
    // 难度调整参数
    private static final double ADJUSTMENT_FACTOR_MAX = 2.0;
    private static final double ADJUSTMENT_FACTOR_MIN = 0.5;
    private static final int ADJUSTMENT_WINDOW = 10; // 每10个区块调整一次
    
    /**
     * 计算PoW难度
     * 
     * @param previousBlock 前一区块
     * @param targetTimeSeconds 目标时间（秒）
     * @return 新的难度目标
     */
    public BigInteger calculateDifficulty(Block previousBlock, int targetTimeSeconds) {
        if (previousBlock == null) {
            // 创世区块使用基础难度
            return BASE_DIFFICULTY;
        }
        
        if (targetTimeSeconds <= 0) {
            throw new IllegalArgumentException("Target time must be positive");
        }
        
        // 获取当前难度
        BigInteger currentDifficulty = previousBlock.getDifficultyTarget();
        
        // 计算调整因子
        double adjustmentFactor = calculateAdjustmentFactor(previousBlock, targetTimeSeconds);
        
        // 计算惩罚因子
        double penaltyFactor = calculatePenaltyFactor(previousBlock);
        
        // 计算新难度
        BigInteger newDifficulty = currentDifficulty
                .multiply(BigInteger.valueOf((long) (adjustmentFactor * 1000)))
                .multiply(BigInteger.valueOf((long) (penaltyFactor * 1000)))
                .divide(BigInteger.valueOf(1000000));
        
        // 限制难度范围
        BigInteger minDifficulty = BASE_DIFFICULTY.divide(BigInteger.valueOf(2));
        BigInteger maxDifficulty = BASE_DIFFICULTY.multiply(BigInteger.valueOf(10));
        
        if (newDifficulty.compareTo(minDifficulty) < 0) {
            newDifficulty = minDifficulty;
        } else if (newDifficulty.compareTo(maxDifficulty) > 0) {
            newDifficulty = maxDifficulty;
        }
        
        logger.debug("Difficulty calculation: previous={}, target={}s, adjustment={}, penalty={}, new={}", 
                    currentDifficulty, targetTimeSeconds, adjustmentFactor, penaltyFactor, newDifficulty);
        
        return newDifficulty;
    }
    
    /**
     * 计算调整因子
     * 
     * @param previousBlock 前一区块
     * @param targetTimeSeconds 目标时间（秒）
     * @return 调整因子
     */
    private double calculateAdjustmentFactor(Block previousBlock, int targetTimeSeconds) {
        // 简化的调整策略：基于前一区块的挖矿时间
        // 实际实现中需要分析更多历史区块
        
        long blockTime = previousBlock.getTimestamp();
        long previousBlockTime = getPreviousBlockTimestamp(previousBlock);
        
        if (previousBlockTime == 0) {
            // 无法获取前一区块时间，使用默认值
            return 1.0;
        }
        
        long actualTime = blockTime - previousBlockTime;
        double timeRatio = (double) actualTime / (targetTimeSeconds * 1000);
        
        // 计算调整因子
        double adjustmentFactor = 1.0 / timeRatio;
        
        // 限制调整因子范围
        if (adjustmentFactor > ADJUSTMENT_FACTOR_MAX) {
            adjustmentFactor = ADJUSTMENT_FACTOR_MAX;
        } else if (adjustmentFactor < ADJUSTMENT_FACTOR_MIN) {
            adjustmentFactor = ADJUSTMENT_FACTOR_MIN;
        }
        
        logger.debug("Adjustment factor calculation: actualTime={}ms, targetTime={}ms, ratio={}, factor={}", 
                    actualTime, targetTimeSeconds * 1000, timeRatio, adjustmentFactor);
        
        return adjustmentFactor;
    }
    
    /**
     * 计算惩罚因子
     * 
     * @param previousBlock 前一区块
     * @return 惩罚因子
     */
    private double calculatePenaltyFactor(Block previousBlock) {
        // 简化的惩罚机制：基于VRF公告数量和签名数量
        double penaltyFactor = 1.0;
        
        // 检查VRF公告数量
        int vrfCount = previousBlock.getVRFAnnouncementCount();
        int expectedVrfCount = 50; // 期望的VRF数量
        
        if (vrfCount < expectedVrfCount * 0.8) {
            // VRF数量不足，增加难度
            penaltyFactor *= 1.5;
            logger.warn("VRF count insufficient: actual={}, expected={}, penalty={}", 
                       vrfCount, expectedVrfCount, penaltyFactor);
        }
        
        // 检查签名数量
        int signatureCount = previousBlock.getSignatureCount();
        int expectedSignatureCount = 50; // 期望的签名数量
        
        if (signatureCount < expectedSignatureCount * 0.8) {
            // 签名数量不足，增加难度
            penaltyFactor *= 1.2;
            logger.warn("Signature count insufficient: actual={}, expected={}, penalty={}", 
                       signatureCount, expectedSignatureCount, penaltyFactor);
        }
        
        // 检查交易数量
        int transactionCount = previousBlock.getTransactionCount();
        if (transactionCount == 0) {
            // 空区块，轻微增加难度
            penaltyFactor *= 1.1;
        }
        
        return penaltyFactor;
    }
    
    /**
     * 获取前一区块的时间戳
     * 
     * @param block 当前区块
     * @return 前一区块的时间戳，如果无法获取返回0
     */
    private long getPreviousBlockTimestamp(Block block) {
        // 这里需要访问区块链状态来获取前一区块
        // 在实际实现中，需要通过Blockchain服务获取
        // 目前返回0表示无法获取
        return 0;
    }
    
    /**
     * 获取基础难度
     * 
     * @return 基础难度
     */
    public BigInteger getBaseDifficulty() {
        return BASE_DIFFICULTY;
    }
    
    /**
     * 计算难度倍数
     * 
     * @param difficulty 难度值
     * @return 难度倍数（相对于基础难度）
     */
    public double getDifficultyMultiplier(BigInteger difficulty) {
        return difficulty.doubleValue() / BASE_DIFFICULTY.doubleValue();
    }
    
    /**
     * 检查难度是否合理
     * 
     * @param difficulty 难度值
     * @return true如果合理，false否则
     */
    public boolean isDifficultyReasonable(BigInteger difficulty) {
        if (difficulty == null || difficulty.compareTo(BigInteger.ZERO) <= 0) {
            return false;
        }
        
        BigInteger minDifficulty = BASE_DIFFICULTY.divide(BigInteger.valueOf(10));
        BigInteger maxDifficulty = BASE_DIFFICULTY.multiply(BigInteger.valueOf(100));
        
        return difficulty.compareTo(minDifficulty) >= 0 && 
               difficulty.compareTo(maxDifficulty) <= 0;
    }
    
    /**
     * 估算挖矿时间
     * 
     * @param difficulty 难度值
     * @param hashesPerSecond 每秒哈希数
     * @return 估算的挖矿时间（秒）
     */
    public double estimateMiningTime(BigInteger difficulty, long hashesPerSecond) {
        if (difficulty == null || difficulty.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Difficulty must be positive");
        }
        if (hashesPerSecond <= 0) {
            throw new IllegalArgumentException("Hashes per second must be positive");
        }
        
        // 简化的估算：假设平均需要尝试难度值次数的哈希
        double averageHashes = difficulty.doubleValue();
        return averageHashes / hashesPerSecond;
    }
}
