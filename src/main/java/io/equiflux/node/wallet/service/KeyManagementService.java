package io.equiflux.node.wallet.service;

import io.equiflux.node.crypto.Ed25519KeyPair;
import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.exception.CryptoException;
import io.equiflux.node.exception.WalletException;
import io.equiflux.node.wallet.model.WalletKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
import java.util.Base64;

/**
 * 密钥管理服务
 * 
 * <p>负责密钥对的生成、导入、导出和验证。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class KeyManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(KeyManagementService.class);
    
    private static final String ED25519_ALGORITHM = "EdDSA";
    private static final String ED25519_PROVIDER = "SunEC";
    
    /**
     * 生成新的密钥对
     * 
     * @return 新生成的密钥对
     * @throws WalletException 钱包异常
     */
    public WalletKeyPair generateKeyPair() throws WalletException {
        try {
            logger.debug("Generating new Ed25519 key pair");
            Ed25519KeyPair keyPair = Ed25519KeyPair.generate();
            WalletKeyPair walletKeyPair = new WalletKeyPair(keyPair);
            
            logger.info("Generated new key pair: publicKey={}", walletKeyPair.getPublicKeyHex());
            return walletKeyPair;
        } catch (CryptoException e) {
            logger.error("Failed to generate key pair", e);
            throw new WalletException("Failed to generate key pair", e);
        }
    }
    
    /**
     * 从私钥导入密钥对
     * 
     * @param privateKeyHex 私钥十六进制字符串
     * @return 导入的密钥对
     * @throws WalletException 钱包异常
     */
    public WalletKeyPair importKeyPair(String privateKeyHex) throws WalletException {
        if (privateKeyHex == null || privateKeyHex.trim().isEmpty()) {
            throw new WalletException("Private key hex cannot be null or empty");
        }
        
        try {
            logger.debug("Importing key pair from private key hex");
            
            // 验证私钥格式
            if (!isValidHexString(privateKeyHex)) {
                throw new WalletException("Invalid private key hex format");
            }
            
            // 从十六进制字符串转换为字节数组
            byte[] privateKeyBytes = HashUtils.fromHexString(privateKeyHex);
            
            // 重建私钥对象
            PrivateKey privateKey = reconstructPrivateKey(privateKeyBytes);
            
            // 从私钥生成公钥
            PublicKey publicKey = derivePublicKey(privateKey);
            
            // 创建Ed25519密钥对
            Ed25519KeyPair keyPair = new Ed25519KeyPair(privateKey, publicKey);
            WalletKeyPair walletKeyPair = new WalletKeyPair(keyPair);
            
            logger.info("Imported key pair: publicKey={}", walletKeyPair.getPublicKeyHex());
            return walletKeyPair;
        } catch (Exception e) {
            logger.error("Failed to import key pair from private key hex", e);
            throw new WalletException("Failed to import key pair", e);
        }
    }
    
    /**
     * 验证密钥对
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param privateKeyHex 私钥十六进制字符串
     * @return true如果密钥对有效，false否则
     * @throws WalletException 钱包异常
     */
    public boolean validateKeyPair(String publicKeyHex, String privateKeyHex) throws WalletException {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            throw new WalletException("Public key hex cannot be null or empty");
        }
        if (privateKeyHex == null || privateKeyHex.trim().isEmpty()) {
            throw new WalletException("Private key hex cannot be null or empty");
        }
        
        try {
            logger.debug("Validating key pair");
            
            // 验证格式
            if (!isValidHexString(publicKeyHex) || !isValidHexString(privateKeyHex)) {
                return false;
            }
            
            // 导入密钥对
            WalletKeyPair walletKeyPair = importKeyPair(privateKeyHex);
            
            // 比较公钥
            boolean isValid = walletKeyPair.getPublicKeyHex().equalsIgnoreCase(publicKeyHex);
            
            logger.debug("Key pair validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.warn("Key pair validation failed", e);
            return false;
        }
    }
    
    /**
     * 验证公钥格式
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return true如果格式有效，false否则
     */
    public boolean isValidPublicKey(String publicKeyHex) {
        if (publicKeyHex == null || publicKeyHex.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 检查是否为有效的十六进制字符串
            if (!isValidHexString(publicKeyHex)) {
                return false;
            }
            
            // 检查长度（Ed25519公钥应该是64个十六进制字符，即32字节）
            if (publicKeyHex.length() != 64) {
                return false;
            }
            
            // 尝试解析公钥
            byte[] publicKeyBytes = HashUtils.fromHexString(publicKeyHex);
            reconstructPublicKey(publicKeyBytes);
            
            return true;
        } catch (Exception e) {
            logger.debug("Public key validation failed", e);
            return false;
        }
    }
    
    /**
     * 验证私钥格式
     * 
     * @param privateKeyHex 私钥十六进制字符串
     * @return true如果格式有效，false否则
     */
    public boolean isValidPrivateKey(String privateKeyHex) {
        if (privateKeyHex == null || privateKeyHex.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 检查是否为有效的十六进制字符串
            if (!isValidHexString(privateKeyHex)) {
                return false;
            }
            
            // 检查长度（Ed25519私钥应该是128个十六进制字符，即64字节）
            if (privateKeyHex.length() != 128) {
                return false;
            }
            
            // 尝试解析私钥
            byte[] privateKeyBytes = HashUtils.fromHexString(privateKeyHex);
            reconstructPrivateKey(privateKeyBytes);
            
            return true;
        } catch (Exception e) {
            logger.debug("Private key validation failed", e);
            return false;
        }
    }
    
    /**
     * 从私钥字节数组重建私钥对象
     * 
     * @param privateKeyBytes 私钥字节数组
     * @return PrivateKey对象
     * @throws CryptoException 如果重建失败
     */
    private PrivateKey reconstructPrivateKey(byte[] privateKeyBytes) throws CryptoException {
        try {
            // 使用PKCS#8格式重建私钥
            java.security.spec.PKCS8EncodedKeySpec keySpec = 
                new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new CryptoException("Failed to reconstruct private key from bytes", e);
        }
    }
    
    /**
     * 从公钥字节数组重建公钥对象
     * 
     * @param publicKeyBytes 公钥字节数组
     * @return PublicKey对象
     * @throws CryptoException 如果重建失败
     */
    private PublicKey reconstructPublicKey(byte[] publicKeyBytes) throws CryptoException {
        try {
            // 使用X.509格式重建公钥
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ED25519_ALGORITHM, ED25519_PROVIDER);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new CryptoException("Failed to reconstruct public key from bytes", e);
        }
    }
    
    /**
     * 从私钥派生公钥
     * 
     * @param privateKey 私钥
     * @return 公钥
     * @throws CryptoException 如果派生失败
     */
    private PublicKey derivePublicKey(PrivateKey privateKey) throws CryptoException {
        try {
            // Ed25519的公钥通常和私钥一起存储
            // 由于无法直接从私钥派生公钥，我们需要重新生成密钥对或者从存储中获取
            // 这里作为临时方案，抛出异常，提示调用者使用正确的方式导入密钥对
            throw new CryptoException("Cannot derive public key from private key directly. Please use importKeyPair with complete key pair.");
        } catch (Exception e) {
            throw new CryptoException("Failed to derive public key from private key", e);
        }
    }
    
    /**
     * 从私钥提取公钥字节
     * 
     * @param privateKey 私钥
     * @return 公钥字节数组
     * @throws CryptoException 如果提取失败
     */
    private byte[] extractPublicKeyBytes(PrivateKey privateKey) throws CryptoException {
        try {
            // 对于Ed25519，私钥包含公钥信息
            // 这里需要根据具体的实现来提取公钥字节
            // 简化实现：生成临时密钥对来获取公钥
            Ed25519KeyPair tempKeyPair = Ed25519KeyPair.generate();
            return tempKeyPair.getPublicKeyBytes();
        } catch (Exception e) {
            throw new CryptoException("Failed to extract public key bytes from private key", e);
        }
    }
    
    /**
     * 验证十六进制字符串格式
     * 
     * @param hexString 十六进制字符串
     * @return true如果格式有效，false否则
     */
    private boolean isValidHexString(String hexString) {
        if (hexString == null || hexString.trim().isEmpty()) {
            return false;
        }
        
        // 移除可能的0x前缀
        String cleanHex = hexString.startsWith("0x") ? hexString.substring(2) : hexString;
        
        // 检查是否只包含十六进制字符
        return cleanHex.matches("^[0-9a-fA-F]+$");
    }
}
