#!/bin/bash
# Equiflux Node 快速启动脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查系统依赖..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    # 检查Docker服务状态
    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker服务"
        exit 1
    fi
    
    log_success "系统依赖检查通过"
}

# 环境配置
setup_environment() {
    log_info "设置环境配置..."
    
    # 创建必要目录
    mkdir -p data logs config backups
    
    # 设置权限
    chmod 755 data logs config backups
    
    # 检查环境配置文件
    if [ ! -f .env.prod ]; then
        if [ -f env.prod.template ]; then
            cp env.prod.template .env.prod
            log_success "已创建 .env.prod 配置文件"
            log_warning "请编辑 .env.prod 文件配置生产环境参数"
        else
            log_error "环境配置模板文件不存在"
            exit 1
        fi
    else
        log_success "环境配置文件已存在"
    fi
}

# 构建镜像
build_image() {
    log_info "构建Docker镜像..."
    
    # 检查Dockerfile
    if [ ! -f Dockerfile ]; then
        log_error "Dockerfile不存在"
        exit 1
    fi
    
    # 构建镜像
    docker build -t equiflux/core:1.0.0 .
    docker tag equiflux/core:1.0.0 equiflux/core:latest
    
    log_success "Docker镜像构建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."
    
    # 检查docker-compose文件
    if [ ! -f docker-compose-prod.yml ]; then
        log_error "docker-compose-prod.yml文件不存在"
        exit 1
    fi
    
    # 启动服务
    docker-compose -f docker-compose-prod.yml --env-file .env.prod up -d
    
    log_success "服务启动完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."
    
    # 等待Equiflux Node
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8081/actuator/health &> /dev/null; then
            log_success "Equiflux Node服务已就绪"
            break
        fi
        
        log_info "等待Equiflux Node服务启动... ($attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
    
    if [ $attempt -gt $max_attempts ]; then
        log_error "Equiflux Node服务启动超时"
        exit 1
    fi
    
    # 等待Grafana服务
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:3000 &> /dev/null; then
            log_success "Grafana服务已就绪"
            break
        fi
        
        log_info "等待Grafana服务启动... ($attempt/$max_attempts)"
        sleep 5
        ((attempt++))
    done
    
    if [ $attempt -gt $max_attempts ]; then
        log_warning "Grafana服务启动超时，但核心服务已就绪"
    fi
}

# 显示服务信息
show_service_info() {
    log_success "部署完成！"
    echo ""
    echo "服务访问地址："
    echo "  RPC API:        http://localhost:8080"
    echo "  管理端点:        http://localhost:8081/actuator/health"
    echo "  Grafana:        http://localhost:3000 (admin/Equiflux2024!@#)"
    echo "  Prometheus:     http://localhost:9090"
    echo ""
    echo "常用命令："
    echo "  查看状态:       docker-compose -f docker-compose-prod.yml ps"
    echo "  查看日志:       docker-compose -f docker-compose-prod.yml logs -f equiflux-node"
    echo "  停止服务:       docker-compose -f docker-compose-prod.yml down"
    echo "  重启服务:       docker-compose -f docker-compose-prod.yml restart"
    echo ""
    echo "或使用Makefile："
    echo "  查看状态:       make status"
    echo "  查看日志:       make logs"
    echo "  停止服务:       make deploy-down"
    echo "  重启服务:       make deploy-restart"
}

# 主函数
main() {
    echo "=========================================="
    echo "    Equiflux Node 快速启动脚"
    echo "=========================================="
    echo ""
    
    # 检查参数
    if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
        echo "用法: $0 [选项]"
        echo ""
        echo "选项:"
        echo "  --help, -h     显示帮助信息"
        echo "  --skip-build   跳过镜像构建"
        echo "  --skip-check   跳过依赖检查"
        echo ""
        exit 0
    fi
    
    # 解析参数
    SKIP_BUILD=false
    SKIP_CHECK=false
    
    for arg in "$@"; do
        case $arg in
            --skip-build)
                SKIP_BUILD=true
                ;;
            --skip-check)
                SKIP_CHECK=true
                ;;
        esac
    done
    
    # 执行步骤
    if [ "$SKIP_CHECK" = false ]; then
        check_dependencies
    fi
    
    setup_environment
    
    if [ "$SKIP_BUILD" = false ]; then
        build_image
    fi
    
    start_services
    wait_for_services
    show_service_info
}

# 错误处理
trap 'log_error "脚本执行失败，请检查错误信息"; exit 1' ERR

# 执行主函数
main "$@"
