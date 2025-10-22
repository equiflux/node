package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 哈希工具类
 * 
 * <p>提供SHA-256哈希计算功能，用于：
 * <ul>
 *   <li>区块哈希计算</li>
 *   <li>VRF输入计算</li>
 *   <li>Merkle树构造</li>
 *   <li>交易哈希计算</li>
 * </ul>
 * 
 * <p>使用Java内置的MessageDigest实现，确保安全性和性能。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public final class HashUtils {
    
    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final String SHA3_256_ALGORITHM = "SHA3-256";
    
    // 私有构造函数，防止实例化
    private HashUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 计算SHA-256哈希值
     * 
     * @param input 输入数据
     * @return 32字节的SHA-256哈希值
     * @throws CryptoException 如果哈希计算失败
     */
    public static byte[] sha256(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * 计算SHA-256哈希值（字符串输入）
     * 
     * @param input 输入字符串
     * @return 32字节的SHA-256哈希值
     * @throws CryptoException 如果哈希计算失败
     */
    public static byte[] sha256(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        return sha256(input.getBytes());
    }
    
    /**
     * 计算双重SHA-256哈希值
     * 
     * <p>对输入数据进行两次SHA-256计算，常用于比特币等区块链系统。
     * 
     * @param input 输入数据
     * @return 32字节的双重SHA-256哈希值
     * @throws CryptoException 如果哈希计算失败
     */
    public static byte[] doubleSha256(byte[] input) {
        byte[] firstHash = sha256(input);
        return sha256(firstHash);
    }
    
    /**
     * 计算多个输入的组合哈希值
     * 
     * <p>将多个输入数据连接后计算SHA-256哈希值。
     * 
     * @param inputs 输入数据数组
     * @return 32字节的SHA-256哈希值
     * @throws CryptoException 如果哈希计算失败
     */
    public static byte[] sha256(byte[]... inputs) {
        if (inputs == null || inputs.length == 0) {
            throw new IllegalArgumentException("Inputs cannot be null or empty");
        }
        
        // 计算总长度
        int totalLength = 0;
        for (byte[] input : inputs) {
            if (input != null) {
                totalLength += input.length;
            }
        }
        
        // 连接所有输入
        byte[] combined = new byte[totalLength];
        int offset = 0;
        for (byte[] input : inputs) {
            if (input != null) {
                System.arraycopy(input, 0, combined, offset, input.length);
                offset += input.length;
            }
        }
        
        return sha256(combined);
    }
    
    /**
     * 计算VRF输入哈希值
     * 
     * <p>根据白皮书规范，VRF输入为：
     * <code>H(prev_block_hash || round || epoch)</code>
     * 
     * @param previousBlockHash 前一区块哈希
     * @param round 轮次
     * @param epoch 纪元
     * @return 32字节的VRF输入哈希值
     * @throws CryptoException 如果哈希计算失败
     */
    public static byte[] computeVRFInput(byte[] previousBlockHash, long round, long epoch) {
        if (previousBlockHash == null) {
            throw new IllegalArgumentException("Previous block hash cannot be null");
        }
        
        // 将round和epoch转换为字节数组
        byte[] roundBytes = longToBytes(round);
        byte[] epochBytes = longToBytes(epoch);
        
        return sha256(previousBlockHash, roundBytes, epochBytes);
    }
    
    /**
     * 计算Merkle根
     * 
     * <p>使用SHA-256计算交易列表的Merkle根。
     * 
     * @param transactions 交易哈希列表
     * @return 32字节的Merkle根
     * @throws CryptoException 如果哈希计算失败
     */
    public static byte[] computeMerkleRoot(byte[][] transactions) {
        if (transactions == null || transactions.length == 0) {
            // 空交易列表返回零哈希
            return new byte[32];
        }
        
        if (transactions.length == 1) {
            // 单个交易返回原始数据
            return transactions[0];
        }
        
        // 复制交易哈希数组
        byte[][] currentLevel = Arrays.copyOf(transactions, transactions.length);
        
        while (currentLevel.length > 1) {
            byte[][] nextLevel = new byte[(currentLevel.length + 1) / 2][];
            
            for (int i = 0; i < currentLevel.length; i += 2) {
                if (i + 1 < currentLevel.length) {
                    // 两个哈希值
                    nextLevel[i / 2] = sha256(currentLevel[i], currentLevel[i + 1]);
                } else {
                    // 奇数个哈希值，最后一个重复
                    nextLevel[i / 2] = sha256(currentLevel[i], currentLevel[i]);
                }
            }
            
            currentLevel = nextLevel;
        }
        
        return currentLevel[0];
    }
    
    /**
     * 将long值转换为8字节数组（大端序）
     * 
     * @param value long值
     * @return 8字节数组
     */
    private static byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * 将十六进制字符串转换为字节数组
     * 
     * @param hexString 十六进制字符串
     * @return 字节数组
     * @throws IllegalArgumentException 如果十六进制字符串格式不正确
     */
    public static byte[] fromHexString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }
        
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            bytes[i] = (byte) Integer.parseInt(hexString.substring(index, index + 2), 16);
        }
        return bytes;
    }
    
    /**
     * 计算SHA-3-256哈希值
     * 
     * @param input 输入数据
     * @return 32字节的SHA-3-256哈希值
     * @throws CryptoException 如果哈希计算失败
     */
    public static byte[] sha3_256(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA3_256_ALGORITHM);
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("SHA-3-256 algorithm not available", e);
        }
    }
    
    /**
     * 计算SHA-3-256哈希值（字符串输入）
     * 
     * @param input 输入字符串
     * @return 32字节的SHA-3-256哈希值
     * @throws CryptoException 如果哈希计算失败
     */
    public static byte[] sha3_256(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        return sha3_256(input.getBytes());
    }
    
    /**
     * 计算HMAC-SHA256
     * 
     * @param key 密钥
     * @param data 数据
     * @return 32字节的HMAC-SHA256值
     * @throws CryptoException 如果HMAC计算失败
     */
    public static byte[] hmacSha256(byte[] key, byte[] data) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key, "HmacSHA256");
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException("HMAC-SHA256 calculation failed", e);
        }
    }
    
    /**
     * 计算HMAC-SHA256（字符串输入）
     * 
     * @param key 密钥字符串
     * @param data 数据字符串
     * @return 32字节的HMAC-SHA256值
     * @throws CryptoException 如果HMAC计算失败
     */
    public static byte[] hmacSha256(String key, String data) {
        if (key == null) {
            throw new IllegalArgumentException("Key string cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data string cannot be null");
        }
        return hmacSha256(key.getBytes(), data.getBytes());
    }
    
    /**
     * 生成随机字节数组
     * 
     * @param length 长度
     * @return 随机字节数组
     * @throws CryptoException 如果随机数生成失败
     */
    public static byte[] generateRandomBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        try {
            java.security.SecureRandom random = java.security.SecureRandom.getInstanceStrong();
            byte[] bytes = new byte[length];
            random.nextBytes(bytes);
            return bytes;
        } catch (Exception e) {
            throw new CryptoException("Failed to generate random bytes", e);
        }
    }
    
    /**
     * 生成随机盐值
     * 
     * @return 16字节的随机盐值
     * @throws CryptoException 如果随机数生成失败
     */
    public static byte[] generateSalt() {
        return generateRandomBytes(16);
    }
    
    /**
     * 生成随机IV（初始化向量）
     * 
     * @return 12字节的随机IV
     * @throws CryptoException 如果随机数生成失败
     */
    public static byte[] generateIV() {
        return generateRandomBytes(12);
    }
}
