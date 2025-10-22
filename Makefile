# Equiflux Node Makefile
# 编译、发布、运维指令

# 变量定义
PROJECT_NAME := equiflux-node
VERSION := 1.0.0
DOCKER_IMAGE := equiflux/node
DOCKER_TAG := $(VERSION)
DOCKER_REGISTRY := ghcr.io/equiflux
COMPOSE_FILE := docker-compose-prod.yml
ENV_FILE := .env.prod

# 颜色定义
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

# 默认目标
.DEFAULT_GOAL := help

# 帮助信息
.PHONY: help
help: ## 显示帮助信息
	@echo "$(BLUE)Equiflux Node 构建和部署工具$(NC)"
	@echo ""
	@echo "$(GREEN)可用命令:$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# ==================== 开发环境 ====================

.PHONY: clean
clean: ## 清理构建文件
	@echo "$(BLUE)清理构建文件...$(NC)"
	mvn clean
	rm -rf target/
	rm -rf logs/
	rm -rf data/
	@echo "$(GREEN)清理完成$(NC)"

.PHONY: compile
compile: ## 编译项目
	@echo "$(BLUE)编译项目...$(NC)"
	mvn compile
	@echo "$(GREEN)编译完成$(NC)"

.PHONY: test
test: ## 运行测试
	@echo "$(BLUE)运行测试...$(NC)"
	mvn test
	@echo "$(GREEN)测试完成$(NC)"

.PHONY: test-coverage
test-coverage: ## 运行测试并生成覆盖率报告
	@echo "$(BLUE)运行测试并生成覆盖率报告...$(NC)"
	mvn test jacoco:report
	@echo "$(GREEN)测试覆盖率报告生成完成$(NC)"
	@echo "$(YELLOW)报告位置: target/site/jacoco/index.html$(NC)"

.PHONY: package
package: ## 打包项目
	@echo "$(BLUE)打包项目...$(NC)"
	mvn package -DskipTests
	@echo "$(GREEN)打包完成$(NC)"

.PHONY: install
install: ## 安装到本地仓库
	@echo "$(BLUE)安装到本地仓库...$(NC)"
	mvn install -DskipTests
	@echo "$(GREEN)安装完成$(NC)"

.PHONY: run-dev
run-dev: ## 运行开发环境
	@echo "$(BLUE)启动开发环境...$(NC)"
	mvn spring-boot:run -Dspring-boot.run.profiles=dev
	@echo "$(GREEN)开发环境启动完成$(NC)"

# ==================== Docker 构建 ====================

.PHONY: docker-build
docker-build: ## 构建Docker镜像
	@echo "$(BLUE)构建Docker镜像...$(NC)"
	docker build -t $(DOCKER_IMAGE):$(DOCKER_TAG) .
	docker tag $(DOCKER_IMAGE):$(DOCKER_TAG) $(DOCKER_IMAGE):latest
	@echo "$(GREEN)Docker镜像构建完成$(NC)"
	@echo "$(YELLOW)镜像标签: $(DOCKER_IMAGE):$(DOCKER_TAG)$(NC)"

.PHONY: docker-build-no-cache
docker-build-no-cache: ## 无缓存构建Docker镜像
	@echo "$(BLUE)无缓存构建Docker镜像...$(NC)"
	docker build --no-cache -t $(DOCKER_IMAGE):$(DOCKER_TAG) .
	docker tag $(DOCKER_IMAGE):$(DOCKER_TAG) $(DOCKER_IMAGE):latest
	@echo "$(GREEN)Docker镜像构建完成$(NC)"

.PHONY: docker-push
docker-push: ## 推送Docker镜像到GitHub Container Registry
	@echo "$(BLUE)推送Docker镜像到GitHub Container Registry...$(NC)"
	@echo "$(YELLOW)推送目标: $(DOCKER_REGISTRY)/$(DOCKER_IMAGE)$(NC)"
	docker tag $(DOCKER_IMAGE):$(DOCKER_TAG) $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(DOCKER_TAG)
	docker tag $(DOCKER_IMAGE):latest $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):latest
	docker push $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(DOCKER_TAG)
	docker push $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):latest
	@echo "$(GREEN)Docker镜像推送完成$(NC)"
	@echo "$(YELLOW)镜像地址: $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(DOCKER_TAG)$(NC)"

