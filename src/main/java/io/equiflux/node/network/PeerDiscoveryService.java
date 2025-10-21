package io.equiflux.node.network;

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
                List<PeerInfo> peers = requestPeersFromSeed(host, port);
                discoveredPeers.addAll(peers);
                
                logger.debug("从种子节点发现: {} 个节点", peers.size());
                
            } catch (Exception e) {
                logger.debug("从种子节点发现失败: " + seedNode, e);
            }
        }
        
        return discoveredPeers;
    }
    
    /**
     * 从种子节点请求节点列表
     * 
     * @param host 种子节点主机
     * @param port 种子节点端口
     * @return 发现的节点列表
     */
    private List<PeerInfo> requestPeersFromSeed(String host, int port) {
        List<PeerInfo> peers = new ArrayList<>();
        
        try {
            // 创建节点发现请求消息
            NetworkMessage requestMessage = createPeerDiscoveryRequest();
            
            // 发送请求到种子节点
            CompletableFuture<Void> future = networkService.sendMessage(host, port, requestMessage);
            
            // 等待响应（简化实现，实际应该使用异步回调）
            try {
                future.get(5000, TimeUnit.MILLISECONDS);
                
                // 模拟从种子节点获取的节点列表
                // 实际实现中应该解析响应消息
                peers.addAll(generateMockPeersFromSeed(host, port));
                
            } catch (TimeoutException e) {
                logger.debug("种子节点响应超时: {}:{}", host, port);
            } catch (Exception e) {
                logger.debug("种子节点通信失败: {}:{}", host, port, e);
            }
            
        } catch (Exception e) {
            logger.error("请求种子节点失败: {}:{}", host, port, e);
        }
        
        return peers;
    }
    
    /**
     * 创建节点发现请求消息
     * 
     * @return 节点发现请求消息
     */
    private NetworkMessage createPeerDiscoveryRequest() {
        // 创建请求负载
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("type", "peer_discovery_request");
        requestPayload.put("timestamp", System.currentTimeMillis());
        requestPayload.put("maxPeers", 10);
        
        // 创建网络消息
        return new NetworkMessage(
            NetworkMessage.MessageType.PEER_DISCOVERY,
            null, // 发送者将在发送时设置
            System.currentTimeMillis(),
            System.currentTimeMillis() + Thread.currentThread().threadId(),
            requestPayload,
            new byte[64] // 签名将在发送时设置
        );
    }
    
    /**
     * 生成模拟的种子节点响应
     * 
     * @param seedHost 种子节点主机
     * @param seedPort 种子节点端口
     * @return 模拟的节点列表
     */
    private List<PeerInfo> generateMockPeersFromSeed(String seedHost, int seedPort) {
        List<PeerInfo> mockPeers = new ArrayList<>();
        
        // 生成一些模拟的节点信息
        for (int i = 1; i <= 5; i++) {
            String mockHost = "peer" + i + ".equiflux.io";
            int mockPort = 8080 + i;
            String mockNodeId = mockHost + ":" + mockPort;
            
            PeerInfo mockPeer = new PeerInfo(
                mockNodeId,
                null, // 公钥将在连接时获取
                mockHost,
                mockPort,
                PeerInfo.PeerStatus.DISCONNECTED,
                System.currentTimeMillis(),
                0,
                0
            );
            
            mockPeers.add(mockPeer);
        }
        
        return mockPeers;
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
        
        try {
            // DNS发现配置
            String[] dnsSeeds = {
                "seeds.equiflux.io",
                "nodes.equiflux.io",
                "peers.equiflux.io"
            };
            
            for (String dnsSeed : dnsSeeds) {
                try {
                    List<PeerInfo> peers = queryDNSForPeers(dnsSeed);
                    discoveredPeers.addAll(peers);
                    
                    logger.debug("从DNS种子发现: {} 个节点", peers.size());
                    
                } catch (Exception e) {
                    logger.debug("DNS查询失败: " + dnsSeed, e);
                }
            }
            
        } catch (Exception e) {
            logger.error("DNS发现失败", e);
        }
        
        return discoveredPeers;
    }
    
    /**
     * 查询DNS获取节点列表
     * 
     * @param dnsSeed DNS种子
     * @return 发现的节点列表
     */
    private List<PeerInfo> queryDNSForPeers(String dnsSeed) {
        List<PeerInfo> peers = new ArrayList<>();
        
        try {
            // 查询TXT记录获取节点列表
            // 这里使用简化的实现，实际应该使用DNS查询库
            List<String> dnsRecords = queryDNSTXTRecords(dnsSeed);
            
            for (String record : dnsRecords) {
                try {
                    PeerInfo peer = parseDNSRecord(record);
                    if (peer != null) {
                        peers.add(peer);
                    }
                } catch (Exception e) {
                    logger.debug("解析DNS记录失败: " + record, e);
                }
            }
            
        } catch (Exception e) {
            logger.error("查询DNS记录失败: " + dnsSeed, e);
        }
        
        return peers;
    }
    
    /**
     * 查询DNS TXT记录（模拟实现）
     * 
     * @param domain 域名
     * @return TXT记录列表
     */
    private List<String> queryDNSTXTRecords(String domain) {
        List<String> records = new ArrayList<>();
        
        // 模拟DNS查询结果
        // 实际实现应该使用DNS查询库如dnsjava
        records.add("peer1.equiflux.io:8081");
        records.add("peer2.equiflux.io:8082");
        records.add("peer3.equiflux.io:8083");
        
        return records;
    }
    
    /**
     * 解析DNS记录
     * 
     * @param record DNS记录
     * @return 节点信息
     */
    private PeerInfo parseDNSRecord(String record) {
        try {
            // 解析格式: "host:port"
            String[] parts = record.split(":");
            if (parts.length != 2) {
                return null;
            }
            
            String host = parts[0].trim();
            int port = Integer.parseInt(parts[1].trim());
            
            String nodeId = host + ":" + port;
            
            return new PeerInfo(
                nodeId,
                null, // 公钥将在连接时获取
                host,
                port,
                PeerInfo.PeerStatus.DISCONNECTED,
                System.currentTimeMillis(),
                0,
                0
            );
            
        } catch (Exception e) {
            logger.debug("解析DNS记录失败: " + record, e);
            return null;
        }
    }
    
    private List<PeerInfo> discoverFromLocalNetwork() {
        List<PeerInfo> discoveredPeers = new ArrayList<>();
        
        try {
            // 获取本地网络地址
            String localNetwork = getLocalNetworkAddress();
            if (localNetwork == null) {
                logger.debug("无法获取本地网络地址");
                return discoveredPeers;
            }
            
            // 扫描本地网络
            List<PeerInfo> peers = scanLocalNetwork(localNetwork);
            discoveredPeers.addAll(peers);
            
            logger.debug("从本地网络发现: {} 个节点", peers.size());
            
        } catch (Exception e) {
            logger.error("本地网络发现失败", e);
        }
        
        return discoveredPeers;
    }
    
    /**
     * 获取本地网络地址
     * 
     * @return 本地网络地址
     */
    private String getLocalNetworkAddress() {
        try {
            // 获取本地IP地址
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String localIP = localHost.getHostAddress();
            
            // 提取网络段（假设是/24网络）
            String[] parts = localIP.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".0/24";
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("获取本地网络地址失败", e);
            return null;
        }
    }
    
    /**
     * 扫描本地网络
     * 
     * @param networkAddress 网络地址
     * @return 发现的节点列表
     */
    private List<PeerInfo> scanLocalNetwork(String networkAddress) {
        List<PeerInfo> peers = new ArrayList<>();
        
        try {
            // 解析网络地址
            String[] parts = networkAddress.split("/");
            if (parts.length != 2) {
                return peers;
            }
            
            String baseIP = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);
            
            // 计算扫描范围
            String[] ipParts = baseIP.split("\\.");
            if (ipParts.length != 4) {
                return peers;
            }
            
            int startIP = Integer.parseInt(ipParts[3]);
            int endIP = startIP + (1 << (32 - prefixLength)) - 1;
            
            // 扫描IP范围
            for (int i = startIP; i <= Math.min(endIP, startIP + 10); i++) { // 限制扫描数量
                String targetIP = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + "." + i;
                
                // 跳过自己的IP
                if (isLocalIP(targetIP)) {
                    continue;
                }
                
                // 尝试连接常见端口
                for (int port : getCommonPorts()) {
                    try {
                        PeerInfo peer = probePeer(targetIP, port);
                        if (peer != null) {
                            peers.add(peer);
                        }
                    } catch (Exception e) {
                        // 忽略连接失败
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("扫描本地网络失败", e);
        }
        
        return peers;
    }
    
    /**
     * 检查是否为本地IP
     * 
     * @param ip IP地址
     * @return true如果是本地IP，false否则
     */
    private boolean isLocalIP(String ip) {
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            return ip.equals(localHost.getHostAddress()) || ip.equals("127.0.0.1");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取常见端口列表
     * 
     * @return 端口列表
     */
    private int[] getCommonPorts() {
        return new int[]{8080, 8081, 8082, 8083, 8084, 8085};
    }
    
    /**
     * 探测节点
     * 
     * @param host 主机地址
     * @param port 端口
     * @return 节点信息，如果无法连接则返回null
     */
    private PeerInfo probePeer(String host, int port) {
        try {
            // 尝试建立连接
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), 1000); // 1秒超时
            socket.close();
            
            // 如果连接成功，创建节点信息
            String nodeId = host + ":" + port;
            
            return new PeerInfo(
                nodeId,
                null, // 公钥将在连接时获取
                host,
                port,
                PeerInfo.PeerStatus.DISCONNECTED,
                System.currentTimeMillis(),
                0,
                0
            );
            
        } catch (Exception e) {
            return null; // 连接失败
        }
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
