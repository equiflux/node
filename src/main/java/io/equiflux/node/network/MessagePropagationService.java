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
 * 消息传播服务
 * 
 * <p>负责区块和交易在网络中的传播，确保重要消息能够快速、可靠地到达所有节点。
 * 
 * <p>传播策略：
 * <ul>
 *   <li>优先级传播：重要消息优先传播</li>
 *   <li>批量传播：相似消息批量处理</li>
 *   <li>重传机制：确保消息到达</li>
 *   <li>流量控制：避免网络拥塞</li>
 * </ul>
 * 
 * <p>消息优先级：
 * <ul>
 *   <li>HIGH: 区块提议、区块投票</li>
 *   <li>MEDIUM: VRF公告</li>
 *   <li>LOW: 交易</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class MessagePropagationService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessagePropagationService.class);
    
    @Autowired
    private NetworkService networkService;
    
    @Autowired
    private GossipProtocol gossipProtocol;
    
    @Autowired
    private NetworkConfig networkConfig;
    
    // 消息优先级
    public enum MessagePriority {
        HIGH(1),    // 区块提议、区块投票
        MEDIUM(2),  // VRF公告
        LOW(3);     // 交易
        
        private final int level;
        
        MessagePriority(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
        
        public boolean isHigherThan(MessagePriority other) {
            return this.level < other.level;
        }
    }
    
    // 传播配置
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long BATCH_TIMEOUT_MS = 100;
    private static final int MAX_BATCH_SIZE = 10;
    
    // 消息队列
    private final PriorityBlockingQueue<PropagationMessage> messageQueue = 
        new PriorityBlockingQueue<>(1000, Comparator.comparing(PropagationMessage::getPriority));
    
    // 执行器
    private ExecutorService propagationExecutor;
    private ScheduledExecutorService retryExecutor;
    
    // 状态
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong messagesPropagated = new AtomicLong(0);
    private final AtomicLong messagesFailed = new AtomicLong(0);
    
    // 消息跟踪
    private final Map<String, PropagationMessage> pendingMessages = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> retryCounts = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        logger.info("初始化消息传播服务");
        
        // 创建执行器
        propagationExecutor = Executors.newFixedThreadPool(4, 
            r -> new Thread(r, "message-propagation"));
        retryExecutor = Executors.newScheduledThreadPool(2, 
            r -> new Thread(r, "message-retry"));
        
        running.set(true);
        
        // 启动传播任务
        startPropagationTasks();
        
        logger.info("消息传播服务初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        logger.info("停止消息传播服务");
        
        running.set(false);
        
        // 关闭执行器
        if (propagationExecutor != null) {
            propagationExecutor.shutdown();
            try {
                if (!propagationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    propagationExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                propagationExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (retryExecutor != null) {
            retryExecutor.shutdown();
            try {
                if (!retryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    retryExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                retryExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("消息传播服务停止完成");
    }
    
    /**
     * 传播区块提议
     * 
     * @param block 区块
     */
    public void propagateBlockProposal(Block block) {
        PropagationMessage message = new PropagationMessage(
            PropagationMessage.MessageType.BLOCK_PROPOSAL,
            block,
            MessagePriority.HIGH,
            System.currentTimeMillis()
        );
        
        queueMessage(message);
    }
    
    /**
     * 传播区块投票
     * 
     * @param block 区块
     */
    public void propagateBlockVote(Block block) {
        PropagationMessage message = new PropagationMessage(
            PropagationMessage.MessageType.BLOCK_VOTE,
            block,
            MessagePriority.HIGH,
            System.currentTimeMillis()
        );
        
        queueMessage(message);
    }
    
    /**
     * 传播交易
     * 
     * @param transaction 交易
     */
    public void propagateTransaction(Transaction transaction) {
        PropagationMessage message = new PropagationMessage(
            PropagationMessage.MessageType.TRANSACTION,
            transaction,
            MessagePriority.LOW,
            System.currentTimeMillis()
        );
        
        queueMessage(message);
    }
    
    /**
     * 传播VRF公告
     * 
     * @param announcement VRF公告
     */
    public void propagateVRFAnnouncement(VRFAnnouncement announcement) {
        PropagationMessage message = new PropagationMessage(
            PropagationMessage.MessageType.VRF_ANNOUNCEMENT,
            announcement,
            MessagePriority.MEDIUM,
            System.currentTimeMillis()
        );
        
        queueMessage(message);
    }
    
    /**
     * 批量传播消息
     * 
     * @param messages 消息列表
     */
    public void propagateBatch(List<PropagationMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }
        
        // 按优先级排序
        messages.sort(Comparator.comparing(PropagationMessage::getPriority));
        
        // 添加到队列
        for (PropagationMessage message : messages) {
            queueMessage(message);
        }
    }
    
    /**
     * 获取传播统计信息
     * 
     * @return 统计信息
     */
    public PropagationStats getStats() {
        return new PropagationStats(
            messagesPropagated.get(),
            messagesFailed.get(),
            messageQueue.size(),
            pendingMessages.size()
        );
    }
    
    // 私有方法
    
    private void startPropagationTasks() {
        // 启动消息处理任务
        for (int i = 0; i < 4; i++) {
            propagationExecutor.submit(this::processMessageQueue);
        }
        
        // 启动重试任务
        retryExecutor.scheduleWithFixedDelay(
            this::processRetries,
            RETRY_DELAY_MS,
            RETRY_DELAY_MS,
            TimeUnit.MILLISECONDS
        );
        
        // 启动批量处理任务
        retryExecutor.scheduleWithFixedDelay(
            this::processBatchMessages,
            BATCH_TIMEOUT_MS,
            BATCH_TIMEOUT_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    private void queueMessage(PropagationMessage message) {
        if (!running.get()) {
            return;
        }
        
        try {
            messageQueue.offer(message);
            logger.debug("消息已加入传播队列: {}", message.getType());
        } catch (Exception e) {
            logger.error("加入消息到传播队列失败", e);
        }
    }
    
    private void processMessageQueue() {
        while (running.get()) {
            try {
                PropagationMessage message = messageQueue.take();
                processMessage(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("处理消息队列失败", e);
            }
        }
    }
    
    private void processMessage(PropagationMessage message) {
        try {
            String messageId = message.getMessageId();
            
            // 检查是否已处理
            if (pendingMessages.containsKey(messageId)) {
                logger.debug("消息已在处理中，跳过: {}", messageId);
                return;
            }
            
            // 添加到待处理列表
            pendingMessages.put(messageId, message);
            retryCounts.put(messageId, new AtomicLong(0));
            
            // 根据消息类型选择传播方式
            switch (message.getType()) {
                case BLOCK_PROPOSAL:
                    propagateBlockProposalInternal(message);
                    break;
                case BLOCK_VOTE:
                    propagateBlockVoteInternal(message);
                    break;
                case TRANSACTION:
                    propagateTransactionInternal(message);
                    break;
                case VRF_ANNOUNCEMENT:
                    propagateVRFAnnouncementInternal(message);
                    break;
                default:
                    logger.warn("未知的消息类型: {}", message.getType());
            }
            
        } catch (Exception e) {
            logger.error("处理消息失败", e);
            messagesFailed.incrementAndGet();
        }
    }
    
    private void propagateBlockProposalInternal(PropagationMessage message) {
        Block block = (Block) message.getPayload();
        
        // 使用Gossip协议传播
        gossipProtocol.gossipBlockProposal(block);
        
        // 直接广播给所有连接的节点
        networkService.broadcastBlockProposal(block);
        
        messagesPropagated.incrementAndGet();
        logger.debug("区块提议传播完成: {}", block.getHashHex());
    }
    
    private void propagateBlockVoteInternal(PropagationMessage message) {
        Block block = (Block) message.getPayload();
        
        // 使用Gossip协议传播
        gossipProtocol.gossipBlockVote(block);
        
        // 直接广播给所有连接的节点
        networkService.broadcastBlockVote(block);
        
        messagesPropagated.incrementAndGet();
        logger.debug("区块投票传播完成: {}", block.getHashHex());
    }
    
    private void propagateTransactionInternal(PropagationMessage message) {
        Transaction transaction = (Transaction) message.getPayload();
        
        // 使用Gossip协议传播
        gossipProtocol.gossipTransaction(transaction);
        
        // 直接广播给所有连接的节点
        networkService.broadcastTransaction(transaction);
        
        messagesPropagated.incrementAndGet();
        logger.debug("交易传播完成: {}", transaction.getHashHex());
    }
    
    private void propagateVRFAnnouncementInternal(PropagationMessage message) {
        VRFAnnouncement announcement = (VRFAnnouncement) message.getPayload();
        
        // 使用Gossip协议传播
        gossipProtocol.gossipVRFAnnouncement(announcement);
        
        // 直接广播给所有连接的节点
        networkService.broadcastVRFAnnouncement(announcement);
        
        messagesPropagated.incrementAndGet();
        logger.debug("VRF公告传播完成: {}", announcement.getPublicKey());
    }
    
    private void processRetries() {
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, PropagationMessage> entry : pendingMessages.entrySet()) {
            String messageId = entry.getKey();
            PropagationMessage message = entry.getValue();
            AtomicLong retryCount = retryCounts.get(messageId);
            
            if (retryCount != null && retryCount.get() < MAX_RETRY_ATTEMPTS) {
                // 检查是否需要重试
                if (shouldRetry(message)) {
                    retryCount.incrementAndGet();
                    logger.debug("重试传播消息: {} 次数: {}", messageId, retryCount.get());
                    
                    // 重新处理消息
                    processMessage(message);
                }
            } else {
                // 达到最大重试次数，移除消息
                toRemove.add(messageId);
                messagesFailed.incrementAndGet();
                logger.warn("消息传播失败，达到最大重试次数: {}", messageId);
            }
        }
        
        // 清理失败的消息
        for (String messageId : toRemove) {
            pendingMessages.remove(messageId);
            retryCounts.remove(messageId);
        }
    }
    
    private void processBatchMessages() {
        List<PropagationMessage> batch = new ArrayList<>();
        
        // 收集批量消息
        PropagationMessage message;
        while (batch.size() < MAX_BATCH_SIZE && (message = messageQueue.poll()) != null) {
            batch.add(message);
        }
        
        if (!batch.isEmpty()) {
            propagateBatch(batch);
        }
    }
    
    private boolean shouldRetry(PropagationMessage message) {
        // 检查消息是否过期
        long age = System.currentTimeMillis() - message.getTimestamp();
        return age < networkConfig.getMessageExpirationMs();
    }
    
    /**
     * 传播消息
     */
    public static class PropagationMessage {
        
        public enum MessageType {
            BLOCK_PROPOSAL,
            BLOCK_VOTE,
            TRANSACTION,
            VRF_ANNOUNCEMENT
        }
        
        private final MessageType type;
        private final Object payload;
        private final MessagePriority priority;
        private final long timestamp;
        private final String messageId;
        
        public PropagationMessage(MessageType type, Object payload, 
                                MessagePriority priority, long timestamp) {
            this.type = type;
            this.payload = payload;
            this.priority = priority;
            this.timestamp = timestamp;
            this.messageId = UUID.randomUUID().toString();
        }
        
        public MessageType getType() {
            return type;
        }
        
        public Object getPayload() {
            return payload;
        }
        
        public MessagePriority getPriority() {
            return priority;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getMessageId() {
            return messageId;
        }
    }
    
    /**
     * 传播统计信息
     */
    public static class PropagationStats {
        private final long messagesPropagated;
        private final long messagesFailed;
        private final int queueSize;
        private final int pendingMessages;
        
        public PropagationStats(long messagesPropagated, long messagesFailed, 
                              int queueSize, int pendingMessages) {
            this.messagesPropagated = messagesPropagated;
            this.messagesFailed = messagesFailed;
            this.queueSize = queueSize;
            this.pendingMessages = pendingMessages;
        }
        
        public long getMessagesPropagated() {
            return messagesPropagated;
        }
        
        public long getMessagesFailed() {
            return messagesFailed;
        }
        
        public int getQueueSize() {
            return queueSize;
        }
        
        public int getPendingMessages() {
            return pendingMessages;
        }
        
        public double getSuccessRate() {
            long total = messagesPropagated + messagesFailed;
            return total > 0 ? (double) messagesPropagated / total : 0.0;
        }
        
        @Override
        public String toString() {
            return "PropagationStats{" +
                   "messagesPropagated=" + messagesPropagated +
                   ", messagesFailed=" + messagesFailed +
                   ", queueSize=" + queueSize +
                   ", pendingMessages=" + pendingMessages +
                   ", successRate=" + String.format("%.2f%%", getSuccessRate() * 100) +
                   '}';
        }
    }
}
