package io.equiflux.node.wallet.service;

import io.equiflux.node.exception.WalletException;
import io.equiflux.node.storage.StorageService;
import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.wallet.model.WalletInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 钱包存储服务
 * 
 * <p>负责钱包数据的存储、检索和管理。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class WalletStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletStorageService.class);
    
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    
    // 缓存
    private final Map<String, WalletInfo> walletCache = new ConcurrentHashMap<>();
    private final Map<String, String> encryptedPrivateKeyCache = new ConcurrentHashMap<>();
    
    // 线程安全
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 缓存配置
    // private final int maxCacheSize = 1000; // 暂时未使用
    
    public WalletStorageService(StorageService storageService) {
        this.storageService = storageService;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 存储钱包信息
     * 
     * @param walletInfo 钱包信息
     * @throws WalletException 钱包异常
     */
    public void storeWalletInfo(WalletInfo walletInfo) throws WalletException {
        try {
            lock.writeLock().lock();
            
            String publicKeyHex = walletInfo.getPublicKeyHex();
            StorageKey key = walletInfoKey(publicKeyHex);
            
            // 序列化钱包信息
            String json = objectMapper.writeValueAsString(walletInfo);
            StorageValue value = new StorageValue(json.getBytes(), "wallet_info");
            
            // 存储到数据库
            storageService.put(key, value);
            
            // 更新缓存
            walletCache.put(publicKeyHex, walletInfo);
            
            logger.debug("Stored wallet info: publicKey={}", publicKeyHex);
        } catch (Exception e) {
            logger.error("Failed to store wallet info", e);
            throw new WalletException("Failed to store wallet info", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取钱包信息
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 钱包信息
     * @throws WalletException 钱包异常
     */
    public Optional<WalletInfo> getWalletInfo(String publicKeyHex) throws WalletException {
        try {
            lock.readLock().lock();
            
            // 先检查缓存
            WalletInfo cached = walletCache.get(publicKeyHex);
            if (cached != null) {
                return Optional.of(cached);
            }
            
            // 从数据库获取
            StorageKey key = walletInfoKey(publicKeyHex);
            StorageValue value = storageService.get(key);
            
            if (value == null) {
                return Optional.empty();
            }
            
            // 反序列化
            String json = new String(value.getData());
            WalletInfo walletInfo = objectMapper.readValue(json, WalletInfo.class);
            
            // 更新缓存
            walletCache.put(publicKeyHex, walletInfo);
            
            logger.debug("Retrieved wallet info: publicKey={}", publicKeyHex);
            return Optional.of(walletInfo);
        } catch (Exception e) {
            logger.error("Failed to get wallet info", e);
            throw new WalletException("Failed to get wallet info", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取所有钱包信息
     * 
     * @return 钱包信息列表
     * @throws WalletException 钱包异常
     */
    public List<WalletInfo> getAllWalletInfos() throws WalletException {
        try {
            lock.readLock().lock();
            
            List<WalletInfo> wallets = new ArrayList<>();
            
            // 从数据库获取所有钱包
            Map<StorageKey, StorageValue> entries = storageService.getByNamespace("wallet");
            
            for (Map.Entry<StorageKey, StorageValue> entry : entries.entrySet()) {
                try {
                    String json = new String(entry.getValue().getData());
                    WalletInfo walletInfo = objectMapper.readValue(json, WalletInfo.class);
                    wallets.add(walletInfo);
                    
                    // 更新缓存
                    walletCache.put(walletInfo.getPublicKeyHex(), walletInfo);
                } catch (Exception e) {
                    logger.warn("Failed to deserialize wallet info: key={}", entry.getKey(), e);
                }
            }
            
            logger.debug("Retrieved {} wallet infos", wallets.size());
            return wallets;
        } catch (Exception e) {
            logger.error("Failed to get all wallet infos", e);
            throw new WalletException("Failed to get all wallet infos", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 删除钱包信息
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @throws WalletException 钱包异常
     */
    public void deleteWalletInfo(String publicKeyHex) throws WalletException {
        try {
            lock.writeLock().lock();
            
            StorageKey key = walletInfoKey(publicKeyHex);
            storageService.delete(key);
            
            // 从缓存中移除
            walletCache.remove(publicKeyHex);
            encryptedPrivateKeyCache.remove(publicKeyHex);
            
            logger.debug("Deleted wallet info: publicKey={}", publicKeyHex);
        } catch (Exception e) {
            logger.error("Failed to delete wallet info", e);
            throw new WalletException("Failed to delete wallet info", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 存储加密的私钥
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @param encryptedPrivateKey 加密的私钥
     * @throws WalletException 钱包异常
     */
    public void storeEncryptedPrivateKey(String publicKeyHex, String encryptedPrivateKey) throws WalletException {
        try {
            lock.writeLock().lock();
            
            StorageKey key = encryptedPrivateKeyKey(publicKeyHex);
            StorageValue value = new StorageValue(encryptedPrivateKey.getBytes(), "encrypted_private_key");
            
            storageService.put(key, value);
            
            // 更新缓存
            encryptedPrivateKeyCache.put(publicKeyHex, encryptedPrivateKey);
            
            logger.debug("Stored encrypted private key: publicKey={}", publicKeyHex);
        } catch (Exception e) {
            logger.error("Failed to store encrypted private key", e);
            throw new WalletException("Failed to store encrypted private key", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取加密的私钥
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 加密的私钥
     * @throws WalletException 钱包异常
     */
    public Optional<String> getEncryptedPrivateKey(String publicKeyHex) throws WalletException {
        try {
            lock.readLock().lock();
            
            // 先检查缓存
            String cached = encryptedPrivateKeyCache.get(publicKeyHex);
            if (cached != null) {
                return Optional.of(cached);
            }
            
            // 从数据库获取
            StorageKey key = encryptedPrivateKeyKey(publicKeyHex);
            StorageValue value = storageService.get(key);
            
            if (value == null) {
                return Optional.empty();
            }
            
            String encryptedPrivateKey = new String(value.getData());
            
            // 更新缓存
            encryptedPrivateKeyCache.put(publicKeyHex, encryptedPrivateKey);
            
            logger.debug("Retrieved encrypted private key: publicKey={}", publicKeyHex);
            return Optional.of(encryptedPrivateKey);
        } catch (Exception e) {
            logger.error("Failed to get encrypted private key", e);
            throw new WalletException("Failed to get encrypted private key", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 删除加密的私钥
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @throws WalletException 钱包异常
     */
    public void deleteEncryptedPrivateKey(String publicKeyHex) throws WalletException {
        try {
            lock.writeLock().lock();
            
            StorageKey key = encryptedPrivateKeyKey(publicKeyHex);
            storageService.delete(key);
            
            // 从缓存中移除
            encryptedPrivateKeyCache.remove(publicKeyHex);
            
            logger.debug("Deleted encrypted private key: publicKey={}", publicKeyHex);
        } catch (Exception e) {
            logger.error("Failed to delete encrypted private key", e);
            throw new WalletException("Failed to delete encrypted private key", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 检查钱包是否存在
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return true如果存在，false否则
     * @throws WalletException 钱包异常
     */
    public boolean walletExists(String publicKeyHex) throws WalletException {
        try {
            lock.readLock().lock();
            
            // 先检查缓存
            if (walletCache.containsKey(publicKeyHex)) {
                return true;
            }
            
            // 检查数据库
            StorageKey key = walletInfoKey(publicKeyHex);
            return storageService.exists(key);
        } catch (Exception e) {
            logger.error("Failed to check wallet existence", e);
            throw new WalletException("Failed to check wallet existence", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 更新钱包信息
     * 
     * @param walletInfo 钱包信息
     * @throws WalletException 钱包异常
     */
    public void updateWalletInfo(WalletInfo walletInfo) throws WalletException {
        try {
            lock.writeLock().lock();
            
            String publicKeyHex = walletInfo.getPublicKeyHex();
            
            // 检查钱包是否存在
            if (!walletExists(publicKeyHex)) {
                throw new WalletException("Wallet does not exist: " + publicKeyHex);
            }
            
            // 存储更新后的钱包信息
            storeWalletInfo(walletInfo);
            
            logger.debug("Updated wallet info: publicKey={}", publicKeyHex);
        } catch (Exception e) {
            logger.error("Failed to update wallet info", e);
            throw new WalletException("Failed to update wallet info", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 清理缓存
     */
    public void clearCache() {
        try {
            lock.writeLock().lock();
            
            walletCache.clear();
            encryptedPrivateKeyCache.clear();
            
            logger.debug("Cleared wallet cache");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 创建钱包信息存储键
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 存储键
     */
    private StorageKey walletInfoKey(String publicKeyHex) {
        return new StorageKey("wallet", "info:" + publicKeyHex);
    }
    
    /**
     * 创建加密私钥存储键
     * 
     * @param publicKeyHex 公钥十六进制字符串
     * @return 存储键
     */
    private StorageKey encryptedPrivateKeyKey(String publicKeyHex) {
        return new StorageKey("wallet", "private_key:" + publicKeyHex);
    }
}
