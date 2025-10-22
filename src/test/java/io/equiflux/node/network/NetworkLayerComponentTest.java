package io.equiflux.node.network;

import io.equiflux.node.crypto.Ed25519KeyPair;
import io.equiflux.node.model.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网络层组件测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class NetworkLayerComponentTest {
    
    private NetworkConfig networkConfig;
    private Ed25519KeyPair keyPair;
    
    @BeforeEach
    void setUp() {
        // 初始化配置
        networkConfig = new NetworkConfig();
        networkConfig.setPort(8080);
        networkConfig.setMaxConnections(10);
        networkConfig.setEnableCompression(true);
        networkConfig.setEnableEncryption(true);
        networkConfig.setEnableSignatureVerification(true);
        networkConfig.setEnableMessageDeduplication(true);
        networkConfig.setEnableMonitoring(true);
        
        // 初始化密钥对
        keyPair = Ed25519KeyPair.generate();
    }
    
    @Test
    void testNetworkConfigValidation() {
        // 测试配置验证
        assertDoesNotThrow(() -> networkConfig.validate());
        
        // 测试无效配置
        NetworkConfig invalidConfig = new NetworkConfig();
        invalidConfig.setPort(-1);
        
        assertThrows(IllegalArgumentException.class, () -> invalidConfig.validate());
    }
    
    @Test
    void testNetworkMessageCreation() {
        // 测试网络消息创建
        Block testBlock = createTestBlock();
        
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.BLOCK_PROPOSAL,
            keyPair.getPublicKey(),
            System.currentTimeMillis(),
            12345L,
            testBlock,
            new byte[64]
        );
        
        assertNotNull(message);
        assertEquals(NetworkMessage.MessageType.BLOCK_PROPOSAL, message.getType());
        assertEquals(keyPair.getPublicKey(), message.getSender());
        assertEquals(testBlock, message.getBlockPayload());
        assertTrue(message.isValidFormat());
        assertFalse(message.isExpired(60000)); // 1分钟过期时间
    }
    
    @Test
    void testPeerInfoCreation() {
        // 测试节点信息创建
        PeerInfo peerInfo = new PeerInfo(
            "test-peer-1",
            keyPair.getPublicKey(),
            "localhost",
            8080,
            PeerInfo.PeerStatus.CONNECTED,
            System.currentTimeMillis(),
            0,
            0
        );
        
        assertNotNull(peerInfo);
        assertEquals("test-peer-1", peerInfo.getNodeId());
        assertEquals("localhost", peerInfo.getHost());
        assertEquals(8080, peerInfo.getPort());
        assertEquals(PeerInfo.PeerStatus.CONNECTED, peerInfo.getStatus());
        assertTrue(peerInfo.isConnected());
        assertFalse(peerInfo.isExpired(60000)); // 1分钟过期时间
        assertEquals("localhost:8080", peerInfo.getAddressString());
    }
    
    @Test
    void testSyncRequestCreation() {
        // 测试同步请求创建
        SyncRequest syncRequest = new SyncRequest(
            1L,
            100L,
            50,
            System.currentTimeMillis()
        );
        
        assertNotNull(syncRequest);
        assertEquals(1L, syncRequest.getStartHeight());
        assertEquals(100L, syncRequest.getEndHeight());
        assertEquals(50, syncRequest.getMaxBlocks());
        assertEquals(100L, syncRequest.getRequestedBlockCount());
        assertTrue(syncRequest.isValid());
    }
    
    @Test
    void testSyncResponseCreation() {
        // 测试同步响应创建
        List<Block> blocks = List.of(createTestBlock());
        
        SyncResponse syncResponse = SyncResponse.success(blocks, 1L, 1L);
        
        assertNotNull(syncResponse);
        assertEquals(SyncResponse.SyncStatus.SUCCESS, syncResponse.getStatus());
        assertEquals(1, syncResponse.getBlockCount());
        assertTrue(syncResponse.isSuccess());
        assertTrue(syncResponse.hasBlocks());
        assertFalse(syncResponse.hasError());
        
        // 测试失败响应
        SyncResponse failedResponse = SyncResponse.failed("Test error", 1L, 1L);
        assertTrue(failedResponse.isFailed());
        assertTrue(failedResponse.hasError());
        assertEquals("Test error", failedResponse.getErrorMessage());
    }
    
    @Test
    void testNetworkStatsCreation() {
        // 测试网络统计信息创建
        NetworkStats stats = new NetworkStats(
            100L, 200L, 1000L, 2000L, 5, 10, 60000L, System.currentTimeMillis()
        );
        
        assertNotNull(stats);
        assertEquals(100L, stats.getMessagesSent());
        assertEquals(200L, stats.getMessagesReceived());
        assertEquals(1000L, stats.getBytesSent());
        assertEquals(2000L, stats.getBytesReceived());
        assertEquals(5, stats.getConnectedPeers());
        assertEquals(10, stats.getTotalPeers());
        assertEquals(300L, stats.getTotalMessages());
        assertEquals(3000L, stats.getTotalBytes());
        assertEquals(0.5, stats.getConnectionRate());
        
        // 测试速率计算
        assertTrue(stats.getMessagesSentPerSecond() > 0);
        assertTrue(stats.getMessagesReceivedPerSecond() > 0);
        assertTrue(stats.getBytesSentPerSecond() > 0);
        assertTrue(stats.getBytesReceivedPerSecond() > 0);
    }
    
    @Test
    void testMessageCompressionService() {
        // 测试消息压缩服务
        MessageCompressionService compressionService = new MessageCompressionService();
        
        byte[] originalData = "This is a test message for compression".getBytes();
        
        byte[] compressedData = compressionService.compress(originalData);
        assertNotNull(compressedData);
        
        byte[] decompressedData = compressionService.decompress(compressedData);
        assertNotNull(decompressedData);
        
        assertArrayEquals(originalData, decompressedData);
        
        // 测试压缩检测
        assertTrue(compressionService.isCompressed(compressedData));
        assertFalse(compressionService.isCompressed(originalData));
        
        // 测试统计信息
        MessageCompressionService.CompressionStats stats = compressionService.getStats();
        assertNotNull(stats);
        assertTrue(stats.getCompressionCount() > 0);
        assertTrue(stats.getDecompressionCount() > 0);
    }
    
    @Test
    void testMessageEncryptionService() {
        // 测试消息加密服务
        MessageEncryptionService encryptionService = new MessageEncryptionService();
        
        byte[] originalData = "This is a test message for encryption".getBytes();
        
        // 创建测试公钥
        Ed25519KeyPair testKeyPair = Ed25519KeyPair.generate();
        
        byte[] encryptedData = encryptionService.encrypt(originalData, testKeyPair.getPublicKey());
        assertNotNull(encryptedData);
        assertNotEquals(originalData.length, encryptedData.length);
        
        byte[] decryptedData = encryptionService.decrypt(encryptedData, testKeyPair.getPublicKey());
        assertNotNull(decryptedData);
        
        assertArrayEquals(originalData, decryptedData);
        
        // 测试加密检测
        assertTrue(encryptionService.isEncrypted(encryptedData));
        assertFalse(encryptionService.isEncrypted(originalData));
        
        // 测试统计信息
        MessageEncryptionService.EncryptionStats stats = encryptionService.getStats();
        assertNotNull(stats);
        assertTrue(stats.getEncryptionCount() > 0);
        assertTrue(stats.getDecryptionCount() > 0);
    }
    
    @Test
    void testGossipMessageCreation() {
        // 测试Gossip消息创建
        Block testBlock = createTestBlock();
        
        GossipProtocol.GossipMessage gossipMessage = new GossipProtocol.GossipMessage(
            GossipProtocol.GossipMessage.MessageType.BLOCK_PROPOSAL,
            testBlock,
            System.currentTimeMillis(),
            "test-message-id"
        );
        
        assertNotNull(gossipMessage);
        assertEquals(GossipProtocol.GossipMessage.MessageType.BLOCK_PROPOSAL, gossipMessage.getType());
        assertEquals(testBlock, gossipMessage.getPayload());
        assertEquals("test-message-id", gossipMessage.getMessageId());
    }
    
    @Test
    void testMessagePropagationStats() {
        // 测试消息传播统计信息
        MessagePropagationService.PropagationStats stats = new MessagePropagationService.PropagationStats(
            100L, 10L, 5, 2
        );
        
        assertNotNull(stats);
        assertEquals(100L, stats.getMessagesPropagated());
        assertEquals(10L, stats.getMessagesFailed());
        assertEquals(5, stats.getQueueSize());
        assertEquals(2, stats.getPendingMessages());
        
        // 测试成功率计算
        double successRate = stats.getSuccessRate();
        assertTrue(successRate >= 0.0 && successRate <= 1.0);
        assertEquals(0.909, successRate, 0.001); // 100/(100+10) = 0.909
    }
    
    @Test
    void testNetworkMonitoringMetrics() {
        // 测试网络监控指标
        NetworkMonitoringService.NetworkMetrics metrics = new NetworkMonitoringService.NetworkMetrics(
            System.currentTimeMillis(),
            100L, 200L, 1000L, 2000L, 5, 10, 0L, 0L, 60000L
        );
        
        assertNotNull(metrics);
        assertEquals(100L, metrics.getMessagesSent());
        assertEquals(200L, metrics.getMessagesReceived());
        assertEquals(1000L, metrics.getBytesSent());
        assertEquals(2000L, metrics.getBytesReceived());
        assertEquals(5, metrics.getConnectedPeers());
        assertEquals(10, metrics.getTotalPeers());
        assertEquals(300L, metrics.getTotalMessages());
        assertEquals(3000L, metrics.getTotalBytes());
    }
    
    @Test
    void testNetworkHealthStatus() {
        // 测试网络健康状态
        NetworkMonitoringService.NetworkMetrics metrics = new NetworkMonitoringService.NetworkMetrics(
            System.currentTimeMillis(),
            100L, 200L, 1000L, 2000L, 5, 10, 0L, 0L, 60000L
        );
        
        NetworkMonitoringService.NetworkHealthStatus health = new NetworkMonitoringService.NetworkHealthStatus(
            NetworkMonitoringService.NetworkHealthStatus.HealthLevel.HEALTHY,
            0.95,
            metrics
        );
        
        assertNotNull(health);
        assertEquals(NetworkMonitoringService.NetworkHealthStatus.HealthLevel.HEALTHY, health.getLevel());
        assertEquals(0.95, health.getScore());
        assertNotNull(health.getMetrics());
    }
    
    @Test
    void testPeerDiscoveryStats() {
        // 测试节点发现统计信息
        PeerDiscoveryService.DiscoveryStats stats = new PeerDiscoveryService.DiscoveryStats(
            50, 10, 100L, 20L
        );
        
        assertNotNull(stats);
        assertEquals(50, stats.getKnownPeers());
        assertEquals(10, stats.getConnectedPeers());
        assertEquals(100L, stats.getPeersDiscovered());
        assertEquals(20L, stats.getPeersConnected());
        
        // 测试连接率计算
        double connectionRate = stats.getConnectionRate();
        assertEquals(0.2, connectionRate); // 10/50 = 0.2
    }
    
    @Test
    void testBlockSyncStats() {
        // 测试区块同步统计信息
        BlockSyncService.SyncStats stats = new BlockSyncService.SyncStats(
            1000L, 10L, 2, 1, 500L, System.currentTimeMillis()
        );
        
        assertNotNull(stats);
        assertEquals(1000L, stats.getTotalBlocksSynced());
        assertEquals(10L, stats.getSyncFailures());
        assertEquals(2, stats.getActiveTasks());
        assertEquals(1, stats.getPendingTasks());
        assertEquals(500L, stats.getLastSyncHeight());
        
        // 测试成功率计算
        double successRate = stats.getSuccessRate();
        assertTrue(successRate >= 0.0 && successRate <= 1.0);
        assertEquals(0.99, successRate, 0.01); // 1000/(1000+10) = 0.99
    }
    
    // 辅助方法
    
    private Block createTestBlock() {
        return new Block.Builder()
                .height(1L)
                .round(1)
                .timestamp(System.currentTimeMillis())
                .previousHash(new byte[32])
                .proposer(new byte[32])
                .vrfOutput(new byte[32])
                .vrfProof(null)
                .allVRFAnnouncements(List.of())
                .rewardedNodes(List.of())
                .transactions(List.of())
                .nonce(0L)
                .difficultyTarget(java.math.BigInteger.valueOf(1000000))
                .signatures(java.util.Map.of())
                .build();
    }
}
