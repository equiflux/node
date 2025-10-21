package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RPC响应对象测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class RpcResponseTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testSuccessConstructor() {
        // Given
        Object result = "test result";
        Object id = 1;
        
        // When
        RpcResponse response = new RpcResponse(result, id);
        
        // Then
        assertEquals("2.0", response.getJsonrpc());
        assertEquals(result, response.getResult());
        assertNull(response.getError());
        assertEquals(id, response.getId());
    }
    
    @Test
    void testErrorConstructor() {
        // Given
        RpcError error = new RpcError(-32601, "Method not found");
        Object id = 1;
        
        // When
        RpcResponse response = new RpcResponse(error, id);
        
        // Then
        assertEquals("2.0", response.getJsonrpc());
        assertNull(response.getResult());
        assertEquals(error, response.getError());
        assertEquals(id, response.getId());
    }
    
    @Test
    void testSuccessStaticMethod() {
        // Given
        Object result = "test result";
        Object id = 1;
        
        // When
        RpcResponse response = RpcResponse.success(result, id);
        
        // Then
        assertEquals("2.0", response.getJsonrpc());
        assertEquals(result, response.getResult());
        assertNull(response.getError());
        assertEquals(id, response.getId());
    }
    
    @Test
    void testErrorStaticMethod() {
        // Given
        int code = -32601;
        String message = "Method not found";
        Object id = 1;
        
        // When
        RpcResponse response = RpcResponse.error(code, message, id);
        
        // Then
        assertEquals("2.0", response.getJsonrpc());
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(code, response.getError().getCode());
        assertEquals(message, response.getError().getMessage());
        assertEquals(id, response.getId());
    }
    
    @Test
    void testErrorStaticMethodWithData() {
        // Given
        int code = -32601;
        String message = "Method not found";
        Object data = "additional data";
        Object id = 1;
        
        // When
        RpcResponse response = RpcResponse.error(code, message, data, id);
        
        // Then
        assertEquals("2.0", response.getJsonrpc());
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(code, response.getError().getCode());
        assertEquals(message, response.getError().getMessage());
        assertEquals(data, response.getError().getData());
        assertEquals(id, response.getId());
    }
    
    @Test
    void testJsonSerialization_Success() throws Exception {
        // Given
        Map<String, Object> result = new HashMap<>();
        result.put("height", 100L);
        result.put("hash", "test-hash");
        RpcResponse response = RpcResponse.success(result, 1);
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"result\""));
        assertTrue(json.contains("\"id\":1"));
    }
    
    @Test
    void testJsonSerialization_Error() throws Exception {
        // Given
        RpcResponse response = RpcResponse.error(-32601, "Method not found", 1);
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"error\""));
        assertTrue(json.contains("\"code\":-32601"));
        assertTrue(json.contains("\"message\":\"Method not found\""));
        assertTrue(json.contains("\"id\":1"));
    }
    
    @Test
    void testJsonDeserialization_Success() throws Exception {
        // Given
        String json = "{\"jsonrpc\":\"2.0\",\"result\":{\"height\":100},\"id\":1}";
        
        // When
        RpcResponse response = objectMapper.readValue(json, RpcResponse.class);
        
        // Then
        assertEquals("2.0", response.getJsonrpc());
        assertNotNull(response.getResult());
        assertNull(response.getError());
        assertEquals(1, response.getId());
    }
    
    @Test
    void testJsonDeserialization_Error() throws Exception {
        // Given
        String json = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found\"},\"id\":1}";
        
        // When
        RpcResponse response = objectMapper.readValue(json, RpcResponse.class);
        
        // Then
        assertEquals("2.0", response.getJsonrpc());
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(-32601, response.getError().getCode());
        assertEquals("Method not found", response.getError().getMessage());
        assertEquals(1, response.getId());
    }
    
    @Test
    void testIsSuccess() {
        // Given
        RpcResponse successResponse = RpcResponse.success("result", 1);
        RpcResponse errorResponse = RpcResponse.error(-32601, "error", 1);
        
        // When & Then
        assertTrue(successResponse.isSuccess());
        assertFalse(errorResponse.isSuccess());
    }
    
    @Test
    void testIsError() {
        // Given
        RpcResponse successResponse = RpcResponse.success("result", 1);
        RpcResponse errorResponse = RpcResponse.error(-32601, "error", 1);
        
        // When & Then
        assertFalse(successResponse.isError());
        assertTrue(errorResponse.isError());
    }
}
