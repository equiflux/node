package io.equiflux.node.consensus;

import io.equiflux.node.config.ConsensusConfig;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Equiflux共识引擎测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class EquifluxConsensusTest {
    
    private EquifluxConsensus consensus;
    
    @Mock
    private ConsensusConfig config;
    
    @Mock
    private VRFCollector vrfCollector;
    
    @Mock
    private BlockProposer blockProposer;
    
    @Mock
    private BlockValidator blockValidator;
    
    @BeforeEach
    void setUp() {
        consensus = new EquifluxConsensus(config, vrfCollector, blockProposer, blockValidator);
    }
    
    @Test
    void testConsensusEngineInterface() {
        // 验证实现了ConsensusEngine接口
        assertTrue(consensus instanceof ConsensusEngine);
    }
    
    @Test
    void testGetCurrentHeight() {
        // 初始高度应该为0
        assertEquals(0, consensus.getCurrentHeight());
        
        // 设置高度
        consensus.setCurrentHeight(100);
        assertEquals(100, consensus.getCurrentHeight());
    }
    
    @Test
    void testGetCurrentRound() {
        // 初始轮次应该为0
        assertEquals(0, consensus.getCurrentRound());
        
        // 设置轮次
        consensus.setCurrentRound(50);
        assertEquals(50, consensus.getCurrentRound());
    }
    
    @Test
    void testGetCurrentEpoch() {
        // 初始纪元应该为1
        assertEquals(1, consensus.getCurrentEpoch());
        
        // 设置纪元
        consensus.setCurrentEpoch(2);
        assertEquals(2, consensus.getCurrentEpoch());
    }
    
    @Test
    void testIsConsensusHealthy() {
        // 验证配置
        doNothing().when(config).validate();
        
        // 健康检查应该通过
        assertTrue(consensus.isConsensusHealthy());
        
        // 验证配置被调用
        verify(config).validate();
    }
    
    @Test
    void testIsConsensusHealthyWithInvalidConfig() {
        // 配置验证失败
        doThrow(new IllegalArgumentException("Invalid config")).when(config).validate();
        
        // 健康检查应该失败
        assertFalse(consensus.isConsensusHealthy());
    }
    
    @Test
    void testIsConsensusHealthyWithInvalidState() {
        // 设置无效状态
        consensus.setCurrentHeight(-1);
        
        // 健康检查应该失败
        assertFalse(consensus.isConsensusHealthy());
    }
    
    @Test
    void testGetConsensusStatus() {
        // 设置配置值
        when(config.getSuperNodeCount()).thenReturn(50);
        when(config.getBlockTimeSeconds()).thenReturn(3);
        when(config.getVrfCollectionTimeoutMs()).thenReturn(3000);
        when(config.getBlockProductionTimeoutMs()).thenReturn(5000);
        when(config.getRewardedTopX()).thenReturn(15);
        when(config.getPowBaseDifficulty()).thenReturn(java.math.BigInteger.valueOf(2500000));
        
        // 获取共识状态
        var status = consensus.getConsensusStatus();
        
        // 验证状态包含必要信息
        assertNotNull(status);
        assertTrue(status.containsKey("currentHeight"));
        assertTrue(status.containsKey("currentRound"));
        assertTrue(status.containsKey("currentEpoch"));
        assertTrue(status.containsKey("superNodeCount"));
        assertTrue(status.containsKey("blockTimeSeconds"));
        assertTrue(status.containsKey("vrfCollectionTimeoutMs"));
        assertTrue(status.containsKey("blockProductionTimeoutMs"));
        assertTrue(status.containsKey("rewardedTopX"));
        assertTrue(status.containsKey("powBaseDifficulty"));
        
        // 验证值
        assertEquals(0L, status.get("currentHeight"));
        assertEquals(0L, status.get("currentRound"));
        assertEquals(1L, status.get("currentEpoch"));
    }
    
    @Test
    void testGetConsensusStats() {
        // 设置配置值
        when(config.getSuperNodeCount()).thenReturn(50);
        when(config.getBlockTimeSeconds()).thenReturn(3);
        when(config.getVrfCollectionTimeoutMs()).thenReturn(3000);
        when(config.getBlockProductionTimeoutMs()).thenReturn(5000);
        when(config.getRewardedTopX()).thenReturn(15);
        when(config.getPowBaseDifficulty()).thenReturn(java.math.BigInteger.valueOf(2500000));
        
        // 获取共识统计信息
        var stats = consensus.getConsensusStats();
        
        // 验证统计信息包含必要信息
        assertNotNull(stats);
        assertTrue(stats.containsKey("height"));
        assertTrue(stats.containsKey("round"));
        assertTrue(stats.containsKey("epoch"));
        assertTrue(stats.containsKey("isHealthy"));
        assertTrue(stats.containsKey("config"));
        
        // 验证值
        assertEquals(0L, stats.get("height"));
        assertEquals(0L, stats.get("round"));
        assertEquals(1L, stats.get("epoch"));
        assertTrue((Boolean) stats.get("isHealthy"));
    }
    
    @Test
    void testResetConsensus() {
        // 设置一些状态
        consensus.setCurrentHeight(100);
        consensus.setCurrentRound(50);
        consensus.setCurrentEpoch(2);
        
        // 重置共识
        consensus.resetConsensus();
        
        // 验证状态被重置
        assertEquals(0, consensus.getCurrentHeight());
        assertEquals(0, consensus.getCurrentRound());
        assertEquals(1, consensus.getCurrentEpoch());
    }
    
    @Test
    void testVerifyBlockWithNullBlock() {
        // 验证null区块应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            consensus.verifyBlock(null, createMockBlock(0));
        });
    }
    
    @Test
    void testVerifyBlockWithNullPreviousBlock() {
        // 验证null前一区块应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            consensus.verifyBlock(createMockBlock(1), null);
        });
    }
    
    @Test
    void testVerifyBlockSuccess() {
        // 创建模拟区块
        Block block = createMockBlock(1);
        Block previousBlock = createMockBlock(0);
        
        // 设置验证器返回true
        when(blockValidator.verifyBlock(any(Block.class), any(byte[].class), anyLong(), anyLong()))
                .thenReturn(true);
        
        // 验证区块应该成功
        assertTrue(consensus.verifyBlock(block, previousBlock));
        
        // 验证验证器被调用
        verify(blockValidator).verifyBlock(eq(block), any(byte[].class), eq(1L), eq(1L));
    }
    
    @Test
    void testVerifyBlockFailure() {
        // 创建模拟区块
        Block block = createMockBlock(1);
        Block previousBlock = createMockBlock(0);
        
        // 设置验证器返回false
        when(blockValidator.verifyBlock(any(Block.class), any(byte[].class), anyLong(), anyLong()))
                .thenReturn(false);
        
        // 验证区块应该失败
        assertFalse(consensus.verifyBlock(block, previousBlock));
    }
    
    @Test
    void testVerifyBlockWithException() {
        // 创建模拟区块
        Block block = createMockBlock(1);
        Block previousBlock = createMockBlock(0);
        
        // 设置验证器抛出异常
        when(blockValidator.verifyBlock(any(Block.class), any(byte[].class), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Validation failed"));
        
        // 验证区块应该失败
        assertFalse(consensus.verifyBlock(block, previousBlock));
    }
    
    /**
     * 创建模拟区块
     */
    private Block createMockBlock(long height) {
        return mock(Block.class);
    }
    
    /**
     * 创建模拟交易
     */
    private Transaction createMockTransaction() {
        return new Transaction(
                new byte[32], // senderPublicKey
                new byte[32], // receiverPublicKey
                1000L,        // amount
                10L,          // fee
                System.currentTimeMillis(), // timestamp
                1L,           // nonce
                new byte[64], // signature
                new byte[32], // hash
                TransactionType.TRANSFER
        );
    }
}
