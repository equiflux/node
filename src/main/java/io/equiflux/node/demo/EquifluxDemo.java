package io.equiflux.node.demo;

import io.equiflux.node.crypto.VRFKeyPair;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.config.ConsensusConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Equiflux Node 演示程序
 * 
 * <p>展示Equiflux共识引擎的基本功能：
 * <ul>
 *   <li>VRF密钥对生成</li>
 *   <li>区块构造</li>
 *   <li>共识流程演示</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@SpringBootApplication
public class EquifluxDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(EquifluxDemo.class);
    
    public static void main(String[] args) {
        SpringApplication.run(EquifluxDemo.class, args);
    }
    
    @Bean
    public CommandLineRunner demo() {
        return args -> {
            logger.info("=== Equiflux Node Demo ===");
            
            // 1. 生成VRF密钥对
            logger.info("1. Generating VRF key pair...");
            VRFKeyPair vrfKeyPair = VRFKeyPair.generate();
            logger.info("   Public Key: {}", vrfKeyPair.getPublicKeyHex());
            logger.info("   Private Key: {}", vrfKeyPair.getPrivateKeyHex());
            
            // 2. 创建配置
            logger.info("2. Creating consensus configuration...");
            ConsensusConfig config = new ConsensusConfig();
            config.setSuperNodeCount(10);
            config.setBlockTimeSeconds(3);
            config.setVrfCollectionTimeoutMs(1000);
            config.setBlockProductionTimeoutMs(2000);
            config.setRewardedTopX(5);
            config.setPowBaseDifficulty(BigInteger.valueOf(1000000));
            logger.info("   Super Node Count: {}", config.getSuperNodeCount());
            logger.info("   Block Time: {}s", config.getBlockTimeSeconds());
            logger.info("   VRF Collection Timeout: {}ms", config.getVrfCollectionTimeoutMs());
            
            // 3. 创建创世区块
            logger.info("3. Creating genesis block...");
            Block genesisBlock = createGenesisBlock(vrfKeyPair);
            logger.info("   Genesis Block Hash: {}", genesisBlock.getHashHex());
            logger.info("   Genesis Block Height: {}", genesisBlock.getHeight());
            
            // 4. 创建测试交易
            logger.info("4. Creating test transactions...");
            List<Transaction> transactions = createTestTransactions();
            logger.info("   Created {} transactions", transactions.size());
            
            // 5. 演示VRF计算
            logger.info("5. Demonstrating VRF calculation...");
            byte[] vrfInput = "test input".getBytes();
            var vrfOutput = vrfKeyPair.evaluate(vrfInput);
            logger.info("   VRF Input: {}", new String(vrfInput));
            logger.info("   VRF Output: {}", vrfOutput.toHexString());
            logger.info("   VRF Score: {}", vrfOutput.toScore());
            
            // 6. 演示区块验证
            logger.info("6. Demonstrating block validation...");
            boolean isValid = genesisBlock.isValidFormat();
            logger.info("   Genesis Block Valid: {}", isValid);
            
            // 7. 演示共识状态
            logger.info("7. Demonstrating consensus status...");
            logger.info("   Current Height: {}", genesisBlock.getHeight());
            logger.info("   Current Round: {}", genesisBlock.getRound());
            logger.info("   Block Size: {}B", estimateBlockSize(genesisBlock));
            
            logger.info("=== Demo Completed ===");
        };
    }
    
    /**
     * 创建创世区块
     */
    private Block createGenesisBlock(VRFKeyPair vrfKeyPair) {
        return new Block.Builder()
                .height(0)
                .round(0)
                .timestamp(System.currentTimeMillis())
                .previousHash(new byte[32])
                .proposer(vrfKeyPair.getPublicKey().getEncoded())
                .vrfOutput(new byte[32])
                .vrfProof(new io.equiflux.node.model.VRFProof(new byte[64]))
                .allVRFAnnouncements(new ArrayList<>())
                .rewardedNodes(new ArrayList<>())
                .transactions(new ArrayList<>())
                .nonce(0)
                .difficultyTarget(BigInteger.valueOf(1000000))
                .signatures(new java.util.HashMap<>())
                .build();
    }
    
    /**
     * 创建测试交易
     */
    private List<Transaction> createTestTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        
        try {
            VRFKeyPair senderKeyPair = VRFKeyPair.generate();
            VRFKeyPair receiverKeyPair = VRFKeyPair.generate();
            
            Transaction transaction = new Transaction(
                senderKeyPair.getPublicKey().getEncoded(),
                receiverKeyPair.getPublicKey().getEncoded(),
                1000L,
                10L,
                System.currentTimeMillis(),
                1L,
                new byte[64], // 模拟签名
                new byte[32], // 模拟哈希
                io.equiflux.node.model.TransactionType.TRANSFER
            );
            
            transactions.add(transaction);
        } catch (Exception e) {
            logger.warn("Failed to create test transaction", e);
        }
        
        return transactions;
    }
    
    /**
     * 估算区块大小
     */
    private long estimateBlockSize(Block block) {
        long baseSize = 8 + 4 + 8 + 32 + 32 + 32 + 32; // 基础字段
        long vrfSize = block.getVRFAnnouncementCount() * 200; // 每个VRF公告约200字节
        long transactionSize = block.getTransactionCount() * 200; // 每个交易约200字节
        long signatureSize = block.getSignatureCount() * 64; // 每个签名64字节
        
        return baseSize + vrfSize + transactionSize + signatureSize;
    }
}
