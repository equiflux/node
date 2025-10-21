package io.equiflux.node.network;

import io.equiflux.node.crypto.Ed25519KeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.*;

/**
 * 节点信息测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class PeerInfoTest {
    
    private Ed25519KeyPair keyPair;
    private PublicKey publicKey;
    
    @BeforeEach
    void setUp() {
        keyPair = Ed25519KeyPair.generate();
        publicKey = keyPair.getPublicKey();
    }
    
    @Test
    void testPeerInfoCreation() {
        // Given
        String nodeId = "test-node-1";
        String host = "localhost";
        int port = 8080;
        PeerInfo.PeerStatus status = PeerInfo.PeerStatus.CONNECTED;
        long lastSeen = System.currentTimeMillis();
        int connectionAttempts = 0;
        long lastConnectionAttempt = 0;
        
        // When
        PeerInfo peerInfo = new PeerInfo(nodeId, publicKey, host, port, status, 
                                       lastSeen, connectionAttempts, lastConnectionAttempt);
        
        // Then
        assertThat(peerInfo.getNodeId()).isEqualTo(nodeId);
        assertThat(peerInfo.getPublicKey()).isEqualTo(publicKey);
        assertThat(peerInfo.getHost()).isEqualTo(host);
        assertThat(peerInfo.getPort()).isEqualTo(port);
        assertThat(peerInfo.getStatus()).isEqualTo(status);
        assertThat(peerInfo.getLastSeen()).isEqualTo(lastSeen);
        assertThat(peerInfo.getConnectionAttempts()).isEqualTo(connectionAttempts);
        assertThat(peerInfo.getLastConnectionAttempt()).isEqualTo(lastConnectionAttempt);
    }
    
    @Test
    void testPeerInfoAddress() {
        // Given
        String nodeId = "test-node-1";
        String host = "localhost";
        int port = 8080;
        PeerInfo peerInfo = new PeerInfo(nodeId, publicKey, host, port, 
                                       PeerInfo.PeerStatus.CONNECTED, 
                                       System.currentTimeMillis(), 0, 0);
        
        // When & Then
        assertThat(peerInfo.getAddressString()).isEqualTo("localhost:8080");
        assertThat(peerInfo.getAddress().getHostName()).isEqualTo("localhost");
        assertThat(peerInfo.getAddress().getPort()).isEqualTo(8080);
    }
    
    @Test
    void testPeerInfoStatus() {
        // Given
        String nodeId = "test-node-1";
        PeerInfo connectedPeer = new PeerInfo(nodeId, publicKey, "localhost", 8080, 
                                            PeerInfo.PeerStatus.CONNECTED, 
                                            System.currentTimeMillis(), 0, 0);
        
        PeerInfo disconnectedPeer = new PeerInfo(nodeId, publicKey, "localhost", 8080, 
                                               PeerInfo.PeerStatus.DISCONNECTED, 
                                               System.currentTimeMillis(), 0, 0);
        
        PeerInfo failedPeer = new PeerInfo(nodeId, publicKey, "localhost", 8080, 
                                         PeerInfo.PeerStatus.FAILED, 
                                         System.currentTimeMillis(), 0, 0);
        
        // When & Then
        assertThat(connectedPeer.isOnline()).isTrue();
        assertThat(connectedPeer.isConnected()).isTrue();
        assertThat(connectedPeer.canConnect()).isFalse();
        assertThat(connectedPeer.isFailed()).isFalse();
        
        assertThat(disconnectedPeer.isOnline()).isFalse();
        assertThat(disconnectedPeer.isConnected()).isFalse();
        assertThat(disconnectedPeer.canConnect()).isTrue();
        assertThat(disconnectedPeer.isFailed()).isFalse();
        
        assertThat(failedPeer.isOnline()).isFalse();
        assertThat(failedPeer.isConnected()).isFalse();
        assertThat(failedPeer.canConnect()).isTrue();
        assertThat(failedPeer.isFailed()).isTrue();
    }
    
    @Test
    void testPeerInfoExpiration() {
        // Given
        long currentTime = System.currentTimeMillis();
        long oldTime = currentTime - 10000; // 10秒前
        
        PeerInfo oldPeer = new PeerInfo("old-node", publicKey, "localhost", 8080, 
                                      PeerInfo.PeerStatus.CONNECTED, oldTime, 0, 0);
        
        PeerInfo recentPeer = new PeerInfo("recent-node", publicKey, "localhost", 8080, 
                                         PeerInfo.PeerStatus.CONNECTED, currentTime, 0, 0);
        
        // When & Then
        assertThat(oldPeer.isExpired(5000)).isTrue(); // 5秒过期
        assertThat(oldPeer.isExpired(15000)).isFalse(); // 15秒过期
        
        assertThat(recentPeer.isExpired(5000)).isFalse(); // 5秒过期
        assertThat(recentPeer.isExpired(15000)).isFalse(); // 15秒过期
    }
    
    @Test
    void testPeerInfoRetryConnection() {
        // Given
        PeerInfo peerInfo = new PeerInfo("test-node", publicKey, "localhost", 8080, 
                                       PeerInfo.PeerStatus.FAILED, 
                                       System.currentTimeMillis(), 2, 
                                       System.currentTimeMillis() - 2000);
        
        // When & Then
        assertThat(peerInfo.canRetryConnection(5, 1000)).isTrue(); // 最大5次，间隔1秒
        assertThat(peerInfo.canRetryConnection(2, 1000)).isFalse(); // 已达到最大次数
        assertThat(peerInfo.canRetryConnection(5, 5000)).isFalse(); // 间隔未到
    }
    
    @Test
    void testPeerInfoWithMethods() {
        // Given
        long originalTime = System.currentTimeMillis();
        PeerInfo originalPeer = new PeerInfo("test-node", publicKey, "localhost", 8080, 
                                           PeerInfo.PeerStatus.DISCONNECTED, 
                                           originalTime, 0, 0);
        
        // When
        PeerInfo connectedPeer = originalPeer.withStatus(PeerInfo.PeerStatus.CONNECTED);
        long updatedTime = originalTime + 1000; // 确保时间戳不同
        PeerInfo updatedPeer = connectedPeer.withLastSeen(updatedTime);
        PeerInfo incrementedPeer = updatedPeer.withIncrementedConnectionAttempts();
        PeerInfo resetPeer = incrementedPeer.withResetConnectionAttempts();
        
        // Then
        assertThat(connectedPeer.getStatus()).isEqualTo(PeerInfo.PeerStatus.CONNECTED);
        assertThat(updatedPeer.getLastSeen()).isGreaterThan(originalPeer.getLastSeen());
        assertThat(incrementedPeer.getConnectionAttempts()).isEqualTo(1);
        assertThat(resetPeer.getConnectionAttempts()).isEqualTo(0);
    }
    
    @Test
    void testPeerInfoEquality() {
        // Given
        PeerInfo peerInfo1 = new PeerInfo("test-node", publicKey, "localhost", 8080, 
                                        PeerInfo.PeerStatus.CONNECTED, 
                                        System.currentTimeMillis(), 0, 0);
        
        PeerInfo peerInfo2 = new PeerInfo("test-node", publicKey, "localhost", 8080, 
                                        PeerInfo.PeerStatus.CONNECTED, 
                                        System.currentTimeMillis(), 0, 0);
        
        // When & Then
        assertThat(peerInfo1).isEqualTo(peerInfo2);
        assertThat(peerInfo1.hashCode()).isEqualTo(peerInfo2.hashCode());
    }
    
    @Test
    void testPeerInfoValidation() {
        // Given & When & Then
        assertThatThrownBy(() -> new PeerInfo(null, publicKey, "localhost", 8080, 
                                            PeerInfo.PeerStatus.CONNECTED, 
                                            System.currentTimeMillis(), 0, 0))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Node ID cannot be null");
        
        assertThatThrownBy(() -> new PeerInfo("test-node", null, "localhost", 8080, 
                                            PeerInfo.PeerStatus.CONNECTED, 
                                            System.currentTimeMillis(), 0, 0))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Public key cannot be null");
        
        assertThatThrownBy(() -> new PeerInfo("test-node", publicKey, null, 8080, 
                                            PeerInfo.PeerStatus.CONNECTED, 
                                            System.currentTimeMillis(), 0, 0))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Host cannot be null");
        
        assertThatThrownBy(() -> new PeerInfo("test-node", publicKey, "localhost", 0, 
                                            PeerInfo.PeerStatus.CONNECTED, 
                                            System.currentTimeMillis(), 0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Port must be between 1 and 65535");
        
        assertThatThrownBy(() -> new PeerInfo("test-node", publicKey, "localhost", 8080, 
                                            PeerInfo.PeerStatus.CONNECTED, 
                                            System.currentTimeMillis(), -1, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Connection attempts cannot be negative");
    }
}
