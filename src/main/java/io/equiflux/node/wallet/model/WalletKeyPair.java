package io.equiflux.node.wallet.model;

import io.equiflux.node.crypto.Ed25519KeyPair;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 钱包密钥对
 * 
 * <p>包含密钥对和相关的钱包信息。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class WalletKeyPair {
    
    private final Ed25519KeyPair keyPair;
    private final String publicKeyHex;
    private final String privateKeyHex;
    private final LocalDateTime createdAt;
    
    /**
     * 构造钱包密钥对
     * 
     * @param keyPair Ed25519密钥对
     */
    public WalletKeyPair(Ed25519KeyPair keyPair) {
        this.keyPair = Objects.requireNonNull(keyPair, "Key pair cannot be null");
        this.publicKeyHex = keyPair.getPublicKeyHex();
        this.privateKeyHex = keyPair.getPrivateKeyHex();
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 获取Ed25519密钥对
     * 
     * @return Ed25519密钥对
     */
    public Ed25519KeyPair getKeyPair() {
        return keyPair;
    }
    
    /**
     * 获取公钥
     * 
     * @return 公钥
     */
    public PublicKey getPublicKey() {
        return keyPair.getPublicKey();
    }
    
    /**
     * 获取公钥十六进制字符串
     * 
     * @return 公钥十六进制字符串
     */
    public String getPublicKeyHex() {
        return publicKeyHex;
    }
    
    /**
     * 获取私钥十六进制字符串
     * 
     * @return 私钥十六进制字符串
     */
    public String getPrivateKeyHex() {
        return privateKeyHex;
    }
    
    /**
     * 获取创建时间
     * 
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WalletKeyPair that = (WalletKeyPair) obj;
        return Objects.equals(publicKeyHex, that.publicKeyHex);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(publicKeyHex);
    }
    
    @Override
    public String toString() {
        return "WalletKeyPair{" +
               "publicKeyHex='" + publicKeyHex + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}
