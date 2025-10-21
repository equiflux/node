package io.equiflux.node.storage;

import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;
import io.equiflux.node.model.VRFProof;
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
 * 区块存储服务测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
class BlockStorageServiceTest {
    
    @Mock
    private StorageService storageService;
    
    private BlockStorageService blockStorageService;
    
    @BeforeEach
    void setUp() {
        blockStorageService = new BlockStorageService(storageService);
    }
    
    @Test
    void testStoreBlock() throws StorageException {
        // 准备测试数据
        Block block = createTestBlock(1);
        
        // 执行测试
        blockStorageService.storeBlock(block);
        
        // 验证结果
        verify(storageService).put(eq(StorageKey.blockKey(1)), any(StorageValue.class));
        verify(storageService).put(eq(StorageKey.blockHashKey(block.getHashHex())), any(StorageValue.class));
        verify(storageService).put(eq(new StorageKey("block_index", "1")), any(StorageValue.class));
        verify(storageService).put(eq(new StorageKey("block", "latest")), any(StorageValue.class));
    }
    
    @Test
    void testGetBlockByHeight() throws StorageException {
        // 准备测试数据
        Block block = createTestBlock(1);
        StorageValue blockValue = new StorageValue(serializeBlock(block), "Block");
        
        when(storageService.get(StorageKey.blockKey(1))).thenReturn(blockValue);
        
        // 执行测试
        Block result = blockStorageService.getBlockByHeight(1);
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getHeight()).isEqualTo(1);
        assertThat(result.getHashHex()).isEqualTo(block.getHashHex());
    }
    
    @Test
    void testGetBlockByHeightNotFound() throws StorageException {
        // 准备测试数据
        when(storageService.get(StorageKey.blockKey(1))).thenReturn(null);
        
        // 执行测试
        Block result = blockStorageService.getBlockByHeight(1);
        
        // 验证结果
        assertThat(result).isNull();
    }
    
    @Test
    void testGetBlockByHash() throws StorageException {
        // 准备测试数据
        Block block = createTestBlock(1);
        StorageValue blockValue = new StorageValue(serializeBlock(block), "Block");
        StorageValue heightValue = new StorageValue("1".getBytes(), "Height");
        
        when(storageService.get(StorageKey.blockHashKey(block.getHashHex()))).thenReturn(heightValue);
        when(storageService.get(StorageKey.blockKey(1))).thenReturn(blockValue);
        
        // 执行测试
        Block result = blockStorageService.getBlockByHash(block.getHashHex());
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getHeight()).isEqualTo(1);
        assertThat(result.getHashHex()).isEqualTo(block.getHashHex());
    }
    
    @Test
    void testGetBlockByHashNotFound() throws StorageException {
        // 准备测试数据
        String hash = "testhash";
        when(storageService.get(StorageKey.blockHashKey(hash))).thenReturn(null);
        
        // 执行测试
        Block result = blockStorageService.getBlockByHash(hash);
        
        // 验证结果
        assertThat(result).isNull();
    }
    
    @Test
    void testGetLatestBlock() throws StorageException {
        // 准备测试数据
        Block block = createTestBlock(1);
        StorageValue latestValue = new StorageValue(serializeBlock(block), "Block");
        
        when(storageService.get(new StorageKey("block", "latest"))).thenReturn(latestValue);
        
        // 执行测试
        Block result = blockStorageService.getLatestBlock();
        
        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getHeight()).isEqualTo(1);
    }
    
    @Test
    void testGetCurrentHeight() throws StorageException {
        // 准备测试数据
        Block block = createTestBlock(5);
        StorageValue latestValue = new StorageValue(serializeBlock(block), "Block");
        
        when(storageService.get(new StorageKey("block", "latest"))).thenReturn(latestValue);
        
        // 执行测试
        long height = blockStorageService.getCurrentHeight();
        
        // 验证结果
        assertThat(height).isEqualTo(5);
    }
    
    @Test
    void testGetCurrentHeightNoBlocks() throws StorageException {
        // 准备测试数据
        when(storageService.get(new StorageKey("block", "latest"))).thenReturn(null);
        
        // 执行测试
        long height = blockStorageService.getCurrentHeight();
        
        // 验证结果
        assertThat(height).isEqualTo(-1);
    }
    
    @Test
    void testBlockExists() throws StorageException {
        // 准备测试数据
        when(storageService.exists(StorageKey.blockKey(1))).thenReturn(true);
        
        // 执行测试
        boolean exists = blockStorageService.blockExists(1);
        
        // 验证结果
        assertThat(exists).isTrue();
    }
    
    @Test
    void testBlockExistsByHash() throws StorageException {
        // 准备测试数据
        String hash = "testhash";
        when(storageService.exists(StorageKey.blockHashKey(hash))).thenReturn(true);
        
        // 执行测试
        boolean exists = blockStorageService.blockExistsByHash(hash);
        
        // 验证结果
        assertThat(exists).isTrue();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testStoreBlocks() throws StorageException {
        // 准备测试数据
        List<Block> blocks = Arrays.asList(
            createTestBlock(1),
            createTestBlock(2),
            createTestBlock(3)
        );
        
        // 执行测试
        blockStorageService.storeBlocks(blocks);
        
        // 验证结果
        verify(storageService, times(1)).putBatch(any(Map.class));
        verify(storageService, times(1)).put(eq(new StorageKey("block", "latest")), any(StorageValue.class));
    }
    
    @Test
    void testGetBlocks() throws StorageException {
        // 准备测试数据
        Block block1 = createTestBlock(1);
        Block block2 = createTestBlock(2);
        Block block3 = createTestBlock(3);
        
        when(storageService.get(StorageKey.blockKey(1))).thenReturn(new StorageValue(serializeBlock(block1), "Block"));
        when(storageService.get(StorageKey.blockKey(2))).thenReturn(new StorageValue(serializeBlock(block2), "Block"));
        when(storageService.get(StorageKey.blockKey(3))).thenReturn(new StorageValue(serializeBlock(block3), "Block"));
        
        // 执行测试
        List<Block> result = blockStorageService.getBlocks(1, 3);
        
        // 验证结果
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getHeight()).isEqualTo(1);
        assertThat(result.get(1).getHeight()).isEqualTo(2);
        assertThat(result.get(2).getHeight()).isEqualTo(3);
    }
    
    @Test
    void testGetRecentBlocks() throws StorageException {
        // 准备测试数据
        Block block1 = createTestBlock(1);
        Block block2 = createTestBlock(2);
        Block block3 = createTestBlock(3);
        
        when(storageService.get(new StorageKey("block", "latest"))).thenReturn(new StorageValue(serializeBlock(block3), "Block"));
        when(storageService.get(StorageKey.blockKey(1))).thenReturn(new StorageValue(serializeBlock(block1), "Block"));
        when(storageService.get(StorageKey.blockKey(2))).thenReturn(new StorageValue(serializeBlock(block2), "Block"));
        when(storageService.get(StorageKey.blockKey(3))).thenReturn(new StorageValue(serializeBlock(block3), "Block"));
        
        // 执行测试
        List<Block> result = blockStorageService.getRecentBlocks(3);
        
        // 验证结果
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getHeight()).isEqualTo(1);
        assertThat(result.get(1).getHeight()).isEqualTo(2);
        assertThat(result.get(2).getHeight()).isEqualTo(3);
    }
    
    @Test
    void testGetRecentBlocksNoBlocks() throws StorageException {
        // 准备测试数据
        when(storageService.get(new StorageKey("block", "latest"))).thenReturn(null);
        
        // 执行测试
        List<Block> result = blockStorageService.getRecentBlocks(3);
        
        // 验证结果
        assertThat(result).isEmpty();
    }
    
    @Test
    void testClearCache() {
        // 执行测试
        blockStorageService.clearCache();
        
        // 验证结果
        Map<String, Object> stats = blockStorageService.getCacheStats();
        assertThat(stats.get("blockCacheSize")).isEqualTo(0);
        assertThat(stats.get("hashToHeightCacheSize")).isEqualTo(0);
        assertThat(stats.get("latestBlockHeight")).isEqualTo(-1);
    }
    
    @Test
    void testGetCacheStats() {
        // 执行测试
        Map<String, Object> stats = blockStorageService.getCacheStats();
        
        // 验证结果
        assertThat(stats).containsKeys("blockCacheSize", "hashToHeightCacheSize", "latestBlockHeight");
        assertThat(stats.get("blockCacheSize")).isInstanceOf(Integer.class);
        assertThat(stats.get("hashToHeightCacheSize")).isInstanceOf(Integer.class);
        assertThat(stats.get("latestBlockHeight")).isInstanceOf(Long.class);
    }
    
    @Test
    void testStoreBlockException() throws StorageException {
        // 准备测试数据
        Block block = createTestBlock(1);
        doThrow(new StorageException("Test exception")).when(storageService).put(any(StorageKey.class), any(StorageValue.class));
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> blockStorageService.storeBlock(block))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to store block");
    }
    
    @Test
    void testGetBlockByHeightException() throws StorageException {
        // 准备测试数据
        when(storageService.get(StorageKey.blockKey(1))).thenThrow(new StorageException("Test exception"));
        
        // 执行测试并验证异常
        assertThatThrownBy(() -> blockStorageService.getBlockByHeight(1))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to retrieve block by height");
    }
    
    @Test
    void testGetBlocksInvalidRange() {
        // 执行测试并验证异常
        assertThatThrownBy(() -> blockStorageService.getBlocks(3, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start height cannot be greater than end height");
    }
    
    @Test
    void testGetRecentBlocksInvalidCount() {
        // 执行测试并验证异常
        assertThatThrownBy(() -> blockStorageService.getRecentBlocks(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Count must be positive");
        
        assertThatThrownBy(() -> blockStorageService.getRecentBlocks(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Count must be positive");
    }
    
    /**
     * 创建测试区块
     */
    private Block createTestBlock(long height) {
        try {
            KeyPair keyPair = java.security.KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
            byte[] proposer = keyPair.getPublic().getEncoded();
            
            byte[] previousHash = new byte[32];
            Arrays.fill(previousHash, (byte) 1);
            
            byte[] vrfOutput = new byte[32];
            Arrays.fill(vrfOutput, (byte) 2);
            
            VRFProof vrfProof = new VRFProof(new byte[64]);
            List<VRFAnnouncement> vrfAnnouncements = new ArrayList<>();
            List<byte[]> rewardedNodes = new ArrayList<>();
            List<Transaction> transactions = new ArrayList<>();
            
            return new Block(height, 1, System.currentTimeMillis(), previousHash, proposer, vrfOutput, vrfProof,
                           vrfAnnouncements, rewardedNodes, transactions, 12345L, BigInteger.valueOf(1000000),
                           new HashMap<>());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test block", e);
        }
    }
    
    /**
     * 序列化区块
     */
    private byte[] serializeBlock(Block block) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsBytes(block);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize block", e);
        }
    }
}
