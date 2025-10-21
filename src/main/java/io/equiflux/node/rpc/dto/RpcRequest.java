package io.equiflux.node.rpc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RPC请求对象
 * 
 * <p>遵循JSON-RPC 2.0规范，支持单个请求和批量请求。
 * 
 * <p>主要字段：
 * <ul>
 *   <li>jsonrpc: JSON-RPC版本，固定为"2.0"</li>
 *   <li>method: 要调用的方法名</li>
 *   <li>params: 方法参数</li>
 *   <li>id: 请求ID，用于匹配响应</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
public class RpcRequest {
    
    @JsonProperty("jsonrpc")
    private String jsonrpc;
    
    @NotBlank(message = "Method name cannot be empty")
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("params")
    private Object params;
    
    @NotNull(message = "Request ID is required")
    @JsonProperty("id")
    private Object id;
    
    /**
     * 默认构造函数
     */
    public RpcRequest() {
    }
    
    /**
     * 构造函数
     * 
     * @param method 方法名
     * @param params 参数
     * @param id 请求ID
     */
    public RpcRequest(String method, Object params, Object id) {
        this.jsonrpc = "2.0";
        this.method = method;
        this.params = params;
        this.id = id;
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
     * 获取方法名
     * 
     * @return 方法名
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * 设置方法名
     * 
     * @param method 方法名
     */
    public void setMethod(String method) {
        this.method = method;
    }
    
    /**
     * 获取参数
     * 
     * @return 参数
     */
    public Object getParams() {
        return params;
    }
    
    /**
     * 设置参数
     * 
     * @param params 参数
     */
    public void setParams(Object params) {
        this.params = params;
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
     * 获取参数作为Map
     * 
     * @return 参数Map
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParamsAsMap() {
        if (params instanceof Map) {
            return (Map<String, Object>) params;
        }
        return null;
    }
    
    /**
     * 获取参数作为List
     * 
     * @return 参数List
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public List<Object> getParamsAsList() {
        if (params instanceof List) {
            return (List<Object>) params;
        }
        return null;
    }
    
    /**
     * 检查是否为批量请求
     * 
     * @return true如果是批量请求
     */
    public boolean isBatchRequest() {
        return params instanceof List;
    }
    
    @Override
    public String toString() {
        return "RpcRequest{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", method='" + method + '\'' +
                ", params=" + params +
                ", id=" + id +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RpcRequest that = (RpcRequest) obj;
        return Objects.equals(jsonrpc, that.jsonrpc) &&
               Objects.equals(method, that.method) &&
               Objects.equals(params, that.params) &&
               Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(jsonrpc, method, params, id);
    }
}
