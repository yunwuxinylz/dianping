# 点评系统 (DianPing)

## 项目简介
点评系统是一个基于Spring Boot的商铺点评平台，类似大众点评，提供用户注册登录、商铺浏览、优惠券秒杀、商品订单、商户点评、用户关注等功能。

## 技术栈
- Spring Boot
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- AOP
- 分布式锁
- 定时任务

## 目录结构
```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── dp
│   │   │           ├── aspect        # 切面相关
│   │   │           ├── config        # 配置类
│   │   │           ├── controller    # 控制器
│   │   │           ├── dto           # 数据传输对象
│   │   │           ├── entity        # 实体类
│   │   │           ├── listener      # 消息监听器
│   │   │           ├── mapper        # MyBatis映射器
│   │   │           ├── service       # 服务接口
│   │   │           │   └── impl      # 服务实现
│   │   │           └── utils         # 工具类
│   │   └── resources
│   │       ├── db                   # 数据库脚本
│   │       ├── mapper               # XML映射文件
│   │       ├── static               # 静态资源
│   │       ├── application.yaml     # 应用配置
│   │       ├── seckill.lua          # 秒杀Lua脚本
│   │       └── unlock.lua           # 解锁Lua脚本
│   └── test                         # 测试代码
```

## 统一响应格式

系统所有接口都使用统一的响应格式：

```json
{
  "success": true|false,      // 操作是否成功
  "data": object,             // 返回的数据
  "errorMsg": "错误信息",      // 错误提示，success为false时才有
  "total": 100                // 分页查询时的总记录数
}
```

## 核心功能模块

### 1. 用户模块
用户注册、登录、信息管理、登出等功能。

#### 接口详情

**1.1 发送手机验证码**  
- 请求方式：`POST /user/code`
- 请求参数：
  ```
  phone: string  // 手机号码
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": "验证码发送成功"
  }
  ```
- 错误示例：
  ```json
  {
    "success": false,
    "errorMsg": "手机号格式错误！"
  }
  ```

**1.2 用户注册**
- 请求方式：`POST /user/register`
- 请求头：
  ```
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "phone": "13800138000",  // 手机号
    "code": "123456",        // 验证码
    "password": "your_password" // 密码
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": "jwt令牌"  // 注册成功后自动登录，返回token
  }
  ```
- 错误示例：
  ```json
  {
    "success": false,
    "errorMsg": "验证码错误"
  }
  ```

**1.3 用户登录**  
- 请求方式：`POST /user/login`
- 请求头：
  ```
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "phone": "13800138000",  // 手机号
    "code": "123456"         // 验证码登录
  }
  ```
  或
  ```json
  {
    "phone": "13800138000",     // 手机号
    "password": "your_password" // 密码登录
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": "jwt令牌"
  }
  ```
- 错误示例：
  ```json
  {
    "success": false,
    "errorMsg": "用户不存在，请注册"
  }
  ```

**1.4 用户登出**
- 请求方式：`POST /user/logout`
- 请求参数：
  ```
  phone: string  // 手机号码
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": "登出成功"
  }
  ```

**1.5 获取当前用户信息**  
- 请求方式：`GET /user/me`
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "nickName": "用户昵称",
      "icon": "头像地址"
    }
  }
  ```

**1.6 查询用户详情**  
- 请求方式：`GET /user/info/{id}`
- 路径参数：
  ```
  id: long  // 用户ID
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": {
      "userId": 1,
      "city": "杭州",
      "introduce": "个人介绍",
      "fans": 10,
      "followee": 20,
      "gender": true,  // true-女，false-男
      "birthday": "2000-01-01",
      "credits": 100,
      "level": false   // false-未开通会员，true-已开通
    }
  }
  ```

**1.7 更新用户详细信息**
- 请求方式：`PUT /user/info`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "userId": 1,
    "city": "杭州",
    "introduce": "这是我的新介绍",
    "gender": true,
    "birthday": "2000-01-01"
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": "更新成功"
  }
  ```

