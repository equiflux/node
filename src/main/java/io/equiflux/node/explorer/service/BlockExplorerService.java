package io.equiflux.node.explorer.service;

import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.storage.BlockStorageService;
import io.equiflux.node.storage.StateStorageService;
import io.equiflux.node.storage.TransactionStorageService;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.storage.model.ChainState;
import io.equiflux.node.explorer.dto.*;
import io.equiflux.node.explorer.exception.ExplorerException;
import io.equiflux.node.crypto.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 区块浏览器服务
 * 
 * <p>提供区块链数据的查询和展示功能，支持区块、交易、账户等信息的查询。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>区块查询：按高度、哈希、时间范围查询</li>
 *   <li>交易查询：按哈希、地址、时间范围查询</li>
 *   <li>账户查询：余额、质押、交易历史</li>
 *   <li>统计信息：链状态、网络统计、节点信息</li>
 *   <li>搜索功能：支持多种搜索条件</li>
 *   <li>分页支持：大数据量查询的分页处理</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class BlockExplorerService {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockExplorerService.class);
    
    private final BlockStorageService blockStorageService;
    private final StateStorageService stateStorageService;
    private final TransactionStorageService transactionStorageService;
    
    public BlockExplorerService(BlockStorageService blockStorageService,
                               StateStorageService stateStorageService,
                               TransactionStorageService transactionStorageService) {
        this.blockStorageService = blockStorageService;
        this.stateStorageService = stateStorageService;
        this.transactionStorageService = transactionStorageService;
    }
    
    // ==================== 区块查询方法 ====================
    
    /**
     * 获取最新区块
     * 
     * @return 最新区块详细信息
     * @throws ExplorerException 查询异常
     */
    public BlockDetailDto getLatestBlock() throws ExplorerException {
        try {
            Block block = blockStorageService.getLatestBlock();
            if (block == null) {
                throw new ExplorerException("No blocks found");
            }
            return convertToBlockDetailDto(block);
        } catch (Exception e) {
            logger.error("Failed to get latest block", e);
            throw new ExplorerException("Failed to get latest block", e);
        }
    }
    
    /**
     * 按高度获取区块详细信息
     * 
     * @param height 区块高度
     * @return 区块详细信息
     * @throws ExplorerException 查询异常
     */
    public BlockDetailDto getBlockByHeight(long height) throws ExplorerException {
        try {
            Block block = blockStorageService.getBlockByHeight(height);
            if (block == null) {
                throw new ExplorerException("Block not found at height: " + height);
            }
            return convertToBlockDetailDto(block);
        } catch (Exception e) {
            logger.error("Failed to get block by height: {}", height, e);
            throw new ExplorerException("Failed to get block by height: " + height, e);
        }
    }
    
    /**
     * 按哈希获取区块详细信息
     * 
     * @param hash 区块哈希
     * @return 区块详细信息
     * @throws ExplorerException 查询异常
     */
    public BlockDetailDto getBlockByHash(String hash) throws ExplorerException {
        try {
            Block block = blockStorageService.getBlockByHash(hash);
            if (block == null) {
                throw new ExplorerException("Block not found with hash: " + hash);
            }
            return convertToBlockDetailDto(block);
        } catch (Exception e) {
            logger.error("Failed to get block by hash: {}", hash, e);
            throw new ExplorerException("Failed to get block by hash: " + hash, e);
        }
    }
    
    /**
     * 获取区块列表（分页）
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 区块列表和分页信息
     * @throws ExplorerException 查询异常
     */
    public BlockListDto getBlocks(int page, int size) throws ExplorerException {
        try {
            long currentHeight = blockStorageService.getCurrentHeight();
            long startHeight = Math.max(1, currentHeight - (page * size));
            long endHeight = Math.max(1, startHeight - size + 1);
            
            List<Block> blocks = blockStorageService.getBlocks(endHeight, startHeight);
            
            List<BlockSummaryDto> blockSummaries = blocks.stream()
                    .map(this::convertToBlockSummaryDto)
                    .collect(Collectors.toList());
            
            BlockListDto result = new BlockListDto();
            result.setBlocks(blockSummaries);
            result.setTotalBlocks(currentHeight);
            result.setPage(page);
            result.setSize(size);
            result.setTotalPages((int) Math.ceil((double) currentHeight / size));
            
            return result;
        } catch (Exception e) {
            logger.error("Failed to get blocks page: {}, size: {}", page, size, e);
            throw new ExplorerException("Failed to get blocks", e);
        }
    }
    
    /**
     * 获取区块范围
     * 
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @return 区块列表
     * @throws ExplorerException 查询异常
     */
    public List<BlockSummaryDto> getBlocks(long startHeight, long endHeight) throws ExplorerException {
        try {
            List<Block> blocks = blockStorageService.getBlocks(startHeight, endHeight);
            return blocks.stream()
                    .map(this::convertToBlockSummaryDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get blocks from {} to {}", startHeight, endHeight, e);
            throw new ExplorerException("Failed to get blocks", e);
        }
    }
    
    // ==================== 交易查询方法 ====================
    
    /**
     * 按哈希获取交易详细信息
     * 
     * @param hash 交易哈希
     * @return 交易详细信息
     * @throws ExplorerException 查询异常
     */
    public TransactionDetailDto getTransactionByHash(String hash) throws ExplorerException {
        try {
            Transaction transaction = transactionStorageService.getTransactionByHash(hash);
            if (transaction == null) {
                throw new ExplorerException("Transaction not found with hash: " + hash);
            }
            return convertToTransactionDetailDto(transaction);
        } catch (Exception e) {
            logger.error("Failed to get transaction by hash: {}", hash, e);
            throw new ExplorerException("Failed to get transaction by hash: " + hash, e);
        }
    }
    
    /**
     * 获取地址的交易历史
     * 
     * @param address 地址
     * @param page 页码
     * @param size 每页大小
     * @return 交易列表和分页信息
     * @throws ExplorerException 查询异常
     */
    public TransactionListDto getTransactionsByAddress(String address, int page, int size) throws ExplorerException {
        try {
            // 这里需要实现按地址查询交易的逻辑
            // 由于当前TransactionStorageService没有按地址查询的方法，这里返回空列表
            TransactionListDto result = new TransactionListDto();
            result.setTransactions(new ArrayList<>());
            result.setTotalTransactions(0);
            result.setPage(page);
            result.setSize(size);
            result.setTotalPages(0);
            
            return result;
        } catch (Exception e) {
            logger.error("Failed to get transactions for address: {}", address, e);
            throw new ExplorerException("Failed to get transactions for address: " + address, e);
        }
    }
    
    /**
     * 获取最近的交易
     * 
     * @param count 交易数量
     * @return 交易列表
     * @throws ExplorerException 查询异常
     */
    public List<TransactionSummaryDto> getRecentTransactions(int count) throws ExplorerException {
        try {
            // 从最新区块中获取交易
            Block latestBlock = blockStorageService.getLatestBlock();
            if (latestBlock == null) {
                return new ArrayList<>();
            }
            
            return latestBlock.getTransactions().stream()
                    .limit(count)
                    .map(this::convertToTransactionSummaryDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get recent transactions", e);
            throw new ExplorerException("Failed to get recent transactions", e);
        }
    }
    
    // ==================== 账户查询方法 ====================
    
    /**
     * 获取账户详细信息
     * 
     * @param address 账户地址
     * @return 账户详细信息
     * @throws ExplorerException 查询异常
     */
    public AccountDetailDto getAccountDetail(String address) throws ExplorerException {
        try {
            AccountState accountState = stateStorageService.getAccountStateByPublicKeyHex(address);
            if (accountState == null) {
                throw new ExplorerException("Account not found: " + address);
            }
            return convertToAccountDetailDto(accountState);
        } catch (Exception e) {
            logger.error("Failed to get account detail: {}", address, e);
            throw new ExplorerException("Failed to get account detail: " + address, e);
        }
    }
    
    /**
     * 获取账户余额
     * 
     * @param address 账户地址
     * @return 账户余额
     * @throws ExplorerException 查询异常
     */
    public long getAccountBalance(String address) throws ExplorerException {
        try {
            AccountState accountState = stateStorageService.getAccountStateByPublicKeyHex(address);
            return accountState != null ? accountState.getBalance() : 0L;
        } catch (Exception e) {
            logger.error("Failed to get account balance: {}", address, e);
            throw new ExplorerException("Failed to get account balance: " + address, e);
        }
    }
    
    /**
     * 获取账户质押金额
     * 
     * @param address 账户地址
     * @return 账户质押金额
     * @throws ExplorerException 查询异常
     */
    public long getAccountStake(String address) throws ExplorerException {
        try {
            AccountState accountState = stateStorageService.getAccountStateByPublicKeyHex(address);
            return accountState != null ? accountState.getStakeAmount() : 0L;
        } catch (Exception e) {
            logger.error("Failed to get account stake: {}", address, e);
            throw new ExplorerException("Failed to get account stake: " + address, e);
        }
    }
    
    // ==================== 统计信息方法 ====================
    
    /**
     * 获取链统计信息
     * 
     * @return 链统计信息
     * @throws ExplorerException 查询异常
     */
    public ChainStatsDto getChainStats() throws ExplorerException {
        try {
            ChainState chainState = stateStorageService.getChainState();
            if (chainState == null) {
                throw new ExplorerException("Chain state not found");
            }
            
            ChainStatsDto stats = new ChainStatsDto();
            stats.setCurrentHeight(chainState.getCurrentHeight());
            stats.setCurrentRound(chainState.getCurrentRound());
            stats.setTotalSupply(chainState.getTotalSupply());
            stats.setCurrentDifficulty(chainState.getCurrentDifficulty().toString());
            stats.setBlockTime(3000L); // 3秒出块时间
            stats.setSuperNodeCount(50);
            stats.setCoreNodeCount(20);
            stats.setRotateNodeCount(30);
            stats.setRewardedTopX(15);
            stats.setConsensusVersion("1.0.0");
            stats.setNetworkId("equiflux-mainnet");
            stats.setChainId("equiflux-chain");
            
            return stats;
        } catch (Exception e) {
            logger.error("Failed to get chain stats", e);
            throw new ExplorerException("Failed to get chain stats", e);
        }
    }
    
    /**
     * 获取网络统计信息
     * 
     * @return 网络统计信息
     * @throws ExplorerException 查询异常
     */
    public NetworkStatsDto getNetworkStats() throws ExplorerException {
        try {
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
            throw new ExplorerException("Failed to get network stats", e);
        }
    }
    
    // ==================== 搜索方法 ====================
    
    /**
     * 搜索功能
     * 
     * @param query 搜索查询
     * @return 搜索结果
     * @throws ExplorerException 查询异常
     */
    public SearchResultDto search(String query) throws ExplorerException {
        try {
            SearchResultDto result = new SearchResultDto();
            result.setQuery(query);
            
            // 判断查询类型
            if (isBlockHash(query)) {
                // 尝试作为区块哈希搜索
                try {
                    BlockDetailDto block = getBlockByHash(query);
                    result.setBlockResult(block);
                    result.setType("block");
                } catch (ExplorerException e) {
                    // 不是区块哈希，继续其他搜索
                }
            }
            
            if (isTransactionHash(query)) {
                // 尝试作为交易哈希搜索
                try {
                    TransactionDetailDto transaction = getTransactionByHash(query);
                    result.setTransactionResult(transaction);
                    result.setType("transaction");
                } catch (ExplorerException e) {
                    // 不是交易哈希，继续其他搜索
                }
            }
            
            if (isAddress(query)) {
                // 尝试作为地址搜索
                try {
                    AccountDetailDto account = getAccountDetail(query);
                    result.setAccountResult(account);
                    result.setType("account");
                } catch (ExplorerException e) {
                    // 不是有效地址
                }
            }
            
            if (isBlockHeight(query)) {
                // 尝试作为区块高度搜索
                try {
                    long height = Long.parseLong(query);
                    BlockDetailDto block = getBlockByHeight(height);
                    result.setBlockResult(block);
                    result.setType("block");
                } catch (NumberFormatException | ExplorerException e) {
                    // 不是有效高度
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Failed to search: {}", query, e);
            throw new ExplorerException("Failed to search: " + query, e);
        }
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 转换Block为BlockDetailDto
     */
    private BlockDetailDto convertToBlockDetailDto(Block block) {
        BlockDetailDto dto = new BlockDetailDto();
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
            List<TransactionSummaryDto> txDtos = block.getTransactions().stream()
                    .map(this::convertToTransactionSummaryDto)
                    .collect(Collectors.toList());
            dto.setTransactions(txDtos);
        }
        
        return dto;
    }
    
    /**
     * 转换Block为BlockSummaryDto
     */
    private BlockSummaryDto convertToBlockSummaryDto(Block block) {
        BlockSummaryDto dto = new BlockSummaryDto();
        dto.setHeight(block.getHeight());
        dto.setHash(block.getHashHex());
        dto.setPreviousHash(block.getPreviousHashHex());
        dto.setTimestamp(block.getTimestamp());
        dto.setRound((long) block.getRound());
        dto.setProposer(block.getProposerHex());
        dto.setTransactionCount(block.getTransactions().size());
        dto.setDifficultyTarget(block.getDifficultyTarget().toString());
        
        return dto;
    }
    
    /**
     * 转换Transaction为TransactionDetailDto
     */
    private TransactionDetailDto convertToTransactionDetailDto(Transaction transaction) {
        TransactionDetailDto dto = new TransactionDetailDto();
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
     * 转换Transaction为TransactionSummaryDto
     */
    private TransactionSummaryDto convertToTransactionSummaryDto(Transaction transaction) {
        TransactionSummaryDto dto = new TransactionSummaryDto();
        dto.setHash(transaction.getHashHex());
        dto.setFrom(transaction.getSenderPublicKeyHex());
        dto.setTo(transaction.getReceiverPublicKeyHex());
        dto.setAmount(transaction.getAmount());
        dto.setFee(transaction.getFee());
        dto.setTimestamp(transaction.getTimestamp());
        
        return dto;
    }
    
    /**
     * 转换VRFAnnouncement为VrfAnnouncementDto
     */
    private VrfAnnouncementDto convertToVrfAnnouncementDto(VRFAnnouncement announcement) {
        VrfAnnouncementDto dto = new VrfAnnouncementDto();
        dto.setNodeId(announcement.getPublicKeyHex());
        dto.setPublicKey(announcement.getPublicKeyHex());
        dto.setVrfOutput(announcement.getVrfOutput() != null ? announcement.getVrfOutput().toString() : null);
        dto.setVrfProof(announcement.getVrfProof() != null ? announcement.getVrfProof().toString() : null);
        dto.setRound(announcement.getRound());
        dto.setTimestamp(announcement.getTimestamp());
        dto.setScore(announcement.getScore());
        
        return dto;
    }
    
    /**
     * 转换AccountState为AccountDetailDto
     */
    private AccountDetailDto convertToAccountDetailDto(AccountState accountState) {
        AccountDetailDto dto = new AccountDetailDto();
        dto.setAddress(accountState.getPublicKeyHex());
        dto.setPublicKey(accountState.getPublicKeyHex());
        dto.setBalance(accountState.getBalance());
        dto.setStakeAmount(accountState.getStakeAmount());
        dto.setNonce(accountState.getNonce());
        dto.setLastUpdated(accountState.getLastUpdateTimestamp());
        dto.setIsSuperNode(accountState.getStakeAmount() >= 100000);
        
        return dto;
    }
    
    /**
     * 判断是否为区块哈希
     */
    private boolean isBlockHash(String query) {
        return query != null && query.length() == 64 && query.matches("[0-9a-fA-F]+");
    }
    
    /**
     * 判断是否为交易哈希
     */
    private boolean isTransactionHash(String query) {
        return query != null && query.length() == 64 && query.matches("[0-9a-fA-F]+");
    }
    
    /**
     * 判断是否为地址
     */
    private boolean isAddress(String query) {
        return query != null && query.length() >= 32 && query.matches("[0-9a-fA-F]+");
    }
    
    /**
     * 判断是否为区块高度
     */
    private boolean isBlockHeight(String query) {
        try {
            long height = Long.parseLong(query);
            return height > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
