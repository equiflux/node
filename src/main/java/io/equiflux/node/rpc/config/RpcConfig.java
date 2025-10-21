package io.equiflux.node.rpc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * RPC配置类
 * 
 * <p>配置RPC服务的相关参数，包括端口、超时、限制等。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Configuration
@ConfigurationProperties(prefix = "equiflux.rpc")
@Data
public class RpcConfig {
    
    /**
     * RPC服务端口
     */
    private int port = 8080;
    
    /**
     * 是否启用RPC服务
     */
    private boolean enabled = true;
    
    /**
     * 最大请求大小（字节）
     */
    private long maxRequestSize = 1048576; // 1MB
    
    /**
     * 请求超时时间（毫秒）
     */
    private long requestTimeoutMs = 30000; // 30秒
    
    /**
     * 最大并发请求数
     */
    private int maxConcurrentRequests = 100;
    
    /**
     * 是否启用CORS
     */
    private boolean enableCors = true;
    
    /**
     * CORS允许的源
     */
    private String[] corsAllowedOrigins = {"*"};
    
    /**
     * CORS允许的方法
     */
    private String[] corsAllowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};
    
    /**
     * CORS允许的头部
     */
    private String[] corsAllowedHeaders = {"*"};
    
    /**
     * 是否启用请求日志
     */
    private boolean enableRequestLogging = true;
    
    /**
     * 是否启用响应日志
     */
    private boolean enableResponseLogging = false;
    
    /**
     * 日志级别
     */
    private String logLevel = "INFO";
    
    /**
     * 是否启用方法统计
     */
    private boolean enableMethodStats = true;
    
    /**
     * 统计窗口大小（秒）
     */
    private int statsWindowSeconds = 60;
    
    /**
     * 是否启用限流
     */
    private boolean enableRateLimit = true;
    
    /**
     * 限流速率（请求/秒）
     */
    private int rateLimitPerSecond = 100;
    
    /**
     * 限流突发大小
     */
    private int rateLimitBurstSize = 200;
    
    /**
     * 是否启用认证
     */
    private boolean enableAuth = false;
    
    /**
     * 认证令牌
     */
    private String authToken;
    
    /**
     * 是否启用SSL
     */
    private boolean enableSsl = false;
    
    /**
     * SSL证书文件路径
     */
    private String sslCertFile;
    
    /**
     * SSL私钥文件路径
     */
    private String sslKeyFile;
    
    /**
     * 是否启用压缩
     */
    private boolean enableCompression = true;
    
    /**
     * 压缩级别
     */
    private int compressionLevel = 6;
    
    /**
     * 是否启用Keep-Alive
     */
    private boolean enableKeepAlive = true;
    
    /**
     * Keep-Alive超时时间（毫秒）
     */
    private long keepAliveTimeoutMs = 30000; // 30秒
    
    /**
     * 最大Keep-Alive请求数
     */
    private int maxKeepAliveRequests = 100;
    
    /**
     * 是否启用HTTP/2
     */
    private boolean enableHttp2 = true;
    
    /**
     * 线程池配置
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();
    
    /**
     * 线程池配置
     */
    @Data
    public static class ThreadPoolConfig {
        /**
         * 核心线程数
         */
        private int coreSize = 10;
        
        /**
         * 最大线程数
         */
        private int maxSize = 50;
        
        /**
         * 队列容量
         */
        private int queueCapacity = 1000;
        
        /**
         * 线程空闲时间（秒）
         */
        private int keepAliveSeconds = 60;
        
        /**
         * 线程名前缀
         */
        private String threadNamePrefix = "rpc-";
    }
    
    // 手动添加getter方法以确保编译通过
    public boolean isEnableCors() {
        return enableCors;
    }
    
    public String[] getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }
    
    public String[] getCorsAllowedMethods() {
        return corsAllowedMethods;
    }
    
    public String[] getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }
}
