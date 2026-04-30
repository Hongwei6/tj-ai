# 天机学堂 (TianJi Academy)

基于 Spring Cloud Alibaba 的在线教育平台，来自黑马程序员 2023 微服务实战课程。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 11 |
| 框架 | Spring Boot 2.7.2, Spring Cloud 2021.0.3 |
| 微服务 | Spring Cloud Alibaba 2021.0.1.0, Nacos (注册+配置) |
| 数据库 | MySQL 8, MyBatis-Plus 3.4.3 |
| 缓存 | Redis / Redisson |
| 消息队列 | RabbitMQ |
| 搜索 | Elasticsearch 7.12.1 |
| 分布式事务 | Seata |
| 定时任务 | XXL-Job |
| 网关 | Spring Cloud Gateway |
| 认证 | JWT + 自研权限体系 |
| API文档 | Knife4j (Swagger) |

## 服务架构

```
客户端 → tj-gateway (10010) → 各业务微服务
```

| 服务 | 端口 | 路由前缀 | 数据库 | 职责 |
|------|------|----------|--------|------|
| tj-gateway | 10010 | - | - | API网关、JWT认证、权限校验 |
| tj-auth | 8081 | /as | tj_auth | 登录认证、JWT签发、权限管理 |
| tj-user | 8082 | /us | tj_user | 用户管理（学员/教师/员工） |
| tj-course | 8086 | /cs | tj_course | 课程管理 |
| tj-learning | 8090 | /ls | tj_learning | 学习记录 |
| tj-exam | 8089 | /es | tj_exam | 考试系统 |
| tj-trade | 8088 | /ts | tj_trade | 交易订单 |
| tj-pay | 8087 | /ps | tj_pay | 支付服务 |
| tj-promotion | 8092 | /prs | tj_promotion | 优惠券/促销 |
| tj-remark | 8091 | /rs | tj_remark | 评价系统 |
| tj-search | 8083 | /ss | tj_search | 搜索服务 |
| tj-media | 8084 | /ms | tj_media | 媒体服务 |
| tj-message | 8085 | /sms | tj_message | 消息通知 |
| tj-data | 8093 | /ds | tj_data | 数据统计 |
| tj-admin | 18081 | - | - | 前端管理后台 (Vue 3) |

## 核心模块

- **tj-common** — 通用工具：`R<T>` 响应封装、`UserContext` 用户上下文、全局异常处理、分布式锁、MQ助手
- **tj-api** — Feign 客户端接口 + 共享 DTO，服务间调用的桥梁
- **tj-auth-resource-sdk** — 资源服务拦截器，自动提取用户信息到 `UserContext`

## 快速开始

### 环境依赖

- JDK 11+ (JDK 17 需添加 `--add-opens java.base/java.lang.invoke=ALL-UNNAMED`)
- Maven 3.6+
- MySQL 8、Redis、Nacos、RabbitMQ、Elasticsearch 7.12.1

### 构建

```bash
# 构建全部
mvn clean package -DskipTests

# 构建单个模块
mvn clean package -pl tj-learning -am -DskipTests
```

### 启动

1. 启动基础服务（Nacos、MySQL、Redis、RabbitMQ、ES）
2. 修改各服务 `bootstrap-local.yml` 中的 `discovery.ip` 为本机 IP
3. 按顺序启动：tj-gateway → tj-auth → tj-user → 其他业务服务
4. 启动前端：`cd tj-admin && npm run dev`

### 管理员登录

- 手机号：`13800000001`
- 密码：`123456`

### Nacos 配置

共享配置在 Nacos 的 `tjxt-dev` 命名空间中，包括：
`shared-spring.yaml`、`shared-redis.yaml`、`shared-mybatis.yaml`、`shared-mq.yaml`、`shared-feign.yaml`、`shared-seata.yaml`、`shared-xxljob.yaml`

## 在线笔记

https://du1in9.github.io/tj-exam/
