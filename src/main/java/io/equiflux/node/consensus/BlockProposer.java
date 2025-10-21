package io.equiflux.node.consensus;

import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.exception.ConsensusException;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.consensus.pow.PoWMiner;
import io.equiflux.node.consensus.vrf.VRFRoundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * 区块提议器
 * 
 * <p>负责区块构造和出块，实现完整的区块提议流程：
 * <ol>
 *   <li>构造区块头</li>
 *   <li>填充VRF信息和所有VRF公告</li>
 *   <li>选择交易</li>
 *   <li>计算Merkle根</li>
 *   <li>执行PoW</li>
 *   <li>广播区块（模拟）</li>
 * </ol>
 * 
 * <p>区块构造过程：
 * <ul>
 *   <li>基础信息：高度、轮次、时间戳、前一区块哈希</li>
 *   <li>出块者VRF信息：公钥、VRF输出、VRF证明</li>
 *   <li>所有VRF公告列表（关键！约5KB）</li>
 *   <li>奖励节点列表（前15名）</li>
 *   <li>交易列表和Merkle根</li>
 *   <li>PoW信息：nonce、难度目标</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class BlockProposer {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockProposer.class);
    
    private final PoWMiner powMiner;
    
    /**
     * 构造区块提议器
     * 
     * @param powMiner PoW矿工
     */
    public BlockProposer(PoWMiner powMiner) {
        this.powMiner = powMiner;
    }
    
    /**
     * 提议区块
     * 
     * @param height 区块高度
     * @param round 轮次
     * @param previousBlockHash 前一区块哈希
     * @param vrfResult VRF轮次结果
     * @param transactions 交易列表
     * @param difficultyTarget PoW难度目标
     * @return 提议的区块
     * @throws ConsensusException 如果区块提议失败
     */
    public Block proposeBlock(long height, int round, byte[] previousBlockHash,
                              VRFRoundResult vrfResult, List<Transaction> transactions,
                              BigInteger difficultyTarget) {
        if (previousBlockHash == null) {
            throw new IllegalArgumentException("Previous block hash cannot be null");
        }
        if (vrfResult == null) {
            throw new IllegalArgumentException("VRF result cannot be null");
        }
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions cannot be null");
        }
        if (difficultyTarget == null) {
            throw new IllegalArgumentException("Difficulty target cannot be null");
        }
        
        logger.info("Proposing block: height={}, round={}, transactions={}", 
                   height, round, transactions.size());
        
        try {
            // 1. 构造区块头
            Block.Builder blockBuilder = new Block.Builder()
                    .height(height)
                    .round(round)
                    .timestamp(System.currentTimeMillis())
                    .previousHash(previousBlockHash);
            
            // 2. 填充VRF信息
            VRFAnnouncement winner = vrfResult.getWinner();
            blockBuilder.proposer(winner.getPublicKey().getEncoded())
                       .vrfOutput(winner.getVrfOutput().getOutput())
                       .vrfProof(winner.getVrfProof());
            
            // 3. 填充所有VRF公告（关键！）
            blockBuilder.allVRFAnnouncements(vrfResult.getAllValid());
            
            // 4. 填充奖励节点列表（前15名）
            blockBuilder.rewardedNodes(vrfResult.getTopXPublicKeys().stream()
                    .map(PublicKey::getEncoded)
                    .collect(Collectors.toList()));
            
            // 5. 填充交易列表
            blockBuilder.transactions(transactions);
            
            // 6. 设置PoW难度目标
            blockBuilder.difficultyTarget(difficultyTarget);
            
            // 7. 构造初始区块（nonce=0）
            Block initialBlock = blockBuilder.nonce(0).build();
            
            // 8. 执行PoW挖矿
            Block minedBlock = powMiner.mine(initialBlock, 3); // 3秒超时
            
            logger.info("Block proposed successfully: height={}, hash={}, nonce={}", 
                       minedBlock.getHeight(), minedBlock.getHashHex(), minedBlock.getNonce());
            
            return minedBlock;
        } catch (Exception e) {
            logger.error("Block proposal failed for height {}", height, e);
            throw new ConsensusException("Block proposal failed", e);
        }
    }
    
    /**
     * 提议区块（异步版本）
     * 
     * @param height 区块高度
     * @param round 轮次
     * @param previousBlockHash 前一区块哈希
     * @param vrfResult VRF轮次结果
     * @param transactions 交易列表
     * @param difficultyTarget PoW难度目标
     * @return 提议区块的CompletableFuture
     */
    public CompletableFuture<Block> proposeBlockAsync(long height, int round, byte[] previousBlockHash,
                                                      VRFRoundResult vrfResult, List<Transaction> transactions,
                                                      BigInteger difficultyTarget) {
        return CompletableFuture.supplyAsync(() -> 
            proposeBlock(height, round, previousBlockHash, vrfResult, transactions, difficultyTarget)
        );
    }
    
    /**
     * 选择交易
     * 
     * @param availableTransactions 可用交易列表
     * @param maxTransactions 最大交易数量
     * @param maxBlockSize 最大区块大小（字节）
     * @return 选择的交易列表
     */
    public List<Transaction> selectTransactions(List<Transaction> availableTransactions, 
                                              int maxTransactions, long maxBlockSize) {
        if (availableTransactions == null) {
            throw new IllegalArgumentException("Available transactions cannot be null");
        }
        if (maxTransactions <= 0) {
            throw new IllegalArgumentException("Max transactions must be positive");
        }
        if (maxBlockSize <= 0) {
            throw new IllegalArgumentException("Max block size must be positive");
        }
        
        List<Transaction> selected = new ArrayList<>();
        long currentSize = 0;
        
        // 按手续费率排序（手续费/交易大小）
        List<Transaction> sortedTransactions = availableTransactions.stream()
                .sorted((a, b) -> {
                    double feeRateA = (double) a.getFee() / a.getHash().length;
                    double feeRateB = (double) b.getFee() / b.getHash().length;
                    return Double.compare(feeRateB, feeRateA);
                })
                .toList();
        
        for (Transaction transaction : sortedTransactions) {
            // 检查是否超过最大交易数量
            if (selected.size() >= maxTransactions) {
                break;
            }
            
            // 估算交易大小（简化）
            long transactionSize = estimateTransactionSize(transaction);
            
            // 检查是否超过最大区块大小
            if (currentSize + transactionSize > maxBlockSize) {
                break;
            }
            
            selected.add(transaction);
            currentSize += transactionSize;
        }
        
        logger.debug("Transaction selection completed: available={}, selected={}, size={}B", 
                    availableTransactions.size(), selected.size(), currentSize);
        
        return selected;
    }
    
    /**
     * 估算交易大小
     * 
     * @param transaction 交易
     * @return 估算的交易大小（字节）
     */
    private long estimateTransactionSize(Transaction transaction) {
        // 简化的估算：公钥(32) + 公钥(32) + 金额(8) + 手续费(8) + 时间戳(8) + nonce(8) + 签名(64) + 哈希(32)
        return 32 + 32 + 8 + 8 + 8 + 8 + 64 + 32;
    }
    
    /**
     * 验证区块提议
     * 
     * @param block 区块
     * @param vrfResult VRF轮次结果
     * @return true如果提议有效，false否则
     */
    public boolean validateProposal(Block block, VRFRoundResult vrfResult) {
        if (block == null || vrfResult == null) {
            return false;
        }
        
        try {
            // 检查出块者是否匹配
            if (!Arrays.equals(block.getProposer(), vrfResult.getWinnerPublicKey().getEncoded())) {
                logger.warn("Block proposer mismatch: block={}, vrf={}", 
                           HashUtils.toHexString(block.getProposer()),
                           vrfResult.getWinnerPublicKeyHex());
                return false;
            }
            
            // 检查VRF输出是否匹配
            if (!Arrays.equals(block.getVrfOutput(), vrfResult.getWinner().getVrfOutput().getOutput())) {
                logger.warn("VRF output mismatch");
                return false;
            }
            
            // 检查VRF公告数量
            if (block.getVRFAnnouncementCount() != vrfResult.getAllValidCount()) {
                logger.warn("VRF announcement count mismatch: block={}, vrf={}", 
                           block.getVRFAnnouncementCount(), vrfResult.getAllValidCount());
                return false;
            }
            
            // 检查奖励节点数量
            if (block.getRewardedNodes().size() != vrfResult.getTopXCount()) {
                logger.warn("Rewarded nodes count mismatch: block={}, vrf={}", 
                           block.getRewardedNodes().size(), vrfResult.getTopXCount());
                return false;
            }
            
            // 检查PoW
            if (!powMiner.verifyPoW(block)) {
                logger.warn("PoW verification failed");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Block proposal validation failed", e);
            return false;
        }
    }
    
    /**
     * 广播区块（模拟）
     * 
     * @param block 区块
     */
    public void broadcastBlock(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        
        // 在实际实现中，这里会通过P2P网络广播区块
        logger.info("Broadcasting block: height={}, hash={}, size={}B", 
                   block.getHeight(), block.getHashHex(), estimateBlockSize(block));
    }
    
    /**
     * 估算区块大小
     * 
     * @param block 区块
     * @return 估算的区块大小（字节）
     */
    private long estimateBlockSize(Block block) {
        // 简化的估算
        long baseSize = 8 + 4 + 8 + 32 + 32 + 32 + 32; // 基础字段
        long vrfSize = block.getVRFAnnouncementCount() * 200; // 每个VRF公告约200字节
        long transactionSize = block.getTransactionCount() * 200; // 每个交易约200字节
        long signatureSize = block.getSignatureCount() * 64; // 每个签名64字节
        
        return baseSize + vrfSize + transactionSize + signatureSize;
    }
    
    /**
     * 获取区块提议统计信息
     * 
     * @param block 区块
     * @return 统计信息
     */
    public Map<String, Object> getProposalStats(Block block) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("height", block.getHeight());
        stats.put("round", block.getRound());
        stats.put("hash", block.getHashHex());
        stats.put("timestamp", block.getTimestamp());
        stats.put("proposer", block.getProposerHex());
        stats.put("vrfAnnouncements", block.getVRFAnnouncementCount());
        stats.put("transactions", block.getTransactionCount());
        stats.put("signatures", block.getSignatureCount());
        stats.put("nonce", block.getNonce());
        stats.put("difficulty", block.getDifficultyTarget());
        stats.put("estimatedSize", estimateBlockSize(block));
        
        return stats;
    }
}
