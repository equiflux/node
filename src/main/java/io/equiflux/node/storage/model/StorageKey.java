package io.equiflux.node.storage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 存储键
 * 
 * <p>表示存储系统中的键，用于标识不同的数据项。
 * 
 * <p>存储键结构：
 * <ul>
 *   <li>命名空间（如 "block", "transaction", "account"）</li>
 *   <li>键值（具体的标识符）</li>
 *   <li>版本号（可选，用于版本控制）</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class StorageKey {
    
    private final String namespace;
    private final String key;
    private final long version;
    
    /**
     * 构造存储键
     * 
     * @param namespace 命名空间
     * @param key 键值
     * @param version 版本号
     */
    @JsonCreator
    public StorageKey(@JsonProperty("namespace") String namespace,
                      @JsonProperty("key") String key,
                      @JsonProperty("version") long version) {
        this.namespace = Objects.requireNonNull(namespace, "Namespace cannot be null");
        this.key = Objects.requireNonNull(key, "Key cannot be null");
        this.version = version;
        
        if (namespace.trim().isEmpty()) {
            throw new IllegalArgumentException("Namespace cannot be empty");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be empty");
        }
        if (version < 0) {
            throw new IllegalArgumentException("Version cannot be negative");
        }
    }
    
    /**
     * 构造存储键（版本号为0）
     * 
     * @param namespace 命名空间
     * @param key 键值
     */
    public StorageKey(String namespace, String key) {
        this(namespace, key, 0);
    }
    
    /**
     * 获取命名空间
     * 
     * @return 命名空间
     */
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * 获取键值
     * 
     * @return 键值
     */
    public String getKey() {
        return key;
    }
    
    /**
     * 获取版本号
     * 
     * @return 版本号
     */
    public long getVersion() {
        return version;
    }
    
    /**
     * 创建新版本的存储键
     * 
     * @return 新版本的存储键
     */
    public StorageKey nextVersion() {
        return new StorageKey(namespace, key, version + 1);
    }
    
    /**
     * 创建指定版本的存储键
     * 
     * @param newVersion 新版本号
     * @return 指定版本的存储键
     */
    public StorageKey withVersion(long newVersion) {
        return new StorageKey(namespace, key, newVersion);
    }
    
    /**
     * 获取完整的键字符串
     * 
     * @return 完整的键字符串
     */
    public String getFullKey() {
        if (version == 0) {
            return namespace + ":" + key;
        } else {
            return namespace + ":" + key + ":" + version;
        }
    }
    
    /**
     * 从完整键字符串解析存储键
     * 
     * @param fullKey 完整的键字符串
     * @return 存储键
     */
    public static StorageKey fromFullKey(String fullKey) {
        if (fullKey == null || fullKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Full key cannot be null or empty");
        }
        
        String[] parts = fullKey.split(":");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid full key format");
        }
        
        String namespace = parts[0];
        String key = parts[1];
        long version = 0;
        
        if (parts.length > 2) {
            try {
                version = Long.parseLong(parts[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid version format", e);
            }
        }
        
        return new StorageKey(namespace, key, version);
    }
    
    /**
     * 创建区块存储键
     * 
     * @param height 区块高度
     * @return 区块存储键
     */
    public static StorageKey blockKey(long height) {
        return new StorageKey("block", String.valueOf(height));
    }
    
    /**
     * 创建区块哈希存储键
     * 
     * @param hash 区块哈希
     * @return 区块哈希存储键
     */
    public static StorageKey blockHashKey(String hash) {
        return new StorageKey("block_hash", hash);
    }
    
    /**
     * 创建交易存储键
     * 
     * @param hash 交易哈希
     * @return 交易存储键
     */
    public static StorageKey transactionKey(String hash) {
        return new StorageKey("transaction", hash);
    }
    
    /**
     * 创建账户存储键
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户存储键
     */
    public static StorageKey accountKey(String publicKeyHex) {
        return new StorageKey("account", publicKeyHex);
    }
    
    /**
     * 创建链状态存储键
     * 
     * @return 链状态存储键
     */
    public static StorageKey chainStateKey() {
        return new StorageKey("chain", "state");
    }
    
    /**
     * 创建交易池存储键
     * 
     * @param hash 交易哈希
     * @return 交易池存储键
     */
    public static StorageKey transactionPoolKey(String hash) {
        return new StorageKey("tx_pool", hash);
    }
    
    /**
     * 创建VRF公告存储键
     * 
     * @param round 轮次
     * @param publicKeyHex 公钥十六进制字符串
     * @return VRF公告存储键
     */
    public static StorageKey vrfAnnouncementKey(long round, String publicKeyHex) {
        return new StorageKey("vrf", round + ":" + publicKeyHex);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StorageKey that = (StorageKey) obj;
        return version == that.version &&
               Objects.equals(namespace, that.namespace) &&
               Objects.equals(key, that.key);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(namespace, key, version);
    }
    
    @Override
    public String toString() {
        return "StorageKey{" +
               "namespace='" + namespace + '\'' +
               ", key='" + key + '\'' +
               ", version=" + version +
               '}';
    }
}
