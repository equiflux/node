package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账户信息DTO测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class AccountInfoDtoTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testDefaultConstructor() {
        // When
        AccountInfoDto dto = new AccountInfoDto();
        
        // Then
        assertNull(dto.getPublicKey());
        assertNull(dto.getBalance());
        assertNull(dto.getStakeAmount());
        assertNull(dto.getNonce());
        assertNull(dto.getIsSuperNode());
        assertNull(dto.getLastUpdated());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        AccountInfoDto dto = new AccountInfoDto();
        String publicKey = "test-public-key";
        Long balance = 1000L;
        Long stakeAmount = 500L;
        Long nonce = 1L;
        Boolean isSuperNode = false;
        Long lastUpdated = System.currentTimeMillis();
        
        // When
        dto.setPublicKey(publicKey);
        dto.setBalance(balance);
        dto.setStakeAmount(stakeAmount);
        dto.setNonce(nonce);
        dto.setIsSuperNode(isSuperNode);
        dto.setLastUpdated(lastUpdated);
        
        // Then
        assertEquals(publicKey, dto.getPublicKey());
        assertEquals(balance, dto.getBalance());
        assertEquals(stakeAmount, dto.getStakeAmount());
        assertEquals(nonce, dto.getNonce());
        assertEquals(isSuperNode, dto.getIsSuperNode());
        assertEquals(lastUpdated, dto.getLastUpdated());
    }
    
    @Test
    void testJsonSerialization() throws Exception {
        // Given
        AccountInfoDto dto = new AccountInfoDto();
        dto.setPublicKey("test-public-key");
        dto.setBalance(1000L);
        dto.setStakeAmount(500L);
        dto.setNonce(1L);
        dto.setIsSuperNode(false);
        dto.setLastUpdated(System.currentTimeMillis());
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"publicKey\":\"test-public-key\""));
        assertTrue(json.contains("\"balance\":1000"));
        assertTrue(json.contains("\"stakeAmount\":500"));
        assertTrue(json.contains("\"nonce\":1"));
        assertTrue(json.contains("\"isSuperNode\":false"));
    }
    
    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"publicKey\":\"test-public-key\",\"balance\":1000,\"stakeAmount\":500," +
                     "\"nonce\":1,\"isSuperNode\":false,\"lastUpdated\":1640995200000}";
        
        // When
        AccountInfoDto dto = objectMapper.readValue(json, AccountInfoDto.class);
        
        // Then
        assertEquals("test-public-key", dto.getPublicKey());
        assertEquals(1000L, dto.getBalance());
        assertEquals(500L, dto.getStakeAmount());
        assertEquals(1L, dto.getNonce());
        assertEquals(false, dto.getIsSuperNode());
        assertEquals(1640995200000L, dto.getLastUpdated());
    }
    
    @Test
    void testToString() {
        // Given
        AccountInfoDto dto = new AccountInfoDto();
        dto.setPublicKey("test-public-key");
        dto.setBalance(1000L);
        
        // When
        String result = dto.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("AccountInfoDto"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        AccountInfoDto dto1 = new AccountInfoDto();
        dto1.setPublicKey("test-public-key");
        dto1.setBalance(1000L);
        
        AccountInfoDto dto2 = new AccountInfoDto();
        dto2.setPublicKey("test-public-key");
        dto2.setBalance(1000L);
        
        AccountInfoDto dto3 = new AccountInfoDto();
        dto3.setPublicKey("other-public-key");
        dto3.setBalance(1000L);
        
        // When & Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testEqualsWithNull() {
        // Given
        AccountInfoDto dto = new AccountInfoDto();
        dto.setPublicKey("test-public-key");
        
        // When & Then
        assertNotEquals(dto, null);
        assertNotEquals(dto, "not a dto");
    }
    
    @Test
    void testEqualsWithSameInstance() {
        // Given
        AccountInfoDto dto = new AccountInfoDto();
        dto.setPublicKey("test-public-key");
        
        // When & Then
        assertEquals(dto, dto);
    }
}
