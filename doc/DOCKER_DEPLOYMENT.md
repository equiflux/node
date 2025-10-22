# Equiflux Core Docker 部署指南

## 概述

本文档介绍如何使用Docker和Docker Compose部署Equiflux Core公链节点。部署方案包括：

- Equiflux Core节点
- Prometheus监控
- Grafana可视化
- Loki日志聚合
- Node Exporter系统监控

## 系统要求

### 硬件要求
- **CPU**: 4核心以上 (推荐8核心)
- **内存**: 8GB以上 (推荐16GB)
- **存储**: 100GB以上SSD
- **网络**: 100Mbps以上带宽

### 软件要求
- Docker 20.10+
- Docker Compose 2.0+
- Make (可选，用于便捷操作)

## 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/equiflux/node.git
cd node
```

### 2. 环境配置
```bash
# 复制环境配置模板
cp env.prod.template .env.prod

# 编辑配置文件
vim .env.prod
```

### 3. 一键部署
```bash
# 使用Makefile快速启动
make quick-start

# 或者手动部署
make env-setup
make deploy-up
```

### 4. 验证部署
```bash
# 检查服务状态
make status

# 检查健康状态
make health

# 查看日志
make logs
```

## 详细配置

### 环境变量配置

主要配置项说明：

#### 应用配置
- `EQUIFLUX_VERSION`: 应用版本
- `RPC_PORT`: RPC服务端口 (默认8080)
- `MANAGEMENT_PORT`: 管理端口 (默认8081)

#### 共识配置
- `EQUIFLUX_CONSENSUS_SUPER_NODE_COUNT`: 超级节点数量 (默认50)
- `EQUIFLUX_CONSENSUS_BLOCK_TIME_SECONDS`: 出块时间 (默认3秒)
- `EQUIFLUX_CONSENSUS_REWARDED_TOP_X`: 奖励节点数量 (默认15)

#### 网络配置
- `EQUIFLUX_NETWORK_MAX_CONNECTIONS`: 最大连接数 (默认200)
- `EQUIFLUX_NETWORK_WORKER_THREADS`: 工作线程数 (默认16)

#### JVM配置
- `JAVA_OPTS`: JVM参数，包含内存、GC等设置

### 数据持久化

数据存储在以下目录：
- `data/`: 区块链数据
- `logs/`: 应用日志
- `config/`: 配置文件

### 网络端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Equiflux Core | 8080 | RPC API |
| Management | 8081 | 健康检查和指标 |
| Prometheus | 9090 | 监控数据 |
| Grafana | 3000 | 可视化界面 |
| Node Exporter | 9100 | 系统指标 |
| Loki | 3100 | 日志聚合 |

## 运维操作

### 基本操作

```bash
# 启动服务
make deploy-up

# 停止服务
make deploy-down

# 重启服务
make deploy-restart

# 查看状态
make status

# 查看日志
make logs
```

### 监控和调试

```bash
# 健康检查
make health

# 进入容器调试
make shell

# 查看资源使用
make status
```

### 数据管理

```bash
# 备份数据
make backup-data

# 恢复数据
make restore-data BACKUP_FILE=backups/data_backup_20240101_120000.tar.gz

# 清理数据 (危险操作)
make clean-data
```

### 更新部署

```bash
# 更新并重启
make deploy-update

# 或者手动更新
make docker-build
make deploy-down
make deploy-up
```

## 监控配置

### Prometheus监控

Prometheus自动收集以下指标：
- JVM指标 (内存、GC、线程等)
- Spring Boot指标 (HTTP请求、响应时间等)
- 自定义业务指标 (区块高度、TPS等)

访问地址: http://localhost:9090

### Grafana可视化

预配置的仪表板包括：
- JVM性能监控
- HTTP请求监控
- 系统资源监控
- 自定义业务指标

访问地址: http://localhost:3000
默认账号: admin / Equiflux2024!@#

### 日志聚合

Loki收集应用日志，支持：
- 结构化日志查询
- 实时日志流
- 日志告警

## 性能优化

### JVM调优

生产环境JVM参数：
```bash
JAVA_OPTS="-Xms4g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"
```

### 网络优化

- 启用TCP_NODELAY
- 使用零拷贝
- 配置连接池
- 启用压缩

### 存储优化

- 使用SSD存储
- 配置RocksDB参数
- 定期清理旧数据

## 安全配置

### 网络安全

- 配置防火墙规则
- 使用TLS加密
- 限制访问IP

### 容器安全

- 使用非root用户
- 只读文件系统
- 安全上下文

### 密钥管理

- 使用环境变量
- 密钥轮换
- 访问控制

## 故障排查

### 常见问题

#### 1. 服务启动失败
```bash
# 检查日志
make logs

# 检查配置
make env-check

# 检查端口占用
netstat -tlnp | grep :8080
```

#### 2. 内存不足
```bash
# 检查内存使用
make status

# 调整JVM参数
vim .env.prod
# 修改 JAVA_OPTS
```

#### 3. 网络连接问题
```bash
# 检查网络配置
docker network ls
docker network inspect equiflux-network

# 检查端口映射
docker port equiflux-node
```

### 日志分析

```bash
# 查看应用日志
make logs

# 查看特定时间日志
docker-compose logs --since="2024-01-01T00:00:00" equiflux-node

# 查看错误日志
docker-compose logs equiflux-node | grep ERROR
```

### 性能分析

```bash
# 运行性能测试
make benchmark

# 负载测试
make load-test

# 查看资源使用
docker stats
```

## 备份和恢复

### 自动备份

设置定时备份：
```bash
# 添加到crontab
0 2 * * * cd /path/to/equiflux && make backup-data
```

### 手动备份

```bash
# 备份数据
make backup-data

# 备份配置
tar -czf config_backup.tar.gz .env.prod monitoring/

# 备份日志
tar -czf logs_backup.tar.gz logs/
```

### 恢复流程

```bash
# 停止服务
make deploy-down

# 恢复数据
make restore-data BACKUP_FILE=backups/data_backup_20240101_120000.tar.gz

# 恢复配置
tar -xzf config_backup.tar.gz

# 启动服务
make deploy-up
```

## 升级指南

### 版本升级

```bash
# 1. 备份当前版本
make backup-data

# 2. 拉取新版本
git pull origin main

# 3. 构建新镜像
make docker-build

# 4. 更新部署
make deploy-update

# 5. 验证升级
make health
```

### 配置升级

```bash
# 1. 备份配置
cp .env.prod .env.prod.backup

# 2. 更新配置
vim .env.prod

# 3. 重启服务
make deploy-restart
```

## 扩展部署

### 多节点部署

```bash
# 节点1
EQUIFLUX_NODE_ID=1 make deploy-up

# 节点2
EQUIFLUX_NODE_ID=2 make deploy-up

# 节点3
EQUIFLUX_NODE_ID=3 make deploy-up
```

### 负载均衡

使用Nginx或HAProxy进行负载均衡：

```nginx
upstream equiflux {
    server 192.168.1.10:8080;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080;
}

server {
    listen 80;
    location / {
        proxy_pass http://equiflux;
    }
}
```

## 维护计划

### 日常维护

- 检查服务状态
- 监控资源使用
- 查看错误日志
- 备份重要数据

### 定期维护

- 清理旧日志
- 更新系统补丁
- 性能优化
- 安全审计

### 应急响应

- 服务故障处理
- 数据恢复
- 安全事件响应
- 性能问题排查

## 联系支持

如有问题，请联系：
- 技术支持: support@equiflux.io
- 文档反馈: docs@equiflux.io
- 紧急联系: emergency@equiflux.io
