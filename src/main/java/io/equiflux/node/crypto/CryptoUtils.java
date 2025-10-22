package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码学工具类
 * 
 * <p>提供高级密码学功能，包括：
 * <ul>
 *   <li>AES-GCM加密/解密</li>
 *   <li>密钥派生</li>
 *   <li>密钥管理</li>
 *   <li>密码学安全随机数生成</li>
 * </ul>
 * 
 * <p>使用Java 21内置密码学API，确保安全性和性能。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public final class CryptoUtils {
    
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int AES_KEY_LENGTH = 256;
    private static final int PBKDF2_ITERATIONS = 100000;
    private static final int PBKDF2_KEY_LENGTH = 256;
    
    // 私有构造函数，防止实例化
    private CryptoUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 生成AES密钥
     * 
     * @return AES密钥
     * @throws CryptoException 如果密钥生成失败
     */
    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_KEY_LENGTH);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new CryptoException("Failed to generate AES key", e);
        }
    }
    
    /**
     * 从字节数组创建AES密钥
     * 
     * @param keyBytes 密钥字节数组
     * @return AES密钥
     * @throws CryptoException 如果密钥创建失败
     */
    public static SecretKey createAESKey(byte[] keyBytes) {
        if (keyBytes == null) {
            throw new IllegalArgumentException("Key bytes cannot be null");
        }
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES-256 key must be 32 bytes");
        }
        
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }
    
    /**
     * 使用AES-GCM加密数据
     * 
     * @param data 要加密的数据
     * @param key AES密钥
     * @return 加密后的数据（包含IV和密文）
     * @throws CryptoException 如果加密失败
     */
    public static byte[] encryptAES(byte[] data, SecretKey key) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            
            // 生成随机IV
            byte[] iv = HashUtils.generateRandomBytes(GCM_IV_LENGTH);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            byte[] cipherText = cipher.doFinal(data);
            
            // 将IV和密文组合
            byte[] encryptedData = new byte[GCM_IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, encryptedData, 0, GCM_IV_LENGTH);
            System.arraycopy(cipherText, 0, encryptedData, GCM_IV_LENGTH, cipherText.length);
            
            return encryptedData;
        } catch (Exception e) {
            throw new CryptoException("AES encryption failed", e);
        }
    }
    
    /**
     * 使用AES-GCM解密数据
     * 
     * @param encryptedData 加密的数据（包含IV和密文）
     * @param key AES密钥
     * @return 解密后的数据
     * @throws CryptoException 如果解密失败
     */
    public static byte[] decryptAES(byte[] encryptedData, SecretKey key) {
        if (encryptedData == null) {
            throw new IllegalArgumentException("Encrypted data cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (encryptedData.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Encrypted data too short");
        }
        
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            
            // 提取IV和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[encryptedData.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new CryptoException("AES decryption failed", e);
        }
    }
    
    /**
     * 使用PBKDF2派生密钥
     * 
     * @param password 密码
     * @param salt 盐值
     * @return 派生的密钥
     * @throws CryptoException 如果密钥派生失败
     */
    public static SecretKey deriveKey(String password, byte[] salt) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (salt == null) {
            throw new IllegalArgumentException("Salt cannot be null");
        }
        
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH);
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } catch (Exception e) {
            throw new CryptoException("Key derivation failed", e);
        }
    }
    
    /**
     * 使用PBKDF2派生密钥（指定迭代次数）
     * 
     * @param password 密码
     * @param salt 盐值
     * @param iterations 迭代次数
     * @return 派生的密钥
     * @throws CryptoException 如果密钥派生失败
     */
    public static SecretKey deriveKey(String password, byte[] salt, int iterations) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (salt == null) {
            throw new IllegalArgumentException("Salt cannot be null");
        }
        if (iterations <= 0) {
            throw new IllegalArgumentException("Iterations must be positive");
        }
        
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                password.toCharArray(), salt, iterations, PBKDF2_KEY_LENGTH);
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } catch (Exception e) {
            throw new CryptoException("Key derivation failed", e);
        }
    }
    
    /**
     * 生成安全的随机数
     * 
     * @param length 长度
     * @return 随机字节数组
     * @throws CryptoException 如果随机数生成失败
     */
    public static byte[] generateSecureRandom(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] bytes = new byte[length];
            random.nextBytes(bytes);
            return bytes;
        } catch (Exception e) {
            throw new CryptoException("Failed to generate secure random bytes", e);
        }
    }
    
    /**
     * 生成随机盐值
     * 
     * @return 32字节的随机盐值
     * @throws CryptoException 如果随机数生成失败
     */
    public static byte[] generateSalt() {
        return generateSecureRandom(32);
    }
    
    /**
     * 生成随机IV
     * 
     * @return 12字节的随机IV
     * @throws CryptoException 如果随机数生成失败
     */
    public static byte[] generateIV() {
        return generateSecureRandom(GCM_IV_LENGTH);
    }
    
    /**
     * 将密钥编码为Base64字符串
     * 
     * @param key 密钥
     * @return Base64编码的密钥字符串
     */
    public static String encodeKey(SecretKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    /**
     * 从Base64字符串解码密钥
     * 
     * @param encodedKey Base64编码的密钥字符串
     * @return 密钥
     * @throws CryptoException 如果解码失败
     */
    public static SecretKey decodeKey(String encodedKey) {
        if (encodedKey == null) {
            throw new IllegalArgumentException("Encoded key cannot be null");
        }
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            return new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } catch (Exception e) {
            throw new CryptoException("Failed to decode key", e);
        }
    }
    
    /**
     * 验证密钥强度
     * 
     * @param key 密钥
     * @return true如果密钥强度足够，false否则
     */
    public static boolean validateKeyStrength(SecretKey key) {
        if (key == null) {
            return false;
        }
        
        byte[] keyBytes = key.getEncoded();
        if (keyBytes == null || keyBytes.length < 32) {
            return false;
        }
        
        // 检查密钥的熵（简单检查）
        int uniqueBytes = 0;
        boolean[] seen = new boolean[256];
        for (byte b : keyBytes) {
            int unsigned = b & 0xFF;
            if (!seen[unsigned]) {
                seen[unsigned] = true;
                uniqueBytes++;
            }
        }
        
        // 至少要有16个不同的字节值
        return uniqueBytes >= 16;
    }
    
    /**
     * 安全地清除字节数组
     * 
     * @param bytes 要清除的字节数组
     */
    public static void secureWipe(byte[] bytes) {
        if (bytes != null) {
            SecureRandom random = new SecureRandom();
            random.nextBytes(bytes);
            // 多次覆盖以确保数据被清除
            for (int i = 0; i < 3; i++) {
                random.nextBytes(bytes);
            }
        }
    }
    
    /**
     * 安全地清除字符数组
     * 
     * @param chars 要清除的字符数组
     */
    public static void secureWipe(char[] chars) {
        if (chars != null) {
            SecureRandom random = new SecureRandom();
            for (int i = 0; i < chars.length; i++) {
                chars[i] = (char) random.nextInt(256);
            }
            // 多次覆盖
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < chars.length; j++) {
                    chars[j] = (char) random.nextInt(256);
                }
            }
        }
    }
}
