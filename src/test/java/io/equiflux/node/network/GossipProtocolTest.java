package io.equiflux.node.network;

import io.equiflux.node.crypto.Ed25519KeyPair;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

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
    private Ed25519KeyPair keyPair;
    private PublicKey publicKey;
    
    @BeforeEach
    void setUp() {
        gossipProtocol = new GossipProtocol();
        ReflectionTestUtils.setField(gossipProtocol, "networkService", networkService);
        ReflectionTestUtils.setField(gossipProtocol, "networkConfig", networkConfig);
        
        // 手动设置running状态为true，模拟初始化完成
        ReflectionTestUtils.setField(gossipProtocol, "running", new AtomicBoolean(true));
        
        // 初始化执行器
        ExecutorService messageExecutor = Executors.newFixedThreadPool(4);
        ReflectionTestUtils.setField(gossipProtocol, "messageExecutor", messageExecutor);
        
        keyPair = Ed25519KeyPair.generate();
        publicKey = keyPair.getPublicKey();
    }
    
    @Test
    void testGossipBlockProposal() throws InterruptedException {
        // Given
        Block block = createTestBlock();
        
        // When
        gossipProtocol.gossipBlockProposal(block);
        
        // 等待异步处理完成
        Thread.sleep(100);
        
        // Then
        // 验证消息被处理（通过检查内部状态）
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertThat(stats.getCachedMessages()).isGreaterThan(0);
    }
    
    @Test
    void testGossipBlockVote() throws InterruptedException {
        // Given
        Block block = createTestBlock();
        
        // When
        gossipProtocol.gossipBlockVote(block);
        
        // 等待异步处理完成
        Thread.sleep(100);
        
        // Then
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertThat(stats.getCachedMessages()).isGreaterThan(0);
    }
    
    @Test
    void testGossipTransaction() throws InterruptedException {
        // Given
        Transaction transaction = createTestTransaction();
        
        // When
        gossipProtocol.gossipTransaction(transaction);
        
        // 等待异步处理完成
        Thread.sleep(100);
        
        // Then
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertThat(stats.getCachedMessages()).isGreaterThan(0);
    }
    
    @Test
    void testGossipVRFAnnouncement() throws InterruptedException {
        // Given
        VRFAnnouncement announcement = createTestVRFAnnouncement();
        
        // When
        gossipProtocol.gossipVRFAnnouncement(announcement);
        
        // 等待异步处理完成
        Thread.sleep(100);
        
        // Then
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertThat(stats.getCachedMessages()).isGreaterThan(0);
    }
    
    @Test
    void testHandleGossipMessage() throws InterruptedException {
        // Given
        Block block = createTestBlock();
        GossipProtocol.GossipMessage message = new GossipProtocol.GossipMessage(
            GossipProtocol.GossipMessage.MessageType.BLOCK_PROPOSAL,
            block,
            System.currentTimeMillis(),
            "test-message-id"
        );
        
        String senderId = "test-sender";
        
        // When
        gossipProtocol.handleGossipMessage(message, senderId);
        
        // 等待异步处理完成
        Thread.sleep(100);
        
        // Then
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertThat(stats.getCachedMessages()).isGreaterThan(0);
    }
    
    @Test
    void testHandleDuplicateMessage() throws InterruptedException {
        // Given
        Block block = createTestBlock();
        GossipProtocol.GossipMessage message = new GossipProtocol.GossipMessage(
            GossipProtocol.GossipMessage.MessageType.BLOCK_PROPOSAL,
            block,
            System.currentTimeMillis(),
            "duplicate-message-id"
        );
        
        String senderId = "test-sender";
        
        // When
        gossipProtocol.handleGossipMessage(message, senderId);
        gossipProtocol.handleGossipMessage(message, senderId); // 重复消息
        
        // 等待异步处理完成
        Thread.sleep(100);
        
        // Then
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertThat(stats.getCachedMessages()).isEqualTo(1); // 应该只有一条消息
    }
    
    @Test
    void testHandleExpiredMessage() throws InterruptedException {
        // Given
        Block block = createTestBlock();
        long expiredTimestamp = System.currentTimeMillis() - 400000; // 过期时间
        GossipProtocol.GossipMessage message = new GossipProtocol.GossipMessage(
            GossipProtocol.GossipMessage.MessageType.BLOCK_PROPOSAL,
            block,
            expiredTimestamp,
            "expired-message-id"
        );
        
        String senderId = "test-sender";
        
        // When
        gossipProtocol.handleGossipMessage(message, senderId);
        
        // 等待异步处理完成
        Thread.sleep(100);
        
        // Then
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertThat(stats.getCachedMessages()).isEqualTo(0); // 过期消息不应该被缓存
    }
    
    @Test
    void testGossipStats() throws InterruptedException {
        // Given
        Block block1 = createTestBlock();
        Block block2 = createTestBlock();
        Transaction transaction = createTestTransaction();
        
        // When
        gossipProtocol.gossipBlockProposal(block1);
        gossipProtocol.gossipBlockVote(block2);
        gossipProtocol.gossipTransaction(transaction);
        
        // 等待异步处理完成
        Thread.sleep(100);
        
        // Then
        GossipProtocol.GossipStats stats = gossipProtocol.getStats();
        assertThat(stats.getCachedMessages()).isEqualTo(3);
        assertThat(stats.getActiveMessages()).isEqualTo(3);
        assertThat(stats.getTotalRounds()).isEqualTo(3);
    }
    
    @Test
    void testGossipMessageCreation() {
        // Given
        Block block = createTestBlock();
        GossipProtocol.GossipMessage.MessageType type = GossipProtocol.GossipMessage.MessageType.BLOCK_PROPOSAL;
        long timestamp = System.currentTimeMillis();
        String messageId = "test-message-id";
        
        // When
        GossipProtocol.GossipMessage message = new GossipProtocol.GossipMessage(
            type, block, timestamp, messageId
        );
        
        // Then
        assertThat(message.getType()).isEqualTo(type);
        assertThat(message.getPayload()).isEqualTo(block);
        assertThat(message.getTimestamp()).isEqualTo(timestamp);
        assertThat(message.getMessageId()).isEqualTo(messageId);
    }
    
    @Test
    void testGossipStatsCreation() {
        // Given
        int cachedMessages = 10;
        int activeMessages = 5;
        int totalRounds = 15;
        
        // When
        GossipProtocol.GossipStats stats = new GossipProtocol.GossipStats(
            cachedMessages, activeMessages, totalRounds
        );
        
        // Then
        assertThat(stats.getCachedMessages()).isEqualTo(cachedMessages);
        assertThat(stats.getActiveMessages()).isEqualTo(activeMessages);
        assertThat(stats.getTotalRounds()).isEqualTo(totalRounds);
    }
    
    // 辅助方法
    
    private Block createTestBlock() {
        return new Block.Builder()
                .height(1)
                .round(1)
                .timestamp(System.currentTimeMillis())
                .previousHash(new byte[32])
                .proposer(publicKey.getEncoded())
                .vrfOutput(new byte[32])
                .vrfProof(new VRFProof(new byte[64]))
                .allVRFAnnouncements(new ArrayList<>())
                .rewardedNodes(new ArrayList<>())
                .transactions(new ArrayList<>())
                .nonce(12345L)
                .difficultyTarget(BigInteger.valueOf(1000))
                .signatures(new HashMap<>())
                .build();
    }
    
    private Transaction createTestTransaction() {
        return new Transaction(
            publicKey.getEncoded(), publicKey.getEncoded(), 1000L, 10L, 
            System.currentTimeMillis(), 1L, new byte[64], new byte[32],
            io.equiflux.node.model.TransactionType.TRANSFER
        );
    }
    
    private VRFAnnouncement createTestVRFAnnouncement() {
        return new VRFAnnouncement(
            1L, publicKey, new VRFOutput(new byte[32], new VRFProof(new byte[64])), 
            new VRFProof(new byte[64]), 0.5, System.currentTimeMillis()
        );
    }
}
