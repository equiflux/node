package io.equiflux.node.rpc.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RPC Web配置测试
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "equiflux.rpc.enable-cors=true",
    "equiflux.rpc.cors-allowed-origins=*",
    "equiflux.rpc.cors-allowed-methods=GET,POST,PUT,DELETE,OPTIONS",
    "equiflux.rpc.cors-allowed-headers=*"
})
class RpcWebConfigTest {
    
    @Mock
    private RpcConfig rpcConfig;
    
    private RpcWebConfig webConfig;
    
    @BeforeEach
    void setUp() {
        webConfig = new RpcWebConfig(rpcConfig);
    }
    
    @Test
    void testCorsConfiguration() {
        // Given
        when(rpcConfig.isEnableCors()).thenReturn(true);
        when(rpcConfig.getCorsAllowedOrigins()).thenReturn(new String[]{"*"});
        when(rpcConfig.getCorsAllowedMethods()).thenReturn(new String[]{"GET", "POST"});
        when(rpcConfig.getCorsAllowedHeaders()).thenReturn(new String[]{"Content-Type"});
        
        // When & Then
        assertDoesNotThrow(() -> {
            // 测试CORS配置的具体实现
            var corsSource = webConfig.corsConfigurationSource();
            assertNotNull(corsSource);
        });
    }
    
    @Test
    void testCorsFilter() {
        // Given
        when(rpcConfig.isEnableCors()).thenReturn(true);
        when(rpcConfig.getCorsAllowedOrigins()).thenReturn(new String[]{"*"});
        when(rpcConfig.getCorsAllowedMethods()).thenReturn(new String[]{"GET", "POST"});
        when(rpcConfig.getCorsAllowedHeaders()).thenReturn(new String[]{"Content-Type"});
        
        // When & Then
        assertDoesNotThrow(() -> {
            // 测试CORS过滤器配置
            var filter = webConfig.corsFilter();
            assertNotNull(filter);
        });
    }
}
