package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账户未找到异常测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class AccountNotFoundExceptionTest {
    
    @Test
    void testConstructorWithPublicKey() {
        // Given
        String publicKey = "test-public-key";
        
        // When
        AccountNotFoundException exception = new AccountNotFoundException(publicKey);
        
        // Then
        assertTrue(exception.getMessage().contains("test-public-key"));
        assertEquals(RpcError.ACCOUNT_NOT_FOUND, exception.getRpcError().getCode());
        assertTrue(exception.getRpcError().getMessage().contains("test-public-key"));
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithPublicKeyAndCause() {
        // Given
        String publicKey = "test-public-key";
        RuntimeException cause = new RuntimeException("Root cause");
        
        // When
        AccountNotFoundException exception = new AccountNotFoundException(publicKey, cause);
        
        // Then
        assertTrue(exception.getMessage().contains("test-public-key"));
        assertEquals(RpcError.ACCOUNT_NOT_FOUND, exception.getRpcError().getCode());
        assertTrue(exception.getRpcError().getMessage().contains("test-public-key"));
        assertEquals(cause, exception.getCause());
    }
}
