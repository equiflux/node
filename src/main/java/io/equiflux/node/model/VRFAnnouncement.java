package io.equiflux.node.model;

import io.equiflux.node.crypto.HashUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.PublicKey;
import java.util.Objects;

/**
 * VRF公告
 * 
 * <p>包含节点在共识轮次中广播的VRF信息，用于出块者选择和奖励分配。
 * 
 * <p>每个超级节点在每个轮次中都会：
 * <ol>
 *   <li>计算自己的VRF输出</li>
 *   <li>生成VRF证明</li>
 *   <li>计算自己的分数</li>
 *   <li>广播VRF公告</li>
 * </ol>
 * 
 * <p>VRF公告包含以下信息：
 * <ul>
 *   <li>轮次号</li>
 *   <li>节点公钥</li>
 *   <li>VRF输出</li>
 *   <li>VRF证明</li>
 *   <li>计算得出的分数</li>
 *   <li>时间戳</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class VRFAnnouncement {
    
    private final long round;
    private final PublicKey publicKey;
    private final VRFOutput vrfOutput;
    private final VRFProof vrfProof;
    private final double score;
    private final long timestamp;
    
    /**
     * 构造VRF公告
     * 
     * @param round 轮次号
     * @param publicKey 节点公钥
     * @param vrfOutput VRF输出
     * @param vrfProof VRF证明
     * @param score 计算得出的分数
     */
    @JsonCreator
    public VRFAnnouncement(@JsonProperty("round") long round,
                           @JsonProperty("publicKey") PublicKey publicKey,
                           @JsonProperty("vrfOutput") VRFOutput vrfOutput,
                           @JsonProperty("vrfProof") VRFProof vrfProof,
                           @JsonProperty("score") double score) {
        this.round = round;
        this.publicKey = Objects.requireNonNull(publicKey, "Public key cannot be null");
        this.vrfOutput = Objects.requireNonNull(vrfOutput, "VRF output cannot be null");
        this.vrfProof = Objects.requireNonNull(vrfProof, "VRF proof cannot be null");
        this.score = score;
        this.timestamp = System.currentTimeMillis();
        
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 1.0");
        }
    }
    
    /**
     * 构造VRF公告（带自定义时间戳）
     * 
     * @param round 轮次号
     * @param publicKey 节点公钥
     * @param vrfOutput VRF输出
     * @param vrfProof VRF证明
     * @param score 计算得出的分数
     * @param timestamp 时间戳
     */
    public VRFAnnouncement(long round, PublicKey publicKey, VRFOutput vrfOutput, 
                          VRFProof vrfProof, double score, long timestamp) {
        this.round = round;
        this.publicKey = Objects.requireNonNull(publicKey, "Public key cannot be null");
        this.vrfOutput = Objects.requireNonNull(vrfOutput, "VRF output cannot be null");
        this.vrfProof = Objects.requireNonNull(vrfProof, "VRF proof cannot be null");
        this.score = score;
        this.timestamp = timestamp;
        
        if (score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 1.0");
        }
    }
    
    /**
     * 获取轮次号
     * 
     * @return 轮次号
     */
    public long getRound() {
        return round;
    }
    
    /**
     * 获取节点公钥
     * 
     * @return 节点公钥
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    /**
     * 获取VRF输出
     * 
     * @return VRF输出
     */
    public VRFOutput getVrfOutput() {
        return vrfOutput;
    }
    
    /**
     * 获取VRF证明
     * 
     * @return VRF证明
     */
    public VRFProof getVrfProof() {
        return vrfProof;
    }
    
    /**
     * 获取计算得出的分数
     * 
     * @return 分数（0.0-1.0）
     */
    public double getScore() {
        return score;
    }
    
    /**
     * 获取时间戳
     * 
     * @return 时间戳（毫秒）
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取公钥的十六进制字符串表示
     * 
     * @return 公钥的十六进制字符串
     */
    public String getPublicKeyHex() {
        return HashUtils.toHexString(publicKey.getEncoded());
    }
    
    /**
     * 检查公告是否过期
     * 
     * @param timeoutMs 超时时间（毫秒）
     * @return true如果过期，false否则
     */
    public boolean isExpired(long timeoutMs) {
        return System.currentTimeMillis() - timestamp > timeoutMs;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VRFAnnouncement that = (VRFAnnouncement) obj;
        return round == that.round &&
               Double.compare(that.score, score) == 0 &&
               timestamp == that.timestamp &&
               Objects.equals(publicKey, that.publicKey) &&
               Objects.equals(vrfOutput, that.vrfOutput) &&
               Objects.equals(vrfProof, that.vrfProof);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(round, publicKey, vrfOutput, vrfProof, score, timestamp);
    }
    
    @Override
    public String toString() {
        return "VRFAnnouncement{" +
               "round=" + round +
               ", publicKey=" + getPublicKeyHex() +
               ", score=" + score +
               ", timestamp=" + timestamp +
               '}';
    }
}