**1.8 更新用户基本信息**
- 请求方式：`PUT /user/update`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "id": 1,
    "nickName": "新昵称",
    "icon": "新头像地址"
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": "更新成功"
  }
  ```

### 2. 商铺模块
商铺信息管理、商铺类型管理、商铺搜索、地理位置排序等功能。

#### 接口详情

**2.1 查询商铺信息**  
- 请求方式：`GET /shop/{id}`
- 路径参数：
  ```
  id: long  // 商铺ID
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "name": "商铺名称",
      "typeId": 1,
      "images": "图片地址",
      "area": "地区",
      "address": "详细地址",
      "x": 120.123456,
      "y": 30.123456,
      "avgPrice": 100,
      "sold": 200,
      "comments": 300,
      "score": 4.5,
      "openHours": "10:00-22:00",
      "createTime": "2022-01-01 12:00:00",
      "updateTime": "2022-01-02 12:00:00"
    }
  }
  ```

**2.2 按类型查询商铺**  
- 请求方式：`GET /shop/of/type`
- 请求参数：
  ```
  typeId: int      // 商铺类型ID
  current: int     // 当前页码，默认1
  x: double        // 经度，可选
  y: double        // 纬度，可选
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "name": "商铺名称",
        "typeId": 1,
        "images": "图片地址",
        "area": "地区",
        "address": "详细地址",
        "x": 120.123456,
        "y": 30.123456,
        "avgPrice": 100,
        "sold": 200,
        "comments": 300,
        "score": 4.5,
        "openHours": "10:00-22:00",
        "distance": 1500  // 距离，单位米
      }
    ],
    "total": 20
  }
  ```

**2.3 新增商铺**  
- 请求方式：`POST /shop`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "name": "商铺名称",
    "typeId": 1,
    "images": "图片地址",
    "area": "地区",
    "address": "详细地址",
    "x": 120.123456,
    "y": 30.123456,
    "avgPrice": 100,
    "openHours": "10:00-22:00"
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 1  // 新增商铺的ID
  }
  ```

**2.4 更新商铺**  
- 请求方式：`PUT /shop`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "id": 1,
    "name": "商铺名称",
    "typeId": 1,
    "images": "图片地址",
    "area": "地区",
    "address": "详细地址",
    "x": 120.123456,
    "y": 30.123456,
    "avgPrice": 100,
    "openHours": "10:00-22:00"
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```

**2.5 获取商铺类型列表**  
- 请求方式：`GET /shop-type/list`
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "name": "美食",
        "icon": "图标地址",
        "sort": 1
      },
      {
        "id": 2,
        "name": "KTV",
        "icon": "图标地址",
        "sort": 2
      }
    ]
  }
  ```

**2.6 根据关键词搜索商铺**  
- 请求方式：`GET /shop/search`
- 请求参数：
  ```
  keyword: string  // 搜索关键词
  current: int     // 当前页码，默认1
  x: double        // 经度，可选
  y: double        // 纬度，可选
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "name": "商铺名称",
        "typeId": 1,
        "typeName": "美食",
        "images": "图片地址",
        "area": "地区",
        "address": "详细地址",
        "x": 120.123456,
        "y": 30.123456,
        "avgPrice": 100,
        "sold": 200,
        "comments": 300,
        "score": 4.5,
        "openHours": "10:00-22:00",
        "distance": 1500  // 距离，单位米
      }
    ],
    "total": 5
  }
  ```

**2.7 根据商铺名称关键词查询商铺**
- 请求方式：`GET /shop/name`
- 请求参数：
  ```
  name: string     // 商铺名称关键词
  current: int     // 当前页码，默认1
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "name": "商铺名称",
        "typeId": 1,
        "typeName": "美食",
        "images": "图片地址",
        "area": "地区",
        "address": "详细地址",
        "avgPrice": 100,
        "sold": 200,
        "comments": 300,
        "score": 4.5
      }
    ],
    "total": 5
  }
  ```

### 3. 优惠券模块
普通优惠券、秒杀优惠券、异步下单。

#### 接口详情

