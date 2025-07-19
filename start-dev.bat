@echo off
echo 启动 Nazo 解谜游戏开发环境...
echo.

echo [1/2] 启动后端服务器 (Spring Boot)...
start "Nazo Backend" cmd /k "mvn spring-boot:run"

echo [2/2] 等待3秒后启动前端服务器 (Vue)...
timeout /t 3

cd frontend
start "Nazo Frontend" cmd /k "npm run dev"

echo.
echo 开发环境启动完成！
echo 后端服务: http://localhost:8080
echo 前端服务: http://localhost:3000
echo.
pause 