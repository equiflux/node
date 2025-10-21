package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RPC错误对象测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class RpcErrorTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testConstructorWithCodeAndMessage() {
        // Given
        int code = -32601;
        String message = "Method not found";
        
        // When
        RpcError error = new RpcError(code, message);
        
        // Then
        assertEquals(code, error.getCode());
        assertEquals(message, error.getMessage());
        assertNull(error.getData());
    }
    
    @Test
    void testConstructorWithCodeMessageAndData() {
        // Given
        int code = -32601;
        String message = "Method not found";
        Object data = "additional data";
        
        // When
        RpcError error = new RpcError(code, message, data);
        
        // Then
        assertEquals(code, error.getCode());
        assertEquals(message, error.getMessage());
        assertEquals(data, error.getData());
    }
    
    @Test
    void testStandardErrorCodes() {
        // When & Then
        assertEquals(-32700, RpcError.PARSE_ERROR);
        assertEquals(-32600, RpcError.INVALID_REQUEST);
        assertEquals(-32601, RpcError.METHOD_NOT_FOUND);
        assertEquals(-32602, RpcError.INVALID_PARAMS);
        assertEquals(-32603, RpcError.INTERNAL_ERROR);
        assertEquals(-32099, RpcError.SERVER_ERROR_MIN);
        assertEquals(-32000, RpcError.SERVER_ERROR_MAX);
    }
    
    @Test
    void testEquifluxSpecificErrorCodes() {
        // When & Then
        assertEquals(-32001, RpcError.BLOCK_NOT_FOUND);
        assertEquals(-32002, RpcError.TRANSACTION_NOT_FOUND);
        assertEquals(-32003, RpcError.ACCOUNT_NOT_FOUND);
        assertEquals(-32004, RpcError.INSUFFICIENT_BALANCE);
        assertEquals(-32005, RpcError.INVALID_SIGNATURE);
        assertEquals(-32006, RpcError.INVALID_NONCE);
        assertEquals(-32007, RpcError.STORAGE_ERROR);
        assertEquals(-32008, RpcError.NETWORK_ERROR);
        assertEquals(-32009, RpcError.CONSENSUS_ERROR);
        assertEquals(-32010, RpcError.VALIDATION_ERROR);
    }
    
    @Test
    void testJsonSerialization() throws Exception {
        // Given
        RpcError error = new RpcError(-32601, "Method not found", "additional data");
        
        // When
        String json = objectMapper.writeValueAsString(error);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"code\":-32601"));
        assertTrue(json.contains("\"message\":\"Method not found\""));
        assertTrue(json.contains("\"data\":\"additional data\""));
    }
    
    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"code\":-32601,\"message\":\"Method not found\",\"data\":\"additional data\"}";
        
        // When
        RpcError error = objectMapper.readValue(json, RpcError.class);
        
        // Then
        assertEquals(-32601, error.getCode());
        assertEquals("Method not found", error.getMessage());
        assertEquals("additional data", error.getData());
    }
    
    @Test
    void testJsonDeserializationWithoutData() throws Exception {
        // Given
        String json = "{\"code\":-32601,\"message\":\"Method not found\"}";
        
        // When
        RpcError error = objectMapper.readValue(json, RpcError.class);
        
        // Then
        assertEquals(-32601, error.getCode());
        assertEquals("Method not found", error.getMessage());
        assertNull(error.getData());
    }
    
    @Test
    void testIsServerError() {
        // Given
        RpcError standardError = new RpcError(RpcError.METHOD_NOT_FOUND, "Method not found");
        RpcError serverError = new RpcError(RpcError.BLOCK_NOT_FOUND, "Block not found");
        RpcError clientError = new RpcError(-40000, "Client error");
        
        // When & Then
        assertFalse(standardError.isServerError());
        assertTrue(serverError.isServerError());
        assertFalse(clientError.isServerError());
    }
    
    @Test
    void testIsStandardError() {
        // Given
        RpcError standardError = new RpcError(RpcError.METHOD_NOT_FOUND, "Method not found");
        RpcError serverError = new RpcError(RpcError.BLOCK_NOT_FOUND, "Block not found");
        RpcError clientError = new RpcError(-40000, "Client error");
        
        // When & Then
        assertTrue(standardError.isStandardError());
        assertFalse(serverError.isStandardError());
        assertFalse(clientError.isStandardError());
    }
}
