package io.equiflux.node.wallet.rpc.dto;

import io.equiflux.node.model.Transaction;

/**
 * 广播交易请求DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class BroadcastTransactionRequestDto {
    
    private Transaction transaction;
    private String transactionHash;
    
    public BroadcastTransactionRequestDto() {}
    
    public BroadcastTransactionRequestDto(Transaction transaction) {
        this.transaction = transaction;
        this.transactionHash = transaction.getHashHex();
    }
    
    // Getters and Setters
    public Transaction getTransaction() {
        return transaction;
    }
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
    
    public String getTransactionHash() {
        return transactionHash;
    }
    
    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }
}
