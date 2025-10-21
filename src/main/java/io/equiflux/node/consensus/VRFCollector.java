package io.equiflux.node.consensus;

import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.crypto.VRFKeyPair;
import io.equiflux.node.exception.ConsensusException;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.model.VRFOutput;
import io.equiflux.node.model.VRFProof;
import io.equiflux.node.consensus.vrf.ScoreCalculator;
import io.equiflux.node.consensus.vrf.VRFRoundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * VRF收集器
 * 
 * <p>负责VRF收集阶段（3秒）的逻辑，包括：
 * <ul>
 *   <li>计算VRF输入</li>
 *   <li>计算本节点VRF</li>
 *   <li>广播VRF公告（模拟）</li>
 *   <li>收集其他节点VRF</li>
 *   <li>验证所有VRF</li>
 *   <li>确定出块者和前15名</li>
 * </ul>
 * 
 * <p>VRF收集流程：
 * <ol>
 *   <li>计算VRF输入：H(prev_block_hash || round || epoch)</li>
 *   <li>计算本节点VRF输出和证明</li>
 *   <li>广播VRF公告</li>
 *   <li>收集其他节点的VRF公告（等待3秒）</li>
 *   <li>验证所有VRF的合法性</li>
 *   <li>计算分数并排序</li>
 *   <li>确定出块者和前15名</li>
 * </ol>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class VRFCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(VRFCollector.class);
    
    private final ScoreCalculator scoreCalculator;
    
    /**
     * 构造VRF收集器
     * 
     * @param scoreCalculator 分数计算器
     */
    public VRFCollector(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }
    
    /**
     * 收集VRF（同步版本）
     * 
     * @param round 轮次
     * @param previousBlockHash 前一区块哈希
     * @param epoch 纪元
     * @param myVrfKeyPair 本节点VRF密钥对
     * @param timeoutMs 超时时间（毫秒）
     * @return VRF轮次结果
     * @throws ConsensusException 如果收集失败
     */
    public VRFRoundResult collectVRFs(long round, byte[] previousBlockHash, long epoch,
                                     VRFKeyPair myVrfKeyPair, long timeoutMs) {
        if (previousBlockHash == null) {
            throw new IllegalArgumentException("Previous block hash cannot be null");
        }
        if (myVrfKeyPair == null) {
            throw new IllegalArgumentException("VRF key pair cannot be null");
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        
        logger.info("Starting VRF collection: round={}, timeout={}ms", round, timeoutMs);
        
        try {
            // 1. 计算VRF输入
            byte[] vrfInput = HashUtils.computeVRFInput(previousBlockHash, round, epoch);
            
            // 2. 计算本节点VRF
            VRFOutput myVrfOutput = myVrfKeyPair.evaluate(vrfInput);
            VRFAnnouncement myAnnouncement = createVRFAnnouncement(
                round, myVrfKeyPair.getPublicKey(), myVrfOutput, myVrfKeyPair
            );
            
            // 3. 广播VRF公告（模拟）
            broadcastVRFAnnouncement(myAnnouncement);
            
            // 4. 收集其他节点VRF
            List<VRFAnnouncement> collectedAnnouncements = collectOtherVRFs(round, timeoutMs);
            collectedAnnouncements.add(myAnnouncement);
            
            // 5. 验证所有VRF
            List<VRFAnnouncement> validAnnouncements = validateVRFs(collectedAnnouncements, vrfInput);
            
            // 6. 确定出块者和前15名
            VRFAnnouncement winner = scoreCalculator.selectProposer(validAnnouncements);
            List<VRFAnnouncement> top15 = scoreCalculator.selectTopX(validAnnouncements, 15);
            
            VRFRoundResult result = new VRFRoundResult(winner, top15, validAnnouncements);
            
            logger.info("VRF collection completed: round={}, valid={}, winner={}, top15={}", 
                       round, validAnnouncements.size(), winner.getPublicKeyHex(), top15.size());
            
            return result;
        } catch (Exception e) {
            logger.error("VRF collection failed for round {}", round, e);
            throw new ConsensusException("VRF collection failed", e);
        }
    }
    
    /**
     * 收集VRF（异步版本）
     * 
     * @param round 轮次
     * @param previousBlockHash 前一区块哈希
     * @param epoch 纪元
     * @param myVrfKeyPair 本节点VRF密钥对
     * @param timeoutMs 超时时间（毫秒）
     * @return VRF轮次结果的CompletableFuture
     */
    public CompletableFuture<VRFRoundResult> collectVRFsAsync(long round, byte[] previousBlockHash, long epoch,
                                                             VRFKeyPair myVrfKeyPair, long timeoutMs) {
        return CompletableFuture.supplyAsync(() -> 
            collectVRFs(round, previousBlockHash, epoch, myVrfKeyPair, timeoutMs)
        );
    }
    
    /**
     * 创建VRF公告
     * 
     * @param round 轮次
     * @param publicKey 公钥
     * @param vrfOutput VRF输出
     * @param vrfKeyPair VRF密钥对（用于生成证明）
     * @return VRF公告
     */
    private VRFAnnouncement createVRFAnnouncement(long round, PublicKey publicKey, VRFOutput vrfOutput, VRFKeyPair vrfKeyPair) {
        // 计算分数（使用默认参数）
        double stakeWeight = 1.0; // 简化：假设所有节点权益相等
        double score = scoreCalculator.calculateScore(publicKey, vrfOutput, stakeWeight);
        
        return new VRFAnnouncement(round, publicKey, vrfOutput, vrfOutput.getProof(), score);
    }
    
    /**
     * 广播VRF公告（模拟）
     * 
     * @param announcement VRF公告
     */
    private void broadcastVRFAnnouncement(VRFAnnouncement announcement) {
        // 在实际实现中，这里会通过P2P网络广播
        // 目前只是记录日志
        logger.debug("Broadcasting VRF announcement: round={}, publicKey={}, score={}", 
                    announcement.getRound(), announcement.getPublicKeyHex(), announcement.getScore());
    }
    
    /**
     * 收集其他节点的VRF公告
     * 
     * @param round 轮次
     * @param timeoutMs 超时时间（毫秒）
     * @return 收集到的VRF公告列表
     */
    private List<VRFAnnouncement> collectOtherVRFs(long round, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        List<VRFAnnouncement> collected = new ArrayList<>();
        
        logger.debug("Collecting VRF announcements: round={}, timeout={}ms", round, timeoutMs);
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            // 模拟从网络收集VRF公告
            List<VRFAnnouncement> newAnnouncements = simulateVRFCollection(round);
            collected.addAll(newAnnouncements);
            
            // 避免重复添加
            collected = collected.stream()
                    .collect(Collectors.toMap(
                        VRFAnnouncement::getPublicKey,
                        announcement -> announcement,
                        (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            
            try {
                Thread.sleep(100); // 避免忙等待
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        logger.debug("VRF collection completed: round={}, collected={}", round, collected.size());
        return collected;
    }
    
    /**
     * 模拟VRF收集（返回模拟的VRF公告）
     * 
     * @param round 轮次
     * @return 模拟的VRF公告列表
     */
    private List<VRFAnnouncement> simulateVRFCollection(long round) {
        // 在实际实现中，这里会从网络层获取VRF公告
        // 目前返回模拟数据
        List<VRFAnnouncement> announcements = new ArrayList<>();
        
        // 模拟生成一些VRF公告
        for (int i = 0; i < 10; i++) {
            try {
                VRFKeyPair keyPair = VRFKeyPair.generate();
                byte[] vrfInput = HashUtils.computeVRFInput(new byte[32], round, 1);
                VRFOutput vrfOutput = keyPair.evaluate(vrfInput);
                
                double score = Math.random(); // 模拟随机分数
                VRFAnnouncement announcement = new VRFAnnouncement(
                    round, keyPair.getPublicKey(), vrfOutput, vrfOutput.getProof(), score
                );
                
                announcements.add(announcement);
            } catch (Exception e) {
                logger.warn("Failed to generate simulated VRF announcement", e);
            }
        }
        
        return announcements;
    }
    
    /**
     * 验证VRF公告
     * 
     * @param announcements VRF公告列表
     * @param vrfInput VRF输入
     * @return 有效的VRF公告列表
     */
    private List<VRFAnnouncement> validateVRFs(List<VRFAnnouncement> announcements, byte[] vrfInput) {
        List<VRFAnnouncement> validAnnouncements = new ArrayList<>();
        
        for (VRFAnnouncement announcement : announcements) {
            try {
                // 验证VRF证明
                boolean isValid = VRFKeyPair.verify(
                    announcement.getPublicKey(),
                    vrfInput,
                    announcement.getVrfOutput(),
                    announcement.getVrfProof()
                );
                
                if (isValid) {
                    validAnnouncements.add(announcement);
                } else {
                    logger.warn("Invalid VRF proof for public key: {}", 
                               announcement.getPublicKeyHex());
                }
            } catch (Exception e) {
                logger.warn("VRF validation failed for public key: {}", 
                           announcement.getPublicKeyHex(), e);
            }
        }
        
        logger.debug("VRF validation completed: total={}, valid={}", 
                    announcements.size(), validAnnouncements.size());
        
        return validAnnouncements;
    }
    
    /**
     * 检查是否有足够的VRF公告
     * 
     * @param announcements VRF公告列表
     * @param minRequired 最小要求数量
     * @return true如果有足够数量，false否则
     */
    public boolean hasEnoughVRFs(List<VRFAnnouncement> announcements, int minRequired) {
        return announcements.size() >= minRequired;
    }
    
    /**
     * 获取VRF收集统计信息
     * 
     * @param announcements VRF公告列表
     * @return 统计信息
     */
    public Map<String, Object> getCollectionStats(List<VRFAnnouncement> announcements) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalCount", announcements.size());
        stats.put("averageScore", scoreCalculator.calculateAverageScore(announcements));
        stats.put("standardDeviation", scoreCalculator.calculateScoreStandardDeviation(announcements));
        stats.put("isDistributionReasonable", scoreCalculator.isScoreDistributionReasonable(announcements));
        
        if (!announcements.isEmpty()) {
            VRFAnnouncement highest = announcements.stream()
                    .max(Comparator.comparing(VRFAnnouncement::getScore))
                    .orElse(null);
            VRFAnnouncement lowest = announcements.stream()
                    .min(Comparator.comparing(VRFAnnouncement::getScore))
                    .orElse(null);
            
            if (highest != null) {
                stats.put("highestScore", highest.getScore());
                stats.put("highestScorePublicKey", highest.getPublicKeyHex());
            }
            if (lowest != null) {
                stats.put("lowestScore", lowest.getScore());
                stats.put("lowestScorePublicKey", lowest.getPublicKeyHex());
            }
        }
        
        return stats;
    }
}
