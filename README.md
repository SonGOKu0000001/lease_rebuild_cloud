# 公寓租赁微服务系统

本项目拆分自尚硅谷公寓租赁单体项目，基于 **Spring Cloud Alibaba 2025** 微服务架构重构，将原有的单体应用拆分为多个独立微服务，并配套 Vue 3 管理后台与 H5 用户端。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端基础 | JDK / Spring Boot | 21 / 3.5.0 |
| 微服务 | Spring Cloud Alibaba / Spring Cloud | 2025.0.0.0 / 2025.0.0 |
| 注册/配置 | Nacos | 3.1.1 |
| 网关 | Spring Cloud Gateway (WebFlux) | — |
| RPC | OpenFeign | — |
| ORM | MyBatis-Plus | 3.5.9 |
| 缓存 | Redis | — |
| 对象存储 | MinIO | 8.2.0 |
| 限流熔断 | Sentinel | 1.8.9 |
| 分布式事务 | Seata | — |
| 接口文档 | knife4j (OpenAPI 3) | 4.4.0 |
| 管理端前端 | Vue 3 + Vite 4 + Element-Plus | — |
| 用户端前端 | Vue 3 + Vite 4 + Vant 4 | — |

## 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                              │
│    admin_lease (管理后台 :8080)    user_lease (H5 用户端 :8081)   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ /admin/** /app/**
┌──────────────────────────▼──────────────────────────────────────┐
│           Gateway Service (gateway-service800 :800)              │
│        路由分发 + 负载均衡 (lb://lease-*-service)                 │
└──────┬────────┬────────┬────────┬────────┬─────────┬────────────┘
       │        │        │        │        │         │
       ▼        ▼        ▼        ▼        ▼         ▼
┌─────────┐ ┌─────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐
│ Auth    │ │Apartment│ │Agreement │ │  User     │ │  Nacos   │
│ Service │ │ Service │ │ Service  │ │  Service  │ │ (Registry│
│ :1001   │ │ :1002   │ │ :1003    │ │  :1004    │ │ + Config)│
└────┬────┘ └────┬────┘ └────┬─────┘ └─────┬─────┘ └──────────┘
     │           │           │             │
     │    ┌──────▼──────┐    │      ┌──────▼──────┐
     │    │ lease_      │    │      │  lease_     │
     │    │ apartment   │    │      │  user       │
     │    │ (MySQL)     │    │      │  (MySQL)    │
     │    └─────────────┘    │      └─────────────┘
     │                      │
     │              ┌───────▼───────┐
     │              │  lease_       │
     │              │  agreement    │
     │              │  (MySQL)      │
     │              └───────────────┘
     │
     └────────── Redis (验证码缓存、JWT 无状态) ── MinIO (图片/文件存储)
```

## 项目结构

```
公寓微服务拆分/
├── lease_cloud/                  # 后端微服务工程（Maven 多模块）
│   ├── common/                   # 公共模块（工具类、Feign 接口、全局异常处理）
│   ├── gateway-service800/       # 网关服务（Spring Cloud Gateway）
│   ├── auth-service1001/         # 认证服务（登录、验证码、JWT）
│   ├── apartment-service1002/    # 公寓服务（公寓、房间、属性、设施等）
│   ├── agreement-service1003/    # 租约服务（租约、预约看房）
│   └── user-service1004/         # 用户服务（系统用户、租客用户、岗位）
├── admin_lease/                  # 管理后台前端（Vue 3 + Element-Plus）
├── user_lease/                   # H5 用户端前端（Vue 3 + Vant 4）
├── docs/                         # 开发文档
│   ├── 建表语句.md               # 数据库 DDL（28 张表）
│   ├── 开发文档.md               # 开发规范与设计说明
│   └── 测试数据.md               # 测试数据脚本
└── .gitignore
```

## 微服务模块说明

| 模块 | 端口 | 职责 | 数据库 |
|------|------|------|--------|
| **gateway-service800** | 800 | 统一入口，路由分发，路径匹配 `/admin/**` / `/app/**` 到对应服务 | 无 |
| **auth-service1001** | 1001 | 登录认证、图形验证码、JWT 签发、用户注册 | 无（Redis + Feign 调用） |
| **apartment-service1002** | 1002 | 公寓/房间/属性/设施/费用/标签/租赁期限/图片的管理与查询 | `lease_apartment`（23 表） |
| **agreement-service1003** | 1003 | 租约签订/续签/到期、预约看房、定时任务处理到期租约 | `lease_agreement`（2 表） |
| **user-service1004** | 1004 | 系统用户/岗位/租客用户管理、内部用户查询与创建 | `lease_user`（3 表） |
| **common** | — | 公共依赖：JWT 工具、MinIO 配置、Redis 配置、MyBatis-Plus 配置、全局异常处理、Feign 接口定义、登录拦截器 | 无 |

## 数据库设计

4 个独立数据库，每个微服务仅操作自己的数据库：

| 数据库 | 表数 | 归属服务 |
|--------|------|----------|
| `lease_apartment` | 23 | apartment-service1002 |
| `lease_agreement` | 2 | agreement-service1003 |
| `lease_user` | 3 | user-service1004 |
| `lease_auth` | 0 | auth-service1001（无表，认证信息存 Redis 与 Feign 调用） |

DDL 脚本见 `docs/建表语句.md`。

## 配置文件准备

> 敏感配置文件（含 MySQL 密码、MinIO 密钥、邮箱授权码、内网 IP 等）已被 `.gitignore` 忽略，**不会提交到 GitHub**。每个模块下仅提交了 `*.example` 模板文件。

### 后端配置

每个微服务模块的 `src/main/resources/` 下均有 `application.yml.example` 和 `application-test.yml.example` 模板，首次部署时请复制为实际配置文件并填写真实值：

```bash
# 以 gateway 为例，其他服务（auth/apartment/agreement/user）同理
cp lease_cloud/gateway-service800/src/main/resources/application.yml.example \
   lease_cloud/gateway-service800/src/main/resources/application.yml

# 如需要使用 test 环境，还需复制 application-test.yml.example
cp lease_cloud/gateway-service800/src/main/resources/application-test.yml.example \
   lease_cloud/gateway-service800/src/main/resources/application-test.yml
```

`.example` 模板中使用 `${MYSQL_HOST}`、`${REDIS_HOST}`、`${MINIO_HOST}` 等占位符，请替换为实际环境地址与凭据。

### 前端配置

`admin_lease/` 和 `user_lease/` 的 `.env.development` / `.env.production` 已提交至仓库（仅含 `BASE_URL` 和标题），无需额外创建。如需配置高德地图等第三方密钥，请参考 `.env.example` 在 `.env.local` 中配置（该文件已被 `.gitignore` 忽略）。

## 环境要求

- JDK 21+
- Maven 3.9+
- Nacos 2.x（注册中心 + 配置中心）
- Redis（验证码缓存、JWT 无状态校验）
- MySQL 8.0+（4 个数据库）
- MinIO（图片/文件存储）
- Node.js 18+（前端）

## 快速启动

### 1. 基础设施

按以下顺序启动基础服务：

1. Nacos（注册中心 + 配置中心）
2. Redis
3. MySQL（导入 `docs/建表语句.md` 中的 DDL 创建 4 个数据库及表）
4. MinIO（创建 `lease` bucket）

### 2. 构建后端

```bash
# 先安装 common 模块（供其他模块依赖）
cd lease_cloud
mvn -pl common -am clean install -DskipTests

# 构建全部模块
mvn clean package -DskipTests

# 按以下顺序启动服务（依赖关系：网关 → 数据服务 → 认证服务）
# 1. gateway-service800
# 2. user-service1004
# 3. apartment-service1002
# 4. agreement-service1003
# 5. auth-service1001
```

每个模块的启动类均位于 `com.kami.cloud.lease` 包下，模块名即 `*Application.java`。

### 3. 启动前端

```bash
# 管理后台（admin_lease）
cd admin_lease
npm install
npm run dev          # 开发模式，默认 :8080

# H5 用户端（user_lease）
cd user_lease
npm install
npm run dev          # 开发模式，默认 :8081
```

## API 文档

启动后端服务后，可通过 knife4j 在线文档查看接口：

| 服务 | 文档地址 |
|------|----------|
| Auth Service | `http://localhost:1001/doc.html` |
| Apartment Service | `http://localhost:1002/doc.html` |
| Agreement Service | `http://localhost:1003/doc.html` |
| User Service | `http://localhost:1004/doc.html` |

所有前端请求通过网关 `http://localhost:800` 统一转发，路径前缀 `/admin/**` 走管理端接口，`/app/**` 走用户端接口。

## Feign 内部调用

各服务之间通过 OpenFeign 进行内部 RPC 调用，所有内部接口路径以 `/inner/**` 为前缀，不经过网关，不校验 `access-token`：

| 接口定义 | 所在模块 | 调用方 |
|----------|----------|--------|
| `UserFeignApi` | common | auth-service（登录时查用户） |
| `ApartmentFeignApi` | common | agreement-service / user-service |
| `AgreementFeignApi` | common | apartment-service（查租约统计） |