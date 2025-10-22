package io.equiflux.node.explorer.controller;

import io.equiflux.node.explorer.service.BlockExplorerService;
import io.equiflux.node.explorer.dto.*;
import io.equiflux.node.explorer.exception.ExplorerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 区块浏览器Web控制器
 * 
 * <p>提供区块浏览器的Web界面和API接口。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>区块查询页面和API</li>
 *   <li>交易查询页面和API</li>
 *   <li>账户查询页面和API</li>
 *   <li>搜索功能</li>
 *   <li>统计信息展示</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/explorer")
@CrossOrigin(origins = "*")
public class BlockExplorerController {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockExplorerController.class);
    
    private final BlockExplorerService explorerService;
    
    public BlockExplorerController(BlockExplorerService explorerService) {
        this.explorerService = explorerService;
    }
    
    // ==================== 页面路由 ====================
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/explorer/dashboard";
    }
    
    /**
     * 仪表板页面
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "explorer/dashboard";
    }
    
    /**
     * 区块列表页面
     */
    @GetMapping("/blocks")
    public String blocks() {
        return "explorer/blocks";
    }
    
    /**
     * 区块详情页面
     */
    @GetMapping("/block/{hash}")
    public String blockDetail(@PathVariable String hash) {
        return "explorer/block-detail";
    }
    
    /**
     * 交易列表页面
     */
    @GetMapping("/transactions")
    public String transactions() {
        return "explorer/transactions";
    }
    
    /**
     * 交易详情页面
     */
    @GetMapping("/transaction/{hash}")
    public String transactionDetail(@PathVariable String hash) {
        return "explorer/transaction-detail";
    }
    
    /**
     * 账户详情页面
     */
    @GetMapping("/account/{address}")
    public String accountDetail(@PathVariable String address) {
        return "explorer/account-detail";
    }
    
    /**
     * 搜索页面
     */
    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String q) {
        return "explorer/search";
    }
    
    // ==================== API接口 ====================
    
    /**
     * 获取最新区块
     */
    @GetMapping("/api/latest-block")
    public ResponseEntity<BlockDetailDto> getLatestBlock() {
        try {
            BlockDetailDto block = explorerService.getLatestBlock();
            return ResponseEntity.ok(block);
        } catch (ExplorerException e) {
            logger.error("Failed to get latest block", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 按高度获取区块
     */
    @GetMapping("/api/block/height/{height}")
    public ResponseEntity<BlockDetailDto> getBlockByHeight(@PathVariable long height) {
        try {
            BlockDetailDto block = explorerService.getBlockByHeight(height);
            return ResponseEntity.ok(block);
        } catch (ExplorerException e) {
            logger.error("Failed to get block by height: {}", height, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 按哈希获取区块
     */
    @GetMapping("/api/block/hash/{hash}")
    public ResponseEntity<BlockDetailDto> getBlockByHash(@PathVariable String hash) {
        try {
            BlockDetailDto block = explorerService.getBlockByHash(hash);
            return ResponseEntity.ok(block);
        } catch (ExplorerException e) {
            logger.error("Failed to get block by hash: {}", hash, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取区块列表（分页）
     */
    @GetMapping("/api/blocks")
    public ResponseEntity<BlockListDto> getBlocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            BlockListDto blocks = explorerService.getBlocks(page, size);
            return ResponseEntity.ok(blocks);
        } catch (ExplorerException e) {
            logger.error("Failed to get blocks", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取区块范围
     */
    @GetMapping("/api/blocks/range")
    public ResponseEntity<List<BlockSummaryDto>> getBlocks(
            @RequestParam long startHeight,
            @RequestParam long endHeight) {
        try {
            List<BlockSummaryDto> blocks = explorerService.getBlocks(startHeight, endHeight);
            return ResponseEntity.ok(blocks);
        } catch (ExplorerException e) {
            logger.error("Failed to get blocks range", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 按哈希获取交易
     */
    @GetMapping("/api/transaction/{hash}")
    public ResponseEntity<TransactionDetailDto> getTransactionByHash(@PathVariable String hash) {
        try {
            TransactionDetailDto transaction = explorerService.getTransactionByHash(hash);
            return ResponseEntity.ok(transaction);
        } catch (ExplorerException e) {
            logger.error("Failed to get transaction by hash: {}", hash, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取地址的交易历史
     */
    @GetMapping("/api/transactions/address/{address}")
    public ResponseEntity<TransactionListDto> getTransactionsByAddress(
            @PathVariable String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            TransactionListDto transactions = explorerService.getTransactionsByAddress(address, page, size);
            return ResponseEntity.ok(transactions);
        } catch (ExplorerException e) {
            logger.error("Failed to get transactions for address: {}", address, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取最近的交易
     */
    @GetMapping("/api/transactions/recent")
    public ResponseEntity<List<TransactionSummaryDto>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int count) {
        try {
            List<TransactionSummaryDto> transactions = explorerService.getRecentTransactions(count);
            return ResponseEntity.ok(transactions);
        } catch (ExplorerException e) {
            logger.error("Failed to get recent transactions", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取账户详细信息
     */
    @GetMapping("/api/account/{address}")
    public ResponseEntity<AccountDetailDto> getAccountDetail(@PathVariable String address) {
        try {
            AccountDetailDto account = explorerService.getAccountDetail(address);
            return ResponseEntity.ok(account);
        } catch (ExplorerException e) {
            logger.error("Failed to get account detail: {}", address, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取账户余额
     */
    @GetMapping("/api/account/{address}/balance")
    public ResponseEntity<Long> getAccountBalance(@PathVariable String address) {
        try {
            long balance = explorerService.getAccountBalance(address);
            return ResponseEntity.ok(balance);
        } catch (ExplorerException e) {
            logger.error("Failed to get account balance: {}", address, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取账户质押金额
     */
    @GetMapping("/api/account/{address}/stake")
    public ResponseEntity<Long> getAccountStake(@PathVariable String address) {
        try {
            long stake = explorerService.getAccountStake(address);
            return ResponseEntity.ok(stake);
        } catch (ExplorerException e) {
            logger.error("Failed to get account stake: {}", address, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取链统计信息
     */
    @GetMapping("/api/stats/chain")
    public ResponseEntity<ChainStatsDto> getChainStats() {
        try {
            ChainStatsDto stats = explorerService.getChainStats();
            return ResponseEntity.ok(stats);
        } catch (ExplorerException e) {
            logger.error("Failed to get chain stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取网络统计信息
     */
    @GetMapping("/api/stats/network")
    public ResponseEntity<NetworkStatsDto> getNetworkStats() {
        try {
            NetworkStatsDto stats = explorerService.getNetworkStats();
            return ResponseEntity.ok(stats);
        } catch (ExplorerException e) {
            logger.error("Failed to get network stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 搜索功能
     */
    @GetMapping("/api/search")
    public ResponseEntity<SearchResultDto> searchApi(@RequestParam String q) {
        try {
            SearchResultDto result = explorerService.search(q);
            return ResponseEntity.ok(result);
        } catch (ExplorerException e) {
            logger.error("Failed to search: {}", q, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
