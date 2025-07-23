# Nazo应用 Ubuntu快速启动指南

## 快速开始（5分钟部署）

### 1. 环境检查与安装

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 一键安装所有依赖
sudo apt install openjdk-21-jdk maven curl net-tools git -y

# 安装Node.js LTS
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs

# 验证安装
java -version && mvn -version && node -v && npm -v
```

### 2. 部署应用

```bash
# 进入项目目录
cd /path/to/nazo

# 给脚本执行权限
chmod +x deploy-ubuntu.sh health-check.sh

# 一键启动（自动构建和启动所有服务）
./deploy-ubuntu.sh start
```

### 3. 验证部署

```bash
# 检查服务状态
./deploy-ubuntu.sh status

# 访问应用
# 前端: http://localhost:4173
# 后端: http://localhost:8080
```

## 常用命令

```bash
# 启动服务
./deploy-ubuntu.sh start

# 停止服务
./deploy-ubuntu.sh stop

# 重启服务
./deploy-ubuntu.sh restart

# 查看状态
./deploy-ubuntu.sh status

# 查看日志
./deploy-ubuntu.sh logs

# 健康检查
./health-check.sh
```

## 外网访问配置

### 开放端口（防火墙）
```bash
sudo ufw allow 4173  # 前端
sudo ufw allow 8080  # 后端
sudo ufw enable
```

### 使用域名（可选）
如果有域名，可以配置Nginx反向代理：

```bash
# 安装Nginx
sudo apt install nginx -y

# 创建配置文件
sudo nano /etc/nginx/sites-available/nazo

# 配置内容（见完整部署指南）
# 启用配置
sudo ln -s /etc/nginx/sites-available/nazo /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

## 故障排除

### 端口占用
```bash
# 查看端口占用
sudo netstat -tlnp | grep :8080
sudo netstat -tlnp | grep :4173

# 停止服务重新启动
./deploy-ubuntu.sh stop
./deploy-ubuntu.sh start
```

### 构建失败
```bash
# 检查Java版本
java -version  # 需要21+

# 检查Maven
mvn -version

# 检查Node.js版本
node -v  # 需要18+

# 手动构建
./deploy-ubuntu.sh build
```

### 查看详细日志
```bash
# 应用日志
tail -f backend.log
tail -f frontend.log

# 系统日志
journalctl -f
```

## 生产环境建议

1. **使用systemd服务**：配置为系统服务自动启动
2. **使用Nginx**：配置反向代理和静态文件服务
3. **配置SSL**：使用Let's Encrypt免费证书
4. **定时健康检查**：设置crontab定时检查服务状态
5. **日志轮转**：配置logrotate管理日志文件

详细配置请参考 `UBUNTU_DEPLOYMENT_GUIDE.md`

## 支持

如果遇到问题：
1. 检查 `./deploy-ubuntu.sh logs` 查看错误日志
2. 运行 `./health-check.sh --no-restart` 检查服务状态
3. 参考完整部署指南解决问题 