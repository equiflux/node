package io.equiflux.node.crypto;

import io.equiflux.node.exception.CryptoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码学工具类测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@DisplayName("密码学工具类测试")
class CryptoUtilsTest {
    
    @Test
    @DisplayName("测试AES密钥生成")
    void testGenerateAESKey() {
        SecretKey key = CryptoUtils.generateAESKey();
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertEquals(32, key.getEncoded().length); // AES-256
    }
    
    @Test
    @DisplayName("测试AES密钥创建")
    void testCreateAESKey() {
        byte[] keyBytes = HashUtils.generateRandomBytes(32);
        SecretKey key = CryptoUtils.createAESKey(keyBytes);
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertArrayEquals(keyBytes, key.getEncoded());
    }
    
    @Test
    @DisplayName("测试AES密钥创建 - 无效长度")
    void testCreateAESKeyInvalidLength() {
        byte[] keyBytes = HashUtils.generateRandomBytes(16);
        assertThrows(IllegalArgumentException.class, () -> CryptoUtils.createAESKey(keyBytes));
    }
    
    @Test
    @DisplayName("测试AES加密解密")
    void testAESEncryptionDecryption() {
        SecretKey key = CryptoUtils.generateAESKey();
        byte[] originalData = "Hello, Equiflux!".getBytes();
        
        // 加密
        byte[] encryptedData = CryptoUtils.encryptAES(originalData, key);
        assertNotNull(encryptedData);
        assertNotEquals(originalData.length, encryptedData.length); // 包含IV
        
        // 解密
        byte[] decryptedData = CryptoUtils.decryptAES(encryptedData, key);
        assertNotNull(decryptedData);
        assertArrayEquals(originalData, decryptedData);
    }
    
    @Test
    @DisplayName("测试AES加密解密 - 空数据")
    void testAESEncryptionDecryptionEmptyData() {
        SecretKey key = CryptoUtils.generateAESKey();
        byte[] originalData = new byte[0];
        
        byte[] encryptedData = CryptoUtils.encryptAES(originalData, key);
        byte[] decryptedData = CryptoUtils.decryptAES(encryptedData, key);
        
        assertArrayEquals(originalData, decryptedData);
    }
    
    @Test
    @DisplayName("测试AES解密 - 无效数据")
    void testAESDecryptionInvalidData() {
        SecretKey key = CryptoUtils.generateAESKey();
        byte[] invalidData = HashUtils.generateRandomBytes(10);
        
        assertThrows(IllegalArgumentException.class, () -> CryptoUtils.decryptAES(invalidData, key));
    }
    
    @Test
    @DisplayName("测试PBKDF2密钥派生")
    void testPBKDF2KeyDerivation() {
        String password = "testPassword123";
        byte[] salt = CryptoUtils.generateSalt();
        
        SecretKey key1 = CryptoUtils.deriveKey(password, salt);
        SecretKey key2 = CryptoUtils.deriveKey(password, salt);
        
        assertNotNull(key1);
        assertNotNull(key2);
        assertArrayEquals(key1.getEncoded(), key2.getEncoded());
    }
    
    @Test
    @DisplayName("测试PBKDF2密钥派生 - 不同盐值")
    void testPBKDF2KeyDerivationDifferentSalt() {
        String password = "testPassword123";
        byte[] salt1 = CryptoUtils.generateSalt();
        byte[] salt2 = CryptoUtils.generateSalt();
        
        SecretKey key1 = CryptoUtils.deriveKey(password, salt1);
        SecretKey key2 = CryptoUtils.deriveKey(password, salt2);
        
        assertNotNull(key1);
        assertNotNull(key2);
        assertFalse(java.util.Arrays.equals(key1.getEncoded(), key2.getEncoded()));
    }
    
