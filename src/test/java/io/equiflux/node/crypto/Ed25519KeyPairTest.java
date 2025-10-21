package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.*;

/**
 * Ed25519KeyPair单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class Ed25519KeyPairTest {
    
    private Ed25519KeyPair keyPair;
    private byte[] testData;
    
    @BeforeEach
    void setUp() {
        keyPair = Ed25519KeyPair.generate();
        testData = "Hello, Equiflux!".getBytes();
    }
    
    @Test
    void testGenerateKeyPair() {
        // When
        Ed25519KeyPair newKeyPair = Ed25519KeyPair.generate();
        
        // Then
        assertThat(newKeyPair).isNotNull();
        assertThat(newKeyPair.getPrivateKey()).isNotNull();
        assertThat(newKeyPair.getPublicKey()).isNotNull();
        assertThat(newKeyPair.getPrivateKeyBytes()).isNotNull();
        assertThat(newKeyPair.getPublicKeyBytes()).isNotNull();
    }
    
    @Test
    void testSignAndVerify() {
        // When
        byte[] signature = keyPair.sign(testData);
        
        // Then
        assertThat(signature).isNotNull();
        assertThat(signature).hasSize(64); // Ed25519 signature is 64 bytes
        
        // Verify signature
        boolean isValid = keyPair.verify(testData, signature);
        assertThat(isValid).isTrue();
    }
    
    @Test
    void testSignWithNullData() {
        // Then
        assertThatThrownBy(() -> keyPair.sign(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data to sign cannot be null");
    }
    
    @Test
    void testVerifyWithNullData() {
        // Given
        byte[] signature = keyPair.sign(testData);
        
        // Then
        assertThatThrownBy(() -> keyPair.verify(null, signature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data to verify cannot be null");
        
        assertThatThrownBy(() -> keyPair.verify(testData, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Signature cannot be null");
    }
    
    @Test
    void testVerifyWithWrongSignature() {
        // Given
        byte[] signature = keyPair.sign(testData);
        byte[] wrongSignature = new byte[64];
        System.arraycopy(signature, 0, wrongSignature, 0, 63);
        wrongSignature[63] = (byte) (signature[63] + 1); // Change last byte
        
        // When
        boolean isValid = keyPair.verify(testData, wrongSignature);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void testVerifyWithWrongData() {
        // Given
        byte[] signature = keyPair.sign(testData);
        byte[] wrongData = "Wrong data".getBytes();
        
        // When
        boolean isValid = keyPair.verify(wrongData, signature);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void testStaticVerify() {
        // Given
        byte[] signature = keyPair.sign(testData);
        PublicKey publicKey = keyPair.getPublicKey();
        
        // When
        boolean isValid = Ed25519KeyPair.verify(publicKey, testData, signature);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void testStaticVerifyWithNullPublicKey() {
        // Given
        byte[] signature = keyPair.sign(testData);
        
        // Then
        assertThatThrownBy(() -> Ed25519KeyPair.verify((PublicKey) null, testData, signature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Public key cannot be null");
    }
    
    @Test
    void testGetPublicKeyHex() {
        // When
        String publicKeyHex = keyPair.getPublicKeyHex();
        
        // Then
        assertThat(publicKeyHex).isNotNull();
        assertThat(publicKeyHex).isNotEmpty();
        assertThat(publicKeyHex).matches("[0-9a-f]+"); // Should be hex string
    }
    
    @Test
    void testGetPrivateKeyHex() {
        // When
        String privateKeyHex = keyPair.getPrivateKeyHex();
        
        // Then
        assertThat(privateKeyHex).isNotNull();
        assertThat(privateKeyHex).isNotEmpty();
        assertThat(privateKeyHex).matches("[0-9a-f]+"); // Should be hex string
    }
    
    @Test
    void testEquality() {
        // Given
        Ed25519KeyPair keyPair1 = Ed25519KeyPair.generate();
        Ed25519KeyPair keyPair2 = new Ed25519KeyPair(keyPair1.getPrivateKey(), keyPair1.getPublicKey());
        
        // Then
        assertThat(keyPair1).isEqualTo(keyPair2);
        assertThat(keyPair1.hashCode()).isEqualTo(keyPair2.hashCode());
    }
    
    @Test
    void testInequality() {
        // Given
        Ed25519KeyPair keyPair1 = Ed25519KeyPair.generate();
        Ed25519KeyPair keyPair2 = Ed25519KeyPair.generate();
        
        // Then
        assertThat(keyPair1).isNotEqualTo(keyPair2);
    }
    
    @Test
    void testToString() {
        // When
        String toString = keyPair.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("Ed25519KeyPair");
        assertThat(toString).contains("publicKey");
    }
    
    @Test
    void testConstructorWithNullKeys() {
        // Then
        assertThatThrownBy(() -> new Ed25519KeyPair(null, keyPair.getPublicKey()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Private key cannot be null");
        
        assertThatThrownBy(() -> new Ed25519KeyPair(keyPair.getPrivateKey(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Public key cannot be null");
    }
    
    @Test
    void testConsistency() {
        // Given
        byte[] data1 = "test".getBytes();
        byte[] data2 = "test".getBytes();
        
        // When
        byte[] signature1 = keyPair.sign(data1);
        byte[] signature2 = keyPair.sign(data2);
        
        // Then
        assertThat(signature1).isEqualTo(signature2); // Same input should produce same signature
    }
    
    @Test
    void testDifferentKeysProduceDifferentSignatures() {
        // Given
        Ed25519KeyPair keyPair1 = Ed25519KeyPair.generate();
        Ed25519KeyPair keyPair2 = Ed25519KeyPair.generate();
        
        // When
        byte[] signature1 = keyPair1.sign(testData);
        byte[] signature2 = keyPair2.sign(testData);
        
        // Then
        assertThat(signature1).isNotEqualTo(signature2); // Different keys should produce different signatures
    }
    
    @Test
    void testCrossVerification() {
        // Given
        Ed25519KeyPair keyPair1 = Ed25519KeyPair.generate();
        Ed25519KeyPair keyPair2 = Ed25519KeyPair.generate();
        
        // When
        byte[] signature1 = keyPair1.sign(testData);
        boolean isValid1 = keyPair2.verify(testData, signature1);
        boolean isValid2 = keyPair1.verify(testData, signature1);
        
        // Then
        assertThat(isValid1).isFalse(); // Wrong key should fail verification
        assertThat(isValid2).isTrue();  // Correct key should pass verification
    }
}
