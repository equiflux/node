package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.storage.StateStorageService;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.wallet.model.WalletInfo;
import io.equiflux.node.wallet.model.WalletStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 钱包账户服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class WalletAccountServiceTest {
    
    @Mock
    private KeyManagementService keyManagementService;
    
    @Mock
    private WalletStorageService walletStorageService;
    
    @Mock
    private WalletEncryptionService encryptionService;
    
    @Mock
    private StateStorageService stateStorageService;
    
    @InjectMocks
    private WalletAccountService walletAccountService;
    
    private String testPublicKeyHex;
    private String testPrivateKeyHex;
    private String testPassword;
    private String testAddress;
    
    @BeforeEach
    void setUp() {
        testPublicKeyHex = "test_public_key_hex";
        testPrivateKeyHex = "test_private_key_hex";
        testPassword = "test_password";
        testAddress = "EQtest_address";
    }
    
    @Test
    void testCreateWallet() {
        // Given
        when(keyManagementService.generateKeyPair()).thenReturn(createMockWalletKeyPair());
        when(walletStorageService.walletExists(testPublicKeyHex)).thenReturn(false);
        when(encryptionService.encrypt(testPrivateKeyHex, testPassword)).thenReturn("encrypted_private_key");
        
        // When
        WalletInfo walletInfo = walletAccountService.createWallet(testPassword);
        
        // Then
        assertNotNull(walletInfo);
        assertEquals(testPublicKeyHex, walletInfo.getPublicKeyHex());
        assertEquals(testAddress, walletInfo.getAddress());
        assertEquals(WalletStatus.CREATED, walletInfo.getStatus());
        assertTrue(walletInfo.isEncrypted());
        
        verify(walletStorageService).storeWalletInfo(any(WalletInfo.class));
        verify(walletStorageService).storeEncryptedPrivateKey(eq(testPublicKeyHex), eq("encrypted_private_key"));
    }
    
    @Test
    void testCreateWalletWithEmptyPassword() {
        // When & Then
        assertThrows(WalletException.class, () -> {
            walletAccountService.createWallet("");
        });
        
        assertThrows(WalletException.class, () -> {
            walletAccountService.createWallet(null);
        });
    }
    
    @Test
    void testCreateWalletFromPrivateKey() {
        // Given
        when(keyManagementService.isValidPrivateKey(testPrivateKeyHex)).thenReturn(true);
        when(keyManagementService.importKeyPair(testPrivateKeyHex)).thenReturn(createMockWalletKeyPair());
        when(walletStorageService.walletExists(testPublicKeyHex)).thenReturn(false);
        when(encryptionService.encrypt(testPrivateKeyHex, testPassword)).thenReturn("encrypted_private_key");
        
        // When
        WalletInfo walletInfo = walletAccountService.createWalletFromPrivateKey(testPrivateKeyHex, testPassword);
        
        // Then
        assertNotNull(walletInfo);
        assertEquals(testPublicKeyHex, walletInfo.getPublicKeyHex());
        assertEquals(testAddress, walletInfo.getAddress());
        assertEquals(WalletStatus.CREATED, walletInfo.getStatus());
        
        verify(keyManagementService).isValidPrivateKey(testPrivateKeyHex);
        verify(keyManagementService).importKeyPair(testPrivateKeyHex);
        verify(walletStorageService).storeWalletInfo(any(WalletInfo.class));
        verify(walletStorageService).storeEncryptedPrivateKey(eq(testPublicKeyHex), eq("encrypted_private_key"));
    }
    
    @Test
    void testCreateWalletFromPrivateKeyWithInvalidKey() {
        // Given
        when(keyManagementService.isValidPrivateKey(testPrivateKeyHex)).thenReturn(false);
        
        // When & Then
        assertThrows(WalletException.class, () -> {
            walletAccountService.createWalletFromPrivateKey(testPrivateKeyHex, testPassword);
        });
    }
    
    @Test
    void testUnlockWallet() {
        // Given
        WalletInfo walletInfo = createMockWalletInfo(WalletStatus.LOCKED);
        when(walletStorageService.getWalletInfo(testPublicKeyHex)).thenReturn(Optional.of(walletInfo));
        when(walletStorageService.getEncryptedPrivateKey(testPublicKeyHex)).thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.verifyPassword("encrypted_private_key", testPassword)).thenReturn(true);
        
        // When
        boolean result = walletAccountService.unlockWallet(testPublicKeyHex, testPassword);
        
        // Then
        assertTrue(result);
        verify(walletStorageService).updateWalletInfo(any(WalletInfo.class));
    }
    
    @Test
    void testUnlockWalletWithInvalidPassword() {
        // Given
        WalletInfo walletInfo = createMockWalletInfo(WalletStatus.LOCKED);
        when(walletStorageService.getWalletInfo(testPublicKeyHex)).thenReturn(Optional.of(walletInfo));
        when(walletStorageService.getEncryptedPrivateKey(testPublicKeyHex)).thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.verifyPassword("encrypted_private_key", testPassword)).thenReturn(false);
        
        // When
        boolean result = walletAccountService.unlockWallet(testPublicKeyHex, testPassword);
        
        // Then
        assertFalse(result);
        verify(walletStorageService, never()).updateWalletInfo(any(WalletInfo.class));
    }
    
    @Test
    void testUnlockWalletNotFound() {
        // Given
        when(walletStorageService.getWalletInfo(testPublicKeyHex)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(WalletException.class, () -> {
            walletAccountService.unlockWallet(testPublicKeyHex, testPassword);
        });
    }
    
    @Test
    void testLockWallet() {
        // Given
        WalletInfo walletInfo = createMockWalletInfo(WalletStatus.UNLOCKED);
        when(walletStorageService.getWalletInfo(testPublicKeyHex)).thenReturn(Optional.of(walletInfo));
        
        // When
        walletAccountService.lockWallet(testPublicKeyHex);
        
        // Then
        verify(walletStorageService).updateWalletInfo(any(WalletInfo.class));
    }
    
    @Test
    void testGetBalance() {
        // Given
        AccountState accountState = createMockAccountState(testPublicKeyHex, 1000L, 1L, 500L);
        when(stateStorageService.getAccountStateByPublicKeyHex(testPublicKeyHex)).thenReturn(accountState);
        
        // When
        long balance = walletAccountService.getBalance(testPublicKeyHex);
        
        // Then
        assertEquals(1000L, balance);
    }
    
    @Test
    void testGetBalanceNotFound() {
        // Given
        when(stateStorageService.getAccountStateByPublicKeyHex(testPublicKeyHex)).thenReturn(null);
        
        // When
        long balance = walletAccountService.getBalance(testPublicKeyHex);
        
        // Then
        assertEquals(0L, balance);
    }
    
    @Test
    void testGetNonce() {
        // Given
        AccountState accountState = createMockAccountState(testPublicKeyHex, 1000L, 5L, 500L);
        when(stateStorageService.getAccountStateByPublicKeyHex(testPublicKeyHex)).thenReturn(accountState);
        
        // When
        long nonce = walletAccountService.getNonce(testPublicKeyHex);
        
        // Then
        assertEquals(5L, nonce);
    }
    
    @Test
    void testGetStake() {
        // Given
        AccountState accountState = createMockAccountState(testPublicKeyHex, 1000L, 1L, 500L);
        when(stateStorageService.getAccountStateByPublicKeyHex(testPublicKeyHex)).thenReturn(accountState);
        
        // When
        long stake = walletAccountService.getStake(testPublicKeyHex);
        
        // Then
        assertEquals(500L, stake);
    }
    
    @Test
    void testDeleteWallet() {
        // Given
        when(walletStorageService.getEncryptedPrivateKey(testPublicKeyHex)).thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.verifyPassword("encrypted_private_key", testPassword)).thenReturn(true);
        
        // When
        boolean result = walletAccountService.deleteWallet(testPublicKeyHex, testPassword);
        
        // Then
        assertTrue(result);
        verify(walletStorageService).deleteWalletInfo(testPublicKeyHex);
        verify(walletStorageService).deleteEncryptedPrivateKey(testPublicKeyHex);
    }
    
    @Test
    void testDeleteWalletWithInvalidPassword() {
        // Given
        when(walletStorageService.getEncryptedPrivateKey(testPublicKeyHex)).thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.verifyPassword("encrypted_private_key", testPassword)).thenReturn(false);
        
        // When
        boolean result = walletAccountService.deleteWallet(testPublicKeyHex, testPassword);
        
        // Then
        assertFalse(result);
        verify(walletStorageService, never()).deleteWalletInfo(testPublicKeyHex);
        verify(walletStorageService, never()).deleteEncryptedPrivateKey(testPublicKeyHex);
    }
    
    // Helper methods
    private AccountState createMockAccountState(String publicKeyHex, long balance, long nonce, long stakeAmount) {
        try {
            // 创建一个模拟的PublicKey
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("Ed25519");
            java.security.KeyPair keyPair = keyGen.generateKeyPair();
            return new AccountState(keyPair.getPublic(), balance, nonce, stakeAmount, System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock account state", e);
        }
    }
    
    private io.equiflux.node.wallet.model.WalletKeyPair createMockWalletKeyPair() {
        return new io.equiflux.node.wallet.model.WalletKeyPair(
            mock(io.equiflux.node.crypto.Ed25519KeyPair.class)
        ) {
            @Override
            public String getPublicKeyHex() {
                return testPublicKeyHex;
            }
            
            @Override
            public String getPrivateKeyHex() {
                return testPrivateKeyHex;
            }
        };
    }
    
    private WalletInfo createMockWalletInfo(WalletStatus status) {
        return new WalletInfo(
            testPublicKeyHex,
            testAddress,
            "Test Wallet",
            status,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
    }
}
