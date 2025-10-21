package io.equiflux.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Equiflux公链核心应用主类
 * 
 * <p>基于三层混合共识机制的高性能区块链公链：
 * <ul>
 *   <li>Layer 1: PoS治理层 - 超级节点选举和权益管理</li>
 *   <li>Layer 2: VRF选择层 - 可验证随机函数实现和领导者选择</li>
 *   <li>Layer 3: LPoW防护层 - 轻量级工作量证明和动态难度调整</li>
 * </ul>
 * 
 * <p>核心特性：
 * <ul>
 *   <li>目标TPS: ≈1800 (基准)</li>
 *   <li>出块时间: 3秒</li>
 *   <li>确认时间: 8秒</li>
 *   <li>超级节点数: 50个</li>
 * </ul>
 * 
 * @author Equiflux Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@SpringBootApplication
@EnableConfigurationProperties
public class EquifluxApplication {

    /**
     * 应用主入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EquifluxApplication.class, args);
    }
}
