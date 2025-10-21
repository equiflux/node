package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RPC请求对象测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class RpcRequestTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testConstructor() {
        // Given
        String method = "getCurrentHeight";
        Map<String, Object> params = new HashMap<>();
        params.put("test", "value");
        Object id = 1;
        
        // When
        RpcRequest request = new RpcRequest(method, params, id);
        
        // Then
        assertEquals("2.0", request.getJsonrpc());
        assertEquals(method, request.getMethod());
        assertEquals(params, request.getParams());
        assertEquals(id, request.getId());
    }
    
    @Test
    void testGetParamsAsMap_WithMap() {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("height", 100L);
        params.put("hash", "test-hash");
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 1);
        
        // When
        Map<String, Object> result = request.getParamsAsMap();
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.get("height"));
        assertEquals("test-hash", result.get("hash"));
    }
    
    @Test
    void testGetParamsAsMap_WithNull() {
        // Given
        RpcRequest request = new RpcRequest("getCurrentHeight", null, 1);
        
        // When
        Map<String, Object> result = request.getParamsAsMap();
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetParamsAsMap_WithArray() {
        // Given
        Object[] params = {"param1", "param2"};
        RpcRequest request = new RpcRequest("testMethod", params, 1);
        
        // When
        Map<String, Object> result = request.getParamsAsMap();
        
        // Then
        assertNull(result); // 数组参数无法转换为Map
    }
    
    @Test
    void testJsonSerialization() throws Exception {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("height", 100L);
        RpcRequest request = new RpcRequest("getBlockByHeight", params, 1);
        
        // When
        String json = objectMapper.writeValueAsString(request);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"method\":\"getBlockByHeight\""));
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"height\":100"));
    }
    
    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"jsonrpc\":\"2.0\",\"method\":\"getCurrentHeight\",\"params\":null,\"id\":1}";
        
        // When
        RpcRequest request = objectMapper.readValue(json, RpcRequest.class);
        
        // Then
        assertEquals("2.0", request.getJsonrpc());
        assertEquals("getCurrentHeight", request.getMethod());
        assertNull(request.getParams());
        assertEquals(1, request.getId());
    }
    
    @Test
    void testJsonDeserialization_WithParams() throws Exception {
        // Given
        String json = "{\"jsonrpc\":\"2.0\",\"method\":\"getBlockByHeight\",\"params\":{\"height\":100},\"id\":1}";
        
        // When
        RpcRequest request = objectMapper.readValue(json, RpcRequest.class);
        
        // Then
        assertEquals("2.0", request.getJsonrpc());
        assertEquals("getBlockByHeight", request.getMethod());
        assertNotNull(request.getParams());
        Map<String, Object> params = request.getParamsAsMap();
        assertEquals(100, params.get("height"));
        assertEquals(1, request.getId());
    }
}
