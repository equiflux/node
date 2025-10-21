package io.equiflux.node.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * 同步请求
 * 
 * <p>用于请求同步区块数据的消息，包含起始高度、结束高度等信息。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class SyncRequest {
    
    private final long startHeight;
    private final long endHeight;
    private final int maxBlocks;
    private final long timestamp;
    
    /**
     * 构造同步请求
     * 
     * @param startHeight 起始区块高度
     * @param endHeight 结束区块高度
     * @param maxBlocks 最大区块数量
     * @param timestamp 时间戳
     */
    @JsonCreator
    public SyncRequest(@JsonProperty("startHeight") long startHeight,
                       @JsonProperty("endHeight") long endHeight,
                       @JsonProperty("maxBlocks") int maxBlocks,
                       @JsonProperty("timestamp") long timestamp) {
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.maxBlocks = maxBlocks;
        this.timestamp = timestamp;
        
        // 验证参数
        if (startHeight < 0) {
            throw new IllegalArgumentException("Start height cannot be negative");
        }
        if (endHeight < startHeight) {
            throw new IllegalArgumentException("End height cannot be less than start height");
        }
        if (maxBlocks <= 0) {
            throw new IllegalArgumentException("Max blocks must be positive");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }
    
    /**
     * 获取起始区块高度
     * 
     * @return 起始区块高度
     */
    public long getStartHeight() {
        return startHeight;
    }
    
    /**
     * 获取结束区块高度
     * 
     * @return 结束区块高度
     */
    public long getEndHeight() {
        return endHeight;
    }
    
    /**
     * 获取最大区块数量
     * 
     * @return 最大区块数量
     */
    public int getMaxBlocks() {
        return maxBlocks;
    }
    
    /**
     * 获取时间戳
     * 
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取请求的区块数量
     * 
     * @return 请求的区块数量
     */
    public long getRequestedBlockCount() {
        return endHeight - startHeight + 1;
    }
    
    /**
     * 检查请求是否有效
     * 
     * @return true如果有效，false否则
     */
    public boolean isValid() {
        return startHeight >= 0 && 
               endHeight >= startHeight && 
               maxBlocks > 0 && 
               timestamp > 0 &&
               getRequestedBlockCount() <= maxBlocks;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SyncRequest that = (SyncRequest) obj;
        return startHeight == that.startHeight &&
               endHeight == that.endHeight &&
               maxBlocks == that.maxBlocks &&
               timestamp == that.timestamp;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startHeight, endHeight, maxBlocks, timestamp);
    }
    
    @Override
    public String toString() {
        return "SyncRequest{" +
               "startHeight=" + startHeight +
               ", endHeight=" + endHeight +
               ", maxBlocks=" + maxBlocks +
               ", timestamp=" + timestamp +
               '}';
    }
}
