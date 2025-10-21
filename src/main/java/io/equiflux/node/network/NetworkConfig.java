package io.equiflux.node.network;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// import lombok.Data;

/**
 * 网络配置
 * 
 * <p>网络层的配置参数，包括端口、超时、重试等设置。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
// @Data
@Component
@ConfigurationProperties(prefix = "equiflux.network")
public class NetworkConfig {
    
    /**
     * 监听端口
     */
    private int port = 8080;
    
    /**
     * 最大连接数
     */
    private int maxConnections = 100;
    
    /**
     * 连接超时时间（毫秒）
     */
    private long connectionTimeoutMs = 30000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private long readTimeoutMs = 60000;
    
    /**
     * 写入超时时间（毫秒）
     */
    private long writeTimeoutMs = 30000;
    
    /**
     * 心跳间隔（毫秒）
     */
    private long heartbeatIntervalMs = 30000;
    
    /**
     * 心跳超时时间（毫秒）
     */
    private long heartbeatTimeoutMs = 90000;
    
    /**
     * 消息最大大小（字节）
     */
    private int maxMessageSize = 1024 * 1024; // 1MB
    
    /**
     * 缓冲区大小（字节）
     */
    private int bufferSize = 64 * 1024; // 64KB
    
    /**
     * 是否启用TCP_NODELAY
     */
    private boolean tcpNoDelay = true;
    
    /**
     * 是否启用SO_KEEPALIVE
     */
    private boolean keepAlive = true;
    
    /**
     * SO_REUSEADDR选项
     */
    private boolean reuseAddress = true;
    
    /**
     * 最大重试次数
     */
    private int maxRetryAttempts = 3;
    
    /**
     * 重试间隔（毫秒）
     */
    private long retryIntervalMs = 5000;
    
    /**
     * 节点发现间隔（毫秒）
     */
    private long peerDiscoveryIntervalMs = 60000;
    
    /**
     * 最大节点数
     */
    private int maxPeers = 50;
    
    /**
     * 最小节点数
     */
    private int minPeers = 5;
    
    /**
     * 节点过期时间（毫秒）
     */
    private long peerExpirationMs = 300000; // 5分钟
    
    /**
     * 是否启用自动重连
     */
    private boolean autoReconnect = true;
    
    /**
     * 重连间隔（毫秒）
     */
    private long reconnectIntervalMs = 10000;
    
    /**
     * 是否启用消息压缩
     */
    private boolean enableCompression = true;
    
    /**
     * 压缩级别（1-9）
     */
    private int compressionLevel = 6;
    
    /**
     * 是否启用消息加密
     */
    private boolean enableEncryption = true;
    
    /**
     * 是否启用消息签名验证
     */
    private boolean enableSignatureVerification = true;
    
    /**
     * 消息过期时间（毫秒）
     */
    private long messageExpirationMs = 300000; // 5分钟
    
    /**
     * 是否启用消息去重
     */
    private boolean enableMessageDeduplication = true;
    
    /**
     * 消息去重缓存大小
     */
    private int messageDeduplicationCacheSize = 10000;
    
    /**
     * 是否启用网络监控
     */
    private boolean enableMonitoring = true;
    
    /**
     * 监控指标收集间隔（毫秒）
     */
    private long monitoringIntervalMs = 10000;
    
    /**
     * 是否启用调试日志
     */
    private boolean enableDebugLogging = false;
    
    /**
     * 是否启用性能统计
     */
    private boolean enablePerformanceStats = true;
    
    /**
     * 工作线程数
     */
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    /**
     * Boss线程数
     */
    private int bossThreads = 1;
    
    /**
     * 是否启用Epoll（Linux）
     */
    private boolean useEpoll = true;
    
    /**
     * 是否启用NIO
     */
    private boolean useNio = true;
    
    /**
     * 是否启用零拷贝
     */
    private boolean useZeroCopy = true;
    
    /**
     * 是否启用直接内存
     */
    private boolean useDirectMemory = true;
    
    /**
     * 最大直接内存使用量（字节）
     */
    private long maxDirectMemory = 256 * 1024 * 1024; // 256MB
    
    /**
     * 是否启用内存池
     */
    private boolean enableMemoryPool = true;
    
    /**
     * 内存池大小（字节）
     */
    private int memoryPoolSize = 64 * 1024 * 1024; // 64MB
    
    /**
     * 是否启用对象池
     */
    private boolean enableObjectPool = true;
    
    /**
     * 对象池大小
     */
    private int objectPoolSize = 1000;
    
    // Getter和Setter方法
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
    
    public long getConnectionTimeoutMs() { return connectionTimeoutMs; }
    public void setConnectionTimeoutMs(long connectionTimeoutMs) { this.connectionTimeoutMs = connectionTimeoutMs; }
    
    public long getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(long readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    
    public long getWriteTimeoutMs() { return writeTimeoutMs; }
    public void setWriteTimeoutMs(long writeTimeoutMs) { this.writeTimeoutMs = writeTimeoutMs; }
    
    public long getHeartbeatIntervalMs() { return heartbeatIntervalMs; }
    public void setHeartbeatIntervalMs(long heartbeatIntervalMs) { this.heartbeatIntervalMs = heartbeatIntervalMs; }
    
    public long getHeartbeatTimeoutMs() { return heartbeatTimeoutMs; }
    public void setHeartbeatTimeoutMs(long heartbeatTimeoutMs) { this.heartbeatTimeoutMs = heartbeatTimeoutMs; }
    
    public int getMaxMessageSize() { return maxMessageSize; }
    public void setMaxMessageSize(int maxMessageSize) { this.maxMessageSize = maxMessageSize; }
    
    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }
    
    public boolean isTcpNoDelay() { return tcpNoDelay; }
    public void setTcpNoDelay(boolean tcpNoDelay) { this.tcpNoDelay = tcpNoDelay; }
    
    public boolean isKeepAlive() { return keepAlive; }
    public void setKeepAlive(boolean keepAlive) { this.keepAlive = keepAlive; }
    
    public boolean isReuseAddress() { return reuseAddress; }
    public void setReuseAddress(boolean reuseAddress) { this.reuseAddress = reuseAddress; }
    
    public int getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    
    public long getRetryIntervalMs() { return retryIntervalMs; }
    public void setRetryIntervalMs(long retryIntervalMs) { this.retryIntervalMs = retryIntervalMs; }
    
    public long getPeerDiscoveryIntervalMs() { return peerDiscoveryIntervalMs; }
    public void setPeerDiscoveryIntervalMs(long peerDiscoveryIntervalMs) { this.peerDiscoveryIntervalMs = peerDiscoveryIntervalMs; }
    
    public int getMaxPeers() { return maxPeers; }
    public void setMaxPeers(int maxPeers) { this.maxPeers = maxPeers; }
    
    public int getMinPeers() { return minPeers; }
    public void setMinPeers(int minPeers) { this.minPeers = minPeers; }
    
    public long getPeerExpirationMs() { return peerExpirationMs; }
    public void setPeerExpirationMs(long peerExpirationMs) { this.peerExpirationMs = peerExpirationMs; }
    
    public boolean isAutoReconnect() { return autoReconnect; }
    public void setAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; }
    
    public long getReconnectIntervalMs() { return reconnectIntervalMs; }
    public void setReconnectIntervalMs(long reconnectIntervalMs) { this.reconnectIntervalMs = reconnectIntervalMs; }
    
    public boolean isEnableCompression() { return enableCompression; }
    public void setEnableCompression(boolean enableCompression) { this.enableCompression = enableCompression; }
    
    public int getCompressionLevel() { return compressionLevel; }
    public void setCompressionLevel(int compressionLevel) { this.compressionLevel = compressionLevel; }
    
    public boolean isEnableEncryption() { return enableEncryption; }
    public void setEnableEncryption(boolean enableEncryption) { this.enableEncryption = enableEncryption; }
    
    public boolean isEnableSignatureVerification() { return enableSignatureVerification; }
    public void setEnableSignatureVerification(boolean enableSignatureVerification) { this.enableSignatureVerification = enableSignatureVerification; }
    
    public long getMessageExpirationMs() { return messageExpirationMs; }
    public void setMessageExpirationMs(long messageExpirationMs) { this.messageExpirationMs = messageExpirationMs; }
    
    public boolean isEnableMessageDeduplication() { return enableMessageDeduplication; }
    public void setEnableMessageDeduplication(boolean enableMessageDeduplication) { this.enableMessageDeduplication = enableMessageDeduplication; }
    
    public int getMessageDeduplicationCacheSize() { return messageDeduplicationCacheSize; }
    public void setMessageDeduplicationCacheSize(int messageDeduplicationCacheSize) { this.messageDeduplicationCacheSize = messageDeduplicationCacheSize; }
    
    public boolean isEnableMonitoring() { return enableMonitoring; }
    public void setEnableMonitoring(boolean enableMonitoring) { this.enableMonitoring = enableMonitoring; }
    
    public long getMonitoringIntervalMs() { return monitoringIntervalMs; }
    public void setMonitoringIntervalMs(long monitoringIntervalMs) { this.monitoringIntervalMs = monitoringIntervalMs; }
    
    public boolean isEnableDebugLogging() { return enableDebugLogging; }
    public void setEnableDebugLogging(boolean enableDebugLogging) { this.enableDebugLogging = enableDebugLogging; }
    
    public boolean isEnablePerformanceStats() { return enablePerformanceStats; }
    public void setEnablePerformanceStats(boolean enablePerformanceStats) { this.enablePerformanceStats = enablePerformanceStats; }
    
    public int getWorkerThreads() { return workerThreads; }
    public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }
    
    public int getBossThreads() { return bossThreads; }
    public void setBossThreads(int bossThreads) { this.bossThreads = bossThreads; }
    
    public boolean isUseEpoll() { return useEpoll; }
    public void setUseEpoll(boolean useEpoll) { this.useEpoll = useEpoll; }
    
    public boolean isUseNio() { return useNio; }
    public void setUseNio(boolean useNio) { this.useNio = useNio; }
    
    public boolean isUseZeroCopy() { return useZeroCopy; }
    public void setUseZeroCopy(boolean useZeroCopy) { this.useZeroCopy = useZeroCopy; }
    
    public boolean isUseDirectMemory() { return useDirectMemory; }
    public void setUseDirectMemory(boolean useDirectMemory) { this.useDirectMemory = useDirectMemory; }
    
    public long getMaxDirectMemory() { return maxDirectMemory; }
    public void setMaxDirectMemory(long maxDirectMemory) { this.maxDirectMemory = maxDirectMemory; }
    
    public boolean isEnableMemoryPool() { return enableMemoryPool; }
    public void setEnableMemoryPool(boolean enableMemoryPool) { this.enableMemoryPool = enableMemoryPool; }
    
    public int getMemoryPoolSize() { return memoryPoolSize; }
    public void setMemoryPoolSize(int memoryPoolSize) { this.memoryPoolSize = memoryPoolSize; }
    
    public boolean isEnableObjectPool() { return enableObjectPool; }
    public void setEnableObjectPool(boolean enableObjectPool) { this.enableObjectPool = enableObjectPool; }
    
    public int getObjectPoolSize() { return objectPoolSize; }
    public void setObjectPoolSize(int objectPoolSize) { this.objectPoolSize = objectPoolSize; }
    
    /**
     * 验证配置参数
     */
    public void validate() {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("Max connections must be positive");
        }
        if (connectionTimeoutMs <= 0) {
            throw new IllegalArgumentException("Connection timeout must be positive");
        }
        if (readTimeoutMs <= 0) {
            throw new IllegalArgumentException("Read timeout must be positive");
        }
        if (writeTimeoutMs <= 0) {
            throw new IllegalArgumentException("Write timeout must be positive");
        }
        if (heartbeatIntervalMs <= 0) {
            throw new IllegalArgumentException("Heartbeat interval must be positive");
        }
        if (heartbeatTimeoutMs <= heartbeatIntervalMs) {
            throw new IllegalArgumentException("Heartbeat timeout must be greater than heartbeat interval");
        }
        if (maxMessageSize <= 0) {
            throw new IllegalArgumentException("Max message size must be positive");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be positive");
        }
        if (maxRetryAttempts < 0) {
            throw new IllegalArgumentException("Max retry attempts cannot be negative");
        }
        if (retryIntervalMs <= 0) {
            throw new IllegalArgumentException("Retry interval must be positive");
        }
        if (peerDiscoveryIntervalMs <= 0) {
            throw new IllegalArgumentException("Peer discovery interval must be positive");
        }
        if (maxPeers <= 0) {
            throw new IllegalArgumentException("Max peers must be positive");
        }
        if (minPeers < 0) {
            throw new IllegalArgumentException("Min peers cannot be negative");
        }
        if (minPeers > maxPeers) {
            throw new IllegalArgumentException("Min peers cannot be greater than max peers");
        }
        if (peerExpirationMs <= 0) {
            throw new IllegalArgumentException("Peer expiration time must be positive");
        }
        if (reconnectIntervalMs <= 0) {
            throw new IllegalArgumentException("Reconnect interval must be positive");
        }
        if (compressionLevel < 1 || compressionLevel > 9) {
            throw new IllegalArgumentException("Compression level must be between 1 and 9");
        }
        if (messageExpirationMs <= 0) {
            throw new IllegalArgumentException("Message expiration time must be positive");
        }
        if (messageDeduplicationCacheSize <= 0) {
            throw new IllegalArgumentException("Message deduplication cache size must be positive");
        }
        if (monitoringIntervalMs <= 0) {
            throw new IllegalArgumentException("Monitoring interval must be positive");
        }
        if (workerThreads <= 0) {
            throw new IllegalArgumentException("Worker threads must be positive");
        }
        if (bossThreads <= 0) {
            throw new IllegalArgumentException("Boss threads must be positive");
        }
        if (maxDirectMemory <= 0) {
            throw new IllegalArgumentException("Max direct memory must be positive");
        }
        if (memoryPoolSize <= 0) {
            throw new IllegalArgumentException("Memory pool size must be positive");
        }
        if (objectPoolSize <= 0) {
            throw new IllegalArgumentException("Object pool size must be positive");
        }
    }
}
