package io.equiflux.node.storage;

import io.equiflux.node.storage.model.StorageKey;
import io.equiflux.node.storage.model.StorageValue;
import io.equiflux.node.storage.model.StorageStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * RocksDB存储服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class RocksDBStorageServiceTest {
    
    @Test
    void testStorageKeyCreation() {
        // 测试存储键创建
        StorageKey key1 = new StorageKey("block", "123");
        assertThat(key1.getNamespace()).isEqualTo("block");
        assertThat(key1.getKey()).isEqualTo("123");
        assertThat(key1.getVersion()).isEqualTo(0);
        
        StorageKey key2 = new StorageKey("transaction", "abc", 1);
        assertThat(key2.getNamespace()).isEqualTo("transaction");
        assertThat(key2.getKey()).isEqualTo("abc");
        assertThat(key2.getVersion()).isEqualTo(1);
        
        assertThat(key1.getFullKey()).isEqualTo("block:123");
        assertThat(key2.getFullKey()).isEqualTo("transaction:abc:1");
    }
    
    @Test
    void testStorageKeyFromFullKey() {
        // 测试从完整键字符串解析存储键
        StorageKey key1 = StorageKey.fromFullKey("block:123");
        assertThat(key1.getNamespace()).isEqualTo("block");
        assertThat(key1.getKey()).isEqualTo("123");
        assertThat(key1.getVersion()).isEqualTo(0);
        
        StorageKey key2 = StorageKey.fromFullKey("transaction:abc:1");
        assertThat(key2.getNamespace()).isEqualTo("transaction");
        assertThat(key2.getKey()).isEqualTo("abc");
        assertThat(key2.getVersion()).isEqualTo(1);
    }
    
    @Test
    void testStorageKeyFactoryMethods() {
        // 测试存储键工厂方法
        StorageKey blockKey = StorageKey.blockKey(123);
        assertThat(blockKey.getNamespace()).isEqualTo("block");
        assertThat(blockKey.getKey()).isEqualTo("123");
        
        StorageKey txKey = StorageKey.transactionKey("abc123");
        assertThat(txKey.getNamespace()).isEqualTo("transaction");
        assertThat(txKey.getKey()).isEqualTo("abc123");
        
        StorageKey accountKey = StorageKey.accountKey("def456");
        assertThat(accountKey.getNamespace()).isEqualTo("account");
        assertThat(accountKey.getKey()).isEqualTo("def456");
    }
    
    @Test
    void testStorageValueCreation() {
        // 测试存储值创建
        byte[] data = "test data".getBytes();
        StorageValue value = new StorageValue(data, "TestType");
        
        assertThat(value.getData()).isEqualTo(data);
        assertThat(value.getDataType()).isEqualTo("TestType");
        assertThat(value.getDataSize()).isEqualTo(data.length);
        assertThat(value.isEmpty()).isFalse();
    }
    
    @Test
    void testStorageValueUpdate() {
        // 测试存储值更新
        byte[] originalData = "original".getBytes();
        StorageValue original = new StorageValue(originalData, "OriginalType");
        
        byte[] newData = "updated".getBytes();
        StorageValue updated = original.update(newData);
        
        assertThat(updated.getData()).isEqualTo(newData);
        assertThat(updated.getDataType()).isEqualTo("OriginalType");
        assertThat(updated.getVersion()).isEqualTo(original.getVersion() + 1);
        assertThat(updated.getCreateTimestamp()).isEqualTo(original.getCreateTimestamp());
    }
    
    @Test
    void testStorageStatsCreation() {
        // 测试存储统计信息创建
        StorageStats stats = new StorageStats();
        
        assertThat(stats.getTotalKeys()).isEqualTo(0);
        assertThat(stats.getTotalDataSize()).isEqualTo(0);
        assertThat(stats.getReadOperations()).isEqualTo(0);
        assertThat(stats.getWriteOperations()).isEqualTo(0);
        assertThat(stats.getDeleteOperations()).isEqualTo(0);
        assertThat(stats.getCacheHitRate()).isEqualTo(0.0);
    }
    
    @Test
    void testStorageStatsUpdate() {
        // 测试存储统计信息更新
        StorageStats stats = new StorageStats();
        
        StorageStats updated = stats.incrementReadOperations()
                                  .incrementWriteOperations()
                                  .updateKeys(10)
                                  .updateDataSize(1024);
        
        assertThat(updated.getReadOperations()).isEqualTo(1);
        assertThat(updated.getWriteOperations()).isEqualTo(1);
        assertThat(updated.getTotalKeys()).isEqualTo(10);
        assertThat(updated.getTotalDataSize()).isEqualTo(1024);
        assertThat(updated.getTotalOperations()).isEqualTo(2);
    }
    
    @Test
    void testStorageStatsDataSizeConversion() {
        // 测试存储统计信息数据大小转换
        StorageStats stats = new StorageStats(0, 1024 * 1024, 0, 0, 0, 0.0, System.currentTimeMillis());
        
        assertThat(stats.getTotalDataSizeMB()).isEqualTo(1.0);
        assertThat(stats.getTotalDataSizeGB()).isEqualTo(1.0 / 1024.0);
    }
    
    @Test
    void testStorageKeyValidation() {
        // 测试存储键验证
        assertThatThrownBy(() -> new StorageKey("", "key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Namespace cannot be empty");
        
        assertThatThrownBy(() -> new StorageKey("namespace", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Key cannot be empty");
        
        assertThatThrownBy(() -> new StorageKey("namespace", "key", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Version cannot be negative");
    }
    
    @Test
    void testStorageValueValidation() {
        // 测试存储值验证
        assertThatThrownBy(() -> new StorageValue(null, "type"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Data cannot be null");
        
        assertThatThrownBy(() -> new StorageValue(new byte[0], ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data type cannot be empty");
        
        assertThatThrownBy(() -> new StorageValue(new byte[0], 0, 0, -1, "type"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Create timestamp must be positive");
    }
    
    @Test
    void testStorageStatsValidation() {
        // 测试存储统计信息验证
        assertThatThrownBy(() -> new StorageStats(-1, 0, 0, 0, 0, 0.0, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Total keys cannot be negative");
        
        assertThatThrownBy(() -> new StorageStats(0, -1, 0, 0, 0, 0.0, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Total data size cannot be negative");
        
        assertThatThrownBy(() -> new StorageStats(0, 0, 0, 0, 0, 1.5, System.currentTimeMillis()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cache hit rate must be between 0.0 and 1.0");
    }
    
    @Test
    void testStorageKeyEquality() {
        // 测试存储键相等性
        StorageKey key1 = new StorageKey("block", "123");
        StorageKey key2 = new StorageKey("block", "123");
        StorageKey key3 = new StorageKey("block", "456");
        
        assertThat(key1).isEqualTo(key2);
        assertThat(key1).isNotEqualTo(key3);
        assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }
    
    @Test
    void testStorageValueEquality() {
        // 测试存储值相等性
        byte[] data1 = "test".getBytes();
        byte[] data2 = "test".getBytes();
        byte[] data3 = "different".getBytes();
        
        StorageValue value1 = new StorageValue(data1, "type");
        StorageValue value2 = new StorageValue(data2, "type");
        StorageValue value3 = new StorageValue(data3, "type");
        
        assertThat(value1).isEqualTo(value2);
        assertThat(value1).isNotEqualTo(value3);
        assertThat(value1.hashCode()).isEqualTo(value2.hashCode());
    }
    
    @Test
    void testStorageStatsEquality() {
        // 测试存储统计信息相等性
        StorageStats stats1 = new StorageStats(10, 1024, 5, 3, 2, 0.8, System.currentTimeMillis());
        StorageStats stats2 = new StorageStats(10, 1024, 5, 3, 2, 0.8, stats1.getLastUpdateTimestamp());
        
        assertThat(stats1).isEqualTo(stats2);
        assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
    }
}
