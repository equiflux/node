package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 余额不足异常测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
class InsufficientBalanceExceptionTest {
    
    @Test
    void testConstructorWithAmounts() {
        // Given
        String publicKey = "test-public-key";
        long required = 1000L;
        long available = 500L;
        
        // When
        InsufficientBalanceException exception = new InsufficientBalanceException(publicKey, required, available);
        
        // Then
        assertTrue(exception.getMessage().contains("1000"));
        assertTrue(exception.getMessage().contains("500"));
        assertEquals(RpcError.INSUFFICIENT_BALANCE, exception.getRpcError().getCode());
        assertTrue(exception.getRpcError().getMessage().contains("1000"));
        assertTrue(exception.getRpcError().getMessage().contains("500"));
        assertNull(exception.getCause());
        
        // 验证数据对象
        assertNotNull(exception.getRpcError().getData());
        assertTrue(exception.getRpcError().getData() instanceof InsufficientBalanceException.BalanceInfo);
    }
    
    @Test
    void testConstructorWithAmountsAndCause() {
        // Given
        String publicKey = "test-public-key";
        long required = 1000L;
        long available = 500L;
        RuntimeException cause = new RuntimeException("Root cause");
        
        // When
        InsufficientBalanceException exception = new InsufficientBalanceException(publicKey, required, available, cause);
        
        // Then
        assertTrue(exception.getMessage().contains("1000"));
        assertTrue(exception.getMessage().contains("500"));
        assertEquals(RpcError.INSUFFICIENT_BALANCE, exception.getRpcError().getCode());
        assertTrue(exception.getRpcError().getMessage().contains("1000"));
        assertTrue(exception.getRpcError().getMessage().contains("500"));
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testBalanceInfo() {
        // Given
        InsufficientBalanceException.BalanceInfo balanceInfo = new InsufficientBalanceException.BalanceInfo(1000L, 500L);
        
        // When & Then
        assertEquals(1000L, balanceInfo.getRequired());
        assertEquals(500L, balanceInfo.getAvailable());
        assertEquals(500L, balanceInfo.getShortage());
    }
}
