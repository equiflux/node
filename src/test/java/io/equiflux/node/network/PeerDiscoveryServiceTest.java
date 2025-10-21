package io.equiflux.node.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * 节点发现服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class PeerDiscoveryServiceTest {
    
    @Mock
    private NetworkService networkService;
    
    @Mock
    private NetworkConfig networkConfig;
    
    private PeerDiscoveryService peerDiscoveryService;
    
    @BeforeEach
    void setUp() {
        peerDiscoveryService = new PeerDiscoveryService();
        ReflectionTestUtils.setField(peerDiscoveryService, "networkService", networkService);
        ReflectionTestUtils.setField(peerDiscoveryService, "networkConfig", networkConfig);
        
        // 设置默认配置
        lenient().when(networkConfig.getMaxPeers()).thenReturn(50);
        lenient().when(networkConfig.getMinPeers()).thenReturn(5);
        lenient().when(networkConfig.getPeerExpirationMs()).thenReturn(300000L);
        
        // 初始化服务状态
        ReflectionTestUtils.setField(peerDiscoveryService, "running", new java.util.concurrent.atomic.AtomicBoolean(true));
        ReflectionTestUtils.setField(peerDiscoveryService, "peersDiscovered", new java.util.concurrent.atomic.AtomicLong(0));
        ReflectionTestUtils.setField(peerDiscoveryService, "peersConnected", new java.util.concurrent.atomic.AtomicLong(0));
        
        // 初始化执行器
        ReflectionTestUtils.setField(peerDiscoveryService, "discoveryExecutor", 
            java.util.concurrent.Executors.newScheduledThreadPool(2));
        ReflectionTestUtils.setField(peerDiscoveryService, "connectionExecutor", 
            java.util.concurrent.Executors.newFixedThreadPool(4));
        
        // Mock networkService methods
        lenient().when(networkService.connectToPeer(anyString(), anyInt()))
            .thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(networkService.disconnectFromPeer(anyString()))
            .thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(networkService.getLocalPeerInfo())
            .thenReturn(createMockPeerInfo());
    }
    
    @Test
    void testDiscoverPeers() {
        // 测试发现节点
        CompletableFuture<List<PeerInfo>> future = peerDiscoveryService.discoverPeers();
        assertNotNull(future);
        
        // 等待完成
        List<PeerInfo> peers = future.join();
        assertNotNull(peers);
    }
    
    @Test
    void testConnectToPeer() {
        // 创建测试节点信息
        PublicKey mockPublicKey = mock(PublicKey.class);
        PeerInfo peerInfo = new PeerInfo(
            "test-peer",
            mockPublicKey,
            "127.0.0.1",
            8081,
            PeerInfo.PeerStatus.DISCONNECTED,
            System.currentTimeMillis(),
            0,
            0
        );
        
        // 测试连接到节点
        CompletableFuture<Boolean> future = peerDiscoveryService.connectToPeer(peerInfo);
        assertNotNull(future);
    }
    
    @Test
    void testDisconnectFromPeer() {
        // 测试断开节点连接
        CompletableFuture<Boolean> future = peerDiscoveryService.disconnectFromPeer("test-peer");
        assertNotNull(future);
    }
    
    @Test
    void testGetKnownPeers() {
        // 测试获取已知节点列表
        List<PeerInfo> peers = peerDiscoveryService.getKnownPeers();
        assertNotNull(peers);
    }
    
    @Test
    void testGetConnectedPeers() {
        // 测试获取已连接节点列表
        List<PeerInfo> peers = peerDiscoveryService.getConnectedPeers();
        assertNotNull(peers);
    }
    
    @Test
    void testGetAvailablePeers() {
        // 测试获取可用节点列表
        List<PeerInfo> peers = peerDiscoveryService.getAvailablePeers();
        assertNotNull(peers);
    }
    
    @Test
    void testIsConnectedToPeer() {
        // 测试是否连接到指定节点
        boolean connected = peerDiscoveryService.isConnectedToPeer("test-peer");
        assertFalse(connected);
    }
    
    @Test
    void testGetPeerInfo() {
        // 测试获取节点信息
        PeerInfo peerInfo = peerDiscoveryService.getPeerInfo("test-peer");
        assertNull(peerInfo);
    }
    
    @Test
    void testGetStats() {
        // 测试获取发现统计信息
        PeerDiscoveryService.DiscoveryStats stats = peerDiscoveryService.getStats();
        assertNotNull(stats);
        assertEquals(0, stats.getKnownPeers());
        assertEquals(0, stats.getConnectedPeers());
        assertEquals(0, stats.getPeersDiscovered());
        assertEquals(0, stats.getPeersConnected());
        assertEquals(0.0, stats.getConnectionRate());
    }
    
    @Test
    void testPeerInfo() {
        // 测试PeerInfo类
        PublicKey mockPublicKey = mock(PublicKey.class);
        PeerInfo peerInfo = new PeerInfo(
            "test-node",
            mockPublicKey,
            "127.0.0.1",
            8080,
            PeerInfo.PeerStatus.CONNECTED,
            System.currentTimeMillis(),
            0,
            0
        );
        
        assertEquals("test-node", peerInfo.getNodeId());
        assertEquals("127.0.0.1", peerInfo.getHost());
        assertEquals(8080, peerInfo.getPort());
        assertEquals(PeerInfo.PeerStatus.CONNECTED, peerInfo.getStatus());
        assertTrue(peerInfo.isConnected());
        assertFalse(peerInfo.isConnecting());
        assertFalse(peerInfo.isFailed());
        
        String addressString = peerInfo.getAddressString();
        assertEquals("127.0.0.1:8080", addressString);
    }
    
    @Test
    void testPeerInfoStatusChanges() {
        // 测试PeerInfo状态变化
        PublicKey mockPublicKey = mock(PublicKey.class);
        PeerInfo peerInfo = new PeerInfo(
            "test-node",
            mockPublicKey,
            "127.0.0.1",
            8080,
            PeerInfo.PeerStatus.DISCONNECTED,
            System.currentTimeMillis(),
            0,
            0
        );
        
        // 测试状态更新
        PeerInfo connectedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.CONNECTED);
        assertEquals(PeerInfo.PeerStatus.CONNECTED, connectedPeer.getStatus());
        
        // 测试最后见到时间更新
        long newLastSeen = System.currentTimeMillis() + 1000; // 确保时间差
        PeerInfo updatedPeer = peerInfo.withLastSeen(newLastSeen);
        assertTrue(updatedPeer.getLastSeen() > peerInfo.getLastSeen());
        
        // 测试连接尝试次数增加
        PeerInfo incrementedPeer = peerInfo.withIncrementedConnectionAttempts();
        assertEquals(1, incrementedPeer.getConnectionAttempts());
        
        // 测试重置连接尝试次数
        PeerInfo resetPeer = incrementedPeer.withResetConnectionAttempts();
        assertEquals(0, resetPeer.getConnectionAttempts());
    }
    
    @Test
    void testPeerInfoValidation() {
        // 测试PeerInfo验证
        assertThrows(IllegalArgumentException.class, () -> {
            PublicKey mockPublicKey = mock(PublicKey.class);
            new PeerInfo(
                "test-node",
                mockPublicKey,
                "127.0.0.1",
                0, // 无效端口
                PeerInfo.PeerStatus.CONNECTED,
                System.currentTimeMillis(),
                0,
                0
            );
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            PublicKey mockPublicKey = mock(PublicKey.class);
            new PeerInfo(
                "test-node",
                mockPublicKey,
                "127.0.0.1",
                8080,
                PeerInfo.PeerStatus.CONNECTED,
                -1, // 无效时间戳
                0,
                0
            );
        });
    }
    
    @Test
    void testPeerInfoExpiration() {
        // 测试PeerInfo过期检查
        long currentTime = System.currentTimeMillis();
        PublicKey mockPublicKey = mock(PublicKey.class);
        PeerInfo peerInfo = new PeerInfo(
            "test-node",
            mockPublicKey,
            "127.0.0.1",
            8080,
            PeerInfo.PeerStatus.CONNECTED,
            currentTime - 1000, // 1秒前
            0,
            0
        );
        
        // 检查是否过期（5分钟）
        assertFalse(peerInfo.isExpired(300000));
        assertTrue(peerInfo.isExpired(500)); // 500毫秒
    }
    
    @Test
    void testPeerInfoRetryLogic() {
        // 测试PeerInfo重试逻辑
        PublicKey mockPublicKey = mock(PublicKey.class);
        PeerInfo peerInfo = new PeerInfo(
            "test-node",
            mockPublicKey,
            "127.0.0.1",
            8080,
            PeerInfo.PeerStatus.FAILED,
            System.currentTimeMillis(),
            2, // 已尝试2次
            System.currentTimeMillis() - 1000 // 1秒前尝试
        );
        
        // 测试是否可以重试
        assertTrue(peerInfo.canRetryConnection(5, 500)); // 最大5次，间隔500ms
        assertFalse(peerInfo.canRetryConnection(2, 500)); // 已达到最大次数
    }
    
    @Test
    void testDiscoveryStats() {
        // 测试发现统计信息
        PeerDiscoveryService.DiscoveryStats stats = new PeerDiscoveryService.DiscoveryStats(
            10, 5, 100, 50
        );
        
        assertEquals(10, stats.getKnownPeers());
        assertEquals(5, stats.getConnectedPeers());
        assertEquals(100, stats.getPeersDiscovered());
        assertEquals(50, stats.getPeersConnected());
        assertEquals(0.5, stats.getConnectionRate());
        
        String statsString = stats.toString();
        assertTrue(statsString.contains("knownPeers=10"));
        assertTrue(statsString.contains("connectedPeers=5"));
        assertTrue(statsString.contains("peersDiscovered=100"));
        assertTrue(statsString.contains("peersConnected=50"));
        assertTrue(statsString.contains("connectionRate=50.00%"));
    }
    
    /**
     * 创建模拟的PeerInfo对象
     */
    private PeerInfo createMockPeerInfo() {
        PublicKey mockPublicKey = mock(PublicKey.class);
        return new PeerInfo(
            "local-node",
            mockPublicKey,
            "127.0.0.1",
            8080,
            PeerInfo.PeerStatus.CONNECTED,
            System.currentTimeMillis(),
            0,
            0
        );
    }
}
