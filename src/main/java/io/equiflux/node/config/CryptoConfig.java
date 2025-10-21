package io.equiflux.node.config;

import io.equiflux.node.crypto.Ed25519KeyPair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 密码学配置类
 * 
 * <p>提供密码学相关的Bean定义，包括密钥对生成等。
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Configuration
public class CryptoConfig {
    
    /**
     * 创建本地节点密钥对
     * 
     * @return 本地节点的Ed25519密钥对
     */
    @Bean
    public Ed25519KeyPair localKeyPair() {
        return Ed25519KeyPair.generate();
    }
}
