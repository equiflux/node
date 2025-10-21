package io.equiflux.node.wallet.rpc.controller;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.TransactionType;
import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.wallet.model.*;
import io.equiflux.node.wallet.rpc.dto.*;
import io.equiflux.node.wallet.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 钱包RPC控制器测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class WalletRpcControllerTest {
    
    @Mock
    private WalletService walletService;
    
    @InjectMocks
    private WalletRpcController walletRpcController;
    
    private String testPublicKeyHex;
    private String testPassword;
    private WalletInfo testWalletInfo;
    private AccountState testAccountState;
    
    @BeforeEach
    void setUp() {
        testPublicKeyHex = "test_public_key_hex";
        testPassword = "test_password";
        
        testWalletInfo = new WalletInfo(
            testPublicKeyHex,
            "EQtest_address",
            "Test Wallet",
            WalletStatus.CREATED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
        
        testAccountState = createMockAccountState(testPublicKeyHex, 1000L, 1L, 500L);
    }
    
    @Test
    void testCreateWallet() {
        // Given
        CreateWalletRequestDto request = new CreateWalletRequestDto(testPassword);
        when(walletService.createWallet(testPassword)).thenReturn(testWalletInfo);
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.createWallet(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testPublicKeyHex, response.getBody().getPublicKeyHex());
        
        verify(walletService).createWallet(testPassword);
    }
    
    @Test
    void testCreateWalletWithException() {
        // Given
        CreateWalletRequestDto request = new CreateWalletRequestDto(testPassword);
        when(walletService.createWallet(testPassword)).thenThrow(new WalletException("Test error"));
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.createWallet(request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Test error", response.getBody().getMessage());
    }
    
    @Test
    void testImportWallet() {
        // Given
        String privateKeyHex = "test_private_key_hex";
        ImportWalletRequestDto request = new ImportWalletRequestDto(privateKeyHex, testPassword);
        when(walletService.createWalletFromPrivateKey(privateKeyHex, testPassword)).thenReturn(testWalletInfo);
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.importWallet(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testPublicKeyHex, response.getBody().getPublicKeyHex());
        
        verify(walletService).createWalletFromPrivateKey(privateKeyHex, testPassword);
    }
    
    @Test
    void testUnlockWallet() {
        // Given
        UnlockWalletRequestDto request = new UnlockWalletRequestDto(testPublicKeyHex, testPassword);
        when(walletService.unlockWallet(testPublicKeyHex, testPassword)).thenReturn(true);
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.unlockWallet(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Wallet unlocked successfully", response.getBody().getMessage());
        
        verify(walletService).unlockWallet(testPublicKeyHex, testPassword);
    }
    
    @Test
    void testUnlockWalletWithInvalidPassword() {
        // Given
        UnlockWalletRequestDto request = new UnlockWalletRequestDto(testPublicKeyHex, testPassword);
        when(walletService.unlockWallet(testPublicKeyHex, testPassword)).thenReturn(false);
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.unlockWallet(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid password", response.getBody().getMessage());
    }
    
    @Test
    void testLockWallet() {
        // Given
        LockWalletRequestDto request = new LockWalletRequestDto(testPublicKeyHex);
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.lockWallet(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Wallet locked successfully", response.getBody().getMessage());
        
        verify(walletService).lockWallet(testPublicKeyHex);
    }
    
    @Test
    void testGetWalletInfo() {
        // Given
        when(walletService.getWalletInfo(testPublicKeyHex)).thenReturn(Optional.of(testWalletInfo));
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.getWalletInfo(testPublicKeyHex);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testPublicKeyHex, response.getBody().getPublicKeyHex());
        
        verify(walletService).getWalletInfo(testPublicKeyHex);
    }
    
    @Test
    void testGetWalletInfoNotFound() {
        // Given
        when(walletService.getWalletInfo(testPublicKeyHex)).thenReturn(Optional.empty());
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.getWalletInfo(testPublicKeyHex);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        verify(walletService).getWalletInfo(testPublicKeyHex);
    }
    
    @Test
    void testGetAllWallets() {
        // Given
        List<WalletInfo> wallets = Arrays.asList(testWalletInfo);
        when(walletService.getAllWallets()).thenReturn(wallets);
        
        // When
        ResponseEntity<WalletListResponseDto> response = walletRpcController.getAllWallets();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getCount());
        assertEquals(1, response.getBody().getWallets().size());
        
        verify(walletService).getAllWallets();
    }
    
    @Test
    void testGetBalance() {
        // Given
        when(walletService.getBalance(testPublicKeyHex)).thenReturn(1000L);
        
        // When
        ResponseEntity<BalanceResponseDto> response = walletRpcController.getBalance(testPublicKeyHex);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1000L, response.getBody().getBalance());
        assertEquals(testPublicKeyHex, response.getBody().getPublicKeyHex());
        
        verify(walletService).getBalance(testPublicKeyHex);
    }
    
    @Test
    void testGetAccountState() {
        // Given
        when(walletService.getAccountState(testPublicKeyHex)).thenReturn(Optional.of(testAccountState));
        
        // When
        ResponseEntity<AccountStateResponseDto> response = walletRpcController.getAccountState(testPublicKeyHex);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testPublicKeyHex, response.getBody().getPublicKeyHex());
        assertEquals(1000L, response.getBody().getBalance());
        assertEquals(1L, response.getBody().getNonce());
        assertEquals(500L, response.getBody().getStakeAmount());
        
        verify(walletService).getAccountState(testPublicKeyHex);
    }
    
    @Test
    void testGetAccountStateNotFound() {
        // Given
        when(walletService.getAccountState(testPublicKeyHex)).thenReturn(Optional.empty());
        
        // When
        ResponseEntity<AccountStateResponseDto> response = walletRpcController.getAccountState(testPublicKeyHex);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(0L, response.getBody().getBalance());
        assertEquals(0L, response.getBody().getNonce());
        assertEquals(0L, response.getBody().getStakeAmount());
    }
    
    @Test
    void testBuildTransferTransaction() {
        // Given
        String toPublicKeyHex = "to_public_key_hex";
        long amount = 100L;
        long fee = 10L;
        
        TransferTransactionRequestDto request = new TransferTransactionRequestDto(
            testPublicKeyHex, toPublicKeyHex, amount, fee, testPassword
        );
        
        Transaction mockTransaction = createMockTransaction();
        when(walletService.buildTransferTransaction(testPublicKeyHex, toPublicKeyHex, amount, fee, testPassword))
            .thenReturn(mockTransaction);
        
        // When
        ResponseEntity<TransactionResponseDto> response = walletRpcController.buildTransferTransaction(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test_transaction_hash", response.getBody().getTransactionHash());
        
        verify(walletService).buildTransferTransaction(testPublicKeyHex, toPublicKeyHex, amount, fee, testPassword);
    }
    
    @Test
    void testBuildStakeTransaction() {
        // Given
        long stakeAmount = 500L;
        long fee = 10L;
        
        StakeTransactionRequestDto request = new StakeTransactionRequestDto(
            testPublicKeyHex, stakeAmount, fee, testPassword
        );
        
        Transaction mockTransaction = createMockTransaction();
        when(walletService.buildStakeTransaction(testPublicKeyHex, stakeAmount, fee, testPassword))
            .thenReturn(mockTransaction);
        
        // When
        ResponseEntity<TransactionResponseDto> response = walletRpcController.buildStakeTransaction(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test_transaction_hash", response.getBody().getTransactionHash());
        
        verify(walletService).buildStakeTransaction(testPublicKeyHex, stakeAmount, fee, testPassword);
    }
    
    @Test
    void testGetTransactionStatus() {
        // Given
        String transactionHash = "test_transaction_hash";
        TransactionStatus mockStatus = new TransactionStatus(
            transactionHash,
            TransactionState.CONFIRMED,
            LocalDateTime.now(),
            100L,
            0,
            null
        );
        
        when(walletService.getTransactionStatus(transactionHash)).thenReturn(Optional.of(mockStatus));
        
        // When
        ResponseEntity<TransactionStatusResponseDto> response = walletRpcController.getTransactionStatus(transactionHash);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(transactionHash, response.getBody().getTransactionHash());
        assertEquals("confirmed", response.getBody().getState());
        assertEquals(100L, response.getBody().getBlockHeight());
        
        verify(walletService).getTransactionStatus(transactionHash);
    }
    
    @Test
    void testGetTransactionStatusNotFound() {
        // Given
        String transactionHash = "test_transaction_hash";
        when(walletService.getTransactionStatus(transactionHash)).thenReturn(Optional.empty());
        
        // When
        ResponseEntity<TransactionStatusResponseDto> response = walletRpcController.getTransactionStatus(transactionHash);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("not_found", response.getBody().getState());
        assertEquals("Transaction not found", response.getBody().getMessage());
    }
    
    @Test
    void testChangePassword() {
        // Given
        String oldPassword = "old_password";
        String newPassword = "new_password";
        
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(testPublicKeyHex, oldPassword, newPassword);
        when(walletService.changePassword(testPublicKeyHex, oldPassword, newPassword)).thenReturn(true);
        
        // When
        ResponseEntity<WalletResponseDto> response = walletRpcController.changePassword(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Password changed successfully", response.getBody().getMessage());
        
        verify(walletService).changePassword(testPublicKeyHex, oldPassword, newPassword);
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
    
    private Transaction createMockTransaction() {
        return Transaction.builder()
            .fromPublicKey(testPublicKeyHex)
            .toPublicKey("to_public_key_hex")
            .amount(100L)
            .fee(10L)
            .nonce(1L)
            .timestamp(LocalDateTime.now())
            .type(TransactionType.TRANSFER)
            .signature("test_signature")
            .build();
    }
}
