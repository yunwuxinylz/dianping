# 点评项目

## 项目概述
本项目是一个基于Spring Boot的点评系统，实现了商户点评、优惠券秒杀、用户签到等功能。项目采用了分布式架构设计，整合了多种中间件来提升系统性能和可用性。

## 技术架构

### 后端技术栈
- 核心框架：Spring Boot 2.7.18
- 数据访问：MyBatis-Plus 3.5.5
- 数据库：MySQL
- 缓存中间件：Redis
- 消息队列：RabbitMQ
- 分布式锁：Redisson 3.13.6
- 工具库：Hutool 5.8.25
- 连接池：Druid 1.2.20
- 监控：Spring Boot Actuator + Prometheus

### 系统架构
![系统架构](docs/images/architecture.svg)

## 核心功能

### 1. 分布式ID生成
使用Redis实现全局唯一ID生成器，通过时间戳和序列号的组合保证ID的唯一性和有序性。

```java
// RedisIdWorker示例
long timestamp = nowSecond - BEGIN_TIMESTAMP;
Long count = stringRedisTemplate.opsForValue()
        .increment("icr:" + keyPrefix + ":" + now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd")));
return timestamp << COUNT_BITS | count;
```

### 2. 分布式缓存
- 采用Redis作为分布式缓存，提升系统性能
- 实现缓存更新策略，解决缓存击穿、穿透、雪崩等问题
- 使用Redisson实现分布式锁，保证并发安全

### 3. 消息队列
集成RabbitMQ消息队列，支持多种消息模式：
- Direct交换机：点对点消息投递
- Topic交换机：主题订阅模式
- Fanout交换机：广播模式
- Headers交换机：基于消息头属性的路由

### 4. 登录认证
实现基于Token的用户认证机制：
- 登录拦截器：`LoginInterceptor`
- Token刷新拦截器：`RefreshTokenInterceptor`
- 路径过滤：支持动态配置白名单

## 项目结构
```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.dp
│   │   │       ├── config      // 配置类
│   │   │       ├── controller  // 控制器
│   │   │       ├── dto         // 数据传输对象
│   │   │       ├── entity      // 实体类
│   │   │       ├── mapper      // MyBatis接口
│   │   │       ├── service     // 服务层
│   │   │       └── utils       // 工具类
│   │   └── resources
│   │       ├── mapper          // MyBatis映射文件
│   │       ├── static          // 静态资源
│   │       └── application.yml // 配置文件
│   └── test                    // 测试代码
```

## 部署说明
1. 环境要求
   - JDK 1.8+
   - MySQL 5.7+
   - Redis 6.0+
   - RabbitMQ 3.8+

2. 配置说明
   - 修改`application.yml`中的数据库连接信息
   - 配置Redis连接信息
   - 配置RabbitMQ连接信息

3. 启动服务
   - 启动MySQL、Redis、RabbitMQ服务
   - 执行数据库初始化脚本
   - 运行Spring Boot应用

## 性能监控
集成Spring Boot Actuator和Prometheus，支持以下监控指标：
- JVM性能指标
- 系统资源使用情况
- 接口调用统计
- 缓存命中率
- 消息队列状态