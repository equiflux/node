package io.equiflux.node.crypto;

import io.equiflux.node.model.VRFOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.*;

/**
 * VRFCalculator单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class VRFCalculatorTest {
    
    private VRFCalculator vrfCalculator;
    private VRFKeyPair vrfKeyPair;
    private VRFOutput vrfOutput;
    private PublicKey publicKey;
    
    @BeforeEach
    void setUp() {
        vrfCalculator = new VRFCalculator();
        vrfKeyPair = VRFKeyPair.generate();
        vrfOutput = vrfKeyPair.evaluate("test input".getBytes());
        publicKey = vrfKeyPair.getPublicKey();
    }
    
    @Test
    void testCalculateScore() {
        // Given
        double stakeWeight = 0.8;
        double decayFactor = 0.9;
        double performanceFactor = 0.95;
        
        // When
        double score = vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight, decayFactor, performanceFactor);
        
        // Then
        assertThat(score).isBetween(0.0, 1.0);
    }
    
    @Test
    void testCalculateScoreWithDefaultFactors() {
        // Given
        double stakeWeight = 0.8;
        
        // When
        double score = vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight);
        
        // Then
        assertThat(score).isBetween(0.0, 1.0);
    }
    
    @Test
    void testCalculateScoreWithInvalidStakeWeight() {
        // Then
        assertThatThrownBy(() -> vrfCalculator.calculateScore(publicKey, vrfOutput, -0.1, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stake weight must be between 0.0 and 1.0");
        
        assertThatThrownBy(() -> vrfCalculator.calculateScore(publicKey, vrfOutput, 1.1, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stake weight must be between 0.0 and 1.0");
    }
    
    @Test
    void testCalculateScoreWithInvalidDecayFactor() {
        // Then
        assertThatThrownBy(() -> vrfCalculator.calculateScore(publicKey, vrfOutput, 0.8, 0.4, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Decay factor must be between 0.5 and 1.0");
        
        assertThatThrownBy(() -> vrfCalculator.calculateScore(publicKey, vrfOutput, 0.8, 1.1, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Decay factor must be between 0.5 and 1.0");
    }
    
    @Test
    void testCalculateScoreWithInvalidPerformanceFactor() {
        // Then
        assertThatThrownBy(() -> vrfCalculator.calculateScore(publicKey, vrfOutput, 0.8, 1.0, 0.6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Performance factor must be between 0.7 and 1.0");
        
        assertThatThrownBy(() -> vrfCalculator.calculateScore(publicKey, vrfOutput, 0.8, 1.0, 1.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Performance factor must be between 0.7 and 1.0");
    }
    
    @Test
    void testCalculateScoreWithNullParameters() {
        // Then
        assertThatThrownBy(() -> vrfCalculator.calculateScore(null, vrfOutput, 0.8, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Public key cannot be null");
        
        assertThatThrownBy(() -> vrfCalculator.calculateScore(publicKey, null, 0.8, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF output cannot be null");
    }
    
    @Test
    void testCalculateStakeWeight() {
        // Given
        long stakeAmount = 100000L;
        long averageStake = 50000L;
        
        // When
        double stakeWeight = vrfCalculator.calculateStakeWeight(stakeAmount, averageStake);
        
        // Then
        assertThat(stakeWeight).isBetween(0.0, 1.0);
        assertThat(stakeWeight).isEqualTo(1.0); // Should be capped at 1.0
    }
    
    @Test
    void testCalculateStakeWeightWithSmallStake() {
        // Given
        long stakeAmount = 25000L;
        long averageStake = 50000L;
        
        // When
        double stakeWeight = vrfCalculator.calculateStakeWeight(stakeAmount, averageStake);
        
        // Then
        assertThat(stakeWeight).isBetween(0.0, 1.0);
        assertThat(stakeWeight).isEqualTo(0.5); // Should be 0.5
    }
    
    @Test
    void testCalculateStakeWeightWithInvalidParameters() {
        // Then
        assertThatThrownBy(() -> vrfCalculator.calculateStakeWeight(-1L, 50000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stake amount cannot be negative");
        
        assertThatThrownBy(() -> vrfCalculator.calculateStakeWeight(100000L, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Average stake must be positive");
    }
    
    @Test
    void testCalculateDecayFactor() {
        // Given
        int daysSinceElection = 30;
        
        // When
        double decayFactor = vrfCalculator.calculateDecayFactor(daysSinceElection);
        
        // Then
        assertThat(decayFactor).isBetween(0.5, 1.0);
    }
    
    @Test
    void testCalculateDecayFactorWithZeroDays() {
        // Given
        int daysSinceElection = 0;
        
        // When
        double decayFactor = vrfCalculator.calculateDecayFactor(daysSinceElection);
        
        // Then
        assertThat(decayFactor).isEqualTo(1.0); // Should be 1.0 for 0 days
    }
    
    @Test
    void testCalculateDecayFactorWithManyDays() {
        // Given
        int daysSinceElection = 200;
        
        // When
        double decayFactor = vrfCalculator.calculateDecayFactor(daysSinceElection);
        
        // Then
        assertThat(decayFactor).isEqualTo(0.5); // Should be capped at 0.5
    }
    
    @Test
    void testCalculateDecayFactorWithInvalidDays() {
        // Then
        assertThatThrownBy(() -> vrfCalculator.calculateDecayFactor(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Days since election cannot be negative");
    }
    
    @Test
    void testCalculatePerformanceFactor() {
        // Given
        double uptimePercentage = 99.5;
        
        // When
        double performanceFactor = vrfCalculator.calculatePerformanceFactor(uptimePercentage);
        
        // Then
        assertThat(performanceFactor).isBetween(0.7, 1.0);
        assertThat(performanceFactor).isEqualTo(1.0); // Should be 1.0 for 99%+
    }
    
    @Test
    void testCalculatePerformanceFactorWithDifferentUptimes() {
        // Test 95% uptime
        assertThat(vrfCalculator.calculatePerformanceFactor(95.0)).isEqualTo(0.95);
        
        // Test 90% uptime
        assertThat(vrfCalculator.calculatePerformanceFactor(90.0)).isEqualTo(0.85);
        
        // Test 85% uptime
        assertThat(vrfCalculator.calculatePerformanceFactor(85.0)).isEqualTo(0.7);
    }
    
    @Test
    void testCalculatePerformanceFactorWithInvalidUptime() {
        // Then
        assertThatThrownBy(() -> vrfCalculator.calculatePerformanceFactor(-1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Uptime percentage must be between 0.0 and 100.0");
        
        assertThatThrownBy(() -> vrfCalculator.calculatePerformanceFactor(101.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Uptime percentage must be between 0.0 and 100.0");
    }
    
    @Test
    void testVerifyVRF() {
        // Given
        byte[] input = "test input".getBytes();
        
        // When
        boolean isValid = vrfCalculator.verify(publicKey, input, vrfOutput, vrfOutput.getProof());
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void testVerifyVRFWithWrongInput() {
        // Given
        byte[] wrongInput = "wrong input".getBytes();
        
        // When
        boolean isValid = vrfCalculator.verify(publicKey, wrongInput, vrfOutput, vrfOutput.getProof());
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void testVerifyVRFWithNullParameters() {
        // Given
        byte[] input = "test input".getBytes();
        
        // Then
        assertThatThrownBy(() -> vrfCalculator.verify(null, input, vrfOutput, vrfOutput.getProof()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Public key cannot be null");
        
        assertThatThrownBy(() -> vrfCalculator.verify(publicKey, null, vrfOutput, vrfOutput.getProof()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF input cannot be null");
        
        assertThatThrownBy(() -> vrfCalculator.verify(publicKey, input, null, vrfOutput.getProof()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF output cannot be null");
        
        assertThatThrownBy(() -> vrfCalculator.verify(publicKey, input, vrfOutput, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF proof cannot be null");
    }
    
    @Test
    void testScoreCalculationConsistency() {
        // Given
        double stakeWeight = 0.8;
        double decayFactor = 0.9;
        double performanceFactor = 0.95;
        
        // When
        double score1 = vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight, decayFactor, performanceFactor);
        double score2 = vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight, decayFactor, performanceFactor);
        
        // Then
        assertThat(score1).isEqualTo(score2); // Same parameters should produce same score
    }
    
    @Test
    void testScoreCalculationWithDifferentStakeWeights() {
        // Given
        double decayFactor = 1.0;
        double performanceFactor = 1.0;
        
        // When
        double score1 = vrfCalculator.calculateScore(publicKey, vrfOutput, 0.5, decayFactor, performanceFactor);
        double score2 = vrfCalculator.calculateScore(publicKey, vrfOutput, 1.0, decayFactor, performanceFactor);
        
        // Then
        assertThat(score2).isGreaterThan(score1); // Higher stake weight should produce higher score
    }
    
    @Test
    void testScoreCalculationWithDifferentDecayFactors() {
        // Given
        double stakeWeight = 1.0;
        double performanceFactor = 1.0;
        
        // When
        double score1 = vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight, 0.5, performanceFactor);
        double score2 = vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight, 1.0, performanceFactor);
        
        // Then
        assertThat(score2).isGreaterThan(score1); // Higher decay factor should produce higher score
    }
    
    @Test
    void testScoreCalculationWithDifferentPerformanceFactors() {
        // Given
        double stakeWeight = 1.0;
        double decayFactor = 1.0;
        
        // When
        double score1 = vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight, decayFactor, 0.7);
        double score2 = vrfCalculator.calculateScore(publicKey, vrfOutput, stakeWeight, decayFactor, 1.0);
        
        // Then
        assertThat(score2).isGreaterThan(score1); // Higher performance factor should produce higher score
    }
}
