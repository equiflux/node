package io.equiflux.node.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 消息压缩服务
 * 
 * <p>负责网络消息的压缩和解压缩，减少网络传输的数据量。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>GZIP压缩和解压缩</li>
 *   <li>压缩级别配置</li>
 *   <li>压缩效果统计</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class MessageCompressionService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageCompressionService.class);
    
    @Autowired
    private NetworkConfig networkConfig;
    
    // 统计信息
    private long totalCompressedBytes = 0;
    private long totalOriginalBytes = 0;
    private long compressionCount = 0;
    private long decompressionCount = 0;
    
    @PostConstruct
    public void init() {
        logger.info("初始化消息压缩服务，压缩级别: {}", networkConfig.getCompressionLevel());
    }
    
    /**
     * 压缩消息数据
     * 
     * @param data 原始数据
     * @return 压缩后的数据
     */
    public byte[] compress(byte[] data) {
        if (!networkConfig.isEnableCompression()) {
            return data; // 如果未启用压缩，直接返回原数据
        }
        
        if (data == null || data.length == 0) {
            return data;
        }
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos) {
                {
                    def.setLevel(networkConfig.getCompressionLevel());
                }
            };
            
            gzos.write(data);
            gzos.close();
            
            byte[] compressedData = baos.toByteArray();
            
            // 更新统计信息
            totalOriginalBytes += data.length;
            totalCompressedBytes += compressedData.length;
            compressionCount++;
            
            logger.debug("消息压缩完成，原始大小: {} 字节，压缩后: {} 字节，压缩率: {:.2f}%", 
                        data.length, compressedData.length, 
                        (1.0 - (double) compressedData.length / data.length) * 100);
            
            return compressedData;
            
        } catch (IOException e) {
            logger.error("消息压缩失败", e);
            return data; // 压缩失败时返回原数据
        }
    }
    
    /**
     * 解压缩消息数据
     * 
     * @param compressedData 压缩的数据
     * @return 解压缩后的数据
     */
    public byte[] decompress(byte[] compressedData) {
        if (!networkConfig.isEnableCompression()) {
            return compressedData; // 如果未启用压缩，直接返回原数据
        }
        
        if (compressedData == null || compressedData.length == 0) {
            return compressedData;
        }
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            GZIPInputStream gzis = new GZIPInputStream(bais);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            
            while ((len = gzis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            
            gzis.close();
            baos.close();
            
            byte[] decompressedData = baos.toByteArray();
            
            // 更新统计信息
            decompressionCount++;
            
            logger.debug("消息解压缩完成，压缩大小: {} 字节，解压后: {} 字节", 
                        compressedData.length, decompressedData.length);
            
            return decompressedData;
            
        } catch (IOException e) {
            logger.error("消息解压缩失败", e);
            return compressedData; // 解压缩失败时返回原数据
        }
    }
    
    /**
     * 检查数据是否已压缩
     * 
     * @param data 数据
     * @return true如果已压缩，false否则
     */
    public boolean isCompressed(byte[] data) {
        if (data == null || data.length < 2) {
            return false;
        }
        
        // 检查GZIP魔数
        return (data[0] & 0xFF) == 0x1f && (data[1] & 0xFF) == 0x8b;
    }
    
    /**
     * 获取压缩统计信息
     * 
     * @return 压缩统计信息
     */
    public CompressionStats getStats() {
        return new CompressionStats(
            totalOriginalBytes,
            totalCompressedBytes,
            compressionCount,
            decompressionCount
        );
    }
    
    /**
     * 压缩统计信息
     */
    public static class CompressionStats {
        private final long totalOriginalBytes;
        private final long totalCompressedBytes;
        private final long compressionCount;
        private final long decompressionCount;
        
        public CompressionStats(long totalOriginalBytes, long totalCompressedBytes,
                              long compressionCount, long decompressionCount) {
            this.totalOriginalBytes = totalOriginalBytes;
            this.totalCompressedBytes = totalCompressedBytes;
            this.compressionCount = compressionCount;
            this.decompressionCount = decompressionCount;
        }
        
        public long getTotalOriginalBytes() {
            return totalOriginalBytes;
        }
        
        public long getTotalCompressedBytes() {
            return totalCompressedBytes;
        }
        
        public long getCompressionCount() {
            return compressionCount;
        }
        
        public long getDecompressionCount() {
            return decompressionCount;
        }
        
        public double getCompressionRatio() {
            if (totalOriginalBytes == 0) {
                return 0.0;
            }
            return (double) totalCompressedBytes / totalOriginalBytes;
        }
        
        public double getSpaceSaved() {
            if (totalOriginalBytes == 0) {
                return 0.0;
            }
            return 1.0 - getCompressionRatio();
        }
        
        public double getAverageCompressionRatio() {
            if (compressionCount == 0) {
                return 0.0;
            }
            return (double) totalCompressedBytes / compressionCount / 
                   (totalOriginalBytes / compressionCount);
        }
        
        @Override
        public String toString() {
            return "CompressionStats{" +
                   "totalOriginalBytes=" + totalOriginalBytes +
                   ", totalCompressedBytes=" + totalCompressedBytes +
                   ", compressionCount=" + compressionCount +
                   ", decompressionCount=" + decompressionCount +
                   ", compressionRatio=" + String.format("%.2f%%", getCompressionRatio() * 100) +
                   ", spaceSaved=" + String.format("%.2f%%", getSpaceSaved() * 100) +
                   '}';
        }
    }
}
