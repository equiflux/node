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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 网络层测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
public class NetworkLayerTest {
    
    @Mock
    private NetworkService networkService;
    
    @Mock
    private Ed25519KeyPair keyPair;
    
    @Mock
    private NetworkConfig networkConfig;
    
    private PeerInfo testPeer;
    private NetworkMessage testMessage;
    
    @BeforeEach
    void setUp() {
        // 创建测试节点信息
        testPeer = new PeerInfo(
            "test-peer-1",
            null, // 公钥将在测试中设置
            "localhost",
            8080,
            PeerInfo.PeerStatus.CONNECTED,
            System.currentTimeMillis(),
            0,
            0
        );
        
        // 创建测试消息
        testMessage = new NetworkMessage(
            NetworkMessage.MessageType.PING,
            null, // 发送者
            System.currentTimeMillis(),
            12345L,
            null, // 负载
            new byte[64] // 签名
        );
    }
    
    @Test
    void testNetworkServiceStart() {
        // 测试网络服务启动
        when(networkService.start(8080)).thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> startFuture = networkService.start(8080);
        
        assertNotNull(startFuture);
        assertDoesNotThrow(() -> startFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).start(8080);
    }
    
    @Test
    void testNetworkServiceStop() {
        // 测试网络服务停止
        when(networkService.stop()).thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> stopFuture = networkService.stop();
        
        assertNotNull(stopFuture);
        assertDoesNotThrow(() -> stopFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).stop();
    }
    
