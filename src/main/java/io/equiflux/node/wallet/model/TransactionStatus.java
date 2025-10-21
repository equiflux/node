package io.equiflux.node.wallet.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 交易状态
 * 
 * <p>表示交易在区块链中的状态。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionStatus {
    
    private final String transactionHash;
    private final TransactionState state;
    private final LocalDateTime timestamp;
    private final Long blockHeight;
    private final Integer blockIndex;
    private final String errorMessage;
    
    /**
     * 构造交易状态
     * 
     * @param transactionHash 交易哈希
     * @param state 交易状态
     * @param timestamp 时间戳
     * @param blockHeight 区块高度
     * @param blockIndex 区块内索引
     * @param errorMessage 错误消息
     */
    public TransactionStatus(String transactionHash, TransactionState state, 
                           LocalDateTime timestamp, Long blockHeight, 
                           Integer blockIndex, String errorMessage) {
        this.transactionHash = Objects.requireNonNull(transactionHash, "Transaction hash cannot be null");
        this.state = Objects.requireNonNull(state, "State cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.blockHeight = blockHeight;
        this.blockIndex = blockIndex;
        this.errorMessage = errorMessage;
    }
    
    /**
     * 获取交易哈希
     * 
     * @return 交易哈希
     */
    public String getTransactionHash() {
        return transactionHash;
    }
    
    /**
     * 获取交易状态
     * 
     * @return 交易状态
     */
    public TransactionState getState() {
        return state;
    }
    
    /**
     * 获取时间戳
     * 
     * @return 时间戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取区块高度
     * 
     * @return 区块高度
     */
    public Long getBlockHeight() {
        return blockHeight;
    }
    
    /**
     * 获取区块内索引
     * 
     * @return 区块内索引
     */
    public Integer getBlockIndex() {
        return blockIndex;
    }
    
    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 是否已确认
     * 
     * @return true如果已确认，false否则
     */
    public boolean isConfirmed() {
        return state == TransactionState.CONFIRMED;
    }
    
    /**
     * 是否失败
     * 
     * @return true如果失败，false否则
     */
    public boolean isFailed() {
        return state == TransactionState.FAILED;
    }
    
    /**
     * 是否待处理
     * 
     * @return true如果待处理，false否则
     */
    public boolean isPending() {
        return state == TransactionState.PENDING;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TransactionStatus that = (TransactionStatus) obj;
        return Objects.equals(transactionHash, that.transactionHash);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionHash);
    }
    
    @Override
    public String toString() {
        return "TransactionStatus{" +
               "transactionHash='" + transactionHash + '\'' +
               ", state=" + state +
               ", timestamp=" + timestamp +
               ", blockHeight=" + blockHeight +
               ", blockIndex=" + blockIndex +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }
}
