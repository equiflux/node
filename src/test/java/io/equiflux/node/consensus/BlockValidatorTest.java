package io.equiflux.node.consensus;

import io.equiflux.node.crypto.Ed25519KeyPair;
import io.equiflux.node.crypto.SignatureVerifier;
import io.equiflux.node.model.*;
import io.equiflux.node.consensus.pow.PoWMiner;
import io.equiflux.node.consensus.vrf.ScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BlockValidator单元测试
 * 
 * <p>测试区块验证器的各种验证逻辑，包括：
 * <ul>
 *   <li>基本格式验证</li>
 *   <li>时间戳验证</li>
 *   <li>高度验证</li>
 *   <li>轮次验证</li>
 *   <li>快速验证</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class BlockValidatorTest {
    
    @Mock
    private SignatureVerifier signatureVerifier;
    
    @Mock
    private PoWMiner powMiner;
    
    @Mock
    private ScoreCalculator scoreCalculator;
    
    private BlockValidator blockValidator;
    
    private Ed25519KeyPair keyPair;
    private byte[] previousBlockHash;
    
    @BeforeEach
    void setUp() throws Exception {
        blockValidator = new BlockValidator(signatureVerifier, powMiner, scoreCalculator);
        
        // 创建测试密钥对
        keyPair = Ed25519KeyPair.generate();
        
        // 创建前一区块哈希
        previousBlockHash = new byte[32];
        Arrays.fill(previousBlockHash, (byte) 1);
    }
    
    @Test
    void testVerifyBlockFormat_ValidBlock() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.verifyBlockFormat(block);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testVerifyBlockFormat_NullBlock() {
        // When
        boolean result = blockValidator.verifyBlockFormat(null);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testVerifyBlockTimestamp_ValidTimestamp() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.verifyBlockTimestamp(block, 3600000); // 1小时
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testVerifyBlockTimestamp_TooOld() {
        // Given
        Block block = createOldBlock();
        
        // When
        boolean result = blockValidator.verifyBlockTimestamp(block, 1000); // 1秒
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testVerifyBlockTimestamp_FutureTimestamp() {
        // Given
        Block block = createFutureBlock();
        
        // When
        boolean result = blockValidator.verifyBlockTimestamp(block, 3600000); // 1小时
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testVerifyBlockHeight_ValidHeight() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.verifyBlockHeight(block, 1L);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testVerifyBlockHeight_InvalidHeight() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.verifyBlockHeight(block, 2L);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testVerifyBlockRound_ValidRound() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.verifyBlockRound(block, 1);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testVerifyBlockRound_InvalidRound() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.verifyBlockRound(block, 2);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testVerifyBlockHash_ValidHash() {
        // Given
        Block block = createValidBlock();
        byte[] expectedHash = block.getHash();
        
        // When
        boolean result = blockValidator.verifyBlockHash(block, expectedHash);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testVerifyBlockHash_InvalidHash() {
        // Given
        Block block = createValidBlock();
        byte[] wrongHash = new byte[32];
        Arrays.fill(wrongHash, (byte) 2);
        
        // When
        boolean result = blockValidator.verifyBlockHash(block, wrongHash);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testVerifySignatureCount_ValidCount() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.verifySignatureCount(block, 0);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testVerifySignatureCount_InsufficientCount() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.verifySignatureCount(block, 10);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testQuickVerify_ValidBlock() {
        // Given
        Block block = createValidBlock();
        
        // When
        boolean result = blockValidator.quickVerify(block);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void testQuickVerify_NullBlock() {
        // When
        boolean result = blockValidator.quickVerify(null);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testQuickVerify_OldBlock() {
        // Given
        Block block = createVeryOldBlock(); // 使用更旧的区块
        
        // When
        boolean result = blockValidator.quickVerify(block);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void testGetValidationStats() {
        // Given
        Block block = createValidBlock();
        
        // When
        Map<String, Object> stats = blockValidator.getValidationStats(block);
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("height")).isEqualTo(1L);
        assertThat(stats.get("round")).isEqualTo(1);
        assertThat(stats.get("vrfAnnouncements")).isEqualTo(0);
        assertThat(stats.get("transactions")).isEqualTo(0);
    }
    
    @Test
    void testGetValidationStats_NullBlock() {
        // When
        Map<String, Object> stats = blockValidator.getValidationStats(null);
        
        // Then
        assertThat(stats).isNotNull();
        assertThat(stats).isEmpty();
    }
    
    // 辅助方法
    
    private Block createValidBlock() {
        return new Block.Builder()
                .height(1L)
                .round(1)
                .timestamp(System.currentTimeMillis())
                .previousHash(previousBlockHash)
                .proposer(keyPair.getPublicKey().getEncoded())
                .vrfOutput(new byte[32])
                .vrfProof(new VRFProof(new byte[64]))
                .allVRFAnnouncements(new ArrayList<>())
                .rewardedNodes(new ArrayList<>())
                .transactions(new ArrayList<>())
                .nonce(12345L)
                .difficultyTarget(BigInteger.valueOf(1000000))
                .signatures(new HashMap<>())
                .build();
    }
    
    private Block createOldBlock() {
        return new Block.Builder()
                .height(1L)
                .round(1)
                .timestamp(System.currentTimeMillis() - 2000) // 2秒前
                .previousHash(previousBlockHash)
                .proposer(keyPair.getPublicKey().getEncoded())
                .vrfOutput(new byte[32])
                .vrfProof(new VRFProof(new byte[64]))
                .allVRFAnnouncements(new ArrayList<>())
                .rewardedNodes(new ArrayList<>())
                .transactions(new ArrayList<>())
                .nonce(12345L)
                .difficultyTarget(BigInteger.valueOf(1000000))
                .signatures(new HashMap<>())
                .build();
    }
    
    private Block createFutureBlock() {
        return new Block.Builder()
                .height(1L)
                .round(1)
                .timestamp(System.currentTimeMillis() + 2000) // 2秒后
                .previousHash(previousBlockHash)
                .proposer(keyPair.getPublicKey().getEncoded())
                .vrfOutput(new byte[32])
                .vrfProof(new VRFProof(new byte[64]))
                .allVRFAnnouncements(new ArrayList<>())
                .rewardedNodes(new ArrayList<>())
                .transactions(new ArrayList<>())
                .nonce(12345L)
                .difficultyTarget(BigInteger.valueOf(1000000))
                .signatures(new HashMap<>())
                .build();
    }
    
    private Block createVeryOldBlock() {
        return new Block.Builder()
                .height(1L)
                .round(1)
                .timestamp(System.currentTimeMillis() - 7200000) // 2小时前
                .previousHash(previousBlockHash)
                .proposer(keyPair.getPublicKey().getEncoded())
                .vrfOutput(new byte[32])
                .vrfProof(new VRFProof(new byte[64]))
                .allVRFAnnouncements(new ArrayList<>())
                .rewardedNodes(new ArrayList<>())
                .transactions(new ArrayList<>())
                .nonce(12345L)
                .difficultyTarget(BigInteger.valueOf(1000000))
                .signatures(new HashMap<>())
                .build();
    }
}
