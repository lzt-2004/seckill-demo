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
...

现在 **可以先不改**，直接学 Git。

---

# 阶段 B · 本地 Git：让文件夹「会记历史」

## 先懂三步（固定，任何项目一样）

```text
git init     →  把本文件夹变成仓库（生成隐藏的 .git）
git add      →  选定「哪些文件要记进下一笔」
git commit   →  拍一版快照，写下说明

还没连 GitHub；先只在 你电脑 上记一笔。
