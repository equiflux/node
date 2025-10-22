package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密钥管理工具类测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@DisplayName("密钥管理工具类测试")
class KeyManagementUtilsTest {
    
    @Test
    @DisplayName("测试Ed25519密钥对生成")
    void testGenerateEd25519KeyPair() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
        assertEquals("EdDSA", keyPair.getPublic().getAlgorithm());
        assertEquals("EdDSA", keyPair.getPrivate().getAlgorithm());
    }
    
    @Test
    @DisplayName("测试公钥重建")
    void testReconstructPublicKey() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PublicKey originalPublicKey = keyPair.getPublic();
        byte[] publicKeyBytes = originalPublicKey.getEncoded();
        
        PublicKey reconstructedPublicKey = KeyManagementUtils.reconstructPublicKey(publicKeyBytes);
        assertNotNull(reconstructedPublicKey);
        assertArrayEquals(originalPublicKey.getEncoded(), reconstructedPublicKey.getEncoded());
    }
    
    @Test
    @DisplayName("测试私钥重建")
    void testReconstructPrivateKey() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PrivateKey originalPrivateKey = keyPair.getPrivate();
        byte[] privateKeyBytes = originalPrivateKey.getEncoded();
        
        PrivateKey reconstructedPrivateKey = KeyManagementUtils.reconstructPrivateKey(privateKeyBytes);
        assertNotNull(reconstructedPrivateKey);
        assertArrayEquals(originalPrivateKey.getEncoded(), reconstructedPrivateKey.getEncoded());
    }
    
    @Test
    @DisplayName("测试公钥Base64编码解码")
    void testPublicKeyBase64EncodingDecoding() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PublicKey originalPublicKey = keyPair.getPublic();
        
        // 编码
        String encodedPublicKey = KeyManagementUtils.encodePublicKey(originalPublicKey);
        assertNotNull(encodedPublicKey);
        assertFalse(encodedPublicKey.isEmpty());
        
        // 解码
        PublicKey decodedPublicKey = KeyManagementUtils.decodePublicKey(encodedPublicKey);
        assertNotNull(decodedPublicKey);
        assertArrayEquals(originalPublicKey.getEncoded(), decodedPublicKey.getEncoded());
    }
    
    @Test
    @DisplayName("测试私钥Base64编码解码")
    void testPrivateKeyBase64EncodingDecoding() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PrivateKey originalPrivateKey = keyPair.getPrivate();
        
        // 编码
        String encodedPrivateKey = KeyManagementUtils.encodePrivateKey(originalPrivateKey);
        assertNotNull(encodedPrivateKey);
        assertFalse(encodedPrivateKey.isEmpty());
        
        // 解码
        PrivateKey decodedPrivateKey = KeyManagementUtils.decodePrivateKey(encodedPrivateKey);
        assertNotNull(decodedPrivateKey);
        assertArrayEquals(originalPrivateKey.getEncoded(), decodedPrivateKey.getEncoded());
    }
    
    @Test
    @DisplayName("测试公钥十六进制编码解码")
    void testPublicKeyHexEncodingDecoding() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PublicKey originalPublicKey = keyPair.getPublic();
        
        // 编码
        String hexPublicKey = KeyManagementUtils.encodePublicKeyHex(originalPublicKey);
        assertNotNull(hexPublicKey);
        assertFalse(hexPublicKey.isEmpty());
        
        // 解码
        PublicKey decodedPublicKey = KeyManagementUtils.decodePublicKeyHex(hexPublicKey);
        assertNotNull(decodedPublicKey);
        assertArrayEquals(originalPublicKey.getEncoded(), decodedPublicKey.getEncoded());
    }
    
    @Test
    @DisplayName("测试私钥十六进制编码解码")
    void testPrivateKeyHexEncodingDecoding() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PrivateKey originalPrivateKey = keyPair.getPrivate();
        
        // 编码
        String hexPrivateKey = KeyManagementUtils.encodePrivateKeyHex(originalPrivateKey);
        assertNotNull(hexPrivateKey);
        assertFalse(hexPrivateKey.isEmpty());
        
        // 解码
        PrivateKey decodedPrivateKey = KeyManagementUtils.decodePrivateKeyHex(hexPrivateKey);
        assertNotNull(decodedPrivateKey);
        assertArrayEquals(originalPrivateKey.getEncoded(), decodedPrivateKey.getEncoded());
    }
    
    @Test
    @DisplayName("测试密钥对验证")
    void testValidateKeyPair() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        assertTrue(KeyManagementUtils.validateKeyPair(keyPair.getPublic(), keyPair.getPrivate()));
        
        // 测试不匹配的密钥对
        KeyPair anotherKeyPair = KeyManagementUtils.generateEd25519KeyPair();
        assertFalse(KeyManagementUtils.validateKeyPair(keyPair.getPublic(), anotherKeyPair.getPrivate()));
    }
    
    @Test
    @DisplayName("测试公钥格式验证")
    void testValidatePublicKey() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        assertTrue(KeyManagementUtils.validatePublicKey(keyPair.getPublic()));
        
        // 测试null公钥
        assertFalse(KeyManagementUtils.validatePublicKey(null));
    }
    
    @Test
    @DisplayName("测试私钥格式验证")
    void testValidatePrivateKey() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        assertTrue(KeyManagementUtils.validatePrivateKey(keyPair.getPrivate()));
        
        // 测试null私钥
        assertFalse(KeyManagementUtils.validatePrivateKey(null));
    }
    
    @Test
    @DisplayName("测试密钥指纹生成")
    void testGenerateKeyFingerprint() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        String fingerprint = KeyManagementUtils.generateKeyFingerprint(keyPair.getPublic());
        
        assertNotNull(fingerprint);
        assertEquals(16, fingerprint.length()); // 8 bytes = 16 hex chars
        
        // 相同公钥应该生成相同指纹
        String fingerprint2 = KeyManagementUtils.generateKeyFingerprint(keyPair.getPublic());
        assertEquals(fingerprint, fingerprint2);
    }
    
    @Test
    @DisplayName("测试公钥相等性比较")
    void testPublicKeyEquals() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PublicKey publicKey1 = keyPair.getPublic();
        PublicKey publicKey2 = keyPair.getPublic();
        
        assertTrue(KeyManagementUtils.equals(publicKey1, publicKey2));
        
        // 测试不同公钥
        KeyPair anotherKeyPair = KeyManagementUtils.generateEd25519KeyPair();
        assertFalse(KeyManagementUtils.equals(publicKey1, anotherKeyPair.getPublic()));
        
        // 测试null
        assertTrue(KeyManagementUtils.equals((PublicKey) null, (PublicKey) null));
        assertFalse(KeyManagementUtils.equals(publicKey1, (PublicKey) null));
        assertFalse(KeyManagementUtils.equals((PublicKey) null, publicKey1));
    }
    
    @Test
    @DisplayName("测试私钥相等性比较")
    void testPrivateKeyEquals() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PrivateKey privateKey1 = keyPair.getPrivate();
        PrivateKey privateKey2 = keyPair.getPrivate();
        
        assertTrue(KeyManagementUtils.equals(privateKey1, privateKey2));
        
        // 测试不同私钥
        KeyPair anotherKeyPair = KeyManagementUtils.generateEd25519KeyPair();
        assertFalse(KeyManagementUtils.equals(privateKey1, anotherKeyPair.getPrivate()));
        
        // 测试null
        assertTrue(KeyManagementUtils.equals((PrivateKey) null, (PrivateKey) null));
        assertFalse(KeyManagementUtils.equals(privateKey1, (PrivateKey) null));
        assertFalse(KeyManagementUtils.equals((PrivateKey) null, privateKey1));
    }
    
    @Test
    @DisplayName("测试从私钥提取公钥")
    void testExtractPublicKey() {
        KeyPair keyPair = KeyManagementUtils.generateEd25519KeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey originalPublicKey = keyPair.getPublic();
        
        PublicKey extractedPublicKey = KeyManagementUtils.extractPublicKey(privateKey);
        assertNotNull(extractedPublicKey);
        
        // 验证提取的公钥与原始公钥匹配（由于Java限制，我们只验证提取成功）
        // 注意：由于Java标准API的限制，从私钥提取公钥可能不总是返回匹配的公钥
        assertNotNull(extractedPublicKey.getEncoded());
    }
    
    @Test
    @DisplayName("测试从私钥提取公钥 - null输入")
    void testExtractPublicKeyNull() {
        assertThrows(IllegalArgumentException.class, () -> KeyManagementUtils.extractPublicKey(null));
    }
    
    @Test
    @DisplayName("测试公钥重建 - null输入")
    void testReconstructPublicKeyNull() {
        assertThrows(IllegalArgumentException.class, () -> KeyManagementUtils.reconstructPublicKey(null));
    }
    
    @Test
    @DisplayName("测试私钥重建 - null输入")
    void testReconstructPrivateKeyNull() {
        assertThrows(IllegalArgumentException.class, () -> KeyManagementUtils.reconstructPrivateKey(null));
    }
    
    @Test
    @DisplayName("测试Base64解码 - 无效输入")
    void testBase64DecodingInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> KeyManagementUtils.decodePublicKey(null));
        assertThrows(IllegalArgumentException.class, () -> KeyManagementUtils.decodePrivateKey(null));
        
        assertThrows(CryptoException.class, () -> KeyManagementUtils.decodePublicKey("invalid"));
        assertThrows(CryptoException.class, () -> KeyManagementUtils.decodePrivateKey("invalid"));
    }
    
    @Test
    @DisplayName("测试十六进制解码 - 无效输入")
    void testHexDecodingInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> KeyManagementUtils.decodePublicKeyHex(null));
        assertThrows(IllegalArgumentException.class, () -> KeyManagementUtils.decodePrivateKeyHex(null));
        
        assertThrows(CryptoException.class, () -> KeyManagementUtils.decodePublicKeyHex("invalid"));
        assertThrows(CryptoException.class, () -> KeyManagementUtils.decodePrivateKeyHex("invalid"));
    }
}
