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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 消息传播服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class MessagePropagationServiceTest {
    
    @Mock
    private NetworkService networkService;
    
    @Mock
    private GossipProtocol gossipProtocol;
    
    @Mock
    private NetworkConfig networkConfig;
    
    private MessagePropagationService propagationService;
    private Ed25519KeyPair keyPair;
    private PublicKey publicKey;
    
    @BeforeEach
    void setUp() {
        propagationService = new MessagePropagationService();
        ReflectionTestUtils.setField(propagationService, "networkService", networkService);
        ReflectionTestUtils.setField(propagationService, "gossipProtocol", gossipProtocol);
        ReflectionTestUtils.setField(propagationService, "networkConfig", networkConfig);

        // 手动设置running状态为true，模拟初始化完成
        ReflectionTestUtils.setField(propagationService, "running", new AtomicBoolean(true));

        // 初始化执行器（messageQueue是final的，不能设置）
        ExecutorService propagationExecutor = Executors.newFixedThreadPool(2);
        ReflectionTestUtils.setField(propagationService, "propagationExecutor", propagationExecutor);

        ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1);
        ReflectionTestUtils.setField(propagationService, "retryExecutor", retryExecutor);

        // 启动传播任务
        try {
            ReflectionTestUtils.invokeMethod(propagationService, "startPropagationTasks");
        } catch (Exception e) {
            // 忽略启动任务的异常
        }

        keyPair = Ed25519KeyPair.generate();
        publicKey = keyPair.getPublicKey();
    }
    
    @Test
    void testPropagateBlockProposal() throws InterruptedException {
        // Given
        Block block = createTestBlock();

        // When
        propagationService.propagateBlockProposal(block);

        // 等待异步处理完成
        Thread.sleep(200);

        // Then
        verify(gossipProtocol).gossipBlockProposal(block);
        verify(networkService).broadcastBlockProposal(block);
    }
    
    @Test
    void testPropagateBlockVote() throws InterruptedException {
        // Given
        Block block = createTestBlock();

        // When
        propagationService.propagateBlockVote(block);

        // 等待异步处理完成
        Thread.sleep(200);

        // Then
        verify(gossipProtocol).gossipBlockVote(block);
        verify(networkService).broadcastBlockVote(block);
    }
    
    @Test
    void testPropagateTransaction() throws InterruptedException {
        // Given
        Transaction transaction = createTestTransaction();

        // When
        propagationService.propagateTransaction(transaction);

        // 等待异步处理完成
        Thread.sleep(200);

        // Then
        verify(gossipProtocol).gossipTransaction(transaction);
        verify(networkService).broadcastTransaction(transaction);
    }
    
    @Test
    void testPropagateVRFAnnouncement() throws InterruptedException {
        // Given
        VRFAnnouncement announcement = createTestVRFAnnouncement();

        // When
        propagationService.propagateVRFAnnouncement(announcement);

        // 等待异步处理完成
        Thread.sleep(200);

        // Then
        verify(gossipProtocol).gossipVRFAnnouncement(announcement);
        verify(networkService).broadcastVRFAnnouncement(announcement);
    }
    
    @Test
    void testPropagateBatch() throws InterruptedException {
        // Given
        Block block = createTestBlock();
        Transaction transaction = createTestTransaction();
        
        MessagePropagationService.PropagationMessage message1 = 
            new MessagePropagationService.PropagationMessage(
                MessagePropagationService.PropagationMessage.MessageType.BLOCK_PROPOSAL,
                block,
                MessagePropagationService.MessagePriority.HIGH,
                System.currentTimeMillis()
            );
        
        MessagePropagationService.PropagationMessage message2 = 
            new MessagePropagationService.PropagationMessage(
                MessagePropagationService.PropagationMessage.MessageType.TRANSACTION,
                transaction,
                MessagePropagationService.MessagePriority.LOW,
                System.currentTimeMillis()
            );
        
        List<MessagePropagationService.PropagationMessage> messages = Arrays.asList(message1, message2);
        
        // When
        propagationService.propagateBatch(messages);
        
        // 等待异步处理完成
        Thread.sleep(200);
        
        // Then
        verify(gossipProtocol).gossipBlockProposal(block);
        verify(networkService).broadcastBlockProposal(block);
        verify(gossipProtocol).gossipTransaction(transaction);
        verify(networkService).broadcastTransaction(transaction);
    }
    
    @Test
    void testPropagationStats() throws InterruptedException {
        // Given
        Block block = createTestBlock();
        Transaction transaction = createTestTransaction();
        
        // When
        propagationService.propagateBlockProposal(block);
        propagationService.propagateTransaction(transaction);
        
        // 等待异步处理完成
        Thread.sleep(200);
        
        // Then
        MessagePropagationService.PropagationStats stats = propagationService.getStats();
        assertThat(stats.getMessagesPropagated()).isGreaterThan(0);
        assertThat(stats.getSuccessRate()).isGreaterThan(0.0);
    }
    
    @Test
    void testPropagationMessageCreation() {
        // Given
        Block block = createTestBlock();
        MessagePropagationService.PropagationMessage.MessageType type = 
            MessagePropagationService.PropagationMessage.MessageType.BLOCK_PROPOSAL;
        MessagePropagationService.MessagePriority priority = 
            MessagePropagationService.MessagePriority.HIGH;
        long timestamp = System.currentTimeMillis();
        
        // When
        MessagePropagationService.PropagationMessage message = 
            new MessagePropagationService.PropagationMessage(type, block, priority, timestamp);
        
        // Then
        assertThat(message.getType()).isEqualTo(type);
        assertThat(message.getPayload()).isEqualTo(block);
        assertThat(message.getPriority()).isEqualTo(priority);
        assertThat(message.getTimestamp()).isEqualTo(timestamp);
        assertThat(message.getMessageId()).isNotNull();
    }
    
    @Test
    void testMessagePriorityComparison() {
        // Given
        MessagePropagationService.MessagePriority high = 
            MessagePropagationService.MessagePriority.HIGH;
        MessagePropagationService.MessagePriority medium = 
            MessagePropagationService.MessagePriority.MEDIUM;
        MessagePropagationService.MessagePriority low = 
            MessagePropagationService.MessagePriority.LOW;
        
        // When & Then
        assertThat(high.isHigherThan(medium)).isTrue();
        assertThat(high.isHigherThan(low)).isTrue();
        assertThat(medium.isHigherThan(low)).isTrue();
        assertThat(medium.isHigherThan(high)).isFalse();
        assertThat(low.isHigherThan(high)).isFalse();
        assertThat(low.isHigherThan(medium)).isFalse();
    }
    
    @Test
    void testPropagationStatsCreation() {
        // Given
        long messagesPropagated = 100;
        long messagesFailed = 10;
        int queueSize = 5;
        int pendingMessages = 3;
        
        // When
        MessagePropagationService.PropagationStats stats = 
            new MessagePropagationService.PropagationStats(
                messagesPropagated, messagesFailed, queueSize, pendingMessages
            );
        
        // Then
        assertThat(stats.getMessagesPropagated()).isEqualTo(messagesPropagated);
        assertThat(stats.getMessagesFailed()).isEqualTo(messagesFailed);
        assertThat(stats.getQueueSize()).isEqualTo(queueSize);
        assertThat(stats.getPendingMessages()).isEqualTo(pendingMessages);
        assertThat(stats.getSuccessRate()).isEqualTo(100.0 / 110.0);
    }
    
    @Test
    void testPropagationStatsWithZeroMessages() {
        // Given
        MessagePropagationService.PropagationStats stats = 
            new MessagePropagationService.PropagationStats(0, 0, 0, 0);
        
        // When & Then
        assertThat(stats.getSuccessRate()).isEqualTo(0.0);
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
            System.currentTimeMillis(), 1L, new byte[64]
        );
    }
    
    private VRFAnnouncement createTestVRFAnnouncement() {
        return new VRFAnnouncement(
            1L, publicKey, new VRFOutput(new byte[32], new VRFProof(new byte[64])), 
            new VRFProof(new byte[64]), 0.5, System.currentTimeMillis()
        );
    }
}
