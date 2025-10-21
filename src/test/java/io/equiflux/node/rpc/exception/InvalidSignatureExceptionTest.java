package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 无效签名异常测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class InvalidSignatureExceptionTest {
    
    @Test
    void testConstructorWithMessage() {
        // Given
        String message = "Invalid signature: verification failed";
        
        // When
        InvalidSignatureException exception = new InvalidSignatureException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(RpcError.INVALID_SIGNATURE, exception.getRpcError().getCode());
        assertEquals(message, exception.getRpcError().getMessage());
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithMessageAndCause() {
        // Given
        String message = "Invalid signature: verification failed";
        RuntimeException cause = new RuntimeException("Root cause");
        
        // When
        InvalidSignatureException exception = new InvalidSignatureException(message, cause);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(RpcError.INVALID_SIGNATURE, exception.getRpcError().getCode());
        assertEquals(message, exception.getRpcError().getMessage());
        assertEquals(cause, exception.getCause());
    }
}
