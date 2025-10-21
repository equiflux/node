package io.equiflux.node.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.*;

/**
 * VRFProof单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class VRFProofTest {
    
    private byte[] validProof;
    
    @BeforeEach
    void setUp() {
        // 创建64字节的有效证明
        validProof = new byte[64];
        for (int i = 0; i < 64; i++) {
            validProof[i] = (byte) (i % 256);
        }
    }
    
    @Test
    void testConstructor() {
        // When
        VRFProof vrfProof = new VRFProof(validProof);
        
        // Then
        assertThat(vrfProof.getProof()).isEqualTo(validProof);
        assertThat(vrfProof.getProof()).hasSize(64);
    }
    
    @Test
    void testConstructorWithNullProof() {
        // Then
        assertThatThrownBy(() -> new VRFProof(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("VRF proof cannot be null");
    }
    
    @Test
    void testConstructorWithInvalidProofSize() {
        // Given
        byte[] shortProof = new byte[32];
        byte[] longProof = new byte[128];
        
        // Then
        assertThatThrownBy(() -> new VRFProof(shortProof))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF proof must be 64 bytes");
        
        assertThatThrownBy(() -> new VRFProof(longProof))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF proof must be 64 bytes");
    }
    
    @Test
    void testGetProofReturnsClone() {
        // Given
        VRFProof vrfProof = new VRFProof(validProof);
        
        // When
        byte[] returnedProof = vrfProof.getProof();
        
        // Then
        assertThat(returnedProof).isEqualTo(validProof);
        assertThat(returnedProof).isNotSameAs(validProof); // Should be a clone
    }
    
    @Test
    void testToHexString() {
        // Given
        VRFProof vrfProof = new VRFProof(validProof);
        
        // When
        String hexString = vrfProof.toHexString();
        
        // Then
        assertThat(hexString).isNotNull();
        assertThat(hexString).hasSize(128); // 64 bytes * 2 hex chars per byte
        assertThat(hexString).matches("[0-9a-f]+");
    }
    
    @Test
    void testToHexStringWithSpecificBytes() {
        // Given
        byte[] specificProof = new byte[64];
        specificProof[0] = (byte) 0xAB;
        specificProof[1] = (byte) 0xCD;
        specificProof[2] = (byte) 0xEF;
        VRFProof vrfProof = new VRFProof(specificProof);
        
        // When
        String hexString = vrfProof.toHexString();
        
        // Then
        assertThat(hexString).startsWith("abcdef");
    }
    
    @Test
    void testEquality() {
        // Given
        VRFProof vrfProof1 = new VRFProof(validProof);
        VRFProof vrfProof2 = new VRFProof(validProof);
        
        // Then
        assertThat(vrfProof1).isEqualTo(vrfProof2);
        assertThat(vrfProof1.hashCode()).isEqualTo(vrfProof2.hashCode());
    }
    
    @Test
    void testInequality() {
        // Given
        byte[] differentProof = new byte[64];
        for (int i = 0; i < 64; i++) {
            differentProof[i] = (byte) ((i + 1) % 256);
        }
        
        VRFProof vrfProof1 = new VRFProof(validProof);
        VRFProof vrfProof2 = new VRFProof(differentProof);
        
        // Then
        assertThat(vrfProof1).isNotEqualTo(vrfProof2);
    }
    
    @Test
    void testToString() {
        // Given
        VRFProof vrfProof = new VRFProof(validProof);
        
        // When
        String toString = vrfProof.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("VRFProof");
        assertThat(toString).contains("proof=");
    }
    
    @Test
    void testSelfEquality() {
        // Given
        VRFProof vrfProof = new VRFProof(validProof);
        
        // Then
        assertThat(vrfProof).isEqualTo(vrfProof);
    }
    
    @Test
    void testNullEquality() {
        // Given
        VRFProof vrfProof = new VRFProof(validProof);
        
        // Then
        assertThat(vrfProof).isNotEqualTo(null);
    }
    
    @Test
    void testDifferentClassEquality() {
        // Given
        VRFProof vrfProof = new VRFProof(validProof);
        String differentObject = "not a VRFProof";
        
        // Then
        assertThat(vrfProof).isNotEqualTo(differentObject);
    }
    
    @Test
    void testHashCodeConsistency() {
        // Given
        VRFProof vrfProof1 = new VRFProof(validProof);
        VRFProof vrfProof2 = new VRFProof(validProof);
        
        // Then
        assertThat(vrfProof1.hashCode()).isEqualTo(vrfProof2.hashCode());
    }
    
    @Test
    void testHashCodeDifferentForDifferentProofs() {
        // Given
        byte[] differentProof = new byte[64];
        for (int i = 0; i < 64; i++) {
            differentProof[i] = (byte) ((i + 1) % 256);
        }
        
        VRFProof vrfProof1 = new VRFProof(validProof);
        VRFProof vrfProof2 = new VRFProof(differentProof);
        
        // Then
        assertThat(vrfProof1.hashCode()).isNotEqualTo(vrfProof2.hashCode());
    }
    
    @Test
    void testEmptyProof() {
        // Given
        byte[] emptyProof = new byte[64]; // All zeros
        
        // When
        VRFProof vrfProof = new VRFProof(emptyProof);
        
        // Then
        assertThat(vrfProof.getProof()).isEqualTo(emptyProof);
        assertThat(vrfProof.toHexString()).isEqualTo("0".repeat(128));
    }
    
    @Test
    void testMaxValueProof() {
        // Given
        byte[] maxProof = new byte[64];
        for (int i = 0; i < 64; i++) {
            maxProof[i] = (byte) 0xFF;
        }
        
        // When
        VRFProof vrfProof = new VRFProof(maxProof);
        
        // Then
        assertThat(vrfProof.getProof()).isEqualTo(maxProof);
        assertThat(vrfProof.toHexString()).isEqualTo("f".repeat(128));
    }
}
