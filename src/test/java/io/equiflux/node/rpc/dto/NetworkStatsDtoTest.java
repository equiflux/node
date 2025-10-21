package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网络统计DTO测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class NetworkStatsDtoTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testDefaultConstructor() {
        // When
        NetworkStatsDto dto = new NetworkStatsDto();
        
        // Then
        assertNull(dto.getTotalPeers());
        assertNull(dto.getConnectedPeers());
        assertNull(dto.getActivePeers());
        assertNull(dto.getNetworkVersion());
        assertNull(dto.getUptime());
        assertNull(dto.getBytesReceived());
        assertNull(dto.getBytesSent());
        assertNull(dto.getMessagesReceived());
        assertNull(dto.getMessagesSent());
    }
    
    @Test
    void testSettersAndGetters() {
        // Given
        NetworkStatsDto dto = new NetworkStatsDto();
        Integer totalPeers = 100;
        Integer connectedPeers = 50;
        Integer activePeers = 25;
        String networkVersion = "1.0.0";
        Long uptime = 3600000L;
        Long bytesReceived = 1000000L;
        Long bytesSent = 500000L;
        Long messagesReceived = 10000L;
        Long messagesSent = 8000L;
        
        // When
        dto.setTotalPeers(totalPeers);
        dto.setConnectedPeers(connectedPeers);
        dto.setActivePeers(activePeers);
        dto.setNetworkVersion(networkVersion);
        dto.setUptime(uptime);
        dto.setBytesReceived(bytesReceived);
        dto.setBytesSent(bytesSent);
        dto.setMessagesReceived(messagesReceived);
        dto.setMessagesSent(messagesSent);
        
        // Then
        assertEquals(totalPeers, dto.getTotalPeers());
        assertEquals(connectedPeers, dto.getConnectedPeers());
        assertEquals(activePeers, dto.getActivePeers());
        assertEquals(networkVersion, dto.getNetworkVersion());
        assertEquals(uptime, dto.getUptime());
        assertEquals(bytesReceived, dto.getBytesReceived());
        assertEquals(bytesSent, dto.getBytesSent());
        assertEquals(messagesReceived, dto.getMessagesReceived());
        assertEquals(messagesSent, dto.getMessagesSent());
    }
    
    @Test
    void testJsonSerialization() throws Exception {
        // Given
        NetworkStatsDto dto = new NetworkStatsDto();
        dto.setTotalPeers(100);
        dto.setConnectedPeers(50);
        dto.setActivePeers(25);
        dto.setNetworkVersion("1.0.0");
        dto.setUptime(3600000L);
        dto.setBytesReceived(1000000L);
        dto.setBytesSent(500000L);
        dto.setMessagesReceived(10000L);
        dto.setMessagesSent(8000L);
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"totalPeers\":100"));
        assertTrue(json.contains("\"connectedPeers\":50"));
        assertTrue(json.contains("\"activePeers\":25"));
        assertTrue(json.contains("\"networkVersion\":\"1.0.0\""));
        assertTrue(json.contains("\"uptime\":3600000"));
        assertTrue(json.contains("\"bytesReceived\":1000000"));
        assertTrue(json.contains("\"bytesSent\":500000"));
        assertTrue(json.contains("\"messagesReceived\":10000"));
        assertTrue(json.contains("\"messagesSent\":8000"));
    }
    
    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"totalPeers\":100,\"connectedPeers\":50,\"activePeers\":25," +
                     "\"networkVersion\":\"1.0.0\",\"uptime\":3600000,\"bytesReceived\":1000000," +
                     "\"bytesSent\":500000,\"messagesReceived\":10000,\"messagesSent\":8000}";
        
        // When
        NetworkStatsDto dto = objectMapper.readValue(json, NetworkStatsDto.class);
        
        // Then
        assertEquals(100, dto.getTotalPeers());
        assertEquals(50, dto.getConnectedPeers());
        assertEquals(25, dto.getActivePeers());
        assertEquals("1.0.0", dto.getNetworkVersion());
        assertEquals(3600000L, dto.getUptime());
        assertEquals(1000000L, dto.getBytesReceived());
        assertEquals(500000L, dto.getBytesSent());
        assertEquals(10000L, dto.getMessagesReceived());
        assertEquals(8000L, dto.getMessagesSent());
    }
    
    @Test
    void testToString() {
        // Given
        NetworkStatsDto dto = new NetworkStatsDto();
        dto.setTotalPeers(100);
        dto.setConnectedPeers(50);
        
        // When
        String result = dto.toString();
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("NetworkStatsDto"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Given
        NetworkStatsDto dto1 = new NetworkStatsDto();
        dto1.setTotalPeers(100);
        dto1.setConnectedPeers(50);
        
        NetworkStatsDto dto2 = new NetworkStatsDto();
        dto2.setTotalPeers(100);
        dto2.setConnectedPeers(50);
        
        NetworkStatsDto dto3 = new NetworkStatsDto();
        dto3.setTotalPeers(101);
        dto3.setConnectedPeers(50);
        
        // When & Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void testEqualsWithNull() {
        // Given
        NetworkStatsDto dto = new NetworkStatsDto();
        dto.setTotalPeers(100);
        
        // When & Then
        assertNotEquals(dto, null);
        assertNotEquals(dto, "not a dto");
    }
    
    @Test
    void testEqualsWithSameInstance() {
        // Given
        NetworkStatsDto dto = new NetworkStatsDto();
        dto.setTotalPeers(100);
        
        // When & Then
        assertEquals(dto, dto);
    }
}
