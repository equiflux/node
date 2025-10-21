package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 区块信息DTO测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class BlockInfoDtoTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testDefaultConstructor() {
        // When
        BlockInfoDto dto = new BlockInfoDto();
        
        // Then
        assertNull(dto.getHeight());
        assertNull(dto.getHash());
        assertNull(dto.getPreviousHash());
        assertNull(dto.getTimestamp());
        assertNull(dto.getRound());
        assertNull(dto.getProposer());
        assertNull(dto.getVrfOutput());
        assertNull(dto.getVrfProof());
        assertNull(dto.getMerkleRoot());
        assertNull(dto.getNonce());
        assertNull(dto.getDifficultyTarget());
        assertNull(dto.getTransactionCount());
        assertNull(dto.getAllVrfAnnouncements());
        assertNull(dto.getRewardedNodes());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        BlockInfoDto dto = new BlockInfoDto();
        Long height = 100L;
        String hash = "test-hash";
        String previousHash = "previous-hash";
        Long timestamp = System.currentTimeMillis();
        Long round = 1L;
        String proposer = "test-proposer";
        String vrfOutput = "test-vrf-output";
        String vrfProof = "test-vrf-proof";
        String merkleRoot = "test-merkle-root";
        Long nonce = 12345L;
        String difficultyTarget = "1000000";
        Integer transactionCount = 5;
        List<String> rewardedNodes = Arrays.asList("node1", "node2");
        
        // When
        dto.setHeight(height);
        dto.setHash(hash);
        dto.setPreviousHash(previousHash);
        dto.setTimestamp(timestamp);
        dto.setRound(round);
        dto.setProposer(proposer);
        dto.setVrfOutput(vrfOutput);
        dto.setVrfProof(vrfProof);
        dto.setMerkleRoot(merkleRoot);
        dto.setNonce(nonce);
        dto.setDifficultyTarget(difficultyTarget);
        dto.setTransactionCount(transactionCount);
        dto.setRewardedNodes(rewardedNodes);
        
        // Then
        assertEquals(height, dto.getHeight());
        assertEquals(hash, dto.getHash());
        assertEquals(previousHash, dto.getPreviousHash());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(round, dto.getRound());
        assertEquals(proposer, dto.getProposer());
        assertEquals(vrfOutput, dto.getVrfOutput());
        assertEquals(vrfProof, dto.getVrfProof());
        assertEquals(merkleRoot, dto.getMerkleRoot());
        assertEquals(nonce, dto.getNonce());
        assertEquals(difficultyTarget, dto.getDifficultyTarget());
        assertEquals(transactionCount, dto.getTransactionCount());
        assertEquals(rewardedNodes, dto.getRewardedNodes());
    }
    
    @Test
    void testJsonSerialization() throws Exception {
        // Given
        BlockInfoDto dto = new BlockInfoDto();
        dto.setHeight(100L);
        dto.setHash("test-hash");
        dto.setPreviousHash("previous-hash");
        dto.setTimestamp(System.currentTimeMillis());
        dto.setRound(1L);
        dto.setProposer("test-proposer");
        dto.setTransactionCount(5);
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"height\":100"));
        assertTrue(json.contains("\"hash\":\"test-hash\""));
        assertTrue(json.contains("\"previousHash\":\"previous-hash\""));
        assertTrue(json.contains("\"round\":1"));
        assertTrue(json.contains("\"proposer\":\"test-proposer\""));
        assertTrue(json.contains("\"transactionCount\":5"));
    }
    
    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"height\":100,\"hash\":\"test-hash\",\"previousHash\":\"previous-hash\"," +
                     "\"timestamp\":1640995200000,\"round\":1,\"proposer\":\"test-proposer\"," +
                     "\"transactionCount\":5}";
        
        // When
        BlockInfoDto dto = objectMapper.readValue(json, BlockInfoDto.class);
        
        // Then
        assertEquals(100L, dto.getHeight());
        assertEquals("test-hash", dto.getHash());
        assertEquals("previous-hash", dto.getPreviousHash());
        assertEquals(1640995200000L, dto.getTimestamp());
        assertEquals(1L, dto.getRound());
        assertEquals("test-proposer", dto.getProposer());
        assertEquals(5, dto.getTransactionCount());
    }
    
    @Test
    void testToString() {
        // Given
        BlockInfoDto dto = new BlockInfoDto();
        dto.setHeight(100L);
        dto.setHash("test-hash");
        
        // When
        String result = dto.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("BlockInfoDto"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        BlockInfoDto dto1 = new BlockInfoDto();
        dto1.setHeight(100L);
        dto1.setHash("test-hash");
        
        BlockInfoDto dto2 = new BlockInfoDto();
        dto2.setHeight(100L);
        dto2.setHash("test-hash");
        
        BlockInfoDto dto3 = new BlockInfoDto();
        dto3.setHeight(101L);
        dto3.setHash("test-hash");
        
        // When & Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testEqualsWithNull() {
        // Given
        BlockInfoDto dto = new BlockInfoDto();
        dto.setHeight(100L);
        
        // When & Then
        assertNotEquals(dto, null);
        assertNotEquals(dto, "not a dto");
    }
    
    @Test
    void testEqualsWithSameInstance() {
        // Given
        BlockInfoDto dto = new BlockInfoDto();
        dto.setHeight(100L);
        
        // When & Then
        assertEquals(dto, dto);
    }
}
