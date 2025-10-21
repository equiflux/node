package io.equiflux.node.storage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.PublicKey;
import java.util.Objects;

/**
 * 账户状态
 * 
 * <p>表示区块链中一个账户的当前状态，包括余额、nonce等信息。
 * 
 * <p>账户状态结构：
 * <ul>
 *   <li>账户公钥</li>
 *   <li>账户余额</li>
 *   <li>交易nonce（防重放）</li>
 *   <li>质押金额（用于PoS）</li>
 *   <li>最后更新时间戳</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class AccountState {
    
    private final PublicKey publicKey;
    private final long balance;
    private final long nonce;
    private final long stakeAmount;
    private final long lastUpdateTimestamp;
    
    /**
     * 构造账户状态
     * 
     * @param publicKeyBytes 账户公钥字节数组
     * @param balance 账户余额
     * @param nonce 交易nonce
     * @param stakeAmount 质押金额
     * @param lastUpdateTimestamp 最后更新时间戳
     */
    @JsonCreator
    public AccountState(@JsonProperty("publicKey") byte[] publicKeyBytes,
                        @JsonProperty("balance") long balance,
                        @JsonProperty("nonce") long nonce,
                        @JsonProperty("stakeAmount") long stakeAmount,
                        @JsonProperty("lastUpdateTimestamp") long lastUpdateTimestamp) {
        this.publicKey = createPublicKeyFromBytes(publicKeyBytes);
        this.balance = balance;
        this.nonce = nonce;
        this.stakeAmount = stakeAmount;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        
        // 验证参数
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce cannot be negative");
        }
        if (stakeAmount < 0) {
            throw new IllegalArgumentException("Stake amount cannot be negative");
        }
        if (lastUpdateTimestamp <= 0) {
            throw new IllegalArgumentException("Last update timestamp must be positive");
        }
    }
    
    /**
     * 构造账户状态（使用PublicKey对象）
     * 
     * @param publicKey 账户公钥
     * @param balance 账户余额
     * @param nonce 交易nonce
     * @param stakeAmount 质押金额
     * @param lastUpdateTimestamp 最后更新时间戳
     */
    public AccountState(PublicKey publicKey,
                        long balance,
                        long nonce,
                        long stakeAmount,
                        long lastUpdateTimestamp) {
        this.publicKey = Objects.requireNonNull(publicKey, "Public key cannot be null");
        this.balance = balance;
        this.nonce = nonce;
        this.stakeAmount = stakeAmount;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        
        // 验证参数
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce cannot be negative");
        }
        if (stakeAmount < 0) {
            throw new IllegalArgumentException("Stake amount cannot be negative");
        }
        if (lastUpdateTimestamp <= 0) {
            throw new IllegalArgumentException("Last update timestamp must be positive");
        }
    }
    
    /**
     * 获取账户公钥
     * 
     * @return 账户公钥
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    /**
     * 获取账户公钥的字节数组（用于序列化）
     * 
     * @return 账户公钥的字节数组
     */
    @com.fasterxml.jackson.annotation.JsonProperty("publicKey")
    public byte[] getPublicKeyBytes() {
        return publicKey.getEncoded();
    }
    
    /**
     * 获取账户余额
     * 
     * @return 账户余额
     */
    public long getBalance() {
        return balance;
    }
    
    /**
     * 获取交易nonce
     * 
     * @return 交易nonce
     */
    public long getNonce() {
        return nonce;
    }
    
    /**
     * 获取质押金额
     * 
     * @return 质押金额
     */
    public long getStakeAmount() {
        return stakeAmount;
    }
    
    /**
     * 获取最后更新时间戳
     * 
     * @return 最后更新时间戳
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
    
    /**
     * 获取最后更新时间（LocalDateTime格式）
     * 
     * @return 最后更新时间
     */
    public java.time.LocalDateTime getLastUpdated() {
        return java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastUpdateTimestamp),
            java.time.ZoneId.systemDefault()
        );
    }
    
    /**
     * 从字节数组创建PublicKey
     * 
     * @param publicKeyBytes 公钥字节数组
     * @return PublicKey对象
     */
    private PublicKey createPublicKeyFromBytes(byte[] publicKeyBytes) {
        try {
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(publicKeyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("Ed25519");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid public key bytes", e);
        }
    }
    
    /**
     * 获取账户公钥的十六进制字符串
     * 
     * @return 账户公钥的十六进制字符串
     */
    public String getPublicKeyHex() {
        return io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded());
    }
    
    /**
     * 检查账户是否有足够余额
     * 
     * @param amount 需要的金额
     * @return true如果有足够余额，false否则
     */
    public boolean hasEnoughBalance(long amount) {
        return balance >= amount;
    }
    
    /**
     * 检查账户是否有足够质押
     * 
     * @param amount 需要的质押金额
     * @return true如果有足够质押，false否则
     */
    public boolean hasEnoughStake(long amount) {
        return stakeAmount >= amount;
    }
    
    /**
     * 检查nonce是否有效
     * 
     * @param transactionNonce 交易nonce
     * @return true如果nonce有效，false否则
     */
    public boolean isValidNonce(long transactionNonce) {
        return transactionNonce == nonce + 1;
    }
    
    /**
     * 创建更新后的账户状态
     * 
     * @param newBalance 新余额
     * @param newNonce 新nonce
     * @param newStakeAmount 新质押金额
     * @return 更新后的账户状态
     */
    public AccountState update(long newBalance, long newNonce, long newStakeAmount) {
        return new AccountState(publicKey, newBalance, newNonce, newStakeAmount, System.currentTimeMillis());
    }
    
    /**
     * 创建余额更新后的账户状态
     * 
     * @param balanceChange 余额变化（可为负数）
     * @return 更新后的账户状态
     */
    public AccountState updateBalance(long balanceChange) {
        long newBalance = balance + balanceChange;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        return new AccountState(publicKey, newBalance, nonce, stakeAmount, System.currentTimeMillis());
    }
    
    /**
     * 创建nonce更新后的账户状态
     * 
     * @return 更新后的账户状态
     */
    public AccountState incrementNonce() {
        return new AccountState(publicKey, balance, nonce + 1, stakeAmount, System.currentTimeMillis());
    }
    
    /**
     * 创建质押更新后的账户状态
     * 
     * @param stakeChange 质押变化（可为负数）
     * @return 更新后的账户状态
     */
    public AccountState updateStake(long stakeChange) {
        long newStakeAmount = stakeAmount + stakeChange;
        if (newStakeAmount < 0) {
            throw new IllegalArgumentException("Insufficient stake");
        }
        return new AccountState(publicKey, balance, nonce, newStakeAmount, System.currentTimeMillis());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AccountState that = (AccountState) obj;
        return balance == that.balance &&
               nonce == that.nonce &&
               stakeAmount == that.stakeAmount &&
               lastUpdateTimestamp == that.lastUpdateTimestamp &&
               Objects.equals(publicKey, that.publicKey);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(publicKey, balance, nonce, stakeAmount, lastUpdateTimestamp);
    }
    
    @Override
    public String toString() {
        return "AccountState{" +
               "publicKey=" + getPublicKeyHex() +
               ", balance=" + balance +
               ", nonce=" + nonce +
               ", stakeAmount=" + stakeAmount +
               ", lastUpdateTimestamp=" + lastUpdateTimestamp +
               '}';
    }
}
