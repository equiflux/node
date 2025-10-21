package io.equiflux.node.storage;

import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.storage.model.ChainState;
import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 状态存储服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class StateStorageServiceTest {
    
    @Mock
    private StorageService storageService;
    
    private StateStorageService stateStorageService;
    
    @BeforeEach
    void setUp() {
        stateStorageService = new StateStorageService(storageService);
    }
    
    @Test
    void testStoreAccountState() throws StorageException {
        // 准备测试数据
        AccountState accountState = createTestAccountState();
        
        // 执行测试
        stateStorageService.storeAccountState(accountState);
        
        // 验证结果
        verify(storageService).put(eq(StorageKey.accountKey(accountState.getPublicKeyHex())), any(StorageValue.class));
        verify(storageService).put(eq(new StorageKey("account_index", accountState.getPublicKeyHex())), any(StorageValue.class));
        verify(storageService).put(eq(new StorageKey("stake_index", accountState.getPublicKeyHex())), any(StorageValue.class));
    }
    
    @Test
    void testGetAccountState() throws StorageException {
        // 准备测试数据
        AccountState accountState = createTestAccountState();
        StorageValue accountValue = new StorageValue(serializeAccountState(accountState), "AccountState");
        
        when(storageService.get(StorageKey.accountKey(accountState.getPublicKeyHex()))).thenReturn(accountValue);
        
        // 执行测试
        AccountState result = stateStorageService.getAccountState(accountState.getPublicKey());
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getPublicKeyHex()).isEqualTo(accountState.getPublicKeyHex());
        assertThat(result.getBalance()).isEqualTo(accountState.getBalance());
        assertThat(result.getStakeAmount()).isEqualTo(accountState.getStakeAmount());
    }
    
    @Test
    void testGetAccountStateNotFound() throws StorageException {
        // 准备测试数据
        PublicKey publicKey = createTestPublicKey();
        when(storageService.get(StorageKey.accountKey(io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded())))).thenReturn(null);
        
        // 执行测试
        AccountState result = stateStorageService.getAccountState(publicKey);
        
        // 验证结果
        assertThat(result).isNull();
    }
    
    @Test
    void testAccountExists() throws StorageException {
        // 准备测试数据
        PublicKey publicKey = createTestPublicKey();
        String publicKeyHex = io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded());
        when(storageService.exists(StorageKey.accountKey(publicKeyHex))).thenReturn(true);
        
        // 执行测试
        boolean exists = stateStorageService.accountExists(publicKey);
        
        // 验证结果
        assertThat(exists).isTrue();
    }
    
    @Test
    void testUpdateAccountBalance() throws StorageException {
        // 准备测试数据
        PublicKey publicKey = createTestPublicKey();
        String publicKeyHex = io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded());
        
        // 模拟账户不存在，将创建新账户
        when(storageService.get(StorageKey.accountKey(publicKeyHex))).thenReturn(null);
        
        // 执行测试
        AccountState result = stateStorageService.updateAccountBalance(publicKey, 1000L);
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getBalance()).isEqualTo(1000L);
        assertThat(result.getStakeAmount()).isEqualTo(0L);
    }
    
    @Test
    void testUpdateAccountBalanceExistingAccount() throws StorageException {
        // 准备测试数据
        AccountState existingAccount = createTestAccountState();
        String publicKeyHex = existingAccount.getPublicKeyHex();
        StorageValue accountValue = new StorageValue(serializeAccountState(existingAccount), "AccountState");
        
        when(storageService.get(StorageKey.accountKey(publicKeyHex))).thenReturn(accountValue);
        
        // 执行测试
        AccountState result = stateStorageService.updateAccountBalance(existingAccount.getPublicKey(), 500L);
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getBalance()).isEqualTo(existingAccount.getBalance() + 500L);
    }
    
    @Test
    void testUpdateAccountNonce() throws StorageException {
        // 准备测试数据
        AccountState existingAccount = createTestAccountState();
        String publicKeyHex = existingAccount.getPublicKeyHex();
        StorageValue accountValue = new StorageValue(serializeAccountState(existingAccount), "AccountState");
        
        when(storageService.get(StorageKey.accountKey(publicKeyHex))).thenReturn(accountValue);
        
        // 执行测试
        AccountState result = stateStorageService.updateAccountNonce(existingAccount.getPublicKey());
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getNonce()).isEqualTo(existingAccount.getNonce() + 1);
    }
    
    @Test
    void testUpdateAccountNonceAccountNotExists() throws StorageException {
        // 准备测试数据
        PublicKey publicKey = createTestPublicKey();
        String publicKeyHex = io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded());
        
        when(storageService.get(StorageKey.accountKey(publicKeyHex))).thenReturn(null);
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> stateStorageService.updateAccountNonce(publicKey))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to update account nonce");
    }
    
    @Test
    void testUpdateAccountStake() throws StorageException {
        // 准备测试数据
        PublicKey publicKey = createTestPublicKey();
        String publicKeyHex = io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded());
        
        // 模拟账户不存在，将创建新账户
        when(storageService.get(StorageKey.accountKey(publicKeyHex))).thenReturn(null);
        
        // 执行测试
        AccountState result = stateStorageService.updateAccountStake(publicKey, 2000L);
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getStakeAmount()).isEqualTo(2000L);
        assertThat(result.getBalance()).isEqualTo(0L);
    }
    
    @Test
    void testStoreChainState() throws StorageException {
        // 准备测试数据
        ChainState chainState = createTestChainState();
        
        // 执行测试
        stateStorageService.storeChainState(chainState);
        
        // 验证结果
        verify(storageService).put(eq(StorageKey.chainStateKey()), any(StorageValue.class));
    }
    
    @Test
    void testGetChainState() throws StorageException {
        // 准备测试数据
        ChainState chainState = createTestChainState();
        StorageValue chainValue = new StorageValue(serializeChainState(chainState), "ChainState");
        
        when(storageService.get(StorageKey.chainStateKey())).thenReturn(chainValue);
        
        // 执行测试
        ChainState result = stateStorageService.getChainState();
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getCurrentHeight()).isEqualTo(chainState.getCurrentHeight());
        assertThat(result.getCurrentRound()).isEqualTo(chainState.getCurrentRound());
        assertThat(result.getTotalSupply()).isEqualTo(chainState.getTotalSupply());
    }
    
    @Test
    void testGetChainStateNotFound() throws StorageException {
        // 准备测试数据
        when(storageService.get(StorageKey.chainStateKey())).thenReturn(null);
        
        // 执行测试
        ChainState result = stateStorageService.getChainState();
        
        // 验证结果
        assertThat(result).isNull();
    }
    
    @Test
    void testUpdateChainHeight() throws StorageException {
        // 准备测试数据
        ChainState existingChainState = createTestChainState();
        StorageValue chainValue = new StorageValue(serializeChainState(existingChainState), "ChainState");
        
        when(storageService.get(StorageKey.chainStateKey())).thenReturn(chainValue);
        
        // 执行测试
        ChainState result = stateStorageService.updateChainHeight();
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getCurrentHeight()).isEqualTo(existingChainState.getCurrentHeight() + 1);
    }
    
    @Test
    void testUpdateChainHeightChainStateNotExists() throws StorageException {
        // 准备测试数据
        when(storageService.get(StorageKey.chainStateKey())).thenReturn(null);
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> stateStorageService.updateChainHeight())
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to update chain height");
    }
    
    @Test
    void testUpdateChainRound() throws StorageException {
        // 准备测试数据
        ChainState existingChainState = createTestChainState();
        StorageValue chainValue = new StorageValue(serializeChainState(existingChainState), "ChainState");
        
        when(storageService.get(StorageKey.chainStateKey())).thenReturn(chainValue);
        
        // 执行测试
        ChainState result = stateStorageService.updateChainRound();
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getCurrentRound()).isEqualTo(existingChainState.getCurrentRound() + 1);
    }
    
    @Test
    void testUpdateChainSupply() throws StorageException {
        // 准备测试数据
        ChainState existingChainState = createTestChainState();
        StorageValue chainValue = new StorageValue(serializeChainState(existingChainState), "ChainState");
        
        when(storageService.get(StorageKey.chainStateKey())).thenReturn(chainValue);
        
        // 执行测试
        ChainState result = stateStorageService.updateChainSupply(1000L);
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getTotalSupply()).isEqualTo(existingChainState.getTotalSupply() + 1000L);
    }
    
    @Test
    void testUpdateChainDifficulty() throws StorageException {
        // 准备测试数据
        ChainState existingChainState = createTestChainState();
        StorageValue chainValue = new StorageValue(serializeChainState(existingChainState), "ChainState");
        BigInteger newDifficulty = BigInteger.valueOf(2000000);
        
        when(storageService.get(StorageKey.chainStateKey())).thenReturn(chainValue);
        
        // 执行测试
        ChainState result = stateStorageService.updateChainDifficulty(newDifficulty);
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getCurrentDifficulty()).isEqualTo(newDifficulty);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testStoreAccountStates() throws StorageException {
        // 准备测试数据
        List<AccountState> accountStates = Arrays.asList(
            createTestAccountState(),
            createTestAccountState(),
            createTestAccountState()
        );
        
        // 执行测试
        stateStorageService.storeAccountStates(accountStates);
        
        // 验证结果
        verify(storageService, times(1)).putBatch(any(Map.class));
    }
    
    @Test
    void testGetAllAccountStates() throws StorageException {
        // 准备测试数据
        AccountState accountState1 = createTestAccountState();
        AccountState accountState2 = createTestAccountState();
        
        Map<StorageKey, StorageValue> accountEntries = new HashMap<>();
        accountEntries.put(StorageKey.accountKey(accountState1.getPublicKeyHex()), 
                          new StorageValue(serializeAccountState(accountState1), "AccountState"));
        accountEntries.put(StorageKey.accountKey(accountState2.getPublicKeyHex()), 
                          new StorageValue(serializeAccountState(accountState2), "AccountState"));
        
        when(storageService.getByNamespace("account")).thenReturn(accountEntries);
        
        // 执行测试
        List<AccountState> result = stateStorageService.getAllAccountStates();
        
        // 验证结果
        assertThat(result).hasSize(2);
    }
    
    @Test
    void testCreateSnapshot() throws StorageException {
        // 准备测试数据
        ChainState chainState = createTestChainState();
        AccountState accountState = createTestAccountState();
        
        StorageValue chainValue = new StorageValue(serializeChainState(chainState), "ChainState");
        Map<StorageKey, StorageValue> accountEntries = new HashMap<>();
        accountEntries.put(StorageKey.accountKey(accountState.getPublicKeyHex()), 
                          new StorageValue(serializeAccountState(accountState), "AccountState"));
        
        when(storageService.get(StorageKey.chainStateKey())).thenReturn(chainValue);
        when(storageService.getByNamespace("account")).thenReturn(accountEntries);
        
        // 执行测试
        StateStorageService.StateSnapshot snapshot = stateStorageService.createSnapshot();
        
        // 验证结果
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.getChainState()).isNotNull();
        assertThat(snapshot.getAccountStates()).hasSize(1);
        assertThat(snapshot.getTimestamp()).isGreaterThan(0);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testRestoreSnapshot() throws StorageException {
        // 准备测试数据
        ChainState chainState = createTestChainState();
        AccountState accountState = createTestAccountState();
        StateStorageService.StateSnapshot snapshot = new StateStorageService.StateSnapshot(
            chainState, Arrays.asList(accountState), System.currentTimeMillis());
        
        // 执行测试
        stateStorageService.restoreSnapshot(snapshot);
        
        // 验证结果
        verify(storageService).put(eq(StorageKey.chainStateKey()), any(StorageValue.class));
        verify(storageService, times(1)).putBatch(any(Map.class));
    }
    
    @Test
    void testClearCache() {
        // 执行测试
        stateStorageService.clearCache();
        
        // 验证结果
        Map<String, Object> stats = stateStorageService.getCacheStats();
        assertThat(stats.get("accountCacheSize")).isEqualTo(0);
        assertThat(stats.get("chainStateCached")).isEqualTo(false);
    }
    
    @Test
    void testGetCacheStats() {
        // 执行测试
        Map<String, Object> stats = stateStorageService.getCacheStats();
        
        // 验证结果
        assertThat(stats).containsKeys("accountCacheSize", "chainStateCached");
        assertThat(stats.get("accountCacheSize")).isInstanceOf(Integer.class);
        assertThat(stats.get("chainStateCached")).isInstanceOf(Boolean.class);
    }
    
    @Test
    void testStoreAccountStateException() throws StorageException {
        // 准备测试数据
        AccountState accountState = createTestAccountState();
        doThrow(new StorageException("Test exception")).when(storageService).put(any(StorageKey.class), any(StorageValue.class));
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> stateStorageService.storeAccountState(accountState))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to store account state");
    }
    
    @Test
    void testGetAccountStateException() throws StorageException {
        // 准备测试数据
        PublicKey publicKey = createTestPublicKey();
        String publicKeyHex = io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded());
        when(storageService.get(StorageKey.accountKey(publicKeyHex))).thenThrow(new StorageException("Test exception"));
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> stateStorageService.getAccountState(publicKey))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to retrieve account state");
    }
    
    @Test
    void testStoreAccountStatesEmptyList() throws StorageException {
        // 执行测试
        stateStorageService.storeAccountStates(new ArrayList<>());
        
        // 验证结果 - 不应该调用存储服务
        verify(storageService, never()).putBatch(any());
    }
    
    @Test
    void testStoreAccountStatesNull() throws StorageException {
        // 执行测试
        stateStorageService.storeAccountStates(null);
        
        // 验证结果 - 不应该调用存储服务
        verify(storageService, never()).putBatch(any());
    }
    
    /**
     * 创建测试账户状态
     */
    private AccountState createTestAccountState() {
        try {
            PublicKey publicKey = createTestPublicKey();
            return new AccountState(publicKey, 10000L, 1L, 5000L, System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test account state", e);
        }
    }
    
    /**
     * 创建测试链状态
     */
    private ChainState createTestChainState() {
        return new ChainState(100L, 50L, 1000000L, 50, BigInteger.valueOf(1000000), System.currentTimeMillis());
    }
    
    /**
     * 创建测试公钥
     */
    private PublicKey createTestPublicKey() {
        try {
            KeyPair keyPair = java.security.KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            return keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test public key", e);
        }
    }
    
    /**
     * 序列化账户状态
     */
    private byte[] serializeAccountState(AccountState accountState) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsBytes(accountState);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize account state", e);
        }
    }
    
    /**
     * 序列化链状态
     */
    private byte[] serializeChainState(ChainState chainState) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsBytes(chainState);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize chain state", e);
        }
    }
}
