package io.equiflux.node.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 网络监控服务
 * 
 * <p>负责监控网络层的性能指标，收集统计信息，提供监控数据。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>性能指标收集</li>
 *   <li>网络状态监控</li>
 *   <li>告警机制</li>
 *   <li>统计报告</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class NetworkMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(NetworkMonitoringService.class);
    
    @Autowired
    private NetworkService networkService;
    
    @Autowired
    private NetworkConfig networkConfig;
    
    // 监控配置
    private static final long MONITORING_INTERVAL_MS = 10000; // 10秒
    private static final long ALERT_CHECK_INTERVAL_MS = 30000; // 30秒
    
    // 性能指标
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong totalMessagesReceived = new AtomicLong(0);
    private final AtomicLong totalBytesSent = new AtomicLong(0);
    private final AtomicLong totalBytesReceived = new AtomicLong(0);
    private final AtomicLong connectionFailures = new AtomicLong(0);
    private final AtomicLong messageFailures = new AtomicLong(0);
    
    // 历史数据
    private final CircularBuffer<NetworkMetrics> metricsHistory = new CircularBuffer<>(100);
    
    // 执行器
    private ScheduledExecutorService monitoringExecutor;
    private ScheduledExecutorService alertExecutor;
    
    // 状态
    private volatile boolean running = false;
    
    @PostConstruct
    public void init() {
        logger.info("初始化网络监控服务");
        
        if (!networkConfig.isEnableMonitoring()) {
            logger.info("网络监控已禁用");
            return;
        }
        
        // 创建执行器
        monitoringExecutor = Executors.newScheduledThreadPool(2, 
            r -> new Thread(r, "network-monitoring"));
        alertExecutor = Executors.newScheduledThreadPool(1, 
            r -> new Thread(r, "network-alert"));
        
        running = true;
        
        // 启动监控任务
        startMonitoringTasks();
        
        logger.info("网络监控服务初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        logger.info("停止网络监控服务");
        
        running = false;
        
        // 关闭执行器
        if (monitoringExecutor != null) {
            monitoringExecutor.shutdown();
            try {
                if (!monitoringExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    monitoringExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                monitoringExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (alertExecutor != null) {
            alertExecutor.shutdown();
            try {
                if (!alertExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    alertExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                alertExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("网络监控服务停止完成");
    }
    
    /**
     * 记录消息发送
     * 
     * @param messageSize 消息大小
     */
    public void recordMessageSent(int messageSize) {
        totalMessagesSent.incrementAndGet();
        totalBytesSent.addAndGet(messageSize);
    }
    
    /**
     * 记录消息接收
     * 
     * @param messageSize 消息大小
     */
    public void recordMessageReceived(int messageSize) {
        totalMessagesReceived.incrementAndGet();
        totalBytesReceived.addAndGet(messageSize);
    }
    
    /**
     * 记录连接失败
     */
    public void recordConnectionFailure() {
        connectionFailures.incrementAndGet();
    }
    
    /**
     * 记录消息失败
     */
    public void recordMessageFailure() {
        messageFailures.incrementAndGet();
    }
    
    /**
     * 获取当前网络指标
     * 
     * @return 网络指标
     */
    public NetworkMetrics getCurrentMetrics() {
        NetworkStats stats = networkService.getNetworkStats();
        
        return new NetworkMetrics(
            System.currentTimeMillis(),
            stats.getMessagesSent(),
            stats.getMessagesReceived(),
            stats.getBytesSent(),
            stats.getBytesReceived(),
            stats.getConnectedPeers(),
            stats.getTotalPeers(),
            connectionFailures.get(),
            messageFailures.get(),
            stats.getUptimeMs()
        );
    }
    
    /**
     * 获取历史指标
     * 
     * @return 历史指标列表
     */
    public java.util.List<NetworkMetrics> getMetricsHistory() {
        return metricsHistory.getAll();
    }
    
    /**
     * 获取网络健康状态
     * 
     * @return 健康状态
     */
    public NetworkHealthStatus getHealthStatus() {
        NetworkMetrics current = getCurrentMetrics();
        
        // 计算健康分数
        double healthScore = calculateHealthScore(current);
        
        // 确定健康状态
        NetworkHealthStatus.HealthLevel level;
        if (healthScore >= 0.9) {
            level = NetworkHealthStatus.HealthLevel.HEALTHY;
        } else if (healthScore >= 0.7) {
            level = NetworkHealthStatus.HealthLevel.WARNING;
        } else if (healthScore >= 0.5) {
            level = NetworkHealthStatus.HealthLevel.DEGRADED;
        } else {
            level = NetworkHealthStatus.HealthLevel.CRITICAL;
        }
        
        return new NetworkHealthStatus(level, healthScore, current);
    }
    
    // 私有方法
    
    private void startMonitoringTasks() {
        // 定期收集指标
        monitoringExecutor.scheduleWithFixedDelay(
            this::collectMetrics,
            MONITORING_INTERVAL_MS,
            MONITORING_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
        
        // 定期检查告警
        alertExecutor.scheduleWithFixedDelay(
            this::checkAlerts,
            ALERT_CHECK_INTERVAL_MS,
            ALERT_CHECK_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    private void collectMetrics() {
        if (!running) {
            return;
        }
        
        try {
            NetworkMetrics metrics = getCurrentMetrics();
            metricsHistory.add(metrics);
            
            logger.debug("收集网络指标: {}", metrics);
            
        } catch (Exception e) {
            logger.error("收集网络指标失败", e);
        }
    }
    
    private void checkAlerts() {
        if (!running) {
            return;
        }
        
        try {
            NetworkHealthStatus health = getHealthStatus();
            
            if (health.getLevel() == NetworkHealthStatus.HealthLevel.CRITICAL) {
                logger.error("网络健康状态严重: {}", health);
                // 这里可以发送告警通知
            } else if (health.getLevel() == NetworkHealthStatus.HealthLevel.DEGRADED) {
                logger.warn("网络健康状态降级: {}", health);
            }
            
        } catch (Exception e) {
            logger.error("检查网络告警失败", e);
        }
    }
    
    private double calculateHealthScore(NetworkMetrics metrics) {
        double score = 1.0;
        
        // 连接数健康度
        if (metrics.getConnectedPeers() < networkConfig.getMinPeers()) {
            score -= 0.3;
        }
        
        // 消息失败率
        long totalMessages = metrics.getMessagesSent() + metrics.getMessagesReceived();
        if (totalMessages > 0) {
            double failureRate = (double) metrics.getMessageFailures() / totalMessages;
            if (failureRate > 0.1) {
                score -= 0.2;
            }
        }
        
        // 连接失败率
        if (metrics.getConnectionFailures() > 10) {
            score -= 0.2;
        }
        
        // 网络活动
        if (metrics.getBytesSent() == 0 && metrics.getBytesReceived() == 0) {
            score -= 0.3;
        }
        
        return Math.max(0.0, score);
    }
    
    /**
     * 网络指标
     */
    public static class NetworkMetrics {
        private final long timestamp;
        private final long messagesSent;
        private final long messagesReceived;
        private final long bytesSent;
        private final long bytesReceived;
        private final int connectedPeers;
        private final int totalPeers;
        private final long connectionFailures;
        private final long messageFailures;
        private final long uptimeMs;
        
        public NetworkMetrics(long timestamp, long messagesSent, long messagesReceived,
                            long bytesSent, long bytesReceived, int connectedPeers,
                            int totalPeers, long connectionFailures, long messageFailures,
                            long uptimeMs) {
            this.timestamp = timestamp;
            this.messagesSent = messagesSent;
            this.messagesReceived = messagesReceived;
            this.bytesSent = bytesSent;
            this.bytesReceived = bytesReceived;
            this.connectedPeers = connectedPeers;
            this.totalPeers = totalPeers;
            this.connectionFailures = connectionFailures;
            this.messageFailures = messageFailures;
            this.uptimeMs = uptimeMs;
        }
        
        // Getters
        public long getTimestamp() { return timestamp; }
        public long getMessagesSent() { return messagesSent; }
        public long getMessagesReceived() { return messagesReceived; }
        public long getBytesSent() { return bytesSent; }
        public long getBytesReceived() { return bytesReceived; }
        public int getConnectedPeers() { return connectedPeers; }
        public int getTotalPeers() { return totalPeers; }
        public long getConnectionFailures() { return connectionFailures; }
        public long getMessageFailures() { return messageFailures; }
        public long getUptimeMs() { return uptimeMs; }
        
        public long getTotalMessages() { return messagesSent + messagesReceived; }
        public long getTotalBytes() { return bytesSent + bytesReceived; }
        
        @Override
        public String toString() {
            return "NetworkMetrics{" +
                   "timestamp=" + timestamp +
                   ", messagesSent=" + messagesSent +
                   ", messagesReceived=" + messagesReceived +
                   ", bytesSent=" + bytesSent +
                   ", bytesReceived=" + bytesReceived +
                   ", connectedPeers=" + connectedPeers +
                   ", totalPeers=" + totalPeers +
                   ", connectionFailures=" + connectionFailures +
                   ", messageFailures=" + messageFailures +
                   ", uptimeMs=" + uptimeMs +
                   '}';
        }
    }
    
    /**
     * 网络健康状态
     */
    public static class NetworkHealthStatus {
        
        public enum HealthLevel {
            HEALTHY("healthy"),
            WARNING("warning"),
            DEGRADED("degraded"),
            CRITICAL("critical");
            
            private final String value;
            
            HealthLevel(String value) {
                this.value = value;
            }
            
            public String getValue() {
                return value;
            }
        }
        
        private final HealthLevel level;
        private final double score;
        private final NetworkMetrics metrics;
        
        public NetworkHealthStatus(HealthLevel level, double score, NetworkMetrics metrics) {
            this.level = level;
            this.score = score;
            this.metrics = metrics;
        }
        
        public HealthLevel getLevel() { return level; }
        public double getScore() { return score; }
        public NetworkMetrics getMetrics() { return metrics; }
        
        @Override
        public String toString() {
            return "NetworkHealthStatus{" +
                   "level=" + level +
                   ", score=" + String.format("%.2f", score) +
                   ", metrics=" + metrics +
                   '}';
        }
    }
    
    /**
     * 循环缓冲区
     */
    private static class CircularBuffer<T> {
        private final T[] buffer;
        private final int capacity;
        private int head = 0;
        private int size = 0;
        
        @SuppressWarnings("unchecked")
        public CircularBuffer(int capacity) {
            this.capacity = capacity;
            this.buffer = (T[]) new Object[capacity];
        }
        
        public synchronized void add(T item) {
            buffer[head] = item;
            head = (head + 1) % capacity;
            if (size < capacity) {
                size++;
            }
        }
        
        public synchronized java.util.List<T> getAll() {
            java.util.List<T> result = new java.util.ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                int index = (head - size + i + capacity) % capacity;
                result.add(buffer[index]);
            }
            return result;
        }
    }
}
