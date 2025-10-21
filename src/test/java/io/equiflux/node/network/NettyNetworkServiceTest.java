package io.equiflux.node.network;

import io.equiflux.node.crypto.Ed25519KeyPair;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * 网络服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class NettyNetworkServiceTest {
    
    @Mock
    private NetworkConfig networkConfig;
    
    @Mock
    private Ed25519KeyPair localKeyPair;
    
    @Mock
    private MessageCompressionService compressionService;
    
    @Mock
    private MessageEncryptionService encryptionService;
    
    private NettyNetworkService networkService;
    
    @BeforeEach
    void setUp() {
        networkService = new NettyNetworkService();
        ReflectionTestUtils.setField(networkService, "networkConfig", networkConfig);
        ReflectionTestUtils.setField(networkService, "localKeyPair", localKeyPair);
        ReflectionTestUtils.setField(networkService, "compressionService", compressionService);
        ReflectionTestUtils.setField(networkService, "encryptionService", encryptionService);
        
        // 使用lenient模式避免不必要的stubbing警告
        lenient().when(localKeyPair.getPublicKey()).thenReturn(mock(PublicKey.class));
        lenient().when(compressionService.getStats()).thenReturn(
            new MessageCompressionService.CompressionStats(0, 0, 0, 0));
        lenient().when(encryptionService.getStats()).thenReturn(
            new MessageEncryptionService.EncryptionStats(0, 0, 0, 0, 0));
    }
    
    @Test
    void testStartAndStop() {
        // 测试启动和停止
        CompletableFuture<Void> startFuture = networkService.start(8080);
        assertNotNull(startFuture);
        
        CompletableFuture<Void> stopFuture = networkService.stop();
        assertNotNull(stopFuture);
    }
    
    @Test
    void testIsRunning() {
        // 初始状态应该未运行
        assertFalse(networkService.isRunning());
    }
    
    @Test
    void testBroadcastBlockProposal() {
        // 创建测试区块
        Block block = createTestBlock(1);
        
        // 测试广播区块提议
        CompletableFuture<Void> future = networkService.broadcastBlockProposal(block);
        assertNotNull(future);
    }
    
    @Test
    void testBroadcastTransaction() {
        // 创建测试交易
        Transaction transaction = createTestTransaction();
        
        // 测试广播交易
        CompletableFuture<Void> future = networkService.broadcastTransaction(transaction);
        assertNotNull(future);
    }
    
    @Test
    void testBroadcastVRFAnnouncement() {
        // 创建测试VRF公告
        VRFAnnouncement announcement = createTestVRFAnnouncement();
        
        // 测试广播VRF公告
        CompletableFuture<Void> future = networkService.broadcastVRFAnnouncement(announcement);
        assertNotNull(future);
    }
    
    @Test
    void testConnectToPeer() {
        // 测试连接到节点
        CompletableFuture<Void> future = networkService.connectToPeer("127.0.0.1", 8081);
        assertNotNull(future);
    }
    
    @Test
    void testDisconnectFromPeer() {
        // 测试断开节点连接
        CompletableFuture<Void> future = networkService.disconnectFromPeer("127.0.0.1:8081");
        assertNotNull(future);
    }
    
    @Test
    void testGetConnectedPeers() {
        // 测试获取已连接节点列表
        List<PeerInfo> peers = networkService.getConnectedPeers();
        assertNotNull(peers);
        assertTrue(peers.isEmpty());
    }
    
    @Test
    void testGetPeerCount() {
        // 测试获取节点数量
        int count = networkService.getPeerCount();
        assertEquals(0, count);
    }
    
    @Test
    void testIsConnectedToPeer() {
        // 测试是否连接到指定节点
        boolean connected = networkService.isConnectedToPeer("127.0.0.1:8081");
        assertFalse(connected);
    }
    
    @Test
    void testGetLocalPeerInfo() {
        // 测试获取本地节点信息
        PeerInfo localPeer = networkService.getLocalPeerInfo();
        assertNotNull(localPeer);
        assertEquals("localhost", localPeer.getHost());
        assertEquals(8080, localPeer.getPort());
        assertEquals(PeerInfo.PeerStatus.CONNECTED, localPeer.getStatus());
    }
    
    @Test
    void testGetNetworkStats() {
        // 测试获取网络统计信息
        NetworkStats stats = networkService.getNetworkStats();
        assertNotNull(stats);
        assertEquals(0, stats.getMessagesSent());
        assertEquals(0, stats.getMessagesReceived());
        assertEquals(0, stats.getBytesSent());
        assertEquals(0, stats.getBytesReceived());
        assertEquals(0, stats.getConnectedPeers());
        assertEquals(0, stats.getTotalPeers());
    }
    
    @Test
    void testGetCompressionStats() {
        // 测试获取压缩统计信息
        MessageCompressionService.CompressionStats stats = networkService.getCompressionStats();
        assertNotNull(stats);
    }
    
    @Test
    void testGetEncryptionStats() {
        // 测试获取加密统计信息
        MessageEncryptionService.EncryptionStats stats = networkService.getEncryptionStats();
        assertNotNull(stats);
    }
    
    @Test
    void testDiscoverPeers() {
        // 测试发现节点
        CompletableFuture<List<PeerInfo>> future = networkService.discoverPeers();
        assertNotNull(future);
    }
    
    @Test
    void testSyncBlocks() {
        // 测试同步区块
        CompletableFuture<List<Block>> future = networkService.syncBlocks(1, 10);
        assertNotNull(future);
    }
    
    @Test
    void testSendHeartbeat() {
        // 测试发送心跳
        CompletableFuture<Void> future = networkService.sendHeartbeat();
        assertNotNull(future);
    }
    
    @Test
    void testIsHealthy() {
        // 测试健康检查
        boolean healthy = networkService.isHealthy();
        assertFalse(healthy); // 没有连接节点时应该不健康
    }
    
    @Test
    void testMessageListener() {
        // 测试消息监听器
        MessageListener listener = mock(MessageListener.class);
        
        networkService.addMessageListener(listener);
        networkService.removeMessageListener(listener);
        
        // 验证没有异常抛出
        assertTrue(true);
    }
    
    @Test
    void testPeerListener() {
        // 测试节点监听器
        PeerListener listener = mock(PeerListener.class);
        
        networkService.addPeerListener(listener);
        networkService.removePeerListener(listener);
        
        // 验证没有异常抛出
        assertTrue(true);
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