.PHONY: docker-pull
docker-pull: ## 从GitHub Container Registry拉取Docker镜像
	@echo "$(BLUE)从GitHub Container Registry拉取Docker镜像...$(NC)"
	docker pull $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(DOCKER_TAG)
	docker pull $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):latest
	docker tag $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(DOCKER_TAG) $(DOCKER_IMAGE):$(DOCKER_TAG)
	docker tag $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):latest $(DOCKER_IMAGE):latest
	@echo "$(GREEN)Docker镜像拉取完成$(NC)"

.PHONY: docker-push-dockerhub
docker-push-dockerhub: ## 推送Docker镜像到Docker Hub
	@echo "$(BLUE)推送Docker镜像到Docker Hub...$(NC)"
	docker tag $(DOCKER_IMAGE):$(DOCKER_TAG) equiflux/node:$(DOCKER_TAG)
	docker tag $(DOCKER_IMAGE):latest equiflux/node:latest
	docker push equiflux/node:$(DOCKER_TAG)
	docker push equiflux/node:latest
	@echo "$(GREEN)Docker Hub推送完成$(NC)"
	@echo "$(YELLOW)镜像地址: equiflux/node:$(DOCKER_TAG)$(NC)"

.PHONY: docker-push-github
docker-push-github: ## 推送Docker镜像到GitHub Container Registry
	@echo "$(BLUE)推送Docker镜像到GitHub Container Registry...$(NC)"
	docker tag $(DOCKER_IMAGE):$(DOCKER_TAG) ghcr.io/equiflux/node:$(DOCKER_TAG)
	docker tag $(DOCKER_IMAGE):latest ghcr.io/equiflux/node:latest
	docker push ghcr.io/equiflux/node:$(DOCKER_TAG)
	docker push ghcr.io/equiflux/node:latest
	@echo "$(GREEN)GitHub Container Registry推送完成$(NC)"
	@echo "$(YELLOW)镜像地址: ghcr.io/equiflux/node:$(DOCKER_TAG)$(NC)"

# ==================== 环境配置 ====================

.PHONY: env-setup
env-setup: ## 设置环境配置文件
	@echo "$(BLUE)设置环境配置文件...$(NC)"
	@if [ ! -f .env.prod ]; then \
		cp env.prod.template .env.prod; \
		echo "$(GREEN)已创建 .env.prod 文件$(NC)"; \
		echo "$(YELLOW)请编辑 .env.prod 文件配置生产环境参数$(NC)"; \
	else \
		echo "$(YELLOW).env.prod 文件已存在$(NC)"; \
	fi

.PHONY: env-check
env-check: ## 检查环境配置
	@echo "$(BLUE)检查环境配置...$(NC)"
	@if [ -f .env.prod ]; then \
		echo "$(GREEN)✓ .env.prod 文件存在$(NC)"; \
	else \
		echo "$(RED)✗ .env.prod 文件不存在$(NC)"; \
		echo "$(YELLOW)请运行 'make env-setup' 创建配置文件$(NC)"; \
	fi
	@if [ -d data ]; then \
		echo "$(GREEN)✓ data 目录存在$(NC)"; \
	else \
		echo "$(YELLOW)⚠ data 目录不存在，将自动创建$(NC)"; \
	fi
	@if [ -d logs ]; then \
		echo "$(GREEN)✓ logs 目录存在$(NC)"; \
	else \
		echo "$(YELLOW)⚠ logs 目录不存在，将自动创建$(NC)"; \
	fi

# ==================== 部署 ====================

.PHONY: deploy-prepare
deploy-prepare: ## 准备部署环境
	@echo "$(BLUE)准备部署环境...$(NC)"
	mkdir -p data logs config
	chmod 755 data logs config
	@echo "$(GREEN)部署环境准备完成$(NC)"

.PHONY: deploy-up
deploy-up: env-check deploy-prepare ## 启动生产环境
	@echo "$(BLUE)启动生产环境...$(NC)"
	docker-compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up -d
	@echo "$(GREEN)生产环境启动完成$(NC)"
	@echo "$(YELLOW)服务状态:$(NC)"
	@docker-compose -f $(COMPOSE_FILE) ps

.PHONY: deploy-down
deploy-down: ## 停止生产环境
	@echo "$(BLUE)停止生产环境...$(NC)"
	docker-compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) down
	@echo "$(GREEN)生产环境已停止$(NC)"

.PHONY: deploy-restart
deploy-restart: ## 重启生产环境
	@echo "$(BLUE)重启生产环境...$(NC)"
	docker-compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) restart
	@echo "$(GREEN)生产环境重启完成$(NC)"

