package io.equiflux.node.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 网络统计信息
 * 
 * <p>包含网络运行的各种统计信息，用于监控和调试。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class NetworkStats {
    
    private final long messagesSent;
    private final long messagesReceived;
    private final long bytesSent;
    private final long bytesReceived;
    private final int connectedPeers;
    private final int totalPeers;
    private final long uptimeMs;
    private final long lastActivityTime;
    
    /**
     * 构造网络统计信息
     * 
     * @param messagesSent 发送消息数
     * @param messagesReceived 接收消息数
     * @param bytesSent 发送字节数
     * @param bytesReceived 接收字节数
     * @param connectedPeers 连接节点数
     * @param totalPeers 总节点数
     * @param uptimeMs 运行时间（毫秒）
     * @param lastActivityTime 最后活动时间
     */
    @JsonCreator
    public NetworkStats(@JsonProperty("messagesSent") long messagesSent,
                       @JsonProperty("messagesReceived") long messagesReceived,
                       @JsonProperty("bytesSent") long bytesSent,
                       @JsonProperty("bytesReceived") long bytesReceived,
                       @JsonProperty("connectedPeers") int connectedPeers,
                       @JsonProperty("totalPeers") int totalPeers,
                       @JsonProperty("uptimeMs") long uptimeMs,
                       @JsonProperty("lastActivityTime") long lastActivityTime) {
        this.messagesSent = messagesSent;
        this.messagesReceived = messagesReceived;
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.connectedPeers = connectedPeers;
        this.totalPeers = totalPeers;
        this.uptimeMs = uptimeMs;
        this.lastActivityTime = lastActivityTime;
        
        // 验证参数
        if (messagesSent < 0) {
            throw new IllegalArgumentException("Messages sent cannot be negative");
        }
        if (messagesReceived < 0) {
            throw new IllegalArgumentException("Messages received cannot be negative");
        }
        if (bytesSent < 0) {
            throw new IllegalArgumentException("Bytes sent cannot be negative");
        }
        if (bytesReceived < 0) {
            throw new IllegalArgumentException("Bytes received cannot be negative");
        }
        if (connectedPeers < 0) {
            throw new IllegalArgumentException("Connected peers cannot be negative");
        }
        if (totalPeers < 0) {
            throw new IllegalArgumentException("Total peers cannot be negative");
        }
        if (uptimeMs < 0) {
            throw new IllegalArgumentException("Uptime cannot be negative");
        }
        if (lastActivityTime < 0) {
            throw new IllegalArgumentException("Last activity time cannot be negative");
        }
    }
    
    /**
     * 获取发送消息数
     * 
     * @return 发送消息数
     */
    public long getMessagesSent() {
        return messagesSent;
    }
    
    /**
     * 获取接收消息数
     * 
     * @return 接收消息数
     */
    public long getMessagesReceived() {
        return messagesReceived;
    }
    
    /**
     * 获取发送字节数
     * 
     * @return 发送字节数
     */
    public long getBytesSent() {
        return bytesSent;
    }
    
    /**
     * 获取接收字节数
     * 
     * @return 接收字节数
     */
    public long getBytesReceived() {
        return bytesReceived;
    }
    
    /**
     * 获取连接节点数
     * 
     * @return 连接节点数
     */
    public int getConnectedPeers() {
        return connectedPeers;
    }
    
    /**
     * 获取总节点数
     * 
     * @return 总节点数
     */
    public int getTotalPeers() {
        return totalPeers;
    }
    
    /**
     * 获取运行时间（毫秒）
     * 
     * @return 运行时间
     */
    public long getUptimeMs() {
        return uptimeMs;
    }
    
    /**
     * 获取最后活动时间
     * 
     * @return 最后活动时间
     */
    public long getLastActivityTime() {
        return lastActivityTime;
    }
    
    /**
     * 获取总消息数
     * 
     * @return 总消息数
     */
    public long getTotalMessages() {
        return messagesSent + messagesReceived;
    }
    
    /**
     * 获取总字节数
     * 
     * @return 总字节数
     */
    public long getTotalBytes() {
        return bytesSent + bytesReceived;
    }
    
    /**
     * 获取消息发送速率（消息/秒）
     * 
     * @return 消息发送速率
     */
    public double getMessagesSentPerSecond() {
        if (uptimeMs == 0) {
            return 0.0;
        }
        return (double) messagesSent / (uptimeMs / 1000.0);
    }
    
    /**
     * 获取消息接收速率（消息/秒）
     * 
     * @return 消息接收速率
     */
    public double getMessagesReceivedPerSecond() {
        if (uptimeMs == 0) {
            return 0.0;
        }
        return (double) messagesReceived / (uptimeMs / 1000.0);
    }
    
    /**
     * 获取字节发送速率（字节/秒）
     * 
     * @return 字节发送速率
     */
    public double getBytesSentPerSecond() {
        if (uptimeMs == 0) {
            return 0.0;
        }
        return (double) bytesSent / (uptimeMs / 1000.0);
    }
    
    /**
     * 获取字节接收速率（字节/秒）
     * 
     * @return 字节接收速率
     */
    public double getBytesReceivedPerSecond() {
        if (uptimeMs == 0) {
            return 0.0;
        }
        return (double) bytesReceived / (uptimeMs / 1000.0);
    }
    
    /**
     * 获取连接率
     * 
     * @return 连接率（0.0-1.0）
     */
    public double getConnectionRate() {
        if (totalPeers == 0) {
            return 0.0;
        }
        return (double) connectedPeers / totalPeers;
    }
    
    /**
     * 获取平均消息大小（字节）
     * 
     * @return 平均消息大小
     */
    public double getAverageMessageSize() {
        long totalMessages = getTotalMessages();
        if (totalMessages == 0) {
            return 0.0;
        }
        return (double) getTotalBytes() / totalMessages;
    }
    
    /**
     * 检查是否有活动
     * 
     * @param maxIdleTimeMs 最大空闲时间（毫秒）
     * @return true如果有活动，false否则
     */
    public boolean hasActivity(long maxIdleTimeMs) {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastActivityTime <= maxIdleTimeMs;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NetworkStats that = (NetworkStats) obj;
        return messagesSent == that.messagesSent &&
               messagesReceived == that.messagesReceived &&
               bytesSent == that.bytesSent &&
               bytesReceived == that.bytesReceived &&
               connectedPeers == that.connectedPeers &&
               totalPeers == that.totalPeers &&
               uptimeMs == that.uptimeMs &&
               lastActivityTime == that.lastActivityTime;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(messagesSent, messagesReceived, bytesSent, bytesReceived,
                           connectedPeers, totalPeers, uptimeMs, lastActivityTime);
    }
    
    @Override
    public String toString() {
        return "NetworkStats{" +
               "messagesSent=" + messagesSent +
               ", messagesReceived=" + messagesReceived +
               ", bytesSent=" + bytesSent +
               ", bytesReceived=" + bytesReceived +
               ", connectedPeers=" + connectedPeers +
               ", totalPeers=" + totalPeers +
               ", uptimeMs=" + uptimeMs +
               ", lastActivityTime=" + lastActivityTime +
               '}';
    }
}
