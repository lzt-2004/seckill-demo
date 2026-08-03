# 秒杀交易系统（seckill-demo）

个人练手的 Spring Boot 秒杀后端项目。围绕秒杀资格、订单状态、真实库存、权限校验和并发验证，实现注册登录、商品管理、抢购、取消和支付的完整链路。

> 用于学习和演示，不是可直接用于生产的完整秒杀方案。

## 技术栈

- Java 17、Spring Boot 3、Maven
- Spring Security、JWT、BCrypt、RBAC
- MySQL、Spring Data JPA、事务与条件更新
- Redis String / Set、Lua 原子脚本
- Swagger / SpringDoc、SLF4J、统一返回体与全局异常处理

## 核心设计

### 两层库存职责

| 层级 | 职责 | 变化时机 |
| --- | --- | --- |
| Redis 资格库存 | 高并发抢购资格、限购与快速拦截 | 抢购成功时扣减；取消或订单创建失败时回补 |
| MySQL 真实库存 | 记录最终成交库存 | 支付成功时通过条件更新扣减 |

抢购阶段不会直接扣减 MySQL 真实库存；用户支付成功后才扣真实库存。这样能避免大量无效抢购请求直接竞争数据库库存更新。

### 抢购流程

~~~mermaid
sequenceDiagram
    participant U as 用户
    participant S as SeckillService
    participant R as Redis Lua
    participant D as MySQL

    U->>S: POST /api/seckill/{productId}
    S->>D: 查询商品与活动时间
    S->>R: 查重 + 判断库存 + DECR + SADD
    alt Lua 返回成功
        R-->>S: 1
        S->>D: 创建 PENDING 订单
        alt 订单保存失败
            S->>R: SREM 成功后 INCR，回补资格库存
            S-->>U: 创建订单失败
        else 保存成功
            S-->>U: 返回订单号
        end
    else 用户已抢购
        R-->>S: 2
        S-->>U: 你已购买
    else 库存不足或缓存不存在
        R-->>S: 0
        S-->>U: 库存不够
    end
~~~

Redis Lua 将以下步骤合并为原子操作：

~~~text
判断库存 Key 是否存在
-> 判断用户是否在已购 Set 中
-> 判断库存是否大于 0
-> DECR 缓存库存
-> SADD 已购用户
~~~

因此避免了 Java 代码先查库存、再扣库存之间被其他请求插入的问题。

### 订单状态与取消/支付竞争

~~~text
PENDING  ->  PAID
    |
    ->  CANCELLED
~~~

- 主动取消与过期取消均先执行 PENDING 到 CANCELLED 的条件更新；只有影响行数为 1 时才执行 Redis 回补。
- 支付先执行 PENDING 到 PAID 的条件更新，再通过 stock > 0 条件更新扣减 MySQL 真实库存。
- 支付和取消并发时，只有一方可以抢到订单状态；另一方条件更新返回 0，不会错误回补库存。
- 当前业务规则：支付成功后删除 Redis 已购标记，允许用户再次参与后续抢购；但不会恢复 Redis 或 MySQL 库存。

### Redis 与 MySQL 非同事务补偿

Redis 与 MySQL 不共享一个本地事务。抢购 Lua 成功后，如果创建订单的 saveAndFlush 失败，项目会执行回滚 Lua：

~~~text
SREM seckill:users:{productId} username
-> 只有 SREM 返回 1
-> INCR stock:{productId}
~~~

补偿逻辑具备幂等性：重复执行时，第一次删除用户资格成功并加库存；后续删除返回 0，不会重复加库存。

## 权限模型

| 角色 | 权限 |
| --- | --- |
| BUYER | 抢购、查询自己的订单、支付、取消自己的待支付订单 |
| MERCHANT | 创建、修改商品，库存预热或重置 |
| ADMIN | 用户管理、批量创建测试用户、执行受控压测接口 |

JWT 过滤器校验 Token 格式、签名和过期时间，并按用户名查询当前角色后写入 SecurityContext。订单查询和取消还会比较订单归属；非本人统一返回订单不存在，避免通过订单 ID 枚举他人订单。

## API 概览

| 功能 | 方法与路径 | 说明 |
| --- | --- | --- |
| 注册 | POST /users | 注册普通用户，默认 BUYER |
| 登录 | POST /auth/login | 返回 JWT |
| 批量创建测试用户 | POST /users/test | 仅 ADMIN，创建 testUser_0 到 testUser_99 |
| 商品管理 | POST/PATCH /api/products | MERCHANT 或 ADMIN |
| 缓存重置 | POST /api/products/{id}/reset | 重建 Redis 库存和已购用户集合 |
| 抢购 | POST /api/seckill/{productId} | 用户参与秒杀 |
| 我的订单 | GET /api/seckill/orders | 仅返回当前登录用户的订单，按创建时间倒序 |
| 查询订单 | GET /api/seckill/orders/{orderId} | 仅订单本人 |
| 支付订单 | PUT /api/seckill/pay/{orderId} | 仅订单本人 |
| 取消订单 | POST /api/seckill/cancel/{orderId} | 仅订单本人且状态为 PENDING |
| 并发验证 | POST /api/test/seckill-load | 仅 ADMIN 且需显式开启 |

