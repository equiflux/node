package io.equiflux.node.rpc.exception;

import io.equiflux.node.rpc.dto.RpcError;
import io.equiflux.node.rpc.dto.RpcResponse;

/**
 * RPC异常基类
 * 
 * <p>所有RPC相关异常的基类，包含RPC错误信息。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class RpcException extends RuntimeException {
    
    private final RpcError rpcError;
    
    /**
     * 构造函数
     * 
     * @param rpcError RPC错误信息
     */
    public RpcException(RpcError rpcError) {
        super(rpcError.getMessage());
        this.rpcError = rpcError;
    }
    
    /**
     * 构造函数
     * 
     * @param rpcError RPC错误信息
     * @param cause 原因异常
     */
    public RpcException(RpcError rpcError, Throwable cause) {
        super(rpcError.getMessage(), cause);
        this.rpcError = rpcError;
    }
    
    /**
     * 构造函数
     * 
     * @param code 错误代码
     * @param message 错误消息
     */
    public RpcException(int code, String message) {
        super(message);
        this.rpcError = new RpcError(code, message);
    }
    
    /**
     * 构造函数
     * 
     * @param code 错误代码
     * @param message 错误消息
     * @param cause 原因异常
     */
    public RpcException(int code, String message, Throwable cause) {
        super(message, cause);
        this.rpcError = new RpcError(code, message);
    }
    
    /**
     * 构造函数
     * 
     * @param code 错误代码
     * @param message 错误消息
     * @param data 错误数据
     */
    public RpcException(int code, String message, Object data) {
        super(message);
        this.rpcError = new RpcError(code, message, data);
    }
    
    /**
     * 构造函数
     * 
     * @param code 错误代码
     * @param message 错误消息
     * @param data 错误数据
     * @param cause 原因异常
     */
    public RpcException(int code, String message, Object data, Throwable cause) {
        super(message, cause);
        this.rpcError = new RpcError(code, message, data);
    }
    
    /**
     * 获取RPC错误信息
     * 
     * @return RPC错误信息
     */
    public RpcError getRpcError() {
        return rpcError;
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public int getCode() {
        return rpcError.getCode();
    }
    
    /**
     * 获取错误数据
     * 
     * @return 错误数据
     */
    public Object getData() {
        return rpcError.getData();
    }
    
    /**
     * 创建RPC响应
     * 
     * @param id 请求ID
     * @return RPC响应
     */
    public RpcResponse toRpcResponse(Object id) {
        return RpcResponse.error(rpcError.getCode(), rpcError.getMessage(), rpcError.getData(), id);
    }
}
