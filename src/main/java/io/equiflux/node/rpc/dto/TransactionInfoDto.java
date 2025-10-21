package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

/**
 * 交易信息DTO
 * 
 * <p>用于RPC接口返回交易信息，包含交易的基本信息和状态。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class TransactionInfoDto {
    
    @NotBlank(message = "Transaction hash is required")
    @JsonProperty("hash")
    private String hash;
    
    @JsonProperty("from")
    private String from;
    
    @JsonProperty("to")
    private String to;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("fee")
    private Long fee;
    
    @JsonProperty("nonce")
    private Long nonce;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("signature")
    private String signature;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("blockHeight")
    private Long blockHeight;
    
    @JsonProperty("blockHash")
    private String blockHash;
    
    @JsonProperty("gasLimit")
    private Long gasLimit;
    
    @JsonProperty("gasUsed")
    private Long gasUsed;
    
    @JsonProperty("data")
    private String data;
    
    /**
     * 默认构造函数
     */
    public TransactionInfoDto() {
    }
    
    /**
     * 构造函数
     * 
     * @param hash 交易哈希
     * @param from 发送方
     * @param to 接收方
     * @param amount 金额
     * @param fee 手续费
     * @param nonce 随机数
     */
    public TransactionInfoDto(String hash, String from, String to, Long amount, 
                            Long fee, Long nonce) {
        this.hash = hash;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.fee = fee;
        this.nonce = nonce;
    }
    
    // Getters and Setters
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public Long getFee() {
        return fee;
    }
    
    public void setFee(Long fee) {
        this.fee = fee;
    }
    
    public Long getNonce() {
        return nonce;
    }
    
    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getBlockHeight() {
        return blockHeight;
    }
    
    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }
    
    public String getBlockHash() {
        return blockHash;
    }
    
    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }
    
    public Long getGasLimit() {
        return gasLimit;
    }
    
    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }
    
    public Long getGasUsed() {
        return gasUsed;
    }
    
    public void setGasUsed(Long gasUsed) {
        this.gasUsed = gasUsed;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "TransactionInfoDto{" +
                "hash='" + hash + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", amount=" + amount +
                ", fee=" + fee +
                ", nonce=" + nonce +
                ", status='" + status + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TransactionInfoDto that = (TransactionInfoDto) obj;
        return Objects.equals(hash, that.hash) &&
               Objects.equals(from, that.from) &&
               Objects.equals(to, that.to) &&
               Objects.equals(amount, that.amount) &&
               Objects.equals(fee, that.fee) &&
               Objects.equals(nonce, that.nonce) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(signature, that.signature) &&
               Objects.equals(status, that.status) &&
               Objects.equals(blockHeight, that.blockHeight) &&
               Objects.equals(blockHash, that.blockHash) &&
               Objects.equals(gasLimit, that.gasLimit) &&
               Objects.equals(gasUsed, that.gasUsed) &&
               Objects.equals(data, that.data);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hash, from, to, amount, fee, nonce, timestamp, 
                           signature, status, blockHeight, blockHash, 
                           gasLimit, gasUsed, data);
    }
}
