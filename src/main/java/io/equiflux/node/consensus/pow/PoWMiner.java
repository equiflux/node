package io.equiflux.node.consensus.pow;

import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.exception.ConsensusException;
import io.equiflux.node.model.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * PoW矿工
 * 
 * <p>实现轻量级工作量证明（Lightweight Proof of Work），用于增加单次作恶成本。
 * 
 * <p>PoW特点：
 * <ul>
 *   <li>轻量级：2-3秒可完成（普通CPU）</li>
 *   <li>动态难度：根据网络状况调整</li>
 *   <li>防护作用：增加作恶成本，而非竞争记账权</li>
 * </ul>
 * 
 * <p>PoW算法：
 * <ul>
 *   <li>哈希算法：SHA-256</li>
 *   <li>目标：hash(block_header) < difficulty_target</li>
 *   <li>搜索：递增nonce直到满足条件</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
public class PoWMiner {
    
    private static final Logger logger = LoggerFactory.getLogger(PoWMiner.class);
    
    private final DifficultyCalculator difficultyCalculator;
    
    /**
     * 构造PoW矿工
     * 
     * @param difficultyCalculator 难度计算器
     */
    public PoWMiner(DifficultyCalculator difficultyCalculator) {
        this.difficultyCalculator = difficultyCalculator;
    }
    
    /**
     * 挖矿（同步版本）
     * 
     * @param block 区块
     * @param timeoutSeconds 超时时间（秒）
     * @return 挖矿后的区块
     * @throws ConsensusException 如果挖矿失败或超时
     */
    public Block mine(Block block, int timeoutSeconds) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        
        long startTime = System.currentTimeMillis();
        BigInteger target = block.getDifficultyTarget();
        long nonce = 0;
        
        logger.debug("Starting PoW mining: target={}, timeout={}s", target, timeoutSeconds);
        
        while (true) {
            // 检查超时
            if (System.currentTimeMillis() - startTime > timeoutSeconds * 1000L) {
                throw new ConsensusException("PoW mining timeout after " + timeoutSeconds + " seconds");
            }
            
            // 尝试当前nonce
            if (tryNonce(block, nonce, target)) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("PoW mining completed: nonce={}, duration={}ms", nonce, duration);
                
                // 返回挖矿后的区块
                return createMinedBlock(block, nonce);
            }
            
            nonce++;
            
            // 检查是否被中断
            if (Thread.currentThread().isInterrupted()) {
                throw new ConsensusException("PoW mining interrupted");
            }
        }
    }
    
    /**
     * 挖矿（异步版本）
     * 
     * @param block 区块
     * @param timeoutSeconds 超时时间（秒）
     * @return 挖矿结果的CompletableFuture
     */
    public CompletableFuture<Block> mineAsync(Block block, int timeoutSeconds) {
        return CompletableFuture.supplyAsync(() -> mine(block, timeoutSeconds));
    }
    
    /**
     * 尝试指定的nonce
     * 
     * @param block 区块
     * @param nonce nonce值
     * @param target 难度目标
     * @return true如果满足条件，false否则
     */
    private boolean tryNonce(Block block, long nonce, BigInteger target) {
        // 构造区块头（包含nonce）
        byte[] blockHeader = createBlockHeader(block, nonce);
        
        // 计算哈希
        byte[] hash = HashUtils.sha256(blockHeader);
        
        // 转换为BigInteger进行比较
        BigInteger hashValue = new BigInteger(1, hash);
        
        // 检查是否满足难度条件
        return hashValue.compareTo(target) < 0;
    }
    
    /**
     * 构造区块头（用于PoW）
     * 
     * @param block 区块
     * @param nonce nonce值
     * @return 区块头字节数组
     */
    private byte[] createBlockHeader(Block block, long nonce) {
        // 构造区块头数据（排除签名字段）
        byte[] heightBytes = longToBytes(block.getHeight());
        byte[] roundBytes = intToBytes(block.getRound());
        byte[] timestampBytes = longToBytes(block.getTimestamp());
        byte[] nonceBytes = longToBytes(nonce);
        byte[] difficultyBytes = block.getDifficultyTarget().toByteArray();
        
        // 连接所有数据
        return HashUtils.sha256(heightBytes, roundBytes, timestampBytes, 
                               block.getPreviousHash(), block.getProposer(),
                               block.getVrfOutput(), block.getMerkleRoot(), 
                               nonceBytes, difficultyBytes);
    }
    
    /**
     * 创建挖矿后的区块
     * 
     * @param originalBlock 原始区块
     * @param nonce 挖矿得到的nonce
     * @return 挖矿后的区块
     */
    private Block createMinedBlock(Block originalBlock, long nonce) {
        // 这里需要重新构造区块，因为Block是不可变的
        // 在实际实现中，可能需要使用Builder模式或者重新设计Block类
        return new Block(
            originalBlock.getHeight(),
            originalBlock.getRound(),
            originalBlock.getTimestamp(),
            originalBlock.getPreviousHash(),
            originalBlock.getProposer(),
            originalBlock.getVrfOutput(),
            originalBlock.getVrfProof(),
            originalBlock.getAllVRFAnnouncements(),
            originalBlock.getRewardedNodes(),
            originalBlock.getTransactions(),
            nonce,
            originalBlock.getDifficultyTarget(),
            originalBlock.getSignatures()
        );
    }
    
    /**
     * 验证PoW
     * 
     * @param block 区块
     * @return true如果PoW有效，false否则
     */
    public boolean verifyPoW(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        
        try {
            // 构造区块头
            byte[] blockHeader = createBlockHeader(block, block.getNonce());
            
            // 计算哈希
            byte[] hash = HashUtils.sha256(blockHeader);
            
            // 转换为BigInteger进行比较
            BigInteger hashValue = new BigInteger(1, hash);
            
            // 检查是否满足难度条件
            boolean isValid = hashValue.compareTo(block.getDifficultyTarget()) < 0;
            
            logger.debug("PoW verification: block={}, nonce={}, valid={}", 
                        block.getHeight(), block.getNonce(), isValid);
            
            return isValid;
        } catch (Exception e) {
            logger.error("PoW verification failed", e);
            return false;
        }
    }
    
    /**
     * 计算PoW难度
     * 
     * @param previousBlock 前一区块
     * @param targetTimeSeconds 目标时间（秒）
     * @return 新的难度目标
     */
    public BigInteger calculateDifficulty(Block previousBlock, int targetTimeSeconds) {
        return difficultyCalculator.calculateDifficulty(previousBlock, targetTimeSeconds);
    }
    
    /**
     * 估算挖矿时间
     * 
     * @param difficulty 难度目标
     * @return 估算的挖矿时间（毫秒）
     */
    public long estimateMiningTime(BigInteger difficulty) {
        // 简化的估算：假设每秒可以尝试1,000,000次哈希
        long hashesPerSecond = 1_000_000L;
        BigInteger totalHashes = difficulty.multiply(BigInteger.valueOf(hashesPerSecond));
        
        // 转换为毫秒
        return totalHashes.divide(BigInteger.valueOf(hashesPerSecond)).longValue() * 1000;
    }
    
    /**
     * 将long值转换为8字节数组（大端序）
     * 
     * @param value long值
     * @return 8字节数组
     */
    private byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }
    
    /**
     * 将int值转换为4字节数组（大端序）
     * 
     * @param value int值
     * @return 4字节数组
     */
    private byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        for (int i = 3; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }
}
