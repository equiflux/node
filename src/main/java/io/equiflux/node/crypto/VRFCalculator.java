package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;
import io.equiflux.node.model.VRFOutput;
import io.equiflux.node.model.VRFProof;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Objects;

/**
 * VRF计算器
 * 
 * <p>提供VRF（可验证随机函数）的计算和验证功能。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>VRF输出计算</li>
 *   <li>VRF证明验证</li>
 *   <li>VRF分数计算</li>
 * </ul>
 * 
 * <p>VRF分数计算公式：
 * <pre>
 * final_score = vrf_score × sqrt(stake_weight) × decay_factor × performance_factor
 * </pre>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class VRFCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(VRFCalculator.class);
    
    /**
     * 计算VRF得分
     * 
     * <p>根据白皮书规范，VRF得分计算公式为：
     * <pre>
     * final_score = vrf_score × sqrt(stake_weight) × decay_factor × performance_factor
     * </pre>
     * 
     * @param publicKey 节点公钥
     * @param vrfOutput VRF输出
     * @param stakeWeight 权益权重（0.0-1.0）
     * @param decayFactor 衰减因子（0.5-1.0）
     * @param performanceFactor 性能因子（0.7-1.0）
     * @return 最终得分（0.0-1.0）
     */
    public double calculateScore(PublicKey publicKey, VRFOutput vrfOutput, 
                               double stakeWeight, double decayFactor, double performanceFactor) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        if (vrfOutput == null) {
            throw new IllegalArgumentException("VRF output cannot be null");
        }
        
        // 验证参数范围
        if (stakeWeight < 0.0 || stakeWeight > 1.0) {
            throw new IllegalArgumentException("Stake weight must be between 0.0 and 1.0");
        }
        if (decayFactor < 0.5 || decayFactor > 1.0) {
            throw new IllegalArgumentException("Decay factor must be between 0.5 and 1.0");
        }
        if (performanceFactor < 0.7 || performanceFactor > 1.0) {
            throw new IllegalArgumentException("Performance factor must be between 0.7 and 1.0");
        }
        
        // 计算VRF基础分数
        double vrfScore = vrfOutput.toScore();
        
        // 应用权益权重（使用平方根）
        double adjustedStakeWeight = Math.sqrt(stakeWeight);
        
        // 计算最终得分
        double finalScore = vrfScore * adjustedStakeWeight * decayFactor * performanceFactor;
        
        // 确保得分在有效范围内
        finalScore = Math.max(0.0, Math.min(1.0, finalScore));
        
        logger.debug("VRF score calculation: vrf={}, stake={}, decay={}, performance={}, final={}", 
                    vrfScore, stakeWeight, decayFactor, performanceFactor, finalScore);
        
        return finalScore;
    }
    
    /**
     * 计算VRF得分（简化版本）
     * 
     * <p>使用默认的衰减因子和性能因子计算得分。
     * 
     * @param publicKey 节点公钥
     * @param vrfOutput VRF输出
     * @param stakeWeight 权益权重
     * @return 最终得分
     */
    public double calculateScore(PublicKey publicKey, VRFOutput vrfOutput, double stakeWeight) {
        return calculateScore(publicKey, vrfOutput, stakeWeight, 1.0, 1.0);
    }
    
    /**
     * 验证VRF输出和证明
     * 
     * @param publicKey 公钥
     * @param input VRF输入
     * @param output VRF输出
     * @param proof VRF证明
     * @return true如果验证通过，false否则
     */
    public boolean verify(PublicKey publicKey, byte[] input, VRFOutput output, VRFProof proof) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("VRF input cannot be null");
        }
        if (output == null) {
            throw new IllegalArgumentException("VRF output cannot be null");
        }
        if (proof == null) {
            throw new IllegalArgumentException("VRF proof cannot be null");
        }
        
        try {
            return VRFKeyPair.verify(publicKey, input, output, proof);
        } catch (Exception e) {
            logger.error("VRF verification failed for public key: {}", 
                        HashUtils.toHexString(publicKey.getEncoded()), e);
            return false;
        }
    }
    
    /**
     * 计算权益权重
     * 
     * <p>根据节点的质押数量和平均质押数量计算权益权重。
     * 
     * @param stakeAmount 节点质押数量
     * @param averageStake 平均质押数量
     * @return 权益权重（0.0-1.0）
     */
    public double calculateStakeWeight(long stakeAmount, long averageStake) {
        if (stakeAmount < 0) {
            throw new IllegalArgumentException("Stake amount cannot be negative");
        }
        if (averageStake <= 0) {
            throw new IllegalArgumentException("Average stake must be positive");
        }
        
        // 权益权重 = min(stake_amount / avg_stake, 1.0)
        double weight = Math.min((double) stakeAmount / averageStake, 1.0);
        
        logger.debug("Stake weight calculation: stake={}, avg={}, weight={}", 
                    stakeAmount, averageStake, weight);
        
        return weight;
    }
    
    /**
     * 计算衰减因子
     * 
     * <p>根据节点当选后的时间计算衰减因子。
     * 
     * @param daysSinceElection 当选后天数
     * @return 衰减因子（0.5-1.0）
     */
    public double calculateDecayFactor(int daysSinceElection) {
        if (daysSinceElection < 0) {
            throw new IllegalArgumentException("Days since election cannot be negative");
        }
        
        // 衰减因子 = max(0.5, 1.0 - days_since_election/180)
        double decayFactor = Math.max(0.5, 1.0 - (double) daysSinceElection / 180.0);
        
        logger.debug("Decay factor calculation: days={}, factor={}", 
                    daysSinceElection, decayFactor);
        
        return decayFactor;
    }
    
    /**
     * 计算性能因子
     * 
     * <p>根据节点的在线率计算性能因子。
     * 
     * @param uptimePercentage 在线率百分比（0-100）
     * @return 性能因子（0.7-1.0）
     */
    public double calculatePerformanceFactor(double uptimePercentage) {
        if (uptimePercentage < 0.0 || uptimePercentage > 100.0) {
            throw new IllegalArgumentException("Uptime percentage must be between 0.0 and 100.0");
        }
        
        double performanceFactor;
        if (uptimePercentage >= 99.0) {
            performanceFactor = 1.0;
        } else if (uptimePercentage >= 95.0) {
            performanceFactor = 0.95;
        } else if (uptimePercentage >= 90.0) {
            performanceFactor = 0.85;
        } else {
            performanceFactor = 0.7;
        }
        
        logger.debug("Performance factor calculation: uptime={}%, factor={}", 
                    uptimePercentage, performanceFactor);
        
        return performanceFactor;
    }
}
