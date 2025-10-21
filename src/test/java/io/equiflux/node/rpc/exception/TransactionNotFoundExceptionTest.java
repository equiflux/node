package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 交易未找到异常测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class TransactionNotFoundExceptionTest {
    
    @Test
    void testConstructorWithHash() {
        // Given
        String hash = "test-hash";
        
        // When
        TransactionNotFoundException exception = new TransactionNotFoundException(hash);
        
        // Then
        assertTrue(exception.getMessage().contains("test-hash"));
        assertEquals(RpcError.TRANSACTION_NOT_FOUND, exception.getRpcError().getCode());
        assertTrue(exception.getRpcError().getMessage().contains("test-hash"));
        assertNull(exception.getCause());
    }
}
