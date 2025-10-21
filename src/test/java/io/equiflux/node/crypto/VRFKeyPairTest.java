package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;
import io.equiflux.node.model.VRFOutput;
import io.equiflux.node.model.VRFProof;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.*;

/**
 * VRFKeyPair单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class VRFKeyPairTest {
    
    private VRFKeyPair vrfKeyPair;
    private byte[] testInput;
    
    @BeforeEach
    void setUp() {
        vrfKeyPair = VRFKeyPair.generate();
        testInput = "VRF test input".getBytes();
    }
    
    @Test
    void testGenerateVRFKeyPair() {
        // When
        VRFKeyPair newVrfKeyPair = VRFKeyPair.generate();
        
        // Then
        assertThat(newVrfKeyPair).isNotNull();
        assertThat(newVrfKeyPair.getPrivateKey()).isNotNull();
        assertThat(newVrfKeyPair.getPublicKey()).isNotNull();
        assertThat(newVrfKeyPair.getPrivateKeyBytes()).isNotNull();
        assertThat(newVrfKeyPair.getPublicKeyBytes()).isNotNull();
    }
    
    @Test
    void testEvaluateVRF() {
        // When
        VRFOutput vrfOutput = vrfKeyPair.evaluate(testInput);
        
        // Then
        assertThat(vrfOutput).isNotNull();
        assertThat(vrfOutput.getOutput()).hasSize(32); // VRF output is 32 bytes
        assertThat(vrfOutput.getProof()).isNotNull();
        assertThat(vrfOutput.getProof().getProof()).hasSize(64); // VRF proof is 64 bytes
    }
    
    @Test
    void testEvaluateVRFWithNullInput() {
        // Then
        assertThatThrownBy(() -> vrfKeyPair.evaluate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF input cannot be null");
    }
    
    @Test
    void testVerifyVRF() {
        // Given
        VRFOutput vrfOutput = vrfKeyPair.evaluate(testInput);
        
        // When
        boolean isValid = VRFKeyPair.verify(vrfKeyPair.getPublicKey(), testInput, vrfOutput, vrfOutput.getProof());
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void testVerifyVRFWithWrongInput() {
        // Given
        VRFOutput vrfOutput = vrfKeyPair.evaluate(testInput);
        byte[] wrongInput = "Wrong input".getBytes();
        
        // When
        boolean isValid = VRFKeyPair.verify(vrfKeyPair.getPublicKey(), wrongInput, vrfOutput, vrfOutput.getProof());
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void testVerifyVRFWithWrongPublicKey() {
        // Given
        VRFOutput vrfOutput = vrfKeyPair.evaluate(testInput);
        VRFKeyPair otherKeyPair = VRFKeyPair.generate();
        
        // When
        boolean isValid = VRFKeyPair.verify(otherKeyPair.getPublicKey(), testInput, vrfOutput, vrfOutput.getProof());
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void testVerifyVRFWithNullParameters() {
        // Given
        VRFOutput vrfOutput = vrfKeyPair.evaluate(testInput);
        
        // Then
        assertThatThrownBy(() -> VRFKeyPair.verify(null, testInput, vrfOutput, vrfOutput.getProof()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Public key cannot be null");
        
        assertThatThrownBy(() -> VRFKeyPair.verify(vrfKeyPair.getPublicKey(), null, vrfOutput, vrfOutput.getProof()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF input cannot be null");
        
        assertThatThrownBy(() -> VRFKeyPair.verify(vrfKeyPair.getPublicKey(), testInput, null, vrfOutput.getProof()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF output cannot be null");
        
        assertThatThrownBy(() -> VRFKeyPair.verify(vrfKeyPair.getPublicKey(), testInput, vrfOutput, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF proof cannot be null");
    }
    
    @Test
    void testVRFConsistency() {
        // Given
        byte[] input1 = "test".getBytes();
        byte[] input2 = "test".getBytes();
        
        // When
        VRFOutput output1 = vrfKeyPair.evaluate(input1);
        VRFOutput output2 = vrfKeyPair.evaluate(input2);
        
        // Then
        assertThat(output1.getOutput()).isEqualTo(output2.getOutput()); // Same input should produce same output
        assertThat(output1.getProof()).isEqualTo(output2.getProof()); // Same input should produce same proof
    }
    
    @Test
    void testVRFUniqueness() {
        // Given
        byte[] input1 = "test1".getBytes();
        byte[] input2 = "test2".getBytes();
        
        // When
        VRFOutput output1 = vrfKeyPair.evaluate(input1);
        VRFOutput output2 = vrfKeyPair.evaluate(input2);
        
        // Then
        assertThat(output1.getOutput()).isNotEqualTo(output2.getOutput()); // Different input should produce different output
        assertThat(output1.getProof()).isNotEqualTo(output2.getProof()); // Different input should produce different proof
    }
    
    @Test
    void testVRFScore() {
        // Given
        VRFOutput vrfOutput = vrfKeyPair.evaluate(testInput);
        
        // When
        double score = vrfOutput.toScore();
        
        // Then
        assertThat(score).isBetween(0.0, 1.0); // Score should be between 0 and 1
    }
    
    @Test
    void testGetPublicKeyHex() {
        // When
        String publicKeyHex = vrfKeyPair.getPublicKeyHex();
        
        // Then
        assertThat(publicKeyHex).isNotNull();
        assertThat(publicKeyHex).isNotEmpty();
        assertThat(publicKeyHex).matches("[0-9a-f]+"); // Should be hex string
    }
    
    @Test
    void testGetPrivateKeyHex() {
        // When
        String privateKeyHex = vrfKeyPair.getPrivateKeyHex();
        
        // Then
        assertThat(privateKeyHex).isNotNull();
        assertThat(privateKeyHex).isNotEmpty();
        assertThat(privateKeyHex).matches("[0-9a-f]+"); // Should be hex string
    }
    
    @Test
    void testEquality() {
        // Given
        VRFKeyPair vrfKeyPair1 = VRFKeyPair.generate();
        VRFKeyPair vrfKeyPair2 = new VRFKeyPair(vrfKeyPair1.getPrivateKey(), vrfKeyPair1.getPublicKey());
        
        // Then
        assertThat(vrfKeyPair1).isEqualTo(vrfKeyPair2);
        assertThat(vrfKeyPair1.hashCode()).isEqualTo(vrfKeyPair2.hashCode());
    }
    
    @Test
    void testInequality() {
        // Given
        VRFKeyPair vrfKeyPair1 = VRFKeyPair.generate();
        VRFKeyPair vrfKeyPair2 = VRFKeyPair.generate();
        
        // Then
        assertThat(vrfKeyPair1).isNotEqualTo(vrfKeyPair2);
    }
    
    @Test
    void testToString() {
        // When
        String toString = vrfKeyPair.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("VRFKeyPair");
        assertThat(toString).contains("publicKey");
    }
    
    @Test
    void testConstructorWithNullKeys() {
        // Then
        assertThatThrownBy(() -> new VRFKeyPair(null, vrfKeyPair.getPublicKey()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Private key cannot be null");
        
        assertThatThrownBy(() -> new VRFKeyPair(vrfKeyPair.getPrivateKey(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Public key cannot be null");
    }
    
    @Test
    void testDifferentKeysProduceDifferentOutputs() {
        // Given
        VRFKeyPair vrfKeyPair1 = VRFKeyPair.generate();
        VRFKeyPair vrfKeyPair2 = VRFKeyPair.generate();
        
        // When
        VRFOutput output1 = vrfKeyPair1.evaluate(testInput);
        VRFOutput output2 = vrfKeyPair2.evaluate(testInput);
        
        // Then
        assertThat(output1.getOutput()).isNotEqualTo(output2.getOutput()); // Different keys should produce different outputs
        assertThat(output1.getProof()).isNotEqualTo(output2.getProof()); // Different keys should produce different proofs
    }
    
    @Test
    void testVRFRandomness() {
        // Given
        byte[] input = "randomness test".getBytes();
        
        // When
        VRFOutput output1 = vrfKeyPair.evaluate(input);
        VRFOutput output2 = vrfKeyPair.evaluate(input);
        
        // Then
        assertThat(output1.getOutput()).isEqualTo(output2.getOutput()); // Same input should produce same output
        
        // Test randomness by checking that outputs look random
        byte[] output = output1.getOutput();
        int zeroBytes = 0;
        int maxBytes = 0;
        int minBytes = 255;
        
        for (byte b : output) {
            int unsignedByte = b & 0xFF;
            if (unsignedByte == 0) zeroBytes++;
            if (unsignedByte > maxBytes) maxBytes = unsignedByte;
            if (unsignedByte < minBytes) minBytes = unsignedByte;
        }
        
        // Random output should have good distribution
        assertThat(zeroBytes).isLessThan(8); // Not too many zeros
        assertThat(maxBytes).isGreaterThan(200); // Some high values
        assertThat(minBytes).isLessThan(55); // Some low values
    }
}
