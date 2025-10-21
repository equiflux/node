# Equiflux Core

Equiflux公链核心实现 - 基于三层混合共识机制的高性能区块链

## 概述

Equiflux是一个创新的区块链公链，采用PoS + VRF + 轻量级PoW的三层混合共识机制，目标实现约1800 TPS的性能和8秒确认时间。

## 核心特性

- **三层混合共识**: PoS治理层 + VRF选择层 + LPoW防护层
- **完全透明VRF**: 区块包含所有VRF公告，实时可验证
- **高性能**: 目标1800 TPS，3秒出块，8秒确认
- **低能耗**: 轻量级PoW，仅用于增加作恶成本
- **强去中心化**: 50个超级节点，动态轮换

## 技术栈

- **Java 17+**: 主要开发语言
- **Spring Boot 3.x**: 应用框架
- **Maven**: 构建工具
- **JUnit 5**: 测试框架
- **SLF4J + Logback**: 日志框架

## 项目结构

```
core/
├── src/main/java/com/equiflux/
│   ├── EquifluxApplication.java          # Spring Boot主类
│   ├── crypto/                           # 密码学模块
│   │   ├── Ed25519KeyPair.java          # Ed25519密钥对
│   │   ├── VRFKeyPair.java              # VRF密钥对
│   │   ├── VRFCalculator.java           # VRF计算器
│   │   ├── SignatureVerifier.java       # 签名验证器
│   │   └── HashUtils.java               # 哈希工具
│   ├── model/                            # 数据模型
│   │   ├── Block.java                   # 区块
│   │   ├── Transaction.java             # 交易
│   │   ├── VRFAnnouncement.java         # VRF公告
│   │   ├── VRFOutput.java               # VRF输出
│   │   └── VRFProof.java                # VRF证明
│   ├── consensus/                        # 共识引擎
│   │   ├── ConsensusEngine.java         # 共识引擎接口
│   │   ├── EquifluxConsensus.java       # Equiflux共识实现
│   │   ├── VRFCollector.java            # VRF收集器
│   │   ├── BlockProposer.java           # 区块提议器
│   │   └── BlockValidator.java          # 区块验证器
│   ├── consensus/vrf/                    # VRF相关
│   │   ├── VRFRoundResult.java          # VRF轮次结果
│   │   └── ScoreCalculator.java         # 分数计算器
│   ├── consensus/pow/                     # PoW相关
│   │   ├── PoWMiner.java                # PoW矿工
│   │   └── DifficultyCalculator.java    # 难度计算器
│   ├── exception/                        # 异常类
│   │   ├── ConsensusException.java
│   │   ├── CryptoException.java
│   │   └── ValidationException.java
│   ├── config/                           # 配置类
│   │   └── ConsensusConfig.java          # 共识配置
│   └── demo/                             # 演示程序
│       └── EquifluxDemo.java            # 演示程序
├── src/main/resources/
│   ├── application.yml                   # 应用配置
│   └── logback.xml                       # 日志配置
└── src/test/java/com/equiflux/          # 测试代码
    ├── crypto/                           # 密码学测试
    ├── model/                            # 模型测试
    └── consensus/                        # 共识测试
```

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.8+
- Git

### 2. 克隆项目

```bash
git clone <repository-url>
cd equiflux/group/core
```

### 3. 编译项目

```bash
mvn clean compile
```

### 4. 运行测试

```bash
mvn test
```

### 5. 运行演示程序

```bash
mvn spring-boot:run
```

## 核心模块说明

### 密码学模块 (crypto)

- **HashUtils**: SHA-256哈希计算，支持Merkle根计算
- **Ed25519KeyPair**: Ed25519数字签名，用于区块和交易签名
- **VRFKeyPair**: VRF密钥对，用于可验证随机函数
- **VRFCalculator**: VRF计算器，计算VRF分数
- **SignatureVerifier**: 签名验证器，验证区块和交易签名

### 数据模型 (model)

- **Block**: 区块结构，包含所有VRF公告（约5KB）
- **Transaction**: 交易结构，支持转账和手续费
- **VRFAnnouncement**: VRF公告，包含轮次、公钥、输出、证明、分数
- **VRFOutput**: VRF输出，32字节的伪随机值
- **VRFProof**: VRF证明，64字节的EdDSA签名

### 共识引擎 (consensus)

- **EquifluxConsensus**: 主共识引擎，整合所有组件
- **VRFCollector**: VRF收集器，负责3秒VRF收集阶段
- **BlockProposer**: 区块提议器，负责区块构造和PoW
- **BlockValidator**: 区块验证器，实现5步验证流程
- **ScoreCalculator**: 分数计算器，计算VRF分数

### PoW模块 (consensus/pow)

- **PoWMiner**: 轻量级PoW矿工，2-3秒可完成
- **DifficultyCalculator**: 难度计算器，动态调整难度

## 共识流程

### Phase 1: VRF收集阶段（3秒）

1. 计算VRF输入：`H(prev_block_hash || round || epoch)`
2. 计算本节点VRF输出和证明
3. 广播VRF公告
4. 收集其他节点的VRF公告
5. 验证所有VRF的合法性
6. 计算分数并排序
7. 确定出块者和前15名

### Phase 2: 区块生产阶段（5秒）

1. 出块者构造区块头
2. 填充VRF信息和所有VRF公告
3. 选择交易
4. 计算Merkle根
5. 执行PoW挖矿
6. 广播区块

### Phase 3: 区块验证阶段（实时）

1. **VRF完整性验证**: 检查VRF公告数量、证明、分数
2. **出块者合法性验证**: 验证出块者确实是最高分
3. **奖励分配验证**: 验证前15名节点的正确性
4. **PoW验证**: 检查工作量证明的有效性
5. **交易验证**: 验证所有交易的签名和格式

## 配置说明

### 共识配置 (application.yml)

```yaml
equiflux:
  consensus:
    # 超级节点配置
    super-node-count: 50
    core-node-count: 20
    rotate-node-count: 30
    
    # 时间配置
    block-time-seconds: 3
    vrf-collection-timeout-ms: 3000
    block-production-timeout-ms: 5000
    
    # 奖励配置
    rewarded-top-x: 15
    
    # PoW配置
    pow-base-difficulty: 2500000
    pow-target-time-seconds: 3
    
    # 质押配置
    min-stake-core: 100000
    min-stake-rotate: 50000
    
    # 性能配置
    max-transactions-per-block: 1000
    max-block-size-mb: 2
```

## 测试

### 单元测试

```bash
mvn test
```

### 集成测试

```bash
mvn test -Dtest=*IntegrationTest
```

### 代码覆盖率

```bash
mvn jacoco:report
```

## 性能指标

- **目标TPS**: ≈1800 (基准)
- **理论TPS**: 2666 (最大)
- **实际TPS**: 1600-2100 (考虑网络开销)
- **出块时间**: 3秒
- **确认时间**: 8秒
- **最终性**: 2/3签名后

## 安全特性

- **VRF透明性**: 所有VRF公告公开可验证
- **防重放攻击**: 交易nonce机制
- **防双花**: 交易验证和状态管理
- **防恶意节点**: PoW增加作恶成本
- **防长程攻击**: 检查点机制

## 开发规范

- 遵循Java 17+规范
- 使用Spring Boot 3.x
- 单元测试覆盖率 > 90%
- 使用SLF4J日志框架
- 自定义业务异常
- 线程安全设计

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 联系方式

- 项目主页: [Equiflux官网]
- 技术文档: [技术白皮书]
- 问题反馈: [GitHub Issues]

---

**Equiflux Team** - 构建下一代高性能区块链
