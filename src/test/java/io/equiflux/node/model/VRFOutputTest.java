package io.equiflux.node.model;

import io.equiflux.node.crypto.VRFKeyPair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.*;

/**
 * VRFOutput单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class VRFOutputTest {
    
    private VRFKeyPair vrfKeyPair;
    private VRFOutput vrfOutput;
    private byte[] testInput;
    
    @BeforeEach
    void setUp() {
        vrfKeyPair = VRFKeyPair.generate();
        testInput = "VRF test input".getBytes();
        vrfOutput = vrfKeyPair.evaluate(testInput);
    }
    
    @Test
    void testConstructor() {
        // Given
        byte[] output = new byte[32];
        for (int i = 0; i < 32; i++) {
            output[i] = (byte) (i % 256);
        }
        VRFProof proof = new VRFProof(new byte[64]);
        
        // When
        VRFOutput vrfOutput = new VRFOutput(output, proof);
        
        // Then
        assertThat(vrfOutput.getOutput()).isEqualTo(output);
        assertThat(vrfOutput.getProof()).isEqualTo(proof);
    }
    
    @Test
    void testConstructorWithNullOutput() {
        // Given
        VRFProof proof = new VRFProof(new byte[64]);
        
        // Then
        assertThatThrownBy(() -> new VRFOutput(null, proof))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("VRF output cannot be null");
    }
    
    @Test
    void testConstructorWithNullProof() {
        // Given
        byte[] output = new byte[32];
        
        // Then
        assertThatThrownBy(() -> new VRFOutput(output, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("VRF proof cannot be null");
    }
    
    @Test
    void testConstructorWithInvalidOutputSize() {
        // Given
        byte[] shortOutput = new byte[16];
        byte[] longOutput = new byte[64];
        VRFProof proof = new VRFProof(new byte[64]);
        
        // Then
        assertThatThrownBy(() -> new VRFOutput(shortOutput, proof))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF output must be 32 bytes");
        
        assertThatThrownBy(() -> new VRFOutput(longOutput, proof))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF output must be 32 bytes");
    }
    
    @Test
    void testGetOutputReturnsClone() {
        // When
        byte[] returnedOutput = vrfOutput.getOutput();
        
        // Then
        assertThat(returnedOutput).isEqualTo(vrfOutput.getOutput());
        assertThat(returnedOutput).isNotSameAs(vrfOutput.getOutput()); // Should be a clone
    }
    
    @Test
    void testToScore() {
        // When
        double score = vrfOutput.toScore();
        
        // Then
        assertThat(score).isBetween(0.0, 1.0);
    }
    
    @Test
    void testToScoreConsistency() {
        // Given
        VRFOutput output1 = vrfKeyPair.evaluate(testInput);
        VRFOutput output2 = vrfKeyPair.evaluate(testInput);
        
        // When
        double score1 = output1.toScore();
        double score2 = output2.toScore();
        
        // Then
        assertThat(score1).isEqualTo(score2); // Same input should produce same score
    }
    
    @Test
    void testToScoreWithDifferentInputs() {
        // Given
        byte[] input1 = "input1".getBytes();
        byte[] input2 = "input2".getBytes();
        VRFOutput output1 = vrfKeyPair.evaluate(input1);
        VRFOutput output2 = vrfKeyPair.evaluate(input2);
        
        // When
        double score1 = output1.toScore();
        double score2 = output2.toScore();
        
        // Then
        assertThat(score1).isNotEqualTo(score2); // Different inputs should produce different scores
    }
    
    @Test
    void testToScoreWithZeroOutput() {
        // Given
        byte[] zeroOutput = new byte[32]; // All zeros
        VRFProof proof = new VRFProof(new byte[64]);
        VRFOutput zeroVrfOutput = new VRFOutput(zeroOutput, proof);
        
        // When
        double score = zeroVrfOutput.toScore();
        
        // Then
        assertThat(score).isEqualTo(0.0);
    }
    
    @Test
    void testToScoreWithMaxOutput() {
        // Given
        byte[] maxOutput = new byte[32];
        for (int i = 0; i < 32; i++) {
            maxOutput[i] = (byte) 0xFF;
        }
        VRFProof proof = new VRFProof(new byte[64]);
        VRFOutput maxVrfOutput = new VRFOutput(maxOutput, proof);
        
        // When
        double score = maxVrfOutput.toScore();
        
        // Then
        assertThat(score).isBetween(0.0, 1.0);
        assertThat(score).isGreaterThan(0.0);
    }
    
    @Test
    void testToHexString() {
        // When
        String hexString = vrfOutput.toHexString();
        
        // Then
        assertThat(hexString).isNotNull();
        assertThat(hexString).hasSize(64); // 32 bytes * 2 hex chars per byte
        assertThat(hexString).matches("[0-9a-f]+");
    }
    
    @Test
    void testToHexStringWithSpecificBytes() {
        // Given
        byte[] specificOutput = new byte[32];
        specificOutput[0] = (byte) 0xAB;
        specificOutput[1] = (byte) 0xCD;
        specificOutput[2] = (byte) 0xEF;
        VRFProof proof = new VRFProof(new byte[64]);
        VRFOutput specificVrfOutput = new VRFOutput(specificOutput, proof);
        
        // When
        String hexString = specificVrfOutput.toHexString();
        
        // Then
        assertThat(hexString).startsWith("abcdef");
    }
    
    @Test
    void testEquality() {
        // Given
        VRFOutput output1 = vrfKeyPair.evaluate(testInput);
        VRFOutput output2 = vrfKeyPair.evaluate(testInput);
        
        // Then
        assertThat(output1).isEqualTo(output2);
        assertThat(output1.hashCode()).isEqualTo(output2.hashCode());
    }
    
    @Test
    void testInequality() {
        // Given
        byte[] input1 = "input1".getBytes();
        byte[] input2 = "input2".getBytes();
        VRFOutput output1 = vrfKeyPair.evaluate(input1);
        VRFOutput output2 = vrfKeyPair.evaluate(input2);
        
        // Then
        assertThat(output1).isNotEqualTo(output2);
    }
    
    @Test
    void testToString() {
        // When
        String toString = vrfOutput.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("VRFOutput");
        assertThat(toString).contains("output=");
        assertThat(toString).contains("proof=");
    }
    
    @Test
    void testSelfEquality() {
        // Then
        assertThat(vrfOutput).isEqualTo(vrfOutput);
    }
    
    @Test
    void testNullEquality() {
        // Then
        assertThat(vrfOutput).isNotEqualTo(null);
    }
    
    @Test
    void testDifferentClassEquality() {
        // Given
        String differentObject = "not a VRFOutput";
        
        // Then
        assertThat(vrfOutput).isNotEqualTo(differentObject);
    }
    
    @Test
    void testHashCodeConsistency() {
        // Given
        VRFOutput output1 = vrfKeyPair.evaluate(testInput);
        VRFOutput output2 = vrfKeyPair.evaluate(testInput);
        
        // Then
        assertThat(output1.hashCode()).isEqualTo(output2.hashCode());
    }
    
    @Test
    void testHashCodeDifferentForDifferentOutputs() {
        // Given
        byte[] input1 = "input1".getBytes();
        byte[] input2 = "input2".getBytes();
        VRFOutput output1 = vrfKeyPair.evaluate(input1);
        VRFOutput output2 = vrfKeyPair.evaluate(input2);
        
        // Then
        assertThat(output1.hashCode()).isNotEqualTo(output2.hashCode());
    }
    
    @Test
    void testScoreDistribution() {
        // Given
        int numTests = 100;
        double sum = 0.0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        // When
        for (int i = 0; i < numTests; i++) {
            byte[] input = ("test" + i).getBytes();
            VRFOutput output = vrfKeyPair.evaluate(input);
            double score = output.toScore();
            
            sum += score;
            min = Math.min(min, score);
            max = Math.max(max, score);
        }
        
        double average = sum / numTests;
        
        // Then
        assertThat(average).isBetween(0.0, 1.0);
        assertThat(min).isBetween(0.0, 1.0);
        assertThat(max).isBetween(0.0, 1.0);
        assertThat(max).isGreaterThan(min); // Should have some variation
    }
}
