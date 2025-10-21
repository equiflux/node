package io.equiflux.node.storage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 存储值
 * 
 * <p>表示存储系统中的值，包含数据和元数据信息。
 * 
 * <p>存储值结构：
 * <ul>
 *   <li>数据内容（字节数组）</li>
 *   <li>创建时间戳</li>
 *   <li>最后更新时间戳</li>
 *   <li>版本号</li>
 *   <li>数据类型</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class StorageValue {
    
    private final byte[] data;
    private final long createTimestamp;
    private final long lastUpdateTimestamp;
    private final long version;
    private final String dataType;
    
    /**
     * 构造存储值
     * 
     * @param data 数据内容
     * @param createTimestamp 创建时间戳
     * @param lastUpdateTimestamp 最后更新时间戳
     * @param version 版本号
     * @param dataType 数据类型
     */
    @JsonCreator
    public StorageValue(@JsonProperty("data") byte[] data,
                         @JsonProperty("createTimestamp") long createTimestamp,
                         @JsonProperty("lastUpdateTimestamp") long lastUpdateTimestamp,
                         @JsonProperty("version") long version,
                         @JsonProperty("dataType") String dataType) {
        this.data = Objects.requireNonNull(data, "Data cannot be null");
        this.createTimestamp = createTimestamp;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.version = version;
        this.dataType = Objects.requireNonNull(dataType, "Data type cannot be null");
        
        // 验证参数
        if (createTimestamp <= 0) {
            throw new IllegalArgumentException("Create timestamp must be positive");
        }
        if (lastUpdateTimestamp <= 0) {
            throw new IllegalArgumentException("Last update timestamp must be positive");
        }
        if (version < 0) {
            throw new IllegalArgumentException("Version cannot be negative");
        }
        if (dataType.trim().isEmpty()) {
            throw new IllegalArgumentException("Data type cannot be empty");
        }
    }
    
    /**
     * 构造存储值（使用当前时间戳）
     * 
     * @param data 数据内容
     * @param dataType 数据类型
     */
    public StorageValue(byte[] data, String dataType) {
        this(data, System.currentTimeMillis(), System.currentTimeMillis(), 0, dataType);
    }
    
    /**
     * 获取数据内容
     * 
     * @return 数据内容
     */
    public byte[] getData() {
        return data.clone();
    }
    
    /**
     * 获取创建时间戳
     * 
     * @return 创建时间戳
     */
    public long getCreateTimestamp() {
        return createTimestamp;
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
     * 获取版本号
     * 
     * @return 版本号
     */
    public long getVersion() {
        return version;
    }
    
    /**
     * 获取数据类型
     * 
     * @return 数据类型
     */
    public String getDataType() {
        return dataType;
    }
    
    /**
     * 获取数据大小
     * 
     * @return 数据大小（字节）
     */
    public int getDataSize() {
        return data.length;
    }
    
    /**
     * 检查数据是否为空
     * 
     * @return true如果数据为空，false否则
     */
    public boolean isEmpty() {
        return data.length == 0;
    }
    
    /**
     * 创建更新后的存储值
     * 
     * @param newData 新数据
     * @return 更新后的存储值
     */
    public StorageValue update(byte[] newData) {
        return new StorageValue(newData, createTimestamp, System.currentTimeMillis(), 
                               version + 1, dataType);
    }
    
    /**
     * 创建更新后的存储值（指定数据类型）
     * 
     * @param newData 新数据
     * @param newDataType 新数据类型
     * @return 更新后的存储值
     */
    public StorageValue update(byte[] newData, String newDataType) {
        return new StorageValue(newData, createTimestamp, System.currentTimeMillis(), 
                               version + 1, newDataType);
    }
    
    /**
     * 创建版本更新后的存储值
     * 
     * @return 版本更新后的存储值
     */
    public StorageValue incrementVersion() {
        return new StorageValue(data, createTimestamp, System.currentTimeMillis(), 
                               version + 1, dataType);
    }
    
    /**
     * 检查存储值是否过期
     * 
     * @param maxAgeMs 最大年龄（毫秒）
     * @return true如果过期，false否则
     */
    public boolean isExpired(long maxAgeMs) {
        return System.currentTimeMillis() - lastUpdateTimestamp > maxAgeMs;
    }
    
    /**
     * 获取数据的十六进制字符串表示
     * 
     * @return 数据的十六进制字符串
     */
    public String getDataHex() {
        return io.equiflux.node.crypto.HashUtils.toHexString(data);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StorageValue that = (StorageValue) obj;
        return createTimestamp == that.createTimestamp &&
               lastUpdateTimestamp == that.lastUpdateTimestamp &&
               version == that.version &&
               Objects.equals(dataType, that.dataType) &&
               java.util.Arrays.equals(data, that.data);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(java.util.Arrays.hashCode(data), createTimestamp, 
                           lastUpdateTimestamp, version, dataType);
    }
    
    @Override
    public String toString() {
        return "StorageValue{" +
               "dataSize=" + data.length +
               ", createTimestamp=" + createTimestamp +
               ", lastUpdateTimestamp=" + lastUpdateTimestamp +
               ", version=" + version +
               ", dataType='" + dataType + '\'' +
               '}';
    }
}
