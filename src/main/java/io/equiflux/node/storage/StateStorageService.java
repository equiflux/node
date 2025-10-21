package io.equiflux.node.storage;

import io.equiflux.node.storage.model.AccountState;
import io.equiflux.node.storage.model.ChainState;
import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.exception.StorageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 状态存储管理服务
 * 
 * <p>负责区块链状态的存储、检索和管理，包括账户状态和链状态。
 * 
 * <p>存储结构：
 * <ul>
 *   <li>账户状态：account:publicKey -> AccountState</li>
 *   <li>链状态：chain:state -> ChainState</li>
 *   <li>账户索引：account_index:publicKey -> balance</li>
 *   <li>质押索引：stake_index:publicKey -> stakeAmount</li>
 * </ul>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>账户状态的存储和检索</li>
 *   <li>链状态的存储和检索</li>
 *   <li>账户余额管理</li>
 *   <li>质押管理</li>
 *   <li>状态快照和恢复</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class StateStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(StateStorageService.class);
    
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    
    // 缓存
    private final Map<String, AccountState> accountCache = new ConcurrentHashMap<>();
    private volatile ChainState chainState;
    
    // 线程安全
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 缓存配置
    private final int maxCacheSize = 2000;
    
    public StateStorageService(StorageService storageService) {
        this.storageService = storageService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }
    
    /**
     * 存储账户状态
     * 
     * @param accountState 账户状态
     * @throws StorageException 存储异常
     */
    public void storeAccountState(AccountState accountState) throws StorageException {
        lock.writeLock().lock();
        try {
            String publicKeyHex = accountState.getPublicKeyHex();
            
            // 序列化账户状态
            byte[] accountBytes = objectMapper.writeValueAsBytes(accountState);
            StorageValue accountValue = new StorageValue(accountBytes, "AccountState");
            
            // 存储账户状态
            StorageKey accountKey = StorageKey.accountKey(publicKeyHex);
            storageService.put(accountKey, accountValue);
            
            // 更新账户索引
            updateAccountIndex(accountState);
            
            // 更新缓存
            accountCache.put(publicKeyHex, accountState);
            evictCacheIfNeeded();
            
            logger.debug("Stored account state: publicKey={}, balance={}, stake={}", 
                        publicKeyHex, accountState.getBalance(), accountState.getStakeAmount());
            
        } catch (Exception e) {
            logger.error("Failed to store account state: publicKey={}", 
                        accountState.getPublicKeyHex(), e);
            throw new StorageException("Failed to store account state: " + accountState.getPublicKeyHex(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 按公钥十六进制字符串获取账户状态
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 账户状态，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public AccountState getAccountStateByPublicKeyHex(String publicKeyHex) throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            AccountState cachedAccount = accountCache.get(publicKeyHex);
            if (cachedAccount != null) {
                return cachedAccount;
            }
            
            // 从存储中获取
            StorageKey accountKey = StorageKey.accountKey(publicKeyHex);
            StorageValue accountValue = storageService.get(accountKey);
            
            if (accountValue == null) {
                return null;
            }
            
            AccountState accountState = objectMapper.readValue(accountValue.getData(), AccountState.class);
            
            // 更新缓存
            accountCache.put(publicKeyHex, accountState);
            evictCacheIfNeeded();
            
            logger.debug("Retrieved account state: publicKey={}", publicKeyHex);
            
            return accountState;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve account state: publicKey={}", publicKeyHex, e);
            throw new StorageException("Failed to retrieve account state: " + publicKeyHex, e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取账户状态
     * 
     * @param publicKey 公钥
     * @return 账户状态，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public AccountState getAccountState(PublicKey publicKey) throws StorageException {
        lock.readLock().lock();
        try {
            String publicKeyHex = io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded());
            
            // 先检查缓存
            AccountState cachedAccount = accountCache.get(publicKeyHex);
            if (cachedAccount != null) {
                return cachedAccount;
            }
            
            // 从存储中获取
            StorageKey accountKey = StorageKey.accountKey(publicKeyHex);
            StorageValue accountValue = storageService.get(accountKey);
            
            if (accountValue == null) {
                return null;
            }
            
            AccountState accountState = objectMapper.readValue(accountValue.getData(), AccountState.class);
            
            // 更新缓存
            accountCache.put(publicKeyHex, accountState);
            evictCacheIfNeeded();
            
            logger.debug("Retrieved account state: publicKey={}", publicKeyHex);
            
            return accountState;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve account state", e);
            throw new StorageException("Failed to retrieve account state", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 检查账户是否存在
     * 
     * @param publicKey 公钥
     * @return true如果存在，false否则
     * @throws StorageException 存储异常
     */
    public boolean accountExists(PublicKey publicKey) throws StorageException {
        lock.readLock().lock();
        try {
            String publicKeyHex = io.equiflux.node.crypto.HashUtils.toHexString(publicKey.getEncoded());
            
            // 先检查缓存
            if (accountCache.containsKey(publicKeyHex)) {
                return true;
            }
            
            // 检查存储
            StorageKey accountKey = StorageKey.accountKey(publicKeyHex);
            return storageService.exists(accountKey);
            
        } catch (Exception e) {
            logger.error("Failed to check account existence", e);
            throw new StorageException("Failed to check account existence", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 更新账户余额
     * 
     * @param publicKey 公钥
     * @param balanceChange 余额变化
     * @return 更新后的账户状态
     * @throws StorageException 存储异常
     */
    public AccountState updateAccountBalance(PublicKey publicKey, long balanceChange) throws StorageException {
        lock.writeLock().lock();
        try {
            AccountState currentState = getAccountState(publicKey);
            
            if (currentState == null) {
                // 创建新账户
                currentState = new AccountState(publicKey, 0, 0, 0, System.currentTimeMillis());
            }
            
            AccountState newState = currentState.updateBalance(balanceChange);
            storeAccountState(newState);
            
            logger.debug("Updated account balance: publicKey={}, change={}, newBalance={}", 
                        newState.getPublicKeyHex(), balanceChange, newState.getBalance());
            
            return newState;
            
        } catch (Exception e) {
            logger.error("Failed to update account balance", e);
            throw new StorageException("Failed to update account balance", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 更新账户nonce
     * 
     * @param publicKey 公钥
     * @return 更新后的账户状态
     * @throws StorageException 存储异常
     */
    public AccountState updateAccountNonce(PublicKey publicKey) throws StorageException {
        lock.writeLock().lock();
        try {
            AccountState currentState = getAccountState(publicKey);
            
            if (currentState == null) {
                throw new StorageException("Account does not exist");
            }
            
            AccountState newState = currentState.incrementNonce();
            storeAccountState(newState);
            
            logger.debug("Updated account nonce: publicKey={}, newNonce={}", 
                        newState.getPublicKeyHex(), newState.getNonce());
            
            return newState;
            
        } catch (Exception e) {
            logger.error("Failed to update account nonce", e);
            throw new StorageException("Failed to update account nonce", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 更新账户质押
     * 
     * @param publicKey 公钥
     * @param stakeChange 质押变化
     * @return 更新后的账户状态
     * @throws StorageException 存储异常
     */
    public AccountState updateAccountStake(PublicKey publicKey, long stakeChange) throws StorageException {
        lock.writeLock().lock();
        try {
            AccountState currentState = getAccountState(publicKey);
            
            if (currentState == null) {
                // 创建新账户
                currentState = new AccountState(publicKey, 0, 0, 0, System.currentTimeMillis());
            }
            
            AccountState newState = currentState.updateStake(stakeChange);
            storeAccountState(newState);
            
            logger.debug("Updated account stake: publicKey={}, change={}, newStake={}", 
                        newState.getPublicKeyHex(), stakeChange, newState.getStakeAmount());
            
            return newState;
            
        } catch (Exception e) {
            logger.error("Failed to update account stake", e);
            throw new StorageException("Failed to update account stake", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 存储链状态
     * 
     * @param chainState 链状态
     * @throws StorageException 存储异常
     */
    public void storeChainState(ChainState chainState) throws StorageException {
        lock.writeLock().lock();
        try {
            // 序列化链状态
            byte[] chainBytes = objectMapper.writeValueAsBytes(chainState);
            StorageValue chainValue = new StorageValue(chainBytes, "ChainState");
            
            // 存储链状态
            StorageKey chainKey = StorageKey.chainStateKey();
            storageService.put(chainKey, chainValue);
            
            // 更新缓存
            this.chainState = chainState;
            
            logger.debug("Stored chain state: height={}, round={}, totalSupply={}", 
                        chainState.getCurrentHeight(), chainState.getCurrentRound(), 
                        chainState.getTotalSupply());
            
        } catch (Exception e) {
            logger.error("Failed to store chain state", e);
            throw new StorageException("Failed to store chain state", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取链状态
     * 
     * @return 链状态，如果不存在返回null
     * @throws StorageException 存储异常
     */
    public ChainState getChainState() throws StorageException {
        lock.readLock().lock();
        try {
            // 先检查缓存
            if (chainState != null) {
                return chainState;
            }
            
            // 从存储中获取
            StorageKey chainKey = StorageKey.chainStateKey();
            StorageValue chainValue = storageService.get(chainKey);
            
            if (chainValue == null) {
                return null;
            }
            
            ChainState state = objectMapper.readValue(chainValue.getData(), ChainState.class);
            this.chainState = state;
            
            logger.debug("Retrieved chain state: height={}", state.getCurrentHeight());
            
            return state;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve chain state", e);
            throw new StorageException("Failed to retrieve chain state", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 更新链状态高度
     * 
     * @return 更新后的链状态
     * @throws StorageException 存储异常
     */
    public ChainState updateChainHeight() throws StorageException {
        lock.writeLock().lock();
        try {
            ChainState currentState = getChainState();
            
            if (currentState == null) {
                throw new StorageException("Chain state does not exist");
            }
            
            ChainState newState = currentState.incrementHeight();
            storeChainState(newState);
            
            logger.debug("Updated chain height: newHeight={}", newState.getCurrentHeight());
            
            return newState;
            
        } catch (Exception e) {
            logger.error("Failed to update chain height", e);
            throw new StorageException("Failed to update chain height", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 更新链状态轮次
     * 
     * @return 更新后的链状态
     * @throws StorageException 存储异常
     */
    public ChainState updateChainRound() throws StorageException {
        lock.writeLock().lock();
        try {
            ChainState currentState = getChainState();
            
            if (currentState == null) {
                throw new StorageException("Chain state does not exist");
            }
            
            ChainState newState = currentState.incrementRound();
            storeChainState(newState);
            
            logger.debug("Updated chain round: newRound={}", newState.getCurrentRound());
            
            return newState;
            
        } catch (Exception e) {
            logger.error("Failed to update chain round", e);
            throw new StorageException("Failed to update chain round", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 更新链状态供应量
     * 
     * @param supplyChange 供应量变化
     * @return 更新后的链状态
     * @throws StorageException 存储异常
     */
    public ChainState updateChainSupply(long supplyChange) throws StorageException {
        lock.writeLock().lock();
        try {
            ChainState currentState = getChainState();
            
            if (currentState == null) {
                throw new StorageException("Chain state does not exist");
            }
            
            ChainState newState = currentState.updateSupply(supplyChange);
            storeChainState(newState);
            
            logger.debug("Updated chain supply: change={}, newSupply={}", 
                        supplyChange, newState.getTotalSupply());
            
            return newState;
            
        } catch (Exception e) {
            logger.error("Failed to update chain supply", e);
            throw new StorageException("Failed to update chain supply", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 更新链状态难度
     * 
     * @param newDifficulty 新难度
     * @return 更新后的链状态
     * @throws StorageException 存储异常
     */
    public ChainState updateChainDifficulty(java.math.BigInteger newDifficulty) throws StorageException {
        lock.writeLock().lock();
        try {
            ChainState currentState = getChainState();
            
            if (currentState == null) {
                throw new StorageException("Chain state does not exist");
            }
            
            ChainState newState = currentState.updateDifficulty(newDifficulty);
            storeChainState(newState);
            
            logger.debug("Updated chain difficulty: newDifficulty={}", newDifficulty);
            
            return newState;
            
        } catch (Exception e) {
            logger.error("Failed to update chain difficulty", e);
            throw new StorageException("Failed to update chain difficulty", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 批量存储账户状态
     * 
     * @param accountStates 账户状态列表
     * @throws StorageException 存储异常
     */
    public void storeAccountStates(List<AccountState> accountStates) throws StorageException {
        if (accountStates == null || accountStates.isEmpty()) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            Map<StorageKey, StorageValue> entries = new HashMap<>();
            
            for (AccountState accountState : accountStates) {
                String publicKeyHex = accountState.getPublicKeyHex();
                
                // 序列化账户状态
                byte[] accountBytes = objectMapper.writeValueAsBytes(accountState);
                StorageValue accountValue = new StorageValue(accountBytes, "AccountState");
                
                // 存储账户状态
                StorageKey accountKey = StorageKey.accountKey(publicKeyHex);
                entries.put(accountKey, accountValue);
                
                // 更新缓存
                accountCache.put(publicKeyHex, accountState);
            }
            
            // 批量存储
            storageService.putBatch(entries);
            
            evictCacheIfNeeded();
            
            logger.info("Batch stored {} account states", accountStates.size());
            
        } catch (Exception e) {
            logger.error("Failed to batch store {} account states", accountStates.size(), e);
            throw new StorageException("Failed to batch store account states", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取所有账户状态
     * 
     * @return 账户状态列表
     * @throws StorageException 存储异常
     */
    public List<AccountState> getAllAccountStates() throws StorageException {
        lock.readLock().lock();
        try {
            Map<StorageKey, StorageValue> accountEntries = storageService.getByNamespace("account");
            List<AccountState> accountStates = new ArrayList<>();
            
            for (StorageValue value : accountEntries.values()) {
                AccountState accountState = objectMapper.readValue(value.getData(), AccountState.class);
                accountStates.add(accountState);
            }
            
            logger.debug("Retrieved {} account states", accountStates.size());
            
            return accountStates;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve all account states", e);
            throw new StorageException("Failed to retrieve all account states", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 创建状态快照
     * 
     * @return 状态快照
     * @throws StorageException 存储异常
     */
    public StateSnapshot createSnapshot() throws StorageException {
        lock.readLock().lock();
        try {
            ChainState chainState = getChainState();
            List<AccountState> accountStates = getAllAccountStates();
            
            StateSnapshot snapshot = new StateSnapshot(chainState, accountStates, System.currentTimeMillis());
            
            logger.info("Created state snapshot: height={}, accounts={}", 
                       chainState != null ? chainState.getCurrentHeight() : -1, 
                       accountStates.size());
            
            return snapshot;
            
        } catch (Exception e) {
            logger.error("Failed to create state snapshot", e);
            throw new StorageException("Failed to create state snapshot", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 恢复状态快照
     * 
     * @param snapshot 状态快照
     * @throws StorageException 存储异常
     */
    public void restoreSnapshot(StateSnapshot snapshot) throws StorageException {
        lock.writeLock().lock();
        try {
            if (snapshot.getChainState() != null) {
                storeChainState(snapshot.getChainState());
            }
            
            if (snapshot.getAccountStates() != null && !snapshot.getAccountStates().isEmpty()) {
                storeAccountStates(snapshot.getAccountStates());
            }
            
            logger.info("Restored state snapshot: height={}, accounts={}", 
                       snapshot.getChainState() != null ? snapshot.getChainState().getCurrentHeight() : -1,
                       snapshot.getAccountStates() != null ? snapshot.getAccountStates().size() : 0);
            
        } catch (Exception e) {
            logger.error("Failed to restore state snapshot", e);
            throw new StorageException("Failed to restore state snapshot", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        lock.writeLock().lock();
        try {
            accountCache.clear();
            chainState = null;
            logger.info("State cache cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        lock.readLock().lock();
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("accountCacheSize", accountCache.size());
            stats.put("chainStateCached", chainState != null);
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 更新账户索引
     */
    private void updateAccountIndex(AccountState accountState) throws StorageException {
        String publicKeyHex = accountState.getPublicKeyHex();
        
        // 更新账户索引
        StorageKey accountIndexKey = new StorageKey("account_index", publicKeyHex);
        byte[] balanceBytes = String.valueOf(accountState.getBalance()).getBytes();
        StorageValue balanceValue = new StorageValue(balanceBytes, "Balance");
        storageService.put(accountIndexKey, balanceValue);
        
        // 更新质押索引
        StorageKey stakeIndexKey = new StorageKey("stake_index", publicKeyHex);
        byte[] stakeBytes = String.valueOf(accountState.getStakeAmount()).getBytes();
        StorageValue stakeValue = new StorageValue(stakeBytes, "Stake");
        storageService.put(stakeIndexKey, stakeValue);
    }
    
    /**
     * 缓存淘汰
     */
    private void evictCacheIfNeeded() {
        if (accountCache.size() > maxCacheSize) {
            // 简单的LRU淘汰策略
            Iterator<Map.Entry<String, AccountState>> iterator = accountCache.entrySet().iterator();
            int toRemove = accountCache.size() - maxCacheSize + 100;
            
            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
                iterator.next();
                iterator.remove();
            }
            
            logger.debug("Evicted {} entries from account cache", toRemove);
        }
    }
    
    /**
     * 状态快照
     */
    public static class StateSnapshot {
        private final ChainState chainState;
        private final List<AccountState> accountStates;
        private final long timestamp;
        
        public StateSnapshot(ChainState chainState, List<AccountState> accountStates, long timestamp) {
            this.chainState = chainState;
            this.accountStates = accountStates != null ? new ArrayList<>(accountStates) : new ArrayList<>();
            this.timestamp = timestamp;
        }
        
        public ChainState getChainState() {
            return chainState;
        }
        
        public List<AccountState> getAccountStates() {
            return new ArrayList<>(accountStates);
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