**3.1 新增普通优惠券**  
- 请求方式：`POST /voucher`
- 请求参数：
  ```json
  {
    "shopId": 1,           // 商铺ID
    "title": "100元代金券", // 优惠券标题
    "subTitle": "周一至周五可用", // 副标题
    "rules": "使用规则",    // 使用规则
    "payValue": 8000,      // 支付金额，单位分
    "actualValue": 10000,  // 实际价值，单位分
    "type": 0              // 优惠券类型：0-普通券，1-秒杀券
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 1  // 新增优惠券的ID
  }
  ```

**3.2 新增秒杀优惠券**  
- 请求方式：`POST /voucher/seckill`
- 请求参数：
  ```json
  {
    "shopId": 1,           // 商铺ID
    "title": "100元代金券", // 优惠券标题
    "subTitle": "周一至周五可用", // 副标题
    "rules": "使用规则",    // 使用规则
    "payValue": 8000,      // 支付金额，单位分
    "actualValue": 10000,  // 实际价值，单位分
    "type": 1,             // 优惠券类型：0-普通券，1-秒杀券
    "stock": 100,          // 库存
    "beginTime": "2022-01-01T10:00:00", // 开始时间
    "endTime": "2022-01-01T22:00:00"    // 结束时间
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 1  // 新增优惠券的ID
  }
  ```

**3.3 查询店铺优惠券**  
- 请求方式：`GET /voucher/list/{shopId}`
- 路径参数：
  ```
  shopId: long  // 商铺ID
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "shopId": 1,
        "title": "100元代金券",
        "subTitle": "周一至周五可用",
        "rules": "使用规则",
        "payValue": 8000,
        "actualValue": 10000,
        "type": 1,
        "stock": 100,
        "beginTime": "2022-01-01T10:00:00",
        "endTime": "2022-01-01T22:00:00"
      }
    ]
  }
  ```

**3.4 秒杀下单**  
- 请求方式：`POST /voucher-order/seckill/{id}`
- 路径参数：
  ```
  id: long  // 优惠券ID
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 123456789  // 订单ID
  }
  ```

### 4. 商品模块
商品信息管理与查询。

#### 接口详情

**4.1 查询商品信息**  
- 请求方式：`GET /goods/{id}`
- 路径参数：
  ```
  id: long  // 商品ID
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "name": "商品名称",
      "price": 9900,
      "description": "商品描述",
      "imageUrl": "图片地址",
      "stock": 100
    }
  }
  ```

**4.2 添加商品**  
- 请求方式：`POST /goods`
- 请求参数：
  ```json
  {
    "name": "商品名称",
    "price": 9900,
    "description": "商品描述",
    "imageUrl": "图片地址",
    "stock": 100
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 1  // 新增商品的ID
  }
  ```

**4.3 更新商品**  
- 请求方式：`PUT /goods`
- 请求参数：
  ```json
  {
    "id": 1,
    "name": "商品名称",
    "price": 9900,
    "description": "商品描述",
    "imageUrl": "图片地址",
    "stock": 100
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```

### 5. 订单模块
订单创建、支付、查询和管理功能。

#### 接口详情

