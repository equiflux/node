# GitHub Container Registry 配置指南

## 📋 概述

Equiflux Node现在使用GitHub Container Registry (ghcr.io)作为主要的Docker镜像发布平台。这为隐私公链项目提供了安全、可靠的容器镜像分发服务。

## 🔧 配置步骤

### 1. 启用GitHub Packages

1. 进入GitHub仓库设置
2. 在左侧菜单中找到"Packages"
3. 确保Packages功能已启用

### 2. 创建Personal Access Token

1. 进入GitHub Settings → Developer settings → Personal access tokens
2. 点击"Generate new token (classic)"
3. 选择以下权限：
   - `write:packages` - 推送包
   - `read:packages` - 拉取包
   - `delete:packages` - 删除包
4. 复制生成的token

### 3. 配置Docker登录

```bash
# 登录到GitHub Container Registry
docker login ghcr.io

# 用户名: 你的GitHub用户名
# 密码: Personal Access Token
```

### 4. 验证配置

```bash
# 构建镜像
make docker-build

# 推送到GitHub Container Registry
make docker-push

# 验证推送成功
docker pull ghcr.io/equiflux/core:1.0.0
```

## 🚀 自动化发布

### GitHub Actions工作流

项目已配置GitHub Actions工作流，支持：

- **自动构建**: 推送代码时自动构建镜像
- **多架构支持**: linux/amd64, linux/arm64
- **安全扫描**: Trivy漏洞扫描
- **版本标签**: 自动生成版本标签

### 触发条件

- **标签推送**: `git tag v1.0.0 && git push origin v1.0.0`
- **Pull Request**: 创建PR时构建测试镜像
- **手动触发**: 在GitHub Actions页面手动触发

## 📊 镜像管理

### 镜像地址

- **主要地址**: `ghcr.io/equiflux/node:1.0.0`
- **最新版本**: `ghcr.io/equiflux/node:latest`
- **开发版本**: `ghcr.io/equiflux/node:dev`

### 版本策略

- `v1.0.0` - 稳定版本
- `latest` - 最新稳定版本
- `dev` - 开发版本
- `beta` - 测试版本

## 🔒 安全特性

### 访问控制

- **公开镜像**: 任何人都可以拉取
- **私有镜像**: 需要GitHub账号权限
- **组织镜像**: 组织成员可以访问

### 安全扫描

- **漏洞扫描**: 自动扫描已知漏洞
- **依赖检查**: 检查依赖包安全性
- **签名验证**: 镜像签名验证

## 📝 使用示例

### 拉取镜像

```bash
# 拉取最新稳定版本
docker pull ghcr.io/equiflux/node:latest

# 拉取特定版本
docker pull ghcr.io/equiflux/node:1.0.0

# 拉取开发版本
docker pull ghcr.io/equiflux/node:dev
```

### Docker Compose使用

```yaml
version: '3.8'
services:
  equiflux-node:
    image: ghcr.io/equiflux/core:1.0.0
    ports:
      - "8080:8080"
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
```

### Kubernetes使用

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: equiflux-node
spec:
  replicas: 1
  selector:
    matchLabels:
      app: equiflux-node
  template:
    metadata:
      labels:
        app: equiflux-node
    spec:
      containers:
      - name: equiflux-node
        image: ghcr.io/equiflux/node:1.0.0
        ports:
        - containerPort: 8080
        - containerPort: 8081
```

## 🔄 发布流程

### 开发环境

```bash
# 1. 本地构建测试
make docker-build
make deploy-up
make health

# 2. 推送到GitHub Registry
make docker-push
```

### 生产环境

```bash
# 1. 创建版本标签
git tag v1.0.0
git push origin v1.0.0

# 2. GitHub Actions自动构建和推送
# 3. 验证镜像
docker pull ghcr.io/equiflux/node:1.0.0

# 4. 部署到生产环境
make docker-pull
make deploy-update
```

## 📈 监控和管理

### GitHub Packages页面

访问 `https://github.com/equiflux/node/pkgs/container/node` 查看：

- 镜像版本列表
- 下载统计
- 安全扫描结果
- 镜像大小

### 清理策略

- **保留策略**: 保留最近10个版本
- **自动清理**: 超过30天的测试镜像
- **手动清理**: 通过GitHub界面删除

## 🛠️ 故障排查

### 常见问题

#### 1. 推送失败
```bash
# 检查登录状态
docker system info | grep -A 5 "Registry"

# 重新登录
docker logout ghcr.io
docker login ghcr.io
```

#### 2. 权限不足
- 检查Personal Access Token权限
- 确认GitHub仓库权限
- 验证组织成员身份

#### 3. 拉取失败
```bash
# 检查镜像是否存在
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
  https://ghcr.io/v2/equiflux/node/tags/list
```

## 📞 支持

如有问题，请联系：
- **GitHub Issues**: [项目Issues](https://github.com/equiflux/node/issues)

## 🔗 相关链接

- [GitHub Container Registry文档](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Docker登录GitHub Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry)
- [GitHub Actions文档](https://docs.github.com/en/actions)
