#!/bin/bash

# Nazo应用Ubuntu部署启动脚本
# 此脚本用于在Ubuntu系统上启动前后端应用

set -e  # 遇到错误时退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# 检查系统要求
check_requirements() {
    log_info "检查系统要求..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        log_error "Java未安装。请先安装Java 21。"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 21 ]; then
        log_error "需要Java 21或更高版本，当前版本: $java_version"
        exit 1
    fi
    log_success "Java版本检查通过: $(java -version 2>&1 | head -n 1)"
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装。请先安装Maven。"
        exit 1
    fi
    log_success "Maven检查通过: $(mvn -version | head -n 1)"
    
    # 检查Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js未安装。请先安装Node.js 18+。"
        exit 1
    fi
    
    node_version=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$node_version" -lt 18 ]; then
        log_error "需要Node.js 18或更高版本，当前版本: $(node -v)"
        exit 1
    fi
    log_success "Node.js版本检查通过: $(node -v)"
    
    # 检查npm
    if ! command -v npm &> /dev/null; then
        log_error "npm未安装。"
        exit 1
    fi
    log_success "npm检查通过: $(npm -v)"
}

# 构建后端
build_backend() {
    log_info "构建后端应用..."
    
    # 清理之前的构建
    if [ -f "target/nazo-tetris-1.0.0.jar" ]; then
        log_info "清理之前的后端构建文件..."
        rm -f target/nazo-tetris-1.0.0.jar
    fi
    
    # Maven构建
    log_info "执行Maven构建..."
    mvn clean package -DskipTests=true
    
    if [ ! -f "target/nazo-tetris-1.0.0.jar" ]; then
        log_error "后端构建失败！"
        exit 1
    fi
    
    log_success "后端构建完成！"
}

# 构建前端
build_frontend() {
    log_info "构建前端应用..."
    
    cd nazo-frontend
    
    # 清理之前的构建
    if [ -d "dist" ]; then
        log_info "清理之前的前端构建文件..."
        rm -rf dist
    fi
    
    # 安装依赖
    log_info "安装前端依赖..."
    npm install
    
    # 构建前端
    log_info "执行前端构建..."
    npm run build
    
    if [ ! -d "dist" ]; then
        log_error "前端构建失败！"
        exit 1
    fi
    
    log_success "前端构建完成！"
    cd ..
}

# 启动后端服务
start_backend() {
    log_info "启动后端服务..."
    
    # 检查8080端口是否被占用
    if netstat -tlnp 2>/dev/null | grep -q ":8080 "; then
        log_warning "端口8080已被占用，尝试终止现有进程..."
        pkill -f "nazo-tetris-1.0.0.jar" || true
        sleep 2
    fi
    
    # 启动后端
    log_info "在后台启动Spring Boot应用..."
    nohup java -jar target/nazo-tetris-1.0.0.jar > backend.log 2>&1 &
    BACKEND_PID=$!
    
    # 等待服务启动
    log_info "等待后端服务启动..."
    for i in {1..30}; do
        if curl -s http://localhost:8080 > /dev/null 2>&1; then
            log_success "后端服务已启动！PID: $BACKEND_PID"
            echo $BACKEND_PID > backend.pid
            return 0
        fi
        sleep 1
    done
    
    log_error "后端服务启动超时！"
    return 1
}

# 启动前端服务
start_frontend() {
    log_info "启动前端服务..."
    
    cd nazo-frontend
    
    # 检查4173端口是否被占用
    if netstat -tlnp 2>/dev/null | grep -q ":4173 "; then
        log_warning "端口4173已被占用，尝试终止现有进程..."
        pkill -f "vite preview" || true
        sleep 2
    fi
    
    # 启动前端预览服务
    log_info "在后台启动前端预览服务..."
    nohup npm run preview > ../frontend.log 2>&1 &
    FRONTEND_PID=$!
    
    # 等待服务启动
    log_info "等待前端服务启动..."
    for i in {1..20}; do
        if curl -s http://localhost:4173 > /dev/null 2>&1; then
            log_success "前端服务已启动！PID: $FRONTEND_PID"
            echo $FRONTEND_PID > ../frontend.pid
            cd ..
            return 0
        fi
        sleep 1
    done
    
    log_error "前端服务启动超时！"
    cd ..
    return 1
}

# 停止服务
stop_services() {
    log_info "停止所有服务..."
    
    # 停止后端
    if [ -f "backend.pid" ]; then
        BACKEND_PID=$(cat backend.pid)
        if kill -0 $BACKEND_PID 2>/dev/null; then
            log_info "停止后端服务 (PID: $BACKEND_PID)..."
            kill $BACKEND_PID
            rm backend.pid
        fi
    fi
    
    # 停止前端
    if [ -f "frontend.pid" ]; then
        FRONTEND_PID=$(cat frontend.pid)
        if kill -0 $FRONTEND_PID 2>/dev/null; then
            log_info "停止前端服务 (PID: $FRONTEND_PID)..."
            kill $FRONTEND_PID
            rm frontend.pid
        fi
    fi
    
    # 强制终止相关进程
    pkill -f "nazo-tetris-1.0.0.jar" || true
    pkill -f "vite preview" || true
    
    log_success "所有服务已停止！"
}

# 显示服务状态
show_status() {
    log_info "检查服务状态..."
    
    # 检查后端
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
        log_success "后端服务运行正常 (http://localhost:8080)"
    else
        log_error "后端服务未运行"
    fi
    
    # 检查前端
    if curl -s http://localhost:4173 > /dev/null 2>&1; then
        log_success "前端服务运行正常 (http://localhost:4173)"
    else
        log_error "前端服务未运行"
    fi
    
    # 显示进程信息
    echo
    log_info "相关进程信息："
    ps aux | grep -E "(nazo-tetris|vite preview)" | grep -v grep || log_warning "未找到相关进程"
}

# 显示日志
show_logs() {
    echo "=== 后端日志 (最后20行) ==="
    if [ -f "backend.log" ]; then
        tail -20 backend.log
    else
        log_warning "后端日志文件不存在"
    fi
    
    echo
    echo "=== 前端日志 (最后20行) ==="
    if [ -f "frontend.log" ]; then
        tail -20 frontend.log
    else
        log_warning "前端日志文件不存在"
    fi
}

# 显示帮助信息
show_help() {
    echo "Nazo应用Ubuntu部署脚本"
    echo
    echo "用法: $0 [命令]"
    echo
    echo "命令："
    echo "  start       - 构建并启动所有服务"
    echo "  stop        - 停止所有服务"
    echo "  restart     - 重启所有服务"
    echo "  status      - 显示服务状态"
    echo "  logs        - 显示应用日志"
    echo "  build       - 仅构建应用"
    echo "  help        - 显示此帮助信息"
    echo
    echo "示例："
    echo "  $0 start    # 启动所有服务"
    echo "  $0 stop     # 停止所有服务"
    echo "  $0 status   # 查看状态"
}

# 主函数
main() {
    case "${1:-start}" in
        "start")
            check_requirements
            build_backend
            build_frontend
            start_backend
            start_frontend
            echo
            log_success "应用启动完成！"
            log_info "前端地址: http://localhost:4173"
            log_info "后端API: http://localhost:8080"
            log_info "使用 '$0 status' 检查状态"
            log_info "使用 '$0 logs' 查看日志"
            log_info "使用 '$0 stop' 停止服务"
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            stop_services
            sleep 2
            $0 start
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs
            ;;
        "build")
            check_requirements
            build_backend
            build_frontend
            log_success "构建完成！使用 '$0 start' 启动服务"
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@" 