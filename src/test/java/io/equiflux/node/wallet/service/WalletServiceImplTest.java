package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.TransactionType;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.wallet.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WalletServiceImpl测试类
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {
    
    @Mock
    private KeyManagementService keyManagementService;
    
    @Mock
    private WalletAccountService walletAccountService;
    
    @Mock
    private TransactionSigningService transactionSigningService;
    
    @Mock
    private TransactionBroadcastService transactionBroadcastService;
    
    @Mock
    private WalletEncryptionService encryptionService;
    
    @Mock
    private WalletStorageService walletStorageService;
    
    private WalletServiceImpl walletService;
    
    @BeforeEach
    void setUp() {
        walletService = new WalletServiceImpl(
            keyManagementService,
            walletAccountService,
            transactionSigningService,
            transactionBroadcastService,
            encryptionService,
            walletStorageService
        );
    }
    
    @Test
    void testGenerateKeyPair() throws WalletException {
        // Given
        WalletKeyPair expectedKeyPair = createMockWalletKeyPair();
        when(keyManagementService.generateKeyPair()).thenReturn(expectedKeyPair);
        
        // When
        WalletKeyPair result = walletService.generateKeyPair();
        
        // Then
        assertNotNull(result);
        assertEquals(expectedKeyPair, result);
        verify(keyManagementService).generateKeyPair();
    }
    
    @Test
    void testImportKeyPair() throws WalletException {
        // Given
        String privateKeyHex = "test_private_key_hex";
        WalletKeyPair expectedKeyPair = createMockWalletKeyPair();
        when(keyManagementService.importKeyPair(privateKeyHex)).thenReturn(expectedKeyPair);
        
        // When
        WalletKeyPair result = walletService.importKeyPair(privateKeyHex);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedKeyPair, result);
        verify(keyManagementService).importKeyPair(privateKeyHex);
    }
    
    @Test
    void testExportPrivateKey() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        String password = "test_password";
        String encryptedPrivateKey = "encrypted_private_key";
        String decryptedPrivateKey = "decrypted_private_key";
        
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.of(encryptedPrivateKey));
        when(encryptionService.verifyPassword(encryptedPrivateKey, password)).thenReturn(true);
        when(encryptionService.decrypt(encryptedPrivateKey, password)).thenReturn(decryptedPrivateKey);
        
        // When
        String result = walletService.exportPrivateKey(publicKeyHex, password);
        
        // Then
        assertEquals(decryptedPrivateKey, result);
        verify(walletStorageService).getEncryptedPrivateKey(publicKeyHex);
        verify(encryptionService).verifyPassword(encryptedPrivateKey, password);
        verify(encryptionService).decrypt(encryptedPrivateKey, password);
    }
    
    @Test
    void testExportPrivateKey_WalletNotFound() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        String password = "test_password";
        
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.empty());
        
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            walletService.exportPrivateKey(publicKeyHex, password);
        });
        
        assertEquals("Wallet not found: " + publicKeyHex, exception.getMessage());
    }
    
    @Test
    void testExportPrivateKey_InvalidPassword() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        String password = "test_password";
        String encryptedPrivateKey = "encrypted_private_key";
        
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.of(encryptedPrivateKey));
        when(encryptionService.verifyPassword(encryptedPrivateKey, password)).thenReturn(false);
        
        // When & Then
        WalletException exception = assertThrows(WalletException.class, () -> {
            walletService.exportPrivateKey(publicKeyHex, password);
        });
        
        assertEquals("Invalid password", exception.getMessage());
    }
    
    @Test
    void testValidateKeyPair() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        String privateKeyHex = "test_private_key_hex";
        when(keyManagementService.validateKeyPair(publicKeyHex, privateKeyHex)).thenReturn(true);
        
        // When
        boolean result = walletService.validateKeyPair(publicKeyHex, privateKeyHex);
        
        // Then
        assertTrue(result);
        verify(keyManagementService).validateKeyPair(publicKeyHex, privateKeyHex);
    }
    
    @Test
    void testCreateWallet() throws WalletException {
        // Given
        String password = "test_password";
        WalletInfo expectedWallet = createMockWalletInfo();
        when(walletAccountService.createWallet(password)).thenReturn(expectedWallet);
        
        // When
        WalletInfo result = walletService.createWallet(password);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedWallet, result);
        verify(walletAccountService).createWallet(password);
    }
    
    @Test
    void testCreateWalletFromPrivateKey() throws WalletException {
        // Given
        String privateKeyHex = "test_private_key_hex";
        String password = "test_password";
        WalletInfo expectedWallet = createMockWalletInfo();
        when(walletAccountService.createWalletFromPrivateKey(privateKeyHex, password))
            .thenReturn(expectedWallet);
        
        // When
        WalletInfo result = walletService.createWalletFromPrivateKey(privateKeyHex, password);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedWallet, result);
        verify(walletAccountService).createWalletFromPrivateKey(privateKeyHex, password);
    }
    
    @Test
    void testUnlockWallet() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        String password = "test_password";
        when(walletAccountService.unlockWallet(publicKeyHex, password)).thenReturn(true);
        
        // When
        boolean result = walletService.unlockWallet(publicKeyHex, password);
        
        // Then
        assertTrue(result);
        verify(walletAccountService).unlockWallet(publicKeyHex, password);
    }
    
    @Test
    void testLockWallet() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        
        // When
        walletService.lockWallet(publicKeyHex);
        
        // Then
        verify(walletAccountService).lockWallet(publicKeyHex);
    }
    
    @Test
    void testGetWalletInfo() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        WalletInfo expectedWallet = createMockWalletInfo();
        when(walletAccountService.getWalletInfo(publicKeyHex))
            .thenReturn(Optional.of(expectedWallet));
        
        // When
        Optional<WalletInfo> result = walletService.getWalletInfo(publicKeyHex);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedWallet, result.get());
        verify(walletAccountService).getWalletInfo(publicKeyHex);
    }
    
    @Test
    void testGetAllWallets() throws WalletException {
        // Given
        List<WalletInfo> expectedWallets = List.of(createMockWalletInfo());
        when(walletAccountService.getAllWallets()).thenReturn(expectedWallets);
        
        // When
        List<WalletInfo> result = walletService.getAllWallets();
        
        // Then
        assertNotNull(result);
        assertEquals(expectedWallets, result);
        verify(walletAccountService).getAllWallets();
    }
    
    @Test
    void testDeleteWallet() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        String password = "test_password";
        when(walletAccountService.deleteWallet(publicKeyHex, password)).thenReturn(true);
        
        // When
        boolean result = walletService.deleteWallet(publicKeyHex, password);
        
        // Then
        assertTrue(result);
        verify(walletAccountService).deleteWallet(publicKeyHex, password);
    }
    
    @Test
    void testGetBalance() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        long expectedBalance = 1000L;
        when(walletAccountService.getBalance(publicKeyHex)).thenReturn(expectedBalance);
        
        // When
        long result = walletService.getBalance(publicKeyHex);
        
        // Then
        assertEquals(expectedBalance, result);
        verify(walletAccountService).getBalance(publicKeyHex);
    }
    
    @Test
    void testGetAccountState() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        AccountState expectedState = createMockAccountState();
        when(walletAccountService.getAccountState(publicKeyHex))
            .thenReturn(Optional.of(expectedState));
        
        // When
        Optional<AccountState> result = walletService.getAccountState(publicKeyHex);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedState, result.get());
        verify(walletAccountService).getAccountState(publicKeyHex);
    }
    
    @Test
    void testGetNonce() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        long expectedNonce = 5L;
        when(walletAccountService.getNonce(publicKeyHex)).thenReturn(expectedNonce);
        
        // When
        long result = walletService.getNonce(publicKeyHex);
        
        // Then
        assertEquals(expectedNonce, result);
        verify(walletAccountService).getNonce(publicKeyHex);
    }
    
    @Test
    void testGetStake() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        long expectedStake = 500L;
        when(walletAccountService.getStake(publicKeyHex)).thenReturn(expectedStake);
        
        // When
        long result = walletService.getStake(publicKeyHex);
        
        // Then
        assertEquals(expectedStake, result);
        verify(walletAccountService).getStake(publicKeyHex);
    }
    
    @Test
    void testBuildTransferTransaction() throws WalletException {
        // Given
        String fromPublicKeyHex = "from_public_key_hex";
        String toPublicKeyHex = "to_public_key_hex";
        long amount = 1000L;
        long fee = 10L;
        String password = "test_password";
        Transaction expectedTransaction = createMockTransaction();
        
        when(transactionSigningService.buildTransferTransaction(
            fromPublicKeyHex, toPublicKeyHex, amount, fee, password))
            .thenReturn(expectedTransaction);
        
        // When
        Transaction result = walletService.buildTransferTransaction(
            fromPublicKeyHex, toPublicKeyHex, amount, fee, password);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedTransaction, result);
        verify(transactionSigningService).buildTransferTransaction(
            fromPublicKeyHex, toPublicKeyHex, amount, fee, password);
    }
    
    @Test
    void testBuildStakeTransaction() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        long stakeAmount = 500L;
        long fee = 10L;
        String password = "test_password";
        Transaction expectedTransaction = createMockTransaction();
        
        when(transactionSigningService.buildStakeTransaction(
            publicKeyHex, stakeAmount, fee, password))
            .thenReturn(expectedTransaction);
        
        // When
        Transaction result = walletService.buildStakeTransaction(
            publicKeyHex, stakeAmount, fee, password);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedTransaction, result);
        verify(transactionSigningService).buildStakeTransaction(
            publicKeyHex, stakeAmount, fee, password);
    }
    
    @Test
    void testBuildUnstakeTransaction() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        long unstakeAmount = 300L;
        long fee = 10L;
        String password = "test_password";
        Transaction expectedTransaction = createMockTransaction();
        
        when(transactionSigningService.buildUnstakeTransaction(
            publicKeyHex, unstakeAmount, fee, password))
            .thenReturn(expectedTransaction);
        
        // When
        Transaction result = walletService.buildUnstakeTransaction(
            publicKeyHex, unstakeAmount, fee, password);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedTransaction, result);
        verify(transactionSigningService).buildUnstakeTransaction(
            publicKeyHex, unstakeAmount, fee, password);
    }
    
    @Test
    void testSignTransaction() throws WalletException {
        // Given
        Transaction transaction = createMockTransaction();
        String publicKeyHex = "test_public_key_hex";
        String password = "test_password";
        Transaction expectedSignedTransaction = createMockTransaction();
        
        when(transactionSigningService.signTransaction(transaction, publicKeyHex, password))
            .thenReturn(expectedSignedTransaction);
        
        // When
        Transaction result = walletService.signTransaction(transaction, publicKeyHex, password);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedSignedTransaction, result);
        verify(transactionSigningService).signTransaction(transaction, publicKeyHex, password);
    }
    
    @Test
    void testVerifyTransactionSignature() throws WalletException {
        // Given
        Transaction transaction = createMockTransaction();
        when(transactionSigningService.verifyTransactionSignature(transaction)).thenReturn(true);
        
        // When
        boolean result = walletService.verifyTransactionSignature(transaction);
        
        // Then
        assertTrue(result);
        verify(transactionSigningService).verifyTransactionSignature(transaction);
    }
    
    @Test
    void testBroadcastTransaction() throws WalletException {
        // Given
        Transaction transaction = createMockTransaction();
        String expectedHash = "transaction_hash";
        when(transactionBroadcastService.broadcastTransaction(transaction)).thenReturn(expectedHash);
        
        // When
        String result = walletService.broadcastTransaction(transaction);
        
        // Then
        assertEquals(expectedHash, result);
        verify(transactionBroadcastService).broadcastTransaction(transaction);
    }
    
    @Test
    void testGetTransactionStatus() throws WalletException {
        // Given
        String transactionHash = "transaction_hash";
        TransactionStatus expectedStatus = createMockTransactionStatus();
        when(transactionBroadcastService.getTransactionStatus(transactionHash))
            .thenReturn(Optional.of(expectedStatus));
        
        // When
        Optional<TransactionStatus> result = walletService.getTransactionStatus(transactionHash);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedStatus, result.get());
        verify(transactionBroadcastService).getTransactionStatus(transactionHash);
    }
    
    @Test
    void testGetTransactionHistory() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        int limit = 10;
        int offset = 0;
        List<TransactionInfo> expectedHistory = List.of(createMockTransactionInfo());
        when(transactionBroadcastService.getTransactionHistory(publicKeyHex, limit, offset))
            .thenReturn(expectedHistory);
        
        // When
        List<TransactionInfo> result = walletService.getTransactionHistory(publicKeyHex, limit, offset);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedHistory, result);
        verify(transactionBroadcastService).getTransactionHistory(publicKeyHex, limit, offset);
    }
    
    @Test
    void testChangePassword() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        String oldPassword = "old_password";
        String newPassword = "new_password";
        String encryptedPrivateKey = "encrypted_private_key";
        String decryptedPrivateKey = "decrypted_private_key";
        String newEncryptedPrivateKey = "new_encrypted_private_key";
        
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.of(encryptedPrivateKey));
        when(encryptionService.verifyPassword(encryptedPrivateKey, oldPassword)).thenReturn(true);
        when(encryptionService.decrypt(encryptedPrivateKey, oldPassword)).thenReturn(decryptedPrivateKey);
        when(encryptionService.encrypt(decryptedPrivateKey, newPassword)).thenReturn(newEncryptedPrivateKey);
        
        // When
        boolean result = walletService.changePassword(publicKeyHex, oldPassword, newPassword);
        
        // Then
        assertTrue(result);
        verify(walletStorageService).getEncryptedPrivateKey(publicKeyHex);
        verify(encryptionService).verifyPassword(encryptedPrivateKey, oldPassword);
        verify(encryptionService).decrypt(encryptedPrivateKey, oldPassword);
        verify(encryptionService).encrypt(decryptedPrivateKey, newPassword);
        verify(walletStorageService).storeEncryptedPrivateKey(publicKeyHex, newEncryptedPrivateKey);
    }
    
    @Test
    void testBackupWallet() throws WalletException {
        // Given
        String publicKeyHex = "test_public_key_hex";
        String password = "test_password";
        WalletInfo walletInfo = createMockWalletInfo();
        String encryptedPrivateKey = "encrypted_private_key";
        
        when(walletAccountService.getWalletInfo(publicKeyHex))
            .thenReturn(Optional.of(walletInfo));
        when(walletStorageService.getEncryptedPrivateKey(publicKeyHex))
            .thenReturn(Optional.of(encryptedPrivateKey));
        when(encryptionService.verifyPassword(encryptedPrivateKey, password)).thenReturn(true);
        
        // When
        WalletBackup result = walletService.backupWallet(publicKeyHex, password);
        
        // Then
        assertNotNull(result);
        assertEquals(publicKeyHex, result.getPublicKeyHex());
        assertEquals(encryptedPrivateKey, result.getEncryptedPrivateKey());
        assertEquals(walletInfo.getName(), result.getWalletName());
        assertNotNull(result.getChecksum());
    }
    
    @Test
    void testRestoreWallet() throws WalletException {
        // Given
        WalletBackup backup = createMockWalletBackup();
        String password = "test_password";
        WalletInfo expectedWallet = createMockWalletInfo();
        
        when(encryptionService.verifyPassword(backup.getEncryptedPrivateKey(), password)).thenReturn(true);
        when(walletStorageService.walletExists(backup.getPublicKeyHex())).thenReturn(false);
        when(walletAccountService.createWalletFromPrivateKey(anyString(), eq(password)))
            .thenReturn(expectedWallet);
        when(encryptionService.decrypt(backup.getEncryptedPrivateKey(), password))
            .thenReturn("decrypted_private_key");
        
        // When
        WalletInfo result = walletService.restoreWallet(backup, password);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedWallet, result);
        verify(encryptionService).verifyPassword(backup.getEncryptedPrivateKey(), password);
        verify(walletStorageService).walletExists(backup.getPublicKeyHex());
        verify(walletAccountService).createWalletFromPrivateKey(anyString(), eq(password));
    }
    
    // ==================== 辅助方法 ====================
    
    private WalletKeyPair createMockWalletKeyPair() {
        // 创建模拟的Ed25519KeyPair
        io.equiflux.node.crypto.Ed25519KeyPair mockKeyPair = mock(io.equiflux.node.crypto.Ed25519KeyPair.class);
        when(mockKeyPair.getPublicKeyHex()).thenReturn("test_public_key_hex");
        when(mockKeyPair.getPrivateKeyHex()).thenReturn("test_private_key_hex");
        
        return new WalletKeyPair(mockKeyPair);
    }
    
    private WalletInfo createMockWalletInfo() {
        return new WalletInfo(
            "test_public_key_hex",
            "test_address",
            "test_wallet",
            WalletStatus.CREATED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
    }
    
    private AccountState createMockAccountState() {
        // 创建模拟的PublicKey
        java.security.PublicKey mockPublicKey = mock(java.security.PublicKey.class);
        // 移除不必要的stubbing，因为测试中没有使用getEncoded()方法
        
        return new AccountState(
            mockPublicKey,
            1000L,
            5L,
            500L,
            System.currentTimeMillis()
        );
    }
    
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
    
    private TransactionStatus createMockTransactionStatus() {
        return new TransactionStatus(
            "transaction_hash",
            TransactionState.PENDING,
            LocalDateTime.now(),
            null,
            null,
            null
        );
    }
    
    private TransactionInfo createMockTransactionInfo() {
        return new TransactionInfo(
            createMockTransaction(),
            "transaction_hash",
            TransactionState.PENDING,
            LocalDateTime.now(),
            null,
            null,
            null
        );
    }
    
    private WalletBackup createMockWalletBackup() {
        String publicKeyHex = "test_public_key_hex";
        String encryptedPrivateKey = "encrypted_private_key";
        String data = publicKeyHex + encryptedPrivateKey;
        String checksum = io.equiflux.node.crypto.HashUtils.toHexString(
            io.equiflux.node.crypto.HashUtils.sha256(data.getBytes())
        );
        
        return new WalletBackup(
            publicKeyHex,
            encryptedPrivateKey,
            "test_wallet",
            LocalDateTime.now(),
            "1.0.0",
            checksum
        );
    }
}
