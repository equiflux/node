package io.equiflux.node.storage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 存储统计信息
 * 
 * <p>表示存储系统的统计信息，用于监控和性能分析。
 * 
 * <p>统计信息包括：
 * <ul>
 *   <li>总键数量</li>
 *   <li>总数据大小</li>
 *   <li>各命名空间的数据统计</li>
 *   <li>读写操作统计</li>
 *   <li>缓存命中率</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class StorageStats {
    
    private final long totalKeys;
    private final long totalDataSize;
    private final long readOperations;
    private final long writeOperations;
    private final long deleteOperations;
    private final double cacheHitRate;
    private final long lastUpdateTimestamp;
    
    /**
     * 构造存储统计信息
     * 
     * @param totalKeys 总键数量
     * @param totalDataSize 总数据大小
     * @param readOperations 读操作次数
     * @param writeOperations 写操作次数
     * @param deleteOperations 删除操作次数
     * @param cacheHitRate 缓存命中率
     * @param lastUpdateTimestamp 最后更新时间戳
     */
    @JsonCreator
    public StorageStats(@JsonProperty("totalKeys") long totalKeys,
                        @JsonProperty("totalDataSize") long totalDataSize,
                        @JsonProperty("readOperations") long readOperations,
                        @JsonProperty("writeOperations") long writeOperations,
                        @JsonProperty("deleteOperations") long deleteOperations,
                        @JsonProperty("cacheHitRate") double cacheHitRate,
                        @JsonProperty("lastUpdateTimestamp") long lastUpdateTimestamp) {
        this.totalKeys = totalKeys;
        this.totalDataSize = totalDataSize;
        this.readOperations = readOperations;
        this.writeOperations = writeOperations;
        this.deleteOperations = deleteOperations;
        this.cacheHitRate = cacheHitRate;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        
        // 验证参数
        if (totalKeys < 0) {
            throw new IllegalArgumentException("Total keys cannot be negative");
        }
        if (totalDataSize < 0) {
            throw new IllegalArgumentException("Total data size cannot be negative");
        }
        if (readOperations < 0) {
            throw new IllegalArgumentException("Read operations cannot be negative");
        }
        if (writeOperations < 0) {
            throw new IllegalArgumentException("Write operations cannot be negative");
        }
        if (deleteOperations < 0) {
            throw new IllegalArgumentException("Delete operations cannot be negative");
        }
        if (cacheHitRate < 0.0 || cacheHitRate > 1.0) {
            throw new IllegalArgumentException("Cache hit rate must be between 0.0 and 1.0");
        }
        if (lastUpdateTimestamp <= 0) {
            throw new IllegalArgumentException("Last update timestamp must be positive");
        }
    }
    
    /**
     * 构造空的存储统计信息
     */
    public StorageStats() {
        this(0, 0, 0, 0, 0, 0.0, System.currentTimeMillis());
    }
    
    /**
     * 获取总键数量
     * 
     * @return 总键数量
     */
    public long getTotalKeys() {
        return totalKeys;
    }
    
    /**
     * 获取总数据大小
     * 
     * @return 总数据大小（字节）
     */
    public long getTotalDataSize() {
        return totalDataSize;
    }
    
    /**
     * 获取读操作次数
     * 
     * @return 读操作次数
     */
    public long getReadOperations() {
        return readOperations;
    }
    
    /**
     * 获取写操作次数
     * 
     * @return 写操作次数
     */
    public long getWriteOperations() {
        return writeOperations;
    }
    
    /**
     * 获取删除操作次数
     * 
     * @return 删除操作次数
     */
    public long getDeleteOperations() {
        return deleteOperations;
    }
    
    /**
     * 获取缓存命中率
     * 
     * @return 缓存命中率（0.0-1.0）
     */
    public double getCacheHitRate() {
        return cacheHitRate;
    }
    
    /**
     * 获取最后更新时间戳
     * 
     * @return 最后更新时间戳
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
    
    /**
     * 获取总操作次数
     * 
     * @return 总操作次数
     */
    public long getTotalOperations() {
        return readOperations + writeOperations + deleteOperations;
    }
    
    /**
     * 获取平均数据大小
     * 
     * @return 平均数据大小（字节）
     */
    public double getAverageDataSize() {
        return totalKeys > 0 ? (double) totalDataSize / totalKeys : 0.0;
    }
    
    /**
     * 获取数据大小（MB）
     * 
     * @return 数据大小（MB）
     */
    public double getTotalDataSizeMB() {
        return totalDataSize / (1024.0 * 1024.0);
    }
    
    /**
     * 获取数据大小（GB）
     * 
     * @return 数据大小（GB）
     */
    public double getTotalDataSizeGB() {
        return totalDataSize / (1024.0 * 1024.0 * 1024.0);
    }
    
    /**
     * 创建更新后的统计信息
     * 
     * @param newTotalKeys 新总键数量
     * @param newTotalDataSize 新总数据大小
     * @param newReadOperations 新读操作次数
     * @param newWriteOperations 新写操作次数
     * @param newDeleteOperations 新删除操作次数
     * @param newCacheHitRate 新缓存命中率
     * @return 更新后的统计信息
     */
    public StorageStats update(long newTotalKeys, long newTotalDataSize, 
                              long newReadOperations, long newWriteOperations, 
                              long newDeleteOperations, double newCacheHitRate) {
        return new StorageStats(newTotalKeys, newTotalDataSize, newReadOperations, 
                               newWriteOperations, newDeleteOperations, newCacheHitRate, 
                               System.currentTimeMillis());
    }
    
    /**
     * 创建键数量更新后的统计信息
     * 
     * @param keyChange 键数量变化
     * @return 更新后的统计信息
     */
    public StorageStats updateKeys(long keyChange) {
        long newTotalKeys = totalKeys + keyChange;
        if (newTotalKeys < 0) {
            throw new IllegalArgumentException("Total keys cannot be negative");
        }
        return new StorageStats(newTotalKeys, totalDataSize, readOperations, 
                               writeOperations, deleteOperations, cacheHitRate, 
                               System.currentTimeMillis());
    }
    
    /**
     * 创建数据大小更新后的统计信息
     * 
     * @param sizeChange 数据大小变化
     * @return 更新后的统计信息
     */
    public StorageStats updateDataSize(long sizeChange) {
        long newTotalDataSize = totalDataSize + sizeChange;
        if (newTotalDataSize < 0) {
            throw new IllegalArgumentException("Total data size cannot be negative");
        }
        return new StorageStats(totalKeys, newTotalDataSize, readOperations, 
                               writeOperations, deleteOperations, cacheHitRate, 
                               System.currentTimeMillis());
    }
    
    /**
     * 创建读操作更新后的统计信息
     * 
     * @return 更新后的统计信息
     */
    public StorageStats incrementReadOperations() {
        return new StorageStats(totalKeys, totalDataSize, readOperations + 1, 
                               writeOperations, deleteOperations, cacheHitRate, 
                               System.currentTimeMillis());
    }
    
    /**
     * 创建写操作更新后的统计信息
     * 
     * @return 更新后的统计信息
     */
    public StorageStats incrementWriteOperations() {
        return new StorageStats(totalKeys, totalDataSize, readOperations, 
                               writeOperations + 1, deleteOperations, cacheHitRate, 
                               System.currentTimeMillis());
    }
    
    /**
     * 创建删除操作更新后的统计信息
     * 
     * @return 更新后的统计信息
     */
    public StorageStats incrementDeleteOperations() {
        return new StorageStats(totalKeys, totalDataSize, readOperations, 
                               writeOperations, deleteOperations + 1, cacheHitRate, 
                               System.currentTimeMillis());
    }
    
    /**
     * 创建缓存命中率更新后的统计信息
     * 
     * @param newCacheHitRate 新缓存命中率
     * @return 更新后的统计信息
     */
    public StorageStats updateCacheHitRate(double newCacheHitRate) {
        return new StorageStats(totalKeys, totalDataSize, readOperations, 
                               writeOperations, deleteOperations, newCacheHitRate, 
                               System.currentTimeMillis());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StorageStats that = (StorageStats) obj;
        return totalKeys == that.totalKeys &&
               totalDataSize == that.totalDataSize &&
               readOperations == that.readOperations &&
               writeOperations == that.writeOperations &&
               deleteOperations == that.deleteOperations &&
               Double.compare(that.cacheHitRate, cacheHitRate) == 0 &&
               lastUpdateTimestamp == that.lastUpdateTimestamp;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalKeys, totalDataSize, readOperations, writeOperations, 
                           deleteOperations, cacheHitRate, lastUpdateTimestamp);
    }
    
    @Override
    public String toString() {
        return "StorageStats{" +
               "totalKeys=" + totalKeys +
               ", totalDataSize=" + totalDataSize +
               ", readOperations=" + readOperations +
               ", writeOperations=" + writeOperations +
               ", deleteOperations=" + deleteOperations +
               ", cacheHitRate=" + cacheHitRate +
               ", lastUpdateTimestamp=" + lastUpdateTimestamp +
               '}';
    }
}
