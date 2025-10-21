package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.wallet.WalletService;
import io.equiflux.node.wallet.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.List;
import java.util.Optional;

/**
 * 钱包服务实现
 * 
 * <p>实现完整的钱包功能，包括密钥管理、账户管理、交易签名和广播等。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class WalletServiceImpl implements WalletService {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);
    
    private final KeyManagementService keyManagementService;
    private final WalletAccountService walletAccountService;
    private final TransactionSigningService transactionSigningService;
    private final TransactionBroadcastService transactionBroadcastService;
    private final WalletEncryptionService encryptionService;
    private final WalletStorageService walletStorageService;
    
    public WalletServiceImpl(KeyManagementService keyManagementService,
                            WalletAccountService walletAccountService,
                            TransactionSigningService transactionSigningService,
                            TransactionBroadcastService transactionBroadcastService,
                            WalletEncryptionService encryptionService,
                            WalletStorageService walletStorageService) {
        this.keyManagementService = keyManagementService;
        this.walletAccountService = walletAccountService;
        this.transactionSigningService = transactionSigningService;
        this.transactionBroadcastService = transactionBroadcastService;
        this.encryptionService = encryptionService;
        this.walletStorageService = walletStorageService;
    }
    
    // ==================== 密钥管理 ====================
    
    @Override
    public WalletKeyPair generateKeyPair() throws WalletException {
        return keyManagementService.generateKeyPair();
    }
    
    @Override
    public WalletKeyPair importKeyPair(String privateKeyHex) throws WalletException {
        return keyManagementService.importKeyPair(privateKeyHex);
    }
    
    @Override
    public String exportPrivateKey(String publicKeyHex, String password) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Exporting private key: publicKey={}", publicKeyHex);
            
            // 获取加密的私钥
            Optional<String> encryptedPrivateKeyOpt = walletStorageService.getEncryptedPrivateKey(publicKeyHex);
            if (encryptedPrivateKeyOpt.isEmpty()) {
                throw new WalletException("Wallet not found: " + publicKeyHex);
            }
            
            // 验证密码并解密私钥
            String encryptedPrivateKey = encryptedPrivateKeyOpt.get();
            if (!encryptionService.verifyPassword(encryptedPrivateKey, password)) {
                throw new WalletException("Invalid password");
            }
            
            String privateKeyHex = encryptionService.decrypt(encryptedPrivateKey, password);
            
            logger.info("Private key exported successfully: publicKey={}", publicKeyHex);
            return privateKeyHex;
        } catch (Exception e) {
            logger.error("Failed to export private key", e);
            throw new WalletException("Failed to export private key", e);
        }
    }
    
    @Override
    public boolean validateKeyPair(String publicKeyHex, String privateKeyHex) throws WalletException {
        return keyManagementService.validateKeyPair(publicKeyHex, privateKeyHex);
    }
    
    // ==================== 钱包账户管理 ====================
    
    @Override
    public WalletInfo createWallet(String password) throws WalletException {
        return walletAccountService.createWallet(password);
    }
    
    @Override
    public WalletInfo createWalletFromPrivateKey(String privateKeyHex, String password) throws WalletException {
        return walletAccountService.createWalletFromPrivateKey(privateKeyHex, password);
    }
    
    @Override
    public boolean unlockWallet(String publicKeyHex, String password) throws WalletException {
        return walletAccountService.unlockWallet(publicKeyHex, password);
    }
    
    @Override
    public void lockWallet(String publicKeyHex) throws WalletException {
        walletAccountService.lockWallet(publicKeyHex);
    }
    
    @Override
    public Optional<WalletInfo> getWalletInfo(String publicKeyHex) throws WalletException {
        return walletAccountService.getWalletInfo(publicKeyHex);
    }
    
    @Override
    public List<WalletInfo> getAllWallets() throws WalletException {
        return walletAccountService.getAllWallets();
    }
    
    @Override
    public boolean deleteWallet(String publicKeyHex, String password) throws WalletException {
        return walletAccountService.deleteWallet(publicKeyHex, password);
    }
    
    // ==================== 账户状态管理 ====================
    
    @Override
    public long getBalance(String publicKeyHex) throws WalletException {
        return walletAccountService.getBalance(publicKeyHex);
    }
    
    @Override
    public Optional<AccountState> getAccountState(String publicKeyHex) throws WalletException {
        return walletAccountService.getAccountState(publicKeyHex);
    }
    
    @Override
    public long getNonce(String publicKeyHex) throws WalletException {
        return walletAccountService.getNonce(publicKeyHex);
    }
    
    @Override
    public long getStake(String publicKeyHex) throws WalletException {
        return walletAccountService.getStake(publicKeyHex);
    }
    
    // ==================== 交易管理 ====================
    
    @Override
    public Transaction buildTransferTransaction(String fromPublicKeyHex, String toPublicKeyHex, 
                                               long amount, long fee, String password) throws WalletException {
        return transactionSigningService.buildTransferTransaction(fromPublicKeyHex, toPublicKeyHex, amount, fee, password);
    }
    
    @Override
    public Transaction buildStakeTransaction(String publicKeyHex, long stakeAmount, 
                                            long fee, String password) throws WalletException {
        return transactionSigningService.buildStakeTransaction(publicKeyHex, stakeAmount, fee, password);
    }
    
    @Override
    public Transaction buildUnstakeTransaction(String publicKeyHex, long unstakeAmount, 
                                               long fee, String password) throws WalletException {
        return transactionSigningService.buildUnstakeTransaction(publicKeyHex, unstakeAmount, fee, password);
    }
    
    @Override
    public Transaction signTransaction(Transaction transaction, String publicKeyHex, String password) throws WalletException {
        return transactionSigningService.signTransaction(transaction, publicKeyHex, password);
    }
    
    @Override
    public boolean verifyTransactionSignature(Transaction transaction) throws WalletException {
        return transactionSigningService.verifyTransactionSignature(transaction);
    }
    
    // ==================== 交易广播 ====================
    
    @Override
    public String broadcastTransaction(Transaction transaction) throws WalletException {
        return transactionBroadcastService.broadcastTransaction(transaction);
    }
    
    @Override
    public Optional<TransactionStatus> getTransactionStatus(String transactionHash) throws WalletException {
        return transactionBroadcastService.getTransactionStatus(transactionHash);
    }
    
    @Override
    public List<TransactionInfo> getTransactionHistory(String publicKeyHex, int limit, int offset) throws WalletException {
        return transactionBroadcastService.getTransactionHistory(publicKeyHex, limit, offset);
    }
    
    // ==================== 钱包安全 ====================
    
    @Override
    public boolean changePassword(String publicKeyHex, String oldPassword, String newPassword) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new WalletException("Old password cannot be null or empty");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new WalletException("New password cannot be null or empty");
        }
        
        try {
            logger.info("Changing password: publicKey={}", publicKeyHex);
            
            // 获取加密的私钥
            Optional<String> encryptedPrivateKeyOpt = walletStorageService.getEncryptedPrivateKey(publicKeyHex);
            if (encryptedPrivateKeyOpt.isEmpty()) {
                throw new WalletException("Wallet not found: " + publicKeyHex);
            }
            
            // 验证旧密码
            String encryptedPrivateKey = encryptedPrivateKeyOpt.get();
            if (!encryptionService.verifyPassword(encryptedPrivateKey, oldPassword)) {
                throw new WalletException("Invalid old password");
            }
            
            // 解密私钥
            String privateKeyHex = encryptionService.decrypt(encryptedPrivateKey, oldPassword);
            
            // 使用新密码加密私钥
            String newEncryptedPrivateKey = encryptionService.encrypt(privateKeyHex, newPassword);
            
            // 更新存储的加密私钥
            walletStorageService.storeEncryptedPrivateKey(publicKeyHex, newEncryptedPrivateKey);
            
            logger.info("Password changed successfully: publicKey={}", publicKeyHex);
            return true;
        } catch (Exception e) {
            logger.error("Failed to change password", e);
            throw new WalletException("Failed to change password", e);
        }
    }
    
    @Override
    public WalletBackup backupWallet(String publicKeyHex, String password) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Backing up wallet: publicKey={}", publicKeyHex);
            
            // 获取钱包信息
            Optional<WalletInfo> walletInfoOpt = walletAccountService.getWalletInfo(publicKeyHex);
            if (walletInfoOpt.isEmpty()) {
                throw new WalletException("Wallet not found: " + publicKeyHex);
            }
            
            WalletInfo walletInfo = walletInfoOpt.get();
            
            // 获取加密的私钥
            Optional<String> encryptedPrivateKeyOpt = walletStorageService.getEncryptedPrivateKey(publicKeyHex);
            if (encryptedPrivateKeyOpt.isEmpty()) {
                throw new WalletException("Encrypted private key not found: " + publicKeyHex);
            }
            
            // 验证密码
            String encryptedPrivateKey = encryptedPrivateKeyOpt.get();
            if (!encryptionService.verifyPassword(encryptedPrivateKey, password)) {
                throw new WalletException("Invalid password");
            }
            
            // 创建备份
            WalletBackup backup = new WalletBackup(
                publicKeyHex,
                encryptedPrivateKey,
                walletInfo.getName(),
                java.time.LocalDateTime.now(),
                "1.0.0",
                calculateChecksum(publicKeyHex + encryptedPrivateKey)
            );
            
            logger.info("Wallet backed up successfully: publicKey={}", publicKeyHex);
            return backup;
        } catch (Exception e) {
            logger.error("Failed to backup wallet", e);
            throw new WalletException("Failed to backup wallet", e);
        }
    }
    
    @Override
    public WalletInfo restoreWallet(WalletBackup backup, String password) throws WalletException {
        if (backup == null) {
            throw new WalletException("Backup cannot be null");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Restoring wallet: publicKey={}", backup.getPublicKeyHex());
            
            // 验证校验和
            String expectedChecksum = calculateChecksum(backup.getPublicKeyHex() + backup.getEncryptedPrivateKey());
            if (!expectedChecksum.equals(backup.getChecksum())) {
                throw new WalletException("Invalid backup checksum");
            }
            
            // 验证密码
            if (!encryptionService.verifyPassword(backup.getEncryptedPrivateKey(), password)) {
                throw new WalletException("Invalid password");
            }
            
            // 检查钱包是否已存在
            if (walletStorageService.walletExists(backup.getPublicKeyHex())) {
                throw new WalletException("Wallet already exists: " + backup.getPublicKeyHex());
            }
            
            // 恢复钱包
            WalletInfo walletInfo = walletAccountService.createWalletFromPrivateKey(
                encryptionService.decrypt(backup.getEncryptedPrivateKey(), password), 
                password
            );
            
            logger.info("Wallet restored successfully: publicKey={}", backup.getPublicKeyHex());
            return walletInfo;
        } catch (Exception e) {
            logger.error("Failed to restore wallet", e);
            throw new WalletException("Failed to restore wallet", e);
        }
    }
    
    /**
     * 计算校验和
     * 
     * @param data 数据
     * @return 校验和
     */
    private String calculateChecksum(String data) {
        return io.equiflux.node.crypto.HashUtils.toHexString(
            io.equiflux.node.crypto.HashUtils.sha256(data.getBytes())
        );
    }
}
