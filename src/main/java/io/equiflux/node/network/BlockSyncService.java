package io.equiflux.node.network;

import io.equiflux.node.model.Block;
import io.equiflux.node.storage.BlockStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 区块同步服务
 * 
 * <p>负责与其他节点同步区块数据，确保节点拥有最新的区块链状态。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>区块同步请求</li>
 *   <li>区块数据验证</li>
 *   <li>同步状态管理</li>
 *   <li>断点续传</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class BlockSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockSyncService.class);
    
    @Autowired
    private NetworkService networkService;
    
    @Autowired
    private BlockStorageService blockStorageService;
    
    @Autowired
    private NetworkConfig networkConfig;
    
    // 同步配置
    private static final int MAX_BLOCKS_PER_REQUEST = 100;
    private static final long SYNC_TIMEOUT_MS = 30000; // 30秒
    private static final int MAX_CONCURRENT_SYNCS = 3;
    private static final long SYNC_RETRY_DELAY_MS = 5000; // 5秒
    
    // 同步状态
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);
    private final AtomicLong lastSyncHeight = new AtomicLong(0);
    private final AtomicLong syncStartTime = new AtomicLong(0);
    
    // 同步任务
    private final Map<String, SyncTask> activeSyncTasks = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<SyncResult>> pendingSyncs = new ConcurrentHashMap<>();
    
    // 执行器
    private ExecutorService syncExecutor;
    private ScheduledExecutorService retryExecutor;
    
    // 统计信息
    private final AtomicLong totalBlocksSynced = new AtomicLong(0);
    private final AtomicLong syncFailures = new AtomicLong(0);
    
    @PostConstruct
    public void init() {
        logger.info("初始化区块同步服务");
        
        // 创建执行器
        syncExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_SYNCS, 
            r -> new Thread(r, "block-sync"));
        retryExecutor = Executors.newScheduledThreadPool(2, 
            r -> new Thread(r, "sync-retry"));
        
        // 注册消息监听器
        networkService.addMessageListener(new SyncMessageListener());
        
        logger.info("区块同步服务初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        logger.info("停止区块同步服务");
        
        // 取消所有同步任务
        cancelAllSyncTasks();
        
        // 关闭执行器
        if (syncExecutor != null) {
            syncExecutor.shutdown();
            try {
                if (!syncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    syncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                syncExecutor.shutdownNow();
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
        
        logger.info("区块同步服务停止完成");
    }
    
    /**
     * 开始区块同步
     * 
     * @param targetHeight 目标高度
     * @return 同步结果
     */
    public CompletableFuture<SyncResult> startSync(long targetHeight) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (isSyncing.compareAndSet(false, true)) {
                    syncStartTime.set(System.currentTimeMillis());
                    
                    logger.info("开始区块同步，目标高度: {}", targetHeight);
                    
                    // 获取当前高度
                    long currentHeight = getCurrentHeight();
                    
                    if (currentHeight >= targetHeight) {
                        logger.info("区块已是最新，无需同步");
                        isSyncing.set(false);
                        return SyncResult.success(currentHeight, 0);
                    }
                    
                    // 执行同步
                    SyncResult result = performSync(currentHeight, targetHeight);
                    
                    isSyncing.set(false);
                    return result;
                    
                } else {
                    logger.warn("区块同步正在进行中");
                    return SyncResult.failed("Sync already in progress");
                }
                
            } catch (Exception e) {
                logger.error("区块同步失败", e);
                isSyncing.set(false);
                syncFailures.incrementAndGet();
                return SyncResult.failed("Sync failed: " + e.getMessage());
            }
        }, syncExecutor);
    }
    
    /**
     * 处理同步请求
     * 
     * @param request 同步请求
     * @param senderId 发送者ID
     * @return 同步响应
     */
    public SyncResponse handleSyncRequest(SyncRequest request, String senderId) {
        try {
            logger.debug("处理同步请求: {} 来自: {}", request, senderId);
            
            // 验证请求
            if (!request.isValid()) {
                return SyncResponse.failed("Invalid sync request", 
                                         request.getStartHeight(), request.getEndHeight());
            }
            
            // 获取区块
            List<Block> blocks = getBlocks(request.getStartHeight(), request.getEndHeight());
            
            if (blocks.isEmpty()) {
                return SyncResponse.notFound(request.getStartHeight(), request.getEndHeight());
            }
            
            // 检查是否部分响应
            if (blocks.size() < request.getRequestedBlockCount()) {
                return SyncResponse.partial(blocks, request.getStartHeight(), request.getEndHeight());
            }
            
            return SyncResponse.success(blocks, request.getStartHeight(), request.getEndHeight());
            
        } catch (Exception e) {
            logger.error("处理同步请求失败", e);
            return SyncResponse.failed("Internal error: " + e.getMessage(),
                                     request.getStartHeight(), request.getEndHeight());
        }
    }
    
    /**
     * 检查同步状态
     * 
     * @return true如果正在同步，false否则
     */
    public boolean isSyncing() {
        return isSyncing.get();
    }
    
    /**
     * 获取同步统计信息
     * 
     * @return 同步统计信息
     */
    public SyncStats getStats() {
        return new SyncStats(
            totalBlocksSynced.get(),
            syncFailures.get(),
            activeSyncTasks.size(),
            pendingSyncs.size(),
            lastSyncHeight.get(),
            syncStartTime.get()
        );
    }
    
    // 私有方法
    
    private long getCurrentHeight() {
        try {
            // 从存储服务获取当前高度
            return blockStorageService.getCurrentHeight();
        } catch (Exception e) {
            logger.error("获取当前高度失败", e);
            return 0;
        }
    }
    
    private List<Block> getBlocks(long startHeight, long endHeight) {
        try {
            List<Block> blocks = new ArrayList<>();
            
            for (long height = startHeight; height <= endHeight; height++) {
                Block block = blockStorageService.getBlockByHeight(height);
                if (block != null) {
                    blocks.add(block);
                }
            }
            
            return blocks;
            
        } catch (Exception e) {
            logger.error("获取区块失败", e);
            return new ArrayList<>();
        }
    }
    
    private SyncResult performSync(long currentHeight, long targetHeight) {
        try {
            long totalBlocks = targetHeight - currentHeight;
            long syncedBlocks = 0;
            
            // 分批同步
            while (currentHeight < targetHeight) {
                long endHeight = Math.min(currentHeight + MAX_BLOCKS_PER_REQUEST - 1, targetHeight);
                
                // 选择同步节点
                String peerId = selectSyncPeer();
                if (peerId == null) {
                    return SyncResult.failed("No available peers for sync");
                }
                
                // 创建同步请求
                SyncRequest request = new SyncRequest(
                    currentHeight,
                    endHeight,
                    MAX_BLOCKS_PER_REQUEST,
                    System.currentTimeMillis()
                );
                
                // 发送同步请求
                SyncResponse response = requestSyncFromPeer(peerId, request);
                
                if (response.isSuccess() && response.hasBlocks()) {
                    // 保存区块
                    for (Block block : response.getBlocks()) {
                        try {
                            blockStorageService.storeBlock(block);
                            syncedBlocks++;
                            totalBlocksSynced.incrementAndGet();
                        } catch (Exception e) {
                            logger.error("保存区块失败: {}", block.getHeight(), e);
                        }
                    }
                    
                    currentHeight = endHeight + 1;
                    lastSyncHeight.set(currentHeight);
                    
                } else {
                    logger.warn("同步请求失败: {}", response.getErrorMessage());
                    return SyncResult.failed("Sync request failed: " + response.getErrorMessage());
                }
            }
            
            logger.info("区块同步完成，同步区块数: {}", syncedBlocks);
            return SyncResult.success(targetHeight, syncedBlocks);
            
        } catch (Exception e) {
            logger.error("执行同步失败", e);
            return SyncResult.failed("Sync execution failed: " + e.getMessage());
        }
    }
    
    private String selectSyncPeer() {
        List<PeerInfo> connectedPeers = networkService.getConnectedPeers();
        if (connectedPeers.isEmpty()) {
            return null;
        }
        
        // 简单选择：选择第一个连接的节点
        // 实际实现中可以根据节点性能、延迟等因素选择
        return connectedPeers.get(0).getNodeId();
    }
    
    private SyncResponse requestSyncFromPeer(String peerId, SyncRequest request) {
        try {
            // 创建网络消息
            NetworkMessage message = new NetworkMessage(
                NetworkMessage.MessageType.SYNC_REQUEST,
                null, // 发送者将在发送时设置
                System.currentTimeMillis(),
                System.currentTimeMillis() + Thread.currentThread().threadId(),
                request,
                new byte[64] // 签名将在发送时设置
            );
            
            // 发送请求
            CompletableFuture<Void> future = networkService.sendMessage(peerId, message);
            
            // 等待响应（简化实现）
            future.get(SYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            // 模拟响应（实际实现中应该通过回调机制获取响应）
            return createMockSyncResponse(request);
            
        } catch (Exception e) {
            logger.error("请求同步失败: " + peerId, e);
            return SyncResponse.failed("Request failed: " + e.getMessage(),
                                     request.getStartHeight(), request.getEndHeight());
        }
    }
    
    private SyncResponse createMockSyncResponse(SyncRequest request) {
        // 模拟同步响应
        List<Block> mockBlocks = new ArrayList<>();
        
        for (long height = request.getStartHeight(); height <= request.getEndHeight(); height++) {
            // 创建模拟区块
            Block mockBlock = createMockBlock(height);
            mockBlocks.add(mockBlock);
        }
        
        return SyncResponse.success(mockBlocks, request.getStartHeight(), request.getEndHeight());
    }
    
    private Block createMockBlock(long height) {
        // 创建模拟区块（实际实现中应该从存储中获取）
        return new Block.Builder()
                .height(height)
                .round(1)
                .timestamp(System.currentTimeMillis())
                .previousHash(new byte[32])
                .proposer(new byte[32])
                .vrfOutput(new byte[32])
                .vrfProof(null)
                .allVRFAnnouncements(new ArrayList<>())
                .rewardedNodes(new ArrayList<>())
                .transactions(new ArrayList<>())
                .nonce(0)
                .difficultyTarget(BigInteger.valueOf(1000000))
                .signatures(new HashMap<>())
                .build();
    }
    
    private void cancelAllSyncTasks() {
        // 取消所有待处理的同步
        pendingSyncs.values().forEach(future -> future.cancel(true));
        pendingSyncs.clear();
        
        // 取消所有活跃的同步任务
        activeSyncTasks.values().forEach(task -> task.cancel());
        activeSyncTasks.clear();
    }
    
    /**
     * 同步消息监听器
     */
    private class SyncMessageListener implements MessageListener {
        
        @Override
        public void onMessageReceived(NetworkMessage message, String senderId) {
            if (message.getType() == NetworkMessage.MessageType.SYNC_REQUEST) {
                SyncRequest request = message.getSyncRequestPayload();
                if (request != null) {
                    SyncResponse response = handleSyncRequest(request, senderId);
                    
                    // 发送响应
                    NetworkMessage responseMessage = new NetworkMessage(
                        NetworkMessage.MessageType.SYNC_RESPONSE,
                        null, // 发送者将在发送时设置
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + Thread.currentThread().threadId(),
                        response,
                        new byte[64] // 签名将在发送时设置
                    );
                    
                    networkService.sendMessage(senderId, responseMessage);
                }
            }
        }
        
        @Override
        public void onMessageFailed(NetworkMessage message, String targetId, Throwable error) {
            logger.warn("同步消息发送失败到节点: " + targetId, error);
        }
    }
    
    /**
     * 同步任务
     */
    private static class SyncTask {
        private final String peerId;
        private final SyncRequest request;
        private final CompletableFuture<SyncResult> future;
        private volatile boolean cancelled = false;
        
        public SyncTask(String peerId, SyncRequest request, CompletableFuture<SyncResult> future) {
            this.peerId = peerId;
            this.request = request;
            this.future = future;
        }
        
        public String getPeerId() { return peerId; }
        public SyncRequest getRequest() { return request; }
        public CompletableFuture<SyncResult> getFuture() { return future; }
        
        public void cancel() {
            cancelled = true;
            future.cancel(true);
        }
        
        public boolean isCancelled() { return cancelled; }
    }
    
    /**
     * 同步结果
     */
    public static class SyncResult {
        private final boolean success;
        private final long finalHeight;
        private final long blocksSynced;
        private final String errorMessage;
        
        private SyncResult(boolean success, long finalHeight, long blocksSynced, String errorMessage) {
            this.success = success;
            this.finalHeight = finalHeight;
            this.blocksSynced = blocksSynced;
            this.errorMessage = errorMessage;
        }
        
        public static SyncResult success(long finalHeight, long blocksSynced) {
            return new SyncResult(true, finalHeight, blocksSynced, null);
        }
        
        public static SyncResult failed(String errorMessage) {
            return new SyncResult(false, 0, 0, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public long getFinalHeight() { return finalHeight; }
        public long getBlocksSynced() { return blocksSynced; }
        public String getErrorMessage() { return errorMessage; }
        
        @Override
        public String toString() {
            return "SyncResult{" +
                   "success=" + success +
                   ", finalHeight=" + finalHeight +
                   ", blocksSynced=" + blocksSynced +
                   ", errorMessage='" + errorMessage + '\'' +
                   '}';
        }
    }
    
    /**
     * 同步统计信息
     */
    public static class SyncStats {
        private final long totalBlocksSynced;
        private final long syncFailures;
        private final int activeTasks;
        private final int pendingTasks;
        private final long lastSyncHeight;
        private final long syncStartTime;
        
        public SyncStats(long totalBlocksSynced, long syncFailures, int activeTasks,
                        int pendingTasks, long lastSyncHeight, long syncStartTime) {
            this.totalBlocksSynced = totalBlocksSynced;
            this.syncFailures = syncFailures;
            this.activeTasks = activeTasks;
            this.pendingTasks = pendingTasks;
            this.lastSyncHeight = lastSyncHeight;
            this.syncStartTime = syncStartTime;
        }
        
        public long getTotalBlocksSynced() { return totalBlocksSynced; }
        public long getSyncFailures() { return syncFailures; }
        public int getActiveTasks() { return activeTasks; }
        public int getPendingTasks() { return pendingTasks; }
        public long getLastSyncHeight() { return lastSyncHeight; }
        public long getSyncStartTime() { return syncStartTime; }
        
        public double getSuccessRate() {
            long total = totalBlocksSynced + syncFailures;
            return total > 0 ? (double) totalBlocksSynced / total : 0.0;
        }
        
        @Override
        public String toString() {
            return "SyncStats{" +
                   "totalBlocksSynced=" + totalBlocksSynced +
                   ", syncFailures=" + syncFailures +
                   ", activeTasks=" + activeTasks +
                   ", pendingTasks=" + pendingTasks +
                   ", lastSyncHeight=" + lastSyncHeight +
                   ", syncStartTime=" + syncStartTime +
                   ", successRate=" + String.format("%.2f%%", getSuccessRate() * 100) +
                   '}';
        }
    }
}
