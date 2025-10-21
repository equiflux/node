package io.equiflux.node.wallet.service;

import io.equiflux.node.crypto.Ed25519KeyPair;
import io.equiflux.node.exception.WalletException;
import io.equiflux.node.wallet.model.WalletKeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 密钥管理服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class KeyManagementServiceTest {
    
    @InjectMocks
    private KeyManagementService keyManagementService;
    
    @Test
    void testGenerateKeyPair() {
        // When
        WalletKeyPair keyPair = keyManagementService.generateKeyPair();
        
        // Then
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublicKeyHex());
        assertNotNull(keyPair.getPrivateKeyHex());
        assertNotNull(keyPair.getCreatedAt());
        
        // 验证公钥格式
        assertTrue(keyManagementService.isValidPublicKey(keyPair.getPublicKeyHex()));
        
        // 验证私钥格式
        assertTrue(keyManagementService.isValidPrivateKey(keyPair.getPrivateKeyHex()));
    }
    
    @Test
    void testImportKeyPair() {
        // Given
        WalletKeyPair originalKeyPair = keyManagementService.generateKeyPair();
        String privateKeyHex = originalKeyPair.getPrivateKeyHex();
        String publicKeyHex = originalKeyPair.getPublicKeyHex();

        // When - 使用完整密钥对导入
        WalletKeyPair importedKeyPair = keyManagementService.importKeyPair(privateKeyHex, publicKeyHex);

        // Then
        assertNotNull(importedKeyPair);
        assertEquals(originalKeyPair.getPublicKeyHex(), importedKeyPair.getPublicKeyHex());
        assertEquals(originalKeyPair.getPrivateKeyHex(), importedKeyPair.getPrivateKeyHex());
    }
    
    @Test
    void testImportKeyPairWithInvalidFormat() {
        // Given
        String invalidPrivateKeyHex = "invalid_hex_string";
        
        // When & Then
        assertThrows(WalletException.class, () -> {
            keyManagementService.importKeyPair(invalidPrivateKeyHex);
        });
    }
    
    @Test
    void testValidateKeyPair() {
        // Given
        WalletKeyPair keyPair = keyManagementService.generateKeyPair();
        String publicKeyHex = keyPair.getPublicKeyHex();
        String privateKeyHex = keyPair.getPrivateKeyHex();

        // When
        boolean isValid = keyManagementService.validateKeyPair(publicKeyHex, privateKeyHex);

        // Then
        assertTrue(isValid);
    }
    
    @Test
    void testValidateKeyPairWithInvalidPair() {
        // Given
        WalletKeyPair keyPair1 = keyManagementService.generateKeyPair();
        WalletKeyPair keyPair2 = keyManagementService.generateKeyPair();
        
        // When
        boolean isValid = keyManagementService.validateKeyPair(
            keyPair1.getPublicKeyHex(), 
            keyPair2.getPrivateKeyHex()
        );
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void testIsValidPublicKey() {
        // Given
        WalletKeyPair keyPair = keyManagementService.generateKeyPair();
        String validPublicKeyHex = keyPair.getPublicKeyHex();
        String invalidPublicKeyHex = "invalid_hex";
        
        // When & Then
        assertTrue(keyManagementService.isValidPublicKey(validPublicKeyHex));
        assertFalse(keyManagementService.isValidPublicKey(invalidPublicKeyHex));
        assertFalse(keyManagementService.isValidPublicKey(null));
        assertFalse(keyManagementService.isValidPublicKey(""));
    }
    
    @Test
    void testIsValidPrivateKey() {
        // Given
        WalletKeyPair keyPair = keyManagementService.generateKeyPair();
        String validPrivateKeyHex = keyPair.getPrivateKeyHex();
        String invalidPrivateKeyHex = "invalid_hex";
        
        // When & Then
        assertTrue(keyManagementService.isValidPrivateKey(validPrivateKeyHex));
        assertFalse(keyManagementService.isValidPrivateKey(invalidPrivateKeyHex));
        assertFalse(keyManagementService.isValidPrivateKey(null));
        assertFalse(keyManagementService.isValidPrivateKey(""));
    }
    
    @Test
    void testValidateKeyPairWithNullInputs() {
        // When & Then
        assertThrows(WalletException.class, () -> {
            keyManagementService.validateKeyPair(null, "private_key");
        });
        
        assertThrows(WalletException.class, () -> {
            keyManagementService.validateKeyPair("public_key", null);
        });
        
        assertThrows(WalletException.class, () -> {
            keyManagementService.validateKeyPair("", "private_key");
        });
        
        assertThrows(WalletException.class, () -> {
            keyManagementService.validateKeyPair("public_key", "");
        });
    }
}
