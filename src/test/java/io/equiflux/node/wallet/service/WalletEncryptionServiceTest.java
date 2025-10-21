package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.WalletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 钱包加密服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class WalletEncryptionServiceTest {
    
    @InjectMocks
    private WalletEncryptionService encryptionService;
    
    private String testData;
    private String testPassword;
    
    @BeforeEach
    void setUp() {
        testData = "test_data_to_encrypt";
        testPassword = "test_password_123";
    }
    
    @Test
    void testEncryptAndDecrypt() {
        // When
        String encrypted = encryptionService.encrypt(testData, testPassword);
        String decrypted = encryptionService.decrypt(encrypted, testPassword);
        
        // Then
        assertNotNull(encrypted);
        assertNotEquals(testData, encrypted);
        assertEquals(testData, decrypted);
    }
    
    @Test
    void testEncryptWithNullData() {
        // When & Then
        assertThrows(WalletException.class, () -> {
            encryptionService.encrypt(null, testPassword);
        });
        
        assertThrows(WalletException.class, () -> {
            encryptionService.encrypt("", testPassword);
        });
    }
    
    @Test
    void testEncryptWithNullPassword() {
        // When & Then
        assertThrows(WalletException.class, () -> {
            encryptionService.encrypt(testData, null);
        });
        
        assertThrows(WalletException.class, () -> {
            encryptionService.encrypt(testData, "");
        });
    }
    
    @Test
    void testDecryptWithInvalidPassword() {
        // Given
        String encrypted = encryptionService.encrypt(testData, testPassword);
        String wrongPassword = "wrong_password";
        
        // When & Then
        assertThrows(WalletException.class, () -> {
            encryptionService.decrypt(encrypted, wrongPassword);
        });
    }
    
    @Test
    void testDecryptWithInvalidData() {
        // When & Then
        assertThrows(WalletException.class, () -> {
            encryptionService.decrypt("invalid_encrypted_data", testPassword);
        });
        
        assertThrows(WalletException.class, () -> {
            encryptionService.decrypt(null, testPassword);
        });
        
        assertThrows(WalletException.class, () -> {
            encryptionService.decrypt("", testPassword);
        });
    }
    
    @Test
    void testVerifyPassword() {
        // Given
        String encrypted = encryptionService.encrypt(testData, testPassword);
        
        // When & Then
        assertTrue(encryptionService.verifyPassword(encrypted, testPassword));
        assertFalse(encryptionService.verifyPassword(encrypted, "wrong_password"));
        assertFalse(encryptionService.verifyPassword("invalid_data", testPassword));
    }
    
    @Test
    void testHashPassword() {
        // When
        String passwordHash = encryptionService.hashPassword(testPassword);
        
        // Then
        assertNotNull(passwordHash);
        assertNotEquals(testPassword, passwordHash);
        
        // 验证密码哈希
        assertTrue(encryptionService.verifyPasswordHash(testPassword, passwordHash));
        assertFalse(encryptionService.verifyPasswordHash("wrong_password", passwordHash));
    }
    
    @Test
    void testHashPasswordWithNullInput() {
        // When & Then
        assertThrows(WalletException.class, () -> {
            encryptionService.hashPassword(null);
        });
        
        assertThrows(WalletException.class, () -> {
            encryptionService.hashPassword("");
        });
    }
    
    @Test
    void testVerifyPasswordHashWithInvalidInput() {
        // When & Then
        assertFalse(encryptionService.verifyPasswordHash(testPassword, "invalid_hash"));
        assertFalse(encryptionService.verifyPasswordHash(testPassword, null));
        assertFalse(encryptionService.verifyPasswordHash(testPassword, ""));
    }
    
    @Test
    void testGenerateRandomKey() {
        // When
        String key1 = encryptionService.generateRandomKey();
        String key2 = encryptionService.generateRandomKey();
        
        // Then
        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2);
        
        // 验证密钥格式（Base64编码）
        assertDoesNotThrow(() -> {
            java.util.Base64.getDecoder().decode(key1);
            java.util.Base64.getDecoder().decode(key2);
        });
    }
    
    @Test
    void testEncryptDifferentDataProducesDifferentResults() {
        // Given
        String data1 = "data1";
        String data2 = "data2";
        
        // When
        String encrypted1 = encryptionService.encrypt(data1, testPassword);
        String encrypted2 = encryptionService.encrypt(data2, testPassword);
        
        // Then
        assertNotEquals(encrypted1, encrypted2);
        
        // 验证都能正确解密
        assertEquals(data1, encryptionService.decrypt(encrypted1, testPassword));
        assertEquals(data2, encryptionService.decrypt(encrypted2, testPassword));
    }
    
    @Test
    void testEncryptSameDataWithDifferentPasswords() {
        // When
        String encrypted1 = encryptionService.encrypt(testData, "password1");
        String encrypted2 = encryptionService.encrypt(testData, "password2");
        
        // Then
        assertNotEquals(encrypted1, encrypted2);
        
        // 验证密码对应关系
        assertEquals(testData, encryptionService.decrypt(encrypted1, "password1"));
        assertEquals(testData, encryptionService.decrypt(encrypted2, "password2"));
        
        // 验证错误密码无法解密
        assertThrows(WalletException.class, () -> {
            encryptionService.decrypt(encrypted1, "password2");
        });
        
        assertThrows(WalletException.class, () -> {
            encryptionService.decrypt(encrypted2, "password1");
        });
    }
}
