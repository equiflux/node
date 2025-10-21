package io.equiflux.node.network;

import io.equiflux.node.model.Block;
import io.equiflux.node.model.Transaction;
import io.equiflux.node.model.VRFAnnouncement;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 网络服务接口
 * 
 * <p>定义P2P网络服务的核心功能，包括消息发送、接收、节点管理等功能。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>消息广播和发送</li>
 *   <li>节点发现和管理</li>
 *   <li>网络连接管理</li>
 *   <li>消息路由和转发</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public interface NetworkService {
    
    /**
     * 启动网络服务
     * 
     * @param port 监听端口
     * @return 启动结果
     */
    CompletableFuture<Void> start(int port);
    
    /**
     * 停止网络服务
     * 
     * @return 停止结果
     */
    CompletableFuture<Void> stop();
    
    /**
     * 检查服务是否运行
     * 
     * @return true如果运行中，false否则
     */
    boolean isRunning();
    
    /**
     * 广播区块提议
     * 
     * @param block 区块
     * @return 广播结果
     */
    CompletableFuture<Void> broadcastBlockProposal(Block block);
    
    /**
     * 广播区块投票
     * 
     * @param block 区块
     * @return 广播结果
     */
    CompletableFuture<Void> broadcastBlockVote(Block block);
    
    /**
     * 广播交易
     * 
     * @param transaction 交易
     * @return 广播结果
     */
    CompletableFuture<Void> broadcastTransaction(Transaction transaction);
    
    /**
     * 广播VRF公告
     * 
     * @param announcement VRF公告
     * @return 广播结果
     */
    CompletableFuture<Void> broadcastVRFAnnouncement(VRFAnnouncement announcement);
    
    /**
     * 发送消息到指定节点
     * 
     * @param peerId 节点ID
     * @param message 消息
     * @return 发送结果
     */
    CompletableFuture<Void> sendMessage(String peerId, NetworkMessage message);
    
    /**
     * 发送消息到指定地址
     * 
     * @param host 主机地址
     * @param port 端口号
     * @param message 消息
     * @return 发送结果
     */
    CompletableFuture<Void> sendMessage(String host, int port, NetworkMessage message);
    
    /**
     * 连接到指定节点
     * 
     * @param host 主机地址
     * @param port 端口号
     * @return 连接结果
     */
    CompletableFuture<Void> connectToPeer(String host, int port);
    
    /**
     * 断开与指定节点的连接
     * 
     * @param peerId 节点ID
     * @return 断开结果
     */
    CompletableFuture<Void> disconnectFromPeer(String peerId);
    
    /**
     * 获取所有连接的节点
     * 
     * @return 节点列表
     */
    List<PeerInfo> getConnectedPeers();
    
    /**
     * 获取指定节点信息
     * 
     * @param peerId 节点ID
     * @return 节点信息，如果不存在则返回null
     */
    PeerInfo getPeerInfo(String peerId);
    
    /**
     * 获取节点数量
     * 
     * @return 节点数量
     */
    int getPeerCount();
    
    /**
     * 检查是否连接到指定节点
     * 
     * @param peerId 节点ID
     * @return true如果已连接，false否则
     */
    boolean isConnectedToPeer(String peerId);
    
    /**
     * 添加消息监听器
     * 
     * @param listener 消息监听器
     */
    void addMessageListener(MessageListener listener);
    
    /**
     * 移除消息监听器
     * 
     * @param listener 消息监听器
     */
    void removeMessageListener(MessageListener listener);
    
    /**
     * 添加节点监听器
     * 
     * @param listener 节点监听器
     */
    void addPeerListener(PeerListener listener);
    
    /**
     * 移除节点监听器
     * 
     * @param listener 节点监听器
     */
    void removePeerListener(PeerListener listener);
    
    /**
     * 获取本地节点信息
     * 
     * @return 本地节点信息
     */
    PeerInfo getLocalPeerInfo();
    
    /**
     * 获取网络统计信息
     * 
     * @return 网络统计信息
     */
    NetworkStats getNetworkStats();
    
    /**
     * 发现新节点
     * 
     * @return 发现结果
     */
    CompletableFuture<List<PeerInfo>> discoverPeers();
    
    /**
     * 同步区块数据
     * 
     * @param startHeight 起始高度
     * @param endHeight 结束高度
     * @return 同步结果
     */
    CompletableFuture<List<Block>> syncBlocks(long startHeight, long endHeight);
    
    /**
     * 发送心跳
     * 
     * @return 心跳结果
     */
    CompletableFuture<Void> sendHeartbeat();
    
    /**
     * 检查网络健康状态
     * 
     * @return true如果健康，false否则
     */
    boolean isHealthy();
}