Swagger 地址：http://localhost:8080/swagger-ui/index.html

## 参数化并发验证

项目提供两种入口，复用同一个并发执行核心：

- 命令行测试类：src/test/java/com/example/app/loadtest/SeckillLoadTest.java
- 管理员测试接口：POST /api/test/seckill-load

压测接口有两层保护：

~~~text
1. Spring Security：仅 ADMIN 可调用
2. 配置开关：app.load-test.enabled 必须为 true
~~~

默认关闭：

~~~yaml
app:
  load-test:
    enabled: false
    base-url: http://127.0.0.1:8080
    test-password: 123456
~~~

仅在本地或受控服务器演示时开启：

~~~yaml
app:
  load-test:
    enabled: true
~~~

Swagger 请求示例：

~~~json
{
  "productId": 1,
  "userCount": 100,
  "requestsPerUser": 100,
  "threadCount": 100
}
~~~

| 参数 | 含义 |
| --- | --- |
| productId | 被测试的商品 ID |
| userCount | 使用多少个不同测试账号，账号格式为 testUser_0... |
| requestsPerUser | 每个测试账号发起的抢购次数 |
| threadCount | 客户端线程池最大并发数，当前接口限制为 1 到 200 |
| 总请求数 | userCount × requestsPerUser |

~~~mermaid
flowchart LR
    A[管理员调用压测接口] --> B[校验 ADMIN 与 enabled 开关]
    B --> C[测试账号真实调用 /auth/login 获取 JWT]
    C --> D[展开 userCount × requestsPerUser 个任务]
    D --> E[ExecutorService + CountDownLatch]
    E --> F[并发请求 /api/seckill/{productId}]
    F --> G[统计成功/库存不足/重复购买/异常]
    G --> H[返回 JSON 结果]
~~~

## 实测结果

所有测试账号先走真实登录接口获取 JWT；并发请求使用 ExecutorService 和 CountDownLatch 统一放行。

### 1. 综合防超卖与限购验证

~~~text
参数：100 用户 × 每用户 100 次 = 10000 请求，100 并发线程
Redis 缓存库存：40
结果：成功 40，重复购买 3960，库存不足 6000，接口异常 0
耗时：2813 ms
~~~

~~~text
40 + 3960 + 6000 + 0 = 10000
成功数 40 未超过缓存库存 40，无超卖
~~~

### 2. 同一用户重复抢购验证

~~~text
参数：1 用户 × 1000 次 = 1000 请求，库存充足
结果：成功 1，重复购买 999，库存不足 0，接口异常 0
耗时：353 ms
~~~

说明 Redis Set 的限购标记在并发下只允许该用户成功一次。

> 上述数据为本地开发环境测试结果，用于验证业务正确性和并发控制逻辑，不代表生产环境性能指标。

## 本地运行

### 环境

- JDK 17
- Maven
- MySQL 8
- Redis

### 配置

~~~bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
~~~

在 application.yml 中配置 MySQL、Redis 连接信息。示例数据库为 user_demo，请先创建数据库：

~~~sql
CREATE DATABASE user_demo DEFAULT CHARACTER SET utf8mb4;
~~~

### 启动

~~~bash
mvn spring-boot:run
~~~

### 推荐演示顺序

~~~text
1. 注册用户并登录获取 Token
2. 将测试用管理员账号设置为 ADMIN
3. ADMIN 调用 POST /users/test，创建 100 个测试账号
4. 创建商品并执行 POST /api/products/{id}/reset
5. 用户调用 POST /api/seckill/{productId} 创建 PENDING 订单
6. 用户调用 GET /api/seckill/orders 查看自己的订单列表
7. 查询订单详情，支付或取消订单
8. 在受控环境中开启 app.load-test.enabled，使用 ADMIN 调用压测接口
~~~

## 已知限制与后续方向

- 当前每次抢购仍会查询商品信息，JWT 过滤器也会查询用户角色；后续可缓存热点商品活动信息和角色信息，进一步降低数据库读压力。
- Redis 补偿失败目前以服务端错误日志记录，后续可增加补偿任务表与定时重试。
- 压测接口仅用于受控环境验证；生产部署默认保持 app.load-test.enabled: false。
- JWT 密钥、数据库密码和 Redis 密码不应提交到公开仓库；部署时应使用环境变量或服务器私有配置。

## 仓库

https://github.com/lzt-2004/seckill-demo
