package io.equiflux.node.network;

import io.equiflux.node.crypto.Ed25519KeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 节点发现和管理服务
 * 
 * <p>负责发现网络中的其他节点，维护节点列表，管理节点连接状态。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>节点发现：通过多种方式发现新节点</li>
 *   <li>连接管理：维护与节点的连接状态</li>
 *   <li>健康检查：定期检查节点健康状态</li>
 *   <li>负载均衡：合理分配连接负载</li>
 * </ul>
 * 
 * <p>发现策略：
 * <ul>
 *   <li>种子节点：从预配置的种子节点开始</li>
 *   <li>邻居发现：从已连接节点获取邻居信息</li>
 *   <li>DNS发现：通过DNS记录发现节点</li>
 *   <li>广播发现：在本地网络中广播发现</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class PeerDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(PeerDiscoveryService.class);
    
    @Autowired
    private NetworkService networkService;
    
    @Autowired
    private NetworkConfig networkConfig;
    
    // 种子节点配置
    private static final List<String> SEED_NODES = Arrays.asList(
        "seed1.equiflux.io:8080",
        "seed2.equiflux.io:8080",
        "seed3.equiflux.io:8080"
    );
    
    // 发现配置
    private static final long DISCOVERY_INTERVAL_MS = 60000; // 1分钟
    private static final long HEALTH_CHECK_INTERVAL_MS = 30000; // 30秒
    private static final long PEER_EXPIRATION_MS = 300000; // 5分钟
    // private static final int MAX_DISCOVERY_ATTEMPTS = 3;
    
    // 节点管理
    private final Map<String, PeerInfo> knownPeers = new ConcurrentHashMap<>();
    private final Map<String, PeerInfo> connectedPeers = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> discoveryAttempts = new ConcurrentHashMap<>();
    
    // 执行器
    private ScheduledExecutorService discoveryExecutor;
    private ExecutorService connectionExecutor;
    
    // 状态
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong peersDiscovered = new AtomicLong(0);
    private final AtomicLong peersConnected = new AtomicLong(0);
    
    @PostConstruct
    public void init() {
        logger.info("初始化节点发现服务");
        
        // 创建执行器
        discoveryExecutor = Executors.newScheduledThreadPool(2, 
            r -> new Thread(r, "peer-discovery"));
        connectionExecutor = Executors.newFixedThreadPool(4, 
            r -> new Thread(r, "peer-connection"));
        
        running.set(true);
        
        // 注册监听器
        networkService.addPeerListener(new PeerEventListener());
        
        // 启动定期任务
        startPeriodicTasks();
        
        // 初始化种子节点
        initializeSeedNodes();
        
        logger.info("节点发现服务初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        logger.info("停止节点发现服务");
        
        running.set(false);
        
        // 关闭执行器
        if (discoveryExecutor != null) {
            discoveryExecutor.shutdown();
            try {
                if (!discoveryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    discoveryExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                discoveryExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (connectionExecutor != null) {
            connectionExecutor.shutdown();
            try {
                if (!connectionExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    connectionExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                connectionExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("节点发现服务停止完成");
    }
    
    /**
     * 发现新节点
     * 
     * @return 发现的节点列表
     */
    public CompletableFuture<List<PeerInfo>> discoverPeers() {
        return CompletableFuture.supplyAsync(() -> {
            List<PeerInfo> discoveredPeers = new ArrayList<>();
            
            try {
                // 从种子节点发现
                discoveredPeers.addAll(discoverFromSeedNodes());
                
                // 从已连接节点发现
                discoveredPeers.addAll(discoverFromConnectedPeers());
                
                // DNS发现
                discoveredPeers.addAll(discoverFromDNS());
                
                // 本地网络发现
                discoveredPeers.addAll(discoverFromLocalNetwork());
                
                // 过滤和验证节点
                List<PeerInfo> validPeers = filterValidPeers(discoveredPeers);
                
                // 添加到已知节点列表
                for (PeerInfo peer : validPeers) {
                    addKnownPeer(peer);
                }
                
                peersDiscovered.addAndGet(validPeers.size());
                logger.info("发现新节点: {} 个", validPeers.size());
                
                return validPeers;
                
            } catch (Exception e) {
                logger.error("节点发现失败", e);
                return new ArrayList<>();
            }
        }, discoveryExecutor);
    }
    
    /**
     * 连接到节点
     * 
     * @param peerInfo 节点信息
     * @return 连接结果
     */
    public CompletableFuture<Boolean> connectToPeer(PeerInfo peerInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String peerId = peerInfo.getNodeId();
                
                // 检查是否已连接
                if (isConnectedToPeer(peerId)) {
                    logger.debug("节点已连接，跳过: {}", peerId);
                    return true;
                }
                
                // 检查连接限制
                if (connectedPeers.size() >= networkConfig.getMaxPeers()) {
                    logger.debug("连接数已达上限，跳过连接: {}", peerId);
                    return false;
                }
                
                // 尝试连接
                networkService.connectToPeer(peerInfo.getHost(), peerInfo.getPort()).join();
                
                // 更新连接状态
                PeerInfo connectedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.CONNECTED);
                connectedPeers.put(peerId, connectedPeer);
                knownPeers.put(peerId, connectedPeer);
                
                peersConnected.incrementAndGet();
                logger.info("成功连接到节点: {}", peerId);
                
                return true;
                
            } catch (Exception e) {
                logger.warn("连接节点失败: " + peerInfo.getNodeId(), e);
                
                // 更新失败状态
                PeerInfo failedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.FAILED);
                knownPeers.put(peerInfo.getNodeId(), failedPeer);
                
                return false;
            }
        }, connectionExecutor);
    }
    
    /**
     * 断开与节点的连接
     * 
     * @param peerId 节点ID
     * @return 断开结果
     */
    public CompletableFuture<Boolean> disconnectFromPeer(String peerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 断开网络连接
                networkService.disconnectFromPeer(peerId).join();
                
                // 更新状态
                PeerInfo peerInfo = connectedPeers.remove(peerId);
                if (peerInfo != null) {
                    PeerInfo disconnectedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.DISCONNECTED);
                    knownPeers.put(peerId, disconnectedPeer);
                    
                    logger.info("断开与节点的连接: {}", peerId);
                    return true;
                }
                
                return false;
                
            } catch (Exception e) {
                logger.error("断开节点连接失败: " + peerId, e);
                return false;
            }
        }, connectionExecutor);
    }
    
    /**
     * 获取已知节点列表
     * 
     * @return 节点列表
     */
    public List<PeerInfo> getKnownPeers() {
        return new ArrayList<>(knownPeers.values());
    }
    
    /**
     * 获取已连接节点列表
     * 
     * @return 节点列表
     */
    public List<PeerInfo> getConnectedPeers() {
        return new ArrayList<>(connectedPeers.values());
    }
    
    /**
     * 获取可用节点列表
     * 
     * @return 节点列表
     */
    public List<PeerInfo> getAvailablePeers() {
        return knownPeers.values().stream()
                .filter(peer -> peer.canConnect())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * 检查是否连接到指定节点
     * 
     * @param peerId 节点ID
     * @return true如果已连接，false否则
     */
    public boolean isConnectedToPeer(String peerId) {
        PeerInfo peerInfo = connectedPeers.get(peerId);
        return peerInfo != null && peerInfo.isConnected();
    }
    
    /**
     * 获取节点信息
     * 
     * @param peerId 节点ID
     * @return 节点信息，如果不存在则返回null
     */
    public PeerInfo getPeerInfo(String peerId) {
        return knownPeers.get(peerId);
    }
    
    /**
     * 获取发现统计信息
     * 
     * @return 统计信息
     */
    public DiscoveryStats getStats() {
        return new DiscoveryStats(
            knownPeers.size(),
            connectedPeers.size(),
            peersDiscovered.get(),
            peersConnected.get()
        );
    }
    
    // 私有方法
    
    private void startPeriodicTasks() {
        // 定期发现新节点
        discoveryExecutor.scheduleWithFixedDelay(
            this::performDiscovery,
            DISCOVERY_INTERVAL_MS,
            DISCOVERY_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
        
        // 定期健康检查
        discoveryExecutor.scheduleWithFixedDelay(
            this::performHealthCheck,
            HEALTH_CHECK_INTERVAL_MS,
            HEALTH_CHECK_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
        
        // 定期清理过期节点
        discoveryExecutor.scheduleWithFixedDelay(
            this::cleanupExpiredPeers,
            PEER_EXPIRATION_MS,
            PEER_EXPIRATION_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    private void initializeSeedNodes() {
        for (String seedNode : SEED_NODES) {
            try {
                String[] parts = seedNode.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                
                PeerInfo seedPeer = new PeerInfo(
                    seedNode, null, host, port,
                    PeerInfo.PeerStatus.DISCONNECTED,
                    System.currentTimeMillis(), 0, 0
                );
                
                addKnownPeer(seedPeer);
                logger.info("添加种子节点: {}", seedNode);
                
            } catch (Exception e) {
                logger.warn("添加种子节点失败: " + seedNode, e);
            }
        }
    }
    
    private void performDiscovery() {
        if (!running.get()) {
            return;
        }
        
        try {
            discoverPeers().thenAccept(discoveredPeers -> {
                // 尝试连接到新发现的节点
                for (PeerInfo peer : discoveredPeers) {
                    if (connectedPeers.size() < networkConfig.getMaxPeers()) {
                        connectToPeer(peer);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("执行节点发现失败", e);
        }
    }
    
    private void performHealthCheck() {
        if (!running.get()) {
            return;
        }
        
        try {
            // 检查已连接节点的健康状态
            List<String> unhealthyPeers = new ArrayList<>();
            
            for (Map.Entry<String, PeerInfo> entry : connectedPeers.entrySet()) {
                String peerId = entry.getKey();
                PeerInfo peerInfo = entry.getValue();
                
                // 检查节点是否过期
                if (peerInfo.isExpired(PEER_EXPIRATION_MS)) {
                    unhealthyPeers.add(peerId);
                }
            }
            
            // 断开不健康的节点
            for (String peerId : unhealthyPeers) {
                disconnectFromPeer(peerId);
            }
            
            // 确保最小连接数
            ensureMinimumConnections();
            
        } catch (Exception e) {
            logger.error("执行健康检查失败", e);
        }
    }
    
    private void cleanupExpiredPeers() {
        if (!running.get()) {
            return;
        }
        
        try {
            long currentTime = System.currentTimeMillis();
            List<String> expiredPeers = new ArrayList<>();
            
            for (Map.Entry<String, PeerInfo> entry : knownPeers.entrySet()) {
                String peerId = entry.getKey();
                PeerInfo peerInfo = entry.getValue();
                
                // 检查节点是否过期
                if (currentTime - peerInfo.getLastSeen() > PEER_EXPIRATION_MS) {
                    expiredPeers.add(peerId);
                }
            }
            
            // 清理过期节点
            for (String peerId : expiredPeers) {
                knownPeers.remove(peerId);
                connectedPeers.remove(peerId);
                discoveryAttempts.remove(peerId);
            }
            
            if (!expiredPeers.isEmpty()) {
                logger.debug("清理过期节点: {} 个", expiredPeers.size());
            }
            
        } catch (Exception e) {
            logger.error("清理过期节点失败", e);
        }
    }
    
    private void ensureMinimumConnections() {
        int currentConnections = connectedPeers.size();
        int minConnections = networkConfig.getMinPeers();
        
        if (currentConnections < minConnections) {
            List<PeerInfo> availablePeers = getAvailablePeers();
            
            // 尝试连接到可用节点
            for (PeerInfo peer : availablePeers) {
                if (connectedPeers.size() >= minConnections) {
                    break;
                }
                
                connectToPeer(peer);
            }
        }
    }
    
    private List<PeerInfo> discoverFromSeedNodes() {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        
        for (String seedNode : SEED_NODES) {
            try {
                String[] parts = seedNode.split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                
                // 发送发现请求
                // TODO: 实现与种子节点的通信
                
            } catch (Exception e) {
                logger.debug("从种子节点发现失败: " + seedNode, e);
            }
        }
        
        return discoveredPeers;
    }
    
    private List<PeerInfo> discoverFromConnectedPeers() {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        
        // 从已连接节点获取邻居信息
        for (PeerInfo peer : connectedPeers.values()) {
            try {
                // 发送邻居发现请求
                // TODO: 实现邻居发现协议
                
            } catch (Exception e) {
                logger.debug("从已连接节点发现失败: " + peer.getNodeId(), e);
            }
        }
        
        return discoveredPeers;
    }
    
    private List<PeerInfo> discoverFromDNS() {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        
        // TODO: 实现DNS发现
        // 查询DNS记录获取节点列表
        
        return discoveredPeers;
    }
    
    private List<PeerInfo> discoverFromLocalNetwork() {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        
        // TODO: 实现本地网络发现
        // 在本地网络中广播发现请求
        
        return discoveredPeers;
    }
    
    private List<PeerInfo> filterValidPeers(List<PeerInfo> peers) {
        List<PeerInfo> validPeers = new ArrayList<>();
        
        for (PeerInfo peer : peers) {
            if (isValidPeer(peer)) {
                validPeers.add(peer);
            }
        }
        
        return validPeers;
    }
    
    private boolean isValidPeer(PeerInfo peer) {
        // 检查基本格式
        if (peer.getNodeId() == null || peer.getNodeId().isEmpty()) {
            return false;
        }
        
        if (peer.getHost() == null || peer.getHost().isEmpty()) {
            return false;
        }
        
        if (peer.getPort() <= 0 || peer.getPort() > 65535) {
            return false;
        }
        
        // 检查是否是自己
        if (isSelf(peer)) {
            return false;
        }
        
        // 检查是否已存在
        if (knownPeers.containsKey(peer.getNodeId())) {
            return false;
        }
        
        return true;
    }
    
    private boolean isSelf(PeerInfo peer) {
        PeerInfo localPeer = networkService.getLocalPeerInfo();
        return peer.getNodeId().equals(localPeer.getNodeId());
    }
    
    private void addKnownPeer(PeerInfo peer) {
        knownPeers.put(peer.getNodeId(), peer);
        discoveryAttempts.put(peer.getNodeId(), new AtomicLong(0));
    }
    
    /**
     * 节点事件监听器
     */
    private class PeerEventListener implements PeerListener {
        
        @Override
        public void onPeerConnected(PeerInfo peerInfo) {
            String peerId = peerInfo.getNodeId();
            connectedPeers.put(peerId, peerInfo);
            knownPeers.put(peerId, peerInfo);
            
            logger.info("节点连接事件: {}", peerId);
        }
        
        @Override
        public void onPeerDisconnected(PeerInfo peerInfo) {
            String peerId = peerInfo.getNodeId();
            connectedPeers.remove(peerId);
            
            PeerInfo disconnectedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.DISCONNECTED);
            knownPeers.put(peerId, disconnectedPeer);
            
            logger.info("节点断开事件: {}", peerId);
        }
        
        @Override
        public void onPeerConnectionFailed(PeerInfo peerInfo, Throwable error) {
            String peerId = peerInfo.getNodeId();
            
            PeerInfo failedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.FAILED);
            knownPeers.put(peerId, failedPeer);
            
            logger.warn("节点连接失败事件: " + peerId, error);
        }
        
        @Override
        public void onPeerDiscovered(PeerInfo peerInfo) {
            addKnownPeer(peerInfo);
            logger.info("发现新节点: {}", peerInfo.getNodeId());
        }
    }
    
    /**
     * 发现统计信息
     */
    public static class DiscoveryStats {
        private final int knownPeers;
        private final int connectedPeers;
        private final long peersDiscovered;
        private final long peersConnected;
        
        public DiscoveryStats(int knownPeers, int connectedPeers, 
                            long peersDiscovered, long peersConnected) {
            this.knownPeers = knownPeers;
            this.connectedPeers = connectedPeers;
            this.peersDiscovered = peersDiscovered;
            this.peersConnected = peersConnected;
        }
        
        public int getKnownPeers() {
            return knownPeers;
        }
        
        public int getConnectedPeers() {
            return connectedPeers;
        }
        
        public long getPeersDiscovered() {
            return peersDiscovered;
        }
        
        public long getPeersConnected() {
            return peersConnected;
        }
        
        public double getConnectionRate() {
            return knownPeers > 0 ? (double) connectedPeers / knownPeers : 0.0;
        }
        
        @Override
        public String toString() {
            return "DiscoveryStats{" +
                   "knownPeers=" + knownPeers +
                   ", connectedPeers=" + connectedPeers +
                   ", peersDiscovered=" + peersDiscovered +
                   ", peersConnected=" + peersConnected +
                   ", connectionRate=" + String.format("%.2f%%", getConnectionRate() * 100) +
                   '}';
        }
    }
}
