# Equiflux Node

Equiflux公链节点实现 - 基于三层混合共识机制的高性能区块链

[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red.svg)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/equiflux/node)

## 📋 项目概述

Equiflux是一个创新的区块链公链，采用**PoS + VRF + 轻量级PoW**的三层混合共识机制，目标实现约**1800 TPS**的性能和**8秒确认时间**。

### 🚀 快速部署

```bash
# 一键部署 (推荐)
git clone https://github.com/equiflux/node.git
cd equiflux/node
./quick-start.sh

# 或使用Makefile
make quick-start
```

**服务地址**:
- RPC API: http://localhost:8080
- 管理端点: http://localhost:8081/actuator/health  
- Grafana: http://localhost:3000 (admin/Equiflux2024!@#)
- Prometheus: http://localhost:9090

### 🎯 核心创新

- **完全透明的VRF机制**：所有超级节点强制公开VRF，区块包含所有VRF证明，实时可验证
- **三层混合共识**：PoS治理层 + VRF选择层 + LPoW防护层
- **实时最终性**：确认即不可逆，无需历史挑战
- **高性能低能耗**：目标1800 TPS，相比PoW节能99.9%+

## 🚀 核心特性

- ✅ **高性能**: 目标1800 TPS，3秒出块，8秒确认
- ✅ **强去中心化**: 50个超级节点，动态轮换机制
- ✅ **完全透明**: 区块包含所有VRF公告，实时可验证
- ✅ **低能耗**: 轻量级PoW，仅用于增加作恶成本
- ✅ **高安全性**: 三层防护系统，抵抗各类攻击
- ✅ **公平启动**: 无预挖，无私募，人人平等

## 🛠️ 技术栈

- **Java 21 LTS**: 主要开发语言，使用内置密码学API
- **Spring Boot 3.3.0**: 应用框架和RPC服务
- **Maven 3.8+**: 构建工具
- **RocksDB**: 高性能区块链存储
- **Netty**: P2P网络通信
- **JUnit 5**: 测试框架
- **SLF4J + Logback**: 日志框架

## 📁 项目结构

```
equiflux-node/
├── src/main/java/io/equiflux/node/
│   ├── EquifluxApplication.java          # Spring Boot主类
│   ├── config/                           # 配置类
│   │   ├── ConsensusConfig.java          # 共识配置
│   │   └── CryptoConfig.java             # 密码学配置
│   ├── crypto/                           # 密码学模块
│   │   ├── Ed25519KeyPair.java          # Ed25519密钥对
│   │   ├── VRFKeyPair.java              # VRF密钥对
│   │   ├── VRFCalculator.java           # VRF计算器
│   │   ├── SignatureVerifier.java       # 签名验证器
│   │   └── HashUtils.java               # 哈希工具
│   ├── model/                            # 数据模型
│   │   ├── Block.java                   # 区块结构
│   │   ├── Transaction.java             # 交易结构
│   │   ├── VRFAnnouncement.java         # VRF公告
│   │   ├── VRFOutput.java               # VRF输出
│   │   └── VRFProof.java                # VRF证明
│   ├── consensus/                        # 共识引擎
│   │   ├── EquifluxConsensus.java       # 主共识引擎
│   │   ├── VRFCollector.java            # VRF收集器
│   │   ├── BlockProposer.java           # 区块提议器
│   │   ├── BlockValidator.java          # 区块验证器
│   │   ├── vrf/                         # VRF相关
│   │   │   ├── VRFRoundResult.java      # VRF轮次结果
│   │   │   └── ScoreCalculator.java     # 分数计算器
│   │   └── pow/                         # PoW相关
│   │       ├── PoWMiner.java            # PoW矿工
│   │       └── DifficultyCalculator.java # 难度计算器
│   ├── network/                         # 网络层
│   │   ├── NettyNetworkService.java     # Netty网络服务
│   │   ├── GossipProtocol.java          # Gossip协议
│   │   ├── PeerDiscoveryService.java    # 节点发现服务
│   │   └── MessagePropagationService.java # 消息传播服务
│   ├── storage/                         # 存储层
│   │   ├── RocksDBStorageService.java   # RocksDB存储服务
│   │   ├── BlockStorageService.java     # 区块存储服务
│   │   ├── StateStorageService.java     # 状态存储服务
│   │   └── TransactionStorageService.java # 交易存储服务
│   ├── rpc/                             # RPC接口层
│   │   ├── controller/                  # RPC控制器
│   │   ├── service/                     # RPC服务
│   │   ├── dto/                         # 数据传输对象
│   │   └── exception/                   # RPC异常处理
│   ├── exception/                       # 异常类
│   │   ├── ConsensusException.java
│   │   ├── CryptoException.java
│   │   ├── StorageException.java
│   │   └── ValidationException.java
│   └── demo/                            # 演示程序
│       └── EquifluxDemo.java           # 演示程序
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   └── logback.xml                      # 日志配置
└── src/test/java/                       # 测试代码
    └── io/equiflux/node/               # 各模块测试
```

