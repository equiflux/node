# Equiflux Node Docker 发布配置

## 📍 当前发布目标

### 🏷️ 镜像信息
- **项目名称**: equiflux-node
- **镜像名称**: equiflux/node
- **当前版本**: 1.0.0
- **标签策略**: 版本号 + latest
- **主要仓库**: GitHub Container Registry (ghcr.io)

### 🚀 发布目标

#### 1. 本地构建
```bash
# 构建到本地Docker
make docker-build
# 生成镜像: equiflux/node:1.0.0 和 equiflux/node:latest
```

#### 2. GitHub Container Registry (主要)
```bash
# 推送到GitHub Container Registry
make docker-push
# 目标地址: ghcr.io/equiflux/node:1.0.0
```

#### 3. Docker Hub (备用)
```bash
# 推送到Docker Hub
make docker-push-dockerhub
# 目标地址: equiflux/node:1.0.0
```

#### 4. 私有仓库 (企业内部)
```bash
# 推送到私有仓库
make docker-push-private
# 目标地址: registry.equiflux.io/equiflux/node:1.0.0
```

## 🔧 发布流程

### 开发环境发布
```bash
# 1. 构建镜像
make docker-build

# 2. 本地测试
make deploy-up
make health

# 3. 推送到GitHub Container Registry
make docker-push
```

### 生产环境发布
```bash
# 1. 从GitHub Container Registry拉取最新镜像
make docker-pull

# 2. 更新部署
make deploy-update
```

### CI/CD自动发布
```bash
# GitHub Actions自动构建和推送
# 触发条件: git tag push
# 目标: ghcr.io/equiflux/node:${GITHUB_REF_NAME}
```

## 📊 发布状态

| 目标 | 状态 | 地址 | 说明 |
|------|------|------|------|
| **本地构建** | ✅ 已配置 | `equiflux/node:1.0.0` | 开发测试 |
| **GitHub Registry** | ✅ 已配置 | `ghcr.io/equiflux/node` | **主要发布平台** |
| **Docker Hub** | ⚠️ 待配置 | `equiflux/node` | 备用分发 |
| **私有仓库** | ⚠️ 待配置 | `registry.equiflux.io/equiflux/node` | 企业内部 |

## 🛠️ 配置要求

### GitHub Container Registry 配置 (主要)
1. 在GitHub仓库中启用Packages
2. 创建Personal Access Token (packages:write权限)
3. 执行 `docker login ghcr.io`
4. 用户名: GitHub用户名
5. 密码: Personal Access Token

### Docker Hub 配置 (备用)
1. 注册Docker Hub账号
2. 创建 `equiflux/node` 仓库
3. 配置访问令牌
4. 执行 `docker login`

### 私有仓库配置 (企业内部)
1. 部署私有Docker Registry
2. 配置域名 `registry.equiflux.io`
3. 配置SSL证书
4. 执行 `docker login registry.equiflux.io`

## 🚀 推荐发布策略

### 开发阶段
- 使用本地构建进行开发测试
- 推送到GitHub Container Registry进行CI/CD

### 测试阶段
- 推送到GitHub Container Registry的测试标签
- 使用私有仓库进行内部测试

### 生产阶段
- 推送到GitHub Container Registry的稳定版本
- 同时推送到Docker Hub作为备用分发

## 📝 使用示例

### 用户拉取镜像
```bash
# 从GitHub Container Registry拉取 (推荐)
docker pull ghcr.io/equiflux/node:1.0.0

# 从Docker Hub拉取 (备用)
docker pull equiflux/node:1.0.0

# 从私有仓库拉取 (企业内部)
docker pull registry.equiflux.io/equiflux/node:1.0.0
```

### Docker Compose使用
```yaml
version: '3.8'
services:
  equiflux-node:
    image: ghcr.io/equiflux/node:1.0.0  # 推荐使用GitHub Container Registry
    ports:
      - "8080:8080"
      - "8081:8081"
```

## 🔄 版本管理

### 版本号规则
- **主版本**: 重大架构变更
- **次版本**: 新功能添加
- **修订版本**: 问题修复

### 标签策略
- `1.0.0`: 稳定版本
- `latest`: 最新稳定版本
- `dev`: 开发版本
- `beta`: 测试版本
