package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

/**
 * 网络统计信息DTO
 * 
 * <p>用于RPC接口返回网络统计信息，包含节点数量、连接状态等。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class NetworkStatsDto {
    
    @JsonProperty("totalPeers")
    private Integer totalPeers;
    
    @JsonProperty("connectedPeers")
    private Integer connectedPeers;
    
    @JsonProperty("activePeers")
    private Integer activePeers;
    
    @JsonProperty("incomingConnections")
    private Integer incomingConnections;
    
    @JsonProperty("outgoingConnections")
    private Integer outgoingConnections;
    
    @JsonProperty("messagesSent")
    private Long messagesSent;
    
    @JsonProperty("messagesReceived")
    private Long messagesReceived;
    
    @JsonProperty("bytesSent")
    private Long bytesSent;
    
    @JsonProperty("bytesReceived")
    private Long bytesReceived;
    
    @JsonProperty("uptime")
    private Long uptime;
    
    @JsonProperty("lastMessageTime")
    private Long lastMessageTime;
    
    @JsonProperty("averageLatency")
    private Double averageLatency;
    
    @JsonProperty("networkVersion")
    private String networkVersion;
    
    @JsonProperty("protocolVersion")
    private String protocolVersion;
    
    /**
     * 默认构造函数
     */
    public NetworkStatsDto() {
    }
    
    /**
     * 构造函数
     * 
     * @param totalPeers 总节点数
     * @param connectedPeers 已连接节点数
     * @param activePeers 活跃节点数
     */
    public NetworkStatsDto(Integer totalPeers, Integer connectedPeers, Integer activePeers) {
        this.totalPeers = totalPeers;
        this.connectedPeers = connectedPeers;
        this.activePeers = activePeers;
    }
    
    // Getters and Setters
    
    public Integer getTotalPeers() {
        return totalPeers;
    }
    
    public void setTotalPeers(Integer totalPeers) {
        this.totalPeers = totalPeers;
    }
    
    public Integer getConnectedPeers() {
        return connectedPeers;
    }
    
    public void setConnectedPeers(Integer connectedPeers) {
        this.connectedPeers = connectedPeers;
    }
    
    public Integer getActivePeers() {
        return activePeers;
    }
    
    public void setActivePeers(Integer activePeers) {
        this.activePeers = activePeers;
    }
    
    public Integer getIncomingConnections() {
        return incomingConnections;
    }
    
    public void setIncomingConnections(Integer incomingConnections) {
        this.incomingConnections = incomingConnections;
    }
    
    public Integer getOutgoingConnections() {
        return outgoingConnections;
    }
    
    public void setOutgoingConnections(Integer outgoingConnections) {
        this.outgoingConnections = outgoingConnections;
    }
    
    public Long getMessagesSent() {
        return messagesSent;
    }
    
    public void setMessagesSent(Long messagesSent) {
        this.messagesSent = messagesSent;
    }
    
    public Long getMessagesReceived() {
        return messagesReceived;
    }
    
    public void setMessagesReceived(Long messagesReceived) {
        this.messagesReceived = messagesReceived;
    }
    
    public Long getBytesSent() {
        return bytesSent;
    }
    
    public void setBytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
    }
    
    public Long getBytesReceived() {
        return bytesReceived;
    }
    
    public void setBytesReceived(Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }
    
    public Long getUptime() {
        return uptime;
    }
    
    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }
    
    public Long getLastMessageTime() {
        return lastMessageTime;
    }
    
    public void setLastMessageTime(Long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
    
    public Double getAverageLatency() {
        return averageLatency;
    }
    
    public void setAverageLatency(Double averageLatency) {
        this.averageLatency = averageLatency;
    }
    
    public String getNetworkVersion() {
        return networkVersion;
    }
    
    public void setNetworkVersion(String networkVersion) {
        this.networkVersion = networkVersion;
    }
    
    public String getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    @Override
    public String toString() {
        return "NetworkStatsDto{" +
                "totalPeers=" + totalPeers +
                ", connectedPeers=" + connectedPeers +
                ", activePeers=" + activePeers +
                ", messagesSent=" + messagesSent +
                ", messagesReceived=" + messagesReceived +
                ", uptime=" + uptime +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NetworkStatsDto that = (NetworkStatsDto) obj;
        return Objects.equals(totalPeers, that.totalPeers) &&
               Objects.equals(connectedPeers, that.connectedPeers) &&
               Objects.equals(activePeers, that.activePeers) &&
               Objects.equals(incomingConnections, that.incomingConnections) &&
               Objects.equals(outgoingConnections, that.outgoingConnections) &&
               Objects.equals(messagesSent, that.messagesSent) &&
               Objects.equals(messagesReceived, that.messagesReceived) &&
               Objects.equals(bytesSent, that.bytesSent) &&
               Objects.equals(bytesReceived, that.bytesReceived) &&
               Objects.equals(uptime, that.uptime) &&
               Objects.equals(lastMessageTime, that.lastMessageTime) &&
               Objects.equals(averageLatency, that.averageLatency) &&
               Objects.equals(networkVersion, that.networkVersion) &&
               Objects.equals(protocolVersion, that.protocolVersion);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalPeers, connectedPeers, activePeers, incomingConnections, 
                           outgoingConnections, messagesSent, messagesReceived, 
                           bytesSent, bytesReceived, uptime, lastMessageTime, 
                           averageLatency, networkVersion, protocolVersion);
    }
}
