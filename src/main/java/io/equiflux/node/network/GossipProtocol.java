package io.equiflux.node.network;

import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;
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
 * Gossip协议实现
 * 
 * <p>实现基于Gossip协议的消息传播机制，确保消息在网络中快速、可靠地传播。
 * 
 * <p>Gossip协议特性：
 * <ul>
 *   <li>去中心化：每个节点都是平等的</li>
 *   <li>容错性：部分节点故障不影响整体</li>
 *   <li>最终一致性：消息最终会传播到所有节点</li>
 *   <li>可扩展性：支持大规模网络</li>
 * </ul>
 * 
 * <p>传播策略：
 * <ul>
 *   <li>Push模式：主动推送消息给邻居节点</li>
 *   <li>Pull模式：定期拉取邻居节点的消息</li>
 *   <li>Push-Pull模式：结合两种模式的优势</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class GossipProtocol {
    
    private static final Logger logger = LoggerFactory.getLogger(GossipProtocol.class);
    
    @Autowired
    private NetworkService networkService;
    
    @Autowired
    private NetworkConfig networkConfig;
    
    // Gossip配置
    private static final int GOSSIP_FANOUT = 3; // 每次传播的节点数
    private static final long GOSSIP_INTERVAL_MS = 1000; // 传播间隔
    private static final int MAX_ROUNDS = 10; // 最大传播轮数
    private static final long MESSAGE_TTL_MS = 300000; // 消息生存时间（5分钟）
    
    // 消息缓存
    private final Map<String, GossipMessage> messageCache = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> messageReceivers = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> messageRounds = new ConcurrentHashMap<>();
    
    // 执行器
    private ScheduledExecutorService gossipExecutor;
    private ExecutorService messageExecutor;
    
    // 状态
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    @PostConstruct
    public void init() {
        logger.info("初始化Gossip协议");
        
        // 创建执行器
        gossipExecutor = Executors.newScheduledThreadPool(2, 
            r -> new Thread(r, "gossip-protocol"));
        messageExecutor = Executors.newFixedThreadPool(4, 
            r -> new Thread(r, "gossip-message"));
        
        // 注册消息监听器
        networkService.addMessageListener(new GossipMessageListener());
        
        running.set(true);
        
        // 启动定期任务
        startPeriodicTasks();
        
        logger.info("Gossip协议初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        logger.info("停止Gossip协议");
        
        running.set(false);
        
        // 关闭执行器
        if (gossipExecutor != null) {
            gossipExecutor.shutdown();
            try {
                if (!gossipExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    gossipExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                gossipExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (messageExecutor != null) {
            messageExecutor.shutdown();
            try {
                if (!messageExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    messageExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                messageExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("Gossip协议停止完成");
    }
    
    /**
     * 传播区块提议
     * 
     * @param block 区块
     */
    public void gossipBlockProposal(Block block) {
        if (!running.get()) {
            return;
        }
        
        GossipMessage message = new GossipMessage(
            GossipMessage.MessageType.BLOCK_PROPOSAL,
            block,
            System.currentTimeMillis(),
            generateMessageId()
        );
        
        gossipMessage(message);
    }
    
    /**
     * 传播区块投票
     * 
     * @param block 区块
     */
    public void gossipBlockVote(Block block) {
        if (!running.get()) {
            return;
        }
        
        GossipMessage message = new GossipMessage(
            GossipMessage.MessageType.BLOCK_VOTE,
            block,
            System.currentTimeMillis(),
            generateMessageId()
        );
        
        gossipMessage(message);
    }
    
    /**
     * 传播交易
     * 
     * @param transaction 交易
     */
    public void gossipTransaction(Transaction transaction) {
        if (!running.get()) {
            return;
        }
        
        GossipMessage message = new GossipMessage(
            GossipMessage.MessageType.TRANSACTION,
            transaction,
            System.currentTimeMillis(),
            generateMessageId()
        );
        
        gossipMessage(message);
    }
    
    /**
     * 传播VRF公告
     * 
     * @param announcement VRF公告
     */
    public void gossipVRFAnnouncement(VRFAnnouncement announcement) {
        if (!running.get()) {
            return;
        }
        
        GossipMessage message = new GossipMessage(
            GossipMessage.MessageType.VRF_ANNOUNCEMENT,
            announcement,
            System.currentTimeMillis(),
            generateMessageId()
        );
        
        gossipMessage(message);
    }
    
    /**
     * 处理接收到的Gossip消息
     * 
     * @param message Gossip消息
     * @param senderId 发送者ID
     */
    public void handleGossipMessage(GossipMessage message, String senderId) {
        if (!running.get()) {
            return;
        }
        
        messageExecutor.submit(() -> {
            try {
                String messageId = message.getMessageId();
                
                // 检查是否已处理过此消息
                if (messageCache.containsKey(messageId)) {
                    logger.debug("重复的Gossip消息，忽略: {}", messageId);
                    return;
                }
                
                // 检查消息是否过期
                if (isMessageExpired(message)) {
                    logger.debug("过期的Gossip消息，忽略: {}", messageId);
                    return;
                }
                
                // 缓存消息
                messageCache.put(messageId, message);
                messageReceivers.put(messageId, new HashSet<>());
                messageRounds.put(messageId, new AtomicLong(0));
                
                // 记录发送者
                messageReceivers.get(messageId).add(senderId);
                
                logger.debug("处理Gossip消息: {} 来自: {}", message.getType(), senderId);
                
                // 继续传播消息
                continueGossip(message);
                
            } catch (Exception e) {
                logger.error("处理Gossip消息失败", e);
            }
        });
    }
    
    /**
     * 获取消息传播统计
     * 
     * @return 统计信息
     */
    public GossipStats getStats() {
        return new GossipStats(
            messageCache.size(),
            messageReceivers.size(),
            messageRounds.size()
        );
    }
    
    // 私有方法
    
    private void startPeriodicTasks() {
        // 定期清理过期消息
        gossipExecutor.scheduleWithFixedDelay(
            this::cleanupExpiredMessages,
            MESSAGE_TTL_MS,
            MESSAGE_TTL_MS,
            TimeUnit.MILLISECONDS
        );
        
        // 定期拉取消息
        gossipExecutor.scheduleWithFixedDelay(
            this::pullMessages,
            GOSSIP_INTERVAL_MS,
            GOSSIP_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
        
        // 使用网络配置调整Gossip参数
        if (networkConfig.isEnableDebugLogging()) {
            logger.info("Gossip协议配置: fanout={}, interval={}ms, maxRounds={}, ttl={}ms", 
                       GOSSIP_FANOUT, GOSSIP_INTERVAL_MS, MAX_ROUNDS, MESSAGE_TTL_MS);
        }
    }
    
    private void gossipMessage(GossipMessage message) {
        messageExecutor.submit(() -> {
            try {
                String messageId = message.getMessageId();
                
                // 缓存消息
                messageCache.put(messageId, message);
                messageReceivers.put(messageId, new HashSet<>());
                messageRounds.put(messageId, new AtomicLong(0));
                
                logger.debug("开始Gossip传播消息: {}", messageId);
                
                // 开始传播
                continueGossip(message);
                
            } catch (Exception e) {
                logger.error("Gossip传播消息失败", e);
            }
        });
    }
    
    private void continueGossip(GossipMessage message) {
        String messageId = message.getMessageId();
        AtomicLong rounds = messageRounds.get(messageId);
        
        if (rounds == null || rounds.get() >= MAX_ROUNDS) {
            logger.debug("Gossip传播达到最大轮数，停止: {}", messageId);
            return;
        }
        
        // 增加轮数
        long currentRound = rounds.incrementAndGet();
        
        // 选择要传播的节点
        List<String> targets = selectGossipTargets(messageId, GOSSIP_FANOUT);
        
        if (targets.isEmpty()) {
            logger.debug("没有可传播的节点，停止Gossip: {}", messageId);
            return;
        }
        
        // 转换为网络消息并发送
        NetworkMessage networkMessage = convertToNetworkMessage(message);
        
        for (String target : targets) {
            try {
                networkService.sendMessage(target, networkMessage);
                logger.debug("Gossip传播消息到节点: {} 轮次: {}", target, currentRound);
            } catch (Exception e) {
                logger.warn("Gossip传播消息到节点失败: " + target, e);
            }
        }
        
        // 延迟后继续传播
        gossipExecutor.schedule(() -> continueGossip(message), 
                              GOSSIP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    private List<String> selectGossipTargets(String messageId, int count) {
        Set<String> receivers = messageReceivers.get(messageId);
        if (receivers == null) {
            receivers = new HashSet<>();
        }
        
        List<PeerInfo> connectedPeers = networkService.getConnectedPeers();
        List<String> availablePeers = new ArrayList<>();
        
        for (PeerInfo peer : connectedPeers) {
            if (!receivers.contains(peer.getNodeId())) {
                availablePeers.add(peer.getNodeId());
            }
        }
        
        // 随机选择节点
        Collections.shuffle(availablePeers);
        return availablePeers.subList(0, Math.min(count, availablePeers.size()));
    }
    
    private NetworkMessage convertToNetworkMessage(GossipMessage gossipMessage) {
        NetworkMessage.MessageType type;
        Object payload;
        
        switch (gossipMessage.getType()) {
            case BLOCK_PROPOSAL:
                type = NetworkMessage.MessageType.BLOCK_PROPOSAL;
                payload = gossipMessage.getPayload();
                break;
            case BLOCK_VOTE:
                type = NetworkMessage.MessageType.BLOCK_VOTE;
                payload = gossipMessage.getPayload();
                break;
            case TRANSACTION:
                type = NetworkMessage.MessageType.TRANSACTION;
                payload = gossipMessage.getPayload();
                break;
            case VRF_ANNOUNCEMENT:
                type = NetworkMessage.MessageType.VRF_ANNOUNCEMENT;
                payload = gossipMessage.getPayload();
                break;
            default:
                throw new IllegalArgumentException("Unknown gossip message type: " + gossipMessage.getType());
        }
        
        return new NetworkMessage(type, null, System.currentTimeMillis(), 
                                generateNonce(), payload, new byte[64]);
    }
    
    private void cleanupExpiredMessages() {
        long currentTime = System.currentTimeMillis();
        List<String> expiredMessages = new ArrayList<>();
        
        for (Map.Entry<String, GossipMessage> entry : messageCache.entrySet()) {
            if (currentTime - entry.getValue().getTimestamp() > MESSAGE_TTL_MS) {
                expiredMessages.add(entry.getKey());
            }
        }
        
        for (String messageId : expiredMessages) {
            messageCache.remove(messageId);
            messageReceivers.remove(messageId);
            messageRounds.remove(messageId);
        }
        
        if (!expiredMessages.isEmpty()) {
            logger.debug("清理过期Gossip消息: {} 条", expiredMessages.size());
        }
    }
    
    private void pullMessages() {
        if (!running.get()) {
            return;
        }
        
        try {
            List<PeerInfo> connectedPeers = networkService.getConnectedPeers();
            if (connectedPeers.isEmpty()) {
                return;
            }
            
            // 随机选择几个节点进行Pull请求
            Collections.shuffle(connectedPeers);
            int pullCount = Math.min(3, connectedPeers.size());
            
            for (int i = 0; i < pullCount; i++) {
                PeerInfo peer = connectedPeers.get(i);
                requestMessagesFromPeer(peer);
            }
            
        } catch (Exception e) {
            logger.error("Pull消息失败", e);
        }
    }
    
    /**
     * 从指定节点请求消息
     * 
     * @param peer 节点信息
     */
    private void requestMessagesFromPeer(PeerInfo peer) {
        try {
            // 创建Pull请求消息
            Map<String, Object> pullRequest = new HashMap<>();
            pullRequest.put("type", "pull_request");
            pullRequest.put("timestamp", System.currentTimeMillis());
            pullRequest.put("requestedTypes", Arrays.asList(
                GossipMessage.MessageType.BLOCK_PROPOSAL,
                GossipMessage.MessageType.BLOCK_VOTE,
                GossipMessage.MessageType.TRANSACTION,
                GossipMessage.MessageType.VRF_ANNOUNCEMENT
            ));
            
            NetworkMessage requestMessage = new NetworkMessage(
                NetworkMessage.MessageType.PEER_DISCOVERY, // 复用消息类型
                null, // 发送者将在发送时设置
                System.currentTimeMillis(),
                generateNonce(),
                pullRequest,
                new byte[64] // 签名将在发送时设置
            );
            
            // 发送Pull请求
            networkService.sendMessage(peer.getNodeId(), requestMessage);
            
            logger.debug("发送Pull请求到节点: {}", peer.getNodeId());
            
        } catch (Exception e) {
            logger.warn("发送Pull请求失败: " + peer.getNodeId(), e);
        }
    }
    
    private boolean isMessageExpired(GossipMessage message) {
        return System.currentTimeMillis() - message.getTimestamp() > MESSAGE_TTL_MS;
    }
    
    private String generateMessageId() {
        return UUID.randomUUID().toString();
    }
    
    private long generateNonce() {
        return System.currentTimeMillis() + Thread.currentThread().threadId();
    }
    
    /**
     * Gossip消息监听器
     */
    private class GossipMessageListener implements MessageListener {
        
        @Override
        public void onMessageReceived(NetworkMessage message, String senderId) {
            if (!running.get()) {
                return;
            }
            
            // 检查是否是Gossip消息
            if (isGossipMessage(message)) {
                GossipMessage gossipMessage = convertFromNetworkMessage(message);
                handleGossipMessage(gossipMessage, senderId);
            }
        }
        
        @Override
        public void onMessageFailed(NetworkMessage message, String targetId, Throwable error) {
            logger.warn("Gossip消息发送失败到节点: " + targetId, error);
        }
        
        private boolean isGossipMessage(NetworkMessage message) {
            NetworkMessage.MessageType type = message.getType();
            return type == NetworkMessage.MessageType.BLOCK_PROPOSAL ||
                   type == NetworkMessage.MessageType.BLOCK_VOTE ||
                   type == NetworkMessage.MessageType.TRANSACTION ||
                   type == NetworkMessage.MessageType.VRF_ANNOUNCEMENT;
        }
        
        private GossipMessage convertFromNetworkMessage(NetworkMessage networkMessage) {
            GossipMessage.MessageType type;
            
            switch (networkMessage.getType()) {
                case BLOCK_PROPOSAL:
                    type = GossipMessage.MessageType.BLOCK_PROPOSAL;
                    break;
                case BLOCK_VOTE:
                    type = GossipMessage.MessageType.BLOCK_VOTE;
                    break;
                case TRANSACTION:
                    type = GossipMessage.MessageType.TRANSACTION;
                    break;
                case VRF_ANNOUNCEMENT:
                    type = GossipMessage.MessageType.VRF_ANNOUNCEMENT;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown network message type: " + networkMessage.getType());
            }
            
            return new GossipMessage(type, networkMessage.getPayload(), 
                                   networkMessage.getTimestamp(), generateMessageId());
        }
    }
    
    /**
     * Gossip消息
     */
    public static class GossipMessage {
        
        public enum MessageType {
            BLOCK_PROPOSAL,
            BLOCK_VOTE,
            TRANSACTION,
            VRF_ANNOUNCEMENT
        }
        
        private final MessageType type;
        private final Object payload;
        private final long timestamp;
        private final String messageId;
        
        public GossipMessage(MessageType type, Object payload, long timestamp, String messageId) {
            this.type = type;
            this.payload = payload;
            this.timestamp = timestamp;
            this.messageId = messageId;
        }
        
        public MessageType getType() {
            return type;
        }
        
        public Object getPayload() {
            return payload;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getMessageId() {
            return messageId;
        }
    }
    
    /**
     * Gossip统计信息
     */
    public static class GossipStats {
        private final int cachedMessages;
        private final int activeMessages;
        private final int totalRounds;
        
        public GossipStats(int cachedMessages, int activeMessages, int totalRounds) {
            this.cachedMessages = cachedMessages;
            this.activeMessages = activeMessages;
            this.totalRounds = totalRounds;
        }
        
        public int getCachedMessages() {
            return cachedMessages;
        }
        
        public int getActiveMessages() {
            return activeMessages;
        }
        
        public int getTotalRounds() {
            return totalRounds;
        }
        
        @Override
        public String toString() {
            return "GossipStats{" +
                   "cachedMessages=" + cachedMessages +
                   ", activeMessages=" + activeMessages +
                   ", totalRounds=" + totalRounds +
                   '}';
        }
    }
}
