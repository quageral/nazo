@echo off
echo 启动 Nazo 解谜游戏开发环境 (调试模式)...
echo.

echo [调试模式] 启动后端服务器 (Spring Boot)...
echo 调试端口: 5005
echo 可以通过 localhost:5005 连接调试器
echo.

start "Nazo Backend Debug" cmd /k "mvn spring-boot:run -Dspring-boot.run.jvmArguments="^"-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"^""

echo.
echo 调试服务器已启动！
echo 在IDE中连接到 localhost:5005 进行调试
echo.
pause 