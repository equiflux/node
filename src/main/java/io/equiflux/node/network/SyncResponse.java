package io.equiflux.node.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.equiflux.node.model.Block;

import java.util.List;
import java.util.Objects;

/**
 * 同步响应
 * 
 * <p>用于响应同步请求的消息，包含区块列表和同步状态信息。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class SyncResponse {
    
    /**
     * 同步状态枚举
     */
    public enum SyncStatus {
        SUCCESS("success"),
        PARTIAL("partial"),
        FAILED("failed"),
        NOT_FOUND("not_found");
        
        private final String value;
        
        SyncStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static SyncStatus fromValue(String value) {
            for (SyncStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown sync status: " + value);
        }
    }
    
    private final SyncStatus status;
    private final List<Block> blocks;
    private final long startHeight;
    private final long endHeight;
    private final long timestamp;
    private final String errorMessage;
    
    /**
     * 构造同步响应
     * 
     * @param status 同步状态
     * @param blocks 区块列表
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @param timestamp 时间戳
     * @param errorMessage 错误消息
     */
    @JsonCreator
    public SyncResponse(@JsonProperty("status") SyncStatus status,
                       @JsonProperty("blocks") List<Block> blocks,
                       @JsonProperty("startHeight") long startHeight,
                       @JsonProperty("endHeight") long endHeight,
                       @JsonProperty("timestamp") long timestamp,
                       @JsonProperty("errorMessage") String errorMessage) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.blocks = blocks;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.timestamp = timestamp;
        this.errorMessage = errorMessage;
        
        // 验证参数
        if (startHeight < 0) {
            throw new IllegalArgumentException("Start height cannot be negative");
        }
        if (endHeight < startHeight) {
            throw new IllegalArgumentException("End height cannot be less than start height");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }
    
    /**
     * 获取同步状态
     * 
     * @return 同步状态
     */
    public SyncStatus getStatus() {
        return status;
    }
    
    /**
     * 获取区块列表
     * 
     * @return 区块列表
     */
    public List<Block> getBlocks() {
        return blocks;
    }
    
    /**
     * 获取起始高度
     * 
     * @return 起始高度
     */
    public long getStartHeight() {
        return startHeight;
    }
    
    /**
     * 获取结束高度
     * 
     * @return 结束高度
     */
    public long getEndHeight() {
        return endHeight;
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
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 获取区块数量
     * 
     * @return 区块数量
     */
    public int getBlockCount() {
        return blocks != null ? blocks.size() : 0;
    }
    
    /**
     * 检查是否成功
     * 
     * @return true如果成功，false否则
     */
    public boolean isSuccess() {
        return status == SyncStatus.SUCCESS;
    }
    
    /**
     * 检查是否部分成功
     * 
     * @return true如果部分成功，false否则
     */
    public boolean isPartial() {
        return status == SyncStatus.PARTIAL;
    }
    
    /**
     * 检查是否失败
     * 
     * @return true如果失败，false否则
     */
    public boolean isFailed() {
        return status == SyncStatus.FAILED;
    }
    
    /**
     * 检查是否未找到
     * 
     * @return true如果未找到，false否则
     */
    public boolean isNotFound() {
        return status == SyncStatus.NOT_FOUND;
    }
    
    /**
     * 检查是否有错误
     * 
     * @return true如果有错误，false否则
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
    
    /**
     * 检查是否有区块
     * 
     * @return true如果有区块，false否则
     */
    public boolean hasBlocks() {
        return blocks != null && !blocks.isEmpty();
    }
    
    /**
     * 创建成功响应
     * 
     * @param blocks 区块列表
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @return 成功响应
     */
    public static SyncResponse success(List<Block> blocks, long startHeight, long endHeight) {
        return new SyncResponse(SyncStatus.SUCCESS, blocks, startHeight, endHeight, 
                               System.currentTimeMillis(), null);
    }
    
    /**
     * 创建部分成功响应
     * 
     * @param blocks 区块列表
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @return 部分成功响应
     */
    public static SyncResponse partial(List<Block> blocks, long startHeight, long endHeight) {
        return new SyncResponse(SyncStatus.PARTIAL, blocks, startHeight, endHeight, 
                               System.currentTimeMillis(), null);
    }
    
    /**
     * 创建失败响应
     * 
     * @param errorMessage 错误消息
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @return 失败响应
     */
    public static SyncResponse failed(String errorMessage, long startHeight, long endHeight) {
        return new SyncResponse(SyncStatus.FAILED, null, startHeight, endHeight, 
                               System.currentTimeMillis(), errorMessage);
    }
    
    /**
     * 创建未找到响应
     * 
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @return 未找到响应
     */
    public static SyncResponse notFound(long startHeight, long endHeight) {
        return new SyncResponse(SyncStatus.NOT_FOUND, null, startHeight, endHeight, 
                               System.currentTimeMillis(), "Blocks not found");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SyncResponse that = (SyncResponse) obj;
        return startHeight == that.startHeight &&
               endHeight == that.endHeight &&
               timestamp == that.timestamp &&
               status == that.status &&
               Objects.equals(blocks, that.blocks) &&
               Objects.equals(errorMessage, that.errorMessage);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(status, blocks, startHeight, endHeight, timestamp, errorMessage);
    }
    
    @Override
    public String toString() {
        return "SyncResponse{" +
               "status=" + status +
               ", blockCount=" + getBlockCount() +
               ", startHeight=" + startHeight +
               ", endHeight=" + endHeight +
               ", timestamp=" + timestamp +
               ", errorMessage='" + errorMessage + '\'' +
               '}';
    }
}
