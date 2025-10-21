package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * RPC错误对象
 * 
 * <p>遵循JSON-RPC 2.0规范，用于表示RPC调用中的错误信息。
 * 
 * <p>主要字段：
 * <ul>
 *   <li>code: 错误代码</li>
 *   <li>message: 错误消息</li>
 *   <li>data: 可选的错误数据</li>
 * </ul>
 * 
 * <p>标准错误代码：
 * <ul>
 *   <li>-32700: Parse error</li>
 *   <li>-32600: Invalid Request</li>
 *   <li>-32601: Method not found</li>
 *   <li>-32602: Invalid params</li>
 *   <li>-32603: Internal error</li>
 *   <li>-32000 to -32099: Server error</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class RpcError {
    
    @JsonProperty("code")
    private int code;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private Object data;
    
    // 标准错误代码
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    
    // 服务器错误代码范围
    public static final int SERVER_ERROR_MIN = -32099;
    public static final int SERVER_ERROR_MAX = -32000;
    
    // Equiflux特定错误代码
    public static final int BLOCK_NOT_FOUND = -32001;
    public static final int TRANSACTION_NOT_FOUND = -32002;
    public static final int ACCOUNT_NOT_FOUND = -32003;
    public static final int INSUFFICIENT_BALANCE = -32004;
    public static final int INVALID_SIGNATURE = -32005;
    public static final int INVALID_NONCE = -32006;
    public static final int STORAGE_ERROR = -32007;
    public static final int NETWORK_ERROR = -32008;
    public static final int CONSENSUS_ERROR = -32009;
    public static final int VALIDATION_ERROR = -32010;
    
    /**
     * 默认构造函数
     */
    public RpcError() {
    }
    
    /**
     * 构造函数
     * 
     * @param code 错误代码
     * @param message 错误消息
     */
    public RpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 构造函数（带数据）
     * 
     * @param code 错误代码
     * @param message 错误消息
     * @param data 错误数据
     */
    public RpcError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 创建解析错误
     * 
     * @param message 错误消息
     * @return 解析错误
     */
    public static RpcError parseError(String message) {
        return new RpcError(PARSE_ERROR, message);
    }
    
    /**
     * 创建无效请求错误
     * 
     * @param message 错误消息
     * @return 无效请求错误
     */
    public static RpcError invalidRequest(String message) {
        return new RpcError(INVALID_REQUEST, message);
    }
    
    /**
     * 创建方法未找到错误
     * 
     * @param method 方法名
     * @return 方法未找到错误
     */
    public static RpcError methodNotFound(String method) {
        return new RpcError(METHOD_NOT_FOUND, "Method not found: " + method);
    }
    
    /**
     * 创建无效参数错误
     * 
     * @param message 错误消息
     * @return 无效参数错误
     */
    public static RpcError invalidParams(String message) {
        return new RpcError(INVALID_PARAMS, message);
    }
    
    /**
     * 创建内部错误
     * 
     * @param message 错误消息
     * @return 内部错误
     */
    public static RpcError internalError(String message) {
        return new RpcError(INTERNAL_ERROR, message);
    }
    
    /**
     * 创建服务器错误
     * 
     * @param code 错误代码（-32000到-32099之间）
     * @param message 错误消息
     * @return 服务器错误
     */
    public static RpcError serverError(int code, String message) {
        if (code < SERVER_ERROR_MIN || code > SERVER_ERROR_MAX) {
            throw new IllegalArgumentException("Server error code must be between " + SERVER_ERROR_MIN + " and " + SERVER_ERROR_MAX);
        }
        return new RpcError(code, message);
    }
    
    /**
     * 创建服务器错误（带数据）
     * 
     * @param code 错误代码（-32000到-32099之间）
     * @param message 错误消息
     * @param data 错误数据
     * @return 服务器错误
     */
    public static RpcError serverError(int code, String message, Object data) {
        if (code < SERVER_ERROR_MIN || code > SERVER_ERROR_MAX) {
            throw new IllegalArgumentException("Server error code must be between " + SERVER_ERROR_MIN + " and " + SERVER_ERROR_MAX);
        }
        return new RpcError(code, message, data);
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 设置错误代码
     * 
     * @param code 错误代码
     */
    public void setCode(int code) {
        this.code = code;
    }
    
    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置错误消息
     * 
     * @param message 错误消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * 获取错误数据
     * 
     * @return 错误数据
     */
    public Object getData() {
        return data;
    }
    
    /**
     * 设置错误数据
     * 
     * @param data 错误数据
     */
    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * 检查是否为标准错误
     * 
     * @return true如果是标准错误
     */
    public boolean isStandardError() {
        return code >= -32700 && code <= -32600;
    }
    
    /**
     * 检查是否为服务器错误
     * 
     * @return true如果是服务器错误
     */
    public boolean isServerError() {
        return code >= SERVER_ERROR_MIN && code <= SERVER_ERROR_MAX;
    }
    
    @Override
    public String toString() {
        return "RpcError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RpcError rpcError = (RpcError) obj;
        return code == rpcError.code &&
               Objects.equals(message, rpcError.message) &&
               Objects.equals(data, rpcError.data);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
    }
}
