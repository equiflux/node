package io.equiflux.node.wallet.rpc.dto;

import java.time.LocalDateTime;

/**
 * 交易状态响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionStatusResponseDto {
    
    private boolean success;
    private String message;
    private String transactionHash;
    private String state;
    private LocalDateTime timestamp;
    private Long blockHeight;
    private Integer blockIndex;
    private String errorMessage;
    
    public TransactionStatusResponseDto() {
        this.success = true;
    }
    
    public static TransactionStatusResponseDto error(String message) {
        TransactionStatusResponseDto dto = new TransactionStatusResponseDto();
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
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getBlockHeight() {
        return blockHeight;
    }
    
    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }
    
    public Integer getBlockIndex() {
        return blockIndex;
    }
    
    public void setBlockIndex(Integer blockIndex) {
        this.blockIndex = blockIndex;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
