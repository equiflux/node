package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * RPC响应对象
 * 
 * <p>遵循JSON-RPC 2.0规范，支持单个响应和批量响应。
 * 
 * <p>主要字段：
 * <ul>
 *   <li>jsonrpc: JSON-RPC版本，固定为"2.0"</li>
 *   <li>result: 成功时的结果数据</li>
 *   <li>error: 错误时的错误信息</li>
 *   <li>id: 请求ID，与请求中的ID对应</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class RpcResponse {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    
    @JsonProperty("result")
    private Object result;
    
    @JsonProperty("error")
    private RpcError error;
    
    @JsonProperty("id")
    private Object id;
    
    /**
     * 默认构造函数
     */
    public RpcResponse() {
    }
    
    /**
     * 成功响应构造函数
     * 
     * @param result 结果数据
     * @param id 请求ID
     */
    public RpcResponse(Object result, Object id) {
        this.jsonrpc = "2.0";
        this.result = result;
        this.id = id;
    }
    
    /**
     * 错误响应构造函数
     * 
     * @param error 错误信息
     * @param id 请求ID
     */
    public RpcResponse(RpcError error, Object id) {
        this.jsonrpc = "2.0";
        this.error = error;
        this.id = id;
    }
    
    /**
     * 创建成功响应
     * 
     * @param result 结果数据
     * @param id 请求ID
     * @return 成功响应
     */
    public static RpcResponse success(Object result, Object id) {
        return new RpcResponse(result, id);
    }
    
    /**
     * 创建错误响应
     * 
     * @param code 错误代码
     * @param message 错误消息
     * @param id 请求ID
     * @return 错误响应
     */
    public static RpcResponse error(int code, String message, Object id) {
        return new RpcResponse(new RpcError(code, message), id);
    }
    
    /**
     * 创建错误响应（带数据）
     * 
     * @param code 错误代码
     * @param message 错误消息
     * @param data 错误数据
     * @param id 请求ID
     * @return 错误响应
     */
    public static RpcResponse error(int code, String message, Object data, Object id) {
        return new RpcResponse(new RpcError(code, message, data), id);
    }
    
    /**
     * 创建批量响应
     * 
     * @param responses 响应列表
     * @return 批量响应
     */
    public static List<RpcResponse> batch(List<RpcResponse> responses) {
        return responses;
    }
    
    /**
     * 获取JSON-RPC版本
     * 
     * @return JSON-RPC版本
     */
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    /**
     * 设置JSON-RPC版本
     * 
     * @param jsonrpc JSON-RPC版本
     */
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }
    
    /**
     * 获取结果数据
     * 
     * @return 结果数据
     */
    public Object getResult() {
        return result;
    }
    
    /**
     * 设置结果数据
     * 
     * @param result 结果数据
     */
    public void setResult(Object result) {
        this.result = result;
    }
    
    /**
     * 获取错误信息
     * 
     * @return 错误信息
     */
    public RpcError getError() {
        return error;
    }
    
    /**
     * 设置错误信息
     * 
     * @param error 错误信息
     */
    public void setError(RpcError error) {
        this.error = error;
    }
    
    /**
     * 获取请求ID
     * 
     * @return 请求ID
     */
    public Object getId() {
        return id;
    }
    
    /**
     * 设置请求ID
     * 
     * @param id 请求ID
     */
    public void setId(Object id) {
        this.id = id;
    }
    
    /**
     * 检查是否为成功响应
     * 
     * @return true如果是成功响应
     */
    public boolean isSuccess() {
        return error == null;
    }
    
    /**
     * 检查是否为错误响应
     * 
     * @return true如果是错误响应
     */
    public boolean isError() {
        return error != null;
    }
    
    @Override
    public String toString() {
        return "RpcResponse{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", result=" + result +
                ", error=" + error +
                ", id=" + id +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RpcResponse that = (RpcResponse) obj;
        return Objects.equals(jsonrpc, that.jsonrpc) &&
               Objects.equals(result, that.result) &&
               Objects.equals(error, that.error) &&
               Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, result, error, id);
    }
}
