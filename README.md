# Nazo 解谜游戏

一个类似于 Nazo 的闯关解谜游戏网站，采用前后端分离架构。

## 技术栈

### 后端

- **Java 21** + **Spring Boot 3.2.0**
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