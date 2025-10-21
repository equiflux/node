package io.equiflux.node.wallet.rpc.dto;

/**
 * 交易广播响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionBroadcastResponseDto {
    
    private boolean success;
    private String message;
    private String transactionHash;
    
    public TransactionBroadcastResponseDto() {
        this.success = true;
    }
    
    public static TransactionBroadcastResponseDto error(String message) {
        TransactionBroadcastResponseDto dto = new TransactionBroadcastResponseDto();
        dto.success = false;
        dto.message = message;
        return dto;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTransactionHash() {
        return transactionHash;
    }
    
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }
}
