package io.equiflux.node.network;

import io.equiflux.node.crypto.Ed25519KeyPair;
import io.equiflux.node.crypto.HashUtils;
import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * P2P网络服务实现
 * 
 * <p>基于Netty实现的P2P网络通信服务，支持消息广播、节点管理、连接管理等功能。
 * 
 * <p>主要特性：
 * <ul>
 *   <li>基于Netty的高性能网络通信</li>
 *   <li>支持Epoll和NIO两种模式</li>
 *   <li>消息序列化和反序列化</li>
 *   <li>连接管理和心跳检测</li>
 *   <li>消息去重和验证</li>
 *   <li>异步消息处理</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class NettyNetworkService implements NetworkService {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyNetworkService.class);
    
    @Autowired
    private NetworkConfig networkConfig;
    
    @Autowired
    private Ed25519KeyPair localKeyPair;
    
    @Autowired
    private MessageCompressionService compressionService;
    
    @Autowired
    private MessageEncryptionService encryptionService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong startTime = new AtomicLong(0);
    
    // Netty组件
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup clientGroup;
    private ServerBootstrap serverBootstrap;
    private Bootstrap clientBootstrap;
    private Channel serverChannel;
    
    // 节点管理
    private final Map<String, PeerInfo> peers = new ConcurrentHashMap<>();
    private final Map<String, Channel> peerChannels = new ConcurrentHashMap<>();
    private final Map<String, Long> messageNonces = new ConcurrentHashMap<>();
    private final Map<String, Long> messageTimestamps = new ConcurrentHashMap<>();
    
    // 监听器
    private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<>();
    private final List<PeerListener> peerListeners = new CopyOnWriteArrayList<>();
    
    // 统计信息
    private final AtomicLong messagesSent = new AtomicLong(0);
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong bytesSent = new AtomicLong(0);
    private final AtomicLong bytesReceived = new AtomicLong(0);
    private final AtomicLong lastActivityTime = new AtomicLong(System.currentTimeMillis());
    
    @PostConstruct
    public void init() {
        networkConfig.validate();
        logger.info("初始化网络服务，配置: {}", networkConfig);
    }
    
    @Override
    public CompletableFuture<Void> start(int port) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (running.compareAndSet(false, true)) {
                    logger.info("启动网络服务，端口: {}", port);
                    
                    // 创建EventLoopGroup
                    createEventLoopGroups();
                    
                    // 创建Bootstrap
                    createBootstrap();
                    
                    // 启动服务器
                    startServer(port);
                    
                    startTime.set(System.currentTimeMillis());
                    logger.info("网络服务启动成功，端口: {}", port);
                } else {
                    logger.warn("网络服务已经在运行中");
                }
            } catch (Exception e) {
                running.set(false);
                logger.error("启动网络服务失败", e);
                throw new RuntimeException("Failed to start network service", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (running.compareAndSet(true, false)) {
                    logger.info("停止网络服务");
                    
                    // 关闭服务器通道
                    if (serverChannel != null) {
                        serverChannel.close().sync();
                    }
                    
                    // 关闭所有客户端连接
                    closeAllConnections();
                    
                    // 关闭EventLoopGroup
                    shutdownEventLoopGroups();
                    
                    logger.info("网络服务停止成功");
                } else {
                    logger.warn("网络服务未在运行");
                }
            } catch (Exception e) {
                logger.error("停止网络服务失败", e);
                throw new RuntimeException("Failed to stop network service", e);
            }
        });
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    @Override
    public CompletableFuture<Void> broadcastBlockProposal(Block block) {
        return broadcastMessage(NetworkMessage.MessageType.BLOCK_PROPOSAL, block);
    }
    
    @Override
    public CompletableFuture<Void> broadcastBlockVote(Block block) {
        return broadcastMessage(NetworkMessage.MessageType.BLOCK_VOTE, block);
    }
    
    @Override
    public CompletableFuture<Void> broadcastTransaction(Transaction transaction) {
        return broadcastMessage(NetworkMessage.MessageType.TRANSACTION, transaction);
    }
    
    @Override
    public CompletableFuture<Void> broadcastVRFAnnouncement(VRFAnnouncement announcement) {
        return broadcastMessage(NetworkMessage.MessageType.VRF_ANNOUNCEMENT, announcement);
    }
    
    @Override
    public CompletableFuture<Void> sendMessage(String peerId, NetworkMessage message) {
        return CompletableFuture.runAsync(() -> {
            try {
                Channel channel = peerChannels.get(peerId);
                if (channel != null && channel.isActive()) {
                    sendMessageToChannel(channel, message);
                } else {
                    logger.warn("无法发送消息到节点: {}, 连接不存在或已断开", peerId);
                }
            } catch (Exception e) {
                logger.error("发送消息到节点失败: " + peerId, e);
                notifyMessageFailed(message, peerId, e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> sendMessage(String host, int port, NetworkMessage message) {
        return CompletableFuture.runAsync(() -> {
            try {
                ChannelFuture future = clientBootstrap.connect(host, port).sync();
                Channel channel = future.channel();
                sendMessageToChannel(channel, message);
                channel.close();
            } catch (Exception e) {
                logger.error("发送消息到地址失败: {}:{}", host, port, e);
                notifyMessageFailed(message, host + ":" + port, e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> connectToPeer(String host, int port) {
        return CompletableFuture.runAsync(() -> {
            try {
                String peerId = host + ":" + port;
                PeerInfo peerInfo = peers.get(peerId);
                
                if (peerInfo == null) {
                    // 创建新的节点信息，使用本地公钥作为临时公钥
                    PublicKey tempPublicKey = localKeyPair.getPublicKey();
                    if (tempPublicKey == null) {
                        throw new IllegalStateException("Local key pair public key is null");
                    }
                    peerInfo = new PeerInfo(peerId, tempPublicKey, host, port, 
                                           PeerInfo.PeerStatus.CONNECTING, 
                                           System.currentTimeMillis(), 0, 0);
                    peers.put(peerId, peerInfo);
                }
                
                if (peerInfo.canConnect()) {
                    ChannelFuture future = clientBootstrap.connect(host, port).sync();
                    Channel channel = future.channel();
                    peerChannels.put(peerId, channel);
                    
                    // 更新节点状态
                    PeerInfo updatedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.CONNECTED);
                    peers.put(peerId, updatedPeer);
                    
                    notifyPeerConnected(updatedPeer);
                    logger.info("连接到节点成功: {}", peerId);
                } else {
                    logger.warn("节点无法连接: {}", peerId);
                }
            } catch (Exception e) {
                logger.error("连接节点失败: {}:{}", host, port, e);
                String peerId = host + ":" + port;
                PeerInfo peerInfo = peers.get(peerId);
                if (peerInfo != null) {
                    PeerInfo failedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.FAILED);
                    peers.put(peerId, failedPeer);
                    notifyPeerConnectionFailed(failedPeer, e);
                }
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> disconnectFromPeer(String peerId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Channel channel = peerChannels.remove(peerId);
                if (channel != null) {
                    channel.close().sync();
                }
                
                PeerInfo peerInfo = peers.get(peerId);
                if (peerInfo != null) {
                    PeerInfo disconnectedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.DISCONNECTED);
                    peers.put(peerId, disconnectedPeer);
                    notifyPeerDisconnected(disconnectedPeer);
                }
                
                logger.info("断开与节点的连接: {}", peerId);
            } catch (Exception e) {
                logger.error("断开节点连接失败: " + peerId, e);
            }
        });
    }
    
    @Override
    public List<PeerInfo> getConnectedPeers() {
        return peers.values().stream()
                .filter(PeerInfo::isConnected)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    @Override
    public PeerInfo getPeerInfo(String peerId) {
        return peers.get(peerId);
    }
    
    @Override
    public int getPeerCount() {
        return peers.size();
    }
    
    @Override
    public boolean isConnectedToPeer(String peerId) {
        PeerInfo peerInfo = peers.get(peerId);
        return peerInfo != null && peerInfo.isConnected();
    }
    
    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    @Override
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    @Override
    public void addPeerListener(PeerListener listener) {
        peerListeners.add(listener);
    }
    
    @Override
    public void removePeerListener(PeerListener listener) {
        peerListeners.remove(listener);
    }
    
    @Override
    public PeerInfo getLocalPeerInfo() {
        String nodeId = HashUtils.toHexString(localKeyPair.getPublicKey().getEncoded());
        int port = networkConfig.getPort();
        if (port <= 0 || port > 65535) {
            port = 8080; // 默认端口
        }
        return new PeerInfo(nodeId, localKeyPair.getPublicKey(), "localhost", 
                           port, PeerInfo.PeerStatus.CONNECTED, 
                           System.currentTimeMillis(), 0, 0);
    }
    
    @Override
    public NetworkStats getNetworkStats() {
        return new NetworkStats(
            messagesSent.get(),
            messagesReceived.get(),
            bytesSent.get(),
            bytesReceived.get(),
            getConnectedPeers().size(),
            peers.size(),
            System.currentTimeMillis() - startTime.get(),
            lastActivityTime.get()
        );
    }
    
    /**
     * 获取压缩统计信息
     * 
     * @return 压缩统计信息
     */
    public MessageCompressionService.CompressionStats getCompressionStats() {
        return compressionService.getStats();
    }
    
    /**
     * 获取加密统计信息
     * 
     * @return 加密统计信息
     */
    public MessageEncryptionService.EncryptionStats getEncryptionStats() {
        return encryptionService.getStats();
    }
    
    @Override
    public CompletableFuture<List<PeerInfo>> discoverPeers() {
        return CompletableFuture.completedFuture(new ArrayList<>(peers.values()));
    }
    
    @Override
    public CompletableFuture<List<Block>> syncBlocks(long startHeight, long endHeight) {
        // TODO: 实现区块同步逻辑
        return CompletableFuture.completedFuture(new ArrayList<>());
    }
    
    @Override
    public CompletableFuture<Void> sendHeartbeat() {
        return broadcastMessage(NetworkMessage.MessageType.PING, null);
    }
    
    @Override
    public boolean isHealthy() {
        return running.get() && getConnectedPeers().size() >= networkConfig.getMinPeers();
    }
    
    // 私有方法
    
    private void createEventLoopGroups() {
        if (networkConfig.isUseEpoll()) {
            bossGroup = new EpollEventLoopGroup(networkConfig.getBossThreads(), 
                                              new DefaultThreadFactory("netty-boss"));
            workerGroup = new EpollEventLoopGroup(networkConfig.getWorkerThreads(), 
                                                new DefaultThreadFactory("netty-worker"));
            clientGroup = new EpollEventLoopGroup(networkConfig.getWorkerThreads(), 
                                                new DefaultThreadFactory("netty-client"));
            logger.info("使用Epoll EventLoopGroup");
        } else {
            bossGroup = new NioEventLoopGroup(networkConfig.getBossThreads(), 
                                            new DefaultThreadFactory("netty-boss"));
            workerGroup = new NioEventLoopGroup(networkConfig.getWorkerThreads(), 
                                              new DefaultThreadFactory("netty-worker"));
            clientGroup = new NioEventLoopGroup(networkConfig.getWorkerThreads(), 
                                              new DefaultThreadFactory("netty-client"));
            logger.info("使用NIO EventLoopGroup");
        }
    }
    
    private void createBootstrap() {
        // 服务器Bootstrap
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(networkConfig.isUseEpoll() ? 
                        EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, networkConfig.isReuseAddress())
                .childOption(ChannelOption.SO_KEEPALIVE, networkConfig.isKeepAlive())
                .childOption(ChannelOption.TCP_NODELAY, networkConfig.isTcpNoDelay())
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 空闲状态检测
                        pipeline.addLast(new IdleStateHandler(
                                (int)(networkConfig.getReadTimeoutMs() / 1000),
                                (int)(networkConfig.getWriteTimeoutMs() / 1000),
                                (int)(networkConfig.getHeartbeatTimeoutMs() / 1000)));
                        
                        // 长度字段解码器
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                networkConfig.getMaxMessageSize(), 0, 4, 0, 4));
                        pipeline.addLast(new LengthFieldPrepender(4));
                        
                        // 消息处理器
                        pipeline.addLast(new NetworkMessageHandler());
                    }
                });
        
        // 客户端Bootstrap
        clientBootstrap = new Bootstrap();
        clientBootstrap.group(clientGroup)
                .channel(networkConfig.isUseEpoll() ? 
                        EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, networkConfig.isKeepAlive())
                .option(ChannelOption.TCP_NODELAY, networkConfig.isTcpNoDelay())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 空闲状态检测
                        pipeline.addLast(new IdleStateHandler(
                                (int)(networkConfig.getReadTimeoutMs() / 1000),
                                (int)(networkConfig.getWriteTimeoutMs() / 1000),
                                (int)(networkConfig.getHeartbeatTimeoutMs() / 1000)));
                        
                        // 长度字段解码器
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                networkConfig.getMaxMessageSize(), 0, 4, 0, 4));
                        pipeline.addLast(new LengthFieldPrepender(4));
                        
                        // 消息处理器
                        pipeline.addLast(new NetworkMessageHandler());
                    }
                });
    }
    
    private void startServer(int port) throws InterruptedException {
        ChannelFuture future = serverBootstrap.bind(port).sync();
        serverChannel = future.channel();
    }
    
    private void shutdownEventLoopGroups() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (clientGroup != null) {
            clientGroup.shutdownGracefully();
        }
    }
    
    private void closeAllConnections() {
        peerChannels.values().forEach(channel -> {
            if (channel.isActive()) {
                channel.close();
            }
        });
        peerChannels.clear();
    }
    
    private CompletableFuture<Void> broadcastMessage(NetworkMessage.MessageType type, Object payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                NetworkMessage message = createMessage(type, payload);
                List<PeerInfo> connectedPeers = getConnectedPeers();
                
                for (PeerInfo peer : connectedPeers) {
                    Channel channel = peerChannels.get(peer.getNodeId());
                    if (channel != null && channel.isActive()) {
                        sendMessageToChannel(channel, message);
                    }
                }
                
                logger.debug("广播消息成功，类型: {}, 目标节点数: {}", type, connectedPeers.size());
            } catch (Exception e) {
                logger.error("广播消息失败，类型: " + type, e);
            }
        });
    }
    
    private NetworkMessage createMessage(NetworkMessage.MessageType type, Object payload) {
        long timestamp = System.currentTimeMillis();
        long nonce = generateNonce();
        
        // 获取发送者公钥
        PublicKey sender = localKeyPair.getPublicKey();
        if (sender == null) {
            throw new IllegalStateException("Local key pair public key is null");
        }
        
        // 创建消息
        NetworkMessage message = new NetworkMessage(type, sender, 
                                                  timestamp, nonce, payload, new byte[64]);
        
        // 签名消息
        byte[] signature = localKeyPair.sign(message.serializeForSigning());
        if (signature == null) {
            throw new IllegalStateException("Failed to sign message");
        }
        
        // 重新创建带签名的消息
        return new NetworkMessage(type, sender, 
                               timestamp, nonce, payload, signature);
    }
    
    // private NetworkMessage createHeartbeatMessage() {
    //     return createMessage(NetworkMessage.MessageType.PING, null);
    // }
    
    private long generateNonce() {
        return System.currentTimeMillis() + Thread.currentThread().threadId();
    }
    
    /**
     * 验证消息签名
     * 
     * @param message 网络消息
     * @return true如果签名有效，false否则
     */
    private boolean verifyMessageSignature(NetworkMessage message) {
        try {
            if (!networkConfig.isEnableSignatureVerification()) {
                return true; // 如果未启用签名验证，直接返回true
            }
            
            // 获取发送者公钥
            PublicKey senderPublicKey = message.getSender();
            if (senderPublicKey == null) {
                return false;
            }
            
            // 验证签名
            byte[] signature = message.getSignature();
            byte[] dataToVerify = message.serializeForSigning();
            
            return Ed25519KeyPair.verify(senderPublicKey, dataToVerify, signature);
            
        } catch (Exception e) {
            logger.error("验证消息签名失败", e);
            return false;
        }
    }
    
    /**
     * 检查是否为重复消息
     * 
     * @param message 网络消息
     * @return true如果是重复消息，false否则
     */
    private boolean isDuplicateMessage(NetworkMessage message) {
        if (!networkConfig.isEnableMessageDeduplication()) {
            return false; // 如果未启用去重，直接返回false
        }
        
        try {
            String messageKey = generateMessageKey(message);
            
            // 检查是否已存在
            if (messageNonces.containsKey(messageKey)) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("检查重复消息失败", e);
            return false;
        }
    }
    
    /**
     * 检查消息是否过期
     * 
     * @param message 网络消息
     * @return true如果过期，false否则
     */
    private boolean isMessageExpired(NetworkMessage message) {
        try {
            long currentTime = System.currentTimeMillis();
            long messageAge = currentTime - message.getTimestamp();
            
            return messageAge > networkConfig.getMessageExpirationMs();
            
        } catch (Exception e) {
            logger.error("检查消息过期失败", e);
            return true; // 出错时认为消息过期
        }
    }
    
    /**
     * 记录消息
     * 
     * @param message 网络消息
     */
    private void recordMessage(NetworkMessage message) {
        try {
            String messageKey = generateMessageKey(message);
            
            // 记录nonce和时间戳
            messageNonces.put(messageKey, message.getNonce());
            messageTimestamps.put(messageKey, message.getTimestamp());
            
            // 定期清理过期的记录
            cleanupMessageRecords();
            
        } catch (Exception e) {
            logger.error("记录消息失败", e);
        }
    }
    
    /**
     * 生成消息唯一键
     * 
     * @param message 网络消息
     * @return 消息键
     */
    private String generateMessageKey(NetworkMessage message) {
        return message.getType().getValue() + ":" + 
               message.getSender().hashCode() + ":" + 
               message.getNonce() + ":" + 
               message.getTimestamp();
    }
    
    /**
     * 清理过期的消息记录
     */
    private void cleanupMessageRecords() {
        try {
            long currentTime = System.currentTimeMillis();
            long expirationTime = networkConfig.getMessageExpirationMs();
            
            // 清理过期的nonce记录
            messageNonces.entrySet().removeIf(entry -> {
                String messageKey = entry.getKey();
                Long timestamp = messageTimestamps.get(messageKey);
                return timestamp != null && (currentTime - timestamp) > expirationTime;
            });
            
            // 清理过期的时间戳记录
            messageTimestamps.entrySet().removeIf(entry -> 
                (currentTime - entry.getValue()) > expirationTime);
            
        } catch (Exception e) {
            logger.error("清理消息记录失败", e);
        }
    }
    
    /**
     * 获取对端公钥
     * 
     * @param channel 网络通道
     * @return 对端公钥
     */
    private PublicKey getPeerPublicKey(Channel channel) {
        try {
            InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
            String peerId = address.getHostString() + ":" + address.getPort();
            
            PeerInfo peerInfo = peers.get(peerId);
            if (peerInfo != null && peerInfo.getPublicKey() != null) {
                return peerInfo.getPublicKey();
            }
            
            // 如果无法获取对端公钥，返回本地公钥（用于测试）
            logger.debug("无法获取对端公钥，使用本地公钥: {}", peerId);
            return localKeyPair.getPublicKey();
            
        } catch (Exception e) {
            logger.error("获取对端公钥失败", e);
            return localKeyPair.getPublicKey();
        }
    }
    
    private void sendMessageToChannel(Channel channel, NetworkMessage message) {
        try {
            // 序列化消息
            byte[] data = objectMapper.writeValueAsBytes(message);
            
            // 获取对端公钥（用于加密）
            PublicKey peerPublicKey = getPeerPublicKey(channel);
            
            // 压缩消息
            byte[] compressedData = compressionService.compress(data);
            
            // 加密消息
            byte[] encryptedData = encryptionService.encrypt(compressedData, peerPublicKey);
            
            ByteBuf buffer = channel.alloc().buffer(encryptedData.length);
            buffer.writeBytes(encryptedData);
            
            channel.writeAndFlush(buffer).addListener(future -> {
                if (future.isSuccess()) {
                    messagesSent.incrementAndGet();
                    bytesSent.addAndGet(encryptedData.length);
                    lastActivityTime.set(System.currentTimeMillis());
                    
                    logger.debug("消息发送成功，原始大小: {} 字节，最终大小: {} 字节", 
                                data.length, encryptedData.length);
                } else {
                    logger.error("发送消息失败", future.cause());
                }
            });
        } catch (Exception e) {
            logger.error("序列化消息失败", e);
        }
    }
    
    private void notifyMessageReceived(NetworkMessage message, String senderId) {
        messageListeners.forEach(listener -> {
            try {
                listener.onMessageReceived(message, senderId);
            } catch (Exception e) {
                logger.error("消息监听器处理失败", e);
            }
        });
    }
    
    private void notifyMessageFailed(NetworkMessage message, String targetId, Throwable error) {
        messageListeners.forEach(listener -> {
            try {
                listener.onMessageFailed(message, targetId, error);
            } catch (Exception e) {
                logger.error("消息监听器处理失败", e);
            }
        });
    }
    
    private void notifyPeerConnected(PeerInfo peerInfo) {
        peerListeners.forEach(listener -> {
            try {
                listener.onPeerConnected(peerInfo);
            } catch (Exception e) {
                logger.error("节点监听器处理失败", e);
            }
        });
    }
    
    private void notifyPeerDisconnected(PeerInfo peerInfo) {
        peerListeners.forEach(listener -> {
            try {
                listener.onPeerDisconnected(peerInfo);
            } catch (Exception e) {
                logger.error("节点监听器处理失败", e);
            }
        });
    }
    
    private void notifyPeerConnectionFailed(PeerInfo peerInfo, Throwable error) {
        peerListeners.forEach(listener -> {
            try {
                listener.onPeerConnectionFailed(peerInfo, error);
            } catch (Exception e) {
                logger.error("节点监听器处理失败", e);
            }
        });
    }
    
    /**
     * 网络消息处理器
     */
    private class NetworkMessageHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            String peerId = address.getHostString() + ":" + address.getPort();
            
            logger.info("新连接建立: {}", peerId);
            
            // 添加到连接映射
            peerChannels.put(peerId, ctx.channel());
            
            // 创建节点信息
            PeerInfo peerInfo = new PeerInfo(peerId, null, address.getHostString(), 
                                           address.getPort(), PeerInfo.PeerStatus.CONNECTED, 
                                           System.currentTimeMillis(), 0, 0);
            peers.put(peerId, peerInfo);
            
            notifyPeerConnected(peerInfo);
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            String peerId = address.getHostString() + ":" + address.getPort();
            
            logger.info("连接断开: {}", peerId);
            
            // 从连接映射中移除
            peerChannels.remove(peerId);
            
            // 更新节点状态
            PeerInfo peerInfo = peers.get(peerId);
            if (peerInfo != null) {
                PeerInfo disconnectedPeer = peerInfo.withStatus(PeerInfo.PeerStatus.DISCONNECTED);
                peers.put(peerId, disconnectedPeer);
                notifyPeerDisconnected(disconnectedPeer);
            }
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                ByteBuf buffer = (ByteBuf) msg;
                byte[] encryptedData = new byte[buffer.readableBytes()];
                buffer.readBytes(encryptedData);
                
                // 获取对端公钥（用于解密）
                PublicKey peerPublicKey = getPeerPublicKey(ctx.channel());
                
                // 解密消息
                byte[] compressedData = encryptionService.decrypt(encryptedData, peerPublicKey);
                
                // 解压缩消息
                byte[] data = compressionService.decompress(compressedData);
                
                NetworkMessage message = objectMapper.readValue(data, NetworkMessage.class);
                
                // 验证消息
                if (message.isValidFormat()) {
                    InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                    String senderId = address.getHostString() + ":" + address.getPort();
                    
                    // 验证消息签名
                    if (!verifyMessageSignature(message)) {
                        logger.warn("消息签名验证失败，来自: {}", senderId);
                        return;
                    }
                    
                    // 检查消息去重
                    if (isDuplicateMessage(message)) {
                        logger.debug("重复消息，忽略: {} 来自: {}", message.getType(), senderId);
                        return;
                    }
                    
                    // 检查消息时间戳
                    if (isMessageExpired(message)) {
                        logger.debug("过期消息，忽略: {} 来自: {}", message.getType(), senderId);
                        return;
                    }
                    
                    // 记录消息
                    recordMessage(message);
                    
                    messagesReceived.incrementAndGet();
                    bytesReceived.addAndGet(data.length);
                    lastActivityTime.set(System.currentTimeMillis());
                    
                    // 通知监听器
                    notifyMessageReceived(message, senderId);
                    
                    logger.debug("接收到消息: {} 来自: {}", message.getType(), senderId);
                } else {
                    logger.warn("接收到无效消息格式");
                }
            } catch (Exception e) {
                logger.error("处理接收消息失败", e);
            } finally {
                ((ByteBuf) msg).release();
            }
        }
        
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                switch (event.state()) {
                    case READER_IDLE:
                        logger.debug("读取超时，发送心跳");
                        sendHeartbeat();
                        break;
                    case WRITER_IDLE:
                        logger.debug("写入超时，发送心跳");
                        sendHeartbeat();
                        break;
                    case ALL_IDLE:
                        logger.debug("连接空闲，关闭连接");
                        ctx.close();
                        break;
                }
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("网络处理异常", cause);
            ctx.close();
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (running.get()) {
            stop().join();
        }
    }
}
