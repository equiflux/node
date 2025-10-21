package io.equiflux.node.rpc.service;

import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.network.NetworkService;
import io.equiflux.node.rpc.dto.*;
import io.equiflux.node.rpc.exception.*;
import io.equiflux.node.storage.BlockStorageService;
import io.equiflux.node.storage.StateStorageService;
import io.equiflux.node.storage.TransactionStorageService;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.storage.model.ChainState;
import io.equiflux.node.crypto.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RPC服务实现
 * 
 * <p>实现JSON-RPC 2.0规范，提供区块链数据的查询和操作接口。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>区块查询和管理</li>
 *   <li>交易查询和管理</li>
 *   <li>账户状态查询</li>
 *   <li>链状态查询</li>
 *   <li>网络统计查询</li>
 *   <li>VRF信息查询</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class RpcService {
    
    private static final Logger logger = LoggerFactory.getLogger(RpcService.class);
    
    private final BlockStorageService blockStorageService;
    private final StateStorageService stateStorageService;
    private final TransactionStorageService transactionStorageService;
    private final NetworkService networkService;
    
    public RpcService(BlockStorageService blockStorageService,
                     StateStorageService stateStorageService,
                     TransactionStorageService transactionStorageService,
                     NetworkService networkService) {
        this.blockStorageService = blockStorageService;
        this.stateStorageService = stateStorageService;
        this.transactionStorageService = transactionStorageService;
        this.networkService = networkService;
    }
    
    // ==================== 区块相关方法 ====================
    
    /**
     * 获取最新区块
     * 
     * @return 最新区块信息
     * @throws RpcException RPC异常
     */
    public BlockInfoDto getLatestBlock() throws RpcException {
        try {
            Block block = blockStorageService.getLatestBlock();
            if (block == null) {
                throw new BlockNotFoundException("latest");
            }
            return convertToBlockInfoDto(block);
        } catch (BlockNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get latest block", e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get latest block", e);
        }
    }
    
    /**
     * 按高度获取区块
     * 
     * @param height 区块高度
     * @return 区块信息
     * @throws RpcException RPC异常
     */
    public BlockInfoDto getBlockByHeight(long height) throws RpcException {
        try {
            Block block = blockStorageService.getBlockByHeight(height);
            if (block == null) {
                throw new BlockNotFoundException(height);
            }
            return convertToBlockInfoDto(block);
        } catch (BlockNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get block by height: {}", height, e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get block by height: " + height, e);
        }
    }
    
    /**
     * 按哈希获取区块
     * 
     * @param hash 区块哈希
     * @return 区块信息
     * @throws RpcException RPC异常
     */
    public BlockInfoDto getBlockByHash(String hash) throws RpcException {
        try {
            Block block = blockStorageService.getBlockByHash(hash);
            if (block == null) {
                throw new BlockNotFoundException(hash);
            }
            return convertToBlockInfoDto(block);
        } catch (BlockNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get block by hash: {}", hash, e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get block by hash: " + hash, e);
        }
    }
    
    /**
     * 获取区块范围
     * 
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @return 区块信息列表
     * @throws RpcException RPC异常
     */
    public List<BlockInfoDto> getBlocks(long startHeight, long endHeight) throws RpcException {
        try {
            List<Block> blocks = blockStorageService.getBlocks(startHeight, endHeight);
            if (blocks == null) {
                throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get blocks from " + startHeight + " to " + endHeight);
            }
            return blocks.stream()
                    .map(this::convertToBlockInfoDto)
                    .collect(Collectors.toList());
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get blocks from {} to {}", startHeight, endHeight, e);
            throw new RpcException(RpcError.INTERNAL_ERROR, 
                                 "Failed to get blocks from " + startHeight + " to " + endHeight, e);
        }
    }
    
    /**
     * 获取最近的区块
     * 
     * @param count 区块数量
     * @return 区块信息列表
     * @throws RpcException RPC异常
     */
    public List<BlockInfoDto> getRecentBlocks(int count) throws RpcException {
        try {
            List<Block> blocks = blockStorageService.getRecentBlocks(count);
            if (blocks == null) {
                throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get recent blocks");
            }
            return blocks.stream()
                    .map(this::convertToBlockInfoDto)
                    .collect(Collectors.toList());
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get recent {} blocks", count, e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get recent blocks", e);
        }
    }
    
    /**
     * 获取当前区块高度
     * 
     * @return 当前区块高度
     * @throws RpcException RPC异常
     */
    public long getCurrentHeight() throws RpcException {
        try {
            return blockStorageService.getCurrentHeight();
        } catch (Exception e) {
            logger.error("Failed to get current height", e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get current height", e);
        }
    }
    
    // ==================== 交易相关方法 ====================
    
    /**
     * 按哈希获取交易
     * 
     * @param hash 交易哈希
     * @return 交易信息
     * @throws RpcException RPC异常
     */
    public TransactionInfoDto getTransactionByHash(String hash) throws RpcException {
        try {
            Transaction transaction = transactionStorageService.getTransactionByHash(hash);
            if (transaction == null) {
                throw new TransactionNotFoundException(hash);
            }
            return convertToTransactionInfoDto(transaction);
        } catch (TransactionNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get transaction by hash: {}", hash, e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get transaction by hash: " + hash, e);
        }
    }
    
    /**
     * 广播交易
     * 
     * @param transaction 交易
     * @return 交易哈希
     * @throws RpcException RPC异常
     */
    public String broadcastTransaction(Transaction transaction) throws RpcException {
        try {
            // 验证交易
            validateTransaction(transaction);
            
            // 广播交易
            networkService.broadcastTransaction(transaction).join();
            
            // 存储交易
            transactionStorageService.storeTransaction(transaction);
            
            logger.info("Transaction broadcasted successfully: {}", transaction.getHashHex());
            return transaction.getHashHex();
            
        } catch (RpcException e) {
            // 重新抛出RPC异常，不包装
            throw e;
        } catch (Exception e) {
            logger.error("Failed to broadcast transaction", e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to broadcast transaction", e);
        }
    }
    
    // ==================== 账户相关方法 ====================
    
    /**
     * 获取账户信息
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户信息
     * @throws RpcException RPC异常
     */
    public AccountInfoDto getAccountInfo(String publicKeyHex) throws RpcException {
        try {
            if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
                throw new AccountNotFoundException("");
            }
            
            AccountState accountState = stateStorageService.getAccountStateByPublicKeyHex(publicKeyHex);
            
            if (accountState == null) {
                throw new AccountNotFoundException(publicKeyHex);
            }
            
            return convertToAccountInfoDto(accountState);
            
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get account info: {}", publicKeyHex, e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get account info: " + publicKeyHex, e);
        }
    }
    
    /**
     * 获取账户余额
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户余额
     * @throws RpcException RPC异常
     */
    public long getAccountBalance(String publicKeyHex) throws RpcException {
        try {
            AccountInfoDto accountInfo = getAccountInfo(publicKeyHex);
            return accountInfo.getBalance() != null ? accountInfo.getBalance() : 0L;
        } catch (AccountNotFoundException e) {
            return 0; // 账户不存在时返回0余额
        } catch (RpcException e) {
            // 如果是内部错误，重新包装消息
            if (e.getRpcError().getCode() == RpcError.INTERNAL_ERROR) {
                throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get account balance: " + publicKeyHex, e.getCause());
            }
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get account balance: {}", publicKeyHex, e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get account balance: " + publicKeyHex, e);
        }
    }
    
    /**
     * 获取账户质押金额
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户质押金额
     * @throws RpcException RPC异常
     */
    public long getAccountStake(String publicKeyHex) throws RpcException {
        try {
            AccountInfoDto accountInfo = getAccountInfo(publicKeyHex);
            return accountInfo.getStakeAmount() != null ? accountInfo.getStakeAmount() : 0L;
        } catch (AccountNotFoundException e) {
            return 0; // 账户不存在时返回0质押
        } catch (RpcException e) {
            // 如果是内部错误，重新包装消息
            if (e.getRpcError().getCode() == RpcError.INTERNAL_ERROR) {
                throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get account stake: " + publicKeyHex, e.getCause());
            }
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get account stake: {}", publicKeyHex, e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get account stake: " + publicKeyHex, e);
        }
    }
    
    // ==================== 链状态相关方法 ====================
    
    /**
     * 获取链状态
     * 
     * @return 链状态信息
     * @throws RpcException RPC异常
     */
    public ChainStateDto getChainState() throws RpcException {
        try {
            ChainState chainState = stateStorageService.getChainState();
            if (chainState == null) {
                throw new RpcException(RpcError.INTERNAL_ERROR, "Chain state not found");
            }
            return convertToChainStateDto(chainState);
        } catch (Exception e) {
            logger.error("Failed to get chain state", e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get chain state", e);
        }
    }
    
    // ==================== 网络相关方法 ====================
    
    /**
     * 获取网络统计信息
     * 
     * @return 网络统计信息
     * @throws RpcException RPC异常
     */
    public NetworkStatsDto getNetworkStats() throws RpcException {
        try {
            // 这里需要从NetworkService获取统计信息
            // 由于NetworkService接口中没有统计方法，这里返回模拟数据
            NetworkStatsDto stats = new NetworkStatsDto();
            stats.setTotalPeers(50);
            stats.setConnectedPeers(25);
            stats.setActivePeers(20);
            stats.setUptime(System.currentTimeMillis());
            stats.setNetworkVersion("1.0.0");
            stats.setProtocolVersion("1.0.0");
            
            return stats;
        } catch (Exception e) {
            logger.error("Failed to get network stats", e);
            throw new RpcException(RpcError.INTERNAL_ERROR, "Failed to get network stats", e);
        }
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 转换Block为BlockInfoDto
     */
    private BlockInfoDto convertToBlockInfoDto(Block block) {
        BlockInfoDto dto = new BlockInfoDto();
        dto.setHeight(block.getHeight());
        dto.setHash(block.getHashHex());
        dto.setPreviousHash(block.getPreviousHashHex());
        dto.setTimestamp(block.getTimestamp());
        dto.setRound((long) block.getRound());
        dto.setProposer(block.getProposerHex());
        dto.setVrfOutput(block.getVrfOutputHex());
        dto.setVrfProof(block.getVrfProof() != null ? block.getVrfProof().toString() : null);
        dto.setMerkleRoot(block.getMerkleRootHex());
        dto.setNonce(block.getNonce());
        dto.setDifficultyTarget(block.getDifficultyTarget().toString());
        dto.setTransactionCount(block.getTransactions().size());
        
        // 转换VRF公告
        if (block.getAllVRFAnnouncements() != null) {
            List<VrfAnnouncementDto> vrfDtos = block.getAllVRFAnnouncements().stream()
                    .map(this::convertToVrfAnnouncementDto)
                    .collect(Collectors.toList());
            dto.setAllVrfAnnouncements(vrfDtos);
        }
        
        // 转换交易
        if (block.getTransactions() != null) {
            List<TransactionInfoDto> txDtos = block.getTransactions().stream()
                    .map(this::convertToTransactionInfoDto)
                    .collect(Collectors.toList());
            dto.setTransactions(txDtos);
        }
        
        return dto;
    }
    
    /**
     * 转换Transaction为TransactionInfoDto
     */
    private TransactionInfoDto convertToTransactionInfoDto(Transaction transaction) {
        TransactionInfoDto dto = new TransactionInfoDto();
        dto.setHash(transaction.getHashHex());
        dto.setFrom(transaction.getSenderPublicKeyHex());
        dto.setTo(transaction.getReceiverPublicKeyHex());
        dto.setAmount(transaction.getAmount());
        dto.setFee(transaction.getFee());
        dto.setNonce(transaction.getNonce());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setSignature(transaction.getSignature() != null ? HashUtils.toHexString(transaction.getSignature()) : null);
        dto.setData(null); // Transaction类没有data字段
        
        return dto;
    }
    
    /**
     * 转换VRFAnnouncement为VrfAnnouncementDto
     */
    private VrfAnnouncementDto convertToVrfAnnouncementDto(VRFAnnouncement announcement) {
        VrfAnnouncementDto dto = new VrfAnnouncementDto();
        dto.setNodeId(announcement.getPublicKeyHex()); // 使用公钥作为节点ID
        dto.setPublicKey(announcement.getPublicKeyHex());
        dto.setVrfOutput(announcement.getVrfOutput() != null ? announcement.getVrfOutput().toString() : null);
        dto.setVrfProof(announcement.getVrfProof() != null ? announcement.getVrfProof().toString() : null);
        dto.setRound(announcement.getRound());
        dto.setTimestamp(announcement.getTimestamp());
        dto.setScore(announcement.getScore());
        
        return dto;
    }
    
    /**
     * 转换AccountState为AccountInfoDto
     */
    private AccountInfoDto convertToAccountInfoDto(AccountState accountState) {
        AccountInfoDto dto = new AccountInfoDto();
        dto.setPublicKey(accountState.getPublicKeyHex());
        dto.setAddress(accountState.getPublicKeyHex()); // 简化处理，使用公钥作为地址
        dto.setBalance(accountState.getBalance());
        dto.setStakeAmount(accountState.getStakeAmount());
        dto.setNonce(accountState.getNonce());
        dto.setLastUpdated(accountState.getLastUpdateTimestamp());
        
        // 判断是否为超级节点
        dto.setIsSuperNode(accountState.getStakeAmount() >= 100000); // 假设质押>=100000为超级节点
        
        return dto;
    }
    
    /**
     * 转换ChainState为ChainStateDto
     */
    private ChainStateDto convertToChainStateDto(ChainState chainState) {
        ChainStateDto dto = new ChainStateDto();
        dto.setCurrentHeight(chainState.getCurrentHeight());
        dto.setCurrentRound(chainState.getCurrentRound());
        dto.setTotalSupply(chainState.getTotalSupply());
        dto.setCurrentDifficulty(chainState.getCurrentDifficulty().toString());
        dto.setBlockTime(3000L); // 3秒出块时间
        dto.setSuperNodeCount(50);
        dto.setCoreNodeCount(20);
        dto.setRotateNodeCount(30);
        dto.setRewardedTopX(15);
        dto.setConsensusVersion("1.0.0");
        dto.setNetworkId("equiflux-mainnet");
        dto.setChainId("equiflux-chain");
        
        return dto;
    }
    
    /**
     * 验证交易
     */
    private void validateTransaction(Transaction transaction) throws RpcException {
        // 基本验证
        if (transaction == null) {
            throw new RpcException(RpcError.INVALID_PARAMS, "Transaction cannot be null");
        }
        
        if (transaction.getAmount() <= 0) {
            throw new RpcException(RpcError.INVALID_PARAMS, "Transaction amount must be positive");
        }
        
        if (transaction.getFee() < 0) {
            throw new RpcException(RpcError.INVALID_PARAMS, "Transaction fee cannot be negative");
        }
        
        // 验证签名
        try {
            // 这里应该调用签名验证逻辑
            // SignatureVerifier.verify(transaction);
        } catch (Exception e) {
            throw new InvalidSignatureException("Transaction signature verification failed", e);
        }
    }
}
