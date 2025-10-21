package io.equiflux.node.wallet.rpc.dto;

import java.util.List;

/**
 * 交易历史响应DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionHistoryResponseDto {
    
    private boolean success;
    private String message;
    private String publicKeyHex;
    private List<TransactionInfoDto> transactions;
    private int count;
    private int limit;
    private int offset;
    
    public TransactionHistoryResponseDto() {
        this.success = true;
    }
    
    public static TransactionHistoryResponseDto error(String message) {
        TransactionHistoryResponseDto dto = new TransactionHistoryResponseDto();
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
    
    public String getPublicKeyHex() {
        return publicKeyHex;
    }
    
    public void setPublicKeyHex(String publicKeyHex) {
        this.publicKeyHex = publicKeyHex;
    }
    
    public List<TransactionInfoDto> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<TransactionInfoDto> transactions) {
        this.transactions = transactions;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
}
