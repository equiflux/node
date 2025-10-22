package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * VRF公告DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class VrfAnnouncementDto {
    
    @JsonProperty("node_id")
    private String nodeId;
    
    @JsonProperty("public_key")
    private String publicKey;
    
    @JsonProperty("vrf_output")
    private String vrfOutput;
    
    @JsonProperty("vrf_proof")
    private String vrfProof;
    
    @JsonProperty("round")
    private long round;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("score")
    private double score;
    
    // Getters and Setters
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    
    public String getVrfOutput() { return vrfOutput; }
    public void setVrfOutput(String vrfOutput) { this.vrfOutput = vrfOutput; }
    
    public String getVrfProof() { return vrfProof; }
    public void setVrfProof(String vrfProof) { this.vrfProof = vrfProof; }
    
    public long getRound() { return round; }
    public void setRound(long round) { this.round = round; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
