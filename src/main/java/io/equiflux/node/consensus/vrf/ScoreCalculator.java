package io.equiflux.node.consensus.vrf;

import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.crypto.VRFCalculator;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.model.VRFOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.List;
import java.util.Objects;

/**
 * 分数计算器
 * 
 * <p>负责计算VRF分数，用于出块者选择和奖励分配。
 * 
 * <p>VRF分数计算公式（按白皮书规范）：
 * <pre>
 * final_score = vrf_score × sqrt(stake_weight) × decay_factor × performance_factor
 * </pre>
 * 
 * <p>其中：
 * <ul>
 *   <li>vrf_score: VRF输出转换的分数（0.0-1.0）</li>
 *   <li>stake_weight: 权益权重（0.0-1.0）</li>
 *   <li>decay_factor: 衰减因子（0.5-1.0）</li>
 *   <li>performance_factor: 性能因子（0.7-1.0）</li>
 * </ul>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>计算单个节点的VRF分数</li>
 *   <li>排序VRF公告</li>
 *   <li>选择出块者</li>
 *   <li>确定奖励节点</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class ScoreCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(ScoreCalculator.class);
    
    private final VRFCalculator vrfCalculator;
    
    /**
     * 构造分数计算器
     * 
     * @param vrfCalculator VRF计算器
     */
    public ScoreCalculator(VRFCalculator vrfCalculator) {
        this.vrfCalculator = Objects.requireNonNull(vrfCalculator, "VRF calculator cannot be null");
    }
    
    /**
     * 计算VRF分数
     * 
     * @param publicKey 节点公钥
     * @param vrfOutput VRF输出
     * @param stakeWeight 权益权重
     * @param decayFactor 衰减因子
     * @param performanceFactor 性能因子
     * @return 最终分数（0.0-1.0）
     */
    public double calculateScore(PublicKey publicKey, VRFOutput vrfOutput, 
                                double stakeWeight, double decayFactor, double performanceFactor) {
        return vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight, decayFactor, performanceFactor);
    }
    
    /**
     * 计算VRF分数（简化版本）
     * 
     * @param publicKey 节点公钥
     * @param vrfOutput VRF输出
     * @param stakeWeight 权益权重
     * @return 最终分数（0.0-1.0）
     */
    public double calculateScore(PublicKey publicKey, VRFOutput vrfOutput, double stakeWeight) {
        return vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight);
    }
    
    /**
     * 计算权益权重
     * 
     * @param stakeAmount 节点质押数量
     * @param averageStake 平均质押数量
     * @return 权益权重（0.0-1.0）
     */
    public double calculateStakeWeight(long stakeAmount, long averageStake) {
        return vrfCalculator.calculateStakeWeight(stakeAmount, averageStake);
    }
    
    /**
     * 计算衰减因子
     * 
     * @param daysSinceElection 当选后天数
     * @return 衰减因子（0.5-1.0）
     */
    public double calculateDecayFactor(int daysSinceElection) {
        return vrfCalculator.calculateDecayFactor(daysSinceElection);
    }
    
    /**
     * 计算性能因子
     * 
     * @param uptimePercentage 在线率百分比（0-100）
     * @return 性能因子（0.7-1.0）
     */
    public double calculatePerformanceFactor(double uptimePercentage) {
        return vrfCalculator.calculatePerformanceFactor(uptimePercentage);
    }
    
    /**
     * 排序VRF公告（按分数降序）
     * 
     * @param announcements VRF公告列表
     * @return 排序后的VRF公告列表
     */
    public List<VRFAnnouncement> sortByScore(List<VRFAnnouncement> announcements) {
        if (announcements == null) {
            throw new IllegalArgumentException("VRF announcements cannot be null");
        }
        
        return announcements.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .toList();
    }
    
    /**
     * 选择出块者（分数最高的节点）
     * 
     * @param announcements VRF公告列表
     * @return 出块者VRF公告
     * @throws IllegalArgumentException 如果公告列表为空
     */
    public VRFAnnouncement selectProposer(List<VRFAnnouncement> announcements) {
        if (announcements == null || announcements.isEmpty()) {
            throw new IllegalArgumentException("VRF announcements cannot be null or empty");
        }
        
        VRFAnnouncement proposer = announcements.stream()
                .max((a, b) -> Double.compare(a.getScore(), b.getScore()))
                .orElseThrow(() -> new IllegalArgumentException("No valid proposer found"));
        
        logger.debug("Selected proposer: publicKey={}, score={}", 
                    proposer.getPublicKeyHex(), proposer.getScore());
        
        return proposer;
    }
    
    /**
     * 选择前X名奖励节点
     * 
     * @param announcements VRF公告列表
     * @param topX 前X名
     * @return 前X名VRF公告列表
     */
    public List<VRFAnnouncement> selectTopX(List<VRFAnnouncement> announcements, int topX) {
        if (announcements == null) {
            throw new IllegalArgumentException("VRF announcements cannot be null");
        }
        if (topX <= 0) {
            throw new IllegalArgumentException("Top X must be positive");
        }
        
        List<VRFAnnouncement> sorted = sortByScore(announcements);
        List<VRFAnnouncement> topXList = sorted.stream()
                .limit(topX)
                .toList();
        
        logger.debug("Selected top {} nodes: count={}", topX, topXList.size());
        
        return topXList;
    }
    
    /**
     * 检查节点是否有资格出块
     * 
     * @param publicKey 节点公钥
     * @param announcements VRF公告列表
     * @return true如果有资格，false否则
     */
    public boolean canPropose(PublicKey publicKey, List<VRFAnnouncement> announcements) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        if (announcements == null) {
            throw new IllegalArgumentException("VRF announcements cannot be null");
        }
        
        // 检查节点是否在VRF公告列表中
        boolean hasAnnouncement = announcements.stream()
                .anyMatch(announcement -> announcement.getPublicKey().equals(publicKey));
        
        if (!hasAnnouncement) {
            logger.debug("Node {} has no VRF announcement", HashUtils.toHexString(publicKey.getEncoded()));
            return false;
        }
        
        // 检查是否是分数最高的节点
        VRFAnnouncement proposer = selectProposer(announcements);
        boolean isProposer = proposer.getPublicKey().equals(publicKey);
        
        logger.debug("Node {} can propose: {}", HashUtils.toHexString(publicKey.getEncoded()), isProposer);
        
        return isProposer;
    }
    
    /**
     * 获取节点在排序中的位置
     * 
     * @param publicKey 节点公钥
     * @param announcements VRF公告列表
     * @return 位置（从1开始），如果未找到返回-1
     */
    public int getRank(PublicKey publicKey, List<VRFAnnouncement> announcements) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        if (announcements == null) {
            throw new IllegalArgumentException("VRF announcements cannot be null");
        }
        
        List<VRFAnnouncement> sorted = sortByScore(announcements);
        
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getPublicKey().equals(publicKey)) {
                return i + 1;
            }
        }
        
        return -1;
    }
    
    /**
     * 检查节点是否在前X名中
     * 
     * @param publicKey 节点公钥
     * @param announcements VRF公告列表
     * @param topX 前X名
     * @return true如果在前X名中，false否则
     */
    public boolean isInTopX(PublicKey publicKey, List<VRFAnnouncement> announcements, int topX) {
        int rank = getRank(publicKey, announcements);
        return rank > 0 && rank <= topX;
    }
    
    /**
     * 计算平均分数
     * 
     * @param announcements VRF公告列表
     * @return 平均分数
     */
    public double calculateAverageScore(List<VRFAnnouncement> announcements) {
        if (announcements == null || announcements.isEmpty()) {
            return 0.0;
        }
        
        double sum = announcements.stream()
                .mapToDouble(VRFAnnouncement::getScore)
                .sum();
        
        return sum / announcements.size();
    }
    
    /**
     * 计算分数标准差
     * 
     * @param announcements VRF公告列表
     * @return 分数标准差
     */
    public double calculateScoreStandardDeviation(List<VRFAnnouncement> announcements) {
        if (announcements == null || announcements.isEmpty()) {
            return 0.0;
        }
        
        double average = calculateAverageScore(announcements);
        
        double sumSquaredDiff = announcements.stream()
                .mapToDouble(announcement -> Math.pow(announcement.getScore() - average, 2))
                .sum();
        
        return Math.sqrt(sumSquaredDiff / announcements.size());
    }
    
    /**
     * 检查分数分布是否合理
     * 
     * @param announcements VRF公告列表
     * @return true如果分布合理，false否则
     */
    public boolean isScoreDistributionReasonable(List<VRFAnnouncement> announcements) {
        if (announcements == null || announcements.isEmpty()) {
            return false;
        }
        
        double average = calculateAverageScore(announcements);
        double standardDeviation = calculateScoreStandardDeviation(announcements);
        
        // 检查是否有异常高的分数
        boolean hasOutlier = announcements.stream()
                .anyMatch(announcement -> announcement.getScore() > average + 3 * standardDeviation);
        
        // 检查分数范围是否合理
        boolean reasonableRange = announcements.stream()
                .allMatch(announcement -> announcement.getScore() >= 0.0 && announcement.getScore() <= 1.0);
        
        return !hasOutlier && reasonableRange;
    }
}
