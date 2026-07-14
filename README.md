# 秒杀交易练习系统

基于 Spring Boot 的后端练习项目：用户登录鉴权、秒杀抢购（Redis + Lua）、订单与支付/超时。

> 学习作品，不是生产级高并发系统。

## 技术栈

- Java 17、Spring Boot 3
- Spring Security、JWT、BCrypt
- MySQL、Spring Data JPA
- Redis（含 Lua）
- Maven、Swagger（SpringDoc）

## 模块说明（一个应用，不是三个项目）

- 用户鉴权：注册 / 登录 / JWT
- 秒杀主链路：商品、预热、抢购、订单、支付、超时任务
- 待办 Todo：缓存练习（可选，简历不单独当项目）

## 本地运行

### 环境

- JDK 17+
- Maven
- MySQL
- Redis

### 配置

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 编辑 application.yml，填写你的数据库账号密码

MySQL 中需要有库（与配置里库名一致，例如 user_demo）。

启动

mvn spring-boot:run

接口文档

启动后浏览器打开（哪个能开用哪个，自己试过后可改成确定地址）：

• http://localhost:8080/swagger-ui/index.html
• 或 http://localhost:8080/swagger-ui.html

建议调用顺序

1. 注册
2. 登录拿 Token
3. 创建商品
4. 预热库存到 Redis
5. 秒杀
6. 查订单 / 支付

已知限制

• 学习项目，并发与一致性仍有可改进点
• JWT 密钥等可能仍在源码中，公开仓库前注意

保存：`Ctrl+O` 回车，退出：`Ctrl+X`。

---


