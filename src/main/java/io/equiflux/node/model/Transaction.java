package io.equiflux.node.model;

import io.equiflux.node.crypto.Ed25519KeyPair;
import io.equiflux.node.crypto.HashUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * 交易
 * 
 * <p>表示区块链中的一笔交易，包含发送者、接收者、金额、手续费等信息。
 * 
 * <p>交易结构：
 * <ul>
 *   <li>发送者公钥</li>
 *   <li>接收者公钥</li>
 *   <li>转账金额</li>
 *   <li>手续费</li>
 *   <li>时间戳</li>
 *   <li>Nonce（防重放）</li>
 *   <li>数字签名</li>
 * </ul>
 * 
 * <p>交易验证：
 * <ul>
 *   <li>签名验证</li>
 *   <li>格式验证</li>
 *   <li>余额验证（需要状态信息）</li>
 *   <li>Nonce验证（防重放）</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class Transaction {
    
    private final byte[] senderPublicKey;
    private final byte[] receiverPublicKey;
    private final long amount;
    private final long fee;
    private final long timestamp;
    private final long nonce;
    private final byte[] signature;
    private final byte[] hash;
    private final TransactionType type;
    
    /**
     * 构造交易
     * 
     * @param senderPublicKey 发送者公钥
     * @param receiverPublicKey 接收者公钥
     * @param amount 转账金额
     * @param fee 手续费
     * @param timestamp 时间戳
     * @param nonce 防重放随机数
     * @param signature 数字签名
     * @param type 交易类型
     */
    @JsonCreator
    public Transaction(@JsonProperty("senderPublicKey") byte[] senderPublicKey,
                       @JsonProperty("receiverPublicKey") byte[] receiverPublicKey,
                       @JsonProperty("amount") long amount,
                       @JsonProperty("fee") long fee,
                       @JsonProperty("timestamp") long timestamp,
                       @JsonProperty("nonce") long nonce,
                       @JsonProperty("signature") byte[] signature,
                       @JsonProperty("type") TransactionType type) {
        this.senderPublicKey = Objects.requireNonNull(senderPublicKey, "Sender public key cannot be null");
        this.receiverPublicKey = Objects.requireNonNull(receiverPublicKey, "Receiver public key cannot be null");
        this.amount = amount;
        this.fee = fee;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.signature = Objects.requireNonNull(signature, "Signature cannot be null");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        
        // 验证参数
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (fee < 0) {
            throw new IllegalArgumentException("Fee cannot be negative");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce cannot be negative");
        }
        if (signature.length != 64) {
            throw new IllegalArgumentException("Signature must be 64 bytes");
        }
        
        // 计算交易哈希
        this.hash = calculateHash();
    }
    
    /**
     * 构造交易（带哈希）
     * 
     * @param senderPublicKey 发送者公钥
     * @param receiverPublicKey 接收者公钥
     * @param amount 转账金额
     * @param fee 手续费
     * @param timestamp 时间戳
     * @param nonce 防重放随机数
     * @param signature 数字签名
     * @param hash 交易哈希
     * @param type 交易类型
     */
    public Transaction(byte[] senderPublicKey, byte[] receiverPublicKey, 
                      long amount, long fee, long timestamp, long nonce, 
                      byte[] signature, byte[] hash, TransactionType type) {
        this.senderPublicKey = Objects.requireNonNull(senderPublicKey, "Sender public key cannot be null");
        this.receiverPublicKey = Objects.requireNonNull(receiverPublicKey, "Receiver public key cannot be null");
        this.amount = amount;
        this.fee = fee;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.signature = Objects.requireNonNull(signature, "Signature cannot be null");
        this.hash = Objects.requireNonNull(hash, "Hash cannot be null");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        
        // 验证参数
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (fee < 0) {
            throw new IllegalArgumentException("Fee cannot be negative");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce cannot be negative");
        }
        if (signature.length != 64) {
            throw new IllegalArgumentException("Signature must be 64 bytes");
        }
        if (hash.length != 32) {
            throw new IllegalArgumentException("Hash must be 32 bytes");
        }
    }
    
    /**
     * 获取发送者公钥
     * 
     * @return 发送者公钥
     */
    public byte[] getSenderPublicKey() {
        return senderPublicKey.clone();
    }
    
    /**
     * 获取接收者公钥
     * 
     * @return 接收者公钥
     */
    public byte[] getReceiverPublicKey() {
        return receiverPublicKey.clone();
    }
    
    /**
     * 获取转账金额
     * 
     * @return 转账金额
     */
    public long getAmount() {
        return amount;
    }
    
    /**
     * 获取手续费
     * 
     * @return 手续费
     */
    public long getFee() {
        return fee;
    }
    
    /**
     * 获取时间戳
     * 
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取Nonce
     * 
     * @return Nonce
     */
    public long getNonce() {
        return nonce;
    }
    
    /**
     * 获取数字签名
     * 
     * @return 数字签名
     */
    public byte[] getSignature() {
        return signature.clone();
    }
    
    /**
     * 获取交易哈希
     * 
     * @return 交易哈希
     */
    public byte[] getHash() {
        return hash.clone();
    }
    
    /**
     * 获取交易哈希的十六进制字符串
     * 
     * @return 交易哈希的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getHashHex() {
        return HashUtils.toHexString(hash);
    }
    
    /**
     * 获取交易类型
     * 
     * @return 交易类型
     */
    public TransactionType getType() {
        return type;
    }
    
    /**
     * 获取发送者公钥的十六进制字符串
     * 
     * @return 发送者公钥的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getSenderPublicKeyHex() {
        return HashUtils.toHexString(senderPublicKey);
    }
    
    /**
     * 获取接收者公钥的十六进制字符串
     * 
     * @return 接收者公钥的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getReceiverPublicKeyHex() {
        return HashUtils.toHexString(receiverPublicKey);
    }
    
    /**
     * 获取发送者公钥的十六进制字符串（别名方法）
     * 
     * @return 发送者公钥的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getFromPublicKey() {
        return getSenderPublicKeyHex();
    }
    
    /**
     * 获取接收者公钥的十六进制字符串（别名方法）
     * 
     * @return 接收者公钥的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getToPublicKey() {
        return getReceiverPublicKeyHex();
    }
    
    /**
     * 计算交易哈希
     * 
     * @return 32字节的交易哈希
     */
    private byte[] calculateHash() {
        byte[] data = serializeForSigning();
        return HashUtils.sha256(data);
    }
    
    /**
     * 序列化交易用于签名
     * 
     * @return 序列化后的字节数组
     */
    public byte[] serializeForSigning() {
        // 构造待签名数据（排除签名字段）
        byte[] senderKeyBytes = senderPublicKey;
        byte[] receiverKeyBytes = receiverPublicKey;
        
        // 将数值转换为字节数组
        byte[] amountBytes = longToBytes(amount);
        byte[] feeBytes = longToBytes(fee);
        byte[] timestampBytes = longToBytes(timestamp);
        byte[] nonceBytes = longToBytes(nonce);
        
        // 连接所有数据
        return HashUtils.sha256(senderKeyBytes, receiverKeyBytes, amountBytes, 
                               feeBytes, timestampBytes, nonceBytes);
    }
    
    /**
     * 验证交易格式
     * 
     * @return true如果格式正确，false否则
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isValidFormat() {
        try {
            // 检查基本字段
            if (senderPublicKey == null || receiverPublicKey == null) {
                return false;
            }
            if (amount < 0 || fee < 0) {
                return false;
            }
            if (timestamp <= 0 || nonce < 0) {
                return false;
            }
            if (signature == null || signature.length != 64) {
                return false;
            }
            if (hash == null || hash.length != 32) {
                return false;
            }
            
            // 检查哈希是否正确
            byte[] calculatedHash = calculateHash();
            if (!Arrays.equals(hash, calculatedHash)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证交易签名
     * 
     * @return true如果签名有效，false否则
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean verifySignature() {
        try {
            // 使用发送者公钥验证签名
            byte[] dataToVerify = serializeForSigning();
            return Ed25519KeyPair.verify(senderPublicKey, dataToVerify, signature);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查交易是否过期
     * 
     * @param maxAgeMs 最大年龄（毫秒）
     * @return true如果过期，false否则
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isExpired(long maxAgeMs) {
        long currentTime = System.currentTimeMillis();
        return currentTime - timestamp > maxAgeMs;
    }
    
    /**
     * 获取交易总价值（金额 + 手续费）
     * 
     * @return 总价值
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public long getTotalValue() {
        return amount + fee;
    }
    
    /**
     * 将long值转换为8字节数组（大端序）
     * 
     * @param value long值
     * @return 8字节数组
     */
    private byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Transaction that = (Transaction) obj;
        return amount == that.amount &&
               fee == that.fee &&
               timestamp == that.timestamp &&
               nonce == that.nonce &&
               Objects.equals(senderPublicKey, that.senderPublicKey) &&
               Objects.equals(receiverPublicKey, that.receiverPublicKey) &&
               Arrays.equals(signature, that.signature) &&
               Arrays.equals(hash, that.hash) &&
               Objects.equals(type, that.type);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(senderPublicKey), Arrays.hashCode(receiverPublicKey), amount, fee, 
                           timestamp, nonce, Arrays.hashCode(signature), Arrays.hashCode(hash), type);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
               "hash=" + getHashHex() +
               ", sender=" + getSenderPublicKeyHex() +
               ", receiver=" + getReceiverPublicKeyHex() +
               ", amount=" + amount +
               ", fee=" + fee +
               ", timestamp=" + timestamp +
               ", nonce=" + nonce +
               ", type=" + type +
               '}';
    }
    
    /**
     * Transaction Builder
     */
    public static class Builder {
        private byte[] senderPublicKey;
        private byte[] receiverPublicKey;
        private long amount;
        private long fee;
        private long timestamp;
        private long nonce;
        private byte[] signature;
        private TransactionType type;
        
        public Builder() {
            this.timestamp = System.currentTimeMillis();
            this.type = TransactionType.TRANSFER;
        }
        
        public Builder fromPublicKey(String publicKeyHex) {
            this.senderPublicKey = HashUtils.fromHexString(publicKeyHex);
            return this;
        }
        
        public Builder toPublicKey(String publicKeyHex) {
            this.receiverPublicKey = HashUtils.fromHexString(publicKeyHex);
            return this;
        }
        
        public Builder amount(long amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder fee(long fee) {
            this.fee = fee;
            return this;
        }
        
        public Builder nonce(long nonce) {
            this.nonce = nonce;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }
        
        public Builder signature(String signatureHex) {
            this.signature = HashUtils.fromHexString(signatureHex);
            return this;
        }
        
        public Builder signature(byte[] signature) {
            this.signature = signature.clone();
            return this;
        }
        
        public Transaction build() {
            if (signature == null) {
                // 创建空签名，实际使用时需要重新签名
                signature = new byte[64];
            }
            return new Transaction(senderPublicKey, receiverPublicKey, amount, fee, 
                                 timestamp, nonce, signature, type);
        }
    }
    
    /**
     * 创建Builder实例
     * 
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }
}
