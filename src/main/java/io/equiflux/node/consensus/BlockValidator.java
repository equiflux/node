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

import java.math.BigInteger;
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
        
        // 检查VRF公告数量是否足够（至少需要2/3的超级节点）
        int minRequiredVRFs = (int) Math.ceil(50 * 2.0 / 3.0); // 50个超级节点的2/3
        if (vrfAnnouncements.size() < minRequiredVRFs) {
            logger.warn("Insufficient VRF announcements: required={}, actual={}", 
                       minRequiredVRFs, vrfAnnouncements.size());
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
            
            // 验证公钥格式
            if (announcement.getPublicKey() == null) {
                logger.warn("VRF announcement has null public key");
                return false;
            }
            
            // 验证VRF输出格式
            if (announcement.getVrfOutput() == null || 
                announcement.getVrfOutput().getOutput() == null ||
                announcement.getVrfOutput().getOutput().length != 32) {
                logger.warn("Invalid VRF output format for public key: {}", 
                           announcement.getPublicKeyHex());
                return false;
            }
            
            // 验证VRF证明格式
            if (announcement.getVrfProof() == null || 
                announcement.getVrfProof().getProof() == null ||
                announcement.getVrfProof().getProof().length != 64) {
                logger.warn("Invalid VRF proof format for public key: {}", 
                           announcement.getPublicKeyHex());
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
                logger.warn("Invalid VRF score: {} for public key: {}", 
                           announcement.getScore(), announcement.getPublicKeyHex());
                return false;
            }
            
            // 验证时间戳合理性（不能太旧）
            long currentTime = System.currentTimeMillis();
            long announcementAge = currentTime - announcement.getTimestamp();
            if (announcementAge > 30000) { // 30秒超时
                logger.warn("VRF announcement too old: {}ms for public key: {}", 
                           announcementAge, announcement.getPublicKeyHex());
                return false;
            }
        }
        
        // 检查是否有重复的公钥
        long uniquePublicKeys = vrfAnnouncements.stream()
                .map(announcement -> announcement.getPublicKey())
                .distinct()
                .count();
        
        if (uniquePublicKeys != vrfAnnouncements.size()) {
            logger.warn("Duplicate public keys found in VRF announcements");
            return false;
        }
        
        logger.debug("VRF integrity verification passed for block {}: {} announcements", 
                    block.getHeight(), vrfAnnouncements.size());
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
        
        // 验证出块者公钥格式
        if (block.getProposer() == null || block.getProposer().length == 0) {
            logger.warn("Block proposer is null or empty");
            return false;
        }
        
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
            // 记录详细信息用于调试
            VRFAnnouncement highestScoreAnnouncement = vrfAnnouncements.stream()
                    .max((a, b) -> Double.compare(a.getScore(), b.getScore()))
                    .orElse(null);
            if (highestScoreAnnouncement != null) {
                logger.warn("Highest score node: {}, score: {}, proposer score: {}", 
                           highestScoreAnnouncement.getPublicKeyHex(), 
                           highestScoreAnnouncement.getScore(),
                           proposerAnnouncement.getScore());
            }
            return false;
        }
        
        // 验证VRF输出匹配
        if (!Arrays.equals(block.getVrfOutput(), proposerAnnouncement.getVrfOutput().getOutput())) {
            logger.warn("VRF output mismatch for proposer in block {}", block.getHeight());
            return false;
        }
        
        // 验证VRF证明匹配
        if (!Arrays.equals(block.getVrfProof().getProof(), proposerAnnouncement.getVrfProof().getProof())) {
            logger.warn("VRF proof mismatch for proposer in block {}", block.getHeight());
            return false;
        }
        
        // 验证出块者时间戳合理性
        long currentTime = System.currentTimeMillis();
        long proposerAnnouncementAge = currentTime - proposerAnnouncement.getTimestamp();
        if (proposerAnnouncementAge > 30000) { // 30秒超时
            logger.warn("Proposer VRF announcement too old: {}ms", proposerAnnouncementAge);
            return false;
        }
        
        logger.debug("Proposer legitimacy verification passed for block {}: proposer={}, score={}", 
                    block.getHeight(), proposerAnnouncement.getPublicKeyHex(), proposerAnnouncement.getScore());
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
        
        // 验证奖励节点列表不为空
        if (rewardedNodes == null) {
            logger.warn("Rewarded nodes list is null for block {}", block.getHeight());
            return false;
        }
        
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
        
        // 检查奖励节点是否有重复
        long uniqueRewardedNodes = rewardedNodes.stream()
                .map(nodeBytes -> Arrays.hashCode(nodeBytes))
                .distinct()
                .count();
        
        if (uniqueRewardedNodes != rewardedNodes.size()) {
            logger.warn("Duplicate nodes found in rewarded nodes list");
            return false;
        }
        
        // 验证所有奖励节点都在VRF公告中
        for (byte[] rewardedNodeBytes : rewardedNodes) {
            boolean foundInVRF = vrfAnnouncements.stream()
                    .anyMatch(announcement -> Arrays.equals(announcement.getPublicKey().getEncoded(), rewardedNodeBytes));
            
            if (!foundInVRF) {
                logger.warn("Rewarded node not found in VRF announcements: {}", 
                           HashUtils.toHexString(rewardedNodeBytes));
                return false;
            }
        }
        
        // 验证奖励节点的分数确实在前15名
        if (sortedAnnouncements.size() >= 15) {
            double fifteenthScore = sortedAnnouncements.get(14).getScore();
            for (byte[] rewardedNodeBytes : rewardedNodes) {
                VRFAnnouncement announcement = vrfAnnouncements.stream()
                        .filter(ann -> Arrays.equals(ann.getPublicKey().getEncoded(), rewardedNodeBytes))
                        .findFirst()
                        .orElse(null);
                
                if (announcement != null && announcement.getScore() < fifteenthScore) {
                    logger.warn("Rewarded node score {} is lower than 15th place score {}", 
                               announcement.getScore(), fifteenthScore);
                    return false;
                }
            }
        }
        
        logger.debug("Reward distribution verification passed for block {}: {} rewarded nodes", 
                    block.getHeight(), rewardedNodes.size());
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
        
        // 验证PoW字段
        if (block.getNonce() < 0) {
            logger.warn("Invalid nonce: {}", block.getNonce());
            return false;
        }
        
        if (block.getDifficultyTarget() == null || block.getDifficultyTarget().compareTo(BigInteger.ZERO) <= 0) {
            logger.warn("Invalid difficulty target: {}", block.getDifficultyTarget());
            return false;
        }
        
        // 使用PoW矿工验证
        boolean isValidPoW = powMiner.verifyPoW(block);
        
        if (!isValidPoW) {
            logger.warn("PoW verification failed for block {}", block.getHeight());
            return false;
        }
        
        // 验证难度目标是否合理（不能太高或太低）
        BigInteger maxDifficulty = BigInteger.valueOf(2).pow(256).subtract(BigInteger.ONE);
        if (block.getDifficultyTarget().compareTo(maxDifficulty) > 0) {
            logger.warn("Difficulty target too high: {}", block.getDifficultyTarget());
            return false;
        }
        
        // 验证nonce是否在合理范围内
        if (block.getNonce() > Long.MAX_VALUE / 2) {
            logger.warn("Nonce too large: {}", block.getNonce());
            return false;
        }
        
        logger.debug("PoW verification passed for block {}: nonce={}, difficulty={}", 
                    block.getHeight(), block.getNonce(), block.getDifficultyTarget());
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
        
        // 验证交易列表不为null
        if (transactions == null) {
            logger.warn("Transaction list is null for block {}", block.getHeight());
            return false;
        }
        
        // 验证交易数量限制（防止区块过大）
        if (transactions.size() > 10000) { // 最大10000笔交易
            logger.warn("Too many transactions in block: {}", transactions.size());
            return false;
        }
        
        // 验证每个交易
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            
            // 验证交易不为null
            if (transaction == null) {
                logger.warn("Transaction at index {} is null", i);
                return false;
            }
            
            // 验证交易格式
            if (!transaction.isValidFormat()) {
                logger.warn("Invalid transaction format at index {}: {}", i, transaction.getHashHex());
                return false;
            }
            
            // 验证交易签名
            if (!transaction.verifySignature()) {
                logger.warn("Invalid transaction signature at index {}: {}", i, transaction.getHashHex());
                return false;
            }
            
            // 验证交易金额合理性
            if (transaction.getAmount() < 0 || transaction.getFee() < 0) {
                logger.warn("Invalid transaction amounts at index {}: amount={}, fee={}", 
                           i, transaction.getAmount(), transaction.getFee());
                return false;
            }
            
            // 验证交易时间戳合理性
            long currentTime = System.currentTimeMillis();
            long transactionAge = currentTime - transaction.getTimestamp();
            if (transactionAge > 300000) { // 5分钟超时
                logger.warn("Transaction too old at index {}: {}ms", i, transactionAge);
                return false;
            }
            
            // 验证nonce合理性
            if (transaction.getNonce() < 0) {
                logger.warn("Invalid transaction nonce at index {}: {}", i, transaction.getNonce());
                return false;
            }
        }
        
        // 检查交易是否有重复
        long uniqueTransactions = transactions.stream()
                .map(Transaction::getHash)
                .map(Arrays::hashCode)
                .distinct()
                .count();
        
        if (uniqueTransactions != transactions.size()) {
            logger.warn("Duplicate transactions found in block {}", block.getHeight());
            return false;
        }
        
        // 验证Merkle根
        byte[] calculatedMerkleRoot = calculateMerkleRoot(transactions);
        if (!Arrays.equals(block.getMerkleRoot(), calculatedMerkleRoot)) {
            logger.warn("Merkle root mismatch for block {}: expected={}, actual={}", 
                       block.getHeight(), 
                       HashUtils.toHexString(calculatedMerkleRoot),
                       HashUtils.toHexString(block.getMerkleRoot()));
            return false;
        }
        
        logger.debug("Transaction verification passed for block {}: {} transactions", 
                    block.getHeight(), transactions.size());
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
     * 验证区块格式
     * 
     * @param block 区块
     * @return true如果格式正确，false否则
     */
    public boolean verifyBlockFormat(Block block) {
        if (block == null) {
            logger.warn("Block is null");
            return false;
        }
        
        return block.isValidFormat();
    }
    
    /**
     * 验证区块时间戳
     * 
     * @param block 区块
     * @param maxAgeMs 最大年龄（毫秒）
     * @return true如果时间戳有效，false否则
     */
    public boolean verifyBlockTimestamp(Block block, long maxAgeMs) {
        if (block == null) {
            logger.warn("Block is null");
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long blockAge = currentTime - block.getTimestamp();
        
        if (blockAge > maxAgeMs) {
            logger.warn("Block too old: {}ms", blockAge);
            return false;
        }
        
        if (blockAge < 0) {
            logger.warn("Block timestamp in future: {}ms", blockAge);
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证区块高度
     * 
     * @param block 区块
     * @param expectedHeight 期望高度
     * @return true如果高度正确，false否则
     */
    public boolean verifyBlockHeight(Block block, long expectedHeight) {
        if (block == null) {
            logger.warn("Block is null");
            return false;
        }
        
        if (block.getHeight() != expectedHeight) {
            logger.warn("Block height mismatch: expected={}, actual={}", 
                       expectedHeight, block.getHeight());
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证区块轮次
     * 
     * @param block 区块
     * @param expectedRound 期望轮次
     * @return true如果轮次正确，false否则
     */
    public boolean verifyBlockRound(Block block, int expectedRound) {
        if (block == null) {
            logger.warn("Block is null");
            return false;
        }
        
        if (block.getRound() != expectedRound) {
            logger.warn("Block round mismatch: expected={}, actual={}", 
                       expectedRound, block.getRound());
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证区块哈希
     * 
     * @param block 区块
     * @param expectedHash 期望哈希
     * @return true如果哈希正确，false否则
     */
    public boolean verifyBlockHash(Block block, byte[] expectedHash) {
        if (block == null) {
            logger.warn("Block is null");
            return false;
        }
        
        if (expectedHash == null) {
            logger.warn("Expected hash is null");
            return false;
        }
        
        byte[] actualHash = block.getHash();
        if (!Arrays.equals(actualHash, expectedHash)) {
            logger.warn("Block hash mismatch: expected={}, actual={}", 
                       HashUtils.toHexString(expectedHash),
                       HashUtils.toHexString(actualHash));
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证区块签名数量
     * 
     * @param block 区块
     * @param minSignatures 最小签名数量
     * @return true如果签名数量足够，false否则
     */
    public boolean verifySignatureCount(Block block, int minSignatures) {
        if (block == null) {
            logger.warn("Block is null");
            return false;
        }
        
        int signatureCount = block.getSignatureCount();
        if (signatureCount < minSignatures) {
            logger.warn("Insufficient signatures: required={}, actual={}", 
                       minSignatures, signatureCount);
            return false;
        }
        
        return true;
    }
    
    /**
     * 快速验证区块（只验证基本格式）
     * 
     * @param block 区块
     * @return true如果基本验证通过，false否则
     */
    public boolean quickVerify(Block block) {
        if (block == null) {
            return false;
        }
        
        // 基本格式验证
        if (!verifyBlockFormat(block)) {
            return false;
        }
        
        // 时间戳验证（1小时超时）
        if (!verifyBlockTimestamp(block, 3600000)) {
            return false;
        }
        
        // 基本字段验证
        if (block.getHeight() < 0 || block.getRound() < 0) {
            return false;
        }
        
        if (block.getPreviousHash() == null || block.getPreviousHash().length != 32) {
            return false;
        }
        
        if (block.getProposer() == null || block.getProposer().length == 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取验证统计信息
     * 
     * @param block 区块
     * @return 验证统计信息
     */
    public java.util.Map<String, Object> getValidationStats(Block block) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        if (block != null) {
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
            stats.put("timestamp", block.getTimestamp());
            stats.put("merkleRoot", HashUtils.toHexString(block.getMerkleRoot()));
        }
        
        return stats;
    }
}
