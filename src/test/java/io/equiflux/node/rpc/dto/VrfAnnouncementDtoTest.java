package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VRF公告DTO测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class VrfAnnouncementDtoTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testDefaultConstructor() {
        // When
        VrfAnnouncementDto dto = new VrfAnnouncementDto();
        
        // Then
        assertNull(dto.getPublicKey());
        assertNull(dto.getVrfOutput());
        assertNull(dto.getVrfProof());
        assertNull(dto.getTimestamp());
        assertNull(dto.getRound());
        assertNull(dto.getScore());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        VrfAnnouncementDto dto = new VrfAnnouncementDto();
        String publicKey = "test-public-key";
        String vrfOutput = "test-vrf-output";
        String vrfProof = "test-vrf-proof";
        Long timestamp = System.currentTimeMillis();
        Long round = 1L;
        Double score = 0.85;
        
        // When
        dto.setPublicKey(publicKey);
        dto.setVrfOutput(vrfOutput);
        dto.setVrfProof(vrfProof);
        dto.setTimestamp(timestamp);
        dto.setRound(round);
        dto.setScore(score);
        
        // Then
        assertEquals(publicKey, dto.getPublicKey());
        assertEquals(vrfOutput, dto.getVrfOutput());
        assertEquals(vrfProof, dto.getVrfProof());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(round, dto.getRound());
        assertEquals(score, dto.getScore());
    }
    
    @Test
    void testJsonSerialization() throws Exception {
        // Given
        VrfAnnouncementDto dto = new VrfAnnouncementDto();
        dto.setPublicKey("test-public-key");
        dto.setVrfOutput("test-vrf-output");
        dto.setVrfProof("test-vrf-proof");
        dto.setTimestamp(System.currentTimeMillis());
        dto.setRound(1L);
        dto.setScore(0.85);
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"publicKey\":\"test-public-key\""));
        assertTrue(json.contains("\"vrfOutput\":\"test-vrf-output\""));
        assertTrue(json.contains("\"vrfProof\":\"test-vrf-proof\""));
        assertTrue(json.contains("\"round\":1"));
        assertTrue(json.contains("\"score\":0.85"));
    }
    
    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"publicKey\":\"test-public-key\",\"vrfOutput\":\"test-vrf-output\"," +
                     "\"vrfProof\":\"test-vrf-proof\",\"timestamp\":1640995200000,\"round\":1,\"score\":0.85}";
        
        // When
        VrfAnnouncementDto dto = objectMapper.readValue(json, VrfAnnouncementDto.class);
        
        // Then
        assertEquals("test-public-key", dto.getPublicKey());
        assertEquals("test-vrf-output", dto.getVrfOutput());
        assertEquals("test-vrf-proof", dto.getVrfProof());
        assertEquals(1640995200000L, dto.getTimestamp());
        assertEquals(1L, dto.getRound());
        assertEquals(0.85, dto.getScore());
    }
    
    @Test
    void testToString() {
        // Given
        VrfAnnouncementDto dto = new VrfAnnouncementDto();
        dto.setPublicKey("test-public-key");
        dto.setScore(0.85);
        
        // When
        String result = dto.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("VrfAnnouncementDto"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        VrfAnnouncementDto dto1 = new VrfAnnouncementDto();
        dto1.setPublicKey("test-public-key");
        dto1.setScore(0.85);
        
        VrfAnnouncementDto dto2 = new VrfAnnouncementDto();
        dto2.setPublicKey("test-public-key");
        dto2.setScore(0.85);
        
        VrfAnnouncementDto dto3 = new VrfAnnouncementDto();
        dto3.setPublicKey("other-public-key");
        dto3.setScore(0.85);
        
        // When & Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testEqualsWithNull() {
        // Given
        VrfAnnouncementDto dto = new VrfAnnouncementDto();
        dto.setPublicKey("test-public-key");
        
        // When & Then
        assertNotEquals(dto, null);
        assertNotEquals(dto, "not a dto");
    }
    
    @Test
    void testEqualsWithSameInstance() {
        // Given
        VrfAnnouncementDto dto = new VrfAnnouncementDto();
        dto.setPublicKey("test-public-key");
        
        // When & Then
        assertEquals(dto, dto);
    }
}
