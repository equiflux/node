package io.equiflux.node.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Objects;

/**
 * 节点信息
 * 
 * <p>表示网络中的一个节点，包含节点的网络地址、公钥、状态等信息。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class PeerInfo {
    
    /**
     * 节点状态枚举
     */
    public enum PeerStatus {
        CONNECTING("connecting"),
        CONNECTED("connected"),
        DISCONNECTED("disconnected"),
        FAILED("failed");
        
        private final String value;
        
        PeerStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static PeerStatus fromValue(String value) {
            for (PeerStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown peer status: " + value);
        }
    }
    
    private final String nodeId;
    private final PublicKey publicKey;
    private final String host;
    private final int port;
    private final PeerStatus status;
    private final long lastSeen;
    private final int connectionAttempts;
    private final long lastConnectionAttempt;
    
    /**
     * 构造节点信息
     * 
     * @param nodeId 节点ID
     * @param publicKey 节点公钥
     * @param host 主机地址
     * @param port 端口号
     * @param status 节点状态
     * @param lastSeen 最后见到时间
     * @param connectionAttempts 连接尝试次数
     * @param lastConnectionAttempt 最后连接尝试时间
     */
    @JsonCreator
    public PeerInfo(@JsonProperty("nodeId") String nodeId,
                    @JsonProperty("publicKey") PublicKey publicKey,
                    @JsonProperty("host") String host,
                    @JsonProperty("port") int port,
                    @JsonProperty("status") PeerStatus status,
                    @JsonProperty("lastSeen") long lastSeen,
                    @JsonProperty("connectionAttempts") int connectionAttempts,
                    @JsonProperty("lastConnectionAttempt") long lastConnectionAttempt) {
        this.nodeId = Objects.requireNonNull(nodeId, "Node ID cannot be null");
        this.publicKey = Objects.requireNonNull(publicKey, "Public key cannot be null");
        this.host = Objects.requireNonNull(host, "Host cannot be null");
        this.port = port;
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.lastSeen = lastSeen;
        this.connectionAttempts = connectionAttempts;
        this.lastConnectionAttempt = lastConnectionAttempt;
        
        // 验证参数
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if (lastSeen < 0) {
            throw new IllegalArgumentException("Last seen cannot be negative");
        }
        if (connectionAttempts < 0) {
            throw new IllegalArgumentException("Connection attempts cannot be negative");
        }
        if (lastConnectionAttempt < 0) {
            throw new IllegalArgumentException("Last connection attempt cannot be negative");
        }
    }
    
    /**
     * 获取节点ID
     * 
     * @return 节点ID
     */
    public String getNodeId() {
        return nodeId;
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
     * 获取主机地址
     * 
     * @return 主机地址
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 获取端口号
     * 
     * @return 端口号
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 获取节点状态
     * 
     * @return 节点状态
     */
    public PeerStatus getStatus() {
        return status;
    }
    
    /**
     * 获取最后见到时间
     * 
     * @return 最后见到时间
     */
    public long getLastSeen() {
        return lastSeen;
    }
    
    /**
     * 获取连接尝试次数
     * 
     * @return 连接尝试次数
     */
    public int getConnectionAttempts() {
        return connectionAttempts;
    }
    
    /**
     * 获取最后连接尝试时间
     * 
     * @return 最后连接尝试时间
     */
    public long getLastConnectionAttempt() {
        return lastConnectionAttempt;
    }
    
    /**
     * 获取网络地址
     * 
     * @return InetSocketAddress对象
     */
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(host, port);
    }
    
    /**
     * 获取节点地址字符串
     * 
     * @return 格式为 "host:port" 的地址字符串
     */
    public String getAddressString() {
        return host + ":" + port;
    }
    
    /**
     * 检查节点是否在线
     * 
     * @return true如果在线，false否则
     */
    public boolean isOnline() {
        return status == PeerStatus.CONNECTED;
    }
    
    /**
     * 检查节点是否可以连接
     * 
     * @return true如果可以连接，false否则
     */
    public boolean canConnect() {
        return status == PeerStatus.DISCONNECTED || status == PeerStatus.FAILED;
    }
    
    /**
     * 检查节点是否已连接
     * 
     * @return true如果已连接，false否则
     */
    public boolean isConnected() {
        return status == PeerStatus.CONNECTED;
    }
    
    /**
     * 检查节点是否正在连接
     * 
     * @return true如果正在连接，false否则
     */
    public boolean isConnecting() {
        return status == PeerStatus.CONNECTING;
    }
    
    /**
     * 检查节点是否失败
     * 
     * @return true如果失败，false否则
     */
    public boolean isFailed() {
        return status == PeerStatus.FAILED;
    }
    
    /**
     * 检查节点是否过期
     * 
     * @param maxAgeMs 最大年龄（毫秒）
     * @return true如果过期，false否则
     */
    public boolean isExpired(long maxAgeMs) {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastSeen > maxAgeMs;
    }
    
    /**
     * 检查是否可以重试连接
     * 
     * @param maxAttempts 最大尝试次数
     * @param retryIntervalMs 重试间隔（毫秒）
     * @return true如果可以重试，false否则
     */
    public boolean canRetryConnection(int maxAttempts, long retryIntervalMs) {
        if (connectionAttempts >= maxAttempts) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        return currentTime - lastConnectionAttempt >= retryIntervalMs;
    }
    
    /**
     * 创建更新状态的节点信息
     * 
     * @param newStatus 新状态
     * @return 更新后的节点信息
     */
    public PeerInfo withStatus(PeerStatus newStatus) {
        return new PeerInfo(nodeId, publicKey, host, port, newStatus, lastSeen, connectionAttempts, lastConnectionAttempt);
    }
    
    /**
     * 创建更新最后见到时间的节点信息
     * 
     * @param newLastSeen 新的最后见到时间
     * @return 更新后的节点信息
     */
    public PeerInfo withLastSeen(long newLastSeen) {
        return new PeerInfo(nodeId, publicKey, host, port, status, newLastSeen, connectionAttempts, lastConnectionAttempt);
    }
    
    /**
     * 创建增加连接尝试次数的节点信息
     * 
     * @return 更新后的节点信息
     */
    public PeerInfo withIncrementedConnectionAttempts() {
        long currentTime = System.currentTimeMillis();
        return new PeerInfo(nodeId, publicKey, host, port, status, lastSeen, 
                          connectionAttempts + 1, currentTime);
    }
    
    /**
     * 创建重置连接尝试次数的节点信息
     * 
     * @return 更新后的节点信息
     */
    public PeerInfo withResetConnectionAttempts() {
        return new PeerInfo(nodeId, publicKey, host, port, status, lastSeen, 0, lastConnectionAttempt);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PeerInfo peerInfo = (PeerInfo) obj;
        return port == peerInfo.port &&
               lastSeen == peerInfo.lastSeen &&
               connectionAttempts == peerInfo.connectionAttempts &&
               lastConnectionAttempt == peerInfo.lastConnectionAttempt &&
               Objects.equals(nodeId, peerInfo.nodeId) &&
               Objects.equals(publicKey, peerInfo.publicKey) &&
               Objects.equals(host, peerInfo.host) &&
               status == peerInfo.status;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nodeId, publicKey, host, port, status, lastSeen, connectionAttempts, lastConnectionAttempt);
    }
    
    @Override
    public String toString() {
        return "PeerInfo{" +
               "nodeId='" + nodeId + '\'' +
               ", host='" + host + '\'' +
               ", port=" + port +
               ", status=" + status +
               ", lastSeen=" + lastSeen +
               ", connectionAttempts=" + connectionAttempts +
               '}';
    }
}
