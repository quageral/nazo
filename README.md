# Nazo 解谜游戏

一个类似于 Nazo 的闯关解谜游戏网站，采用前后端分离架构。

## 技术栈

### 后端
- **Java 17** + **Spring Boot 3.2.0**
- REST API 架构
- Maven 项目管理
- 内置 Tomcat 服务器

### 前端
- **Vue 3** + **TypeScript**
- **Tailwind CSS** 样式框架
- **Vite** 构建工具
- **Vue Router** 路由管理

## 项目结构

```
nazo/
├── src/main/java/com/nazo/           # 后端 Java 代码
│   ├── controller/
│   │   └── GameController.java       # REST API 控制器
│   └── NazoApplication.java          # Spring Boot 应用入口
├── frontend/                         # 前端 Vue 项目
│   ├── src/
│   │   ├── views/                    # 页面组件
│   │   ├── services/                 # API 服务
│   │   └── router/                   # 路由配置
│   ├── package.json
│   └── vite.config.ts
├── pom.xml                           # Maven 配置
└── README.md
```

## 功能特性

### 🎯 核心功能
- **登录系统**：用户名/密码认证
- **关卡系统**：基于 UUID 的关卡访问
- **解谜机制**：答案验证和关卡解锁
- **错误处理**：无效 UUID 重定向到错误页面

### 🎨 用户界面
- **登录页面**：简洁的登录表单
- **闯关页面**：关卡信息 + 谜题内容 + 提示按钮
- **错误页面**：友好的错误提示

### 🔒 安全特性
- JWT Token 认证（模拟）
- CORS 跨域支持
- 输入验证和错误处理

## 快速开始

### 1. 启动后端服务

```bash
# 编译并运行 Spring Boot 应用
mvn spring-boot:run

# 或者直接运行已编译的 JAR
java -jar target/nazo-tetris-1.0.0.jar
```

后端服务运行在: `http://localhost:8080`

### 2. 启动前端服务

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务运行在: `http://localhost:3000`

## API 接口

### 登录接口
```http
POST /api/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

### 获取关卡信息
```http
GET /api/level/{uuid}
```

### 提交关卡答案
```http
POST /api/level/{uuid}/complete
Content-Type: application/json

{
  "answer": "用户答案"
}
```

## 测试账户

- 用户名: `admin`, 密码: `password`
- 用户名: `player1`, 密码: `123456`

## 关卡演示

系统预置了几个示例关卡：

1. **开始关卡** (`start`) - 欢迎页面
2. **数字谜题** (`level-1-uuid-12345`) - 斐波那契数列，答案：8
3. **逻辑推理** (`level-2-uuid-67890`) - 三段论推理，答案：C

## 游戏流程

1. 用户在登录页面输入用户名密码
2. 登录成功后跳转到开始关卡
3. 解决谜题后获得下一关的 UUID
4. 通过 `域名/{uuid}` 访问下一关
5. 错误的 UUID 会重定向到错误页面

## 开发说明

### 添加新关卡

1. 在 `GameController.java` 的 `levels` Map 中添加关卡信息
2. 在 `validateAnswer` 方法中添加答案验证逻辑
3. 在 `GameView.vue` 中添加关卡专用的 UI 组件

### 扩展功能建议

- 数据库持久化（用户、关卡、进度）
- 真实的 JWT 认证
- 关卡编辑器
- 排行榜系统
- 多媒体谜题支持
