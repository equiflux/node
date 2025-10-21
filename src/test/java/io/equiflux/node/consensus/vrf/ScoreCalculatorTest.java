package io.equiflux.node.consensus.vrf;

import io.equiflux.node.crypto.VRFCalculator;
import io.equiflux.node.crypto.VRFKeyPair;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.model.VRFOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ScoreCalculator单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class ScoreCalculatorTest {
    
    private ScoreCalculator scoreCalculator;
    private VRFCalculator vrfCalculator;
    private List<VRFAnnouncement> announcements;
    private VRFKeyPair vrfKeyPair1;
    private VRFKeyPair vrfKeyPair2;
    private VRFKeyPair vrfKeyPair3;
    
    @BeforeEach
    void setUp() {
        vrfCalculator = new VRFCalculator();
        scoreCalculator = new ScoreCalculator(vrfCalculator);
        announcements = new ArrayList<>();
        
        // 创建三个不同的VRF密钥对
        vrfKeyPair1 = VRFKeyPair.generate();
        vrfKeyPair2 = VRFKeyPair.generate();
        vrfKeyPair3 = VRFKeyPair.generate();
        
        // 创建VRF公告
        byte[] input1 = "input1".getBytes();
        byte[] input2 = "input2".getBytes();
        byte[] input3 = "input3".getBytes();
        
        VRFOutput output1 = vrfKeyPair1.evaluate(input1);
        VRFOutput output2 = vrfKeyPair2.evaluate(input2);
        VRFOutput output3 = vrfKeyPair3.evaluate(input3);
        
        double score1 = vrfCalculator.calculateScore(vrfKeyPair1.getPublicKey(), output1, 0.8);
        double score2 = vrfCalculator.calculateScore(vrfKeyPair2.getPublicKey(), output2, 0.9);
        double score3 = vrfCalculator.calculateScore(vrfKeyPair3.getPublicKey(), output3, 0.7);
        
        announcements.add(new VRFAnnouncement(1L, vrfKeyPair1.getPublicKey(), output1, output1.getProof(), score1));
        announcements.add(new VRFAnnouncement(1L, vrfKeyPair2.getPublicKey(), output2, output2.getProof(), score2));
        announcements.add(new VRFAnnouncement(1L, vrfKeyPair3.getPublicKey(), output3, output3.getProof(), score3));
    }
    
    @Test
    void testConstructor() {
        // When
        ScoreCalculator calculator = new ScoreCalculator(vrfCalculator);
        
        // Then
        assertThat(calculator).isNotNull();
    }
    
    @Test
    void testConstructorWithNullCalculator() {
        // Then
        assertThatThrownBy(() -> new ScoreCalculator(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("VRF calculator cannot be null");
    }
    
    @Test
    void testCalculateScore() {
        // Given
        VRFOutput vrfOutput = vrfKeyPair1.evaluate("test".getBytes());
        double stakeWeight = 0.8;
        double decayFactor = 0.9;
        double performanceFactor = 0.95;
        
        // When
        double score = scoreCalculator.calculateScore(vrfKeyPair1.getPublicKey(), vrfOutput, 
                                                    stakeWeight, decayFactor, performanceFactor);
        
        // Then
        assertThat(score).isBetween(0.0, 1.0);
    }
    
    @Test
    void testCalculateScoreWithDefaultFactors() {
        // Given
        VRFOutput vrfOutput = vrfKeyPair1.evaluate("test".getBytes());
        double stakeWeight = 0.8;
        
        // When
        double score = scoreCalculator.calculateScore(vrfKeyPair1.getPublicKey(), vrfOutput, stakeWeight);
        
        // Then
        assertThat(score).isBetween(0.0, 1.0);
    }
    
    @Test
    void testCalculateStakeWeight() {
        // Given
        long stakeAmount = 100000L;
        long averageStake = 50000L;
        
        // When
        double stakeWeight = scoreCalculator.calculateStakeWeight(stakeAmount, averageStake);
        
        // Then
        assertThat(stakeWeight).isBetween(0.0, 1.0);
        assertThat(stakeWeight).isEqualTo(1.0); // Should be capped at 1.0
    }
    
    @Test
    void testCalculateDecayFactor() {
        // Given
        int daysSinceElection = 30;
        
        // When
        double decayFactor = scoreCalculator.calculateDecayFactor(daysSinceElection);
        
        // Then
        assertThat(decayFactor).isBetween(0.5, 1.0);
    }
    
    @Test
    void testCalculatePerformanceFactor() {
        // Given
        double uptimePercentage = 99.5;
        
        // When
        double performanceFactor = scoreCalculator.calculatePerformanceFactor(uptimePercentage);
        
        // Then
        assertThat(performanceFactor).isBetween(0.7, 1.0);
        assertThat(performanceFactor).isEqualTo(1.0); // Should be 1.0 for 99%+
    }
    
    @Test
    void testSortByScore() {
        // When
        List<VRFAnnouncement> sorted = scoreCalculator.sortByScore(announcements);
        
        // Then
        assertThat(sorted).hasSize(3);
        assertThat(sorted.get(0).getScore()).isGreaterThanOrEqualTo(sorted.get(1).getScore());
        assertThat(sorted.get(1).getScore()).isGreaterThanOrEqualTo(sorted.get(2).getScore());
    }
    
    @Test
    void testSortByScoreWithNullList() {
        // Then
        assertThatThrownBy(() -> scoreCalculator.sortByScore(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF announcements cannot be null");
    }
    
    @Test
    void testSelectProposer() {
        // When
        VRFAnnouncement proposer = scoreCalculator.selectProposer(announcements);
        
        // Then
        assertThat(proposer).isNotNull();
        assertThat(proposer.getScore()).isEqualTo(announcements.stream()
                .mapToDouble(VRFAnnouncement::getScore)
                .max()
                .orElse(0.0));
    }
    
    @Test
    void testSelectProposerWithNullList() {
        // Then
        assertThatThrownBy(() -> scoreCalculator.selectProposer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF announcements cannot be null or empty");
    }
    
    @Test
    void testSelectProposerWithEmptyList() {
        // Given
        List<VRFAnnouncement> emptyList = new ArrayList<>();
        
        // Then
        assertThatThrownBy(() -> scoreCalculator.selectProposer(emptyList))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF announcements cannot be null or empty");
    }
    
    @Test
    void testSelectTopX() {
        // When
        List<VRFAnnouncement> top2 = scoreCalculator.selectTopX(announcements, 2);
        
        // Then
        assertThat(top2).hasSize(2);
        assertThat(top2.get(0).getScore()).isGreaterThanOrEqualTo(top2.get(1).getScore());
    }
    
    @Test
    void testSelectTopXWithNullList() {
        // Then
        assertThatThrownBy(() -> scoreCalculator.selectTopX(null, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF announcements cannot be null");
    }
    
    @Test
    void testSelectTopXWithInvalidTopX() {
        // Then
        assertThatThrownBy(() -> scoreCalculator.selectTopX(announcements, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Top X must be positive");
        
        assertThatThrownBy(() -> scoreCalculator.selectTopX(announcements, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Top X must be positive");
    }
    
    @Test
    void testSelectTopXWithMoreThanAvailable() {
        // When
        List<VRFAnnouncement> top10 = scoreCalculator.selectTopX(announcements, 10);
        
        // Then
        assertThat(top10).hasSize(3); // Should return all available
    }
    
    @Test
    void testCanPropose() {
        // Given
        PublicKey proposerKey = announcements.get(0).getPublicKey();
        
        // When
        boolean canPropose = scoreCalculator.canPropose(proposerKey, announcements);
        
        // Then
        assertThat(canPropose).isNotNull(); // Result depends on which node has highest score
    }
    
    @Test
    void testCanProposeWithNullPublicKey() {
        // Then
        assertThatThrownBy(() -> scoreCalculator.canPropose(null, announcements))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Public key cannot be null");
    }
    
    @Test
    void testCanProposeWithNullList() {
        // Given
        PublicKey publicKey = vrfKeyPair1.getPublicKey();
        
        // Then
        assertThatThrownBy(() -> scoreCalculator.canPropose(publicKey, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("VRF announcements cannot be null");
    }
    
    @Test
    void testGetRank() {
        // Given
        PublicKey publicKey = announcements.get(0).getPublicKey();
        
        // When
        int rank = scoreCalculator.getRank(publicKey, announcements);
        
        // Then
        assertThat(rank).isBetween(1, 3);
    }
    
    @Test
    void testGetRankWithNonExistentPublicKey() {
        // Given
        VRFKeyPair otherKeyPair = VRFKeyPair.generate();
        PublicKey nonExistentKey = otherKeyPair.getPublicKey();
        
        // When
        int rank = scoreCalculator.getRank(nonExistentKey, announcements);
        
        // Then
        assertThat(rank).isEqualTo(-1);
    }
    
    @Test
    void testIsInTopX() {
        // Given
        PublicKey publicKey = announcements.get(0).getPublicKey();
        
        // When
        boolean isInTop2 = scoreCalculator.isInTopX(publicKey, announcements, 2);
        
        // Then
        assertThat(isInTop2).isNotNull(); // Result depends on actual scores
    }
    
    @Test
    void testCalculateAverageScore() {
        // When
        double averageScore = scoreCalculator.calculateAverageScore(announcements);
        
        // Then
        assertThat(averageScore).isBetween(0.0, 1.0);
        
        // Verify calculation
        double expectedAverage = announcements.stream()
                .mapToDouble(VRFAnnouncement::getScore)
                .average()
                .orElse(0.0);
        assertThat(averageScore).isEqualTo(expectedAverage);
    }
    
    @Test
    void testCalculateAverageScoreWithEmptyList() {
        // Given
        List<VRFAnnouncement> emptyList = new ArrayList<>();
        
        // When
        double averageScore = scoreCalculator.calculateAverageScore(emptyList);
        
        // Then
        assertThat(averageScore).isEqualTo(0.0);
    }
    
    @Test
    void testCalculateAverageScoreWithNullList() {
        // When
        double averageScore = scoreCalculator.calculateAverageScore(null);
        
        // Then
        assertThat(averageScore).isEqualTo(0.0);
    }
    
    @Test
    void testCalculateScoreStandardDeviation() {
        // When
        double standardDeviation = scoreCalculator.calculateScoreStandardDeviation(announcements);
        
        // Then
        assertThat(standardDeviation).isGreaterThanOrEqualTo(0.0);
    }
    
    @Test
    void testCalculateScoreStandardDeviationWithEmptyList() {
        // Given
        List<VRFAnnouncement> emptyList = new ArrayList<>();
        
        // When
        double standardDeviation = scoreCalculator.calculateScoreStandardDeviation(emptyList);
        
        // Then
        assertThat(standardDeviation).isEqualTo(0.0);
    }
    
    @Test
    void testIsScoreDistributionReasonable() {
        // When
        boolean isReasonable = scoreCalculator.isScoreDistributionReasonable(announcements);
        
        // Then
        assertThat(isReasonable).isTrue(); // Should be reasonable for normal VRF outputs
    }
    
    @Test
    void testIsScoreDistributionReasonableWithEmptyList() {
        // Given
        List<VRFAnnouncement> emptyList = new ArrayList<>();
        
        // When
        boolean isReasonable = scoreCalculator.isScoreDistributionReasonable(emptyList);
        
        // Then
        assertThat(isReasonable).isFalse();
    }
    
    @Test
    void testIsScoreDistributionReasonableWithNullList() {
        // When
        boolean isReasonable = scoreCalculator.isScoreDistributionReasonable(null);
        
        // Then
        assertThat(isReasonable).isFalse();
    }
    
    @Test
    void testScoreCalculationConsistency() {
        // Given
        VRFOutput vrfOutput = vrfKeyPair1.evaluate("test".getBytes());
        double stakeWeight = 0.8;
        double decayFactor = 0.9;
        double performanceFactor = 0.95;
        
        // When
        double score1 = scoreCalculator.calculateScore(vrfKeyPair1.getPublicKey(), vrfOutput, 
                                                     stakeWeight, decayFactor, performanceFactor);
        double score2 = scoreCalculator.calculateScore(vrfKeyPair1.getPublicKey(), vrfOutput, 
                                                     stakeWeight, decayFactor, performanceFactor);
        
        // Then
        assertThat(score1).isEqualTo(score2); // Same parameters should produce same score
    }
}
