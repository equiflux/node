package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 交易信息DTO测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class TransactionInfoDtoTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testDefaultConstructor() {
        // When
        TransactionInfoDto dto = new TransactionInfoDto();
        
        // Then
        assertNull(dto.getHash());
        assertNull(dto.getFrom());
        assertNull(dto.getTo());
        assertNull(dto.getAmount());
        assertNull(dto.getFee());
        assertNull(dto.getNonce());
        assertNull(dto.getTimestamp());
        assertNull(dto.getSignature());
        assertNull(dto.getData());
        assertNull(dto.getBlockHeight());
        assertNull(dto.getStatus());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        TransactionInfoDto dto = new TransactionInfoDto();
        String hash = "test-hash";
        String from = "test-from";
        String to = "test-to";
        Long amount = 1000L;
        Long fee = 10L;
        Long nonce = 1L;
        Long timestamp = System.currentTimeMillis();
        String signature = "test-signature";
        String data = "test-data";
        Long blockHeight = 100L;
        String status = "confirmed";
        
        // When
        dto.setHash(hash);
        dto.setFrom(from);
        dto.setTo(to);
        dto.setAmount(amount);
        dto.setFee(fee);
        dto.setNonce(nonce);
        dto.setTimestamp(timestamp);
        dto.setSignature(signature);
        dto.setData(data);
        dto.setBlockHeight(blockHeight);
        dto.setStatus(status);
        
        // Then
        assertEquals(hash, dto.getHash());
        assertEquals(from, dto.getFrom());
        assertEquals(to, dto.getTo());
        assertEquals(amount, dto.getAmount());
        assertEquals(fee, dto.getFee());
        assertEquals(nonce, dto.getNonce());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(signature, dto.getSignature());
        assertEquals(data, dto.getData());
        assertEquals(blockHeight, dto.getBlockHeight());
        assertEquals(status, dto.getStatus());
    }
    
    @Test
    void testJsonSerialization() throws Exception {
        // Given
        TransactionInfoDto dto = new TransactionInfoDto();
        dto.setHash("test-hash");
        dto.setFrom("test-from");
        dto.setTo("test-to");
        dto.setAmount(1000L);
        dto.setFee(10L);
        dto.setNonce(1L);
        dto.setTimestamp(System.currentTimeMillis());
        dto.setStatus("confirmed");
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"hash\":\"test-hash\""));
        assertTrue(json.contains("\"from\":\"test-from\""));
        assertTrue(json.contains("\"to\":\"test-to\""));
        assertTrue(json.contains("\"amount\":1000"));
        assertTrue(json.contains("\"fee\":10"));
        assertTrue(json.contains("\"nonce\":1"));
        assertTrue(json.contains("\"status\":\"confirmed\""));
    }
    
    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"hash\":\"test-hash\",\"from\":\"test-from\",\"to\":\"test-to\"," +
                     "\"amount\":1000,\"fee\":10,\"nonce\":1,\"timestamp\":1640995200000," +
                     "\"status\":\"confirmed\"}";
        
        // When
        TransactionInfoDto dto = objectMapper.readValue(json, TransactionInfoDto.class);
        
        // Then
        assertEquals("test-hash", dto.getHash());
        assertEquals("test-from", dto.getFrom());
        assertEquals("test-to", dto.getTo());
        assertEquals(1000L, dto.getAmount());
        assertEquals(10L, dto.getFee());
        assertEquals(1L, dto.getNonce());
        assertEquals(1640995200000L, dto.getTimestamp());
        assertEquals("confirmed", dto.getStatus());
    }
    
    @Test
    void testToString() {
        // Given
        TransactionInfoDto dto = new TransactionInfoDto();
        dto.setHash("test-hash");
        dto.setAmount(1000L);
        
        // When
        String result = dto.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("TransactionInfoDto"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        TransactionInfoDto dto1 = new TransactionInfoDto();
        dto1.setHash("test-hash");
        dto1.setAmount(1000L);
        
        TransactionInfoDto dto2 = new TransactionInfoDto();
        dto2.setHash("test-hash");
        dto2.setAmount(1000L);
        
        TransactionInfoDto dto3 = new TransactionInfoDto();
        dto3.setHash("other-hash");
        dto3.setAmount(1000L);
        
        // When & Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testEqualsWithNull() {
        // Given
        TransactionInfoDto dto = new TransactionInfoDto();
        dto.setHash("test-hash");
        
        // When & Then
        assertNotEquals(dto, null);
        assertNotEquals(dto, "not a dto");
    }
    
    @Test
    void testEqualsWithSameInstance() {
        // Given
        TransactionInfoDto dto = new TransactionInfoDto();
        dto.setHash("test-hash");
        
        // When & Then
        assertEquals(dto, dto);
    }
}
