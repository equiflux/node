package io.equiflux.node.storage;

import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.exception.StorageException;

import java.util.List;
import java.util.Map;

/**
 * 存储服务接口
 * 
 * <p>定义存储服务的基本操作接口，支持键值对的存储、检索和删除。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>基本的CRUD操作</li>
 *   <li>批量操作支持</li>
 *   <li>命名空间查询</li>
 *   <li>统计信息获取</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public interface StorageService {
    
    /**
     * 存储键值对
     * 
     * @param key 存储键
     * @param value 存储值
     * @throws StorageException 存储异常
     */
    void put(StorageKey key, StorageValue value) throws StorageException;
    
    /**
     * 获取存储值
     * 
     * @param key 存储键
     * @return 存储值，如果不存在返回null
     * @throws StorageException 存储异常
     */
    StorageValue get(StorageKey key) throws StorageException;
    
    /**
     * 删除存储键值对
     * 
     * @param key 存储键
     * @throws StorageException 存储异常
     */
    void delete(StorageKey key) throws StorageException;
    
    /**
     * 检查键是否存在
     * 
     * @param key 存储键
     * @return true如果存在，false否则
     * @throws StorageException 存储异常
     */
    boolean exists(StorageKey key) throws StorageException;
    
    /**
     * 批量存储
     * 
     * @param entries 键值对映射
     * @throws StorageException 存储异常
     */
    void putBatch(Map<StorageKey, StorageValue> entries) throws StorageException;
    
    /**
     * 批量获取
     * 
     * @param keys 存储键列表
     * @return 键值对映射
     * @throws StorageException 存储异常
     */
    Map<StorageKey, StorageValue> getBatch(List<StorageKey> keys) throws StorageException;
    
    /**
     * 按命名空间查询
     * 
     * @param namespace 命名空间
     * @return 该命名空间下的所有键值对
     * @throws StorageException 存储异常
     */
    Map<StorageKey, StorageValue> getByNamespace(String namespace) throws StorageException;
}
