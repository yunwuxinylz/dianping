/*
 Navicat MySQL Data Transfer

 Source Server         : ylz
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : dp

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 13/05/2025 19:45:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_address
-- ----------------------------
DROP TABLE IF EXISTS `tb_address`;
CREATE TABLE `tb_address`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) UNSIGNED NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_default` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_address_user_id`(`user_id`) USING BTREE,
  INDEX `idx_address_is_default`(`is_default`) USING BTREE,
  CONSTRAINT `tb_address_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_address
-- ----------------------------

-- ----------------------------
-- Table structure for tb_blog
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) NOT NULL COMMENT '商户id',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '探店的照片，最多9张，多张以\",\"隔开',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '探店的文字描述',
  `liked` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '点赞数量',
  `comments` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '评论数量',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_blog
-- ----------------------------
INSERT INTO `tb_blog` VALUES (4, 4, 2, '无尽浪漫的夜晚丨在万花丛中摇晃着红酒杯🍷品战斧牛排🥩', '/imgs/blogs/7/14/4771fefb-1a87-4252-816c-9f7ec41ffa4a.jpg,/imgs/blogs/4/10/2f07e3c9-ddce-482d-9ea7-c21450f8d7cd.jpg,/imgs/blogs/2/6/b0756279-65da-4f2d-b62a-33f74b06454a.jpg,/imgs/blogs/10/7/7e97f47d-eb49-4dc9-a583-95faa7aed287.jpg,/imgs/blogs/1/2/4a7b496b-2a08-4af7-aa95-df2c3bd0ef97.jpg,/imgs/blogs/14/3/52b290eb-8b5d-403b-8373-ba0bb856d18e.jpg', '生活就是一半烟火·一半诗意<br/>手执烟火谋生活·心怀诗意以谋爱·<br/>当然<br/>\r\n男朋友给不了的浪漫要学会自己给🍒<br/>\n无法重来的一生·尽量快乐.<br/><br/>🏰「小筑里·神秘浪漫花园餐厅」🏰<br/><br/>\n💯这是一家最最最美花园的西餐厅·到处都是花餐桌上是花前台是花  美好无处不在\n品一口葡萄酒，维亚红酒马瑟兰·微醺上头工作的疲惫消失无际·生如此多娇🍃<br/><br/>📍地址:延安路200号(家乐福面)<br/><br/>🚌交通:地铁①号线定安路B口出右转过下通道右转就到啦～<br/><br/>--------------🥣菜品详情🥣---------------<br/><br/>「战斧牛排]<br/>\n超大一块战斧牛排经过火焰的炙烤发出阵阵香，外焦里嫩让人垂涎欲滴，切开牛排的那一刻，牛排的汁水顺势流了出来，分熟的牛排肉质软，简直细嫩到犯规，一刻都等不了要放入嘴里咀嚼～<br/><br/>「奶油培根意面」<br/>太太太好吃了💯<br/>我真的无法形容它的美妙，意面混合奶油香菇的香味真的太太太香了，我真的舔盘了，一丁点美味都不想浪费‼️<br/><br/><br/>「香菜汁烤鲈鱼」<br/>这个酱是辣的 真的绝好吃‼️<br/>鲈鱼本身就很嫩没什么刺，烤过之后外皮酥酥的，鱼肉蘸上酱料根本停不下来啊啊啊啊<br/>能吃辣椒的小伙伴一定要尝尝<br/><br/>非常可 好吃子🍽\n<br/>--------------🍃个人感受🍃---------------<br/><br/>【👩🏻‍🍳服务】<br/>小姐姐特别耐心的给我们介绍彩票 <br/>推荐特色菜品，拍照需要帮忙也是尽心尽力配合，太爱他们了<br/><br/>【🍃环境】<br/>比较有格调的西餐厅 整个餐厅的布局可称得上的万花丛生 有种在人间仙境的感觉🌸<br/>集美食美酒与鲜花为一体的风格店铺 令人向往<br/>烟火皆是生活 人间皆是浪漫<br/>', 1, 104, '2021-12-28 19:50:01', '2022-03-10 14:26:34');
INSERT INTO `tb_blog` VALUES (5, 1, 2, '人均30💰杭州这家港式茶餐厅我疯狂打call‼️', '/imgs/blogs/4/7/863cc302-d150-420d-a596-b16e9232a1a6.jpg,/imgs/blogs/11/12/8b37d208-9414-4e78-b065-9199647bb3e3.jpg,/imgs/blogs/4/1/fa74a6d6-3026-4cb7-b0b6-35abb1e52d11.jpg,/imgs/blogs/9/12/ac2ce2fb-0605-4f14-82cc-c962b8c86688.jpg,/imgs/blogs/4/0/26a7cd7e-6320-432c-a0b4-1b7418f45ec7.jpg,/imgs/blogs/15/9/cea51d9b-ac15-49f6-b9f1-9cf81e9b9c85.jpg', '又吃到一家好吃的茶餐厅🍴环境是怀旧tvb港风📺边吃边拍照片📷几十种菜品均价都在20+💰可以是很平价了！<br>·<br>店名：九记冰厅(远洋店)<br>地址：杭州市丽水路远洋乐堤港负一楼（溜冰场旁边）<br>·<br>✔️黯然销魂饭（38💰）<br>这碗饭我吹爆！米饭上盖满了甜甜的叉烧 还有两颗溏心蛋🍳每一粒米饭都裹着浓郁的酱汁 光盘了<br>·<br>✔️铜锣湾漏奶华（28💰）<br>黄油吐司烤的脆脆的 上面洒满了可可粉🍫一刀切开 奶盖流心像瀑布一样流出来  满足<br>·<br>✔️神仙一口西多士士（16💰）<br>简简单单却超级好吃！西多士烤的很脆 黄油味浓郁 面包体超级柔软 上面淋了炼乳<br>·<br>✔️怀旧五柳炸蛋饭（28💰）<br>四个鸡蛋炸成蓬松的炸蛋！也太好吃了吧！还有大块鸡排 上淋了酸甜的酱汁 太合我胃口了！！<br>·<br>✔️烧味双拼例牌（66💰）<br>选了烧鹅➕叉烧 他家烧腊品质真的惊艳到我！据说是每日广州发货 到店现烧现卖的黑棕鹅 每口都是正宗的味道！肉质很嫩 皮超级超级酥脆！一口爆油！叉烧肉也一点都不柴 甜甜的很入味 搭配梅子酱很解腻 ！<br>·<br>✔️红烧脆皮乳鸽（18.8💰）<br>乳鸽很大只 这个价格也太划算了吧， 肉质很有嚼劲 脆皮很酥 越吃越香～<br>·<br>✔️大满足小吃拼盘（25💰）<br>翅尖➕咖喱鱼蛋➕蝴蝶虾➕盐酥鸡<br>zui喜欢里面的咖喱鱼！咖喱酱香甜浓郁！鱼蛋很q弹～<br>·<br>✔️港式熊仔丝袜奶茶（19💰）<br>小熊🐻造型的奶茶冰也太可爱了！颜值担当 很地道的丝袜奶茶 茶味特别浓郁～<br>·', 1, 0, '2021-12-28 20:57:49', '2022-03-10 09:21:39');
INSERT INTO `tb_blog` VALUES (6, 10, 1, '杭州周末好去处｜💰50就可以骑马啦🐎', '/imgs/blogs/blog1.jpg', '杭州周末好去处｜💰50就可以骑马啦🐎', 1, 0, '2022-01-11 16:05:47', '2022-03-10 09:21:41');
INSERT INTO `tb_blog` VALUES (7, 10, 1, '杭州周末好去处｜💰50就可以骑马啦🐎', '/imgs/blogs/blog1.jpg', '杭州周末好去处｜💰50就可以骑马啦🐎', 1, 0, '2022-01-11 16:05:47', '2022-03-10 09:21:42');

-- ----------------------------
-- Table structure for tb_blog_comments
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog_comments`;
CREATE TABLE `tb_blog_comments`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户id',
  `blog_id` bigint(0) UNSIGNED NOT NULL COMMENT '探店id',
  `parent_id` bigint(0) UNSIGNED NOT NULL COMMENT '关联的1级评论id，如果是一级评论，则值为0',
  `answer_id` bigint(0) UNSIGNED NOT NULL COMMENT '回复的评论id',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复的内容',
  `liked` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '点赞数',
  `status` tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '状态，0：正常，1：被举报，2：禁止查看',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_blog_comments
-- ----------------------------

-- ----------------------------
-- Table structure for tb_cart
-- ----------------------------
DROP TABLE IF EXISTS `tb_cart`;
CREATE TABLE `tb_cart`  (
  `checked` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1',
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '购物车ID（自增主键）',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `shop_id` bigint(0) NOT NULL COMMENT '店铺ID',
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺名称',
  `shop_image` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺LOGO URL',
  `goods_id` bigint(0) NOT NULL COMMENT '商品ID',
  `sku_id` bigint(0) NOT NULL COMMENT 'SKU ID',
  `goods_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `goods_image` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品主图URL',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU规格名称（如：颜色+尺寸）',
  `price` bigint(0) NOT NULL COMMENT '加入时价格（单位：分）',
  `count` int(0) NOT NULL DEFAULT 1 COMMENT '购买数量',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_sku`(`user_id`, `sku_id`) USING BTREE COMMENT '用户+SKU唯一约束',
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '用户查询优化',
  INDEX `idx_shop_id`(`shop_id`) USING BTREE COMMENT '按店铺查询优化',
  INDEX `idx_create_time`(`create_time`) USING BTREE COMMENT '时间排序优化'
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_cart
-- ----------------------------
INSERT INTO `tb_cart` VALUES ('0', 12, 1020, 4, 'Mamala(杭州远洋乐堤港店)', 'https://img.meituan.net/msmerchant/232f8fdf09050838bd33fb24e79f30f9606056.jpg,https://qcloud.dpfile.com/pc/rDe48Xe15nQOHCcEEkmKUp5wEKWbimt-HDeqYRWsYJseXNncvMiXbuED7x1tXqN4uzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', 11, 221301, '珍珠奶茶', 'https://qcloud.dpfile.com/pc/toQMJr7lhCeRaTTmJ-cQRHtS_-J-HDeNJTrTYBNsRHAGJKpO5N-C0jVUaGxBSfkgTYGVDmosZWTLal1WbWRW3A.jpg', '中杯', 2000, 3, '2025-05-11 10:28:36', '2025-05-11 10:42:33');
INSERT INTO `tb_cart` VALUES ('0', 13, 1020, 5, '海底捞火锅(水晶城购物中心店）', 'https://img.meituan.net/msmerchant/054b5de0ba0b50c18a620cc37482129a45739.jpg,https://img.meituan.net/msmerchant/59b7eff9b60908d52bd4aea9ff356e6d145920.jpg,https://qcloud.dpfile.com/pc/Qe2PTEuvtJ5skpUXKKoW9OQ20qc7nIpHYEqJGBStJx0mpoyeBPQOJE4vOdYZwm9AuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', 14, 221601, '红烧牛肉面', 'https://qcloud.dpfile.com/pc/toQMJr7lhCeRaTTmJ-cQRHtS_-J-HDeNJTrTYBNsRHAGJKpO5N-C0jVUaGxBSfkgTYGVDmosZWTLal1WbWRW3A.jpg', '标准碗', 3200, 2, '2025-05-11 10:31:09', '2025-05-11 10:31:10');
INSERT INTO `tb_cart` VALUES ('0', 17, 1020, 4, 'Mamala(杭州远洋乐堤港店)', 'https://img.meituan.net/msmerchant/232f8fdf09050838bd33fb24e79f30f9606056.jpg,https://qcloud.dpfile.com/pc/rDe48Xe15nQOHCcEEkmKUp5wEKWbimt-HDeqYRWsYJseXNncvMiXbuED7x1tXqN4uzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', 12, 221401, '芝士茉莉茶', 'https://qcloud.dpfile.com/pc/Qe2PTT9hxilQWUQZ5bFpwH9ggYgGBfA8-S-THndxBGkFtPVVYso8S0KwMYwb2nTHTYGVDmosZWTLal1WbWRW3A.jpg', '中杯', 2200, 2, '2025-05-11 10:40:55', '2025-05-11 10:40:55');

-- ----------------------------
-- Table structure for tb_comment
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment`;
CREATE TABLE `tb_comment`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID(主键)',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '店铺ID',
  `order_id` bigint(0) UNSIGNED NOT NULL COMMENT '订单ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `score` tinyint(0) UNSIGNED NOT NULL COMMENT '评分(1-5星)',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价图片URL，多个用逗号分隔',
  `nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `is_anonymous` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否匿名评价',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商家回复内容',
  `reply_time` datetime(0) NULL DEFAULT NULL COMMENT '商家回复时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '评价时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_score`(`score`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_comment
-- ----------------------------
INSERT INTO `tb_comment` VALUES (1, 4, 24, 1013, '1111', 4, '[]', 'user_Ucb3sWmUus', 0, NULL, NULL, '2025-05-06 16:36:12', '2025-05-06 16:36:12', 'blob:http://localhost:5173/036839bd-98d6-4e62-b932-22e01671b32f');
INSERT INTO `tb_comment` VALUES (2, 4, 25, 1013, '111111111', 3, '[]', 'user_Ucb3sWmUus', 0, NULL, NULL, '2025-05-06 16:53:37', '2025-05-06 16:53:37', 'blob:http://localhost:5173/036839bd-98d6-4e62-b932-22e01671b32f');
INSERT INTO `tb_comment` VALUES (3, 2, 29, 1013, '111111', 4, '[]', 'ylz', 0, NULL, NULL, '2025-05-07 22:13:38', '2025-05-07 22:13:38', '/imgs/icon/2025/05/07/0718671cca5f49a6.png');
INSERT INTO `tb_comment` VALUES (4, 4, 28, 1013, '1111111111', 5, '[]', 'ylz', 0, NULL, NULL, '2025-05-07 22:13:46', '2025-05-07 22:13:46', '/imgs/icon/2025/05/07/0718671cca5f49a6.png');

-- ----------------------------
-- Table structure for tb_follow
-- ----------------------------
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户id',
  `follow_user_id` bigint(0) UNSIGNED NOT NULL COMMENT '关联的用户id',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_follow
-- ----------------------------

-- ----------------------------
-- Table structure for tb_goods
-- ----------------------------
DROP TABLE IF EXISTS `tb_goods`;
CREATE TABLE `tb_goods`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `price` bigint(0) UNSIGNED NOT NULL COMMENT '价格，单位是分',
  `description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品描述',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品图片，多个图片以\',\'隔开',
  `stock` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '库存',
  `sold` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态，1：上架，2：下架',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `original_price` bigint(0) NOT NULL COMMENT '原价',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_goods
-- ----------------------------
INSERT INTO `tb_goods` VALUES (1, 1, '东坡肉套餐', 6800, '传统杭帮菜代表作，选用三层五花肉慢火炖制4小时', 'https://images.pexels.com/photos/718742/pexels-photo-718742.jpeg', 50, 324, 1, '2025-05-12 23:27:43', 8800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2, 1, '龙井虾仁', 9800, '新鲜河虾仁搭配特级龙井茶叶爆炒', 'https://images.unsplash.com/photo-1567337710282-00832b415979', 40, 215, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (3, 1, '宋嫂鱼羹', 3800, '传统南宋风味鱼羹，鳜鱼熬制汤底', 'https://images.pexels.com/photos/725997/pexels-photo-725997.jpeg', 60, 158, 1, '2025-05-12 23:27:43', 4800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (4, 1, '西湖醋鱼', 8800, '选用西湖草鱼，传统糖醋汁勾芡', 'https://images.unsplash.com/photo-1585032226651-759b368d7246', 35, 89, 1, '2025-05-12 23:27:43', 10800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (5, 1, '蟹粉小笼包', 2800, '现拆蟹粉搭配鲜肉，皮薄汁多', 'https://images.pexels.com/photos/725997/pexels-photo-725997.jpeg', 100, 456, 1, '2025-05-12 23:27:43', 3800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (6, 1, '杭州片儿川', 2200, '传统杭式汤面，雪菜笋片肉片浇头', 'https://images.pexels.com/photos/12737656/pexels-photo-12737656.jpeg', 80, 287, 1, '2025-05-12 23:27:43', 2800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (7, 1, '干炸响铃', 1800, '豆腐皮包裹肉馅油炸，酥脆可口', 'https://images.unsplash.com/photo-1585032226651-759b368d7246', 120, 632, 1, '2025-05-12 23:27:43', 2500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (8, 1, '定胜糕礼盒', 4800, '传统米制糕点，吉祥寓意', 'https://images.pexels.com/photos/1055272/pexels-photo-1055272.jpeg', 45, 78, 1, '2025-05-12 23:27:43', 6800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (9, 1, '杭式卤鸭', 5800, '文火慢卤的酱香鸭肉', 'https://images.unsplash.com/photo-1585032226651-759b368d7246', 30, 45, 1, '2025-05-12 23:27:43', 7800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (10, 1, '桂花糖藕', 2800, '糯米藕段淋桂花糖汁', 'https://images.pexels.com/photos/1055272/pexels-photo-1055272.jpeg', 65, 123, 1, '2025-05-12 23:27:43', 3800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (201, 2, '豪华大床房', 39900, '32㎡全景落地窗，配备智能家居系统', 'https://images.unsplash.com/photo-1564501049412-61c2a3083791', 15, 128, 1, '2025-05-12 23:27:43', 49900, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (202, 2, '会议全日套餐', 188800, '含20人会议室8小时+茶歇3次', 'https://images.pexels.com/photos/1181396/pexels-photo-1181396.jpeg', 5, 23, 1, '2025-05-12 23:27:43', 258800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (203, 2, '周末staycation套餐', 68800, '大床房1晚+双人下午茶+延迟退房至16点', 'https://images.unsplash.com/photo-1539667468225-eebb663053e6', 10, 45, 1, '2025-05-12 23:27:43', 88800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (204, 2, '月租商务房', 599000, '连续入住30天，每日保洁服务', 'https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg', 3, 7, 1, '2025-05-12 23:27:43', 799000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (205, 2, '机场接送套餐', 12800, '专车接送机服务（萧山机场范围内）', 'https://images.pexels.com/photos/241316/pexels-photo-241316.jpeg', 20, 32, 1, '2025-05-12 23:27:43', 18800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (206, 2, '健身中心季卡', 38800, '三个月无限次使用健身中心+泳池', 'https://images.pexels.com/photos/1552242/pexels-photo-1552242.jpeg', 12, 18, 1, '2025-05-12 23:27:43', 58800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (301, 3, '黄金时段欢唱套餐', 19800, '中包间3小时+果盘+饮料无限续杯（19:00-24:00）', 'https://images.pexels.com/photos/274192/pexels-photo-274192.jpeg', 15, 132, 1, '2025-05-12 23:27:43', 29800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (302, 3, '通宵狂欢套餐', 38800, '大包间8小时（23:00-7:00）+啤酒半打', 'https://images.pexels.com/photos/1190298/pexels-photo-1190298.jpeg', 8, 45, 1, '2025-05-12 23:27:43', 58800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (303, 3, '生日派对套餐', 58800, 'VIP包厢4小时+主题布置+生日蛋糕', 'https://images.unsplash.com/photo-1519671482749-fd09be7ccebf', 5, 23, 1, '2025-05-12 23:27:43', 88800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (304, 3, '商务酒水套餐', 28800, '中包2小时+洋酒1瓶+小吃拼盘', 'https://images.pexels.com/photos/1267350/pexels-photo-1267350.jpeg', 12, 38, 1, '2025-05-12 23:27:43', 38800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (305, 3, '学生特惠包', 9800, '小包间2小时（14:00-18:00，凭学生证）', 'https://images.pexels.com/photos/1592384/pexels-photo-1592384.jpeg', 20, 87, 1, '2025-05-12 23:27:43', 15800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (306, 3, '情侣迷你包', 12800, '情侣主题包间2小时+双人饮品', 'https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2', 10, 42, 1, '2025-05-12 23:27:43', 18800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (307, 3, '公司团建套餐', 88800, '超大包间6小时+自助餐+专业音响设备', 'https://images.pexels.com/photos/3184291/pexels-photo-3184291.jpeg', 3, 12, 1, '2025-05-12 23:27:43', 128800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (308, 3, '下午茶欢唱套餐', 12800, '小包间3小时（14:00-18:00）+精致茶点', 'https://images.pexels.com/photos/405238/pexels-photo-405238.jpeg', 18, 65, 1, '2025-05-12 23:27:43', 18800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (401, 4, '招牌卤肉饭套餐', 2800, '秘制卤肉+时蔬+卤蛋+饮料', 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b', 200, 845, 1, '2025-05-12 23:27:43', 3500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (402, 4, '香辣鸡排堡套餐', 3200, '超大鸡排堡+薯条+可乐', 'https://images.pexels.com/photos/1633578/pexels-photo-1633578.jpeg', 180, 723, 1, '2025-05-12 23:27:43', 4000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (403, 4, '工作日特惠便当', 2200, '两荤一素+米饭（仅限周一至周五10:00-14:00）', 'https://images.pexels.com/photos/851184/pexels-photo-851184.jpeg', 150, 632, 1, '2025-05-12 23:27:43', 2800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (404, 4, '深夜烧烤套餐', 5800, '10串混合烧烤+啤酒（20:00-02:00专送）', 'https://images.pexels.com/photos/1267320/pexels-photo-1267320.jpeg', 80, 328, 1, '2025-05-12 23:27:43', 7800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (405, 4, '健康轻食沙拉', 3800, '鸡胸肉+藜麦+8种时蔬', 'https://images.pexels.com/photos/1211887/pexels-photo-1211887.jpeg', 120, 215, 1, '2025-05-12 23:27:43', 4800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (406, 4, '家庭分享装', 8800, '3荤2素+汤+4人份米饭', 'https://images.pexels.com/photos/958545/pexels-photo-958545.jpeg', 60, 98, 1, '2025-05-12 23:27:43', 11800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (407, 4, '早餐元气组合', 1800, '三明治+豆浆+鸡蛋（07:00-10:00专送）', 'https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg', 100, 432, 1, '2025-05-12 23:27:43', 2500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (408, 4, '下午茶甜点组', 3200, '4寸蛋糕+2杯奶茶（14:00-17:00）', 'https://images.pexels.com/photos/2144112/pexels-photo-2144112.jpeg', 50, 87, 1, '2025-05-12 23:27:43', 4500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (501, 5, '蟹粉狮子头', 6800, '传统扬州名菜，手工摔打肉丸嵌入蟹粉', 'https://images.pexels.com/photos/1192031/pexels-photo-1192031.jpeg', 45, 238, 1, '2025-05-12 23:27:43', 8800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (502, 5, '黄酒焖河鳗', 15800, '绍兴黄酒慢火煨制钱塘江河鳗', 'https://images.unsplash.com/photo-1585032226651-759b368d7246', 25, 89, 1, '2025-05-12 23:27:43', 19800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (503, 5, '桂花糖藕', 2800, '糯米灌藕配桂花蜜汁，冷热两吃', 'https://images.pexels.com/photos/1055272/pexels-photo-1055272.jpeg', 60, 156, 1, '2025-05-12 23:27:43', 3800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (504, 5, '清蒸鲥鱼', 18800, '长江鲥鱼配火腿春笋清蒸，保留鳞片油脂', 'https://images.pexels.com/photos/725997/pexels-photo-725997.jpeg', 15, 32, 1, '2025-05-12 23:27:43', 25800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (505, 5, '腌笃鲜砂锅', 7800, '咸肉鲜肉春笋文火慢炖，江南春季限定', 'https://images.unsplash.com/photo-1567337710282-00832b415979', 35, 67, 1, '2025-05-12 23:27:43', 9800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (506, 5, '龙井茶香鸡', 9800, '童子鸡用龙井茶叶熏制，茶香四溢', 'https://images.pexels.com/photos/718742/pexels-photo-718742.jpeg', 28, 43, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (601, 6, '招牌腊味木桶饭', 3200, '广式腊肠+湘西腊肉双拼，搭配秘制酱汁', 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b', 120, 856, 1, '2025-05-12 23:27:43', 3800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (602, 6, '香辣牛肉木桶饭', 3500, '嫩牛肉片配特制辣酱，可选辣度', 'https://images.pexels.com/photos/323682/pexels-photo-323682.jpeg', 95, 723, 1, '2025-05-12 23:27:43', 4200, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (603, 6, '香菇滑鸡木桶饭', 2800, '鸡腿肉+新鲜香菇，酱香口味', 'https://images.pexels.com/photos/2474661/pexels-photo-2474661.jpeg', 150, 642, 1, '2025-05-12 23:27:43', 3500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (604, 6, '酸豆角肉末木桶饭', 2500, '自制酸豆角+猪肉末，开胃首选', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c', 180, 587, 1, '2025-05-12 23:27:43', 3000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (605, 6, '黑椒牛排木桶饭', 4500, '原切牛排+黑椒汁，赠送溏心蛋', 'https://images.pexels.com/photos/1251208/pexels-photo-1251208.jpeg', 65, 328, 1, '2025-05-12 23:27:43', 5500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (606, 6, '素食田园木桶饭', 2200, '时令蔬菜+菌菇+豆腐，健康之选', 'https://images.pexels.com/photos/725991/pexels-photo-725991.jpeg', 200, 415, 1, '2025-05-12 23:27:43', 2800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (701, 7, '湖景套房', 129900, '68㎡全景套房，阳台直面西湖', 'https://images.pexels.com/photos/258154/pexels-photo-258154.jpeg', 6, 24, 1, '2025-05-12 23:27:43', 159900, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (702, 7, '游船早餐套餐', 48800, '含西湖游船票+船上定制早餐', 'https://images.pexels.com/photos/2422588/pexels-photo-2422588.jpeg', 8, 15, 1, '2025-05-12 23:27:43', 68800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (703, 7, '湖畔婚礼套餐', 888800, '含场地布置+50人餐饮+摄影服务', 'https://images.unsplash.com/photo-1519225421980-715cb0215aed', 2, 3, 1, '2025-05-12 23:27:43', 1288800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (704, 7, '茶文化体验', 18800, '龙井茶园参观+炒茶体验+品鉴会', 'https://images.pexels.com/photos/302899/pexels-photo-302899.jpeg', 15, 28, 1, '2025-05-12 23:27:43', 28800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (705, 7, '亲子度假套餐', 158800, '2大1小入住+儿童乐园通票+亲子活动', 'https://images.pexels.com/photos/2253879/pexels-photo-2253879.jpeg', 5, 12, 1, '2025-05-12 23:27:43', 208800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (706, 7, 'SPA水疗套餐', 58800, '90分钟全身精油SPA+私汤体验', 'https://images.pexels.com/photos/237371/pexels-photo-237371.jpeg', 8, 16, 1, '2025-05-12 23:27:43', 88800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (801, 8, '特惠钟点房', 9800, '4小时入住，适合临时休息', 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af', 20, 156, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (802, 8, '长租优惠房', 199000, '连续入住30天，含每周3次保洁', 'https://images.pexels.com/photos/164595/pexels-photo-164595.jpeg', 6, 9, 1, '2025-05-12 23:27:43', 259000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (803, 8, '机场接送套餐', 19800, '专车接送机（萧山机场/火车东站）', 'https://images.pexels.com/photos/1489335/pexels-photo-1489335.jpeg', 15, 42, 1, '2025-05-12 23:27:43', 25800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (804, 8, '学生特惠房', 15800, '凭学生证享特价，含早餐', 'https://images.pexels.com/photos/207691/pexels-photo-207691.jpeg', 12, 38, 1, '2025-05-12 23:27:43', 19800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (805, 8, '凌晨安心住', 12800, '凌晨0-6点入住，次日14点退房', 'https://images.pexels.com/photos/279746/pexels-photo-279746.jpeg', 18, 67, 1, '2025-05-12 23:27:43', 16800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (806, 8, '行李寄存套餐', 1800, '24小时行李保管+免费包装服务', 'https://images.pexels.com/photos/5834/nature-grass-leaf-green.jpg', 50, 213, 1, '2025-05-12 23:27:43', 2800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (901, 9, 'IMAX激光厅套餐', 9800, 'IMAX影票1张+爆米花套餐', 'https://images.unsplash.com/photo-1478720568477-152d9b164e26', 50, 328, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (902, 9, '情侣沙发座套餐', 25800, '双人VIP沙发座+情侣饮品套餐', 'https://images.pexels.com/photos/3758899/pexels-photo-3758899.jpeg', 12, 45, 1, '2025-05-12 23:27:43', 35800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (903, 9, '儿童动画专场', 6800, '儿童票+卡通爆米花桶+玩具礼物', 'https://images.pexels.com/photos/2074130/pexels-photo-2074130.jpeg', 30, 78, 1, '2025-05-12 23:27:43', 8800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (904, 9, '午夜惊悚片专场', 8800, '23:00-01:00限定场次+恐怖主题饮料', 'https://images.pexels.com/photos/1117132/pexels-photo-1117132.jpeg', 15, 42, 1, '2025-05-12 23:27:43', 10800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (905, 9, '电影主题生日派对', 58800, '私人放映厅+定制片头祝福+10人观影', 'https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2', 5, 8, 1, '2025-05-12 23:27:43', 78800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (906, 9, '艺术电影沙龙', 15800, '小众艺术片+导演交流会+限量手册', 'https://images.pexels.com/photos/33129/popcorn-movie-party-entertainment.jpg', 20, 15, 1, '2025-05-12 23:27:43', 19800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (907, 9, '电影马拉松套餐', 38800, '连续观看3部系列电影+专属休息区', 'https://images.pexels.com/photos/436413/pexels-photo-436413.jpeg', 8, 12, 1, '2025-05-12 23:27:43', 48800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (908, 9, '老年人怀旧专场', 4800, '经典老电影+免费茶水（60岁以上）', 'https://images.pexels.com/photos/1117132/pexels-photo-1117132.jpeg', 25, 18, 1, '2025-05-12 23:27:43', 6800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1001, 10, '民谣之夜套餐', 12800, '晚间民谣演出+特调茶饮', 'https://images.pexels.com/photos/164758/pexels-photo-164758.jpeg', 15, 42, 1, '2025-05-12 23:27:43', 16800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1002, 10, '茶道体验课', 8800, '90分钟专业茶艺师指导+三款名茶品鉴', 'https://images.pexels.com/photos/302899/pexels-photo-302899.jpeg', 10, 23, 1, '2025-05-12 23:27:43', 10800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1003, 10, '古琴雅集', 15800, '古琴演奏会+限量版茶点', 'https://images.pexels.com/photos/995301/pexels-photo-995301.jpeg', 8, 15, 1, '2025-05-12 23:27:43', 19800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1004, 10, '文人茶席', 22800, '私密包间+宋代点茶表演+手工茶食', 'https://images.pexels.com/photos/5473955/pexels-photo-5473955.jpeg', 5, 8, 1, '2025-05-12 23:27:43', 28800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1005, 10, '周末读书会', 6800, '主题书籍分享+无限续杯茶饮', 'https://images.pexels.com/photos/904616/pexels-photo-904616.jpeg', 20, 32, 1, '2025-05-12 23:27:43', 8800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1006, 10, '茶香手工课', 12800, '茶叶香囊制作+茶染布艺体验', 'https://images.pexels.com/photos/6344235/pexels-photo-6344235.jpeg', 12, 18, 1, '2025-05-12 23:27:43', 15800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1007, 10, '二十四节气茶宴', 38800, '当季节气主题茶餐搭配讲解', 'https://images.pexels.com/photos/675951/pexels-photo-675951.jpeg', 6, 9, 1, '2025-05-12 23:27:43', 48800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1008, 10, '深夜茶书房', 5800, '23:00-02:00静心阅读空间+安神茶', 'https://images.pexels.com/photos/2041540/pexels-photo-2041540.jpeg', 25, 38, 1, '2025-05-12 23:27:43', 7800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1101, 11, '新鲜水果套餐', 3800, '当季水果混合装（约3kg）', 'https://images.pexels.com/photos/2274787/pexels-photo-2274787.jpeg', 120, 356, 1, '2025-05-12 23:27:43', 4800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1102, 11, '净菜半成品套餐', 2800, '切配好的3菜1汤食材包', 'https://images.pexels.com/photos/4553111/pexels-photo-4553111.jpeg', 150, 278, 1, '2025-05-12 23:27:43', 3500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1103, 11, '海鲜水产组合', 5800, '活鱼+虾+贝类（全程冷链）', 'https://images.pexels.com/photos/725990/pexels-photo-725990.jpeg', 60, 132, 1, '2025-05-12 23:27:43', 7800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1104, 11, '早餐鲜食包', 1980, '鲜奶+面包+鸡蛋（当日生产）', 'https://images.pexels.com/photos/357573/pexels-photo-357573.jpeg', 200, 421, 1, '2025-05-12 23:27:43', 2580, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1105, 11, '健身蛋白餐', 4200, '鸡胸肉+西兰花+糙米饭', 'https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg', 80, 156, 1, '2025-05-12 23:27:43', 5200, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1106, 11, '火锅食材全家福', 8800, '15种涮菜+底料+蘸料', 'https://images.pexels.com/photos/6210956/pexels-photo-6210956.jpeg', 45, 78, 1, '2025-05-12 23:27:43', 11800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1107, 11, '办公室零食箱', 6800, '20款进口零食组合', 'https://images.pexels.com/photos/2641886/pexels-photo-2641886.jpeg', 60, 92, 1, '2025-05-12 23:27:43', 8800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1108, 11, '应急药品速递', 2500, '常备药组合（退烧/肠胃/创可贴）', 'https://images.pexels.com/photos/3683099/pexels-photo-3683099.jpeg', 30, 45, 1, '2025-05-12 23:27:43', 3500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1201, 12, '妈妈红烧肉套餐', 3800, '家常做法+鹌鹑蛋+米饭', 'https://images.unsplash.com/photo-1585937421612-70a008356fbe', 150, 432, 1, '2025-05-12 23:27:43', 4800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1202, 12, '老鸭煲套餐', 5800, '3小时慢炖+时蔬+米饭', 'https://images.pexels.com/photos/6210956/pexels-photo-6210956.jpeg', 80, 156, 1, '2025-05-12 23:27:43', 6800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1203, 12, '下饭菜组合', 2800, '3种招牌小炒+米饭', 'https://images.pexels.com/photos/12737656/pexels-photo-12737656.jpeg', 200, 521, 1, '2025-05-12 23:27:43', 3500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1204, 12, '儿童营养餐', 2500, '卡通造型+少油少盐', 'https://images.pexels.com/photos/675951/pexels-photo-675951.jpeg', 120, 287, 1, '2025-05-12 23:27:43', 3200, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1205, 12, '老人养生套餐', 3200, '低糖低脂+软烂易消化', 'https://images.pexels.com/photos/12737656/pexels-photo-12737656.jpeg', 90, 132, 1, '2025-05-12 23:27:43', 4000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1206, 12, '应急速食包', 1800, '5分钟即食料理（微波加热）', 'https://images.pexels.com/photos/958545/pexels-photo-958545.jpeg', 180, 356, 1, '2025-05-12 23:27:43', 2500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1207, 12, '家乡味腌制品', 3500, '农家自制腊肠/酱菜', 'https://images.pexels.com/photos/5409010/pexels-photo-5409010.jpeg', 60, 98, 1, '2025-05-12 23:27:43', 4500, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1208, 12, '手工面点组合', 2800, '包子/馒头/花卷（10个装）', 'https://images.pexels.com/photos/1055272/pexels-photo-1055272.jpeg', 100, 187, 1, '2025-05-12 23:27:43', 3800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1301, 13, '经典画舫游船票', 8000, '50分钟环湖游览，含景点讲解', 'https://images.pexels.com/photos/6276788/pexels-photo-6276788.jpeg', 501, 2367, 1, '2025-05-12 23:27:43', 10000, '2025-05-13 16:57:08');
INSERT INTO `tb_goods` VALUES (1302, 13, '夜游西湖灯光秀', 12000, '18:30-20:00夜间特别航次，欣赏灯光喷泉', 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d', 200, 680, 1, '2025-05-12 23:27:43', 15000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1303, 13, '西湖深度游套票', 18000, '含游船+雷峰塔登顶+三潭印月岛门票', 'https://images.pexels.com/photos/2422461/pexels-photo-2422461.jpeg', 300, 890, 1, '2025-05-12 23:27:43', 22000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1401, 14, '灵隐寺门票', 4500, '含飞来峰景区+灵隐寺香火券', 'https://images.unsplash.com/photo-1609142621730-db3293839541', 1000, 5232, 1, '2025-05-12 23:27:43', 5000, '2025-05-13 16:58:01');
INSERT INTO `tb_goods` VALUES (1402, 14, '禅修体验课', 8800, '2小时寺庙禅修+素斋体验', 'https://images.pexels.com/photos/685232/pexels-photo-685232.jpeg', 50, 132, 1, '2025-05-12 23:27:43', 10800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1403, 14, '文化讲解套餐', 6800, '专业讲解员带队+重点文物讲解', 'https://images.pexels.com/photos/1629212/pexels-photo-1629212.jpeg', 80, 215, 1, '2025-05-12 23:27:43', 8800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1501, 15, '首席设计师剪发', 19800, '总监级设计师服务+头皮检测+造型建议', 'https://images.pexels.com/photos/3992875/pexels-photo-3992875.jpeg', 30, 256, 1, '2025-05-12 23:27:43', 25800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1502, 15, '日系空气烫染套餐', 58800, '资生堂药水+日本技师操作+护理礼包', 'https://images.pexels.com/photos/3363720/pexels-photo-3363720.jpeg', 15, 89, 1, '2025-05-12 23:27:43', 78800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1503, 15, '头发护理三部曲', 32800, '深层清洁+纳米护理+光泽锁色', 'https://images.pexels.com/photos/3997374/pexels-photo-3997374.jpeg', 20, 132, 1, '2025-05-12 23:27:43', 42800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1504, 15, '新娘造型全案', 128800, '试妆+婚礼日跟妆+3组发型设计', 'https://images.pexels.com/photos/3671083/pexels-photo-3671083.jpeg', 5, 12, 1, '2025-05-12 23:27:43', 158800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1505, 15, '男士精修理发', 12800, '渐变修剪+胡须造型+颈部热敷', 'https://images.pexels.com/photos/3058850/pexels-photo-3058850.jpeg', 40, 187, 1, '2025-05-12 23:27:43', 16800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1601, 16, '古法泰式SPA', 32800, '90分钟全身拉伸+草药包热敷', 'https://images.unsplash.com/photo-1600335895229-6e75511892c8', 12, 45, 1, '2025-05-12 23:27:43', 42800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1602, 16, '玉石热石精油SPA', 48800, '和田玉按摩+植物精油+热石理疗', 'https://images.pexels.com/photos/4056535/pexels-photo-4056535.jpeg', 8, 23, 1, '2025-05-12 23:27:43', 58800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1603, 16, '中式经络调理', 28800, '推拿拔罐+艾灸+穴位疏通', 'https://images.pexels.com/photos/4099467/pexels-photo-4099467.jpeg', 15, 38, 1, '2025-05-12 23:27:43', 38800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1604, 16, '双人浪漫SPA套餐', 98800, '私密包厢+香薰按摩+花瓣浴', 'https://images.pexels.com/photos/6621337/pexels-photo-6621337.jpeg', 5, 9, 1, '2025-05-12 23:27:43', 128800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1605, 16, '孕期舒缓护理', 35800, '孕妇专用精油+侧卧按摩技法', 'https://images.pexels.com/photos/4473327/pexels-photo-4473327.jpeg', 6, 7, 1, '2025-05-12 23:27:43', 45800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1701, 17, '美妆惊喜礼盒', 58800, '包含5款热门大牌中小样+正装口红', 'https://images.pexels.com/photos/2533266/pexels-photo-2533266.jpeg', 120, 356, 1, '2025-05-12 23:27:43', 78800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1702, 17, '轻奢下午茶套餐', 19800, '指定餐厅双人套餐+观景座位', 'https://images.pexels.com/photos/405238/pexels-photo-405238.jpeg', 60, 142, 1, '2025-05-12 23:27:43', 25800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1703, 17, '黄金饰品工费券', 8800, '周大福/老凤祥等品牌工费5折优惠', 'https://images.pexels.com/photos/965981/pexels-photo-965981.jpeg', 80, 215, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1704, 17, '亲子游乐套票', 28800, '儿童乐园+VR体验馆+亲子餐厅代金券', 'https://images.pexels.com/photos/6474584/pexels-photo-6474584.jpeg', 45, 98, 1, '2025-05-12 23:27:43', 38800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1705, 17, '数码产品以旧换新券', 10000, '苹果/华为等品牌额外折价10%', 'https://images.pexels.com/photos/2047905/pexels-photo-2047905.jpeg', 200, 156, 1, '2025-05-12 23:27:43', 15000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1706, 17, '国际运动品牌折扣包', 38800, '耐克/阿迪达斯等品牌满1000减200', 'https://images.pexels.com/photos/2529148/pexels-photo-2529148.jpeg', 150, 287, 1, '2025-05-12 23:27:43', 58800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1707, 17, '超市购物卡', 50000, '银泰超市500元电子购物卡', 'https://images.pexels.com/photos/264547/pexels-photo-264547.jpeg', 300, 542, 1, '2025-05-12 23:27:43', 50000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1708, 17, 'VIP停车特权包', 8800, '3小时免费停车+洗车服务', 'https://images.pexels.com/photos/4480505/pexels-photo-4480505.jpeg', 100, 87, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1801, 18, '奢侈品护理套餐', 128800, '爱马仕/LV等品牌皮具清洁保养', 'https://images.pexels.com/photos/3732891/pexels-photo-3732891.jpeg', 30, 45, 1, '2025-05-12 23:27:43', 158800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1802, 18, '米其林餐厅代金券', 38800, '指定米其林餐厅满500减100', 'https://images.pexels.com/photos/262978/pexels-photo-262978.jpeg', 80, 132, 1, '2025-05-12 23:27:43', 48800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1803, 18, '高端影院套票', 28800, 'VIP厅观影+小吃套餐', 'https://images.pexels.com/photos/436413/pexels-photo-436413.jpeg', 60, 98, 1, '2025-05-12 23:27:43', 38800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1804, 18, '国际美妆体验装', 18800, 'La Mer/SK-II等品牌新品试用套装', 'https://images.pexels.com/photos/3373739/pexels-photo-3373739.jpeg', 150, 215, 1, '2025-05-12 23:27:43', 28800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1805, 18, '设计师品牌折扣券', 10000, '指定设计师品牌季末5折特权', 'https://images.pexels.com/photos/298863/pexels-photo-298863.jpeg', 200, 156, 1, '2025-05-12 23:27:43', 20000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1806, 18, '进口超市尝鲜包', 28800, '10款网红进口零食组合', 'https://images.pexels.com/photos/2641886/pexels-photo-2641886.jpeg', 120, 87, 1, '2025-05-12 23:27:43', 38800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1807, 18, '会员专属停车卡', 18800, '当日不限时停车+代客泊车', 'https://images.pexels.com/photos/4480505/pexels-photo-4480505.jpeg', 80, 65, 1, '2025-05-12 23:27:43', 28800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1808, 18, '儿童乐园畅玩卡', 38800, '全馆儿童设施不限次体验', 'https://images.pexels.com/photos/6474584/pexels-photo-6474584.jpeg', 50, 32, 1, '2025-05-12 23:27:43', 58800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1901, 19, '全能健身月卡', 12800, '器械区+团课无限次使用+体测1次', 'https://images.pexels.com/photos/221247/pexels-photo-221247.jpeg', 100, 286, 1, '2025-05-12 23:27:43', 15800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1902, 19, '私教体验套餐', 9800, '3节私教课+饮食计划制定', 'https://images.pexels.com/photos/6550826/pexels-photo-6550826.jpeg', 50, 132, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1903, 19, '企业团体卡', 88800, '10人团体3个月健身权限', 'https://images.pexels.com/photos/4662348/pexels-photo-4662348.jpeg', 20, 15, 1, '2025-05-12 23:27:43', 108800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (1904, 19, '青少年体适能课', 6800, '8-16岁专项训练（每周2次）', 'https://images.pexels.com/photos/699953/pexels-photo-699953.jpeg', 40, 28, 1, '2025-05-12 23:27:43', 8800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2001, 20, '游泳季卡', 15800, '90天无限次游泳+桑拿使用', 'https://images.unsplash.com/photo-1556817411-31ae72fa3ea0', 80, 195, 1, '2025-05-12 23:27:43', 18800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2002, 20, '亲子游泳课', 12800, '1大1小十次课程包（含教练）', 'https://images.pexels.com/photos/1263348/pexels-photo-1263348.jpeg', 30, 42, 1, '2025-05-12 23:27:43', 15800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2003, 20, '水中康复课程', 19800, '针对运动损伤的水疗康复方案', 'https://images.pexels.com/photos/5069203/pexels-photo-5069203.jpeg', 15, 8, 1, '2025-05-12 23:27:43', 22800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2004, 20, '暑期游泳培训班', 28800, '儿童游泳速成班（连续15天）', 'https://images.pexels.com/photos/863988/pexels-photo-863988.jpeg', 25, 18, 1, '2025-05-12 23:27:43', 38800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2101, 21, '标准干洗套餐', 5800, '西装/大衣专业干洗（3件起洗）', 'https://images.pexels.com/photos/4488643/pexels-photo-4488643.jpeg', 100, 542, 1, '2025-05-12 23:27:43', 6800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2102, 21, '家庭洗涤月卡', 18800, '每月20kg衣物洗护（不限次数）', 'https://images.pexels.com/photos/5709026/pexels-photo-5709026.jpeg', 50, 132, 1, '2025-05-12 23:27:43', 22800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2103, 21, '鞋类深度清洁', 3800, '真皮/运动鞋专业清洁保养', 'https://images.pexels.com/photos/6069557/pexels-photo-6069557.jpeg', 80, 215, 1, '2025-05-12 23:27:43', 4800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2104, 21, '紧急3小时快洗', 9800, '加急服务（需提前预约）', 'https://images.pexels.com/photos/5709030/pexels-photo-5709030.jpeg', 20, 45, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2105, 21, '窗帘地毯专洗', 15800, '大件物品上门取送服务', 'https://images.pexels.com/photos/6001379/pexels-photo-6001379.jpeg', 30, 28, 1, '2025-05-12 23:27:43', 19800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2201, 22, '深度保洁套餐', 16800, '4小时专业深度清洁（含厨房卫生间）', 'https://images.unsplash.com/photo-1584433144859-1fc3ab64a957', 60, 156, 1, '2025-05-12 23:27:43', 19800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2202, 22, '长期保洁月卡', 58800, '每周2次常规保洁（每次3小时）', 'https://images.pexels.com/photos/4107253/pexels-photo-4107253.jpeg', 30, 42, 1, '2025-05-12 23:27:43', 68800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2203, 22, '家电清洗套餐', 12800, '空调/油烟机/洗衣机深度清洗', 'https://images.pexels.com/photos/4488643/pexels-photo-4488643.jpeg', 45, 38, 1, '2025-05-12 23:27:43', 15800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2204, 22, '育儿嫂服务', 380000, '专业育儿嫂（26天/月）', 'https://images.pexels.com/photos/4473327/pexels-photo-4473327.jpeg', 10, 8, 1, '2025-05-12 23:27:43', 450000, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2205, 22, '春节大扫除', 28800, '全屋6小时彻底清洁（含玻璃）', 'https://images.pexels.com/photos/4107275/pexels-photo-4107275.jpeg', 50, 32, 1, '2025-05-12 23:27:43', 38800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2301, 23, '脊柱调理套餐', 29800, '专业整脊+物理治疗（3次疗程）', 'https://images.pexels.com/photos/7088521/pexels-photo-7088521.jpeg', 30, 45, 1, '2025-05-12 23:27:43', 39800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2302, 23, '运动损伤康复', 35800, '针对健身损伤的专业恢复方案', 'https://images.pexels.com/photos/5069203/pexels-photo-5069203.jpeg', 25, 18, 1, '2025-05-12 23:27:43', 45800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2303, 23, '中医推拿套餐', 19800, '60分钟经络疏通+拔罐', 'https://images.pexels.com/photos/4099467/pexels-photo-4099467.jpeg', 40, 32, 1, '2025-05-12 23:27:43', 25800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2304, 23, '产后康复计划', 68800, '盆底肌修复+腹直肌调理（10次）', 'https://images.pexels.com/photos/4473327/pexels-photo-4473327.jpeg', 15, 9, 1, '2025-05-12 23:27:43', 88800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2305, 23, '老年理疗月卡', 48800, '每周3次理疗（含艾灸）', 'https://images.pexels.com/photos/7088523/pexels-photo-7088523.jpeg', 20, 12, 1, '2025-05-12 23:27:43', 58800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2401, 24, '超声波洁牙套餐', 38800, '全口洁治+抛光+口腔检查', 'https://images.pexels.com/photos/6621334/pexels-photo-6621334.jpeg', 60, 45, 1, '2025-05-12 23:27:43', 48800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2402, 24, '儿童窝沟封闭', 28800, '3颗大牙防护（含检查）', 'https://images.pexels.com/photos/3845810/pexels-photo-3845810.jpeg', 40, 28, 1, '2025-05-12 23:27:43', 38800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2403, 24, '隐形矫正咨询', 9800, '数字化方案设计+3D模拟', 'https://images.pexels.com/photos/4269693/pexels-photo-4269693.jpeg', 30, 15, 1, '2025-05-12 23:27:43', 12800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2404, 24, '全瓷牙修复', 258800, '德国材料（含10年质保）', 'https://images.pexels.com/photos/4269688/pexels-photo-4269688.jpeg', 15, 8, 1, '2025-05-12 23:27:43', 288800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2405, 24, '牙齿美白套餐', 18800, '冷光美白（维持2年）', 'https://images.pexels.com/photos/4269689/pexels-photo-4269689.jpeg', 25, 12, 1, '2025-05-12 23:27:43', 25800, '2025-05-12 23:27:43');
INSERT INTO `tb_goods` VALUES (2406, 25, '手冲精品咖啡', 3800, '精选埃塞俄比亚耶加雪菲咖啡豆，专业手冲萃取', 'https://images.pexels.com/photos/312418/pexels-photo-312418.jpeg', 100, 258, 1, '2025-05-12 23:54:56', 4800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2407, 25, '星空拿铁', 4200, '特调蓝色星空拿铁，加入椰子糖浆与蝶豆花', 'https://images.unsplash.com/photo-1546549095-5d8160c121a4', 80, 346, 1, '2025-05-12 23:54:56', 5200, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2408, 25, '招牌提拉米苏', 3600, '意式经典甜点，马斯卡彭芝士与咖啡利口酒完美结合', 'https://images.pexels.com/photos/4087609/pexels-photo-4087609.jpeg', 50, 187, 1, '2025-05-12 23:54:56', 4600, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2409, 25, '季节限定水果茶', 4500, '新鲜水果与进口红茶完美融合，夏季特饮', 'https://images.unsplash.com/photo-1563911892437-1feda0179e1b', 60, 203, 1, '2025-05-12 23:54:56', 5200, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2410, 25, '抹茶蛋糕卷', 3200, '日式抹茶粉制作的蛋糕卷，搭配北海道奶油', 'https://images.pexels.com/photos/3628990/pexels-photo-3628990.jpeg', 40, 167, 1, '2025-05-12 23:54:56', 3800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2411, 26, '午间阅读套餐', 5800, '2小时静谧阅读空间+精选茶饮+轻食点心', 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da', 30, 156, 1, '2025-05-12 23:54:56', 6800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2412, 26, '经典文学鉴赏会', 15800, '每周主题讲座+互动讨论+签名书籍', 'https://images.pexels.com/photos/3747468/pexels-photo-3747468.jpeg', 20, 45, 1, '2025-05-12 23:54:56', 18800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2413, 26, '深夜阅读空间', 4800, '21:00后专属安静环境+安神花草茶', 'https://images.unsplash.com/photo-1532153955177-f59af40d6472', 25, 87, 1, '2025-05-12 23:54:56', 5800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2414, 26, '亲子共读时光', 6800, '周末亲子共读活动，包含儿童读物和互动环节', 'https://images.pexels.com/photos/256431/pexels-photo-256431.jpeg', 15, 68, 1, '2025-05-12 23:54:56', 7800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2415, 26, '作家见面会', 12800, '知名作家签售会，含私人交流环节和定制书签', 'https://images.unsplash.com/photo-1519682577862-22b62b24e493', 10, 32, 1, '2025-05-12 23:54:56', 15800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2416, 27, '4D特效观影', 12800, '全感官沉浸式观影体验+专属震动座椅', 'https://images.pexels.com/photos/7991580/pexels-photo-7991580.jpeg', 60, 325, 1, '2025-05-12 23:54:56', 15800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2417, 27, '双人豪华观影', 22800, 'VIP厅豪华沙发+定制餐点+香槟1杯', 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c', 20, 78, 1, '2025-05-12 23:54:56', 28800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2418, 27, '经典电影回顾展', 8800, '每周五老片放映+导演解析视频+收藏版海报', 'https://images.pexels.com/photos/3709369/pexels-photo-3709369.jpeg', 40, 96, 1, '2025-05-12 23:54:56', 10800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2419, 27, '亲子动画电影专场', 9800, '儿童友好环境，赠送角色玩偶，含爆米花饮料', 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba', 35, 115, 1, '2025-05-12 23:54:56', 12800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2420, 27, '环球纪录片展映', 7800, '国家地理纪录片精选，高清放映，含专家讲解', 'https://images.pexels.com/photos/2774289/pexels-photo-2774289.jpeg', 25, 67, 1, '2025-05-12 23:54:56', 9800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2421, 28, '九宫格麻辣火锅', 12800, '九种不同麻辣程度的锅底拼盘，适合多人分享体验', 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624', 60, 278, 1, '2025-05-12 23:54:56', 15800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2422, 28, '秘制牛油锅底套餐', 16800, '特制牛油锅底+精选肥牛+手打虾滑+特色蘸料', 'https://images.pexels.com/photos/5836771/pexels-photo-5836771.jpeg', 45, 195, 1, '2025-05-12 23:54:56', 19800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2423, 28, '养生菌菇火锅', 9800, '五种名贵菌菇熬制高汤+豆腐+菌菇拼盘+时令蔬菜', 'https://images.unsplash.com/photo-1563245372-f21724e3856d', 55, 152, 1, '2025-05-12 23:54:56', 12800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2424, 28, '海鲜鲜货火锅', 19800, '活海鲜现煮，包含龙虾、帝王蟹、鲍鱼等高级食材', 'https://images.pexels.com/photos/699953/pexels-photo-699953.jpeg', 30, 87, 1, '2025-05-12 23:54:56', 25800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2425, 28, '情侣甜蜜锅', 13800, '心形锅底，赠送情侣酒杯与纪念照，含甜品', 'https://images.unsplash.com/photo-1515669097368-22e68427d265', 20, 56, 1, '2025-05-12 23:54:56', 16800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2426, 29, '私教体能评估', 18800, '专业体能测试+身体成分分析+定制健身计划', 'https://images.pexels.com/photos/4498482/pexels-photo-4498482.jpeg', 30, 86, 1, '2025-05-12 23:54:56', 25800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2427, 29, '团体训练课程', 6800, '10人小班制HIIT高强度间歇训练，燃脂效果显著', 'https://images.unsplash.com/photo-1518310383802-640c2de311b6', 20, 156, 1, '2025-05-12 23:54:56', 8800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2428, 29, '康复训练套餐', 28800, '针对运动损伤的专业康复训练，含运动理疗', 'https://images.pexels.com/photos/3768916/pexels-photo-3768916.jpeg', 15, 42, 1, '2025-05-12 23:54:56', 35800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2429, 29, '瑜伽月卡', 9800, '每周3次专业瑜伽课，含垫子和瑜伽砖使用', 'https://images.unsplash.com/photo-1588286840104-8957b019727f', 25, 78, 1, '2025-05-12 23:54:56', 12800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2430, 29, '孕产妇健身计划', 15800, '专为孕期和产后女性设计的安全健身方案', 'https://images.pexels.com/photos/3984359/pexels-photo-3984359.jpeg', 15, 36, 1, '2025-05-12 23:54:56', 19800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2431, 30, '灵动城景房', 56800, '28㎡智能房间，落地窗俯瞰城市夜景，含双早', 'https://images.unsplash.com/photo-1566073771259-6a8506099945', 10, 45, 1, '2025-05-12 23:54:56', 68800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2432, 30, '情侣主题套房', 88800, '35㎡浪漫主题套房，含浴缸+投影设备+鲜花布置', 'https://images.pexels.com/photos/271643/pexels-photo-271643.jpeg', 5, 23, 1, '2025-05-12 23:54:56', 108800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2433, 30, '智享商务房', 46800, '25㎡全智能房间，配备工作区与智能办公设备', 'https://images.unsplash.com/photo-1520277739336-7bf67edfa768', 15, 67, 1, '2025-05-12 23:54:56', 56800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2434, 30, '亲子主题房', 66800, '40㎡亲子主题房，含儿童床、玩具和互动投影', 'https://images.pexels.com/photos/3771807/pexels-photo-3771807.jpeg', 8, 32, 1, '2025-05-12 23:54:56', 78800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2435, 30, '总统套房', 198800, '120㎡超大空间，专属管家服务，含SPA和私人厨师', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32', 2, 5, 1, '2025-05-12 23:54:56', 258800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2436, 31, '宠物全身体检', 29800, '包含血常规+B超+X光+尿检等全面检查项目', 'https://images.pexels.com/photos/6235688/pexels-photo-6235688.jpeg', 20, 65, 1, '2025-05-12 23:54:56', 35800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2437, 31, '猫咪洗护套餐', 15800, '专业洗浴+毛发护理+趾甲修剪+肛门腺挤压', 'https://images.unsplash.com/photo-1526336024174-e58f5cdd8e13', 30, 128, 1, '2025-05-12 23:54:56', 18800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2438, 31, '宠物牙齿护理', 18800, '专业超声波洁牙+牙齿抛光+口腔检查', 'https://images.pexels.com/photos/6568604/pexels-photo-6568604.jpeg', 25, 56, 1, '2025-05-12 23:54:56', 22800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2439, 31, '宠物绝育套餐', 38800, '专业无痛绝育手术，含术前检查和术后护理', 'https://images.unsplash.com/photo-1597514605667-125c696964c0', 15, 42, 1, '2025-05-12 23:54:56', 46800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2440, 31, '狗狗训练课程', 25800, '10次专业训练课，教授基础指令和行为矫正', 'https://images.pexels.com/photos/2607544/pexels-photo-2607544.jpeg', 12, 35, 1, '2025-05-12 23:54:56', 32800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2441, 32, '私人茶席体验', 28800, '专属茶艺师一对一茶艺表演+5款名茶品鉴', 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4', 10, 42, 1, '2025-05-12 23:54:56', 35800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2442, 32, '禅茶一味体验', 18800, '茶道与冥想结合的身心放松体验，含点心', 'https://images.pexels.com/photos/5946748/pexels-photo-5946748.jpeg', 15, 38, 1, '2025-05-12 23:54:56', 22800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2443, 32, '古法制茶工坊', 22800, '亲手体验茶叶揉捻与烘焙，制作属于自己的茶叶', 'https://images.unsplash.com/photo-1563822249366-3e63e990468d', 8, 26, 1, '2025-05-12 23:54:56', 28800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2444, 32, '茶文化讲座', 8800, '中国茶文化历史讲解，含5款代表性茶叶品鉴', 'https://images.pexels.com/photos/230477/pexels-photo-230477.jpeg', 20, 45, 1, '2025-05-12 23:54:56', 12800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2445, 32, '茶宴体验', 32800, '融合茶艺与美食的创新体验，8道茶元素菜品', 'https://images.unsplash.com/photo-1581618285131-f81e9832df6a', 6, 18, 1, '2025-05-12 23:54:56', 39800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2446, 33, '生鲜速递套餐', 4800, '2小时送达，含时令蔬果+肉类+海鲜', 'https://images.pexels.com/photos/4391470/pexels-photo-4391470.jpeg', 50, 235, 1, '2025-05-12 23:54:56', 5800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2447, 33, '运动补给专送', 3800, '健身后黄金恢复期补给品，含蛋白奶昔+能量棒', 'https://images.unsplash.com/photo-1578350691266-a2f415cb80e3', 60, 187, 1, '2025-05-12 23:54:56', 4800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2448, 33, '办公零食箱', 9800, '办公室团队共享装，20种健康零食组合', 'https://images.pexels.com/photos/5677401/pexels-photo-5677401.jpeg', 30, 78, 1, '2025-05-12 23:54:56', 12800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2449, 33, '深夜宵夜速递', 5800, '凌晨配送服务，热门餐厅夜宵精选，含热饮', 'https://images.unsplash.com/photo-1533777857889-4be7c70b33f7', 40, 156, 1, '2025-05-12 23:54:56', 6800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2450, 33, '家庭药品急送', 6800, '常见家庭用药组合，30分钟内送达，含用药指导', 'https://images.pexels.com/photos/3683098/pexels-photo-3683098.jpeg', 25, 68, 1, '2025-05-12 23:54:56', 7800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2451, 34, '精致寿司拼盘', 16800, '18枚创意寿司拼盘，含各类新鲜刺身', 'https://images.unsplash.com/photo-1553621042-f6e147245754', 35, 165, 1, '2025-05-12 23:54:56', 19800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2452, 34, '豪华刺身船', 26800, '5种顶级刺身大份量拼盘，2-3人分享', 'https://images.pexels.com/photos/2098085/pexels-photo-2098085.jpeg', 20, 78, 1, '2025-05-12 23:54:56', 32800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2453, 34, '日式火锅套餐', 22800, '特调昆布汤底+和牛薄片+海鲜拼盘+蔬菜', 'https://images.unsplash.com/photo-1511344407683-b1172dce025f', 25, 96, 1, '2025-05-12 23:54:56', 28800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2454, 34, '天妇罗套餐', 18800, '多种新鲜食材的天妇罗炸物，配特制酱汁', 'https://images.pexels.com/photos/884596/pexels-photo-884596.jpeg', 30, 124, 1, '2025-05-12 23:54:56', 22800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2455, 34, '铁板烧体验', 35800, '厨师现场烹饪表演，顶级和牛与海鲜组合', 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba', 15, 45, 1, '2025-05-12 23:54:56', 42800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2456, 35, '背包客床位', 5800, '舒适单人床位，含公共浴室+储物柜+早餐', 'https://images.pexels.com/photos/2869215/pexels-photo-2869215.jpeg', 40, 286, 1, '2025-05-12 23:54:56', 6800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2457, 35, '独立双人间', 15800, '小巧温馨双人房，带独立卫浴设施', 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6', 20, 124, 1, '2025-05-12 23:54:56', 18800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2458, 35, '旅行者社交活动', 3800, '每晚举办的文化交流活动，含饮品和小食', 'https://images.pexels.com/photos/5778899/pexels-photo-5778899.jpeg', 30, 156, 1, '2025-05-12 23:54:56', 4800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2459, 35, '本地导游服务', 8800, '当地达人带队的小众景点半日游，含交通', 'https://images.unsplash.com/photo-1504150558240-0b4fd8946624', 15, 68, 1, '2025-05-12 23:54:56', 10800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2460, 35, '屋顶观星夜', 4800, '屋顶天台观星活动，提供毯子和热巧克力', 'https://images.pexels.com/photos/1539225/pexels-photo-1539225.jpeg', 25, 96, 1, '2025-05-12 23:54:56', 5800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2461, 36, '园区全票', 8000, '含世界各地特色园区+观光车+植物馆门票', 'https://images.unsplash.com/photo-1585320806297-9794b3e4eeae', 100, 578, 1, '2025-05-12 23:54:56', 10000, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2462, 36, '花艺工作坊', 15800, '专业花艺师指导的插花艺术课程，含花材', 'https://images.pexels.com/photos/4926637/pexels-photo-4926637.jpeg', 15, 42, 1, '2025-05-12 23:54:56', 18800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2463, 36, '夜游花园套票', 12000, '夜间特别开放的灯光主题园区体验', 'https://images.unsplash.com/photo-1468476396571-4d6f2a427ee7', 50, 165, 1, '2025-05-12 23:54:56', 15000, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2464, 36, '季节限定花展', 9800, '春季樱花/秋季菊花等季节性特展门票', 'https://images.pexels.com/photos/757889/pexels-photo-757889.jpeg', 80, 345, 1, '2025-05-12 23:54:56', 12000, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2465, 36, '植物认养计划', 28800, '认养园区名贵植物一年，含定期照片和生长报告', 'https://images.unsplash.com/photo-1532699762751-43192b48a915', 20, 15, 1, '2025-05-12 23:54:56', 35000, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2466, 37, '智能家居套装', 358000, '全屋智能系统，含灯光+温控+安防+语音控制', 'https://images.pexels.com/photos/1029757/pexels-photo-1029757.jpeg', 10, 15, 1, '2025-05-12 23:54:56', 458000, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2467, 37, 'AR家居设计', 18800, '使用AR技术进行家居布置模拟与规划', 'https://images.unsplash.com/photo-1559028012-481c04fa702d', 30, 56, 1, '2025-05-12 23:54:56', 25800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2468, 37, '智能健康监测系统', 88800, '家庭健康数据实时监测与分析系统', 'https://images.pexels.com/photos/4792729/pexels-photo-4792729.jpeg', 15, 28, 1, '2025-05-12 23:54:56', 108800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2469, 37, '智能语音管家', 12800, '高级AI语音助手，可控制家居设备并提供生活建议', 'https://images.unsplash.com/photo-1589903308904-1010c2294adc', 40, 78, 1, '2025-05-12 23:54:56', 15800, '2025-05-12 23:54:56');
INSERT INTO `tb_goods` VALUES (2470, 37, '智能厨房系统', 68800, '厨房自动化系统，含智能冰箱、烹饪助手和营养分析', 'https://images.pexels.com/photos/6489663/pexels-photo-6489663.jpeg', 5, 12, 1, '2025-05-12 23:54:56', 88800, '2025-05-12 23:54:56');

-- ----------------------------
-- Table structure for tb_goods_sku
-- ----------------------------
DROP TABLE IF EXISTS `tb_goods_sku`;
CREATE TABLE `tb_goods_sku`  (
  `id` bigint(0) UNSIGNED NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint(0) UNSIGNED NOT NULL COMMENT '商品ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'SKU名称',
  `price` bigint(0) UNSIGNED NOT NULL COMMENT '价格，单位是分',
  `stock` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '库存',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `sold` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id`) USING BTREE,
  CONSTRAINT `fk_goods_sku` FOREIGN KEY (`goods_id`) REFERENCES `tb_goods` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_goods_sku
-- ----------------------------
INSERT INTO `tb_goods_sku` VALUES (101, 1, '单人餐', 6800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (102, 1, '双人餐', 12800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (103, 1, '家庭餐', 18800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (104, 2, '标准份', 9800, 25, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (105, 2, '大份', 14800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (106, 2, '精品套餐', 19800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (107, 3, '小碗', 3800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (108, 3, '大碗', 5800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (109, 3, '精品装', 8800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110, 4, '标准份', 8800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (111, 4, '精品套餐', 15800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (112, 4, '宴会装', 25800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (113, 5, '4只装', 2800, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (114, 5, '8只装', 4800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (115, 5, '12只装', 6800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (116, 6, '标准碗', 2200, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (117, 6, '加量版', 3200, 25, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (118, 6, '全家福', 4200, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (119, 7, '6个装', 1800, 80, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120, 7, '12个装', 3200, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (121, 7, '24个装', 5800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (122, 8, '6枚装', 4800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (123, 8, '12枚装', 8800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (124, 8, '豪华礼盒', 12800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (125, 9, '半只', 5800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (126, 9, '整只', 10800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (127, 9, '礼品装', 15800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (128, 10, '4片装', 2800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (129, 10, '8片装', 4800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (130, 10, '礼盒装', 6800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20101, 201, '无早', 39900, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20102, 201, '含双早', 45900, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20103, 201, '行政礼遇', 59900, 1, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20201, 202, '基础设备', 188800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20202, 202, '含投影仪', 218800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20301, 203, '周五入住', 68800, 4, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20302, 203, '周六入住', 78800, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20401, 204, '大床房', 599000, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20402, 204, '双床房', 629000, 1, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20501, 205, '经济型轿车', 12800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20502, 205, '商务车', 19800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20601, 206, '个人卡', 38800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (20602, 206, '双人卡', 68800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30101, 301, '工作日', 19800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30102, 301, '周末', 25800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30201, 302, '普通包', 38800, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30202, 302, 'VIP包', 48800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30301, 303, '基础款', 58800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30302, 303, '豪华款', 78800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30401, 304, '威士忌套餐', 28800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30402, 304, '白兰地套餐', 32800, 4, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30501, 305, '平日场', 9800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30502, 305, '周末场', 12800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30601, 306, '基础款', 12800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30602, 306, '浪漫款', 16800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30701, 307, '20人套餐', 88800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30702, 307, '30人套餐', 108800, 1, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30801, 308, '2人套餐', 12800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (30802, 308, '4人套餐', 18800, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40101, 401, '单份', 2800, 150, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40102, 401, '双份装', 4800, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40201, 402, '微辣', 3200, 120, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40202, 402, '中辣', 3200, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40301, 403, 'A套餐', 2200, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40302, 403, 'B套餐', 2200, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40401, 404, '微辣', 5800, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40402, 404, '重辣', 5800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40501, 405, '凯撒酱', 3800, 80, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40502, 405, '油醋汁', 3800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40601, 406, '中式套餐', 8800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40602, 406, '混搭套餐', 9800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40701, 407, '西式', 1800, 70, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40702, 407, '中式', 1800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40801, 408, '水果蛋糕', 3200, 35, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (40802, 408, '巧克力蛋糕', 3500, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50101, 501, '单颗装', 6800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50102, 501, '双人套餐', 12800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50103, 501, '四喜礼盒', 22800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50201, 502, '小份', 15800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50202, 502, '中份', 22800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50203, 502, '宴席装', 35800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50301, 503, '4片装', 2800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50302, 503, '8片礼盒', 4800, 18, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50303, 503, '16片豪华装', 8800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50401, 504, '半条', 18800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50402, 504, '整条', 32800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50501, 505, '小锅', 7800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50502, 505, '中锅', 11800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50503, 505, '家庭锅', 16800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50601, 506, '半只', 9800, 18, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50602, 506, '整只', 16800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (50603, 506, '礼盒装', 22800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60101, 601, '标准份', 3200, 80, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60102, 601, '加量版', 4200, 35, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60103, 601, '套餐（含汤）', 4800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60201, 602, '微辣', 3500, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60202, 602, '中辣', 3500, 35, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60203, 602, '特辣', 3800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60301, 603, '标准份', 2800, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60302, 603, '加鸡腿', 3800, 45, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60303, 603, '儿童餐', 2200, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60401, 604, '标准份', 2500, 120, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60402, 604, '加蛋版', 3000, 55, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60403, 604, '大胃王套餐', 4500, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60501, 605, '标准份', 4500, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60502, 605, '双拼（牛+鸡）', 5800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60503, 605, '豪华套餐', 6800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60601, 606, '标准份', 2200, 150, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60602, 606, '加菌菇', 3000, 45, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (60603, 606, '全素套餐', 3800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70101, 701, '园景房', 89900, 4, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70102, 701, '湖景房', 129900, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70201, 702, '中式早餐', 48800, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70202, 702, '西式早餐', 52800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70301, 703, '春季档', 888800, 1, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70302, 703, '秋季档', 988800, 1, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70401, 704, '基础体验', 18800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70402, 704, '含伴手礼', 25800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70501, 705, '平日价', 158800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70502, 705, '周末价', 178800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70601, 706, '单人套餐', 58800, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (70602, 706, '双人套餐', 98800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80101, 801, '白天场', 9800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80102, 801, '午夜场', 12800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80201, 802, '大床房', 199000, 4, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80202, 802, '双床房', 219000, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80301, 803, '5座轿车', 19800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80302, 803, '7座商务', 25800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80401, 804, '平日价', 15800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80402, 804, '周末价', 17800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80501, 805, '大床房', 12800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80502, 805, '双床房', 14800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80601, 806, '小件行李', 1800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (80602, 806, '大件行李', 2800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90101, 901, '普通场', 9800, 35, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90102, 901, '黄金场', 11800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90201, 902, '日场', 25800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90202, 902, '夜场', 28800, 4, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90301, 903, '单儿童', 6800, 25, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90302, 903, '1大1小', 9800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90401, 904, '普通票', 8800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90402, 904, '尖叫套餐', 12800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90501, 905, '动画主题', 58800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90502, 905, '漫威主题', 68800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90601, 906, '标准票', 15800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90602, 906, 'VIP票', 21800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90701, 907, '日间场', 38800, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90702, 907, '通宵场', 42800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90801, 908, '单人票', 4800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (90802, 908, '夫妻票', 7800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100101, 1001, '普通座', 12800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100102, 1001, 'VIP座', 16800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100201, 1002, '单人体验', 8800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100202, 1002, '双人体验', 15800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100301, 1003, '普通票', 15800, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100302, 1003, '前排票', 19800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100401, 1004, '2人席', 22800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100402, 1004, '4人席', 38800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100501, 1005, '上午场', 6800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100502, 1005, '下午场', 7800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100601, 1006, '基础课', 12800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100602, 1006, '精品课', 16800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100701, 1007, '春季节气', 38800, 4, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100702, 1007, '秋季节气', 42800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100801, 1008, '普通位', 5800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (100802, 1008, '窗景位', 7800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110101, 1101, '普通装', 3800, 90, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110102, 1101, '精品装', 4800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110201, 1102, '家常款', 2800, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110202, 1102, '川湘款', 3200, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110301, 1103, '2人份', 5800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110302, 1103, '4人份', 9800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110401, 1104, '基础款', 1980, 150, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110402, 1104, '豪华款', 2580, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110501, 1105, '原味', 4200, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110502, 1105, '黑椒味', 4200, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110601, 1106, '清汤款', 8800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110602, 1106, '麻辣款', 9800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110701, 1107, '亚洲风味', 6800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110702, 1107, '欧美风味', 7200, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110801, 1108, '基础款', 2500, 25, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (110802, 1108, '家庭款', 4500, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120101, 1201, '标准份', 3800, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120102, 1201, '加量版', 4800, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120201, 1202, '半只鸭', 5800, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120202, 1202, '整只鸭', 8800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120301, 1203, '微辣', 2800, 150, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120302, 1203, '中辣', 2800, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120401, 1204, 'A套餐', 2500, 80, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120402, 1204, 'B套餐', 2500, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120501, 1205, '清淡款', 3200, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120502, 1205, '滋补款', 3800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120601, 1206, '炒饭', 1800, 120, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120602, 1206, '意面', 2000, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120701, 1207, '200g装', 3500, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120702, 1207, '500g装', 6800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120801, 1208, '混搭装', 2800, 80, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (120802, 1208, '单一口味', 2500, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (130101, 1301, '成人票', 8000, 351, '2025-05-12 23:28:15', '2025-05-13 16:57:08', 17);
INSERT INTO `tb_goods_sku` VALUES (130102, 1301, '儿童票', 5000, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (130103, 1301, 'VIP包厢', 15000, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (130201, 1302, '普通座', 12000, 150, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (130202, 1302, '露台座', 18000, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (130301, 1303, '单人套票', 18000, 200, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (130302, 1303, '家庭套票', 40000, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (140101, 1401, '成人票', 4500, 700, '2025-05-12 23:28:15', '2025-05-13 16:58:01', 2);
INSERT INTO `tb_goods_sku` VALUES (140102, 1401, '优惠票', 2500, 200, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (140103, 1401, '香客年卡', 20000, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (140201, 1402, '上午场', 8800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (140202, 1402, '下午场', 8800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (140301, 1403, '中文讲解', 6800, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (140302, 1403, '英文讲解', 9800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150101, 1501, '男士剪发', 15800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150102, 1501, '女士剪发', 19800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150201, 1502, '短发', 58800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150202, 1502, '中长发', 68800, 7, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150301, 1503, '单次体验', 32800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150302, 1503, '3次卡', 88800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150401, 1504, '简约款', 128800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150402, 1504, '豪华款', 158800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150501, 1505, '基础修剪', 12800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (150502, 1505, '全套服务', 18800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160101, 1601, '经典泰式', 32800, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160102, 1601, '皇家泰式', 42800, 4, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160201, 1602, '60分钟', 38800, 6, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160202, 1602, '90分钟', 48800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160301, 1603, '局部调理', 28800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160302, 1603, '全身调理', 38800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160401, 1604, '月光套餐', 98800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160402, 1604, '星空套餐', 118800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160501, 1605, '孕中期', 35800, 4, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (160502, 1605, '孕晚期', 38800, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170101, 1701, '护肤套装', 58800, 80, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170102, 1701, '彩妆套装', 65800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170201, 1702, '平日券', 19800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170202, 1702, '周末券', 22800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170301, 1703, '项链类', 8800, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170302, 1703, '戒指类', 6800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170401, 1704, '1大1小', 28800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170402, 1704, '2大1小', 38800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170501, 1705, '手机类', 10000, 150, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170502, 1705, '电脑类', 15000, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170601, 1706, '鞋类专用', 38800, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170602, 1706, '服装专用', 28800, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170701, 1707, '电子卡', 50000, 200, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170702, 1707, '实体卡', 50000, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170801, 1708, '平日券', 8800, 70, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (170802, 1708, '周末券', 10800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180101, 1801, '小件护理', 128800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180102, 1801, '大件护理', 158800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180201, 1802, '午餐券', 38800, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180202, 1802, '晚餐券', 48800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180301, 1803, '单人票', 28800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180302, 1803, '情侣票', 48800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180401, 1804, '护肤套装', 18800, 100, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180402, 1804, '彩妆套装', 15800, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180501, 1805, '女装专用', 10000, 150, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180502, 1805, '男装专用', 10000, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180601, 1806, '亚洲风味', 28800, 80, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180602, 1806, '欧美风味', 32800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180701, 1807, '普通会员', 18800, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180702, 1807, '金卡会员', 0, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180801, 1808, '单日卡', 38800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (180802, 1808, '月卡', 88800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (190101, 1901, '普通时段', 12800, 70, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (190102, 1901, '全时段', 15800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (190201, 1902, '基础课', 9800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (190202, 1902, '专项课', 11800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (190301, 1903, '基础版', 88800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (190302, 1903, 'VIP版', 108800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (190401, 1904, '基础班', 6800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (190402, 1904, '进阶班', 8800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (200101, 2001, '单泳池', 15800, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (200102, 2001, '全设施', 18800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (200201, 2002, '周末班', 12800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (200202, 2002, '平日班', 10800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (200301, 2003, '5次卡', 19800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (200302, 2003, '10次卡', 35800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (200401, 2004, '基础班', 28800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (200402, 2004, '提高班', 38800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210101, 2101, '普通衣物', 5800, 80, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210102, 2101, '奢侈品牌', 8800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210201, 2102, '普通衣物', 18800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210202, 2102, '含奢侈品', 25800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210301, 2103, '普通鞋', 3800, 60, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210302, 2103, '奢侈品牌', 6800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210401, 2104, '单件', 9800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210402, 2104, '3件套', 22800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210501, 2105, '窗帘', 15800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (210502, 2105, '地毯', 19800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220101, 2201, '80㎡以下', 16800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220102, 2201, '80-120㎡', 21800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220201, 2202, '基础清洁', 58800, 25, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220202, 2202, '含擦窗', 68800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220301, 2203, '单台', 12800, 35, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220302, 2203, '3台组合', 32800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220401, 2204, '白班', 380000, 8, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220402, 2204, '住家', 480000, 2, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220501, 2205, '小户型', 28800, 40, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (220502, 2205, '大户型', 38800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230101, 2301, '基础调理', 29800, 25, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230102, 2301, '深度矫正', 39800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230201, 2302, '单次', 35800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230202, 2302, '5次卡', 158000, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230301, 2303, '局部', 19800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230302, 2303, '全身', 25800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230401, 2304, '基础版', 68800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230402, 2304, '尊享版', 98800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230501, 2305, '基础理疗', 48800, 15, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (230502, 2305, '定制方案', 68800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240101, 2401, '基础洁牙', 38800, 50, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240102, 2401, '舒适洁牙', 58800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240201, 2402, '单颗', 12800, 30, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240202, 2402, '3颗套餐', 28800, 10, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240301, 2403, '初诊', 9800, 25, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240302, 2403, '含X光', 15800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240401, 2404, '单颗', 258800, 12, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240402, 2404, '3颗套装', 688800, 3, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240501, 2405, '基础美白', 18800, 20, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (240502, 2405, '尊享美白', 28800, 5, '2025-05-12 23:28:15', '2025-05-12 23:28:15', 0);
INSERT INTO `tb_goods_sku` VALUES (250101, 2501, '小杯', 3800, 70, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250102, 2501, '中杯', 4500, 30, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250103, 2501, '大杯', 5200, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250201, 2502, '标准配方', 4200, 60, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250202, 2502, '低糖版', 4500, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250203, 2502, '加冰版', 4000, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250301, 2503, '单块', 3600, 40, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250302, 2503, '双人份', 6800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250303, 2503, '家庭装', 10800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250401, 2504, '热饮', 4500, 40, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250402, 2504, '冰饮', 4500, 35, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250403, 2504, '加料版', 5500, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250501, 2505, '单人份', 3200, 30, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250502, 2505, '双人份', 6000, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (250503, 2505, '含饮料套餐', 4500, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260101, 2601, '工作日', 5800, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260102, 2601, '周末', 6800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260103, 2601, '加时版', 7800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260201, 2602, '单次体验', 15800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260202, 2602, '月卡', 58800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260203, 2602, '季卡', 168000, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260301, 2603, '单次', 4800, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260302, 2603, '10次卡', 38800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260303, 2603, '含餐版', 6800, 8, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260401, 2604, '单次体验', 6800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260402, 2604, '月卡', 25800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260403, 2604, '含绘本礼包', 8800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260501, 2605, '普通席', 12800, 8, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260502, 2605, 'VIP席', 19800, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (260503, 2605, '含签名书', 15800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270101, 2701, '普通座', 12800, 45, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270102, 2701, 'VIP座', 18800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270103, 2701, '家庭套票', 39800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270201, 2702, '常规影片', 22800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270202, 2702, '3D影片', 26800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270203, 2702, '限量主题版', 32800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270301, 2703, '单人票', 8800, 30, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270302, 2703, '双人票', 15800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270303, 2703, '套票5场', 38800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270401, 2704, '亲子票（1大1小）', 9800, 25, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270402, 2704, '家庭票（2大1小）', 15800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270403, 2704, '含玩具礼包', 12800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270501, 2705, '单人票', 7800, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270502, 2705, '双人票', 14500, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (270503, 2705, '含讲座版', 10800, 8, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280101, 2801, '2-3人餐', 12800, 40, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280102, 2801, '4-6人餐', 22800, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280103, 2801, '麻辣鸳鸯锅', 14800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280201, 2802, '小锅', 16800, 30, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280202, 2802, '大锅', 25800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280203, 2802, '豪华套餐', 32800, 8, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280301, 2803, '2人套餐', 9800, 40, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280302, 2803, '4人套餐', 18800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280303, 2803, '家庭套餐', 28800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280401, 2804, '标准2人份', 19800, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280402, 2804, '豪华4人份', 36800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280403, 2804, '帝王蟹特餐', 59800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280501, 2805, '标准套餐', 13800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280502, 2805, '豪华版', 19800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (280503, 2805, '周年纪念版', 26800, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290101, 2901, '单次评估', 18800, 25, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290102, 2901, '含训练计划', 28800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290103, 2901, '季度追踪版', 58800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290201, 2902, '单次', 6800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290202, 2902, '10次套餐', 58800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290203, 2902, '情侣套餐', 12800, 8, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290301, 2903, '肌肉拉伤', 28800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290302, 2903, '关节损伤', 32800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290303, 2903, '专业运动员版', 45800, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290401, 2904, '基础课程', 9800, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290402, 2904, '进阶课程', 12800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290403, 2904, '私教1对1', 18800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290501, 2905, '孕期版', 15800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290502, 2905, '产后版', 16800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (290503, 2905, '全程跟踪版', 38800, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300101, 3001, '平日价', 56800, 7, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300102, 3001, '周末价', 66800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300103, 3001, '节假日价', 76800, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300201, 3002, '玫瑰主题', 88800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300202, 3002, '星空主题', 98800, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300203, 3002, '海洋主题', 95800, 1, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300301, 3003, '标准房', 46800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300302, 3003, '高级房', 56800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (300303, 3003, '含会议室使用', 65800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310101, 3101, '小型犬', 29800, 15, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310102, 3101, '大型犬', 39800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310103, 3101, '猫咪体检', 32800, 8, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310201, 3102, '短毛猫', 15800, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310202, 3102, '长毛猫', 19800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310203, 3102, '含造型', 22800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310301, 3103, '单次护理', 18800, 20, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310302, 3103, '套餐3次', 48800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310303, 3103, '含麻醉治疗', 28800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310401, 3104, '小型犬', 38800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310402, 3104, '中型犬', 45800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310403, 3104, '母猫绝育', 35800, 8, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310501, 3105, '基础服从训练', 25800, 8, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310502, 3105, '行为纠正', 32800, 4, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (310503, 3105, '高级技能训练', 39800, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370101, 3701, '基础版', 358000, 7, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370102, 3701, '豪华版', 458000, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370103, 3701, '定制版', 558000, 1, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370201, 3702, '单次设计', 18800, 25, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370202, 3702, '含实施', 38800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370203, 3702, 'VR展示版', 26800, 3, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370301, 3703, '基础套装', 88800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370302, 3703, '专业套装', 128800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370303, 3703, '全家版', 168800, 2, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370401, 3704, '单设备版', 12800, 30, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370402, 3704, '多房间版', 28800, 10, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370403, 3704, '全屋互联版', 38800, 5, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370501, 3705, '基础套装', 68800, 4, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370502, 3705, '高配版', 98800, 1, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);
INSERT INTO `tb_goods_sku` VALUES (370503, 3705, '定制版', 128800, 1, '2025-05-12 23:59:21', '2025-05-12 23:59:21', 0);

-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order`  (
  `shop_image` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '店铺图片',
  `count` int(0) NOT NULL COMMENT '总数',
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '下单的用户id',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺id',
  `shop_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `amount` bigint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
  `pay_type` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '取消原因',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2,4,5：已支付；3：已取消；4：待收货；5：已完成',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '下单时间',
  `pay_time` timestamp(0) NULL DEFAULT NULL COMMENT '支付时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `address_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '使用的地址ID',
  `address_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人',
  `address_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `address_detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '详细地址',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `commented` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已评价：0-未评价 1-已评价',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_order
-- ----------------------------

-- ----------------------------
-- Table structure for tb_order_items
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_items`;
CREATE TABLE `tb_order_items`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(0) UNSIGNED NOT NULL COMMENT '订单ID(外键)',
  `goods_id` bigint(0) UNSIGNED NOT NULL COMMENT '商品ID(仅记录不关联)',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `count` int(0) UNSIGNED NOT NULL COMMENT '购买数量',
  `price` bigint(0) UNSIGNED NOT NULL COMMENT '单价(单位:分)',
  `goods_image` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品图片，多个以逗号分隔',
  `sku_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT 'SKU ID',
  `sku_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'SKU名称',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `tb_order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单商品明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_order_items
-- ----------------------------

-- ----------------------------
-- Table structure for tb_seckill_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_voucher`;
CREATE TABLE `tb_seckill_voucher`  (
  `voucher_id` bigint(0) UNSIGNED NOT NULL COMMENT '关联的优惠券的id',
  `stock` int(0) NOT NULL COMMENT '库存',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `begin_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '生效时间',
  `end_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '失效时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '秒杀优惠券表，与优惠券是一对一关系' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_seckill_voucher
-- ----------------------------
INSERT INTO `tb_seckill_voucher` VALUES (11, 92, '2025-03-19 19:53:26', '2025-03-19 19:53:26', '2025-03-28 20:53:26', '2025-03-22 17:15:52');

-- ----------------------------
-- Table structure for tb_shop
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop`;
CREATE TABLE `tb_shop`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商铺名称',
  `type_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺类型的id',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商铺图片，多个图片以\',\'隔开',
  `area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商圈，例如陆家嘴',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址',
  `x` double UNSIGNED NOT NULL COMMENT '经度',
  `y` double UNSIGNED NOT NULL COMMENT '维度',
  `avg_price` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '均价，取整数',
  `sold` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '销量',
  `comments` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '评论数量',
  `score` int(2) UNSIGNED ZEROFILL NOT NULL COMMENT '评分，1~5分，乘10保存，避免小数',
  `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间，例如 10:00-22:00',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `foreign_key_type`(`type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_shop
-- ----------------------------
INSERT INTO `tb_shop` VALUES (1, '品味轩餐厅', 1, 'https://images.unsplash.com/photo-1550547660-d9450f859349', '朝阳区', '北京市朝阳区建国路87号SKP商场B1层', 116.480356, 39.913486, 88, 0000001024, 0000000968, 48, '10:00-22:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (2, '舒适酒店', 2, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791', '福田区', '深圳市福田区福华路28号', 114.067543, 22.538765, 299, 0000000802, 0000000756, 47, '14:00-12:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (3, '欢乐KTV', 3, 'https://images.pexels.com/photos/274192/pexels-photo-274192.jpeg', '玄武区', '南京市玄武区中山路18号德基广场8楼', 118.787654, 32.047654, 198, 0000001256, 0000001180, 46, '12:00-02:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (4, '快乐外卖', 4, 'https://images.unsplash.com/photo-1585032226651-759b368d7246', '河西区', '天津市河西区友谊路32号', 117.217654, 39.097654, 45, 0000002048, 0000001890, 49, '10:00-22:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (5, '江南小厨', 1, 'https://images.pexels.com/photos/5409010/pexels-photo-5409010.jpeg', '黄浦区', '上海市黄浦区南京东路228号新世界城6楼', 121.478765, 31.237654, 65, 0000001658, 0000001520, 46, '10:30-21:30', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (6, '老板木桶饭', 1, 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b', '天河区', '广州市天河区体育西路77号', 113.327654, 23.136789, 35, 0000002356, 0000002127, 49, '10:00-21:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (7, '西湖风景酒店', 2, 'https://images.pexels.com/photos/258154/pexels-photo-258154.jpeg', '锦江区', '成都市锦江区红星路三段16号', 104.083456, 30.657654, 499, 0000000532, 0000000487, 49, '全天营业', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (8, '商务快捷酒店', 2, 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af', '武昌区', '武汉市武昌区中南路14号', 114.337654, 30.547654, 168, 0000000968, 0000000912, 45, '全天营业', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (9, '星空电影院', 3, 'https://images.unsplash.com/photo-1478720568477-152d9b164e26', '碑林区', '西安市碑林区南关正街88号', 108.937654, 34.247654, 45, 0000002632, 0000002510, 47, '10:00-24:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (10, '音乐茶馆', 3, 'https://images.pexels.com/photos/164758/pexels-photo-164758.jpeg', '渝中区', '重庆市渝中区解放碑民权路89号', 106.577654, 29.557654, 128, 0000000875, 0000000820, 48, '13:00-01:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (11, '鲜食快递', 4, 'https://images.pexels.com/photos/4553111/pexels-photo-4553111.jpeg', '芙蓉区', '长沙市芙蓉区黄兴中路88号', 113.007654, 28.197654, 28, 0000003256, 0000003012, 47, '10:00-22:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (12, '家常菜外送', 4, 'https://images.unsplash.com/photo-1585937421612-70a008356fbe', '鼓楼区', '福州市鼓楼区五四路128号', 119.307654, 26.097654, 38, 0000002148, 0000002023, 46, '09:30-22:30', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (13, '西湖游船', 5, 'https://images.pexels.com/photos/6276788/pexels-photo-6276788.jpeg', '西湖区', '杭州市西湖区湖滨路1号', 120.167654, 30.257654, 80, 0000005637, 0000005218, 49, '08:00-17:30', '2025-05-12 23:54:36', '2025-05-13 00:12:18');
INSERT INTO `tb_shop` VALUES (14, '灵隐寺', 5, 'https://images.unsplash.com/photo-1609142621730-db3293839541', '西湖区', '杭州市西湖区法云弄1号', 120.097654, 30.247654, 45, 0000009874, 0000009246, 48, '07:00-18:00', '2025-05-12 23:54:36', '2025-05-13 16:57:54');
INSERT INTO `tb_shop` VALUES (15, '时尚美发沙龙', 6, 'https://images.pexels.com/photos/3992875/pexels-photo-3992875.jpeg', '天河区', '广州市天河区天河路208号', 113.327654, 23.137654, 198, 0000001356, 0000001267, 47, '10:00-22:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (16, '东方SPA会所', 6, 'https://images.unsplash.com/photo-1600335895229-6e75511892c8', '静安区', '上海市静安区南京西路1266号', 121.467654, 31.237654, 328, 0000000868, 0000000817, 49, '10:00-23:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (17, '西湖银泰', 7, 'https://images.pexels.com/photos/273209/pexels-photo-273209.jpeg', '西湖区', '杭州市西湖区延安路98号', 120.167654, 30.257654, 500, 0000012568, 0000011824, 48, '10:00-22:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (18, '杭州万象城', 7, 'https://images.unsplash.com/photo-1521782462922-9318be1cfd04', '江干区', '杭州市江干区富春路701号', 120.217654, 30.257654, 600, 0000015632, 0000014758, 47, '10:00-22:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (19, '超级健身房', 8, 'https://images.pexels.com/photos/221247/pexels-photo-221247.jpeg', '南山区', '深圳市南山区科技南路18号', 113.947654, 22.537654, 128, 0000002865, 0000002654, 46, '09:00-23:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (20, '游泳健身中心', 8, 'https://images.unsplash.com/photo-1556817411-31ae72fa3ea0', '朝阳区', '北京市朝阳区建国路93号', 116.487654, 39.917654, 158, 0000001958, 0000001823, 47, '09:00-22:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (21, '便民洗衣店', 9, 'https://images.pexels.com/photos/4488643/pexels-photo-4488643.jpeg', '海珠区', '广州市海珠区新港中路397号', 113.327654, 23.097654, 58, 0000003542, 0000003328, 48, '08:00-20:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (22, '家政服务中心', 9, 'https://images.unsplash.com/photo-1584433144859-1fc3ab64a957', '武侯区', '成都市武侯区人民南路四段8号', 104.077654, 30.637654, 168, 0000001685, 0000001562, 46, '08:30-20:30', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (23, '康复理疗中心', 10, 'https://images.pexels.com/photos/7088521/pexels-photo-7088521.jpeg', '福田区', '深圳市福田区福华一路88号', 114.057654, 22.537654, 298, 0000001452, 0000001358, 49, '09:00-21:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (24, '口腔诊所', 10, 'https://sghimages.shobserver.com/img/catch/2025/04/28/2d6a2023-983d-4aa3-9394-3e0319fd131b.jpg', '浦东新区', '上海市浦东新区陆家嘴环路1288号', 121.507654, 31.237654, 388, 0000000986, 0000000921, 47, '09:00-18:00', '2025-05-12 23:54:36', '2025-05-12 23:54:36');
INSERT INTO `tb_shop` VALUES (25, '星座咖啡馆', 1, 'https://images.pexels.com/photos/1855214/pexels-photo-1855214.jpeg', '海淀区', '北京市海淀区中关村南大街5号', 116.317654, 39.987654, 58, 0000001256, 0000001182, 47, '08:30-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (26, '城市书房', 3, 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da', '江干区', '杭州市江干区钱江新城4号大街', 120.217654, 30.267654, 35, 0000002873, 0000002765, 48, '09:00-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (27, '天空之城影院', 3, 'https://images.pexels.com/photos/7991580/pexels-photo-7991580.jpeg', '锦江区', '成都市锦江区红星路二段159号', 104.077654, 30.647654, 65, 0000003621, 0000003425, 46, '10:00-24:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (28, '觅食火锅店', 1, 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624', '南岸区', '重庆市南岸区南坪西路18号', 106.567654, 29.527654, 128, 0000002486, 0000002315, 48, '11:00-24:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (29, '梦想健身中心', 8, 'https://images.pexels.com/photos/4498482/pexels-photo-4498482.jpeg', '浦东新区', '上海市浦东新区世纪大道1号', 121.527654, 31.227654, 188, 0000001756, 0000001652, 47, '06:00-23:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (30, '极光精品酒店', 2, 'https://images.unsplash.com/photo-1566073771259-6a8506099945', '西城区', '北京市西城区西单北大街120号', 116.367654, 39.907654, 458, 0000000952, 0000000876, 49, '全天营业', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (31, '幸福宠物医院', 10, 'https://images.pexels.com/photos/6235688/pexels-photo-6235688.jpeg', '越秀区', '广州市越秀区东风东路761号', 113.287654, 23.127654, 258, 0000001325, 0000001216, 48, '08:00-20:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (32, '云端茶社', 3, 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4', '徐汇区', '上海市徐汇区襄阳南路218号', 121.447654, 31.197654, 88, 0000001862, 0000001756, 47, '10:00-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (33, '运动快递', 4, 'https://images.pexels.com/photos/4391470/pexels-photo-4391470.jpeg', '朝阳区', '北京市朝阳区望京西路48号', 116.477654, 39.997654, 42, 0000004281, 0000004125, 48, '08:00-21:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (34, '樱花日料', 1, 'https://images.unsplash.com/photo-1553621042-f6e147245754', '姑苏区', '苏州市姑苏区平江路89号', 120.627654, 31.307654, 168, 0000001526, 0000001438, 49, '11:00-21:30', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (35, '远方青年旅社', 2, 'https://images.pexels.com/photos/2869215/pexels-photo-2869215.jpeg', '西湖区', '杭州市西湖区曙光路25号', 120.137654, 30.277654, 128, 0000002362, 0000002218, 46, '全天营业', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (36, '世界花园', 5, 'https://images.unsplash.com/photo-1585320806297-9794b3e4eeae', '白云区', '广州市白云区云城东路503号', 113.267654, 23.167654, 60, 0000008634, 0000008125, 47, '08:00-18:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (37, '科技生活馆', 7, 'https://images.pexels.com/photos/1029757/pexels-photo-1029757.jpeg', '南山区', '深圳市南山区科技园路1号', 113.937654, 22.527654, 350, 0000005826, 0000005621, 48, '10:00-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (38, '亲子教育中心', 9, 'https://images.unsplash.com/photo-1566125882500-87e10f726cdc', '长宁区', '上海市长宁区长宁路1027号', 121.417654, 31.217654, 228, 0000001052, 0000000987, 49, '09:00-19:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (39, '家居智能馆', 7, 'https://images.pexels.com/photos/1668860/pexels-photo-1668860.jpeg', '海淀区', '北京市海淀区清华东路35号', 116.327654, 39.977654, 280, 0000003256, 0000003125, 47, '10:00-21:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (40, '潮流服饰店', 7, 'https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5', '江北区', '重庆市江北区观音桥步行街18号', 106.537654, 29.577654, 158, 0000004862, 0000004625, 46, '10:00-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (41, '绿野素食餐厅', 1, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd', '南山区', '深圳市南山区科技园南路12号', 113.947654, 22.517654, 89, 0000001865, 0000001723, 48, '10:00-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (42, '梦幻主题乐园', 5, 'https://images.pexels.com/photos/784919/pexels-photo-784919.jpeg', '大兴区', '北京市大兴区欢乐谷路1号', 116.487654, 39.727654, 220, 0000009658, 0000009235, 47, '09:00-21:30', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (43, '古韵茶馆', 3, 'https://images.unsplash.com/photo-1577089534741-55709ddb1521', '西湖区', '杭州市西湖区满觉陇路28号', 120.127654, 30.237654, 76, 0000002547, 0000002365, 48, '09:30-22:30', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (44, '轻奢酒店', 2, 'https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg', '江宁区', '南京市江宁区胜太路158号', 118.857654, 31.947654, 368, 0000001543, 0000001426, 47, '全天营业', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (45, '山间别墅', 2, 'https://images.unsplash.com/photo-1560448204-603b3fc33ddc', '西湖区', '杭州市西湖区桃源岭8号', 120.097654, 30.217654, 888, 0000000865, 0000000782, 49, '全天营业', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (46, '音乐爱好者', 3, 'https://images.pexels.com/photos/995301/pexels-photo-995301.jpeg', '武侯区', '成都市武侯区人民南路四段27号', 104.067654, 30.617654, 156, 0000002187, 0000002035, 48, '12:00-24:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (47, '海鲜大排档', 1, 'https://images.pexels.com/photos/725991/pexels-photo-725991.jpeg', '思明区', '厦门市思明区环岛路123号', 118.147654, 24.437654, 198, 0000003254, 0000003056, 46, '17:00-02:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (48, '专业美甲店', 6, 'https://images.unsplash.com/photo-1604654894611-6973b364bdbb', '静安区', '上海市静安区南京西路1378号', 121.457654, 31.227654, 168, 0000001756, 0000001623, 48, '10:00-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (49, '电竞馆', 3, 'https://images.pexels.com/photos/3945683/pexels-photo-3945683.jpeg', '朝阳区', '北京市朝阳区朝阳北路175号', 116.497654, 39.937654, 45, 0000004578, 0000004356, 47, '10:00-06:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (50, '私房菜餐厅', 1, 'https://images.unsplash.com/photo-1476224203421-9ac39bcb3327', '徐汇区', '上海市徐汇区宛平南路528号', 121.437654, 31.187654, 288, 0000000975, 0000000923, 49, '11:30-21:30', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (51, '精品咖啡工坊', 1, 'https://images.pexels.com/photos/302899/pexels-photo-302899.jpeg', '高新区', '西安市高新区科技路33号', 108.887654, 34.217654, 42, 0000002356, 0000002245, 48, '08:00-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (52, '亲子游泳馆', 8, 'https://images.unsplash.com/photo-1576610616656-d3aa5d1f4534', '福田区', '深圳市福田区福华一路98号', 114.047654, 22.527654, 228, 0000001458, 0000001378, 47, '09:00-21:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (53, '艺术空间', 3, 'https://images.pexels.com/photos/1674049/pexels-photo-1674049.jpeg', '锦江区', '成都市锦江区红星路三段1号', 104.087654, 30.667654, 120, 0000001875, 0000001756, 48, '10:00-22:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');
INSERT INTO `tb_shop` VALUES (54, '故宫文创店', 7, 'https://images.unsplash.com/photo-1562619371-b67725b6fde2', '东城区', '北京市东城区景山前街4号', 116.397654, 39.917654, 150, 0000005876, 0000005642, 47, '09:00-17:00', '2025-05-12 23:55:17', '2025-05-12 23:55:17');

-- ----------------------------
-- Table structure for tb_shop_favorite
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_favorite`;
CREATE TABLE `tb_shop_favorite`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '关联用户ID',
  `shop_id` bigint(0) NOT NULL COMMENT '店铺ID',
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺名称',
  `shop_images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺地址',
  `area` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在区域',
  `avg_price` bigint(0) NULL DEFAULT NULL COMMENT '人均消费价格(单位:分)',
  `sold` int(0) NULL DEFAULT 0 COMMENT '已售数量',
  `score` int(0) NULL DEFAULT NULL COMMENT '评分',
  `type_id` bigint(0) NULL DEFAULT NULL COMMENT '店铺类型ID',
  `type_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺类型名称',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_shop_favorite
-- ----------------------------
INSERT INTO `tb_shop_favorite` VALUES (4, 1020, 4, '快乐外卖', 'https://images.unsplash.com/photo-1585032226651-759b368d7246', '天津市河西区友谊路32号', '河西区', 45, 2048, 49, 4, '健身运动', '2025-05-13 17:49:36');
INSERT INTO `tb_shop_favorite` VALUES (5, 1020, 7, '西湖风景酒店', 'https://images.pexels.com/photos/258154/pexels-photo-258154.jpeg', '成都市锦江区红星路三段16号', '锦江区', 499, 532, 49, 2, '医疗健康', '2025-05-13 17:50:06');
INSERT INTO `tb_shop_favorite` VALUES (6, 1020, 6, '老板木桶饭', 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b', '广州市天河区体育西路77号', '天河区', 35, 2356, 49, 1, '美食', '2025-05-13 17:50:11');
INSERT INTO `tb_shop_favorite` VALUES (7, 1020, 37, '科技生活馆', 'https://images.pexels.com/photos/1029757/pexels-photo-1029757.jpeg', '深圳市南山区科技园路1号', '南山区', 350, 5826, 48, 7, '丽人', '2025-05-13 17:50:16');
INSERT INTO `tb_shop_favorite` VALUES (8, 1020, 17, '西湖银泰', 'https://images.pexels.com/photos/273209/pexels-photo-273209.jpeg', '杭州市西湖区延安路98号', '西湖区', 500, 12568, 48, 7, '丽人', '2025-05-13 17:50:20');
INSERT INTO `tb_shop_favorite` VALUES (11, 1020, 13, '西湖游船', 'https://images.pexels.com/photos/6276788/pexels-photo-6276788.jpeg', '杭州市西湖区湖滨路1号', '西湖区', 80, 5637, 49, 5, '购物', '2025-05-13 18:26:19');

-- ----------------------------
-- Table structure for tb_shop_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_type`;
CREATE TABLE `tb_shop_type`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '顺序',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_shop_type
-- ----------------------------
INSERT INTO `tb_shop_type` VALUES (1, '美食', 'Food', 1, '2021-12-22 20:17:47', '2025-05-06 17:04:22');
INSERT INTO `tb_shop_type` VALUES (2, '医疗健康', 'FirstAidKit', 2, '2021-12-22 20:18:27', '2025-05-06 18:00:59');
INSERT INTO `tb_shop_type` VALUES (3, '生活服务', 'Service', 3, '2021-12-22 20:18:48', '2025-05-06 18:01:07');
INSERT INTO `tb_shop_type` VALUES (4, '健身运动', 'Football', 10, '2021-12-22 20:19:04', '2025-05-06 18:01:16');
INSERT INTO `tb_shop_type` VALUES (5, '购物', 'ShoppingCart', 5, '2021-12-22 20:19:27', '2025-05-06 18:01:19');
INSERT INTO `tb_shop_type` VALUES (6, '休闲娱乐', 'VideoPlay', 6, '2021-12-22 20:19:35', '2025-05-06 18:00:31');
INSERT INTO `tb_shop_type` VALUES (7, '丽人', 'Female', 7, '2021-12-22 20:19:53', '2025-05-06 18:01:26');
INSERT INTO `tb_shop_type` VALUES (8, '景点', 'PictureFilled', 8, '2021-12-22 20:20:02', '2025-05-06 18:00:41');
INSERT INTO `tb_shop_type` VALUES (9, '外卖', 'TakeawayBox', 9, '2021-12-22 20:20:08', '2025-05-06 18:00:37');
INSERT INTO `tb_shop_type` VALUES (10, '酒店', 'House', 4, '2021-12-22 20:21:46', '2025-05-06 18:00:49');

-- ----------------------------
-- Table structure for tb_sign
-- ----------------------------
DROP TABLE IF EXISTS `tb_sign`;
CREATE TABLE `tb_sign`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户id',
  `year` year NOT NULL COMMENT '签到的年',
  `month` tinyint(0) NOT NULL COMMENT '签到的月',
  `date` date NOT NULL COMMENT '签到的日期',
  `is_backup` tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '是否补签',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_sign
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码，加密存储',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '昵称，默认是用户id',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '人物头像',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_admin` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '管理员',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniqe_key_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1020 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1018, '18338319215', '123456', 'user_XHVcCjwvbt', '/imgs/icon/2025/05/08/a1c775f9ca734487.png', '2025-05-08 14:50:48', '2025-05-08 19:36:20', '0');
INSERT INTO `tb_user` VALUES (1019, '18338319214', '123456', 'user_DV3u7IsSu3', '', '2025-05-08 14:51:54', '2025-05-08 14:51:54', '0');
INSERT INTO `tb_user` VALUES (1020, '18338319216', '123456', 'user_GlQG8AyQOn', '', '2025-05-08 14:57:33', '2025-05-09 12:32:13', '0');

-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '主键，用户id',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '城市名称',
  `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人介绍，不要超过128个字符',
  `fans` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '粉丝数量',
  `followee` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '关注的人的数量',
  `gender` tinyint(0) UNSIGNED NULL DEFAULT 0 COMMENT '性别，0：男，1：女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `credits` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '积分',
  `level` tinyint(0) UNSIGNED NULL DEFAULT 0 COMMENT '会员级别，0~9级,0代表未开通会员',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  PRIMARY KEY (`user_id`) USING BTREE,
  CONSTRAINT `user_id_1` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_user_info
-- ----------------------------
INSERT INTO `tb_user_info` VALUES (1018, '福建省 / 厦门市 / 市辖区', '', 0, 0, 1, '2025-05-20', 0, 0, '2025-05-08 19:36:20', '2025-05-08 19:36:20', '930924708@qq.com');

-- ----------------------------
-- Table structure for tb_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher`;
CREATE TABLE `tb_voucher`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '商铺id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代金券标题',
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '副标题',
  `rules` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用规则',
  `pay_value` bigint(0) UNSIGNED NOT NULL COMMENT '支付金额，单位是分。例如200代表2元',
  `actual_value` bigint(0) NOT NULL COMMENT '抵扣金额，单位是分。例如200代表2元',
  `type` tinyint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0,普通券；1,秒杀券',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1,上架; 2,下架; 3,过期',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_voucher
-- ----------------------------
INSERT INTO `tb_voucher` VALUES (1, 1, '50元代金券', '周一至周日均可使用', '全场通用\\n无需预约\\n可无限叠加\\不兑现、不找零\\n仅限堂食', 4750, 5000, 0, 1, '2022-01-04 09:42:39', '2025-03-17 21:03:38');
INSERT INTO `tb_voucher` VALUES (10, 1, '50元代金券', '周一至周日均可使用', '全场通用\\n无需预约\\n可无限叠加\\不兑现、不找零\\n仅限堂食', 4750, 5000, 0, 1, '2025-03-19 19:37:30', '2025-03-19 19:37:30');
INSERT INTO `tb_voucher` VALUES (11, 1, '50元代金券', '周一至周日均可使用', '全场通用\\n无需预约\\n可无限叠加\\不兑现、不找零\\n仅限堂食', 8000, 10000, 1, 1, '2025-03-19 19:53:26', '2025-03-19 19:53:26');

-- ----------------------------
-- Table structure for tb_voucher_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher_order`;
CREATE TABLE `tb_voucher_order`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '下单的用户id',
  `voucher_id` bigint(0) UNSIGNED NOT NULL COMMENT '购买的代金券id',
  `pay_type` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '下单时间',
  `pay_time` timestamp(0) NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp(0) NULL DEFAULT NULL COMMENT '核销时间',
  `refund_time` timestamp(0) NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_voucher_order
-- ----------------------------
INSERT INTO `tb_voucher_order` VALUES (436663105871675393, 1010, 11, 1, 1, '2025-03-22 17:15:52', NULL, NULL, NULL, '2025-03-22 17:15:52');

SET FOREIGN_KEY_CHECKS = 1;
