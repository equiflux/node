package io.equiflux.node.explorer.exception;

/**
 * 区块浏览器异常
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class ExplorerException extends RuntimeException {
    
    public ExplorerException(String message) {
        super(message);
    }
    
    public ExplorerException(String message, Throwable cause) {
        super(message, cause);
    }
}
