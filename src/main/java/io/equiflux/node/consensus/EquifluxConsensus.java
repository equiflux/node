package io.equiflux.node.consensus;

import io.equiflux.node.crypto.VRFKeyPair;
import io.equiflux.node.exception.ConsensusException;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.config.ConsensusConfig;
import io.equiflux.node.consensus.vrf.VRFRoundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Equiflux共识引擎
 * 
 * <p>整合所有组件，实现完整的Equiflux共识流程：
 * <ol>
 *   <li><strong>VRF收集阶段</strong>（3秒）：收集所有节点的VRF公告</li>
 *   <li><strong>区块提议/等待阶段</strong>（5秒）：出块者构造区块并执行PoW</li>
 *   <li><strong>区块验证阶段</strong>：验证区块的完整性和合法性</li>
 * </ol>
 * 
 * <p>共识流程：
 * <ul>
 *   <li>Phase 1: VRF收集（3秒）</li>
 *   <li>Phase 2: 区块生产（5秒）</li>
 *   <li>Phase 3: 区块验证（实时）</li>
 * </ul>
 * 
 * <p>关键特性：
 * <ul>
 *   <li>完全透明：区块包含所有VRF公告</li>
 *   <li>实时可验证：无需历史挑战</li>
 *   <li>高性能：目标1800 TPS</li>
 *   <li>低延迟：3秒出块，8秒确认</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class EquifluxConsensus implements ConsensusEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(EquifluxConsensus.class);
    
    private final ConsensusConfig config;
    private final VRFCollector vrfCollector;
    private final BlockProposer blockProposer;
    private final BlockValidator blockValidator;
    
    // 状态管理
    private final AtomicLong currentHeight = new AtomicLong(0);
    private final AtomicLong currentRound = new AtomicLong(0);
    private final AtomicLong currentEpoch = new AtomicLong(1);
    
    /**
     * 构造Equiflux共识引擎
     * 
     * @param config 共识配置
     * @param vrfCollector VRF收集器
     * @param blockProposer 区块提议器
     * @param blockValidator 区块验证器
     */
    public EquifluxConsensus(ConsensusConfig config, VRFCollector vrfCollector, 
                            BlockProposer blockProposer, BlockValidator blockValidator) {
        this.config = config;
        this.vrfCollector = vrfCollector;
        this.blockProposer = blockProposer;
        this.blockValidator = blockValidator;
    }
    
    /**
     * 生产区块
     * 
     * @param myVrfKeyPair 本节点VRF密钥对
     * @param previousBlock 前一区块
     * @param availableTransactions 可用交易列表
     * @return 生产的区块
     * @throws ConsensusException 如果区块生产失败
     */
    public Block produceBlock(VRFKeyPair myVrfKeyPair, Block previousBlock, 
                             List<Transaction> availableTransactions) {
        if (myVrfKeyPair == null) {
            throw new IllegalArgumentException("VRF key pair cannot be null");
        }
        if (previousBlock == null) {
            throw new IllegalArgumentException("Previous block cannot be null");
        }
        if (availableTransactions == null) {
            throw new IllegalArgumentException("Available transactions cannot be null");
        }
        
        long height = previousBlock.getHeight() + 1;
        long round = currentRound.incrementAndGet();
        long epoch = currentEpoch.get();
        
        logger.info("Starting block production: height={}, round={}, epoch={}", height, round, epoch);
        
        try {
            // Phase 1: VRF收集阶段（3秒）
            logger.debug("Phase 1: VRF collection started");
            VRFRoundResult vrfResult = vrfCollector.collectVRFs(
                round, 
                previousBlock.getHash(), 
                epoch, 
                myVrfKeyPair, 
                config.getVrfCollectionTimeoutMs()
            );
            
            // 检查是否有足够的VRF
            if (!vrfResult.hasEnoughVRFs(config.getMinVrfRequired())) {
                throw new ConsensusException("Insufficient VRF announcements: required=" + 
                                           config.getMinVrfRequired() + ", actual=" + vrfResult.getAllValidCount());
            }
            
            // 检查本节点是否有资格出块
            if (!vrfResult.getWinnerPublicKey().equals(myVrfKeyPair.getPublicKey())) {
                logger.info("Not selected as proposer for round {}", round);
                return null; // 本节点不是出块者
            }
            
            logger.info("Selected as proposer for round {}", round);
            
            // Phase 2: 区块生产阶段（5秒）
            logger.debug("Phase 2: Block production started");
            
            // 选择交易
            List<Transaction> selectedTransactions = blockProposer.selectTransactions(
                availableTransactions, 
                config.getMaxTransactionsPerBlock(), 
                config.getMaxBlockSizeBytes()
            );
            
            // 计算PoW难度
            BigInteger difficultyTarget = calculateDifficulty(previousBlock);
            
            // 提议区块
            Block proposedBlock = blockProposer.proposeBlock(
                height, 
                (int) round, 
                previousBlock.getHash(), 
                vrfResult, 
                selectedTransactions, 
                difficultyTarget
            );
            
            // Phase 3: 区块验证阶段
            logger.debug("Phase 3: Block validation started");
            boolean isValid = blockValidator.verifyBlock(
                proposedBlock, 
                previousBlock.getHash(), 
                round, 
                epoch
            );
            
            if (!isValid) {
                throw new ConsensusException("Block validation failed");
            }
            
            // 广播区块
            blockProposer.broadcastBlock(proposedBlock);
            
            // 更新状态
            currentHeight.set(height);
            
            logger.info("Block production completed: height={}, hash={}, transactions={}", 
                       height, proposedBlock.getHashHex(), selectedTransactions.size());
            
            return proposedBlock;
        } catch (Exception e) {
            logger.error("Block production failed for height {}", height, e);
            throw new ConsensusException("Block production failed", e);
        }
    }
    
    /**
     * 生产区块（异步版本）
     * 
     * @param myVrfKeyPair 本节点VRF密钥对
     * @param previousBlock 前一区块
     * @param availableTransactions 可用交易列表
     * @return 生产区块的CompletableFuture
     */
    public CompletableFuture<Block> produceBlockAsync(VRFKeyPair myVrfKeyPair, Block previousBlock, 
                                                      List<Transaction> availableTransactions) {
        return CompletableFuture.supplyAsync(() -> 
            produceBlock(myVrfKeyPair, previousBlock, availableTransactions)
        );
    }
    
    /**
     * 验证区块
     * 
     * @param block 区块
     * @param previousBlock 前一区块
     * @return true如果验证通过，false否则
     */
    public boolean verifyBlock(Block block, Block previousBlock) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        if (previousBlock == null) {
            throw new IllegalArgumentException("Previous block cannot be null");
        }
        
        long round = block.getRound();
        long epoch = currentEpoch.get();
        
        logger.debug("Verifying block: height={}, round={}", block.getHeight(), round);
        
        try {
            return blockValidator.verifyBlock(block, previousBlock.getHash(), round, epoch);
        } catch (Exception e) {
            logger.error("Block verification failed for block {}", block.getHeight(), e);
            return false;
        }
    }
    
    /**
     * 计算PoW难度
     * 
     * @param previousBlock 前一区块
     * @return 新的难度目标
     */
    private BigInteger calculateDifficulty(Block previousBlock) {
        // 简化的难度计算：使用基础难度
        // 在实际实现中，需要根据网络状况动态调整
        return config.getPowBaseDifficulty();
    }
    
    /**
     * 获取当前高度
     * 
     * @return 当前高度
     */
    public long getCurrentHeight() {
        return currentHeight.get();
    }
    
    /**
     * 获取当前轮次
     * 
     * @return 当前轮次
     */
    public long getCurrentRound() {
        return currentRound.get();
    }
    
    /**
     * 获取当前纪元
     * 
     * @return 当前纪元
     */
    public long getCurrentEpoch() {
        return currentEpoch.get();
    }
    
    /**
     * 设置当前高度
     * 
     * @param height 高度
     */
    public void setCurrentHeight(long height) {
        currentHeight.set(height);
    }
    
    /**
     * 设置当前轮次
     * 
     * @param round 轮次
     */
    public void setCurrentRound(long round) {
        currentRound.set(round);
    }
    
    /**
     * 设置当前纪元
     * 
     * @param epoch 纪元
     */
    public void setCurrentEpoch(long epoch) {
        currentEpoch.set(epoch);
    }
    
    /**
     * 获取共识状态
     * 
     * @return 共识状态信息
     */
    public java.util.Map<String, Object> getConsensusStatus() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        
        status.put("currentHeight", getCurrentHeight());
        status.put("currentRound", getCurrentRound());
        status.put("currentEpoch", getCurrentEpoch());
        status.put("superNodeCount", config.getSuperNodeCount());
        status.put("blockTimeSeconds", config.getBlockTimeSeconds());
        status.put("vrfCollectionTimeoutMs", config.getVrfCollectionTimeoutMs());
        status.put("blockProductionTimeoutMs", config.getBlockProductionTimeoutMs());
        status.put("rewardedTopX", config.getRewardedTopX());
        status.put("powBaseDifficulty", config.getPowBaseDifficulty());
        
        return status;
    }
    
    /**
     * 检查共识是否正常运行
     * 
     * @return true如果正常运行，false否则
     */
    public boolean isConsensusHealthy() {
        try {
            // 检查配置
            config.validate();
            
            // 检查状态
            if (getCurrentHeight() < 0 || getCurrentRound() < 0 || getCurrentEpoch() < 0) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Consensus health check failed", e);
            return false;
        }
    }
    
    /**
     * 重置共识状态
     */
    public void resetConsensus() {
        logger.info("Resetting consensus state");
        currentHeight.set(0);
        currentRound.set(0);
        currentEpoch.set(1);
    }
    
    /**
     * 获取共识统计信息
     * 
     * @return 共识统计信息
     */
    public java.util.Map<String, Object> getConsensusStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("height", getCurrentHeight());
        stats.put("round", getCurrentRound());
        stats.put("epoch", getCurrentEpoch());
        stats.put("isHealthy", isConsensusHealthy());
        stats.put("config", getConsensusStatus());
        
        return stats;
    }
}