## 🏗️ 架构设计

### 三层混合共识机制

```
┌─────────────────────────────────────────────────────────────┐
│                    Equiflux Consensus                     │
├─────────────────────────────────────────────────────────────┤
│  Layer 1: PoS治理层 (Governance Layer)                     │
│  • 50个超级节点选举和权益管理                               │
│  • 社区治理和参数调整                                       │
│  • 节点质押和惩罚机制                                       │
├─────────────────────────────────────────────────────────────┤
│  Layer 2: VRF选择层 (Selection Layer)                      │
│  • 完全透明的可验证随机函数                                 │
│  • 公平的出块者选择                                         │
│  • 实时VRF验证                                             │
├─────────────────────────────────────────────────────────────┤
│  Layer 3: LPoW防护层 (Protection Layer)                    │
│  • 轻量级工作量证明                                         │
│  • 动态难度调整                                             │
│  • 增加作恶成本                                             │
└─────────────────────────────────────────────────────────────┘
```

### 共识流程

#### Phase 1: VRF收集阶段（3秒）
1. 计算VRF输入：`H(prev_block_hash || round || epoch)`
2. 计算本节点VRF输出和证明
3. 广播VRF公告
4. 收集其他节点的VRF公告
5. 验证所有VRF的合法性
6. 计算分数并排序
7. 确定出块者和前15名

#### Phase 2: 区块生产阶段（5秒）
1. 出块者构造区块头
2. 填充VRF信息和所有VRF公告
3. 选择交易
4. 计算Merkle根
5. 执行PoW挖矿
6. 广播区块

#### Phase 3: 区块验证阶段（实时）
1. **VRF完整性验证**: 检查VRF公告数量、证明、分数
2. **出块者合法性验证**: 验证出块者确实是最高分
3. **奖励分配验证**: 验证前15名节点的正确性
4. **PoW验证**: 检查工作量证明的有效性
5. **交易验证**: 验证所有交易的签名和格式

## 🚀 快速开始

### 环境要求

#### 开发环境
- **Java 21 LTS** 或更高版本
- **Maven 3.8+**
- **Git**

#### 生产环境 (Docker部署)
- **Docker 20.10+**
- **Docker Compose 2.0+**
- **Make** (可选，用于便捷操作)

### 方式一：Docker部署 (推荐)

#### 一键部署
```bash
# 克隆项目
git clone https://github.com/equiflux/node.git
cd equiflux/node

# 一键启动
./quick-start.sh

# 或使用Makefile
make quick-start
```

#### 手动部署
```bash
# 1. 环境配置
cp env.prod.template .env.prod
vim .env.prod

# 2. 构建镜像
make docker-build

# 3. 启动服务
make deploy-up

# 4. 验证部署
make health
```

#### 服务访问地址
- **RPC API**: http://localhost:8080
- **管理端点**: http://localhost:8081/actuator/health
- **Grafana**: http://localhost:3000 (admin/Equiflux2024!@#)
- **Prometheus**: http://localhost:9090

### 方式二：本地开发

