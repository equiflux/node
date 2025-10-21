package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 区块未找到异常测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class BlockNotFoundExceptionTest {
    
    @Test
    void testConstructorWithHeight() {
        // Given
        long height = 100L;
        
        // When
        BlockNotFoundException exception = new BlockNotFoundException(height);
        
        // Then
        assertTrue(exception.getMessage().contains("100"));
        assertEquals(RpcError.BLOCK_NOT_FOUND, exception.getRpcError().getCode());
        assertTrue(exception.getRpcError().getMessage().contains("100"));
        assertNull(exception.getCause());
    }
    
    @Test
    void testConstructorWithHash() {
        // Given
        String hash = "test-hash";
        
        // When
        BlockNotFoundException exception = new BlockNotFoundException(hash);
        
        // Then
        assertTrue(exception.getMessage().contains("test-hash"));
        assertEquals(RpcError.BLOCK_NOT_FOUND, exception.getRpcError().getCode());
        assertTrue(exception.getRpcError().getMessage().contains("test-hash"));
        assertNull(exception.getCause());
    }
}
