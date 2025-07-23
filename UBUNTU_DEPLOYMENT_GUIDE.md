# Nazo应用 Ubuntu部署指引

本指引将帮助你在Ubuntu系统上部署和运行Nazo前后端应用。

## 系统要求

### 操作系统
- Ubuntu 18.04 LTS 或更高版本
- 建议使用 Ubuntu 20.04 LTS 或 22.04 LTS

### 硬件要求
- CPU: 双核或更高
- 内存: 最低2GB，建议4GB
- 磁盘空间: 最低2GB可用空间

## 环境准备

### 1. 更新系统包
```bash
sudo apt update
sudo apt upgrade -y
```

### 2. 安装Java 21
```bash
# 安装OpenJDK 21
sudo apt install openjdk-21-jdk -y

# 验证安装
java -version
javac -version
```

### 3. 安装Maven
```bash
# 安装Maven
sudo apt install maven -y

# 验证安装
mvn -version
```

### 4. 安装Node.js 18+
```bash
# 使用NodeSource仓库安装最新LTS版本
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs

# 验证安装
node -v
npm -v
```

### 5. 安装其他必要工具
```bash
# 安装curl和网络工具
sudo apt install curl net-tools -y

# 安装git（如果需要克隆代码）
sudo apt install git -y
```

## 部署步骤

### 1. 下载/克隆项目代码
```bash
# 如果是从git仓库克隆
git clone <your-repository-url> nazo
cd nazo

# 或者直接上传项目文件夹到服务器
```

### 2. 给启动脚本执行权限
```bash
chmod +x deploy-ubuntu.sh
```

### 3. 启动应用
```bash
# 构建并启动所有服务
./deploy-ubuntu.sh start
```

## 脚本使用说明

### 可用命令

```bash
# 启动所有服务（包含构建）
./deploy-ubuntu.sh start

# 停止所有服务
./deploy-ubuntu.sh stop

# 重启所有服务
./deploy-ubuntu.sh restart

# 查看服务状态
./deploy-ubuntu.sh status

# 查看应用日志
./deploy-ubuntu.sh logs

# 仅构建应用（不启动）
./deploy-ubuntu.sh build

# 显示帮助信息
./deploy-ubuntu.sh help
```

### 服务端口

- **前端**: http://localhost:4173
- **后端API**: http://localhost:8080

## 防火墙配置

如果需要外部访问，请配置防火墙：

```bash
# 使用ufw防火墙
sudo ufw allow 4173  # 前端端口
sudo ufw allow 8080  # 后端端口

# 或使用iptables
sudo iptables -A INPUT -p tcp --dport 4173 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
```

## 生产环境部署建议

### 1. 使用反向代理（Nginx）

安装Nginx：
```bash
sudo apt install nginx -y
```

配置Nginx（/etc/nginx/sites-available/nazo）：
```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        proxy_pass http://localhost:4173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 后端API
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置：
```bash
sudo ln -s /etc/nginx/sites-available/nazo /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 2. 设置为系统服务

创建后端服务文件（/etc/systemd/system/nazo-backend.service）：
```ini
[Unit]
Description=Nazo Backend Service
After=network.target

[Service]
Type=simple
User=your-username
WorkingDirectory=/path/to/nazo
ExecStart=/usr/bin/java -jar target/nazo-tetris-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

创建前端服务文件（/etc/systemd/system/nazo-frontend.service）：
```ini
[Unit]
Description=Nazo Frontend Service
After=network.target

[Service]
Type=simple
User=your-username
WorkingDirectory=/path/to/nazo/nazo-frontend
ExecStart=/usr/bin/npm run preview
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用和启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable nazo-backend nazo-frontend
sudo systemctl start nazo-backend nazo-frontend
```

### 3. 日志管理

使用logrotate管理日志：
```bash
# 创建日志轮转配置
sudo tee /etc/logrotate.d/nazo << EOF
/path/to/nazo/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 644 your-username your-username
}
EOF
```

## 故障排除

### 常见问题

1. **端口被占用**
```bash
# 查看端口占用
sudo netstat -tlnp | grep :8080
sudo netstat -tlnp | grep :4173

# 杀死占用进程
sudo kill -9 <PID>
```

2. **Java版本问题**
```bash
# 查看已安装的Java版本
sudo update-alternatives --config java

# 设置JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
```

3. **内存不足**
```bash
# 为Java应用设置内存限制
java -Xmx1024m -jar target/nazo-tetris-1.0.0.jar
```

4. **权限问题**
```bash
# 确保用户有执行权限
chmod +x deploy-ubuntu.sh
sudo chown -R $USER:$USER /path/to/nazo
```

### 查看日志

```bash
# 应用日志
./deploy-ubuntu.sh logs

# 系统服务日志（如果使用systemd）
sudo journalctl -u nazo-backend -f
sudo journalctl -u nazo-frontend -f

# Nginx日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

## 监控和维护

### 健康检查脚本

创建健康检查脚本（health-check.sh）：
```bash
#!/bin/bash

# 检查后端健康状态
if curl -f http://localhost:8080 > /dev/null 2>&1; then
    echo "Backend: OK"
else
    echo "Backend: FAILED"
    # 重启后端服务
    sudo systemctl restart nazo-backend
fi

# 检查前端健康状态
if curl -f http://localhost:4173 > /dev/null 2>&1; then
    echo "Frontend: OK"
else
    echo "Frontend: FAILED"
    # 重启前端服务
    sudo systemctl restart nazo-frontend
fi
```

设置定时检查：
```bash
# 添加到crontab，每5分钟检查一次
crontab -e
# 添加以下行：
# */5 * * * * /path/to/health-check.sh >> /var/log/nazo-health.log 2>&1
```

## 性能优化

### 1. JVM优化
```bash
# 在启动命令中添加JVM参数
java -Xms512m -Xmx2g -XX:+UseG1GC -jar target/nazo-tetris-1.0.0.jar
```

### 2. 前端优化
- 启用gzip压缩（Nginx配置）
- 配置静态资源缓存
- 使用CDN加速

### 3. 系统优化
```bash
# 优化文件描述符限制
echo "* soft nofile 65536" | sudo tee -a /etc/security/limits.conf
echo "* hard nofile 65536" | sudo tee -a /etc/security/limits.conf
```

## 安全建议

1. **更新系统和依赖**
```bash
# 定期更新系统
sudo apt update && sudo apt upgrade -y
```

2. **配置防火墙**
```bash
# 只开放必要端口
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable
```

3. **SSL/TLS加密**
- 使用Let's Encrypt获取免费SSL证书
- 配置HTTPS重定向

4. **定期备份**
```bash
# 创建备份脚本
tar -czf nazo-backup-$(date +%Y%m%d).tar.gz /path/to/nazo
```

## 支持

如果遇到问题，请检查：
1. 系统要求是否满足
2. 所有依赖是否正确安装
3. 端口是否被其他服务占用
4. 防火墙配置是否正确
5. 日志文件中的错误信息

更多支持请参考项目文档或提交Issue。 