package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.network.NetworkService;
import io.equiflux.node.storage.TransactionStorageService;
import io.equiflux.node.wallet.model.TransactionInfo;
import io.equiflux.node.wallet.model.TransactionState;
import io.equiflux.node.wallet.model.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 交易广播服务
 * 
 * <p>负责交易的广播、状态跟踪和历史查询。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class TransactionBroadcastService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionBroadcastService.class);
    
    private final NetworkService networkService;
    private final TransactionStorageService transactionStorageService;
    private final TransactionSigningService transactionSigningService;
    
    // 交易状态跟踪
    private final ConcurrentHashMap<String, TransactionStatus> transactionStatusMap = new ConcurrentHashMap<>();
    
    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public TransactionBroadcastService(NetworkService networkService,
                                      TransactionStorageService transactionStorageService,
                                      TransactionSigningService transactionSigningService) {
        this.networkService = networkService;
        this.transactionStorageService = transactionStorageService;
        this.transactionSigningService = transactionSigningService;
        
        // 启动交易状态检查任务
        startTransactionStatusChecker();
    }
    
    /**
     * 广播交易
     * 
     * @param transaction 要广播的交易
     * @return 交易哈希
     * @throws WalletException 钱包异常
     */
    public String broadcastTransaction(Transaction transaction) throws WalletException {
        if (transaction == null) {
            throw new WalletException("Transaction cannot be null");
        }
        
        try {
            logger.info("Broadcasting transaction: hash={}", transaction.getHash());
            
            // 验证交易签名
            if (!transactionSigningService.verifyTransactionSignature(transaction)) {
                throw new WalletException("Invalid transaction signature");
            }
            
            // 存储交易到交易池
            transactionStorageService.storeTransaction(transaction);
            
            // 初始化交易状态
            String transactionHash = transaction.getHashHex();
            TransactionStatus status = new TransactionStatus(
                transactionHash,
                TransactionState.PENDING,
                LocalDateTime.now(),
                null,
                null,
                null
            );
            transactionStatusMap.put(transactionHash, status);
            
            // 广播交易到网络
            networkService.broadcastTransaction(transaction);
            
            logger.info("Transaction broadcasted successfully: hash={}", transactionHash);
            return transactionHash;
        } catch (Exception e) {
            logger.error("Failed to broadcast transaction", e);
            throw new WalletException("Failed to broadcast transaction", e);
        }
    }
    
    /**
     * 获取交易状态
     * 
     * @param transactionHash 交易哈希
     * @return 交易状态
     * @throws WalletException 钱包异常
     */
    public Optional<TransactionStatus> getTransactionStatus(String transactionHash) throws WalletException {
        if (transactionHash == null || transactionHash.trim().isEmpty()) {
            throw new WalletException("Transaction hash cannot be null or empty");
        }
        
        try {
            // 先从内存中获取
            TransactionStatus status = transactionStatusMap.get(transactionHash);
            if (status != null) {
                return Optional.of(status);
            }
            
            // 从存储中查询交易
            Transaction transaction = transactionStorageService.getTransactionByHash(transactionHash);
            if (transaction == null) {
                return Optional.empty();
            }
            
            // 检查交易是否已确认 - 暂时都设为PENDING，因为Transaction模型中没有区块信息
            status = new TransactionStatus(
                transactionHash,
                TransactionState.PENDING,
                LocalDateTime.ofEpochSecond(transaction.getTimestamp() / 1000, 0, java.time.ZoneOffset.UTC),
                null,
                null,
                null
            );
            
            // 更新内存状态
            transactionStatusMap.put(transactionHash, status);
            
            return Optional.of(status);
        } catch (Exception e) {
            logger.error("Failed to get transaction status", e);
            throw new WalletException("Failed to get transaction status", e);
        }
    }
    
    /**
     * 获取交易历史
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 交易历史列表
     * @throws WalletException 钱包异常
     */
    public List<TransactionInfo> getTransactionHistory(String publicKeyHex, int limit, int offset) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (limit <= 0) {
            limit = 50; // 默认限制
        }
        if (offset < 0) {
            offset = 0;
        }
        
        try {
            logger.debug("Getting transaction history: publicKey={}, limit={}, offset={}", 
                        publicKeyHex, limit, offset);
            
            List<TransactionInfo> transactionInfos = new ArrayList<>();
            
            // 将公钥字符串转换为PublicKey
            byte[] publicKeyBytes = io.equiflux.node.crypto.HashUtils.fromHexString(publicKeyHex);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("Ed25519", "SunEC");
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(publicKeyBytes);
            java.security.PublicKey publicKey = keyFactory.generatePublic(keySpec);
            
            // 获取发送的交易
            List<Transaction> sentTransactions = transactionStorageService.getTransactionsBySender(publicKey);
            
            // 获取接收的交易
            List<Transaction> receivedTransactions = transactionStorageService.getTransactionsByReceiver(publicKey);
            
            // 合并并排序
            List<Transaction> allTransactions = new ArrayList<>();
            allTransactions.addAll(sentTransactions);
            allTransactions.addAll(receivedTransactions);
            
            // 按时间戳排序（最新的在前）
            allTransactions.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
            
            // 应用分页
            int startIndex = offset;
            int endIndex = Math.min(startIndex + limit, allTransactions.size());
            
            for (int i = startIndex; i < endIndex; i++) {
                Transaction transaction = allTransactions.get(i);
                TransactionInfo transactionInfo = convertToTransactionInfo(transaction);
                transactionInfos.add(transactionInfo);
            }
            
            logger.debug("Retrieved {} transaction history entries", transactionInfos.size());
            return transactionInfos;
        } catch (Exception e) {
            logger.error("Failed to get transaction history", e);
            throw new WalletException("Failed to get transaction history", e);
        }
    }
    
    /**
     * 更新交易状态
     * 
     * @param transactionHash 交易哈希
     * @param state 新状态
     * @param blockHeight 区块高度
     * @param blockIndex 区块内索引
     * @param errorMessage 错误消息
     */
    public void updateTransactionStatus(String transactionHash, TransactionState state, 
                                       Long blockHeight, Integer blockIndex, String errorMessage) {
        try {
            TransactionStatus currentStatus = transactionStatusMap.get(transactionHash);
            if (currentStatus != null) {
                TransactionStatus newStatus = new TransactionStatus(
                    transactionHash,
                    state,
                    currentStatus.getTimestamp(),
                    blockHeight,
                    blockIndex,
                    errorMessage
                );
                transactionStatusMap.put(transactionHash, newStatus);
                
                logger.debug("Updated transaction status: hash={}, state={}", transactionHash, state);
            }
        } catch (Exception e) {
            logger.error("Failed to update transaction status", e);
        }
    }
    
    /**
     * 清理过期的交易状态
     */
    public void cleanupExpiredTransactionStatus() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24); // 24小时前
            
            List<String> expiredHashes = new ArrayList<>();
            for (Map.Entry<String, TransactionStatus> entry : transactionStatusMap.entrySet()) {
                TransactionStatus status = entry.getValue();
                if (status.getTimestamp().isBefore(cutoffTime) && 
                    status.getState() != TransactionState.CONFIRMED) {
                    expiredHashes.add(entry.getKey());
                }
            }
            
            for (String hash : expiredHashes) {
                transactionStatusMap.remove(hash);
            }
            
            if (!expiredHashes.isEmpty()) {
                logger.debug("Cleaned up {} expired transaction statuses", expiredHashes.size());
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup expired transaction status", e);
        }
    }
    
    /**
     * 启动交易状态检查任务
     */
    private void startTransactionStatusChecker() {
        // 每10秒检查一次交易状态
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkTransactionStatuses();
            } catch (Exception e) {
                logger.error("Error in transaction status checker", e);
            }
        }, 10, 10, TimeUnit.SECONDS);
        
        // 每小时清理一次过期状态
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredTransactionStatus();
            } catch (Exception e) {
                logger.error("Error in transaction status cleanup", e);
            }
        }, 1, 1, TimeUnit.HOURS);
    }
    
    /**
     * 检查交易状态
     */
    private void checkTransactionStatuses() {
        try {
            for (String transactionHash : transactionStatusMap.keySet()) {
                TransactionStatus status = transactionStatusMap.get(transactionHash);
                if (status.getState() == TransactionState.PENDING) {
                    // 检查交易是否已确认
                    Transaction transaction = transactionStorageService.getTransactionByHash(transactionHash);
                    if (transaction != null) {
                        // 暂时保持PENDING状态，因为Transaction模型中没有区块信息
                        // 这里可以添加更复杂的状态检查逻辑
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check transaction statuses", e);
        }
    }
    
    /**
     * 转换交易为交易信息
     * 
     * @param transaction 交易
     * @return 交易信息
     */
    private TransactionInfo convertToTransactionInfo(Transaction transaction) {
        TransactionState state;
        Long blockHeight = null;
        Integer blockIndex = null;
        String errorMessage = null;
        
        // 暂时都设为PENDING，因为Transaction模型中没有区块信息
        state = TransactionState.PENDING;
        
        return new TransactionInfo(
            transaction,
            transaction.getHashHex(),
            state,
            LocalDateTime.ofEpochSecond(transaction.getTimestamp() / 1000, 0, java.time.ZoneOffset.UTC),
            blockHeight,
            blockIndex,
            errorMessage
        );
    }
}
