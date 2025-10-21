package io.equiflux.node.wallet.service;

import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.exception.WalletException;
import io.equiflux.node.storage.StateStorageService;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.wallet.model.WalletInfo;
import io.equiflux.node.wallet.model.WalletKeyPair;
import io.equiflux.node.wallet.model.WalletStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 钱包账户管理服务
 * 
 * <p>负责钱包账户的创建、管理和状态查询。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class WalletAccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletAccountService.class);
    
    private final KeyManagementService keyManagementService;
    private final WalletStorageService walletStorageService;
    private final WalletEncryptionService encryptionService;
    private final StateStorageService stateStorageService;
    
    public WalletAccountService(KeyManagementService keyManagementService,
                               WalletStorageService walletStorageService,
                               WalletEncryptionService encryptionService,
                               StateStorageService stateStorageService) {
        this.keyManagementService = keyManagementService;
        this.walletStorageService = walletStorageService;
        this.encryptionService = encryptionService;
        this.stateStorageService = stateStorageService;
    }
    
    /**
     * 创建新钱包
     * 
     * @param password 钱包密码
     * @return 新创建的钱包信息
     * @throws WalletException 钱包异常
     */
    public WalletInfo createWallet(String password) throws WalletException {
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Creating new wallet");
            
            // 生成新的密钥对
            WalletKeyPair keyPair = keyManagementService.generateKeyPair();
            String publicKeyHex = keyPair.getPublicKeyHex();
            
            // 检查钱包是否已存在
            if (walletStorageService.walletExists(publicKeyHex)) {
                throw new WalletException("Wallet already exists: " + publicKeyHex);
            }
            
            // 生成钱包地址
            String address = generateAddress(publicKeyHex);
            
            // 生成钱包名称
            String walletName = generateWalletName(publicKeyHex);
            
            // 加密私钥
            String encryptedPrivateKey = encryptionService.encrypt(keyPair.getPrivateKeyHex(), password);
            
            // 创建钱包信息
            WalletInfo walletInfo = new WalletInfo(
                publicKeyHex,
                address,
                walletName,
                WalletStatus.CREATED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true
            );
            
            // 存储钱包信息
            walletStorageService.storeWalletInfo(walletInfo);
            
            // 存储加密的私钥
            walletStorageService.storeEncryptedPrivateKey(publicKeyHex, encryptedPrivateKey);
            
            logger.info("Created new wallet: publicKey={}, address={}", publicKeyHex, address);
            return walletInfo;
        } catch (Exception e) {
            logger.error("Failed to create wallet", e);
            throw new WalletException("Failed to create wallet", e);
        }
    }
    
    /**
     * 从私钥创建钱包
     * 
     * @param privateKeyHex 私钥十六进制字符串
     * @param password 钱包密码
     * @return 创建的钱包信息
     * @throws WalletException 钱包异常
     */
    public WalletInfo createWalletFromPrivateKey(String privateKeyHex, String password) throws WalletException {
        if (privateKeyHex == null || privateKeyHex.trim().isEmpty()) {
            throw new WalletException("Private key hex cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Creating wallet from private key");
            
            // 验证私钥格式
            if (!keyManagementService.isValidPrivateKey(privateKeyHex)) {
                throw new WalletException("Invalid private key format");
            }
            
            // 导入密钥对
            WalletKeyPair keyPair = keyManagementService.importKeyPair(privateKeyHex);
            String publicKeyHex = keyPair.getPublicKeyHex();
            
            // 检查钱包是否已存在
            if (walletStorageService.walletExists(publicKeyHex)) {
                throw new WalletException("Wallet already exists: " + publicKeyHex);
            }
            
            // 生成钱包地址
            String address = generateAddress(publicKeyHex);
            
            // 生成钱包名称
            String walletName = generateWalletName(publicKeyHex);
            
            // 加密私钥
            String encryptedPrivateKey = encryptionService.encrypt(privateKeyHex, password);
            
            // 创建钱包信息
            WalletInfo walletInfo = new WalletInfo(
                publicKeyHex,
                address,
                walletName,
                WalletStatus.CREATED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                true
            );
            
            // 存储钱包信息
            walletStorageService.storeWalletInfo(walletInfo);
            
            // 存储加密的私钥
            walletStorageService.storeEncryptedPrivateKey(publicKeyHex, encryptedPrivateKey);
            
            logger.info("Created wallet from private key: publicKey={}, address={}", publicKeyHex, address);
            return walletInfo;
        } catch (Exception e) {
            logger.error("Failed to create wallet from private key", e);
            throw new WalletException("Failed to create wallet from private key", e);
        }
    }
    
    /**
     * 解锁钱包
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param password 钱包密码
     * @return true如果解锁成功，false否则
     * @throws WalletException 钱包异常
     */
    public boolean unlockWallet(String publicKeyHex, String password) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.debug("Unlocking wallet: publicKey={}", publicKeyHex);
            
            // 获取钱包信息
            Optional<WalletInfo> walletInfoOpt = walletStorageService.getWalletInfo(publicKeyHex);
            if (walletInfoOpt.isEmpty()) {
                throw new WalletException("Wallet not found: " + publicKeyHex);
            }
            
            WalletInfo walletInfo = walletInfoOpt.get();
            
            // 检查钱包状态
            if (walletInfo.getStatus() == WalletStatus.DISABLED) {
                throw new WalletException("Wallet is disabled: " + publicKeyHex);
            }
            
            // 获取加密的私钥
            Optional<String> encryptedPrivateKeyOpt = walletStorageService.getEncryptedPrivateKey(publicKeyHex);
            if (encryptedPrivateKeyOpt.isEmpty()) {
                throw new WalletException("Encrypted private key not found: " + publicKeyHex);
            }
            
            // 验证密码
            String encryptedPrivateKey = encryptedPrivateKeyOpt.get();
            if (!encryptionService.verifyPassword(encryptedPrivateKey, password)) {
                logger.warn("Invalid password for wallet: publicKey={}", publicKeyHex);
                return false;
            }
            
            // 更新钱包状态为已解锁
            WalletInfo unlockedWalletInfo = walletInfo.updateStatus(WalletStatus.UNLOCKED)
                .updateLastUsedAt();
            walletStorageService.updateWalletInfo(unlockedWalletInfo);
            
            logger.info("Wallet unlocked successfully: publicKey={}", publicKeyHex);
            return true;
        } catch (Exception e) {
            logger.error("Failed to unlock wallet", e);
            throw new WalletException("Failed to unlock wallet", e);
        }
    }
    
    /**
     * 锁定钱包
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @throws WalletException 钱包异常
     */
    public void lockWallet(String publicKeyHex) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        
        try {
            logger.debug("Locking wallet: publicKey={}", publicKeyHex);
            
            // 获取钱包信息
            Optional<WalletInfo> walletInfoOpt = walletStorageService.getWalletInfo(publicKeyHex);
            if (walletInfoOpt.isEmpty()) {
                throw new WalletException("Wallet not found: " + publicKeyHex);
            }
            
            WalletInfo walletInfo = walletInfoOpt.get();
            
            // 更新钱包状态为已锁定
            WalletInfo lockedWalletInfo = walletInfo.updateStatus(WalletStatus.LOCKED);
            walletStorageService.updateWalletInfo(lockedWalletInfo);
            
            logger.info("Wallet locked successfully: publicKey={}", publicKeyHex);
        } catch (Exception e) {
            logger.error("Failed to lock wallet", e);
            throw new WalletException("Failed to lock wallet", e);
        }
    }
    
    /**
     * 获取钱包信息
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 钱包信息
     * @throws WalletException 钱包异常
     */
    public Optional<WalletInfo> getWalletInfo(String publicKeyHex) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        
        try {
            return walletStorageService.getWalletInfo(publicKeyHex);
        } catch (Exception e) {
            logger.error("Failed to get wallet info", e);
            throw new WalletException("Failed to get wallet info", e);
        }
    }
    
    /**
     * 获取所有钱包列表
     * 
     * @return 钱包信息列表
     * @throws WalletException 钱包异常
     */
    public List<WalletInfo> getAllWallets() throws WalletException {
        try {
            return walletStorageService.getAllWalletInfos();
        } catch (Exception e) {
            logger.error("Failed to get all wallets", e);
            throw new WalletException("Failed to get all wallets", e);
        }
    }
    
    /**
     * 删除钱包
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param password 钱包密码
     * @return true如果删除成功，false否则
     * @throws WalletException 钱包异常
     */
    public boolean deleteWallet(String publicKeyHex, String password) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Deleting wallet: publicKey={}", publicKeyHex);
            
            // 验证密码
            Optional<String> encryptedPrivateKeyOpt = walletStorageService.getEncryptedPrivateKey(publicKeyHex);
            if (encryptedPrivateKeyOpt.isEmpty()) {
                throw new WalletException("Wallet not found: " + publicKeyHex);
            }
            
            String encryptedPrivateKey = encryptedPrivateKeyOpt.get();
            if (!encryptionService.verifyPassword(encryptedPrivateKey, password)) {
                logger.warn("Invalid password for wallet deletion: publicKey={}", publicKeyHex);
                return false;
            }
            
            // 删除钱包信息
            walletStorageService.deleteWalletInfo(publicKeyHex);
            
            // 删除加密的私钥
            walletStorageService.deleteEncryptedPrivateKey(publicKeyHex);
            
            logger.info("Wallet deleted successfully: publicKey={}", publicKeyHex);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete wallet", e);
            throw new WalletException("Failed to delete wallet", e);
        }
    }
    
    /**
     * 获取账户余额
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户余额
     * @throws WalletException 钱包异常
     */
    public long getBalance(String publicKeyHex) throws WalletException {
        try {
            Optional<AccountState> accountStateOpt = getAccountState(publicKeyHex);
            return accountStateOpt.map(AccountState::getBalance).orElse(0L);
        } catch (Exception e) {
            logger.error("Failed to get balance", e);
            throw new WalletException("Failed to get balance", e);
        }
    }
    
    /**
     * 获取账户状态
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户状态
     * @throws WalletException 钱包异常
     */
    public Optional<AccountState> getAccountState(String publicKeyHex) throws WalletException {
        try {
            return Optional.ofNullable(stateStorageService.getAccountStateByPublicKeyHex(publicKeyHex));
        } catch (Exception e) {
            logger.error("Failed to get account state", e);
            throw new WalletException("Failed to get account state", e);
        }
    }
    
    /**
     * 获取账户nonce
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户nonce
     * @throws WalletException 钱包异常
     */
    public long getNonce(String publicKeyHex) throws WalletException {
        try {
            Optional<AccountState> accountStateOpt = getAccountState(publicKeyHex);
            return accountStateOpt.map(AccountState::getNonce).orElse(0L);
        } catch (Exception e) {
            logger.error("Failed to get nonce", e);
            throw new WalletException("Failed to get nonce", e);
        }
    }
    
    /**
     * 获取账户质押金额
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 质押金额
     * @throws WalletException 钱包异常
     */
    public long getStake(String publicKeyHex) throws WalletException {
        try {
            Optional<AccountState> accountStateOpt = getAccountState(publicKeyHex);
            return accountStateOpt.map(AccountState::getStakeAmount).orElse(0L);
        } catch (Exception e) {
            logger.error("Failed to get stake", e);
            throw new WalletException("Failed to get stake", e);
        }
    }
    
    /**
     * 生成钱包地址
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 钱包地址
     */
    private String generateAddress(String publicKeyHex) {
        // 使用公钥的哈希作为地址
        byte[] publicKeyBytes = HashUtils.fromHexString(publicKeyHex);
        byte[] addressBytes = HashUtils.sha256(publicKeyBytes);
        return "EQ" + HashUtils.toHexString(addressBytes).substring(0, 40); // 取前40个字符
    }
    
    /**
     * 生成钱包名称
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 钱包名称
     */
    private String generateWalletName(String publicKeyHex) {
        // 使用公钥的前8个字符作为钱包名称
        return "Wallet_" + publicKeyHex.substring(0, 8);
    }
}
