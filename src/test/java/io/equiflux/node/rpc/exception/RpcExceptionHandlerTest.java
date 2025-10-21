package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RPC异常处理器测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class RpcExceptionHandlerTest {
    
    @InjectMocks
    private RpcExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        // 初始化测试环境
    }
    
    @Test
    void testHandleRpcException() {
        // Given
        RpcException exception = new RpcException(-32601, "Method not found");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRpcException(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("2.0", body.get("jsonrpc"));
        assertNotNull(body.get("error"));
        assertNull(body.get("id"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        assertEquals(-32601, error.get("code"));
        assertEquals("Method not found", error.get("message"));
    }
    
    @Test
    void testHandleRpcExceptionWithData() {
        // Given
        RpcException exception = new RpcException(-32601, "Method not found", "additional data");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRpcException(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("2.0", body.get("jsonrpc"));
        assertNotNull(body.get("error"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        assertEquals(-32601, error.get("code"));
        assertEquals("Method not found", error.get("message"));
        assertEquals("additional data", error.get("data"));
    }
    
    @Test
    void testHandleBlockNotFoundException() {
        // Given
        BlockNotFoundException exception = new BlockNotFoundException("Block not found: 100");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBlockNotFoundException(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("2.0", body.get("jsonrpc"));
        assertNotNull(body.get("error"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        assertEquals(RpcError.BLOCK_NOT_FOUND, error.get("code"));
        assertTrue(((String) error.get("message")).contains("Block not found: 100"));
    }
    
    @Test
    void testHandleTransactionNotFoundException() {
        // Given
        TransactionNotFoundException exception = new TransactionNotFoundException("Transaction not found: test-hash");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleTransactionNotFoundException(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("2.0", body.get("jsonrpc"));
        assertNotNull(body.get("error"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        assertEquals(RpcError.TRANSACTION_NOT_FOUND, error.get("code"));
        assertTrue(((String) error.get("message")).contains("Transaction not found: test-hash"));
    }
    
    @Test
    void testHandleAccountNotFoundException() {
        // Given
        AccountNotFoundException exception = new AccountNotFoundException("Account not found: test-public-key");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleAccountNotFoundException(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("2.0", body.get("jsonrpc"));
        assertNotNull(body.get("error"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        assertEquals(RpcError.ACCOUNT_NOT_FOUND, error.get("code"));
        assertTrue(((String) error.get("message")).contains("Account not found: test-public-key"));
    }
    
    @Test
    void testHandleInsufficientBalanceException() {
        // Given
        InsufficientBalanceException exception = new InsufficientBalanceException("test-public-key", 1000L, 500L);
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInsufficientBalanceException(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("2.0", body.get("jsonrpc"));
        assertNotNull(body.get("error"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        assertEquals(RpcError.INSUFFICIENT_BALANCE, error.get("code"));
        assertTrue(((String) error.get("message")).contains("Insufficient balance: required 1000, available 500"));
    }
    
    @Test
    void testHandleInvalidSignatureException() {
        // Given
        InvalidSignatureException exception = new InvalidSignatureException("Invalid signature: verification failed");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidSignatureException(exception);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("2.0", body.get("jsonrpc"));
        assertNotNull(body.get("error"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        assertEquals(RpcError.INVALID_SIGNATURE, error.get("code"));
        assertTrue(((String) error.get("message")).contains("Invalid signature: verification failed"));
    }
    
}
