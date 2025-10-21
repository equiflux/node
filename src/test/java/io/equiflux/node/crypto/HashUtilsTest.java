package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.*;

/**
 * HashUtils单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class HashUtilsTest {
    
    private byte[] testData;
    private String testString;
    
    @BeforeEach
    void setUp() {
        testData = "Hello, Equiflux!".getBytes();
        testString = "Hello, Equiflux!";
    }
    
    @Test
    void testSha256WithByteArray() {
        // When
        byte[] hash = HashUtils.sha256(testData);
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(32); // SHA-256 produces 32 bytes
        assertThat(hash).isNotEqualTo(testData); // Hash should be different from input
    }
    
    @Test
    void testSha256WithString() {
        // When
        byte[] hash = HashUtils.sha256(testString);
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(32);
    }
    
    @Test
    void testSha256WithNullInput() {
        // Then
        assertThatThrownBy(() -> HashUtils.sha256((byte[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Input cannot be null");
        
        assertThatThrownBy(() -> HashUtils.sha256((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Input string cannot be null");
    }
    
    @Test
    void testDoubleSha256() {
        // When
        byte[] hash1 = HashUtils.sha256(testData);
        byte[] hash2 = HashUtils.doubleSha256(testData);
        
        // Then
        assertThat(hash2).isNotNull();
        assertThat(hash2).hasSize(32);
        assertThat(hash2).isNotEqualTo(hash1); // Double hash should be different from single hash
    }
    
    @Test
    void testSha256WithMultipleInputs() {
        // Given
        byte[] input1 = "Hello".getBytes();
        byte[] input2 = "World".getBytes();
        byte[] input3 = "!".getBytes();
        
        // When
        byte[] hash = HashUtils.sha256(input1, input2, input3);
        
        // Then
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(32);
    }
    
    @Test
    void testComputeVRFInput() {
        // Given
        byte[] previousBlockHash = new byte[32];
        long round = 12345L;
        long epoch = 1L;
        
        // When
        byte[] vrfInput = HashUtils.computeVRFInput(previousBlockHash, round, epoch);
        
        // Then
        assertThat(vrfInput).isNotNull();
        assertThat(vrfInput).hasSize(32);
    }
    
    @Test
    void testComputeVRFInputWithNullPreviousHash() {
        // Then
        assertThatThrownBy(() -> HashUtils.computeVRFInput(null, 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Previous block hash cannot be null");
    }
    
    @Test
    void testComputeMerkleRootWithEmptyTransactions() {
        // When
        byte[] merkleRoot = HashUtils.computeMerkleRoot(new byte[0][]);
        
        // Then
        assertThat(merkleRoot).isNotNull();
        assertThat(merkleRoot).hasSize(32);
        assertThat(merkleRoot).containsOnly(0); // Should be zero hash
    }
    
    @Test
    void testComputeMerkleRootWithSingleTransaction() {
        // Given
        byte[][] transactions = {testData};
        
        // When
        byte[] merkleRoot = HashUtils.computeMerkleRoot(transactions);
        
        // Then
        assertThat(merkleRoot).isNotNull();
        assertThat(merkleRoot).hasSize(testData.length); // Single transaction should return itself
        assertThat(merkleRoot).isEqualTo(testData); // Single transaction should return itself
    }
    
    @Test
    void testComputeMerkleRootWithMultipleTransactions() {
        // Given
        byte[][] transactions = {
            "Transaction1".getBytes(),
            "Transaction2".getBytes(),
            "Transaction3".getBytes()
        };
        
        // When
        byte[] merkleRoot = HashUtils.computeMerkleRoot(transactions);
        
        // Then
        assertThat(merkleRoot).isNotNull();
        assertThat(merkleRoot).hasSize(32);
    }
    
    @Test
    void testToHexString() {
        // Given
        byte[] data = {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF};
        
        // When
        String hex = HashUtils.toHexString(data);
        
        // Then
        assertThat(hex).isEqualTo("0123456789abcdef");
    }
    
    @Test
    void testToHexStringWithNull() {
        // When
        String hex = HashUtils.toHexString(null);
        
        // Then
        assertThat(hex).isEqualTo("null");
    }
    
    @Test
    void testFromHexString() {
        // Given
        String hex = "0123456789abcdef";
        
        // When
        byte[] data = HashUtils.fromHexString(hex);
        
        // Then
        assertThat(data).isEqualTo(new byte[]{0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF});
    }
    
    @Test
    void testFromHexStringWithInvalidInput() {
        // Then
        assertThatThrownBy(() -> HashUtils.fromHexString("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid hex string");
        
        assertThatThrownBy(() -> HashUtils.fromHexString("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid hex string");
        
        assertThatThrownBy(() -> HashUtils.fromHexString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid hex string");
    }
    
    @Test
    void testConsistency() {
        // Given
        byte[] data1 = "test".getBytes();
        byte[] data2 = "test".getBytes();
        
        // When
        byte[] hash1 = HashUtils.sha256(data1);
        byte[] hash2 = HashUtils.sha256(data2);
        
        // Then
        assertThat(hash1).isEqualTo(hash2); // Same input should produce same hash
    }
    
    @Test
    void testAvalancheEffect() {
        // Given
        byte[] data1 = "test1".getBytes();
        byte[] data2 = "test2".getBytes();
        
        // When
        byte[] hash1 = HashUtils.sha256(data1);
        byte[] hash2 = HashUtils.sha256(data2);
        
        // Then
        assertThat(hash1).isNotEqualTo(hash2); // Different input should produce different hash
        
        // Count different bits
        int differentBits = 0;
        for (int i = 0; i < hash1.length; i++) {
            int xor = hash1[i] ^ hash2[i];
            differentBits += Integer.bitCount(xor & 0xFF);
        }
        
        // Should have significant difference (avalanche effect)
        assertThat(differentBits).isGreaterThan(50); // At least 50 bits should be different
    }
}
