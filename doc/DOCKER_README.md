# Equiflux Node Docker 部署方案

## 📋 概述

本项目提供了完整的Docker部署方案，包括Equiflux Node公链节点及其配套的监控、日志和运维工具。

## 🚀 快速开始

### 一键部署
```bash
# 使用快速启动脚本
./quick-start.sh

# 或使用Makefile
make quick-start
```

### 手动部署
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

## 📁 文件结构

```
├── Dockerfile                    # Docker镜像构建文件
├── docker-compose-prod.yml       # 生产环境编排配置
├── .dockerignore                 # Docker构建忽略文件
├── Makefile                      # 构建和运维指令
├── env.prod.template             # 环境配置模板
├── quick-start.sh                # 快速启动脚本
├── DOCKER_DEPLOYMENT.md          # 详细部署文档
└── monitoring/                   # 监控配置
    ├── prometheus.yml            # Prometheus配置
    ├── loki.yml                 # Loki日志配置
    ├── promtail.yml             # Promtail配置
    └── grafana/                 # Grafana配置
        ├── dashboards/          # 仪表板配置
        └── datasources/         # 数据源配置
```

## 🛠️ 核心组件

### Equiflux Node
- **端口**: 8080 (RPC), 8081 (管理)
- **功能**: 公链核心节点，提供RPC API和共识服务
- **资源**: 4-8GB内存，2-4CPU核心

### Prometheus
- **端口**: 9090
- **功能**: 指标收集和存储
- **监控**: JVM指标、HTTP请求、自定义业务指标

### Grafana
- **端口**: 3000
- **功能**: 监控数据可视化
- **账号**: admin / Equiflux2024!@#

### Loki + Promtail
- **端口**: 3100 (Loki)
- **功能**: 日志聚合和查询
- **收集**: 应用日志、系统日志

### Node Exporter
- **端口**: 9100
- **功能**: 系统指标收集
- **监控**: CPU、内存、磁盘、网络

## 📊 监控面板

### 系统监控
- CPU使用率
- 内存使用情况
- 磁盘I/O
- 网络流量

### 应用监控
- JVM堆内存
- GC性能
- HTTP请求响应时间
- 线程池状态

### 业务监控
- 区块高度
- TPS (每秒交易数)
- 共识状态
- 节点连接数

## 🔧 运维命令

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

### 数据管理
```bash
# 备份数据
make backup-data

# 恢复数据
make restore-data BACKUP_FILE=backups/data_backup_20240101_120000.tar.gz

# 清理数据
make clean-data
```

### 监控调试
```bash
# 健康检查
make health

# 进入容器
make shell

# 性能测试
make benchmark
```

## ⚙️ 配置说明

### 环境变量
主要配置项：
- `EQUIFLUX_VERSION`: 应用版本
- `RPC_PORT`: RPC服务端口
- `JAVA_OPTS`: JVM参数
- `EQUIFLUX_CONSENSUS_*`: 共识相关配置
- `EQUIFLUX_NETWORK_*`: 网络相关配置

### 资源限制
- **内存**: 8GB (推荐16GB)
- **CPU**: 4核心 (推荐8核心)
- **存储**: 100GB SSD
- **网络**: 100Mbps

## 🔒 安全特性

### 容器安全
- 非root用户运行
- 只读文件系统
- 安全上下文配置
- 资源限制

### 网络安全
- 内部网络隔离
- 端口访问控制
- TLS加密支持
- 防火墙配置

### 数据安全
- 数据持久化
- 定期备份
- 访问控制
- 审计日志

## 📈 性能优化

### JVM调优
```bash
JAVA_OPTS="-Xms4g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### 网络优化
- TCP_NODELAY
- 零拷贝
- 连接池
- 压缩传输

### 存储优化
- SSD存储
- RocksDB调优
- 数据压缩
- 定期清理

## 🚨 故障排查

### 常见问题
1. **服务启动失败**: 检查日志和配置
2. **内存不足**: 调整JVM参数
3. **网络连接问题**: 检查端口和防火墙
4. **性能问题**: 监控资源使用情况

### 日志分析
```bash
# 查看应用日志
make logs

# 查看错误日志
docker-compose logs equiflux-node | grep ERROR

# 查看特定时间日志
docker-compose logs --since="2024-01-01T00:00:00" equiflux-node
```

## 🔄 升级维护

### 版本升级
```bash
# 1. 备份数据
make backup-data

# 2. 拉取新版本
git pull origin main

# 3. 构建新镜像
make docker-build

# 4. 更新部署
make deploy-update
```

### 定期维护
- 清理旧日志
- 更新系统补丁
- 性能优化
- 安全审计

## 📞 技术支持

- **文档**: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)
- **问题反馈**: GitHub Issues
- **技术支持**: master@equiflux.io

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。
