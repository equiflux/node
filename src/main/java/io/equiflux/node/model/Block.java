package io.equiflux.node.model;

import io.equiflux.node.crypto.HashUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 区块
 * 
 * <p>表示区块链中的一个区块，包含完整的共识信息和交易数据。
 * 
 * <p>区块结构（按白皮书规范）：
 * <ul>
 *   <li>基础信息：高度、轮次、时间戳、前一区块哈希</li>
 *   <li>出块者VRF信息：公钥、VRF输出、VRF证明</li>
 *   <li>所有VRF公告列表（关键！约5KB）</li>
 *   <li>奖励节点列表（前15名）</li>
 *   <li>交易列表和Merkle根</li>
 *   <li>PoW信息：nonce、难度目标</li>
 *   <li>签名集合（最终性）</li>
 * </ul>
 * 
 * <p>关键特性：
 * <ul>
 *   <li>完全透明：包含所有VRF公告</li>
 *   <li>实时可验证：无需历史挑战</li>
 *   <li>最终性：2/3签名后不可逆</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class Block {
    
    // 基础信息
    private final long height;
    private final int round;
    private final long timestamp;
    private final byte[] previousHash;
    
    // 出块者VRF信息
    private final byte[] proposer;
    private final byte[] vrfOutput;
    private final VRFProof vrfProof;
    
    // 关键：所有有效的VRF公告（约5KB）
    private final List<VRFAnnouncement> allVRFAnnouncements;
    
    // 奖励节点列表（前15名）
    private final List<byte[]> rewardedNodes;
    
    // 交易信息
    private final byte[] merkleRoot;
    private final List<Transaction> transactions;
    
    // PoW信息
    private final long nonce;
    private final BigInteger difficultyTarget;
    
    // 签名集合（最终性）
    private final Map<byte[], byte[]> signatures;
    
    /**
     * 构造区块
     * 
     * @param height 区块高度
     * @param round 轮次
     * @param timestamp 时间戳
     * @param previousHash 前一区块哈希
     * @param proposer 出块者公钥
     * @param vrfOutput VRF输出
     * @param vrfProof VRF证明
     * @param allVRFAnnouncements 所有VRF公告
     * @param rewardedNodes 奖励节点列表
     * @param transactions 交易列表
     * @param nonce PoW nonce
     * @param difficultyTarget PoW难度目标
     * @param signatures 签名集合
     */
    @JsonCreator
    public Block(@JsonProperty("height") long height,
                 @JsonProperty("round") int round,
                 @JsonProperty("timestamp") long timestamp,
                 @JsonProperty("previousHash") byte[] previousHash,
                 @JsonProperty("proposer") byte[] proposer,
                 @JsonProperty("vrfOutput") byte[] vrfOutput,
                 @JsonProperty("vrfProof") VRFProof vrfProof,
                 @JsonProperty("allVRFAnnouncements") List<VRFAnnouncement> allVRFAnnouncements,
                 @JsonProperty("rewardedNodes") List<byte[]> rewardedNodes,
                 @JsonProperty("transactions") List<Transaction> transactions,
                 @JsonProperty("nonce") long nonce,
                 @JsonProperty("difficultyTarget") BigInteger difficultyTarget,
                 @JsonProperty("signatures") Map<byte[], byte[]> signatures) {
        this.height = height;
        this.round = round;
        this.timestamp = timestamp;
        this.previousHash = Objects.requireNonNull(previousHash, "Previous hash cannot be null");
        this.proposer = Objects.requireNonNull(proposer, "Proposer cannot be null");
        this.vrfOutput = Objects.requireNonNull(vrfOutput, "VRF output cannot be null");
        this.vrfProof = Objects.requireNonNull(vrfProof, "VRF proof cannot be null");
        this.allVRFAnnouncements = Objects.requireNonNull(allVRFAnnouncements, "VRF announcements cannot be null");
        this.rewardedNodes = Objects.requireNonNull(rewardedNodes, "Rewarded nodes cannot be null");
        this.transactions = Objects.requireNonNull(transactions, "Transactions cannot be null");
        this.nonce = nonce;
        this.difficultyTarget = Objects.requireNonNull(difficultyTarget, "Difficulty target cannot be null");
        this.signatures = Objects.requireNonNull(signatures, "Signatures cannot be null");
        
        // 验证参数
        if (height < 0) {
            throw new IllegalArgumentException("Height cannot be negative");
        }
        if (round < 0) {
            throw new IllegalArgumentException("Round cannot be negative");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
        if (previousHash.length != 32) {
            throw new IllegalArgumentException("Previous hash must be 32 bytes");
        }
        if (vrfOutput.length != 32) {
            throw new IllegalArgumentException("VRF output must be 32 bytes");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce cannot be negative");
        }
        if (difficultyTarget.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Difficulty target must be positive");
        }
        
        // 计算Merkle根
        this.merkleRoot = calculateMerkleRoot();
    }
    
    /**
     * 获取区块高度
     * 
     * @return 区块高度
     */
    public long getHeight() {
        return height;
    }
    
    /**
     * 获取轮次
     * 
     * @return 轮次
     */
    public int getRound() {
        return round;
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
     * 获取前一区块哈希
     * 
     * @return 前一区块哈希
     */
    public byte[] getPreviousHash() {
        return previousHash.clone();
    }
    
    /**
     * 获取出块者公钥
     * 
     * @return 出块者公钥
     */
    public byte[] getProposer() {
        return proposer.clone();
    }
    
    /**
     * 获取VRF输出
     * 
     * @return VRF输出
     */
    public byte[] getVrfOutput() {
        return vrfOutput.clone();
    }
    
    /**
     * 获取VRF证明
     * 
     * @return VRF证明
     */
    public VRFProof getVrfProof() {
        return vrfProof;
    }
    
    /**
     * 获取所有VRF公告
     * 
     * @return 所有VRF公告列表
     */
    public List<VRFAnnouncement> getAllVRFAnnouncements() {
        return Collections.unmodifiableList(allVRFAnnouncements);
    }
    
    /**
     * 获取奖励节点列表
     * 
     * @return 奖励节点列表
     */
    public List<byte[]> getRewardedNodes() {
        return rewardedNodes.stream().map(bytes -> bytes.clone()).collect(Collectors.toList());
    }
    
    /**
     * 获取Merkle根
     * 
     * @return Merkle根
     */
    public byte[] getMerkleRoot() {
        return merkleRoot.clone();
    }
    
    /**
     * 获取交易列表
     * 
     * @return 交易列表
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
    
    /**
     * 获取PoW nonce
     * 
     * @return PoW nonce
     */
    public long getNonce() {
        return nonce;
    }
    
    /**
     * 获取PoW难度目标
     * 
     * @return PoW难度目标
     */
    public BigInteger getDifficultyTarget() {
        return difficultyTarget;
    }
    
    /**
     * 获取签名集合
     * 
     * @return 签名集合
     */
    public Map<byte[], byte[]> getSignatures() {
        Map<byte[], byte[]> result = new HashMap<>();
        signatures.forEach((key, value) -> result.put(key.clone(), value.clone()));
        return result;
    }
    
    /**
     * 获取出块者公钥的十六进制字符串
     * 
     * @return 出块者公钥的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getProposerHex() {
        return HashUtils.toHexString(proposer);
    }
    
    /**
     * 获取前一区块哈希的十六进制字符串
     * 
     * @return 前一区块哈希的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPreviousHashHex() {
        return HashUtils.toHexString(previousHash);
    }
    
    /**
     * 获取VRF输出的十六进制字符串
     * 
     * @return VRF输出的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getVrfOutputHex() {
        return HashUtils.toHexString(vrfOutput);
    }
    
    /**
     * 获取Merkle根的十六进制字符串
     * 
     * @return Merkle根的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getMerkleRootHex() {
        return HashUtils.toHexString(merkleRoot);
    }
    
    /**
     * 验证区块格式是否正确
     * 
     * @return true如果格式正确，false否则
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isValidFormat() {
        try {
            // 检查基础字段
            if (height < 0 || round < 0 || timestamp <= 0) {
                return false;
            }
            
            // 检查哈希字段
            if (previousHash == null || previousHash.length != 32) {
                return false;
            }
            
            // 检查VRF字段
            if (vrfOutput == null || vrfOutput.length != 32) {
                return false;
            }
            
            if (vrfProof == null || vrfProof.getProof() == null) {
                return false;
            }
            
            // 检查交易列表
            if (transactions == null) {
                return false;
            }
            
            // 检查Merkle根
            if (merkleRoot == null || merkleRoot.length != 32) {
                return false;
            }
            
            // 检查PoW字段
            if (nonce < 0 || difficultyTarget == null || difficultyTarget.compareTo(BigInteger.ZERO) <= 0) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 计算区块哈希
     * 
     * @return 32字节的区块哈希
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public byte[] getHash() {
        byte[] data = serializeForSigning();
        return HashUtils.sha256(data);
    }
    
    /**
     * 获取区块哈希的十六进制字符串
     * 
     * @return 区块哈希的十六进制字符串
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getHashHex() {
        return HashUtils.toHexString(getHash());
    }
    
    /**
     * 序列化区块用于签名
     * 
     * @return 序列化后的字节数组
     */
    public byte[] serializeForSigning() {
        // 构造待签名数据（排除签名字段）
        byte[] heightBytes = longToBytes(height);
        byte[] roundBytes = intToBytes(round);
        byte[] timestampBytes = longToBytes(timestamp);
        byte[] proposerBytes = proposer;
        byte[] nonceBytes = longToBytes(nonce);
        byte[] difficultyBytes = difficultyTarget.toByteArray();
        
        // 连接所有数据
        return HashUtils.sha256(heightBytes, roundBytes, timestampBytes, previousHash,
                               proposerBytes, vrfOutput, merkleRoot, nonceBytes, difficultyBytes);
    }
    
    /**
     * 计算Merkle根
     * 
     * @return 32字节的Merkle根
     */
    private byte[] calculateMerkleRoot() {
        if (transactions.isEmpty()) {
            // 空交易列表返回零哈希
            return new byte[32];
        }
        
        // 提取交易哈希
        byte[][] transactionHashes = new byte[transactions.size()][];
        for (int i = 0; i < transactions.size(); i++) {
            transactionHashes[i] = transactions.get(i).getHash();
        }
        
        return HashUtils.computeMerkleRoot(transactionHashes);
    }
    
    /**
     * 获取VRF公告数量
     * 
     * @return VRF公告数量
     */
    public int getVRFAnnouncementCount() {
        return allVRFAnnouncements.size();
    }
    
    /**
     * 获取交易数量
     * 
     * @return 交易数量
     */
    public int getTransactionCount() {
        return transactions.size();
    }
    
    /**
     * 获取签名数量
     * 
     * @return 签名数量
     */
    public int getSignatureCount() {
        return signatures.size();
    }
    
    /**
     * 检查是否有足够的签名（2/3）
     * 
     * @param totalNodes 总节点数
     * @return true如果有足够签名，false否则
     */
    public boolean hasEnoughSignatures(int totalNodes) {
        int requiredSignatures = (totalNodes * 2) / 3;
        return signatures.size() >= requiredSignatures;
    }
    
    /**
     * 检查区块是否包含指定的VRF公告
     * 
     * @param publicKeyBytes 公钥字节数组
     * @return true如果包含，false否则
     */
    public boolean containsVRFAnnouncement(byte[] publicKeyBytes) {
        return allVRFAnnouncements.stream()
                .anyMatch(announcement -> Arrays.equals(announcement.getPublicKey().getEncoded(), publicKeyBytes));
    }
    
    /**
     * 检查节点是否在奖励列表中
     * 
     * @param publicKeyBytes 公钥字节数组
     * @return true如果在奖励列表中，false否则
     */
    public boolean isRewardedNode(byte[] publicKeyBytes) {
        return rewardedNodes.stream()
                .anyMatch(nodeBytes -> Arrays.equals(nodeBytes, publicKeyBytes));
    }
    
    /**
     * 将long值转换为8字节数组（大端序）
     * 
     * @param value long值
     * @return 8字节数组
     */
    private byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }
    
    /**
     * 将int值转换为4字节数组（大端序）
     * 
     * @param value int值
     * @return 4字节数组
     */
    private byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        for (int i = 3; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Block block = (Block) obj;
        return height == block.height &&
               round == block.round &&
               timestamp == block.timestamp &&
               nonce == block.nonce &&
               Arrays.equals(previousHash, block.previousHash) &&
               Objects.equals(proposer, block.proposer) &&
               Arrays.equals(vrfOutput, block.vrfOutput) &&
               Objects.equals(vrfProof, block.vrfProof) &&
               Objects.equals(allVRFAnnouncements, block.allVRFAnnouncements) &&
               Objects.equals(rewardedNodes, block.rewardedNodes) &&
               Arrays.equals(merkleRoot, block.merkleRoot) &&
               Objects.equals(transactions, block.transactions) &&
               Objects.equals(difficultyTarget, block.difficultyTarget) &&
               Objects.equals(signatures, block.signatures);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(height, round, timestamp, Arrays.hashCode(previousHash),
                           proposer, Arrays.hashCode(vrfOutput), vrfProof, allVRFAnnouncements,
                           rewardedNodes, Arrays.hashCode(merkleRoot), transactions, nonce,
                           difficultyTarget, signatures);
    }
    
    @Override
    public String toString() {
        return "Block{" +
               "height=" + height +
               ", round=" + round +
               ", hash=" + getHashHex() +
               ", proposer=" + getProposerHex() +
               ", vrfAnnouncements=" + getVRFAnnouncementCount() +
               ", transactions=" + getTransactionCount() +
               ", signatures=" + getSignatureCount() +
               '}';
    }
    
    /**
     * Block构建器
     * 
     * <p>使用Builder模式构造Block对象，提供更灵活的构造方式。
     */
    public static class Builder {
        private long height;
        private int round;
        private long timestamp;
        private byte[] previousHash;
        private byte[] proposer;
        private byte[] vrfOutput;
        private VRFProof vrfProof;
        private List<VRFAnnouncement> allVRFAnnouncements = new ArrayList<>();
        private List<byte[]> rewardedNodes = new ArrayList<>();
        private List<Transaction> transactions = new ArrayList<>();
        private long nonce;
        private BigInteger difficultyTarget;
        private Map<byte[], byte[]> signatures = new HashMap<>();
        
        public Builder height(long height) {
            this.height = height;
            return this;
        }
        
        public Builder round(int round) {
            this.round = round;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder previousHash(byte[] previousHash) {
            this.previousHash = previousHash;
            return this;
        }
        
        public Builder proposer(byte[] proposer) {
            this.proposer = proposer;
            return this;
        }
        
        public Builder vrfOutput(byte[] vrfOutput) {
            this.vrfOutput = vrfOutput;
            return this;
        }
        
        public Builder vrfProof(VRFProof vrfProof) {
            this.vrfProof = vrfProof;
            return this;
        }
        
        public Builder allVRFAnnouncements(List<VRFAnnouncement> allVRFAnnouncements) {
            this.allVRFAnnouncements = allVRFAnnouncements;
            return this;
        }
        
        public Builder rewardedNodes(List<byte[]> rewardedNodes) {
            this.rewardedNodes = rewardedNodes;
            return this;
        }
        
        public Builder transactions(List<Transaction> transactions) {
            this.transactions = transactions;
            return this;
        }
        
        public Builder nonce(long nonce) {
            this.nonce = nonce;
            return this;
        }
        
        public Builder difficultyTarget(BigInteger difficultyTarget) {
            this.difficultyTarget = difficultyTarget;
            return this;
        }
        
        public Builder signatures(Map<byte[], byte[]> signatures) {
            this.signatures = signatures;
            return this;
        }
        
        public Block build() {
            return new Block(height, round, timestamp, previousHash, proposer, vrfOutput, vrfProof,
                           allVRFAnnouncements, rewardedNodes, transactions, nonce, difficultyTarget, signatures);
        }
    }
}
