package io.equiflux.node.rpc.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Objects;

/**
 * RPC Web配置
 * 
 * <p>配置RPC服务的Web相关设置，包括CORS、过滤器等。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Configuration
public class RpcWebConfig {
    
    private final RpcConfig rpcConfig;
    
    public RpcWebConfig(RpcConfig rpcConfig) {
        this.rpcConfig = rpcConfig;
    }
    
    /**
     * 配置CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        if (rpcConfig.isEnableCors()) {
            configuration.setAllowedOriginPatterns(Arrays.asList(rpcConfig.getCorsAllowedOrigins()));
            configuration.setAllowedMethods(Arrays.asList(rpcConfig.getCorsAllowedMethods()));
            configuration.setAllowedHeaders(Arrays.asList(rpcConfig.getCorsAllowedHeaders()));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);
        }
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/rpc/**", configuration);
        
        return source;
    }
    
    /**
     * 注册CORS过滤器
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorsFilter(corsConfigurationSource()));
        registration.addUrlPatterns("/rpc/*");
        registration.setName("corsFilter");
        registration.setOrder(1);
        return registration;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RpcWebConfig that = (RpcWebConfig) obj;
        return Objects.equals(rpcConfig, that.rpcConfig);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rpcConfig);
    }
}
