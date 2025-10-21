package io.equiflux.node.model;

import io.equiflux.node.crypto.VRFKeyPair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.*;

/**
 * VRFAnnouncement单元测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class VRFAnnouncementTest {
    
    private VRFKeyPair vrfKeyPair;
    private VRFOutput vrfOutput;
    private VRFProof vrfProof;
    private PublicKey publicKey;
    private long round;
    private double score;
    
    @BeforeEach
    void setUp() {
        vrfKeyPair = VRFKeyPair.generate();
        vrfOutput = vrfKeyPair.evaluate("test input".getBytes());
        vrfProof = vrfOutput.getProof();
        publicKey = vrfKeyPair.getPublicKey();
        round = 1L;
        score = 0.8;
    }
    
    @Test
    void testConstructor() {
        // When
        VRFAnnouncement announcement = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score);
        
        // Then
        assertThat(announcement.getRound()).isEqualTo(round);
        assertThat(announcement.getPublicKey()).isEqualTo(publicKey);
        assertThat(announcement.getVrfOutput()).isEqualTo(vrfOutput);
        assertThat(announcement.getVrfProof()).isEqualTo(vrfProof);
        assertThat(announcement.getScore()).isEqualTo(score);
        assertThat(announcement.getTimestamp()).isGreaterThan(0);
    }
    
    @Test
    void testConstructorWithCustomTimestamp() {
        // Given
        long customTimestamp = 1234567890L;
        
        // When
        VRFAnnouncement announcement = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score, customTimestamp);
        
        // Then
        assertThat(announcement.getTimestamp()).isEqualTo(customTimestamp);
    }
    
    @Test
    void testConstructorWithNullPublicKey() {
        // Then
        assertThatThrownBy(() -> new VRFAnnouncement(round, null, vrfOutput, vrfProof, score))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Public key cannot be null");
    }
    
    @Test
    void testConstructorWithNullVrfOutput() {
        // Then
        assertThatThrownBy(() -> new VRFAnnouncement(round, publicKey, null, vrfProof, score))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("VRF output cannot be null");
    }
    
    @Test
    void testConstructorWithNullVrfProof() {
        // Then
        assertThatThrownBy(() -> new VRFAnnouncement(round, publicKey, vrfOutput, null, score))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("VRF proof cannot be null");
    }
    
    @Test
    void testConstructorWithInvalidScore() {
        // Then
        assertThatThrownBy(() -> new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, -0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Score must be between 0.0 and 1.0");
        
        assertThatThrownBy(() -> new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, 1.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Score must be between 0.0 and 1.0");
    }
    
    @Test
    void testGetPublicKeyHex() {
        // Given
        VRFAnnouncement announcement = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score);
        
        // When
        String publicKeyHex = announcement.getPublicKeyHex();
        
        // Then
        assertThat(publicKeyHex).isNotNull();
        assertThat(publicKeyHex).isNotEmpty();
        assertThat(publicKeyHex).matches("[0-9a-f]+");
    }
    
    @Test
    void testIsExpired() {
        // Given
        VRFAnnouncement announcement = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score);
        
        // When & Then
        assertThat(announcement.isExpired(1000)).isFalse(); // Not expired yet
        
        // Create announcement with old timestamp
        long oldTimestamp = System.currentTimeMillis() - 2000;
        VRFAnnouncement oldAnnouncement = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score, oldTimestamp);
        assertThat(oldAnnouncement.isExpired(1000)).isTrue(); // Expired
    }
    
    @Test
    void testEquality() {
        // Given
        VRFAnnouncement announcement1 = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score);
        VRFAnnouncement announcement2 = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score);
        
        // Then
        assertThat(announcement1).isEqualTo(announcement2);
        assertThat(announcement1.hashCode()).isEqualTo(announcement2.hashCode());
    }
    
    @Test
    void testInequality() {
        // Given
        VRFAnnouncement announcement1 = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score);
        VRFAnnouncement announcement2 = new VRFAnnouncement(round + 1, publicKey, vrfOutput, vrfProof, score);
        
        // Then
        assertThat(announcement1).isNotEqualTo(announcement2);
    }
    
    @Test
    void testToString() {
        // Given
        VRFAnnouncement announcement = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score);
        
        // When
        String toString = announcement.toString();
        
        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("VRFAnnouncement");
        assertThat(toString).contains("round=" + round);
        assertThat(toString).contains("score=" + score);
    }
    
    @Test
    void testDifferentRounds() {
        // Given
        VRFAnnouncement announcement1 = new VRFAnnouncement(1L, publicKey, vrfOutput, vrfProof, score);
        VRFAnnouncement announcement2 = new VRFAnnouncement(2L, publicKey, vrfOutput, vrfProof, score);
        
        // Then
        assertThat(announcement1.getRound()).isEqualTo(1L);
        assertThat(announcement2.getRound()).isEqualTo(2L);
        assertThat(announcement1).isNotEqualTo(announcement2);
    }
    
    @Test
    void testDifferentScores() {
        // Given
        VRFAnnouncement announcement1 = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, 0.5);
        VRFAnnouncement announcement2 = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, 0.8);
        
        // Then
        assertThat(announcement1.getScore()).isEqualTo(0.5);
        assertThat(announcement2.getScore()).isEqualTo(0.8);
        assertThat(announcement1).isNotEqualTo(announcement2);
    }
    
    @Test
    void testBoundaryScores() {
        // Test minimum score
        VRFAnnouncement minScore = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, 0.0);
        assertThat(minScore.getScore()).isEqualTo(0.0);
        
        // Test maximum score
        VRFAnnouncement maxScore = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, 1.0);
        assertThat(maxScore.getScore()).isEqualTo(1.0);
    }
    
    @Test
    void testTimestampConsistency() {
        // Given
        long beforeCreation = System.currentTimeMillis();
        VRFAnnouncement announcement = new VRFAnnouncement(round, publicKey, vrfOutput, vrfProof, score);
        long afterCreation = System.currentTimeMillis();
        
        // Then
        assertThat(announcement.getTimestamp()).isBetween(beforeCreation, afterCreation);
    }
}
