package io.equiflux.node.network;

import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.TransactionType;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.model.VRFOutput;
import io.equiflux.node.model.VRFProof;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Gossip协议测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class GossipProtocolTest {
    
    @Mock
    private NetworkService networkService;
    
    @Mock
    private NetworkConfig networkConfig;
    
    private GossipProtocol gossipProtocol;
    
    @BeforeEach
    void setUp() {
        gossipProtocol = new GossipProtocol();
        ReflectionTestUtils.setField(gossipProtocol, "networkService", networkService);
        ReflectionTestUtils.setField(gossipProtocol, "networkConfig", networkConfig);
        
        // 设置默认配置
        lenient().when(networkConfig.isEnableMessageDeduplication()).thenReturn(true);
        lenient().when(networkConfig.getMessageExpirationMs()).thenReturn(300000L);
    }
    
    @Test
    void testGossipBlockProposal() {
        // 创建测试区块
        Block block = createTestBlock(1);
        
        // 测试传播区块提议
        gossipProtocol.gossipBlockProposal(block);
        
        // 验证没有异常抛出
        assertTrue(true);
    }
    
    @Test
    void testGossipBlockVote() {
        // 创建测试区块
        Block block = createTestBlock(1);
        
        // 测试传播区块投票
        gossipProtocol.gossipBlockVote(block);
        
        // 验证没有异常抛出
        assertTrue(true);
    }
    
    @Test
    void testGossipTransaction() {
        // 创建测试交易
        Transaction transaction = createTestTransaction();
        
        // 测试传播交易
        gossipProtocol.gossipTransaction(transaction);
        
        // 验证没有异常抛出
        assertTrue(true);
    }
    
    @Test
    void testGossipVRFAnnouncement() {
        // 创建测试VRF公告
        VRFAnnouncement announcement = createTestVRFAnnouncement();
        
        // 测试传播VRF公告
        gossipProtocol.gossipVRFAnnouncement(announcement);
        
        // 验证没有异常抛出
        assertTrue(true);
    }
    
    @Test
    void testHandleGossipMessage() {
        // 创建测试Gossip消息
        GossipProtocol.GossipMessage message = new GossipProtocol.GossipMessage(
            GossipProtocol.GossipMessage.MessageType.BLOCK_PROPOSAL,
            createTestBlock(1),
            System.currentTimeMillis(),
            "test-message-id"
        );
        
        // 测试处理Gossip消息
        gossipProtocol.handleGossipMessage(message, "test-sender");
        
        // 验证没有异常抛出
        assertTrue(true);
    }
    
    @Test
    void testGetStats() {
        // 测试获取统计信息
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertNotNull(stats);
        assertEquals(0, stats.getCachedMessages());
        assertEquals(0, stats.getActiveMessages());
        assertEquals(0, stats.getTotalRounds());
    }
    
    @Test
    void testGossipMessage() {
        // 测试Gossip消息类
        GossipProtocol.GossipMessage message = new GossipProtocol.GossipMessage(
            GossipProtocol.GossipMessage.MessageType.TRANSACTION,
            createTestTransaction(),
            System.currentTimeMillis(),
            "test-id"
        );
        
        assertEquals(GossipProtocol.GossipMessage.MessageType.TRANSACTION, message.getType());
        assertNotNull(message.getPayload());
        assertTrue(message.getTimestamp() > 0);
        assertEquals("test-id", message.getMessageId());
    }
    
    @Test
    void testGossipStats() {
        // 测试Gossip统计信息类
        GossipProtocol.GossipStats stats = new GossipProtocol.GossipStats(10, 5, 3);
        
        assertEquals(10, stats.getCachedMessages());
        assertEquals(5, stats.getActiveMessages());
        assertEquals(3, stats.getTotalRounds());
        
        String statsString = stats.toString();
        assertTrue(statsString.contains("cachedMessages=10"));
        assertTrue(statsString.contains("activeMessages=5"));
        assertTrue(statsString.contains("totalRounds=3"));
    }
    
    // 辅助方法
    
    private Block createTestBlock(long height) {
        // 创建模拟的VRF证明
        VRFProof vrfProof = new VRFProof(new byte[64]);
        
        return new Block.Builder()
                .height(height)
                .round(1)
                .timestamp(System.currentTimeMillis())
                .previousHash(new byte[32])
                .proposer(new byte[32])
                .vrfOutput(new byte[32])
                .vrfProof(vrfProof)
                .allVRFAnnouncements(new ArrayList<>())
                .rewardedNodes(new ArrayList<>())
                .transactions(new ArrayList<>())
                .nonce(0)
                .difficultyTarget(BigInteger.valueOf(1000000))
                .signatures(new HashMap<>())
                .build();
    }
    
    private Transaction createTestTransaction() {
        return new Transaction.Builder()
                .type(TransactionType.TRANSFER)
                .fromPublicKey("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")
                .toPublicKey("fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210")
                .amount(1000)
                .fee(10)
                .timestamp(System.currentTimeMillis())
                .nonce(1)
                .signature(new byte[64])
                .build();
    }
    
    private VRFAnnouncement createTestVRFAnnouncement() {
        // 创建模拟的VRF输出和证明
        VRFProof vrfProof = new VRFProof(new byte[64]);
        VRFOutput vrfOutput = new VRFOutput(new byte[32], vrfProof);
        
        // 创建模拟的公钥
        PublicKey publicKey = mock(PublicKey.class);
        lenient().when(publicKey.getEncoded()).thenReturn(new byte[32]);
        
        return new VRFAnnouncement(
            1, // round
            publicKey,
            vrfOutput,
            vrfProof,
            0.5 // score
        );
    }
}