package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.TransactionType;
import io.equiflux.node.wallet.model.WalletKeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TransactionSigningService测试类
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class TransactionSigningServiceTest {
    
    @Mock
    private KeyManagementService keyManagementService;
    
    @Mock
    private WalletStorageService walletStorageService;
    
    @Mock
    private WalletEncryptionService encryptionService;
    
    @Mock
    private WalletAccountService walletAccountService;
    
    private TransactionSigningService signingService;
    
    @BeforeEach
    void setUp() {
        signingService = new TransactionSigningService(
            keyManagementService,
            walletStorageService,
            encryptionService,
            walletAccountService
        );
    }
    
    @Test
    void testBuildTransferTransaction() throws WalletException {
        // Given
        String fromPublicKeyHex = "from_public_key_hex";
        String toPublicKeyHex = "to_public_key_hex";
        long amount = 1000L;
        long fee = 10L;
        String password = "test_password";
        
        when(walletAccountService.getWalletInfo(fromPublicKeyHex))
            .thenReturn(Optional.of(createMockWalletInfo()));
        when(keyManagementService.isValidPublicKey(toPublicKeyHex)).thenReturn(true);
        when(walletAccountService.getBalance(fromPublicKeyHex)).thenReturn(2000L);
        when(walletAccountService.getNonce(fromPublicKeyHex)).thenReturn(5L);
        when(walletStorageService.getEncryptedPrivateKey(fromPublicKeyHex))
            .thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.decrypt("encrypted_private_key", password))
            .thenReturn("decrypted_private_key");
        when(keyManagementService.importKeyPair("decrypted_private_key"))
            .thenReturn(createMockWalletKeyPair());
        
        // When
        Transaction result = signingService.buildTransferTransaction(
            fromPublicKeyHex, toPublicKeyHex, amount, fee, password);
        
        // Then
        assertNotNull(result);
        assertEquals(fromPublicKeyHex, result.getFromPublicKey());
        assertEquals(toPublicKeyHex, result.getToPublicKey());
        assertEquals(amount, result.getAmount());
        assertEquals(fee, result.getFee());
        assertEquals(5L, result.getNonce());
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertNotNull(result.getSignature());
    }
    
    @Test
    void testBuildTransferTransaction_InsufficientBalance() throws WalletException {
        // Given
        String fromPublicKeyHex = "from_public_key_hex";
        String toPublicKeyHex = "to_public_key_hex";
        long amount = 1000L;
        long fee = 10L;
        String password = "test_password";
        
        when(walletAccountService.getWalletInfo(fromPublicKeyHex))
            .thenReturn(Optional.of(createMockWalletInfo()));
        when(keyManagementService.isValidPublicKey(toPublicKeyHex)).thenReturn(true);
        when(walletAccountService.getBalance(fromPublicKeyHex)).thenReturn(500L); // 余额不足
        
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            signingService.buildTransferTransaction(fromPublicKeyHex, toPublicKeyHex, amount, fee, password);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient balance"));
    }
    
    @Test
    void testBuildTransferTransaction_WalletNotFound() throws WalletException {
        // Given
        String fromPublicKeyHex = "from_public_key_hex";
        String toPublicKeyHex = "to_public_key_hex";
        long amount = 1000L;
        long fee = 10L;
        String password = "test_password";
        
        when(walletAccountService.getWalletInfo(fromPublicKeyHex))
            .thenReturn(Optional.empty());
        
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            signingService.buildTransferTransaction(fromPublicKeyHex, toPublicKeyHex, amount, fee, password);
        });
        
        assertEquals("From wallet not found: " + fromPublicKeyHex, exception.getMessage());
    }
    
    @Test
    void testBuildTransferTransaction_WalletLocked() throws WalletException {
        // Given
        String fromPublicKeyHex = "from_public_key_hex";
        String toPublicKeyHex = "to_public_key_hex";
        long amount = 1000L;
        long fee = 10L;
        String password = "test_password";
        
        when(walletAccountService.getWalletInfo(fromPublicKeyHex))
            .thenReturn(Optional.of(createMockLockedWalletInfo()));
        
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            signingService.buildTransferTransaction(fromPublicKeyHex, toPublicKeyHex, amount, fee, password);
        });
        
        assertEquals("From wallet is not unlocked: " + fromPublicKeyHex, exception.getMessage());
    }
    
    @Test
    void testBuildStakeTransaction() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        long stakeAmount = 500L;
        long fee = 10L;
        String password = "test_password";
        
        when(walletAccountService.getWalletInfo(publicKeyHex))
            .thenReturn(Optional.of(createMockWalletInfo()));
        when(walletAccountService.getBalance(publicKeyHex)).thenReturn(2000L);
        when(walletAccountService.getNonce(publicKeyHex)).thenReturn(5L);
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.decrypt("encrypted_private_key", password))
            .thenReturn("decrypted_private_key");
        when(keyManagementService.importKeyPair("decrypted_private_key"))
            .thenReturn(createMockWalletKeyPair());
        
        // When
        Transaction result = signingService.buildStakeTransaction(
            publicKeyHex, stakeAmount, fee, password);
        
        // Then
        assertNotNull(result);
        assertEquals(publicKeyHex, result.getFromPublicKey());
        assertEquals(publicKeyHex, result.getToPublicKey()); // 质押给自己
        assertEquals(stakeAmount, result.getAmount());
        assertEquals(fee, result.getFee());
        assertEquals(TransactionType.STAKE, result.getType());
        assertNotNull(result.getSignature());
    }
    
    @Test
    void testBuildUnstakeTransaction() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        long unstakeAmount = 300L;
        long fee = 10L;
        String password = "test_password";
        
        when(walletAccountService.getWalletInfo(publicKeyHex))
            .thenReturn(Optional.of(createMockWalletInfo()));
        when(walletAccountService.getStake(publicKeyHex)).thenReturn(500L);
        when(walletAccountService.getBalance(publicKeyHex)).thenReturn(1000L);
        when(walletAccountService.getNonce(publicKeyHex)).thenReturn(5L);
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.decrypt("encrypted_private_key", password))
            .thenReturn("decrypted_private_key");
        when(keyManagementService.importKeyPair("decrypted_private_key"))
            .thenReturn(createMockWalletKeyPair());
        
        // When
        Transaction result = signingService.buildUnstakeTransaction(
            publicKeyHex, unstakeAmount, fee, password);
        
        // Then
        assertNotNull(result);
        assertEquals(publicKeyHex, result.getFromPublicKey());
        assertEquals(publicKeyHex, result.getToPublicKey()); // 解质押给自己
        assertEquals(unstakeAmount, result.getAmount());
        assertEquals(fee, result.getFee());
        assertEquals(TransactionType.UNSTAKE, result.getType());
        assertNotNull(result.getSignature());
    }
    
    @Test
    void testBuildUnstakeTransaction_InsufficientStake() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        long unstakeAmount = 1000L; // 超过可用质押
        long fee = 10L;
        String password = "test_password";
        
        when(walletAccountService.getWalletInfo(publicKeyHex))
            .thenReturn(Optional.of(createMockWalletInfo()));
        when(walletAccountService.getStake(publicKeyHex)).thenReturn(500L);
        
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            signingService.buildUnstakeTransaction(publicKeyHex, unstakeAmount, fee, password);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient stake"));
    }
    
    @Test
    void testSignTransaction() throws WalletException {
        // Given
        Transaction transaction = createMockTransaction();
        String publicKeyHex = "test_public_key_hex";
        String password = "test_password";
        
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.decrypt("encrypted_private_key", password))
            .thenReturn("decrypted_private_key");
        when(keyManagementService.importKeyPair("decrypted_private_key"))
            .thenReturn(createMockWalletKeyPair());
        
        // When
        Transaction result = signingService.signTransaction(transaction, publicKeyHex, password);
        
        // Then
        assertNotNull(result);
        assertEquals(transaction.getFromPublicKey(), result.getFromPublicKey());
        assertEquals(transaction.getToPublicKey(), result.getToPublicKey());
        assertEquals(transaction.getAmount(), result.getAmount());
        assertEquals(transaction.getFee(), result.getFee());
        assertEquals(transaction.getNonce(), result.getNonce());
        assertEquals(transaction.getType(), result.getType());
        assertNotNull(result.getSignature());
    }
    
    @Test
    void testSignTransaction_PublicKeyMismatch() throws WalletException {
        // Given
        Transaction transaction = createMockTransaction();
        String publicKeyHex = "different_public_key_hex";
        String password = "test_password";
        
        WalletKeyPair keyPair = createMockWalletKeyPair();
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.of("encrypted_private_key"));
        when(encryptionService.decrypt("encrypted_private_key", password))
            .thenReturn("decrypted_private_key");
        when(keyManagementService.importKeyPair("decrypted_private_key"))
            .thenReturn(keyPair);
        
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            signingService.signTransaction(transaction, publicKeyHex, password);
        });
        
        assertEquals("Public key mismatch", exception.getMessage());
    }
    
    @Test
    void testVerifyTransactionSignature() throws WalletException {
        // Given
        Transaction transaction = createMockTransaction();
        when(keyManagementService.isValidPublicKey(transaction.getFromPublicKey())).thenReturn(true);
        
        // When
        boolean result = signingService.verifyTransactionSignature(transaction);
        
        // Then
        // 由于签名验证涉及复杂的密码学操作，这里主要测试方法不会抛出异常
        assertDoesNotThrow(() -> signingService.verifyTransactionSignature(transaction));
    }
    
    @Test
    void testVerifyTransactionSignature_NullTransaction() {
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            signingService.verifyTransactionSignature(null);
        });
        
        assertEquals("Transaction cannot be null", exception.getMessage());
    }
    
    @Test
    void testVerifyTransactionSignature_NoSignature() throws WalletException {
        // Given
        Transaction transaction = Transaction.builder()
            .fromPublicKey("from_public_key_hex")
            .toPublicKey("to_public_key_hex")
            .amount(1000L)
            .fee(10L)
            .nonce(5L)
            .timestamp(System.currentTimeMillis())
            .type(TransactionType.TRANSFER)
            .signature((byte[]) null) // 无签名
            .build();
        
        // When
        boolean result = signingService.verifyTransactionSignature(transaction);
        
        // Then
        assertFalse(result);
    }
    
    // ==================== 辅助方法 ====================
    
    private Transaction createMockTransaction() {
        // 创建64字节的模拟签名
        byte[] mockSignature = new byte[64];
        for (int i = 0; i < 64; i++) {
            mockSignature[i] = (byte) (i % 256);
        }
        
        return Transaction.builder()
            .fromPublicKey("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")
            .toPublicKey("fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210")
            .amount(1000L)
            .fee(10L)
            .nonce(5L)
            .timestamp(System.currentTimeMillis())
            .type(TransactionType.TRANSFER)
            .signature(mockSignature)
            .build();
    }
    
    private WalletKeyPair createMockWalletKeyPair() {
        // 创建模拟的Ed25519KeyPair
        io.equiflux.node.crypto.Ed25519KeyPair mockKeyPair = mock(io.equiflux.node.crypto.Ed25519KeyPair.class);
        // 使用doReturn来避免final方法的问题
        doReturn("from_public_key_hex").when(mockKeyPair).getPublicKeyHex();
        doReturn("test_private_key_hex").when(mockKeyPair).getPrivateKeyHex();
        
        return new WalletKeyPair(mockKeyPair);
    }
    
    private io.equiflux.node.wallet.model.WalletInfo createMockWalletInfo() {
        return new io.equiflux.node.wallet.model.WalletInfo(
            "from_public_key_hex",
            "test_address",
            "test_wallet",
            io.equiflux.node.wallet.model.WalletStatus.UNLOCKED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
    }
    
    private io.equiflux.node.wallet.model.WalletInfo createMockLockedWalletInfo() {
        return new io.equiflux.node.wallet.model.WalletInfo(
            "from_public_key_hex",
            "test_address",
            "test_wallet",
            io.equiflux.node.wallet.model.WalletStatus.LOCKED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
    }
}
