# seckill-demo

个人练手的 Spring Boot 秒杀后端。从注册登录、角色权限，到 Redis 抢购、下单支付、超时取消都串了一遍。

不是生产项目，别直接当高并发方案抄。

## 用了什么

- Java 17、Spring Boot 3
- Spring Security + JWT + BCrypt
- MySQL、Spring Data JPA
- Redis（抢购 / 回滚用 Lua）
- Maven、SpringDoc（Swagger）

## 功能概览

**权限**

- 三种角色：`BUYER`、`MERCHANT`、`ADMIN`，新注册默认买家
- Token 里不信死角色，过滤器会查库再拼 `ROLE_XXX`
- 商品增改、库存预热：商家或管理员
- 改别人角色之类：管理员

**秒杀怎么走**

1. 商家建商品，预热把库存灌进 Redis  
2. 用户抢购：Lua 扣 Redis 资格库存，并记进已抢名单；成功生成 `PENDING` 订单（这时 MySQL 库存还不动）  
3. 支付：  
   - 只能付自己的单，别人的单统一回「订单不存在」  
   - 订单用条件更新：`PENDING` 才能改成 `PAID`  
   - 库存也用条件更新：`stock > 0` 才减 1  
   - 支付成功后从 Redis 名单里删掉自己（当前实现下付完还能再抢）  
4. 超时没付：定时任务扫，或支付时发现过期会懒取消；订单 `PENDING` → `CANCELLED`，再 Lua 把 Redis 资格吐回去  

取消订单同样是「先条件更新抢到取消权，再动 Redis」，避免和支付打架时把已支付单盖掉。

**旁路**

- `/todos` 练过缓存和 Redis 分布式锁，跟秒杀主流程没关系。

## 怎么跑

本机要有 JDK 17、Maven、MySQL、Redis。

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 改账号密码；示例库名 user_demo，没有就先建库
```

```bash
cd ~/project-zz/seckill-demo   # 按你自己的目录改
mvn spring-boot:run
```

文档地址：

http://localhost:8080/swagger-ui/index.html

Authorize 里填：`Bearer <你的token>`。

## 建议点接口顺序

1. 注册 `POST /users`  
2. 登录 `POST /auth/login`  
3. 商家建商品 `POST /api/products`  
4. 预热 `POST /api/products/{id}/update` 或 reset  
5. 抢购 `POST /api/seckill/{productId}`  
6. 查单 `GET /api/seckill/orders/{orderId}`  
7. 支付 `PUT /api/seckill/pay/{orderId}`  

## 为啥要两层库存

抢购一瞬间人多，不宜人人打 MySQL 扣真货。

- **Redis**：发资格、挡重复抢，快  
- **MySQL**：用户真付钱时再扣货，用 `WHERE stock > 0` / `WHERE status = PENDING` 把「判断」和「改」绑在一条更新里，少出现 Java 先读后写那种空窗  

支付方法挂了事务：订单条件更新成功但扣库存失败时会整笔回滚，避免单子显示已付、货却没扣上。

## 已知限制

- 没做过 JMeter 一类真高压，极限表现心里没数；功能路径和串行场景测过一些  
- 支付成功会清限购名单，不是「这商品终身只能买一次」  
- 支付和超时取消主要靠数据库条件更新互斥，没再加订单级分布式锁  
- JWT 密钥之类别往公开仓库塞生产配置  
- 异常对外统一 `ApiResponse`，细节打在服务端日志，不把堆栈塞给前端  

## 仓库

https://github.com/lzt-2004/seckill-demo