#### 1. 克隆项目
```bash
git clone https://github.com/equiflux/node.git
cd equiflux/node
```

#### 2. 编译项目
```bash
mvn clean compile
```

#### 3. 运行测试
```bash
mvn test
```

#### 4. 启动节点
```bash
mvn spring-boot:run
```

#### 5. 运行演示程序
```bash
mvn exec:java -Dexec.mainClass="io.equiflux.node.demo.EquifluxDemo"
```

## 🐳 Docker部署

### 部署架构

Equiflux Node提供完整的Docker部署方案，包括：

- **Equiflux Node**: 公链节点
- **Prometheus**: 指标收集和存储
- **Grafana**: 监控数据可视化
- **Loki + Promtail**: 日志聚合和查询
- **Node Exporter**: 系统指标收集

### 核心特性

- ✅ **生产就绪**: 完整的生产环境配置
- ✅ **监控完善**: Prometheus + Grafana + Loki
- ✅ **安全优化**: 非root用户、资源限制、网络安全
- ✅ **运维友好**: 丰富的Makefile命令、自动化脚本
- ✅ **可扩展**: 支持多节点部署、负载均衡

### 快速命令

```bash
# 一键启动
make quick-start

# 查看状态
make status

# 查看日志
make logs

# 停止服务
make deploy-down

# 重启服务
make deploy-restart

# 备份数据
make backup-data

# 健康检查
make health
```

### 详细文档

- [Docker部署指南](DOCKER_DEPLOYMENT.md) - 完整的部署和运维文档
- [Docker快速开始](DOCKER_README.md) - Docker部署概览

## 📊 项目完成度

### ✅ 已完成模块

| 模块 | 完成度 | 说明 |
|------|--------|------|
| **密码学模块** | 95% | Ed25519、VRF、哈希算法完整实现 |
| **数据模型** | 90% | Block、Transaction、VRF相关模型 |
| **共识引擎** | 85% | VRF收集、区块提议、验证逻辑 |
| **存储层** | 95% | RocksDB存储、状态管理完整实现 |
| **网络层** | 80% | Netty P2P网络、Gossip协议 |
| **RPC接口** | 90% | RESTful API、JSON-RPC支持 |
| **配置管理** | 100% | 完整的配置系统 |
| **异常处理** | 100% | 完整的异常体系 |
| **Docker部署** | 100% | 完整的Docker化部署方案 |
| **监控系统** | 100% | Prometheus + Grafana + Loki |
| **运维工具** | 100% | Makefile + 自动化脚本 |

### 🔄 开发中模块

| 模块 | 完成度 | 说明 |
|------|--------|------|
| **PoW模块** | 70% | 轻量级PoW实现，需要优化 |
| **网络优化** | 60% | 性能优化和稳定性提升 |
| **集成测试** | 50% | 端到端测试用例 |
| **钱包服务** | 80% | 钱包功能基本完成 |
| **区块浏览器** | 75% | Web界面基本完成 |

### 📈 测试覆盖率

- **总体覆盖率**: 46%
- **核心模块覆盖率**: 
  - 密码学模块: 66%
  - 数据模型: 62%
  - 存储层: 40%
  - RPC服务: 79%
  - 网络层: 35%
  - 共识引擎: 45%

## 🔧 运维和监控

### 监控指标

Equiflux Node提供完整的监控体系：

#### 系统指标
- CPU使用率
- 内存使用情况
- 磁盘I/O
- 网络流量

#### 应用指标
- JVM堆内存
- GC性能
- HTTP请求响应时间
- 线程池状态

#### 业务指标
- 区块高度
- TPS (每秒交易数)
- 共识状态
- 节点连接数
- VRF计算性能

### 日志管理

- **结构化日志**: JSON格式，便于解析
- **日志聚合**: Loki + Promtail
- **实时查询**: Grafana日志面板
- **日志轮转**: 自动清理旧日志

### 健康检查

```bash
# 检查服务健康状态
make health

# 查看详细状态
make status

# 查看实时日志
make logs
```

### 数据备份