.PHONY: deploy-update
deploy-update: docker-build deploy-down deploy-up ## 更新并重启生产环境
	@echo "$(GREEN)生产环境更新完成$(NC)"

# ==================== 监控和日志 ====================

.PHONY: logs
logs: ## 查看应用日志
	@echo "$(BLUE)查看应用日志...$(NC)"
	docker-compose -f $(COMPOSE_FILE) logs -f equiflux-node

.PHONY: logs-all
logs-all: ## 查看所有服务日志
	@echo "$(BLUE)查看所有服务日志...$(NC)"
	docker-compose -f $(COMPOSE_FILE) logs -f

.PHONY: status
status: ## 查看服务状态
	@echo "$(BLUE)服务状态:$(NC)"
	docker-compose -f $(COMPOSE_FILE) ps
	@echo ""
	@echo "$(BLUE)资源使用情况:$(NC)"
	docker stats --no-stream

.PHONY: health
health: ## 检查服务健康状态
	@echo "$(BLUE)检查服务健康状态...$(NC)"
	@curl -s http://localhost:8081/actuator/health | jq . || echo "$(RED)健康检查失败$(NC)"

# ==================== 数据库和存储 ====================

.PHONY: backup-data
backup-data: ## 备份数据
	@echo "$(BLUE)备份数据...$(NC)"
	@timestamp=$$(date +%Y%m%d_%H%M%S); \
	mkdir -p backups; \
	tar -czf backups/data_backup_$$timestamp.tar.gz data/; \
	echo "$(GREEN)数据备份完成: backups/data_backup_$$timestamp.tar.gz$(NC)"

.PHONY: restore-data
restore-data: ## 恢复数据 (需要指定备份文件)
	@echo "$(BLUE)恢复数据...$(NC)"
	@if [ -z "$(BACKUP_FILE)" ]; then \
		echo "$(RED)请指定备份文件: make restore-data BACKUP_FILE=backups/data_backup_20240101_120000.tar.gz$(NC)"; \
		exit 1; \
	fi
	@if [ ! -f "$(BACKUP_FILE)" ]; then \
		echo "$(RED)备份文件不存在: $(BACKUP_FILE)$(NC)"; \
		exit 1; \
	fi
	docker-compose -f $(COMPOSE_FILE) down
	tar -xzf $(BACKUP_FILE)
	docker-compose -f $(COMPOSE_FILE) up -d
	@echo "$(GREEN)数据恢复完成$(NC)"

