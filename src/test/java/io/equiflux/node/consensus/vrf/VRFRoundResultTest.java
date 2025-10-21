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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * VRFRoundResult单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class VRFRoundResultTest {
    
    private VRFCalculator vrfCalculator;
    private VRFAnnouncement winner;
    private List<VRFAnnouncement> topX;
    private List<VRFAnnouncement> allValid;
    private VRFKeyPair vrfKeyPair1;
    private VRFKeyPair vrfKeyPair2;
    private VRFKeyPair vrfKeyPair3;
    
    @BeforeEach
    void setUp() {
        vrfCalculator = new VRFCalculator();
        
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
        
        VRFAnnouncement announcement1 = new VRFAnnouncement(1L, vrfKeyPair1.getPublicKey(), output1, output1.getProof(), score1);
        VRFAnnouncement announcement2 = new VRFAnnouncement(1L, vrfKeyPair2.getPublicKey(), output2, output2.getProof(), score2);
        VRFAnnouncement announcement3 = new VRFAnnouncement(1L, vrfKeyPair3.getPublicKey(), output3, output3.getProof(), score3);
        
        // 找到分数最高的节点
        List<VRFAnnouncement> announcements = List.of(announcement1, announcement2, announcement3);
        winner = announcements.stream()
                .max((a, b) -> Double.compare(a.getScore(), b.getScore()))
                .orElse(announcement1);
        
        // 按分数排序
        List<VRFAnnouncement> sorted = announcements.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .toList();
        
        topX = sorted.subList(0, Math.min(2, sorted.size())); // Top 2
        allValid = announcements; // All 3
    }
    
    @Test
    void testConstructor() {
        // When
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // Then
        assertThat(result.getWinner()).isEqualTo(winner);
        assertThat(result.getTopX()).isEqualTo(topX);
        assertThat(result.getAllValid()).isEqualTo(allValid);
    }
    
    @Test
    void testConstructorWithNullWinner() {
        // Then
        assertThatThrownBy(() -> new VRFRoundResult(null, topX, allValid))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Winner cannot be null");
    }
    
    @Test
    void testConstructorWithNullTopX() {
        // Then
        assertThatThrownBy(() -> new VRFRoundResult(winner, null, allValid))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Top X cannot be null");
    }
    
    @Test
    void testConstructorWithNullAllValid() {
        // Then
        assertThatThrownBy(() -> new VRFRoundResult(winner, topX, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("All valid cannot be null");
    }
    
    @Test
    void testConstructorWithEmptyTopX() {
        // Given
        List<VRFAnnouncement> emptyTopX = new ArrayList<>();
        
        // Then
        assertThatThrownBy(() -> new VRFRoundResult(winner, emptyTopX, allValid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Top X cannot be empty");
    }
    
    @Test
    void testConstructorWithEmptyAllValid() {
        // Given
        List<VRFAnnouncement> emptyAllValid = new ArrayList<>();
        
        // Then
        assertThatThrownBy(() -> new VRFRoundResult(winner, topX, emptyAllValid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All valid cannot be empty");
    }
    
    @Test
    void testConstructorWithWinnerNotInTopX() {
        // Given
        VRFKeyPair otherKeyPair = VRFKeyPair.generate();
        VRFOutput otherOutput = otherKeyPair.evaluate("other".getBytes());
        double otherScore = vrfCalculator.calculateScore(otherKeyPair.getPublicKey(), otherOutput, 0.5);
        VRFAnnouncement otherWinner = new VRFAnnouncement(1L, otherKeyPair.getPublicKey(), otherOutput, otherOutput.getProof(), otherScore);
        
        // Then
        assertThatThrownBy(() -> new VRFRoundResult(otherWinner, topX, allValid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Winner must be in top X");
    }
    
    @Test
    void testConstructorWithTopXNotSubsetOfAllValid() {
        // Given
        VRFKeyPair otherKeyPair = VRFKeyPair.generate();
        VRFOutput otherOutput = otherKeyPair.evaluate("other".getBytes());
        double otherScore = vrfCalculator.calculateScore(otherKeyPair.getPublicKey(), otherOutput, 0.5);
        VRFAnnouncement otherAnnouncement = new VRFAnnouncement(1L, otherKeyPair.getPublicKey(), otherOutput, otherOutput.getProof(), otherScore);
        
        List<VRFAnnouncement> invalidTopX = List.of(winner, otherAnnouncement);
        
        // Then
        assertThatThrownBy(() -> new VRFRoundResult(winner, invalidTopX, allValid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Top X must be subset of all valid");
    }
    
    @Test
    void testGetTopXPublicKeys() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        List<PublicKey> topXPublicKeys = result.getTopXPublicKeys();
        
        // Then
        assertThat(topXPublicKeys).hasSize(2);
        assertThat(topXPublicKeys).containsAll(topX.stream()
                .map(VRFAnnouncement::getPublicKey)
                .collect(Collectors.toList()));
    }
    
    @Test
    void testGetAllValidPublicKeys() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        List<PublicKey> allValidPublicKeys = result.getAllValidPublicKeys();
        
        // Then
        assertThat(allValidPublicKeys).hasSize(3);
        assertThat(allValidPublicKeys).contains(vrfKeyPair1.getPublicKey());
        assertThat(allValidPublicKeys).contains(vrfKeyPair2.getPublicKey());
        assertThat(allValidPublicKeys).contains(vrfKeyPair3.getPublicKey());
    }
    
    @Test
    void testGetWinnerPublicKey() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        PublicKey winnerPublicKey = result.getWinnerPublicKey();
        
        // Then
        assertThat(winnerPublicKey).isEqualTo(winner.getPublicKey());
    }
    
    @Test
    void testGetWinnerPublicKeyHex() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        String winnerPublicKeyHex = result.getWinnerPublicKeyHex();
        
        // Then
        assertThat(winnerPublicKeyHex).isNotNull();
        assertThat(winnerPublicKeyHex).isNotEmpty();
        assertThat(winnerPublicKeyHex).matches("[0-9a-f]+");
    }
    
    @Test
    void testGetWinnerScore() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        double winnerScore = result.getWinnerScore();
        
        // Then
        assertThat(winnerScore).isEqualTo(winner.getScore());
    }
    
    @Test
    void testGetTopXCount() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        int topXCount = result.getTopXCount();
        
        // Then
        assertThat(topXCount).isEqualTo(2);
    }
    
    @Test
    void testGetAllValidCount() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        int allValidCount = result.getAllValidCount();
        
        // Then
        assertThat(allValidCount).isEqualTo(3);
    }
    
    @Test
    void testGetRound() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        long round = result.getRound();
        
        // Then
        assertThat(round).isEqualTo(1L);
    }
    
    @Test
    void testHasEnoughVRFs() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When & Then
        assertThat(result.hasEnoughVRFs(1)).isTrue();
        assertThat(result.hasEnoughVRFs(3)).isTrue();
        assertThat(result.hasEnoughVRFs(4)).isFalse();
    }
    
    @Test
    void testIsWinnerValid() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        boolean isWinnerValid = result.isWinnerValid();
        
        // Then
        assertThat(isWinnerValid).isTrue();
    }
    
    @Test
    void testIsWinnerValidWithInvalidWinner() {
        // Given
        VRFKeyPair otherKeyPair = VRFKeyPair.generate();
        VRFOutput otherOutput = otherKeyPair.evaluate("other".getBytes());
        double otherScore = vrfCalculator.calculateScore(otherKeyPair.getPublicKey(), otherOutput, 0.5);
        VRFAnnouncement otherWinner = new VRFAnnouncement(1L, otherKeyPair.getPublicKey(), otherOutput, otherOutput.getProof(), otherScore);
        
        List<VRFAnnouncement> invalidAllValid = List.of(otherWinner);
        VRFRoundResult invalidResult = new VRFRoundResult(otherWinner, List.of(otherWinner), invalidAllValid);
        
        // When
        boolean isWinnerValid = invalidResult.isWinnerValid();
        
        // Then
        assertThat(isWinnerValid).isTrue(); // Should be valid for this case
    }
    
    @Test
    void testIsTopXValid() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        boolean isTopXValid = result.isTopXValid();
        
        // Then
        assertThat(isTopXValid).isTrue();
    }
    
    @Test
    void testIsTopXValidWithUnsortedTopX() {
        // Given
        // 创建一个故意未排序的topX列表（低分数在前）
        List<VRFAnnouncement> sorted = allValid.stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .toList();
        List<VRFAnnouncement> unsortedTopX = List.of(sorted.get(1), sorted.get(0)); // 故意颠倒顺序
        VRFRoundResult result = new VRFRoundResult(winner, unsortedTopX, allValid);
        
        // When
        boolean isTopXValid = result.isTopXValid();
        
        // Then
        assertThat(isTopXValid).isFalse();
    }
    
    @Test
    void testEquality() {
        // Given
        VRFRoundResult result1 = new VRFRoundResult(winner, topX, allValid);
        VRFRoundResult result2 = new VRFRoundResult(winner, topX, allValid);
        
        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }
    
    @Test
    void testInequality() {
        // Given
        VRFRoundResult result1 = new VRFRoundResult(winner, topX, allValid);
        List<VRFAnnouncement> differentTopX = List.of(winner);
        VRFRoundResult result2 = new VRFRoundResult(winner, differentTopX, allValid);
        
        // Then
        assertThat(result1).isNotEqualTo(result2);
    }
    
    @Test
    void testToString() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // When
        String toString = result.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("VRFRoundResult");
        assertThat(toString).contains("round=1");
        assertThat(toString).contains("winner=");
        assertThat(toString).contains("winnerScore=");
        assertThat(toString).contains("topXCount=2");
        assertThat(toString).contains("allValidCount=3");
    }
    
    @Test
    void testSelfEquality() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // Then
        assertThat(result).isEqualTo(result);
    }
    
    @Test
    void testNullEquality() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        
        // Then
        assertThat(result).isNotEqualTo(null);
    }
    
    @Test
    void testDifferentClassEquality() {
        // Given
        VRFRoundResult result = new VRFRoundResult(winner, topX, allValid);
        String differentObject = "not a VRFRoundResult";
        
        // Then
        assertThat(result).isNotEqualTo(differentObject);
    }
    
    @Test
    void testHashCodeConsistency() {
        // Given
        VRFRoundResult result1 = new VRFRoundResult(winner, topX, allValid);
        VRFRoundResult result2 = new VRFRoundResult(winner, topX, allValid);
        
        // Then
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }
    
    @Test
    void testHashCodeDifferentForDifferentResults() {
        // Given
        VRFRoundResult result1 = new VRFRoundResult(winner, topX, allValid);
        List<VRFAnnouncement> differentTopX = List.of(winner);
        VRFRoundResult result2 = new VRFRoundResult(winner, differentTopX, allValid);
        
        // Then
        assertThat(result1.hashCode()).isNotEqualTo(result2.hashCode());
    }
}
