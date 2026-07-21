# seckill-demo

Spring Boot 秒杀练习项目。覆盖登录鉴权、角色权限、Redis 抢购、下单支付和超时取消。

学习用，别当生产系统抄。

## 技术

- Java 17 / Spring Boot 3
- Spring Security + JWT + BCrypt
- MySQL + JPA
- Redis（抢购用 Lua）
- Maven、SpringDoc Swagger

## 现在做成什么样了

**用户与权限**

- 角色：`BUYER`、`MERCHANT`、`ADMIN`，注册默认 `BUYER`
- JWT 过滤器从数据库读角色，组装成 `ROLE_XXX`
- 建商品、改商品、预热/重置缓存：商家或管理员
- 改别人角色、删用户等：管理员

**秒杀链路**

1. 商家建商品，预热把库存写入 Redis  
2. 用户抢购：Lua 扣 Redis 库存 + 记限购，成功写 `PENDING` 订单  
3. 支付：  
   - 订单归属校验（只能操作自己的单，别人的统一当「订单不存在」）  
   - 条件更新订单：`PENDING` 才改成 `PAID`（挡并发重复支付）  
   - 条件扣 MySQL 库存：`stock > 0` 才减 1（挡支付超卖）  
4. 超时未付：定时任务取消订单，Lua 回滚 Redis 资格和库存  

**另外**

- 项目里还有个 Todo 接口，练缓存用的，和秒杀主线无关。

## 本地跑起来

需要：JDK 17、Maven、MySQL、Redis。

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 改数据库账号密码，库名默认示例是 user_demo，先在 MySQL 里建好库
```

```bash
cd ~/project-zz/seckill-demo   # 或你的实际路径
mvn spring-boot:run
```

Swagger（启动后试哪个能开用哪个）：

- http://localhost:8080/swagger-ui/index.html
- http://localhost:8080/swagger-ui.html

登录后把 Token 填到 Authorize（`Bearer <token>`）。

## 建议调用顺序

1. `POST /users` 注册  
2. `POST /auth/login` 登录拿 token  
3. 商家/管理员 `POST /api/products` 建商品  
4. `POST /api/products/{id}/update` 或 reset 预热 Redis  
5. `POST /api/seckill/{productId}` 抢购  
6. `GET /api/seckill/orders/{orderId}` 查单  
7. `PUT /api/seckill/pay/{orderId}` 支付  

## 设计上几点说明

| 环节 | 做法 | 为啥 |
|------|------|------|
| 抢购 | Redis + Lua | 高并发下先挡一波，资格库存走缓存 |
| 支付扣库存 | MySQL `UPDATE ... WHERE stock > 0` | 真库存落库，避免 Java 读改存空窗超卖 |
| 支付改状态 | `UPDATE ... WHERE status = PENDING` | 同一单并发支付时最多成功一次 |
| 看别人订单 | 也返回「订单不存在」 | 少泄露「有这个单但不是你的」 |

支付方法带 `@Transactional`：条件更新订单成功但扣库存失败时会回滚，订单不会卡在已支付。

## 已知限制

- 严格「两请求同一毫秒双支付」没上压测工具验过，主要靠条件更新逻辑兜底  
- 支付成功后会清 Redis 限购标记，同一用户理论上可以再抢（目前不是终身限购一次）  
- 支付和超时取消撞在一起的边界还可以再收紧（例如订单级锁），还没做  
- JWT 密钥等别往公开仓库硬编码真密钥  
- 异常文案、日志、README 都还在边做边补  

## 仓库

https://github.com/lzt-2004/seckill-demo