.PHONY: clean-data
clean-data: ## 清理数据 (危险操作)
	@echo "$(RED)警告: 这将删除所有数据!$(NC)"
	@read -p "确认删除所有数据? (y/N): " confirm && [ "$$confirm" = "y" ]
	docker-compose -f $(COMPOSE_FILE) down
	rm -rf data/*
	@echo "$(GREEN)数据清理完成$(NC)"

# ==================== 开发和调试 ====================

.PHONY: debug
debug: ## 启动调试模式
	@echo "$(BLUE)启动调试模式...$(NC)"
	docker-compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE) up -d
	docker exec -it equiflux-node bash
	@echo "$(GREEN)调试模式启动完成$(NC)"

.PHONY: shell
shell: ## 进入容器shell
	@echo "$(BLUE)进入容器shell...$(NC)"
	docker exec -it equiflux-node bash

.PHONY: port-forward
port-forward: ## 端口转发 (用于本地调试)
	@echo "$(BLUE)设置端口转发...$(NC)"
	@echo "$(YELLOW)RPC端口: localhost:8080 -> container:8080$(NC)"
	@echo "$(YELLOW)管理端口: localhost:8081 -> container:8081$(NC)"
	@echo "$(YELLOW)Grafana: localhost:3000 -> container:3000$(NC)"
	@echo "$(YELLOW)Prometheus: localhost:9090 -> container:9090$(NC)"

# ==================== 性能测试 ====================

.PHONY: benchmark
benchmark: ## 运行性能测试
	@echo "$(BLUE)运行性能测试...$(NC)"
	mvn test -Dtest=*PerformanceTest
	@echo "$(GREEN)性能测试完成$(NC)"

.PHONY: load-test
load-test: ## 运行负载测试
	@echo "$(BLUE)运行负载测试...$(NC)"
	@if command -v wrk >/dev/null 2>&1; then \
		wrk -t12 -c400 -d30s http://localhost:8080/api/health; \
	else \
		echo "$(YELLOW)请安装wrk工具进行负载测试$(NC)"; \
	fi

# ==================== 安全扫描 ====================

.PHONY: security-scan
security-scan: ## 运行安全扫描
	@echo "$(BLUE)运行安全扫描...$(NC)"
	mvn spotbugs:check
	@echo "$(GREEN)安全扫描完成$(NC)"

.PHONY: docker-scan
docker-scan: ## 扫描Docker镜像安全漏洞
	@echo "$(BLUE)扫描Docker镜像安全漏洞...$(NC)"
	@if command -v trivy >/dev/null 2>&1; then \
		trivy image $(DOCKER_IMAGE):$(DOCKER_TAG); \
	else \
		echo "$(YELLOW)请安装trivy工具进行Docker安全扫描$(NC)"; \
	fi

# ==================== 清理和维护 ====================

.PHONY: prune
prune: ## 清理Docker资源
	@echo "$(BLUE)清理Docker资源...$(NC)"
	docker system prune -f
	docker volume prune -f
	@echo "$(GREEN)Docker资源清理完成$(NC)"

.PHONY: clean-all
clean-all: clean prune ## 清理所有资源
	@echo "$(GREEN)所有资源清理完成$(NC)"

# ==================== 版本管理 ====================

.PHONY: version
version: ## 显示版本信息
	@echo "$(BLUE)版本信息:$(NC)"
	@echo "$(YELLOW)项目: $(PROJECT_NAME)$(NC)"
	@echo "$(YELLOW)版本: $(VERSION)$(NC)"
	@echo "$(YELLOW)Docker镜像: $(DOCKER_IMAGE):$(DOCKER_TAG)$(NC)"

.PHONY: bump-version
bump-version: ## 升级版本号
	@echo "$(BLUE)升级版本号...$(NC)"
	@read -p "输入新版本号: " new_version; \
	sed -i "s/VERSION := .*/VERSION := $$new_version/" Makefile; \
	sed -i "s/DOCKER_TAG := .*/DOCKER_TAG := $$new_version/" Makefile; \
	echo "$(GREEN)版本号已升级到: $$new_version$(NC)"

# ==================== 快速命令 ====================

.PHONY: quick-start
quick-start: env-setup deploy-up ## 快速启动 (首次部署)
	@echo "$(GREEN)快速启动完成$(NC)"
	@echo "$(YELLOW)访问地址:$(NC)"
	@echo "  RPC API: http://localhost:8080"
	@echo "  管理端点: http://localhost:8081/actuator/health"
	@echo "  Grafana: http://localhost:3000 (admin/Equiflux2024!@#)"
	@echo "  Prometheus: http://localhost:9090"

.PHONY: quick-stop
quick-stop: deploy-down ## 快速停止
	@echo "$(GREEN)快速停止完成$(NC)"

.PHONY: quick-restart
quick-restart: deploy-restart ## 快速重启
	@echo "$(GREEN)快速重启完成$(NC)"

# ==================== 开发工具 ====================

.PHONY: install-tools
install-tools: ## 安装开发工具
	@echo "$(BLUE)安装开发工具...$(NC)"
	@if command -v docker >/dev/null 2>&1; then \
		echo "$(GREEN)✓ Docker 已安装$(NC)"; \
	else \
		echo "$(RED)✗ Docker 未安装$(NC)"; \
	fi
	@if command -v docker-compose >/dev/null 2>&1; then \
		echo "$(GREEN)✓ Docker Compose 已安装$(NC)"; \
	else \
		echo "$(RED)✗ Docker Compose 未安装$(NC)"; \
	fi
	@if command -v jq >/dev/null 2>&1; then \
		echo "$(GREEN)✓ jq 已安装$(NC)"; \
	else \
		echo "$(YELLOW)⚠ jq 未安装 (用于JSON处理)$(NC)"; \
	fi
	@if command -v wrk >/dev/null 2>&1; then \
		echo "$(GREEN)✓ wrk 已安装$(NC)"; \
	else \
		echo "$(YELLOW)⚠ wrk 未安装 (用于负载测试)$(NC)"; \
	fi
	@if command -v trivy >/dev/null 2>&1; then \
		echo "$(GREEN)✓ trivy 已安装$(NC)"; \
	else \
		echo "$(YELLOW)⚠ trivy 未安装 (用于安全扫描)$(NC)"; \
	fi

# 确保目录存在
data logs config backups:
	mkdir -p $@
