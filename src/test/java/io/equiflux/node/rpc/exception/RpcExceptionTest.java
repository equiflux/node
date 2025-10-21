package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import io.equiflux.node.rpc.dto.RpcResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RPC异常测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class RpcExceptionTest {
    
    @Test
    void testConstructorWithRpcError() {
        // Given
        RpcError rpcError = new RpcError(-32601, "Method not found");
        
        // When
        RpcException exception = new RpcException(rpcError);
        
        // Then
        assertEquals(rpcError, exception.getRpcError());
        assertEquals("Method not found", exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithRpcErrorAndCause() {
        // Given
        RpcError rpcError = new RpcError(-32601, "Method not found");
        RuntimeException cause = new RuntimeException("Root cause");
        
        // When
        RpcException exception = new RpcException(rpcError, cause);
        
        // Then
        assertEquals(rpcError, exception.getRpcError());
        assertEquals("Method not found", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testConstructorWithCodeAndMessage() {
        // Given
        int code = -32601;
        String message = "Method not found";
        
        // When
        RpcException exception = new RpcException(code, message);
        
        // Then
        assertNotNull(exception.getRpcError());
        assertEquals(code, exception.getRpcError().getCode());
        assertEquals(message, exception.getRpcError().getMessage());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithCodeMessageAndCause() {
        // Given
        int code = -32601;
        String message = "Method not found";
        RuntimeException cause = new RuntimeException("Root cause");
        
        // When
        RpcException exception = new RpcException(code, message, cause);
        
        // Then
        assertNotNull(exception.getRpcError());
        assertEquals(code, exception.getRpcError().getCode());
        assertEquals(message, exception.getRpcError().getMessage());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testConstructorWithCodeMessageAndData() {
        // Given
        int code = -32601;
        String message = "Method not found";
        Object data = "additional data";
        
        // When
        RpcException exception = new RpcException(code, message, data);
        
        // Then
        assertNotNull(exception.getRpcError());
        assertEquals(code, exception.getRpcError().getCode());
        assertEquals(message, exception.getRpcError().getMessage());
        assertEquals(data, exception.getRpcError().getData());
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithCodeMessageDataAndCause() {
        // Given
        int code = -32601;
        String message = "Method not found";
        Object data = "additional data";
        RuntimeException cause = new RuntimeException("Root cause");
        
        // When
        RpcException exception = new RpcException(code, message, data, cause);
        
        // Then
        assertNotNull(exception.getRpcError());
        assertEquals(code, exception.getRpcError().getCode());
        assertEquals(message, exception.getRpcError().getMessage());
        assertEquals(data, exception.getRpcError().getData());
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testToRpcResponse() {
        // Given
        RpcException exception = new RpcException(-32601, "Method not found");
        Object requestId = 1;
        
        // When
        RpcResponse response = exception.toRpcResponse(requestId);
        
        // Then
        assertNotNull(response);
        assertTrue(response.isError());
        assertEquals(-32601, response.getError().getCode());
        assertEquals("Method not found", response.getError().getMessage());
        assertEquals(requestId, response.getId());
    }
    
    @Test
    void testToRpcResponseWithData() {
        // Given
        RpcException exception = new RpcException(-32601, "Method not found", "additional data");
        Object requestId = 1;
        
        // When
        RpcResponse response = exception.toRpcResponse(requestId);
        
        // Then
        assertNotNull(response);
        assertTrue(response.isError());
        assertEquals(-32601, response.getError().getCode());
        assertEquals("Method not found", response.getError().getMessage());
        assertEquals("additional data", response.getError().getData());
        assertEquals(requestId, response.getId());
    }
}
