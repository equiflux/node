package io.equiflux.node.consensus;

import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.crypto.SignatureVerifier;
import io.equiflux.node.crypto.VRFKeyPair;
import io.equiflux.node.exception.ValidationException;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.consensus.pow.PoWMiner;
import io.equiflux.node.consensus.vrf.ScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 区块验证器
 * 
 * <p>实现完整的区块验证逻辑，按照白皮书规范进行5步验证：
 * <ol>
 *   <li><strong>Step 1</strong>: VRF完整性验证（数量、证明、分数）</li>
 *   <li><strong>Step 2</strong>: 出块者合法性验证（必须是最高分）</li>
 *   <li><strong>Step 3</strong>: 奖励分配验证（前15名正确性）</li>
 *   <li><strong>Step 4</strong>: PoW验证</li>
 *   <li><strong>Step 5</strong>: 交易验证</li>
 * </ol>
 * 
 * <p>验证过程：
 * <ul>
 *   <li>VRF完整性：检查VRF公告数量、证明有效性、分数计算</li>
 *   <li>出块者合法性：验证出块者确实是分数最高的节点</li>
 *   <li>奖励分配：验证前15名节点的正确性</li>
 *   <li>PoW验证：检查工作量证明的有效性</li>
 *   <li>交易验证：验证所有交易的签名和格式</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class BlockValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockValidator.class);
    
    private final SignatureVerifier signatureVerifier;
    private final PoWMiner powMiner;
    private final ScoreCalculator scoreCalculator;
    
    /**
     * 构造区块验证器
     * 
     * @param signatureVerifier 签名验证器
     * @param powMiner PoW矿工
     * @param scoreCalculator 分数计算器
     */
    public BlockValidator(SignatureVerifier signatureVerifier, PoWMiner powMiner, ScoreCalculator scoreCalculator) {
        this.signatureVerifier = Objects.requireNonNull(signatureVerifier, "Signature verifier cannot be null");
        this.powMiner = Objects.requireNonNull(powMiner, "PoW miner cannot be null");
        this.scoreCalculator = Objects.requireNonNull(scoreCalculator, "Score calculator cannot be null");
    }
    
    /**
     * 验证区块
     * 
     * @param block 区块
     * @param previousBlockHash 前一区块哈希
     * @param round 轮次
     * @param epoch 纪元
     * @return true如果验证通过，false否则
     * @throws ValidationException 如果验证过程失败
     */
    public boolean verifyBlock(Block block, byte[] previousBlockHash, long round, long epoch) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        if (previousBlockHash == null) {
            throw new IllegalArgumentException("Previous block hash cannot be null");
        }
        
        logger.debug("Starting block verification: height={}, round={}", block.getHeight(), round);
        
        try {
            // Step 1: VRF完整性验证
            if (!verifyVRFIntegrity(block, previousBlockHash, round, epoch)) {
                logger.warn("VRF integrity verification failed for block {}", block.getHeight());
                return false;
            }
            
            // Step 2: 出块者合法性验证
            if (!verifyProposerLegitimacy(block)) {
                logger.warn("Proposer legitimacy verification failed for block {}", block.getHeight());
                return false;
            }
            
            // Step 3: 奖励分配验证
            if (!verifyRewardDistribution(block)) {
                logger.warn("Reward distribution verification failed for block {}", block.getHeight());
                return false;
            }
            
            // Step 4: PoW验证
            if (!verifyPoW(block)) {
                logger.warn("PoW verification failed for block {}", block.getHeight());
                return false;
            }
            
            // Step 5: 交易验证
            if (!verifyTransactions(block)) {
                logger.warn("Transaction verification failed for block {}", block.getHeight());
                return false;
            }
            
            logger.info("Block verification completed successfully: height={}", block.getHeight());
            return true;
        } catch (Exception e) {
            logger.error("Block verification failed for block {}", block.getHeight(), e);
            throw new ValidationException("Block verification failed", e);
        }
    }
    
    /**
     * Step 1: VRF完整性验证
     * 
     * @param block 区块
     * @param previousBlockHash 前一区块哈希
     * @param round 轮次
     * @param epoch 纪元
     * @return true如果VRF完整性验证通过，false否则
     */
    private boolean verifyVRFIntegrity(Block block, byte[] previousBlockHash, long round, long epoch) {
        logger.debug("Verifying VRF integrity for block {}", block.getHeight());
        
        // 计算VRF输入
        byte[] vrfInput = HashUtils.computeVRFInput(previousBlockHash, round, epoch);
        
        // 检查VRF公告数量
        List<VRFAnnouncement> vrfAnnouncements = block.getAllVRFAnnouncements();
        if (vrfAnnouncements.isEmpty()) {
            logger.warn("No VRF announcements in block {}", block.getHeight());
            return false;
        }
        
        // 验证每个VRF公告
        for (VRFAnnouncement announcement : vrfAnnouncements) {
            // 验证轮次
            if (announcement.getRound() != round) {
                logger.warn("VRF announcement round mismatch: expected={}, actual={}", 
                           round, announcement.getRound());
                return false;
            }
            
            // 验证VRF证明
            boolean isValidProof = VRFKeyPair.verify(
                announcement.getPublicKey(),
                vrfInput,
                announcement.getVrfOutput(),
                announcement.getVrfProof()
            );
            
            if (!isValidProof) {
                logger.warn("Invalid VRF proof for public key: {}", 
                           announcement.getPublicKeyHex());
                return false;
            }
            
            // 验证分数范围
            if (announcement.getScore() < 0.0 || announcement.getScore() > 1.0) {
                logger.warn("Invalid VRF score: {}", announcement.getScore());
                return false;
            }
        }
        
        logger.debug("VRF integrity verification passed for block {}", block.getHeight());
        return true;
    }
    
    /**
     * Step 2: 出块者合法性验证
     * 
     * @param block 区块
     * @return true如果出块者合法性验证通过，false否则
     */
    private boolean verifyProposerLegitimacy(Block block) {
        logger.debug("Verifying proposer legitimacy for block {}", block.getHeight());
        
        List<VRFAnnouncement> vrfAnnouncements = block.getAllVRFAnnouncements();
        
        // 找到出块者的VRF公告
        VRFAnnouncement proposerAnnouncement = vrfAnnouncements.stream()
                .filter(announcement -> Arrays.equals(announcement.getPublicKey().getEncoded(), block.getProposer()))
                .findFirst()
                .orElse(null);
        
        if (proposerAnnouncement == null) {
            logger.warn("Proposer not found in VRF announcements for block {}", block.getHeight());
            return false;
        }
        
        // 验证出块者确实是分数最高的
        boolean isHighestScore = vrfAnnouncements.stream()
                .allMatch(announcement -> announcement.getScore() <= proposerAnnouncement.getScore());
        
        if (!isHighestScore) {
            logger.warn("Proposer is not the highest score for block {}", block.getHeight());
            return false;
        }
        
        // 验证VRF输出匹配
        if (!Arrays.equals(block.getVrfOutput(), proposerAnnouncement.getVrfOutput().getOutput())) {
            logger.warn("VRF output mismatch for proposer in block {}", block.getHeight());
            return false;
        }
        
        logger.debug("Proposer legitimacy verification passed for block {}", block.getHeight());
        return true;
    }
    
    /**
     * Step 3: 奖励分配验证
     * 
     * @param block 区块
     * @return true如果奖励分配验证通过，false否则
     */
    private boolean verifyRewardDistribution(Block block) {
        logger.debug("Verifying reward distribution for block {}", block.getHeight());
        
        List<VRFAnnouncement> vrfAnnouncements = block.getAllVRFAnnouncements();
        List<byte[]> rewardedNodes = block.getRewardedNodes();
        
        // 按分数排序VRF公告
        List<VRFAnnouncement> sortedAnnouncements = scoreCalculator.sortByScore(vrfAnnouncements);
        
        // 检查奖励节点数量
        if (rewardedNodes.size() != 15) {
            logger.warn("Invalid rewarded nodes count: expected=15, actual={}", rewardedNodes.size());
            return false;
        }
        
        // 检查前15名是否正确
        for (int i = 0; i < 15 && i < sortedAnnouncements.size(); i++) {
            VRFAnnouncement expectedAnnouncement = sortedAnnouncements.get(i);
            byte[] expectedPublicKeyBytes = expectedAnnouncement.getPublicKey().getEncoded();
            
            boolean found = rewardedNodes.stream()
                    .anyMatch(rewardedNodeBytes -> Arrays.equals(rewardedNodeBytes, expectedPublicKeyBytes));
            
            if (!found) {
                logger.warn("Rewarded node mismatch at position {}: expected={}, actual={}", 
                           i, HashUtils.toHexString(expectedPublicKeyBytes), 
                           rewardedNodes.stream().map(HashUtils::toHexString).toList());
                return false;
            }
        }
        
        logger.debug("Reward distribution verification passed for block {}", block.getHeight());
        return true;
    }
    
    /**
     * Step 4: PoW验证
     * 
     * @param block 区块
     * @return true如果PoW验证通过，false否则
     */
    private boolean verifyPoW(Block block) {
        logger.debug("Verifying PoW for block {}", block.getHeight());
        
        // 使用PoW矿工验证
        boolean isValidPoW = powMiner.verifyPoW(block);
        
        if (!isValidPoW) {
            logger.warn("PoW verification failed for block {}", block.getHeight());
            return false;
        }
        
        logger.debug("PoW verification passed for block {}", block.getHeight());
        return true;
    }
    
    /**
     * Step 5: 交易验证
     * 
     * @param block 区块
     * @return true如果交易验证通过，false否则
     */
    private boolean verifyTransactions(Block block) {
        logger.debug("Verifying transactions for block {}", block.getHeight());
        
        List<Transaction> transactions = block.getTransactions();
        
        // 验证每个交易
        for (Transaction transaction : transactions) {
            // 验证交易格式
            if (!transaction.isValidFormat()) {
                logger.warn("Invalid transaction format: {}", transaction.getHashHex());
                return false;
            }
            
            // 验证交易签名
            if (!transaction.verifySignature()) {
                logger.warn("Invalid transaction signature: {}", transaction.getHashHex());
                return false;
            }
        }
        
        // 验证Merkle根
        byte[] calculatedMerkleRoot = calculateMerkleRoot(transactions);
        if (!Arrays.equals(block.getMerkleRoot(), calculatedMerkleRoot)) {
            logger.warn("Merkle root mismatch for block {}", block.getHeight());
            return false;
        }
        
        logger.debug("Transaction verification passed for block {}", block.getHeight());
        return true;
    }
    
    /**
     * 计算Merkle根
     * 
     * @param transactions 交易列表
     * @return Merkle根
     */
    private byte[] calculateMerkleRoot(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return new byte[32];
        }
        
        byte[][] transactionHashes = transactions.stream()
                .map(Transaction::getHash)
                .toArray(byte[][]::new);
        
        return HashUtils.computeMerkleRoot(transactionHashes);
    }
    
    /**
     * 验证区块签名
     * 
     * @param block 区块
     * @return true如果签名验证通过，false否则
     */
    public boolean verifyBlockSignatures(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        
        return signatureVerifier.verifyAllBlockSignatures(block);
    }
    
    /**
     * 验证区块交易签名
     * 
     * @param block 区块
     * @return true如果交易签名验证通过，false否则
     */
    public boolean verifyTransactionSignatures(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        
        return signatureVerifier.verifyAllTransactionSignatures(block);
    }
    
    /**
     * 获取验证统计信息
     * 
     * @param block 区块
     * @return 验证统计信息
     */
    public java.util.Map<String, Object> getValidationStats(Block block) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("height", block.getHeight());
        stats.put("round", block.getRound());
        stats.put("hash", block.getHashHex());
        stats.put("proposer", block.getProposerHex());
        stats.put("vrfAnnouncements", block.getVRFAnnouncementCount());
        stats.put("rewardedNodes", block.getRewardedNodes().size());
        stats.put("transactions", block.getTransactionCount());
        stats.put("signatures", block.getSignatureCount());
        stats.put("nonce", block.getNonce());
        stats.put("difficulty", block.getDifficultyTarget());
        
        return stats;
    }
}
