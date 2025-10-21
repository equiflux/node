package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

/**
 * 账户信息DTO
 * 
 * <p>用于RPC接口返回账户信息，包含账户的余额、质押等信息。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class AccountInfoDto {
    
    @NotBlank(message = "Public key is required")
    @JsonProperty("publicKey")
    private String publicKey;
    
    @JsonProperty("address")
    private String address;
    
    @NotNull(message = "Balance is required")
    @JsonProperty("balance")
    private Long balance;
    
    @JsonProperty("stakeAmount")
    private Long stakeAmount;
    
    @JsonProperty("nonce")
    private Long nonce;
    
    @JsonProperty("lastUpdated")
    private Long lastUpdated;
    
    @JsonProperty("isSuperNode")
    private Boolean isSuperNode;
    
    @JsonProperty("nodeType")
    private String nodeType;
    
    @JsonProperty("performanceScore")
    private Double performanceScore;
    
    @JsonProperty("totalRewards")
    private Long totalRewards;
    
    @JsonProperty("transactionCount")
    private Long transactionCount;
    
    /**
     * 默认构造函数
     */
    public AccountInfoDto() {
    }
    
    /**
     * 构造函数
     * 
     * @param publicKey 公钥
     * @param balance 余额
     * @param stakeAmount 质押金额
     * @param nonce 随机数
     */
    public AccountInfoDto(String publicKey, Long balance, Long stakeAmount, Long nonce) {
        this.publicKey = publicKey;
        this.balance = balance;
        this.stakeAmount = stakeAmount;
        this.nonce = nonce;
    }
    
    // Getters and Setters
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Long getBalance() {
        return balance;
    }
    
    public void setBalance(Long balance) {
        this.balance = balance;
    }
    
    public Long getStakeAmount() {
        return stakeAmount;
    }
    
    public void setStakeAmount(Long stakeAmount) {
        this.stakeAmount = stakeAmount;
    }
    
    public Long getNonce() {
        return nonce;
    }
    
    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }
    
    public Long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public Boolean getIsSuperNode() {
        return isSuperNode;
    }
    
    public void setIsSuperNode(Boolean isSuperNode) {
        this.isSuperNode = isSuperNode;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    
    public Double getPerformanceScore() {
        return performanceScore;
    }
    
    public void setPerformanceScore(Double performanceScore) {
        this.performanceScore = performanceScore;
    }
    
    public Long getTotalRewards() {
        return totalRewards;
    }
    
    public void setTotalRewards(Long totalRewards) {
        this.totalRewards = totalRewards;
    }
    
    public Long getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }
    
    @Override
    public String toString() {
        return "AccountInfoDto{" +
                "publicKey='" + publicKey + '\'' +
                ", address='" + address + '\'' +
                ", balance=" + balance +
                ", stakeAmount=" + stakeAmount +
                ", nonce=" + nonce +
                ", isSuperNode=" + isSuperNode +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AccountInfoDto that = (AccountInfoDto) obj;
        return Objects.equals(publicKey, that.publicKey) &&
               Objects.equals(address, that.address) &&
               Objects.equals(balance, that.balance) &&
               Objects.equals(stakeAmount, that.stakeAmount) &&
               Objects.equals(nonce, that.nonce) &&
               Objects.equals(lastUpdated, that.lastUpdated) &&
               Objects.equals(isSuperNode, that.isSuperNode) &&
               Objects.equals(nodeType, that.nodeType) &&
               Objects.equals(performanceScore, that.performanceScore) &&
               Objects.equals(totalRewards, that.totalRewards) &&
               Objects.equals(transactionCount, that.transactionCount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(publicKey, address, balance, stakeAmount, nonce, lastUpdated, 
                           isSuperNode, nodeType, performanceScore, totalRewards, transactionCount);
    }
}