**5.1 创建订单**  
- 请求方式：`POST /order/create`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "goodsId": 1,    // 商品ID
    "count": 2       // 商品数量
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 123456789  // 订单ID
  }
  ```
- 错误示例：
  ```json
  {
    "success": false,
    "errorMsg": "库存不足"
  }
  ```

**5.2 支付订单**  
- 请求方式：`POST /order/pay/{orderId}`
- 路径参数：
  ```
  orderId: long  // 订单ID
  ```
- 请求参数：
  ```
  payType: int  // 支付方式，1-微信支付，2-支付宝
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```
- 错误示例：
  ```json
  {
    "success": false,
    "errorMsg": "支付失败"
  }
  ```

**5.3 查询订单列表**  
- 请求方式：`GET /order/list`
- 请求头：
  ```
  Authorization: token值
  ```
- 请求参数：
  ```
  status: int   // 订单状态，可选：0-全部，1-未支付，2-已支付，3-已取消
  current: int  // 当前页码，默认1
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 123456789,
        "userId": 1,
        "goodsId": 1,
        "goodsName": "商品名称",
        "count": 2,
        "amount": 19800,
        "status": 1,       // 订单状态：1-未支付，2-已支付，3-已取消，4-已完成
        "createTime": "2022-01-01 12:00:00",
        "payTime": "2022-01-01 12:05:00",
        "payType": 1       // 支付方式：1-微信支付，2-支付宝
      }
    ],
    "total": 10
  }
  ```

**5.4 查询订单详情**  
- 请求方式：`GET /order/status/{orderId}`
- 路径参数：
  ```
  orderId: long  // 订单ID
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": {
      "id": 123456789,
      "userId": 1,
      "shopId": 10,        // 商铺ID
      "goodsId": 1,
      "goodsName": "商品名称",
      "count": 2,
      "goodsPrice": 9900,  // 商品单价，单位分
      "amount": 19800,     // 总金额，单位分
      "status": 2,         // 订单状态：1-未支付，2-已支付，3-已取消，4-已完成
      "createTime": "2022-01-01 12:00:00",
      "payTime": "2022-01-01 12:05:00",
      "payType": 1         // 支付方式：1-微信支付，2-支付宝
    }
  }
  ```
- 错误示例：
  ```json
  {
    "success": false,
    "errorMsg": "订单不存在"
  }
  ```

**5.5 取消订单**
- 请求方式：`POST /order/cancel/{orderId}`
- 路径参数：
  ```
  orderId: long  // 订单ID
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```
- 错误示例：
  ```json
  {
    "success": false,
    "errorMsg": "订单已支付，无法取消"
  }
  ```

### 6. 博客模块
用户发布博客、点赞、评论、关注用户博客等功能。

#### 接口详情

**6.1 发布博客**  
- 请求方式：`POST /blog`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "title": "博客标题",
    "content": "博客内容",
    "images": "图片地址，多个以,分隔",
    "shopId": 1  // 关联的商铺ID，可选
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 1  // 博客ID
  }
  ```
- 错误示例：
  ```json
  {
    "success": false,
    "errorMsg": "请先登录"
  }
  ```

**6.2 点赞博客**  
- 请求方式：`PUT /blog/like/{id}`
- 路径参数：
  ```
  id: long  // 博客ID
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```

**6.3 取消点赞**  
- 请求方式：`PUT /blog/unlike/{id}`
- 路径参数：
  ```
  id: long  // 博客ID
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```

**6.4 查询个人博客**  
- 请求方式：`GET /blog/of/me`
- 请求参数：
  ```
  current: int  // 当前页码，默认1
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "userId": 1,
        "title": "博客标题",
        "content": "博客内容",
        "liked": 10,
        "comments": 5,
        "createTime": "2022-01-01 12:00:00",
        "images": "图片地址",
        "shopId": 1,
        "isLike": true  // 当前用户是否点赞
      }
    ],
    "total": 10
  }
  ```

**6.5 查询热门博客**  
- 请求方式：`GET /blog/hot`
- 请求参数：
  ```
  current: int  // 当前页码，默认1
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "userId": 1,
        "title": "博客标题",
        "content": "博客内容",
        "liked": 10,
        "comments": 5,
        "createTime": "2022-01-01 12:00:00",
        "images": "图片地址",
        "shopId": 1,
        "name": "用户昵称",
        "icon": "用户头像",
        "isLike": false  // 当前用户是否点赞
      }
    ],
    "total": 50
  }
  ```

**6.6 查询博客详情**
- 请求方式：`GET /blog/{id}`
- 路径参数：
  ```
  id: long  // 博客ID
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "userId": 1,
      "title": "博客标题",
      "content": "博客内容",
      "liked": 10,
      "comments": 5,
      "createTime": "2022-01-01 12:00:00",
      "images": "图片地址",
      "shopId": 1,
      "name": "用户昵称",
      "icon": "用户头像",
      "isLike": false  // 当前用户是否点赞
    }
  }
  ```

