package io.equiflux.node.wallet.service;

import io.equiflux.node.crypto.Ed25519KeyPair;
import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.exception.WalletException;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.TransactionType;
import io.equiflux.node.wallet.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 交易签名服务
 * 
 * <p>负责交易的构建、签名和验证。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class TransactionSigningService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionSigningService.class);
    
    private final KeyManagementService keyManagementService;
    private final WalletStorageService walletStorageService;
    private final WalletEncryptionService encryptionService;
    private final WalletAccountService walletAccountService;
    
    public TransactionSigningService(KeyManagementService keyManagementService,
                                    WalletStorageService walletStorageService,
                                    WalletEncryptionService encryptionService,
                                    WalletAccountService walletAccountService) {
        this.keyManagementService = keyManagementService;
        this.walletStorageService = walletStorageService;
        this.encryptionService = encryptionService;
        this.walletAccountService = walletAccountService;
    }
    
    /**
     * 构建转账交易
     * 
     * @param fromPublicKeyHex 发送方公钥十六进制字符串
     * @param toPublicKeyHex 接收方公钥十六进制字符串
     * @param amount 转账金额
     * @param fee 手续费
     * @param password 钱包密码
     * @return 构建的交易
     * @throws WalletException 钱包异常
     */
    public Transaction buildTransferTransaction(String fromPublicKeyHex, String toPublicKeyHex, 
                                             long amount, long fee, String password) throws WalletException {
        if (fromPublicKeyHex == null || fromPublicKeyHex.trim().isEmpty()) {
            throw new WalletException("From public key hex cannot be null or empty");
        }
        if (toPublicKeyHex == null || toPublicKeyHex.trim().isEmpty()) {
            throw new WalletException("To public key hex cannot be null or empty");
        }
        if (amount <= 0) {
            throw new WalletException("Amount must be positive");
        }
        if (fee < 0) {
            throw new WalletException("Fee cannot be negative");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Building transfer transaction: from={}, to={}, amount={}, fee={}", 
                       fromPublicKeyHex, toPublicKeyHex, amount, fee);
            
            // 验证发送方钱包
            Optional<WalletInfo> fromWalletOpt = walletAccountService.getWalletInfo(fromPublicKeyHex);
            if (fromWalletOpt.isEmpty()) {
                throw new WalletException("From wallet not found: " + fromPublicKeyHex);
            }
            
            WalletInfo fromWallet = fromWalletOpt.get();
            if (fromWallet.getStatus() != WalletStatus.UNLOCKED) {
                throw new WalletException("From wallet is not unlocked: " + fromPublicKeyHex);
            }
            
            // 验证接收方公钥格式
            if (!keyManagementService.isValidPublicKey(toPublicKeyHex)) {
                throw new WalletException("Invalid to public key format: " + toPublicKeyHex);
            }
            
            // 检查余额
            long balance = walletAccountService.getBalance(fromPublicKeyHex);
            if (balance < amount + fee) {
                throw new WalletException("Insufficient balance: required=" + (amount + fee) + ", available=" + balance);
            }
            
            // 获取nonce
            long nonce = walletAccountService.getNonce(fromPublicKeyHex);
            
            // 构建交易
            Transaction transaction = Transaction.builder()
                .fromPublicKey(fromPublicKeyHex)
                .toPublicKey(toPublicKeyHex)
                .amount(amount)
                .fee(fee)
                .nonce(nonce)
                .timestamp(LocalDateTime.now())
                .type(TransactionType.TRANSFER)
                .build();
            
            // 签名交易
            Transaction signedTransaction = signTransaction(transaction, fromPublicKeyHex, password);
            
            logger.info("Built transfer transaction: hash={}", signedTransaction.getHash());
            return signedTransaction;
        } catch (Exception e) {
            logger.error("Failed to build transfer transaction", e);
            throw new WalletException("Failed to build transfer transaction", e);
        }
    }
    
    /**
     * 构建质押交易
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param stakeAmount 质押金额
     * @param fee 手续费
     * @param password 钱包密码
     * @return 构建的交易
     * @throws WalletException 钱包异常
     */
    public Transaction buildStakeTransaction(String publicKeyHex, long stakeAmount, 
                                           long fee, String password) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (stakeAmount <= 0) {
            throw new WalletException("Stake amount must be positive");
        }
        if (fee < 0) {
            throw new WalletException("Fee cannot be negative");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Building stake transaction: publicKey={}, stakeAmount={}, fee={}", 
                       publicKeyHex, stakeAmount, fee);
            
            // 验证钱包
            Optional<WalletInfo> walletOpt = walletAccountService.getWalletInfo(publicKeyHex);
            if (walletOpt.isEmpty()) {
                throw new WalletException("Wallet not found: " + publicKeyHex);
            }
            
            WalletInfo wallet = walletOpt.get();
            if (wallet.getStatus() != WalletStatus.UNLOCKED) {
                throw new WalletException("Wallet is not unlocked: " + publicKeyHex);
            }
            
            // 检查余额
            long balance = walletAccountService.getBalance(publicKeyHex);
            if (balance < stakeAmount + fee) {
                throw new WalletException("Insufficient balance: required=" + (stakeAmount + fee) + ", available=" + balance);
            }
            
            // 获取nonce
            long nonce = walletAccountService.getNonce(publicKeyHex);
            
            // 构建交易
            Transaction transaction = Transaction.builder()
                .fromPublicKey(publicKeyHex)
                .toPublicKey(publicKeyHex) // 质押给自己
                .amount(stakeAmount)
                .fee(fee)
                .nonce(nonce)
                .timestamp(LocalDateTime.now())
                .type(TransactionType.STAKE)
                .build();
            
            // 签名交易
            Transaction signedTransaction = signTransaction(transaction, publicKeyHex, password);
            
            logger.info("Built stake transaction: hash={}", signedTransaction.getHash());
            return signedTransaction;
        } catch (Exception e) {
            logger.error("Failed to build stake transaction", e);
            throw new WalletException("Failed to build stake transaction", e);
        }
    }
    
    /**
     * 构建解质押交易
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param unstakeAmount 解质押金额
     * @param fee 手续费
     * @param password 钱包密码
     * @return 构建的交易
     * @throws WalletException 钱包异常
     */
    public Transaction buildUnstakeTransaction(String publicKeyHex, long unstakeAmount, 
                                             long fee, String password) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (unstakeAmount <= 0) {
            throw new WalletException("Unstake amount must be positive");
        }
        if (fee < 0) {
            throw new WalletException("Fee cannot be negative");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.info("Building unstake transaction: publicKey={}, unstakeAmount={}, fee={}", 
                       publicKeyHex, unstakeAmount, fee);
            
            // 验证钱包
            Optional<WalletInfo> walletOpt = walletAccountService.getWalletInfo(publicKeyHex);
            if (walletOpt.isEmpty()) {
                throw new WalletException("Wallet not found: " + publicKeyHex);
            }
            
            WalletInfo wallet = walletOpt.get();
            if (wallet.getStatus() != WalletStatus.UNLOCKED) {
                throw new WalletException("Wallet is not unlocked: " + publicKeyHex);
            }
            
            // 检查质押金额
            long currentStake = walletAccountService.getStake(publicKeyHex);
            if (currentStake < unstakeAmount) {
                throw new WalletException("Insufficient stake: required=" + unstakeAmount + ", available=" + currentStake);
            }
            
            // 检查余额（用于支付手续费）
            long balance = walletAccountService.getBalance(publicKeyHex);
            if (balance < fee) {
                throw new WalletException("Insufficient balance for fee: required=" + fee + ", available=" + balance);
            }
            
            // 获取nonce
            long nonce = walletAccountService.getNonce(publicKeyHex);
            
            // 构建交易
            Transaction transaction = Transaction.builder()
                .fromPublicKey(publicKeyHex)
                .toPublicKey(publicKeyHex) // 解质押给自己
                .amount(unstakeAmount)
                .fee(fee)
                .nonce(nonce)
                .timestamp(LocalDateTime.now())
                .type(TransactionType.UNSTAKE)
                .build();
            
            // 签名交易
            Transaction signedTransaction = signTransaction(transaction, publicKeyHex, password);
            
            logger.info("Built unstake transaction: hash={}", signedTransaction.getHash());
            return signedTransaction;
        } catch (Exception e) {
            logger.error("Failed to build unstake transaction", e);
            throw new WalletException("Failed to build unstake transaction", e);
        }
    }
    
    /**
     * 签名交易
     * 
     * @param transaction 要签名的交易
     * @param publicKeyHex 公钥十六进制字符串
     * @param password 钱包密码
     * @return 签名后的交易
     * @throws WalletException 钱包异常
     */
    public Transaction signTransaction(Transaction transaction, String publicKeyHex, String password) throws WalletException {
        if (transaction == null) {
            throw new WalletException("Transaction cannot be null");
        }
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.debug("Signing transaction: publicKey={}", publicKeyHex);
            
            // 获取加密的私钥
            Optional<String> encryptedPrivateKeyOpt = walletStorageService.getEncryptedPrivateKey(publicKeyHex);
            if (encryptedPrivateKeyOpt.isEmpty()) {
                throw new WalletException("Encrypted private key not found: " + publicKeyHex);
            }
            
            // 解密私钥
            String encryptedPrivateKey = encryptedPrivateKeyOpt.get();
            String privateKeyHex = encryptionService.decrypt(encryptedPrivateKey, password);
            
            // 导入密钥对
            WalletKeyPair keyPair = keyManagementService.importKeyPair(privateKeyHex);
            
            // 验证公钥匹配
            if (!keyPair.getPublicKeyHex().equalsIgnoreCase(publicKeyHex)) {
                throw new WalletException("Public key mismatch");
            }
            
            // 计算交易哈希
            String transactionHash = calculateTransactionHash(transaction);
            
            // 签名交易哈希
            Ed25519KeyPair ed25519KeyPair = keyPair.getKeyPair();
            byte[] signature = ed25519KeyPair.sign(transactionHash.getBytes());
            
            // 创建签名后的交易
            Transaction signedTransaction = Transaction.builder()
                .fromPublicKey(transaction.getFromPublicKey())
                .toPublicKey(transaction.getToPublicKey())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .nonce(transaction.getNonce())
                .timestamp(transaction.getTimestamp())
                .type(transaction.getType())
                .signature(HashUtils.toHexString(signature))
                .build();
            
            logger.debug("Transaction signed successfully: hash={}", signedTransaction.getHash());
            return signedTransaction;
        } catch (Exception e) {
            logger.error("Failed to sign transaction", e);
            throw new WalletException("Failed to sign transaction", e);
        }
    }
    
    /**
     * 验证交易签名
     * 
     * @param transaction 要验证的交易
     * @return true如果签名有效，false否则
     * @throws WalletException 钱包异常
     */
    public boolean verifyTransactionSignature(Transaction transaction) throws WalletException {
        if (transaction == null) {
            throw new WalletException("Transaction cannot be null");
        }
        if (transaction.getSignature() == null || transaction.getSignature().length == 0) {
            return false;
        }
        
        try {
            logger.debug("Verifying transaction signature: hash={}", transaction.getHash());
            
            // 计算交易哈希
            String transactionHash = calculateTransactionHash(transaction);
            
            // 验证公钥格式
            String publicKeyHex = transaction.getFromPublicKey();
            if (!keyManagementService.isValidPublicKey(publicKeyHex)) {
                logger.warn("Invalid public key format: {}", publicKeyHex);
                return false;
            }
            
            // 验证签名
            byte[] signatureBytes = transaction.getSignature();
            byte[] publicKeyBytes = HashUtils.fromHexString(publicKeyHex);
            
            boolean isValid = Ed25519KeyPair.verify(publicKeyBytes, transactionHash.getBytes(), signatureBytes);
            
            logger.debug("Transaction signature verification result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.warn("Transaction signature verification failed", e);
            return false;
        }
    }
    
    /**
     * 计算交易哈希
     * 
     * @param transaction 交易
     * @return 交易哈希
     */
    private String calculateTransactionHash(Transaction transaction) {
        // 构建用于哈希的字符串（不包含签名）
        StringBuilder sb = new StringBuilder();
        sb.append(transaction.getFromPublicKey());
        sb.append(transaction.getToPublicKey());
        sb.append(transaction.getAmount());
        sb.append(transaction.getFee());
        sb.append(transaction.getNonce());
        sb.append(transaction.getTimestamp());
        sb.append(transaction.getType());
        
        return HashUtils.toHexString(HashUtils.sha256(sb.toString().getBytes()));
    }
}
