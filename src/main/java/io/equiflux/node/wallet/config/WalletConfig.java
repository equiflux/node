package io.equiflux.node.wallet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 钱包配置
 * 
 * <p>钱包服务的配置参数。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Component
@ConfigurationProperties(prefix = "equiflux.wallet")
@Data
public class WalletConfig {
    
    /**
     * 钱包存储目录
     */
    private String walletDir = "./wallets";
    
    /**
     * 默认钱包名称
     */
    private String defaultWalletName = "default";
    
    /**
     * 钱包自动锁定时间
     */
    private Duration autoLockTimeout = Duration.ofMinutes(30);
    
    /**
     * 最大钱包数量
     */
    private int maxWalletCount = 100;
    
    /**
     * 交易历史最大记录数
     */
    private int maxTransactionHistory = 1000;
    
    /**
     * 交易状态检查间隔
     */
    private Duration transactionStatusCheckInterval = Duration.ofSeconds(10);
    
    /**
     * 交易超时时间
     */
    private Duration transactionTimeout = Duration.ofMinutes(5);
    
    /**
     * 是否启用钱包加密
     */
    private boolean encryptionEnabled = true;
    
    /**
     * 加密算法
     */
    private String encryptionAlgorithm = "AES/GCM/NoPadding";
    
    /**
     * 密钥派生算法
     */
    private String keyDerivationAlgorithm = "PBKDF2WithHmacSHA256";
    
    /**
     * 密钥派生迭代次数
     */
    private int keyDerivationIterations = 100000;
    
    /**
     * 是否启用交易缓存
     */
    private boolean transactionCacheEnabled = true;
    
    /**
     * 交易缓存大小
     */
    private int transactionCacheSize = 1000;
    
    /**
     * 是否启用余额缓存
     */
    private boolean balanceCacheEnabled = true;
    
    /**
     * 余额缓存过期时间
     */
    private Duration balanceCacheExpiration = Duration.ofSeconds(30);
    
    /**
     * 是否启用自动备份
     */
    private boolean autoBackupEnabled = true;
    
    /**
     * 自动备份间隔
     */
    private Duration autoBackupInterval = Duration.ofHours(24);
    
    /**
     * 备份保留数量
     */
    private int backupRetentionCount = 7;
}