**6.7 查询关注用户的博客**
- 请求方式：`GET /blog/of/follow`
- 请求参数：
  ```
  lastId: long  // 上一次查询的最小博客ID，用于分页
  offset: int   // 偏移量，第一次查询填0
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": {
      "list": [
        {
          "id": 10,
          "userId": 2,
          "title": "博客标题",
          "content": "博客内容",
          "liked": 10,
          "comments": 5,
          "createTime": "2022-01-01 12:00:00",
          "images": "图片地址",
          "shopId": 1,
          "name": "关注的用户",
          "icon": "用户头像",
          "isLike": true
        }
      ],
      "minTime": 1640995200000,  // 最小时间戳，用于下次查询
      "offset": 1  // 偏移量，用于下次查询
    }
  }
  ```

**6.8 查询用户的博客**
- 请求方式：`GET /blog/of/user`
- 请求参数：
  ```
  userId: long  // 目标用户ID
  current: int  // 当前页码，默认1
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "userId": 2,
        "title": "博客标题",
        "content": "博客内容",
        "liked": 10,
        "comments": 5,
        "createTime": "2022-01-01 12:00:00",
        "images": "图片地址",
        "shopId": 1,
        "name": "用户昵称",
        "icon": "用户头像",
        "isLike": false
      }
    ],
    "total": 20
  }
  ```

**6.9 添加评论**
- 请求方式：`POST /blog/comment`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "blogId": 1,
    "content": "评论内容"
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 1  // 评论ID
  }
  ```

**6.10 查询博客评论**
- 请求方式：`GET /blog/comment/{id}`
- 路径参数：
  ```
  id: long  // 博客ID
  ```
- 请求参数：
  ```
  current: int  // 当前页码，默认1
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "userId": 2,
        "blogId": 1,
        "content": "评论内容",
        "createTime": "2022-01-01 12:00:00",
        "nickName": "评论用户昵称",
        "icon": "评论用户头像"
      }
    ],
    "total": 5
  }
  ```

### 7. 关注模块
用户关注与取关、共同关注。

#### 接口详情

**7.1 关注/取关用户**  
- 请求方式：`PUT /follow/{id}/{isFollow}`
- 路径参数：
  ```
  id: long  // 被关注用户ID
  isFollow: boolean  // true为关注，false为取关
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```

**7.2 查询共同关注**  
- 请求方式：`GET /follow/common/{id}`
- 路径参数：
  ```
  id: long  // 目标用户ID
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "id": 1,
        "nickName": "用户昵称",
        "icon": "用户头像"
      }
    ]
  }
  ```

### 8. 收货地址模块
用户收货地址管理，包括添加、修改、删除、设置默认地址等功能。

#### 接口详情

**8.1 获取用户地址列表**  
- 请求方式：`GET /address/list`
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": [
      {
        "addressId": 1,
        "userId": 1,
        "recipientName": "张三",
        "phone": "13800138000",
        "province": "浙江省",
        "city": "杭州市",
        "district": "西湖区",
        "detailedAddress": "古翠路58号",
        "isDefault": true,
        "createdAt": "2022-01-01 12:00:00",
        "updatedAt": "2022-01-01 12:00:00"
      },
      {
        "addressId": 2,
        "userId": 1,
        "recipientName": "李四",
        "phone": "13900139000",
        "province": "北京市",
        "city": "北京市",
        "district": "海淀区",
        "detailedAddress": "清华大学",
        "isDefault": false,
        "createdAt": "2022-01-02 12:00:00",
        "updatedAt": "2022-01-02 12:00:00"
      }
    ]
  }
  ```

**8.2 获取默认地址**  
- 请求方式：`GET /address/default`
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": {
      "addressId": 1,
      "userId": 1,
      "recipientName": "张三",
      "phone": "13800138000",
      "province": "浙江省",
      "city": "杭州市",
      "district": "西湖区",
      "detailedAddress": "古翠路58号",
      "isDefault": true,
      "createdAt": "2022-01-01 12:00:00",
      "updatedAt": "2022-01-01 12:00:00"
    }
  }
  ```

**8.3 添加收货地址**  
- 请求方式：`POST /address`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "recipientName": "张三",
    "phone": "13800138000",
    "province": "浙江省",
    "city": "杭州市",
    "district": "西湖区",
    "detailedAddress": "古翠路58号",
    "isDefault": true
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": 1  // 新增地址的ID
  }
  ```

**8.4 更新收货地址**  
- 请求方式：`PUT /address`
- 请求头：
  ```
  Authorization: token值
  Content-Type: application/json
  ```
