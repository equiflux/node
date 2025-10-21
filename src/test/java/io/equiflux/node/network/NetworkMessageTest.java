package io.equiflux.node.network;

import io.equiflux.node.crypto.Ed25519KeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.*;

/**
 * 网络消息测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class NetworkMessageTest {
    
    private Ed25519KeyPair keyPair;
    private PublicKey publicKey;
    
    @BeforeEach
    void setUp() {
        keyPair = Ed25519KeyPair.generate();
        publicKey = keyPair.getPublicKey();
    }
    
    @Test
    void testNetworkMessageCreation() {
        // Given
        NetworkMessage.MessageType type = NetworkMessage.MessageType.TRANSACTION;
        long timestamp = System.currentTimeMillis();
        long nonce = 12345L;
        Object payload = "test payload";
        byte[] signature = new byte[64];
        
        // When
        NetworkMessage message = new NetworkMessage(type, publicKey, timestamp, nonce, payload, signature);
        
        // Then
        assertThat(message.getType()).isEqualTo(type);
        assertThat(message.getSender()).isEqualTo(publicKey);
        assertThat(message.getTimestamp()).isEqualTo(timestamp);
        assertThat(message.getNonce()).isEqualTo(nonce);
        assertThat(message.getPayload()).isEqualTo(payload);
        assertThat(message.getSignature()).isEqualTo(signature);
    }
    
    @Test
    void testNetworkMessageValidation() {
        // Given
        NetworkMessage.MessageType type = NetworkMessage.MessageType.PING;
        long timestamp = System.currentTimeMillis();
        long nonce = 12345L;
        Object payload = null; // PING消息无负载
        byte[] signature = new byte[64];
        
        // When
        NetworkMessage message = new NetworkMessage(type, publicKey, timestamp, nonce, payload, signature);
        
        // Then
        assertThat(message.isValidFormat()).isTrue();
    }
    
    @Test
    void testNetworkMessageExpiration() {
        // Given
        NetworkMessage.MessageType type = NetworkMessage.MessageType.TRANSACTION;
        long timestamp = System.currentTimeMillis() - 10000; // 10秒前
        long nonce = 12345L;
        Object payload = "test payload";
        byte[] signature = new byte[64];
        
        NetworkMessage message = new NetworkMessage(type, publicKey, timestamp, nonce, payload, signature);
        
        // When & Then
        assertThat(message.isExpired(5000)).isTrue(); // 5秒过期
        assertThat(message.isExpired(15000)).isFalse(); // 15秒过期
    }
    
    @Test
    void testNetworkMessageSerialization() {
        // Given
        NetworkMessage.MessageType type = NetworkMessage.MessageType.TRANSACTION;
        long timestamp = System.currentTimeMillis();
        long nonce = 12345L;
        Object payload = "test payload";
        byte[] signature = new byte[64];
        
        NetworkMessage message = new NetworkMessage(type, publicKey, timestamp, nonce, payload, signature);
        
        // When
        byte[] serialized = message.serializeForSigning();
        
        // Then
        assertThat(serialized).isNotNull();
        assertThat(serialized.length).isGreaterThan(0);
    }
    
    @Test
    void testNetworkMessageEquality() {
        // Given
        NetworkMessage.MessageType type = NetworkMessage.MessageType.TRANSACTION;
        long timestamp = System.currentTimeMillis();
        long nonce = 12345L;
        Object payload = "test payload";
        byte[] signature = new byte[64];
        
        NetworkMessage message1 = new NetworkMessage(type, publicKey, timestamp, nonce, payload, signature);
        NetworkMessage message2 = new NetworkMessage(type, publicKey, timestamp, nonce, payload, signature);
        
        // When & Then
        assertThat(message1).isEqualTo(message2);
        assertThat(message1.hashCode()).isEqualTo(message2.hashCode());
    }
}
