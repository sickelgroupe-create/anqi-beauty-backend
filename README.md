# 暨美丽后端服务

暨美丽项目的 Java API、权限体系和业务服务入口，为管理后台与用户端提供统一接口。

## 项目简介

本仓库基于若依后台框架进行业务化改造，负责用户认证、角色与菜单权限、验证码、系统配置、文件服务、监控能力，以及暨美丽业务模块的接口代理。数据库初始化脚本集中在 [`sql/`](sql/) 目录，并与管理后台菜单权限脚本一起交付。

## 技术栈

- Java 8
- Spring Boot 2.5.15、Spring MVC、Spring Security
- MyBatis、PageHelper、Druid
- MySQL、Redis
- JWT、Swagger、Maven
- 多模块 Maven 工程：`ruoyi-admin`、`ruoyi-common`、`ruoyi-framework`、`ruoyi-system`、`ruoyi-generator`、`ruoyi-quartz`

## 关联仓库

| 项目 | 说明 | GitHub |
| --- | --- | --- |
| anqi-beauty-backend | 后端服务 | [anqi-beauty-backend](https://github.com/sickelgroupe-create/anqi-beauty-backend) |
| anqi-beauty-admin | 管理后台 | [anqi-beauty-admin](https://github.com/sickelgroupe-create/anqi-beauty-admin) |
| anqi-beauty-app | 用户端 | [anqi-beauty-app](https://github.com/sickelgroupe-create/anqi-beauty-app) |

## 快速启动

1. 安装 JDK 8、Maven、MySQL 和 Redis。
2. 创建数据库并按顺序执行 `sql/ry_20250522.sql`、`sql/anqi-deployment-menus.sql`。
3. 复制 `.env.example` 为部署环境变量，至少设置 `DB_PASSWORD`、`TOKEN_SECRET` 和 `ANQI_INTERNAL_SECRET`，不要把真实值写回仓库。
4. 启动开发服务：

```sh
mvn spring-boot:run -pl ruoyi-admin
```

或先打包再运行：

```sh
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

默认 HTTP 端口为 `8080`，业务模块地址由 `ANQI_BUSINESS_URL` 配置。

## 项目结构

- `ruoyi-admin/`：Spring Boot 启动模块、Controller 和运行配置。
- `ruoyi-common/`：公共领域模型、工具、异常和安全基础类。
- `ruoyi-framework/`：认证、Redis、数据源、日志和 Web 框架配置。
- `ruoyi-system/`：用户、角色、菜单、部门、字典等系统业务。
- `ruoyi-generator/`、`ruoyi-quartz/`：代码生成和定时任务模块。
- `sql/`：基础表、初始化数据和暨美丽管理菜单脚本。

## 简历描述示例

负责暨美丽项目 Java 后端的认证授权、系统管理和业务接口建设，基于 Spring Boot、MyBatis、MySQL 与 Redis 支撑管理后台和用户端的统一 API 服务。