- 请求参数：
  ```json
  {
    "addressId": 1,
    "recipientName": "张三",
    "phone": "13800138000",
    "province": "浙江省",
    "city": "杭州市",
    "district": "西湖区",
    "detailedAddress": "古翠路60号",  // 更新后的地址
    "isDefault": true
  }
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```

**8.5 删除收货地址**  
- 请求方式：`DELETE /address/{id}`
- 路径参数：
  ```
  id: int  // 地址ID
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```

**8.6 设置默认地址**  
- 请求方式：`PUT /address/{id}/default`
- 路径参数：
  ```
  id: int  // 地址ID
  ```
- 请求头：
  ```
  Authorization: token值
  ```
- 返回示例：
  ```json
  {
    "success": true,
    "data": null
  }
  ```

## 特色功能

### 1. 分布式锁
项目使用Redis实现分布式锁，解决高并发场景下的数据一致性问题，如秒杀活动中的库存超卖。

### 2. 缓存策略
- 采用多级缓存策略提升查询性能
- 实现缓存更新和淘汰机制，保证数据一致性
- 解决缓存穿透、缓存击穿、缓存雪崩问题

### 3. 异步消息队列
使用RabbitMQ实现异步消息处理，提高系统吞吐量，解耦业务逻辑。

### 4. 定时任务
实现定时任务处理，如优惠券过期处理、订单超时处理等。

## 环境配置

### 数据库配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/dp?useSSL=false&serverTimezone=UTC
    username: root
    password: ******
```

### Redis配置
```yaml
spring:
  redis:
    host: 192.168.11.20
    port: 6379
    password: ******
    lettuce:
      pool:
        max-active: 10
        max-idle: 10
        min-idle: 1
```

### RabbitMQ配置
```yaml
spring:
  rabbitmq:
    host: 192.168.11.20
    port: 5672
    username: ******
    password: ******
    virtual-host: /
```

## 部署说明
1. 确保MySQL、Redis和RabbitMQ服务已启动
2. 执行`src/main/resources/db`目录下的SQL脚本初始化数据库
3. 修改`application.yaml`中的数据库、Redis和RabbitMQ配置
4. 使用Maven打包: `mvn clean package`
5. 运行jar包: `java -jar dp-1.0.0.jar`

## 性能监控
项目集成了Spring Boot Actuator，提供系统监控端点:
- 健康检查: `/actuator/health`
- 指标监控: `/actuator/metrics`
- Prometheus集成: `/actuator/prometheus`

## API错误码说明

系统使用统一的错误码机制，在API返回的`success`为`false`时，会返回相应的错误信息。

| 错误码 | 说明 | 示例 |
|-------|------|------|
| 400 | 请求参数错误 | 参数格式不正确 |
| 401 | 未授权 | 请先登录 |
| 403 | 禁止访问 | 无权操作 |
| 404 | 资源不存在 | 数据不存在 |
| 500 | 服务器内部错误 | 服务器异常 |
| 501 | 功能未实现 | 功能未完成 |
| 1001 | 用户相关错误 | 手机号格式错误 |
| 1002 | 验证码错误 | 验证码已过期 |
| 1003 | 登录失败 | 密码错误 |
| 2001 | 商铺相关错误 | 店铺不存在 |
| 3001 | 优惠券相关错误 | 优惠券已抢完 |
| 3002 | 下单失败 | 库存不足 |
| 4001 | 订单相关错误 | 订单不存在 |
| 4002 | 支付失败 | 支付超时 |

## 接口认证说明

系统使用基于Token的认证机制，大部分接口需要在HTTP请求头中携带Token进行身份验证。

### Token获取
用户登录后，系统返回Token字符串。

### Token使用
在请求头中添加`Authorization`字段，值为登录接口返回的Token。
```
Authorization: eyJ0eXAiOiJKV1QiLCJhbGci...
```

### Token过期
Token有效期为30分钟，过期后需重新登录获取。系统会在Token过期前自动刷新。

### 错误响应
当Token无效或过期时，API将返回401错误码和"请先登录"的错误信息。