    @Test
    @DisplayName("测试PBKDF2密钥派生 - 自定义迭代次数")
    void testPBKDF2KeyDerivationCustomIterations() {
        String password = "testPassword123";
        byte[] salt = CryptoUtils.generateSalt();
        
        SecretKey key1 = CryptoUtils.deriveKey(password, salt, 50000);
        SecretKey key2 = CryptoUtils.deriveKey(password, salt, 100000);
        
        assertNotNull(key1);
        assertNotNull(key2);
        assertFalse(java.util.Arrays.equals(key1.getEncoded(), key2.getEncoded()));
    }
    
    @Test
    @DisplayName("测试安全随机数生成")
    void testGenerateSecureRandom() {
        byte[] random1 = CryptoUtils.generateSecureRandom(32);
        byte[] random2 = CryptoUtils.generateSecureRandom(32);
        
        assertNotNull(random1);
        assertNotNull(random2);
        assertEquals(32, random1.length);
        assertEquals(32, random2.length);
        assertFalse(java.util.Arrays.equals(random1, random2));
    }
    
    @Test
    @DisplayName("测试盐值生成")
    void testGenerateSalt() {
        byte[] salt1 = CryptoUtils.generateSalt();
        byte[] salt2 = CryptoUtils.generateSalt();
        
        assertNotNull(salt1);
        assertNotNull(salt2);
        assertEquals(32, salt1.length);
        assertEquals(32, salt2.length);
        assertFalse(java.util.Arrays.equals(salt1, salt2));
    }
    
    @Test
    @DisplayName("测试IV生成")
    void testGenerateIV() {
        byte[] iv1 = CryptoUtils.generateIV();
        byte[] iv2 = CryptoUtils.generateIV();
        
        assertNotNull(iv1);
        assertNotNull(iv2);
        assertEquals(12, iv1.length);
        assertEquals(12, iv2.length);
        assertFalse(java.util.Arrays.equals(iv1, iv2));
    }
    
    @Test
    @DisplayName("测试密钥编码解码")
    void testKeyEncodingDecoding() {
        SecretKey originalKey = CryptoUtils.generateAESKey();
        
        // 编码
        String encodedKey = CryptoUtils.encodeKey(originalKey);
        assertNotNull(encodedKey);
        assertFalse(encodedKey.isEmpty());
        
        // 解码
        SecretKey decodedKey = CryptoUtils.decodeKey(encodedKey);
        assertNotNull(decodedKey);
        assertArrayEquals(originalKey.getEncoded(), decodedKey.getEncoded());
    }
    
    @Test
    @DisplayName("测试密钥强度验证")
    void testValidateKeyStrength() {
        SecretKey strongKey = CryptoUtils.generateAESKey();
        assertTrue(CryptoUtils.validateKeyStrength(strongKey));
        
        // 创建弱密钥（所有字节相同）
        byte[] weakKeyBytes = new byte[32];
        java.util.Arrays.fill(weakKeyBytes, (byte) 0x00);
        SecretKey weakKey = CryptoUtils.createAESKey(weakKeyBytes);
        assertFalse(CryptoUtils.validateKeyStrength(weakKey));
    }
    
    @Test
    @DisplayName("测试安全清除字节数组")
    void testSecureWipeBytes() {
        byte[] data = HashUtils.generateRandomBytes(32);
        byte[] originalData = data.clone();
        
        CryptoUtils.secureWipe(data);
        
        // 验证数据已被清除（不应该是原始数据）
        assertFalse(java.util.Arrays.equals(originalData, data));
    }
    
    @Test
    @DisplayName("测试安全清除字符数组")
    void testSecureWipeChars() {
        char[] data = "testPassword123".toCharArray();
        char[] originalData = data.clone();
        
        CryptoUtils.secureWipe(data);
        
        // 验证数据已被清除
        assertFalse(java.util.Arrays.equals(originalData, data));
    }
    
    @Test
    @DisplayName("测试安全清除 - 空数组")
    void testSecureWipeNull() {
        assertDoesNotThrow(() -> CryptoUtils.secureWipe((byte[]) null));
        assertDoesNotThrow(() -> CryptoUtils.secureWipe((char[]) null));
    }
}
