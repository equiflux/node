package io.equiflux.node.network;

/**
 * 消息监听器接口
 * 
 * <p>用于监听网络消息的回调接口。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public interface MessageListener {
    
    /**
     * 处理接收到的消息
     * 
     * @param message 网络消息
     * @param senderId 发送者ID
     */
    void onMessageReceived(NetworkMessage message, String senderId);
    
    /**
     * 处理消息发送失败
     * 
     * @param message 网络消息
     * @param targetId 目标节点ID
     * @param error 错误信息
     */
    void onMessageFailed(NetworkMessage message, String targetId, Throwable error);
}
