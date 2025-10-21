package io.equiflux.node.network;

/**
 * 节点监听器接口
 * 
 * <p>用于监听节点连接状态变化的回调接口。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public interface PeerListener {
    
    /**
     * 处理节点连接
     * 
     * @param peerInfo 节点信息
     */
    void onPeerConnected(PeerInfo peerInfo);
    
    /**
     * 处理节点断开连接
     * 
     * @param peerInfo 节点信息
     */
    void onPeerDisconnected(PeerInfo peerInfo);
    
    /**
     * 处理节点连接失败
     * 
     * @param peerInfo 节点信息
     * @param error 错误信息
     */
    void onPeerConnectionFailed(PeerInfo peerInfo, Throwable error);
    
    /**
     * 处理发现新节点
     * 
     * @param peerInfo 节点信息
     */
    void onPeerDiscovered(PeerInfo peerInfo);
}
