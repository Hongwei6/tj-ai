# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

天机学堂 (TianJi Academy) — a microservice-based online education platform built with Spring Cloud Alibaba. This is a learning/study project from Heima Programmer (黑马程序员) 2023 curriculum.

**Tech Stack:** Java 11, Spring Boot 2.7.2, Spring Cloud 2021.0.3, Spring Cloud Alibaba 2021.0.1.0, MyBatis-Plus 3.4.3, Nacos (discovery + config), RabbitMQ, Redis/Redisson, MySQL 8, Seata (distributed transactions), XXL-Job, Elasticsearch 7.12.1, Knife4j (Swagger)

## Build & Run

```bash
# Build entire project
mvn clean package -DskipTests

# Build a single module
mvn clean package -pl tj-learning -am -DskipTests

# Run a service (requires Nacos + MySQL + Redis running)
java -jar tj-learning/target/tj-learning.jar --spring.profiles.active=local

# Run tests for a single module
mvn test -pl tj-learning
```

**Prerequisites:** Nacos server (config center + service discovery), MySQL 8, Redis, RabbitMQ. Config is fetched from Nacos via `bootstrap.yml` — shared configs include `shared-spring.yaml`, `shared-redis.yaml`, `shared-mybatis.yaml`, `shared-mq.yaml`, `shared-feign.yaml`, `shared-seata.yaml`, `shared-xxljob.yaml`.

**Profiles:** Each service has `dev` and `local` profiles. `dev` connects to shared dev servers; `local` points to `192.168.150.1` (local machine).

## Architecture

### Module Dependency Graph

```
tj-common (shared utilities, auto-configurations, base classes)
  ↑
tj-api (Feign client interfaces + shared DTOs)
  ↑
tj-auth-resource-sdk (auth interceptor for resource services)
  ↑
[Business Services] (each depends on tj-common, tj-api, tj-auth-resource-sdk)
```

### Gateway Routing

All API calls go through `tj-gateway` (port 10010). Routes use 2-letter prefixes with `StripPrefix=1`:

| Prefix | Service | Port | Database |
|--------|---------|------|----------|
| `/as` | tj-auth | 8081 | tj_auth |
| `/us` | tj-user | 8082 | tj_user |
| `/ss` | tj-search | 8083 | tj_search |
| `/ms` | tj-media | 8084 | tj_media |
| `/sms` | tj-message | 8085 | tj_message |
| `/cs` | tj-course | 8086 | tj_course |
| `/ps` | tj-pay | 8087 | tj_pay |
| `/ts` | tj-trade | 8088 | tj_trade |
| `/es` | tj-exam | 8089 | tj_exam |
| `/ls` | tj-learning | 8090 | tj_learning |
| `/rs` | tj-remark | 8091 | tj_remark |
| `/prs` | tj-promotion | 8092 | tj_promotion |
| `/ds` | tj-data | 8093 | tj_data |

### Core Shared Modules

- **tj-common** — Base domain objects (`R<T>` response wrapper, `PageQuery`, `PageDTO`), `UserContext` (ThreadLocal userId), `BaseEnum` interface, MyBatis auto-fill (`BaseMetaObjectHandler` fills `creater`/`updater`), distributed lock annotations (`@Lock`), RabbitMQ helpers, exception hierarchy (`BadRequestException`, `DbException`, `CommonException`), global exception handler (`CommonExceptionAdvice`).
- **tj-api** — All `@FeignClient` interfaces for inter-service calls (`UserClient`, `CourseClient`, `LearningClient`, `TradeClient`, `ExamClient`, `PromotionClient`, `RemarkClient`, `SearchClient`, `AuthClient`, `CatalogueClient`, `CategoryClient`, `SubjectClient`). Shared DTOs organized by domain (`dto/course/`, `dto/user/`, `dto/exam/`, etc.). Caffeine caches for `CategoryCache` and `RoleCache`.
- **tj-auth** — 4 sub-modules: `tj-auth-common` (JWT constants), `tj-auth-service` (auth server), `tj-auth-gateway-sdk` (gateway JWT validation), `tj-auth-resource-sdk` (resource server interceptor that extracts user to `UserContext`).

### Key Patterns

- **API Response:** All controllers return `R<T>` — auto-wrapped via `WrapperResponseBodyAdvice`. Gateway requests get HTTP 200 with business `code`; internal Feign calls use real HTTP status codes.
- **Current User:** `UserContext.getUser()` returns `Long` userId from ThreadLocal, set by `LoginAuthInterceptor` in `tj-auth-resource-sdk`.
- **Database:** Each service has its own MySQL database (`tj_xxx`). MyBatis-Plus with auto-fill for `creater`, `updater`, `create_time`, `update_time`. Logical delete field: `deleted` (boolean).
- **Inter-service Communication:** OpenFeign with Nacos discovery. Fallback factories defined in `tj-api/client/*/fallback/`.
- **Async Messaging:** RabbitMQ with topic exchanges defined in `MqConstants` — `course.topic`, `order.topic`, `learning.topic`, `sms.direct`, `pay.topic`, `like.record.topic`, `promotion_topic`, `trade.delay.topic`.
- **Enums:** Implement `BaseEnum` interface (`getValue()`, `getDesc()`). Use `@EnumValid` annotation for validation.
- **Swagger/Knife4j:** Enabled per service via `tj.swagger.*` config in `bootstrap.yml`.

### Internal Domain Structure (per business service)

```
com.tianji.<module>/
  ├── controller/     # REST controllers
  ├── service/        # Interfaces (I*Service)
  │   └── impl/       # Implementations
  ├── mapper/         # MyBatis-Plus mappers
  ├── domain/
  │   ├── po/         # Persistent objects (DB entities)
  │   ├── dto/        # Data transfer objects (input)
  │   ├── query/      # Query/page request objects
  │   └── vo/         # View objects (output)
  ├── enums/          # Business enums
  ├── constants/      # Constants and error codes
  ├── handler/        # MQ message handlers
  └── config/         # Module-specific configuration
```
