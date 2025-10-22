package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.CryptoException;
import io.equiflux.node.exception.WalletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 钱包加密服务
 * 
 * <p>负责钱包数据的加密和解密，使用AES-GCM算法。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class WalletEncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletEncryptionService.class);
    
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256; // 256 bits
    private static final int ITERATIONS = 100000;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 加密数据
     * 
     * @param data 要加密的数据
     * @param password 密码
     * @return 加密后的数据（Base64编码）
     * @throws WalletException 钱包异常
     */
    public String encrypt(String data, String password) throws WalletException {
        if (data == null || data.isEmpty()) {
            throw new WalletException("Data to encrypt cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.debug("Encrypting data with password");
            
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // 从密码派生密钥
            SecretKey key = deriveKey(password, iv);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            
            // 加密数据
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // 组合IV和加密数据
            byte[] combined = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, combined, GCM_IV_LENGTH, encryptedData.length);
            
            // Base64编码
            String encrypted = Base64.getEncoder().encodeToString(combined);
            
            logger.debug("Data encrypted successfully");
            return encrypted;
        } catch (Exception e) {
            logger.error("Failed to encrypt data", e);
            throw new WalletException("Failed to encrypt data", e);
        }
    }
    
    /**
     * 解密数据
     * 
     * @param encryptedData 加密的数据（Base64编码）
     * @param password 密码
     * @return 解密后的数据
     * @throws WalletException 钱包异常
     */
    public String decrypt(String encryptedData, String password) throws WalletException {
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new WalletException("Encrypted data cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.debug("Decrypting data with password");
            
            // Base64解码
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            if (combined.length < GCM_IV_LENGTH) {
                throw new WalletException("Invalid encrypted data format");
            }
            
            // 分离IV和加密数据
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            // 从密码派生密钥
            SecretKey key = deriveKey(password, iv);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            // 解密数据
            byte[] decryptedData = cipher.doFinal(encrypted);
            String decrypted = new String(decryptedData, StandardCharsets.UTF_8);
            
            logger.debug("Data decrypted successfully");
            return decrypted;
        } catch (Exception e) {
            logger.error("Failed to decrypt data", e);
            throw new WalletException("Failed to decrypt data", e);
        }
    }
    
    /**
     * 验证密码
     * 
     * @param encryptedData 加密的数据
     * @param password 密码
     * @return true如果密码正确，false否则
     */
    public boolean verifyPassword(String encryptedData, String password) {
        try {
            decrypt(encryptedData, password);
            return true;
        } catch (Exception e) {
            logger.debug("Password verification failed", e);
            return false;
        }
    }
    
    /**
     * 生成密码哈希
     * 
     * @param password 密码
     * @return 密码哈希
     * @throws WalletException 钱包异常
     */
    public String hashPassword(String password) throws WalletException {
        if (password == null || password.isEmpty()) {
            throw new WalletException("Password cannot be null or empty");
        }
        
        try {
            logger.debug("Hashing password");
            
            // 生成随机盐
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);
            
            // 使用PBKDF2派生哈希
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            
            // 组合盐和哈希
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);
            
            String passwordHash = Base64.getEncoder().encodeToString(combined);
            
            logger.debug("Password hashed successfully");
            return passwordHash;
        } catch (Exception e) {
            logger.error("Failed to hash password", e);
            throw new WalletException("Failed to hash password", e);
        }
    }
    
    /**
     * 验证密码哈希
     * 
     * @param password 密码
     * @param passwordHash 密码哈希
     * @return true如果密码正确，false否则
     */
    public boolean verifyPasswordHash(String password, String passwordHash) {
        try {
            // Base64解码
            byte[] combined = Base64.getDecoder().decode(passwordHash);
            
            if (combined.length < 16) {
                return false;
            }
            
            // 分离盐和哈希
            byte[] salt = new byte[16];
            byte[] hash = new byte[combined.length - 16];
            System.arraycopy(combined, 0, salt, 0, 16);
            System.arraycopy(combined, 16, hash, 0, hash.length);
            
            // 重新计算哈希
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] newHash = factory.generateSecret(spec).getEncoded();
            
            // 比较哈希
            return MessageDigest.isEqual(hash, newHash);
        } catch (Exception e) {
            logger.debug("Password hash verification failed", e);
            return false;
        }
    }
    
    /**
     * 从密码派生密钥
     * 
     * @param password 密码
     * @param salt 盐
     * @return 派生的密钥
     * @throws CryptoException 密码学异常
     */
    private SecretKey deriveKey(String password, byte[] salt) throws CryptoException {
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
        } catch (Exception e) {
            throw new CryptoException("Failed to derive key from password", e);
        }
    }
    
    /**
     * 生成随机密钥
     * 
     * @return 随机密钥
     * @throws WalletException 钱包异常
     */
    public String generateRandomKey() throws WalletException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            logger.error("Failed to generate random key", e);
            throw new WalletException("Failed to generate random key", e);
        }
    }
}
