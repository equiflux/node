package io.equiflux.node.explorer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 网络统计信息DTO
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class NetworkStatsDto {
    
    @JsonProperty("total_peers")
    private int totalPeers;
    
    @JsonProperty("connected_peers")
    private int connectedPeers;
    
    @JsonProperty("active_peers")
    private int activePeers;
    
    @JsonProperty("uptime")
    private long uptime;
    
    @JsonProperty("network_version")
    private String networkVersion;
    
    @JsonProperty("protocol_version")
    private String protocolVersion;
    
    // Getters and Setters
    public int getTotalPeers() { return totalPeers; }
    public void setTotalPeers(int totalPeers) { this.totalPeers = totalPeers; }
    
    public int getConnectedPeers() { return connectedPeers; }
    public void setConnectedPeers(int connectedPeers) { this.connectedPeers = connectedPeers; }
    
    public int getActivePeers() { return activePeers; }
    public void setActivePeers(int activePeers) { this.activePeers = activePeers; }
    
    public long getUptime() { return uptime; }
    public void setUptime(long uptime) { this.uptime = uptime; }
    
    public String getNetworkVersion() { return networkVersion; }
    public void setNetworkVersion(String networkVersion) { this.networkVersion = networkVersion; }
    
    public String getProtocolVersion() { return protocolVersion; }
    public void setProtocolVersion(String protocolVersion) { this.protocolVersion = protocolVersion; }
}
