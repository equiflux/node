package io.equiflux.node.wallet.rpc.controller;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.wallet.model.*;
import io.equiflux.node.wallet.rpc.dto.*;
import io.equiflux.node.wallet.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 钱包RPC控制器
 * 
 * <p>提供钱包相关的REST API接口。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletRpcController {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletRpcController.class);
    
    private final WalletService walletService;
    
    public WalletRpcController(WalletService walletService) {
        this.walletService = walletService;
    }
    
    // ==================== 钱包管理 ====================
    
    /**
     * 创建新钱包
     */
    @PostMapping("/create")
    public ResponseEntity<WalletResponseDto> createWallet(@RequestBody CreateWalletRequestDto request) {
        try {
            logger.info("Creating new wallet via RPC");
            
            WalletInfo walletInfo = walletService.createWallet(request.getPassword());
            WalletResponseDto response = convertToWalletResponseDto(walletInfo);
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to create wallet", e);
            return ResponseEntity.badRequest().body(WalletResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating wallet", e);
            return ResponseEntity.internalServerError().body(WalletResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 从私钥创建钱包
     */
    @PostMapping("/import")
    public ResponseEntity<WalletResponseDto> importWallet(@RequestBody ImportWalletRequestDto request) {
        try {
            logger.info("Importing wallet via RPC");
            
            WalletInfo walletInfo = walletService.createWalletFromPrivateKey(
                request.getPrivateKeyHex(), request.getPassword());
            WalletResponseDto response = convertToWalletResponseDto(walletInfo);
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to import wallet", e);
            return ResponseEntity.badRequest().body(WalletResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error importing wallet", e);
            return ResponseEntity.internalServerError().body(WalletResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 解锁钱包
     */
    @PostMapping("/unlock")
    public ResponseEntity<WalletResponseDto> unlockWallet(@RequestBody UnlockWalletRequestDto request) {
        try {
            logger.info("Unlocking wallet via RPC: publicKey={}", request.getPublicKeyHex());
            
            boolean success = walletService.unlockWallet(request.getPublicKeyHex(), request.getPassword());
            WalletResponseDto response = new WalletResponseDto();
            response.setSuccess(success);
            response.setMessage(success ? "Wallet unlocked successfully" : "Invalid password");
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to unlock wallet", e);
            return ResponseEntity.badRequest().body(WalletResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error unlocking wallet", e);
            return ResponseEntity.internalServerError().body(WalletResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 锁定钱包
     */
    @PostMapping("/lock")
    public ResponseEntity<WalletResponseDto> lockWallet(@RequestBody LockWalletRequestDto request) {
        try {
            logger.info("Locking wallet via RPC: publicKey={}", request.getPublicKeyHex());
            
            walletService.lockWallet(request.getPublicKeyHex());
            WalletResponseDto response = new WalletResponseDto();
            response.setSuccess(true);
            response.setMessage("Wallet locked successfully");
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to lock wallet", e);
            return ResponseEntity.badRequest().body(WalletResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error locking wallet", e);
            return ResponseEntity.internalServerError().body(WalletResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 获取钱包信息
     */
    @GetMapping("/info/{publicKeyHex}")
    public ResponseEntity<WalletResponseDto> getWalletInfo(@PathVariable String publicKeyHex) {
        try {
            logger.debug("Getting wallet info via RPC: publicKey={}", publicKeyHex);
            
            Optional<WalletInfo> walletInfoOpt = walletService.getWalletInfo(publicKeyHex);
            if (walletInfoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            WalletResponseDto response = convertToWalletResponseDto(walletInfoOpt.get());
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to get wallet info", e);
            return ResponseEntity.badRequest().body(WalletResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting wallet info", e);
            return ResponseEntity.internalServerError().body(WalletResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 获取所有钱包
     */
    @GetMapping("/list")
    public ResponseEntity<WalletListResponseDto> getAllWallets() {
        try {
            logger.debug("Getting all wallets via RPC");
            
            List<WalletInfo> wallets = walletService.getAllWallets();
            List<WalletResponseDto> walletDtos = wallets.stream()
                .map(this::convertToWalletResponseDto)
                .collect(Collectors.toList());
            
            WalletListResponseDto response = new WalletListResponseDto();
            response.setSuccess(true);
            response.setWallets(walletDtos);
            response.setCount(wallets.size());
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to get all wallets", e);
            return ResponseEntity.badRequest().body(WalletListResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting all wallets", e);
            return ResponseEntity.internalServerError().body(WalletListResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 删除钱包
     */
    @DeleteMapping("/delete")
    public ResponseEntity<WalletResponseDto> deleteWallet(@RequestBody DeleteWalletRequestDto request) {
        try {
            logger.info("Deleting wallet via RPC: publicKey={}", request.getPublicKeyHex());
            
            boolean success = walletService.deleteWallet(request.getPublicKeyHex(), request.getPassword());
            WalletResponseDto response = new WalletResponseDto();
            response.setSuccess(success);
            response.setMessage(success ? "Wallet deleted successfully" : "Invalid password");
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to delete wallet", e);
            return ResponseEntity.badRequest().body(WalletResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error deleting wallet", e);
            return ResponseEntity.internalServerError().body(WalletResponseDto.error("Internal server error"));
        }
    }
    
    // ==================== 账户状态 ====================
    
    /**
     * 获取账户余额
     */
    @GetMapping("/balance/{publicKeyHex}")
    public ResponseEntity<BalanceResponseDto> getBalance(@PathVariable String publicKeyHex) {
        try {
            logger.debug("Getting balance via RPC: publicKey={}", publicKeyHex);
            
            long balance = walletService.getBalance(publicKeyHex);
            BalanceResponseDto response = new BalanceResponseDto();
            response.setSuccess(true);
            response.setBalance(balance);
            response.setPublicKeyHex(publicKeyHex);
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to get balance", e);
            return ResponseEntity.badRequest().body(BalanceResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting balance", e);
            return ResponseEntity.internalServerError().body(BalanceResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 获取账户状态
     */
    @GetMapping("/account/{publicKeyHex}")
    public ResponseEntity<AccountStateResponseDto> getAccountState(@PathVariable String publicKeyHex) {
        try {
            logger.debug("Getting account state via RPC: publicKey={}", publicKeyHex);
            
            Optional<AccountState> accountStateOpt = walletService.getAccountState(publicKeyHex);
            AccountStateResponseDto response = new AccountStateResponseDto();
            response.setSuccess(true);
            response.setPublicKeyHex(publicKeyHex);
            
            if (accountStateOpt.isPresent()) {
                AccountState accountState = accountStateOpt.get();
                response.setBalance(accountState.getBalance());
                response.setNonce(accountState.getNonce());
                response.setStakeAmount(accountState.getStakeAmount());
                response.setLastUpdated(accountState.getLastUpdated());
            } else {
                response.setBalance(0L);
                response.setNonce(0L);
                response.setStakeAmount(0L);
                response.setLastUpdated(null);
            }
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to get account state", e);
            return ResponseEntity.badRequest().body(AccountStateResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting account state", e);
            return ResponseEntity.internalServerError().body(AccountStateResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 获取账户nonce
     */
    @GetMapping("/nonce/{publicKeyHex}")
    public ResponseEntity<NonceResponseDto> getNonce(@PathVariable String publicKeyHex) {
        try {
            logger.debug("Getting nonce via RPC: publicKey={}", publicKeyHex);
            
            long nonce = walletService.getNonce(publicKeyHex);
            NonceResponseDto response = new NonceResponseDto();
            response.setSuccess(true);
            response.setNonce(nonce);
            response.setPublicKeyHex(publicKeyHex);
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to get nonce", e);
            return ResponseEntity.badRequest().body(NonceResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting nonce", e);
            return ResponseEntity.internalServerError().body(NonceResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 获取账户质押金额
     */
    @GetMapping("/stake/{publicKeyHex}")
    public ResponseEntity<StakeResponseDto> getStake(@PathVariable String publicKeyHex) {
        try {
            logger.debug("Getting stake via RPC: publicKey={}", publicKeyHex);
            
            long stake = walletService.getStake(publicKeyHex);
            StakeResponseDto response = new StakeResponseDto();
            response.setSuccess(true);
            response.setStakeAmount(stake);
            response.setPublicKeyHex(publicKeyHex);
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to get stake", e);
            return ResponseEntity.badRequest().body(StakeResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting stake", e);
            return ResponseEntity.internalServerError().body(StakeResponseDto.error("Internal server error"));
        }
    }
    
    // ==================== 交易管理 ====================
    
    /**
     * 构建转账交易
     */
    @PostMapping("/transaction/transfer")
    public ResponseEntity<TransactionResponseDto> buildTransferTransaction(@RequestBody TransferTransactionRequestDto request) {
        try {
            logger.info("Building transfer transaction via RPC: from={}, to={}, amount={}", 
                       request.getFromPublicKeyHex(), request.getToPublicKeyHex(), request.getAmount());
            
            Transaction transaction = walletService.buildTransferTransaction(
                request.getFromPublicKeyHex(),
                request.getToPublicKeyHex(),
                request.getAmount(),
                request.getFee(),
                request.getPassword()
            );
            
            TransactionResponseDto response = convertToTransactionResponseDto(transaction);
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to build transfer transaction", e);
            return ResponseEntity.badRequest().body(TransactionResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error building transfer transaction", e);
            return ResponseEntity.internalServerError().body(TransactionResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 构建质押交易
     */
    @PostMapping("/transaction/stake")
    public ResponseEntity<TransactionResponseDto> buildStakeTransaction(@RequestBody StakeTransactionRequestDto request) {
        try {
            logger.info("Building stake transaction via RPC: publicKey={}, amount={}", 
                       request.getPublicKeyHex(), request.getStakeAmount());
            
            Transaction transaction = walletService.buildStakeTransaction(
                request.getPublicKeyHex(),
                request.getStakeAmount(),
                request.getFee(),
                request.getPassword()
            );
            
            TransactionResponseDto response = convertToTransactionResponseDto(transaction);
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to build stake transaction", e);
            return ResponseEntity.badRequest().body(TransactionResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error building stake transaction", e);
            return ResponseEntity.internalServerError().body(TransactionResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 构建解质押交易
     */
    @PostMapping("/transaction/unstake")
    public ResponseEntity<TransactionResponseDto> buildUnstakeTransaction(@RequestBody UnstakeTransactionRequestDto request) {
        try {
            logger.info("Building unstake transaction via RPC: publicKey={}, amount={}", 
                       request.getPublicKeyHex(), request.getUnstakeAmount());
            
            Transaction transaction = walletService.buildUnstakeTransaction(
                request.getPublicKeyHex(),
                request.getUnstakeAmount(),
                request.getFee(),
                request.getPassword()
            );
            
            TransactionResponseDto response = convertToTransactionResponseDto(transaction);
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to build unstake transaction", e);
            return ResponseEntity.badRequest().body(TransactionResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error building unstake transaction", e);
            return ResponseEntity.internalServerError().body(TransactionResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 广播交易
     */
    @PostMapping("/transaction/broadcast")
    public ResponseEntity<TransactionBroadcastResponseDto> broadcastTransaction(@RequestBody BroadcastTransactionRequestDto request) {
        try {
            logger.info("Broadcasting transaction via RPC: hash={}", request.getTransactionHash());
            
            // 这里需要根据交易哈希重新构建交易对象
            // 简化实现，实际应该从存储中获取交易
            String transactionHash = walletService.broadcastTransaction(request.getTransaction());
            
            TransactionBroadcastResponseDto response = new TransactionBroadcastResponseDto();
            response.setSuccess(true);
            response.setTransactionHash(transactionHash);
            response.setMessage("Transaction broadcasted successfully");
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to broadcast transaction", e);
            return ResponseEntity.badRequest().body(TransactionBroadcastResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error broadcasting transaction", e);
            return ResponseEntity.internalServerError().body(TransactionBroadcastResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 获取交易状态
     */
    @GetMapping("/transaction/status/{transactionHash}")
    public ResponseEntity<TransactionStatusResponseDto> getTransactionStatus(@PathVariable String transactionHash) {
        try {
            logger.debug("Getting transaction status via RPC: hash={}", transactionHash);
            
            Optional<TransactionStatus> statusOpt = walletService.getTransactionStatus(transactionHash);
            TransactionStatusResponseDto response = new TransactionStatusResponseDto();
            response.setSuccess(true);
            response.setTransactionHash(transactionHash);
            
            if (statusOpt.isPresent()) {
                TransactionStatus status = statusOpt.get();
                response.setState(status.getState().getCode());
                response.setTimestamp(status.getTimestamp());
                response.setBlockHeight(status.getBlockHeight());
                response.setBlockIndex(status.getBlockIndex());
                response.setErrorMessage(status.getErrorMessage());
            } else {
                response.setState("not_found");
                response.setMessage("Transaction not found");
            }
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to get transaction status", e);
            return ResponseEntity.badRequest().body(TransactionStatusResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting transaction status", e);
            return ResponseEntity.internalServerError().body(TransactionStatusResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 获取交易历史
     */
    @GetMapping("/transaction/history/{publicKeyHex}")
    public ResponseEntity<TransactionHistoryResponseDto> getTransactionHistory(
            @PathVariable String publicKeyHex,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            logger.debug("Getting transaction history via RPC: publicKey={}, limit={}, offset={}", 
                        publicKeyHex, limit, offset);
            
            List<TransactionInfo> transactions = walletService.getTransactionHistory(publicKeyHex, limit, offset);
            List<TransactionInfoDto> transactionDtos = transactions.stream()
                .map(this::convertToTransactionInfoDto)
                .collect(Collectors.toList());
            
            TransactionHistoryResponseDto response = new TransactionHistoryResponseDto();
            response.setSuccess(true);
            response.setPublicKeyHex(publicKeyHex);
            response.setTransactions(transactionDtos);
            response.setCount(transactions.size());
            response.setLimit(limit);
            response.setOffset(offset);
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to get transaction history", e);
            return ResponseEntity.badRequest().body(TransactionHistoryResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting transaction history", e);
            return ResponseEntity.internalServerError().body(TransactionHistoryResponseDto.error("Internal server error"));
        }
    }
    
    // ==================== 钱包安全 ====================
    
    /**
     * 修改钱包密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<WalletResponseDto> changePassword(@RequestBody ChangePasswordRequestDto request) {
        try {
            logger.info("Changing password via RPC: publicKey={}", request.getPublicKeyHex());
            
            boolean success = walletService.changePassword(
                request.getPublicKeyHex(),
                request.getOldPassword(),
                request.getNewPassword()
            );
            
            WalletResponseDto response = new WalletResponseDto();
            response.setSuccess(success);
            response.setMessage(success ? "Password changed successfully" : "Invalid old password");
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to change password", e);
            return ResponseEntity.badRequest().body(WalletResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error changing password", e);
            return ResponseEntity.internalServerError().body(WalletResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 备份钱包
     */
    @PostMapping("/backup")
    public ResponseEntity<WalletBackupResponseDto> backupWallet(@RequestBody BackupWalletRequestDto request) {
        try {
            logger.info("Backing up wallet via RPC: publicKey={}", request.getPublicKeyHex());
            
            WalletBackup backup = walletService.backupWallet(request.getPublicKeyHex(), request.getPassword());
            WalletBackupResponseDto response = convertToWalletBackupResponseDto(backup);
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to backup wallet", e);
            return ResponseEntity.badRequest().body(WalletBackupResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error backing up wallet", e);
            return ResponseEntity.internalServerError().body(WalletBackupResponseDto.error("Internal server error"));
        }
    }
    
    /**
     * 恢复钱包
     */
    @PostMapping("/restore")
    public ResponseEntity<WalletResponseDto> restoreWallet(@RequestBody RestoreWalletRequestDto request) {
        try {
            logger.info("Restoring wallet via RPC");
            
            WalletInfo walletInfo = walletService.restoreWallet(request.getBackup(), request.getPassword());
            WalletResponseDto response = convertToWalletResponseDto(walletInfo);
            
            return ResponseEntity.ok(response);
        } catch (WalletException e) {
            logger.error("Failed to restore wallet", e);
            return ResponseEntity.badRequest().body(WalletResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error restoring wallet", e);
            return ResponseEntity.internalServerError().body(WalletResponseDto.error("Internal server error"));
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private WalletResponseDto convertToWalletResponseDto(WalletInfo walletInfo) {
        WalletResponseDto dto = new WalletResponseDto();
        dto.setSuccess(true);
        dto.setPublicKeyHex(walletInfo.getPublicKeyHex());
        dto.setAddress(walletInfo.getAddress());
        dto.setName(walletInfo.getName());
        dto.setStatus(walletInfo.getStatus().getCode());
        dto.setCreatedAt(walletInfo.getCreatedAt());
        dto.setLastUsedAt(walletInfo.getLastUsedAt());
        dto.setEncrypted(walletInfo.isEncrypted());
        return dto;
    }
    
    private TransactionResponseDto convertToTransactionResponseDto(Transaction transaction) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setSuccess(true);
        dto.setTransactionHash(transaction.getHashHex());
        dto.setFromPublicKeyHex(transaction.getFromPublicKey());
        dto.setToPublicKeyHex(transaction.getToPublicKey());
        dto.setAmount(transaction.getAmount());
        dto.setFee(transaction.getFee());
        dto.setNonce(transaction.getNonce());
        dto.setTimestamp(java.time.LocalDateTime.ofEpochSecond(transaction.getTimestamp() / 1000, 0, java.time.ZoneOffset.UTC));
        dto.setType(transaction.getType().toString());
        dto.setSignature(io.equiflux.node.crypto.HashUtils.toHexString(transaction.getSignature()));
        return dto;
    }
    
    private TransactionInfoDto convertToTransactionInfoDto(TransactionInfo transactionInfo) {
        TransactionInfoDto dto = new TransactionInfoDto();
        dto.setTransactionHash(transactionInfo.getTransactionHash());
        dto.setState(transactionInfo.getState().getCode());
        dto.setTimestamp(transactionInfo.getTimestamp());
        dto.setBlockHeight(transactionInfo.getBlockHeight());
        dto.setBlockIndex(transactionInfo.getBlockIndex());
        dto.setErrorMessage(transactionInfo.getErrorMessage());
        
        Transaction transaction = transactionInfo.getTransaction();
        dto.setFromPublicKeyHex(transaction.getFromPublicKey());
        dto.setToPublicKeyHex(transaction.getToPublicKey());
        dto.setAmount(transaction.getAmount());
        dto.setFee(transaction.getFee());
        dto.setType(transaction.getType().toString());
        
        return dto;
    }
    
    private WalletBackupResponseDto convertToWalletBackupResponseDto(WalletBackup backup) {
        WalletBackupResponseDto dto = new WalletBackupResponseDto();
        dto.setSuccess(true);
        dto.setPublicKeyHex(backup.getPublicKeyHex());
        dto.setWalletName(backup.getWalletName());
        dto.setBackupTime(backup.getBackupTime());
        dto.setVersion(backup.getVersion());
        dto.setChecksum(backup.getChecksum());
        return dto;
    }
}
