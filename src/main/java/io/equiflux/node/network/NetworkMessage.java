package io.equiflux.node.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;

import java.security.PublicKey;
import java.util.List;
import java.util.Objects;

/**
 * 网络消息
 * 
 * <p>表示在P2P网络中传输的消息，包含消息类型、发送者、时间戳和数据负载。
 * 
 * <p>支持的消息类型：
 * <ul>
 *   <li>BLOCK_PROPOSAL: 区块提议</li>
 *   <li>BLOCK_VOTE: 区块投票</li>
 *   <li>TRANSACTION: 交易广播</li>
 *   <li>VRF_ANNOUNCEMENT: VRF公告</li>
 *   <li>PEER_DISCOVERY: 节点发现</li>
 *   <li>SYNC_REQUEST: 同步请求</li>
 *   <li>SYNC_RESPONSE: 同步响应</li>
 *   <li>PING: 心跳检测</li>
 *   <li>PONG: 心跳响应</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class NetworkMessage {
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        BLOCK_PROPOSAL("block_proposal"),
        BLOCK_VOTE("block_vote"),
        TRANSACTION("transaction"),
        VRF_ANNOUNCEMENT("vrf_announcement"),
        PEER_DISCOVERY("peer_discovery"),
        SYNC_REQUEST("sync_request"),
        SYNC_RESPONSE("sync_response"),
        PING("ping"),
        PONG("pong");
        
        private final String value;
        
        MessageType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static MessageType fromValue(String value) {
            for (MessageType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown message type: " + value);
        }
    }
    
    private final MessageType type;
    private final PublicKey sender;
    private final long timestamp;
    private final long nonce;
    private final Object payload;
    private final byte[] signature;
    
    /**
     * 构造网络消息
     * 
     * @param type 消息类型
     * @param sender 发送者公钥
     * @param timestamp 时间戳
     * @param nonce 防重放随机数
     * @param payload 消息负载
     * @param signature 数字签名
     */
    @JsonCreator
    public NetworkMessage(@JsonProperty("type") MessageType type,
                         @JsonProperty("sender") PublicKey sender,
                         @JsonProperty("timestamp") long timestamp,
                         @JsonProperty("nonce") long nonce,
                         @JsonProperty("payload") Object payload,
                         @JsonProperty("signature") byte[] signature) {
        this.type = Objects.requireNonNull(type, "Message type cannot be null");
        this.sender = Objects.requireNonNull(sender, "Sender cannot be null");
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.payload = payload;
        this.signature = Objects.requireNonNull(signature, "Signature cannot be null");
        
        // 验证参数
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
        if (nonce < 0) {
            throw new IllegalArgumentException("Nonce cannot be negative");
        }
        if (signature.length != 64) {
            throw new IllegalArgumentException("Signature must be 64 bytes");
        }
    }
    
    /**
     * 获取消息类型
     * 
     * @return 消息类型
     */
    public MessageType getType() {
        return type;
    }
    
    /**
     * 获取发送者公钥
     * 
     * @return 发送者公钥
     */
    public PublicKey getSender() {
        return sender;
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
     * 获取Nonce
     * 
     * @return Nonce
     */
    public long getNonce() {
        return nonce;
    }
    
    /**
     * 获取消息负载
     * 
     * @return 消息负载
     */
    public Object getPayload() {
        return payload;
    }
    
    /**
     * 获取数字签名
     * 
     * @return 数字签名
     */
    public byte[] getSignature() {
        return signature.clone();
    }
    
    /**
     * 获取区块负载（如果是区块相关消息）
     * 
     * @return 区块对象，如果不是区块消息则返回null
     */
    public Block getBlockPayload() {
        if (payload instanceof Block) {
            return (Block) payload;
        }
        return null;
    }
    
    /**
     * 获取交易负载（如果是交易消息）
     * 
     * @return 交易对象，如果不是交易消息则返回null
     */
    public Transaction getTransactionPayload() {
        if (payload instanceof Transaction) {
            return (Transaction) payload;
        }
        return null;
    }
    
    /**
     * 获取VRF公告负载（如果是VRF公告消息）
     * 
     * @return VRF公告对象，如果不是VRF公告消息则返回null
     */
    public VRFAnnouncement getVRFAnnouncementPayload() {
        if (payload instanceof VRFAnnouncement) {
            return (VRFAnnouncement) payload;
        }
        return null;
    }
    
    /**
     * 获取节点列表负载（如果是节点发现消息）
     * 
     * @return 节点列表，如果不是节点发现消息则返回null
     */
    @SuppressWarnings("unchecked")
    public List<PeerInfo> getPeerListPayload() {
        if (payload instanceof List) {
            return (List<PeerInfo>) payload;
        }
        return null;
    }
    
    /**
     * 获取同步请求负载（如果是同步请求消息）
     * 
     * @return 同步请求对象，如果不是同步请求消息则返回null
     */
    public SyncRequest getSyncRequestPayload() {
        if (payload instanceof SyncRequest) {
            return (SyncRequest) payload;
        }
        return null;
    }
    
    /**
     * 获取同步响应负载（如果是同步响应消息）
     * 
     * @return 同步响应对象，如果不是同步响应消息则返回null
     */
    public SyncResponse getSyncResponsePayload() {
        if (payload instanceof SyncResponse) {
            return (SyncResponse) payload;
        }
        return null;
    }
    
    /**
     * 检查消息是否过期
     * 
     * @param maxAgeMs 最大年龄（毫秒）
     * @return true如果过期，false否则
     */
    public boolean isExpired(long maxAgeMs) {
        long currentTime = System.currentTimeMillis();
        return currentTime - timestamp > maxAgeMs;
    }
    
    /**
     * 检查消息格式是否正确
     * 
     * @return true如果格式正确，false否则
     */
    public boolean isValidFormat() {
        try {
            // 检查基本字段
            if (type == null || sender == null) {
                return false;
            }
            if (timestamp <= 0 || nonce < 0) {
                return false;
            }
            if (signature == null || signature.length != 64) {
                return false;
            }
            
            // 检查负载类型是否与消息类型匹配
            switch (type) {
                case BLOCK_PROPOSAL:
                case BLOCK_VOTE:
                    return payload instanceof Block;
                case TRANSACTION:
                    return payload instanceof Transaction;
                case VRF_ANNOUNCEMENT:
                    return payload instanceof VRFAnnouncement;
                case PEER_DISCOVERY:
                    return payload instanceof List;
                case SYNC_REQUEST:
                    return payload instanceof SyncRequest;
                case SYNC_RESPONSE:
                    return payload instanceof SyncResponse;
                case PING:
                case PONG:
                    return payload == null; // 心跳消息无负载
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 序列化消息用于签名
     * 
     * @return 序列化后的字节数组
     */
    public byte[] serializeForSigning() {
        // 构造待签名数据（排除签名字段）
        byte[] typeBytes = type.getValue().getBytes();
        byte[] senderBytes = sender.getEncoded();
        byte[] timestampBytes = longToBytes(timestamp);
        byte[] nonceBytes = longToBytes(nonce);
        
        // 根据负载类型序列化
        byte[] payloadBytes = serializePayload();
        
        // 连接所有数据
        return concatenateBytes(typeBytes, senderBytes, timestampBytes, nonceBytes, payloadBytes);
    }
    
    /**
     * 序列化负载
     * 
     * @return 负载的字节数组
     */
    private byte[] serializePayload() {
        if (payload == null) {
            return new byte[0];
        }
        
        // 这里应该使用JSON序列化，简化实现使用toString()
        return payload.toString().getBytes();
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
     * 连接多个字节数组
     * 
     * @param arrays 字节数组
     * @return 连接后的字节数组
     */
    private byte[] concatenateBytes(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NetworkMessage that = (NetworkMessage) obj;
        return timestamp == that.timestamp &&
               nonce == that.nonce &&
               type == that.type &&
               Objects.equals(sender, that.sender) &&
               Objects.equals(payload, that.payload) &&
               java.util.Arrays.equals(signature, that.signature);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, sender, timestamp, nonce, payload, java.util.Arrays.hashCode(signature));
    }
    
    @Override
    public String toString() {
        return "NetworkMessage{" +
               "type=" + type +
               ", sender=" + sender +
               ", timestamp=" + timestamp +
               ", nonce=" + nonce +
               ", payload=" + payload +
               '}';
    }
}