    @Test
    void testBroadcastBlockProposal() {
        // 测试区块提议广播
        Block testBlock = createTestBlock();
        when(networkService.broadcastBlockProposal(any(Block.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> broadcastFuture = networkService.broadcastBlockProposal(testBlock);
        
        assertNotNull(broadcastFuture);
        assertDoesNotThrow(() -> broadcastFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).broadcastBlockProposal(testBlock);
    }
    
    @Test
    void testBroadcastTransaction() {
        // 测试交易广播
        Transaction testTransaction = createTestTransaction();
        when(networkService.broadcastTransaction(any(Transaction.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> broadcastFuture = networkService.broadcastTransaction(testTransaction);
        
        assertNotNull(broadcastFuture);
        assertDoesNotThrow(() -> broadcastFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).broadcastTransaction(testTransaction);
    }
    
    @Test
    void testBroadcastVRFAnnouncement() {
        // 测试VRF公告广播
        VRFAnnouncement testAnnouncement = createTestVRFAnnouncement();
        when(networkService.broadcastVRFAnnouncement(any(VRFAnnouncement.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> broadcastFuture = networkService.broadcastVRFAnnouncement(testAnnouncement);
        
        assertNotNull(broadcastFuture);
        assertDoesNotThrow(() -> broadcastFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).broadcastVRFAnnouncement(testAnnouncement);
    }
    
    @Test
    void testSendMessage() {
        // 测试发送消息
        when(networkService.sendMessage(anyString(), any(NetworkMessage.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> sendFuture = networkService.sendMessage("test-peer", testMessage);
        
        assertNotNull(sendFuture);
        assertDoesNotThrow(() -> sendFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).sendMessage("test-peer", testMessage);
    }
    
    @Test
    void testConnectToPeer() {
        // 测试连接节点
        when(networkService.connectToPeer(anyString(), anyInt()))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> connectFuture = networkService.connectToPeer("localhost", 8080);
        
        assertNotNull(connectFuture);
        assertDoesNotThrow(() -> connectFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).connectToPeer("localhost", 8080);
    }
    
    @Test
    void testDisconnectFromPeer() {
        // 测试断开节点连接
        when(networkService.disconnectFromPeer(anyString()))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> disconnectFuture = networkService.disconnectFromPeer("test-peer");
        
        assertNotNull(disconnectFuture);
        assertDoesNotThrow(() -> disconnectFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).disconnectFromPeer("test-peer");
    }
    
    @Test
    void testGetConnectedPeers() {
        // 测试获取连接的节点
        List<PeerInfo> mockPeers = List.of(testPeer);
        when(networkService.getConnectedPeers()).thenReturn(mockPeers);
        
        List<PeerInfo> connectedPeers = networkService.getConnectedPeers();
        
        assertNotNull(connectedPeers);
        assertEquals(1, connectedPeers.size());
        assertEquals("test-peer-1", connectedPeers.get(0).getNodeId());
        
        verify(networkService).getConnectedPeers();
    }
    
    @Test
    void testGetPeerInfo() {
        // 测试获取节点信息
        when(networkService.getPeerInfo("test-peer-1")).thenReturn(testPeer);
        
        PeerInfo peerInfo = networkService.getPeerInfo("test-peer-1");
        
        assertNotNull(peerInfo);
        assertEquals("test-peer-1", peerInfo.getNodeId());
        assertEquals("localhost", peerInfo.getHost());
        assertEquals(8080, peerInfo.getPort());
        assertEquals(PeerInfo.PeerStatus.CONNECTED, peerInfo.getStatus());
        
        verify(networkService).getPeerInfo("test-peer-1");
    }
    
    @Test
    void testIsConnectedToPeer() {
        // 测试检查是否连接到节点
        when(networkService.isConnectedToPeer("test-peer-1")).thenReturn(true);
        
        boolean isConnected = networkService.isConnectedToPeer("test-peer-1");
        
        assertTrue(isConnected);
        
        verify(networkService).isConnectedToPeer("test-peer-1");
    }
    
    @Test
    void testGetNetworkStats() {
        // 测试获取网络统计信息
        NetworkStats mockStats = new NetworkStats(
            100L, 200L, 1000L, 2000L, 5, 10, 60000L, System.currentTimeMillis()
        );
        when(networkService.getNetworkStats()).thenReturn(mockStats);
        
        NetworkStats stats = networkService.getNetworkStats();
        
        assertNotNull(stats);
        assertEquals(100L, stats.getMessagesSent());
        assertEquals(200L, stats.getMessagesReceived());
        assertEquals(1000L, stats.getBytesSent());
        assertEquals(2000L, stats.getBytesReceived());
        assertEquals(5, stats.getConnectedPeers());
        assertEquals(10, stats.getTotalPeers());
        
        verify(networkService).getNetworkStats();
    }
    
    @Test
    void testDiscoverPeers() {
        // 测试发现节点
        List<PeerInfo> mockDiscoveredPeers = List.of(testPeer);
        when(networkService.discoverPeers()).thenReturn(CompletableFuture.completedFuture(mockDiscoveredPeers));
        
        CompletableFuture<List<PeerInfo>> discoverFuture = networkService.discoverPeers();
        
        assertNotNull(discoverFuture);
        List<PeerInfo> discoveredPeers = assertDoesNotThrow(() -> 
            discoverFuture.get(5, TimeUnit.SECONDS));
        
        assertNotNull(discoveredPeers);
        assertEquals(1, discoveredPeers.size());
        
        verify(networkService).discoverPeers();
    }
    
    @Test
    void testSyncBlocks() {
        // 测试区块同步
        List<Block> mockBlocks = List.of(createTestBlock());
        when(networkService.syncBlocks(anyLong(), anyLong()))
            .thenReturn(CompletableFuture.completedFuture(mockBlocks));
        
        CompletableFuture<List<Block>> syncFuture = networkService.syncBlocks(1L, 10L);
        
        assertNotNull(syncFuture);
        List<Block> syncedBlocks = assertDoesNotThrow(() -> 
            syncFuture.get(5, TimeUnit.SECONDS));
        
        assertNotNull(syncedBlocks);
        assertEquals(1, syncedBlocks.size());
        
        verify(networkService).syncBlocks(1L, 10L);
    }
    
    @Test
    void testSendHeartbeat() {
        // 测试发送心跳
        when(networkService.sendHeartbeat()).thenReturn(CompletableFuture.completedFuture(null));
        
        CompletableFuture<Void> heartbeatFuture = networkService.sendHeartbeat();
        
        assertNotNull(heartbeatFuture);
        assertDoesNotThrow(() -> heartbeatFuture.get(5, TimeUnit.SECONDS));
        
        verify(networkService).sendHeartbeat();
    }
    
    @Test
    void testIsHealthy() {
        // 测试网络健康状态
        when(networkService.isHealthy()).thenReturn(true);
        
        boolean isHealthy = networkService.isHealthy();
        
        assertTrue(isHealthy);
        
        verify(networkService).isHealthy();
    }
    
    @Test
    void testMessageListener() {
        // 测试消息监听器
        MessageListener mockListener = mock(MessageListener.class);
        
        networkService.addMessageListener(mockListener);
        networkService.removeMessageListener(mockListener);
        
        verify(networkService).addMessageListener(mockListener);
        verify(networkService).removeMessageListener(mockListener);
    }
    
    @Test
    void testPeerListener() {
        // 测试节点监听器
        PeerListener mockListener = mock(PeerListener.class);
        
        networkService.addPeerListener(mockListener);
        networkService.removePeerListener(mockListener);
        
        verify(networkService).addPeerListener(mockListener);
        verify(networkService).removePeerListener(mockListener);
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
    
    private Transaction createTestTransaction() {
        return Transaction.builder()
                .fromPublicKey("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")
                .toPublicKey("fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210")
                .amount(1000L)
                .fee(10L)
                .timestamp(System.currentTimeMillis())
                .nonce(1L)
                .signature(new byte[64])
                .build();
    }
    
    private VRFAnnouncement createTestVRFAnnouncement() {
        // 创建模拟的VRF输出和证明
        VRFProof vrfProof = new VRFProof(new byte[64]);
        VRFOutput vrfOutput = new VRFOutput(new byte[32], vrfProof);
        
        return new VRFAnnouncement(
            1L,
            keyPair.getPublicKey(),
            vrfOutput,
            vrfProof,
            0.5
        );
    }
}