```bash
# 自动备份
make backup-data

# 恢复数据
make restore-data BACKUP_FILE=backups/data_backup_20240101_120000.tar.gz

# 清理数据 (危险操作)
make clean-data
```

## ⚙️ 配置说明

### 共识配置

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

### 网络配置

```yaml
equiflux:
  network:
    port: 8080
    max-connections: 100
    connection-timeout-ms: 30000
    heartbeat-interval-ms: 30000
    enable-compression: true
    enable-encryption: true
```

## 🔧 API接口

### RPC接口

Equiflux提供完整的RPC接口，支持：

- **区块查询**: `getBlock`, `getBlocks`, `getRecentBlocks`
- **交易管理**: `broadcastTransaction`, `getTransaction`
- **账户信息**: `getAccountInfo`, `getAccountBalance`, `getAccountStake`
- **链状态**: `getChainState`, `getNetworkStats`

### 示例请求

```bash
# 获取最新区块
curl -X POST http://localhost:8080/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "getRecentBlocks",
    "params": {"count": 10},
    "id": 1
  }'

# 广播交易
curl -X POST http://localhost:8080/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "broadcastTransaction",
    "params": {
      "from": "sender_public_key",
      "to": "receiver_public_key",
      "amount": 1000,
      "fee": 10
    },
    "id": 2
  }'
```

## 📈 性能指标

| 指标 | 目标值 | 当前状态 |
|------|--------|----------|
| **TPS** | ≈1800 | 开发中 |
| **出块时间** | 3秒 | 已实现 |
| **确认时间** | 8秒 | 已实现 |
| **超级节点数** | 50个 | 已配置 |
| **能源消耗** | 90 MWh/年 | 设计目标 |
| **网络延迟** | <100ms | 测试中 |

## 🔒 安全特性

- **VRF透明性**: 所有VRF公告公开可验证
- **防重放攻击**: 交易nonce机制
- **防双花**: 交易验证和状态管理
- **防恶意节点**: PoW增加作恶成本
- **防长程攻击**: 检查点机制
- **拜占庭容错**: f < n/3

## 🧪 测试

### 单元测试

```bash
mvn test
```

### 集成测试

```bash
mvn test -Dtest=*IntegrationTest
```

### 代码覆盖率报告

```bash
mvn jacoco:report
```

覆盖率报告将生成在 `target/site/jacoco/index.html`

## 📚 文档

- [技术白皮书](doc/whitepaper.md) - 完整的技术方案
- [开发计划](doc/plan.md) - 详细的开发路线图
- [Docker部署指南](doc/DOCKER_DEPLOYMENT.md) - 完整的Docker部署和运维文档
- [Docker快速开始](doc/DOCKER_README.md) - Docker部署概览
- [存储层报告](doc/STORAGE_LAYER_REPORT.md) - 存储层实现详情
- [钱包服务报告](doc/WALLET_SERVICE_COMPLETION_REPORT.md) - 钱包服务实现详情
- [区块浏览器文档](doc/BLOCK_EXPLORER_README.md) - 区块浏览器使用指南

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 开发规范

- 遵循Java 21+规范
- 使用Spring Boot 3.x
- 单元测试覆盖率 > 90%
- 使用SLF4J日志框架
- 自定义业务异常
- 线程安全设计

## 📄 许可证

本项目采用MIT许可证 - 详见 [LICENSE](LICENSE) 文件

## 🔗 相关链接

- **项目主页**: [Equiflux官网](https://equiflux.io)
- **技术文档**: [技术白皮书](doc/whitepaper.md)
- **Docker部署**: [Docker部署指南](doc/DOCKER_DEPLOYMENT.md)
- **问题反馈**: [GitHub Issues](https://github.com/equiflux/node/issues)
- **Docker Hub**: [equiflux/node](https://hub.docker.com/r/equiflux/node)
- **GitHub Container Registry**: [ghcr.io/equiflux/node](https://github.com/equiflux/node/pkgs/container/node)

---

**Equiflux Team** - 构建下一代高性能区块链 🚀

*让区块链更快、更安全、更环保*