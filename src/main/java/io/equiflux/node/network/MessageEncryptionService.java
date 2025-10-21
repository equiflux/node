package io.equiflux.node.network;

import io.equiflux.node.crypto.Ed25519KeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息加密服务
 * 
 * <p>负责网络消息的加密和解密，确保消息传输的安全性。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>ECDH密钥协商</li>
 *   <li>AES-GCM加密和解密</li>
 *   <li>密钥管理</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class MessageEncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageEncryptionService.class);
    
    @Autowired
    private NetworkConfig networkConfig;
    
    @Autowired
    private Ed25519KeyPair localKeyPair;
    
    // 加密配置
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ECDH_ALGORITHM = "ECDH";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    // 密钥缓存
    private final ConcurrentHashMap<String, SecretKey> sharedKeys = new ConcurrentHashMap<>();
    
    // 统计信息
    private long encryptionCount = 0;
    private long decryptionCount = 0;
    private long encryptionFailures = 0;
    private long decryptionFailures = 0;
    
    @PostConstruct
    public void init() {
        logger.info("初始化消息加密服务，加密启用: {}", networkConfig.isEnableEncryption());
    }
    
    /**
     * 加密消息数据
     * 
     * @param data 原始数据
     * @param peerPublicKey 对端公钥
     * @return 加密后的数据
     */
    public byte[] encrypt(byte[] data, PublicKey peerPublicKey) {
        if (!networkConfig.isEnableEncryption()) {
            return data; // 如果未启用加密，直接返回原数据
        }
        
        if (data == null || data.length == 0) {
            return data;
        }
        
        try {
            // 获取共享密钥
            SecretKey sharedKey = getSharedKey(peerPublicKey);
            if (sharedKey == null) {
                logger.warn("无法获取共享密钥，跳过加密");
                return data;
            }
            
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            // 创建加密器
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, sharedKey, gcmSpec);
            
            // 加密数据
            byte[] encryptedData = cipher.doFinal(data);
            
            // 组合IV和加密数据
            byte[] result = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, result, GCM_IV_LENGTH, encryptedData.length);
            
            encryptionCount++;
            logger.debug("消息加密完成，原始大小: {} 字节，加密后: {} 字节", 
                        data.length, result.length);
            
            return result;
            
        } catch (Exception e) {
            logger.error("消息加密失败", e);
            encryptionFailures++;
            return data; // 加密失败时返回原数据
        }
    }
    
    /**
     * 解密消息数据
     * 
     * @param encryptedData 加密的数据
     * @param peerPublicKey 对端公钥
     * @return 解密后的数据
     */
    public byte[] decrypt(byte[] encryptedData, PublicKey peerPublicKey) {
        if (!networkConfig.isEnableEncryption()) {
            return encryptedData; // 如果未启用加密，直接返回原数据
        }
        
        if (encryptedData == null || encryptedData.length <= GCM_IV_LENGTH) {
            return encryptedData;
        }
        
        try {
            // 获取共享密钥
            SecretKey sharedKey = getSharedKey(peerPublicKey);
            if (sharedKey == null) {
                logger.warn("无法获取共享密钥，跳过解密");
                return encryptedData;
            }
            
            // 分离IV和加密数据
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherData = new byte[encryptedData.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherData, 0, cipherData.length);
            
            // 创建解密器
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, sharedKey, gcmSpec);
            
            // 解密数据
            byte[] decryptedData = cipher.doFinal(cipherData);
            
            decryptionCount++;
            logger.debug("消息解密完成，加密大小: {} 字节，解密后: {} 字节", 
                        encryptedData.length, decryptedData.length);
            
            return decryptedData;
            
        } catch (Exception e) {
            logger.error("消息解密失败", e);
            decryptionFailures++;
            return encryptedData; // 解密失败时返回原数据
        }
    }
    
    /**
     * 检查数据是否已加密
     * 
     * @param data 数据
     * @return true如果已加密，false否则
     */
    public boolean isEncrypted(byte[] data) {
        if (data == null || data.length <= GCM_IV_LENGTH) {
            return false;
        }
        
        // 简单检查：如果数据长度大于IV长度，可能已加密
        return data.length > GCM_IV_LENGTH;
    }
    
    /**
     * 获取共享密钥
     * 
     * @param peerPublicKey 对端公钥
     * @return 共享密钥
     */
    private SecretKey getSharedKey(PublicKey peerPublicKey) {
        try {
            String keyId = Base64.getEncoder().encodeToString(peerPublicKey.getEncoded());
            
            // 检查缓存
            SecretKey cachedKey = sharedKeys.get(keyId);
            if (cachedKey != null) {
                return cachedKey;
            }
            
            // 生成新的共享密钥
            KeyAgreement keyAgreement = KeyAgreement.getInstance(ECDH_ALGORITHM);
            keyAgreement.init(localKeyPair.getPrivateKey());
            keyAgreement.doPhase(peerPublicKey, true);
            
            byte[] sharedSecret = keyAgreement.generateSecret();
            
            // 使用SHA-256生成AES密钥
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(sharedSecret);
            SecretKey sharedKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            // 缓存密钥
            sharedKeys.put(keyId, sharedKey);
            
            logger.debug("生成新的共享密钥，对端: {}", keyId.substring(0, 8) + "...");
            
            return sharedKey;
            
        } catch (Exception e) {
            logger.error("生成共享密钥失败", e);
            return null;
        }
    }
    
    /**
     * 清理过期的密钥缓存
     */
    public void cleanupExpiredKeys() {
        // 简单实现：清理所有缓存（实际应用中可以实现更复杂的过期策略）
        int size = sharedKeys.size();
        sharedKeys.clear();
        
        if (size > 0) {
            logger.debug("清理过期密钥缓存: {} 个", size);
        }
    }
    
    /**
     * 获取加密统计信息
     * 
     * @return 加密统计信息
     */
    public EncryptionStats getStats() {
        return new EncryptionStats(
            encryptionCount,
            decryptionCount,
            encryptionFailures,
            decryptionFailures,
            sharedKeys.size()
        );
    }
    
    /**
     * 加密统计信息
     */
    public static class EncryptionStats {
        private final long encryptionCount;
        private final long decryptionCount;
        private final long encryptionFailures;
        private final long decryptionFailures;
        private final int cachedKeys;
        
        public EncryptionStats(long encryptionCount, long decryptionCount,
                             long encryptionFailures, long decryptionFailures,
                             int cachedKeys) {
            this.encryptionCount = encryptionCount;
            this.decryptionCount = decryptionCount;
            this.encryptionFailures = encryptionFailures;
            this.decryptionFailures = decryptionFailures;
            this.cachedKeys = cachedKeys;
        }
        
        public long getEncryptionCount() {
            return encryptionCount;
        }
        
        public long getDecryptionCount() {
            return decryptionCount;
        }
        
        public long getEncryptionFailures() {
            return encryptionFailures;
        }
        
        public long getDecryptionFailures() {
            return decryptionFailures;
        }
        
        public int getCachedKeys() {
            return cachedKeys;
        }
        
        public double getEncryptionSuccessRate() {
            long total = encryptionCount + encryptionFailures;
            return total > 0 ? (double) encryptionCount / total : 0.0;
        }
        
        public double getDecryptionSuccessRate() {
            long total = decryptionCount + decryptionFailures;
            return total > 0 ? (double) decryptionCount / total : 0.0;
        }
        
        @Override
        public String toString() {
            return "EncryptionStats{" +
                   "encryptionCount=" + encryptionCount +
                   ", decryptionCount=" + decryptionCount +
                   ", encryptionFailures=" + encryptionFailures +
                   ", decryptionFailures=" + decryptionFailures +
                   ", cachedKeys=" + cachedKeys +
                   ", encryptionSuccessRate=" + String.format("%.2f%%", getEncryptionSuccessRate() * 100) +
                   ", decryptionSuccessRate=" + String.format("%.2f%%", getDecryptionSuccessRate() * 100) +
                   '}';
        }
    }
}
