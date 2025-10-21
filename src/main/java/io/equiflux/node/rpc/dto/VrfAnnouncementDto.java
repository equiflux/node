package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * VRF公告DTO
 * 
 * <p>用于RPC接口返回VRF公告信息，包含VRF输出和证明。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class VrfAnnouncementDto {
    
    @NotBlank(message = "Node ID is required")
    @JsonProperty("nodeId")
    private String nodeId;
    
    @JsonProperty("publicKey")
    private String publicKey;
    
    @JsonProperty("vrfOutput")
    private String vrfOutput;
    
    @JsonProperty("vrfProof")
    private String vrfProof;
    
    @JsonProperty("round")
    private Long round;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("signature")
    private String signature;
    
    @JsonProperty("score")
    private Double score;
    
    @JsonProperty("stakeWeight")
    private Double stakeWeight;
    
    @JsonProperty("performanceFactor")
    private Double performanceFactor;
    
    /**
     * 默认构造函数
     */
    public VrfAnnouncementDto() {
    }
    
    /**
     * 构造函数
     * 
     * @param nodeId 节点ID
     * @param publicKey 公钥
     * @param vrfOutput VRF输出
     * @param vrfProof VRF证明
     * @param round 轮次
     */
    public VrfAnnouncementDto(String nodeId, String publicKey, String vrfOutput, 
                             String vrfProof, Long round) {
        this.nodeId = nodeId;
        this.publicKey = publicKey;
        this.vrfOutput = vrfOutput;
        this.vrfProof = vrfProof;
        this.round = round;
    }
    
    // Getters and Setters
    
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public String getVrfOutput() {
        return vrfOutput;
    }
    
    public void setVrfOutput(String vrfOutput) {
        this.vrfOutput = vrfOutput;
    }
    
    public String getVrfProof() {
        return vrfProof;
    }
    
    public void setVrfProof(String vrfProof) {
        this.vrfProof = vrfProof;
    }
    
    public Long getRound() {
        return round;
    }
    
    public void setRound(Long round) {
        this.round = round;
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
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public Double getStakeWeight() {
        return stakeWeight;
    }
    
    public void setStakeWeight(Double stakeWeight) {
        this.stakeWeight = stakeWeight;
    }
    
    public Double getPerformanceFactor() {
        return performanceFactor;
    }
    
    public void setPerformanceFactor(Double performanceFactor) {
        this.performanceFactor = performanceFactor;
    }
    
    @Override
    public String toString() {
        return "VrfAnnouncementDto{" +
                "nodeId='" + nodeId + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", round=" + round +
                ", score=" + score +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VrfAnnouncementDto that = (VrfAnnouncementDto) obj;
        return Objects.equals(nodeId, that.nodeId) &&
               Objects.equals(publicKey, that.publicKey) &&
               Objects.equals(vrfOutput, that.vrfOutput) &&
               Objects.equals(vrfProof, that.vrfProof) &&
               Objects.equals(round, that.round) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(signature, that.signature) &&
               Objects.equals(score, that.score) &&
               Objects.equals(stakeWeight, that.stakeWeight) &&
               Objects.equals(performanceFactor, that.performanceFactor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nodeId, publicKey, vrfOutput, vrfProof, round, 
                           timestamp, signature, score, stakeWeight, performanceFactor);
    }
}
