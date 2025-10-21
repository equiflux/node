package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Objects;

/**
 * Ed25519密钥对
 * 
 * <p>使用Java内置的EdDSA算法实现Ed25519签名方案，提供：
 * <ul>
 *   <li>密钥对生成</li>
 *   <li>数字签名生成</li>
 *   <li>数字签名验证</li>
 *   <li>密钥序列化/反序列化</li>
 * </ul>
 * 
 * <p>Ed25519是一种高性能的椭圆曲线数字签名算法，具有以下特点：
 * <ul>
 *   <li>签名速度快</li>
 *   <li>签名大小固定（64字节）</li>
 *   <li>公钥大小固定（32字节）</li>
 *   <li>安全性高</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class Ed25519KeyPair {
    
    private static final String ED25519_ALGORITHM = "EdDSA";
    private static final String ED25519_PROVIDER = "SunEC";
    
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    
    /**
     * 构造Ed25519密钥对
     * 
     * @param privateKey 私钥
     * @param publicKey 公钥
     */
    public Ed25519KeyPair(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = Objects.requireNonNull(privateKey, "Private key cannot be null");
        this.publicKey = Objects.requireNonNull(publicKey, "Public key cannot be null");
    }
    
    /**
     * 生成新的Ed25519密钥对
     * 
     * @return 新的Ed25519密钥对
     * @throws CryptoException 如果密钥生成失败
     */
    public static Ed25519KeyPair generate() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            keyGen.initialize(255); // Ed25519 curve size
            
            KeyPair keyPair = keyGen.generateKeyPair();
            return new Ed25519KeyPair(keyPair.getPrivate(), keyPair.getPublic());
        } catch (Exception e) {
            throw new CryptoException("Failed to generate Ed25519 key pair", e);
        }
    }
    
    /**
     * 生成数字签名
     * 
     * @param data 要签名的数据
     * @return 64字节的数字签名
     * @throws CryptoException 如果签名生成失败
     */
    public byte[] sign(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data to sign cannot be null");
        }
        
        try {
            Signature signature = Signature.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new CryptoException("Failed to generate signature", e);
        }
    }
    
    /**
     * 验证数字签名
     * 
     * @param data 原始数据
     * @param signature 数字签名
     * @return true如果签名有效，false否则
     * @throws CryptoException 如果签名验证失败
     */
    public boolean verify(byte[] data, byte[] signature) {
        if (data == null) {
            throw new IllegalArgumentException("Data to verify cannot be null");
        }
        if (signature == null) {
            throw new IllegalArgumentException("Signature cannot be null");
        }
        
        try {
            Signature sig = Signature.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            // 验证失败时返回false而不是抛出异常
            return false;
        }
    }
    
    /**
     * 使用公钥验证签名
     * 
     * @param publicKey 公钥
     * @param data 原始数据
     * @param signature 数字签名
     * @return true如果签名有效，false否则
     * @throws CryptoException 如果签名验证失败
     */
    public static boolean verify(PublicKey publicKey, byte[] data, byte[] signature) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data to verify cannot be null");
        }
        if (signature == null) {
            throw new IllegalArgumentException("Signature cannot be null");
        }
        
        try {
            Signature sig = Signature.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            // 验证失败时返回false而不是抛出异常
            return false;
        }
    }
    
    /**
     * 验证数字签名（使用byte[]公钥）
     * 
     * @param publicKeyBytes 公钥字节数组
     * @param data 原始数据
     * @param signature 数字签名
     * @return true如果签名有效，false否则
     * @throws CryptoException 如果签名验证失败
     */
    public static boolean verify(byte[] publicKeyBytes, byte[] data, byte[] signature) {
        if (publicKeyBytes == null) {
            throw new IllegalArgumentException("Public key bytes cannot be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data to verify cannot be null");
        }
        if (signature == null) {
            throw new IllegalArgumentException("Signature cannot be null");
        }
        
        try {
            // 从字节数组重建公钥
            PublicKey publicKey = reconstructPublicKey(publicKeyBytes);
            return verify(publicKey, data, signature);
        } catch (Exception e) {
            // 验证失败时返回false而不是抛出异常
            return false;
        }
    }
    
    /**
     * 从字节数组重建公钥
     * 
     * @param publicKeyBytes 公钥字节数组
     * @return PublicKey对象
     * @throws CryptoException 如果重建失败
     */
    private static PublicKey reconstructPublicKey(byte[] publicKeyBytes) throws CryptoException {
        try {
            // 使用KeyFactory重建公钥
            KeyFactory keyFactory = KeyFactory.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new CryptoException("Failed to reconstruct public key from bytes", e);
        }
    }
    
    /**
     * 获取私钥
     * 
     * @return 私钥
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    /**
     * 获取公钥
     * 
     * @return 公钥
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    /**
     * 获取私钥的编码形式
     * 
     * @return 私钥的字节数组
     */
    public byte[] getPrivateKeyBytes() {
        return privateKey.getEncoded();
    }
    
    /**
     * 获取公钥的编码形式
     * 
     * @return 公钥的字节数组
     */
    public byte[] getPublicKeyBytes() {
        return publicKey.getEncoded();
    }
    
    /**
     * 获取公钥的十六进制字符串表示
     * 
     * @return 公钥的十六进制字符串
     */
    public String getPublicKeyHex() {
        return HashUtils.toHexString(getPublicKeyBytes());
    }
    
    /**
     * 获取私钥的十六进制字符串表示
     * 
     * @return 私钥的十六进制字符串
     */
    public String getPrivateKeyHex() {
        return HashUtils.toHexString(getPrivateKeyBytes());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Ed25519KeyPair that = (Ed25519KeyPair) obj;
        return Arrays.equals(privateKey.getEncoded(), that.privateKey.getEncoded()) &&
               Arrays.equals(publicKey.getEncoded(), that.publicKey.getEncoded());
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(privateKey.getEncoded()) * 31 + Arrays.hashCode(publicKey.getEncoded());
    }
    
    @Override
    public String toString() {
        return "Ed25519KeyPair{" +
               "publicKey=" + getPublicKeyHex() +
               '}';
    }
}
