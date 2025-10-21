package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 链状态DTO测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class ChainStateDtoTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testDefaultConstructor() {
        // When
        ChainStateDto dto = new ChainStateDto();
        
        // Then
        assertNull(dto.getCurrentHeight());
        assertNull(dto.getCurrentRound());
        assertNull(dto.getTotalSupply());
        assertNull(dto.getCurrentDifficulty());
        assertNull(dto.getSuperNodeCount());
        assertNull(dto.getCoreNodeCount());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        ChainStateDto dto = new ChainStateDto();
        Long currentHeight = 100L;
        Long currentRound = 1L;
        Long totalSupply = 1000000L;
        String currentDifficulty = "1000000";
        Integer superNodeCount = 50;
        Integer coreNodeCount = 20;
        
        // When
        dto.setCurrentHeight(currentHeight);
        dto.setCurrentRound(currentRound);
        dto.setTotalSupply(totalSupply);
        dto.setCurrentDifficulty(currentDifficulty);
        dto.setSuperNodeCount(superNodeCount);
        dto.setCoreNodeCount(coreNodeCount);
        
        // Then
        assertEquals(currentHeight, dto.getCurrentHeight());
        assertEquals(currentRound, dto.getCurrentRound());
        assertEquals(totalSupply, dto.getTotalSupply());
        assertEquals(currentDifficulty, dto.getCurrentDifficulty());
        assertEquals(superNodeCount, dto.getSuperNodeCount());
        assertEquals(coreNodeCount, dto.getCoreNodeCount());
    }
    
    @Test
    void testJsonSerialization() throws Exception {
        // Given
        ChainStateDto dto = new ChainStateDto();
        dto.setCurrentHeight(100L);
        dto.setCurrentRound(1L);
        dto.setTotalSupply(1000000L);
        dto.setCurrentDifficulty("1000000");
        dto.setSuperNodeCount(50);
        dto.setCoreNodeCount(20);
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"currentHeight\":100"));
        assertTrue(json.contains("\"currentRound\":1"));
        assertTrue(json.contains("\"totalSupply\":1000000"));
        assertTrue(json.contains("\"currentDifficulty\":\"1000000\""));
        assertTrue(json.contains("\"superNodeCount\":50"));
        assertTrue(json.contains("\"coreNodeCount\":20"));
    }
    
    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"currentHeight\":100,\"currentRound\":1,\"totalSupply\":1000000," +
                     "\"currentDifficulty\":\"1000000\",\"superNodeCount\":50,\"coreNodeCount\":20}";
        
        // When
        ChainStateDto dto = objectMapper.readValue(json, ChainStateDto.class);
        
        // Then
        assertEquals(100L, dto.getCurrentHeight());
        assertEquals(1L, dto.getCurrentRound());
        assertEquals(1000000L, dto.getTotalSupply());
        assertEquals("1000000", dto.getCurrentDifficulty());
        assertEquals(50, dto.getSuperNodeCount());
        assertEquals(20, dto.getCoreNodeCount());
    }
    
    @Test
    void testToString() {
        // Given
        ChainStateDto dto = new ChainStateDto();
        dto.setCurrentHeight(100L);
        dto.setTotalSupply(1000000L);
        
        // When
        String result = dto.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("ChainStateDto"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        ChainStateDto dto1 = new ChainStateDto();
        dto1.setCurrentHeight(100L);
        dto1.setTotalSupply(1000000L);
        
        ChainStateDto dto2 = new ChainStateDto();
        dto2.setCurrentHeight(100L);
        dto2.setTotalSupply(1000000L);
        
        ChainStateDto dto3 = new ChainStateDto();
        dto3.setCurrentHeight(101L);
        dto3.setTotalSupply(1000000L);
        
        // When & Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testEqualsWithNull() {
        // Given
        ChainStateDto dto = new ChainStateDto();
        dto.setCurrentHeight(100L);
        
        // When & Then
        assertNotEquals(dto, null);
        assertNotEquals(dto, "not a dto");
    }
    
    @Test
    void testEqualsWithSameInstance() {
        // Given
        ChainStateDto dto = new ChainStateDto();
        dto.setCurrentHeight(100L);
        
        // When & Then
        assertEquals(dto, dto);
    }
}
