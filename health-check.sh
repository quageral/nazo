#!/bin/bash

# Nazo应用健康检查脚本
# 用于监控前后端服务状态并自动重启失败的服务

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 日志函数
log_ok() {
    echo -e "${GREEN}[OK]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 检查后端健康状态
check_backend() {
    if curl -f -s http://localhost:8080 > /dev/null 2>&1; then
        log_ok "后端服务运行正常 (http://localhost:8080)"
        return 0
    else
        log_error "后端服务异常或未响应"
        return 1
    fi
}

# 检查前端健康状态
check_frontend() {
    if curl -f -s http://localhost:4173 > /dev/null 2>&1; then
        log_ok "前端服务运行正常 (http://localhost:4173)"
        return 0
    else
        log_error "前端服务异常或未响应"
        return 1
    fi
}

# 重启后端服务
restart_backend() {
    log_warning "尝试重启后端服务..."
    
    # 停止现有进程
    pkill -f "nazo-tetris-1.0.0.jar" || true
    sleep 3
    
    # 启动新进程
    if [ -f "target/nazo-tetris-1.0.0.jar" ]; then
        nohup java -jar target/nazo-tetris-1.0.0.jar > backend.log 2>&1 &
        echo $! > backend.pid
        
        # 等待启动
        for i in {1..30}; do
            if curl -f -s http://localhost:8080 > /dev/null 2>&1; then
                log_ok "后端服务重启成功"
                return 0
            fi
            sleep 1
        done
        log_error "后端服务重启失败"
        return 1
    else
        log_error "后端jar文件不存在，无法重启"
        return 1
    fi
}

# 重启前端服务
restart_frontend() {
    log_warning "尝试重启前端服务..."
    
    # 停止现有进程
    pkill -f "vite preview" || true
    sleep 3
    
    # 启动新进程
    if [ -d "nazo-frontend/dist" ]; then
        cd nazo-frontend
        nohup npm run preview > ../frontend.log 2>&1 &
        echo $! > ../frontend.pid
        cd ..
        
        # 等待启动
        for i in {1..20}; do
            if curl -f -s http://localhost:4173 > /dev/null 2>&1; then
                log_ok "前端服务重启成功"
                return 0
            fi
            sleep 1
        done
        log_error "前端服务重启失败"
        return 1
    else
        log_error "前端构建文件不存在，无法重启"
        return 1
    fi
}

# 主函数
main() {
    echo "$(date): 开始健康检查..."
    
    backend_ok=true
    frontend_ok=true
    
    # 检查后端
    if ! check_backend; then
        backend_ok=false
        if [ "${1:-}" != "--no-restart" ]; then
            restart_backend || true
        fi
    fi
    
    # 检查前端
    if ! check_frontend; then
        frontend_ok=false
        if [ "${1:-}" != "--no-restart" ]; then
            restart_frontend || true
        fi
    fi
    
    # 总结
    if $backend_ok && $frontend_ok; then
        echo "$(date): 所有服务运行正常"
        exit 0
    else
        echo "$(date): 部分服务存在问题"
        exit 1
    fi
}

# 显示帮助
show_help() {
    echo "Nazo应用健康检查脚本"
    echo
    echo "用法: $0 [选项]"
    echo
    echo "选项："
    echo "  --no-restart    只检查状态，不自动重启服务"
    echo "  --help         显示帮助信息"
    echo
    echo "示例："
    echo "  $0              # 检查并自动重启异常服务"
    echo "  $0 --no-restart # 仅检查状态"
}

# 处理命令行参数
case "${1:-}" in
    "--help"|"-h")
        show_help
        exit 0
        ;;
    *)
        main "$@"
        ;;
esac 