

-- ----------------------------
-- Table structure for tb_goods
-- ----------------------------
DROP TABLE IF EXISTS `tb_goods`;
CREATE TABLE `tb_goods` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(20) UNSIGNED NOT NULL COMMENT '商铺id',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `price` bigint(10) UNSIGNED NOT NULL COMMENT '价格，单位是分',
  `description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品描述',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品图片，多个图片以\',\'隔开',
  `stock` int(8) UNSIGNED NOT NULL DEFAULT 0 COMMENT '库存',
  `sold` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态，1：上架，2：下架',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id` (`shop_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '下单的用户id',
  `shop_id` bigint(20) UNSIGNED NOT NULL COMMENT '商铺id',
  `goods_id` bigint(20) UNSIGNED NOT NULL COMMENT '商品id',
  `goods_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `goods_price` bigint(10) UNSIGNED NOT NULL COMMENT '商品价格，单位是分',
  `amount` int(8) UNSIGNED NOT NULL DEFAULT 1 COMMENT '购买数量',
  `total_price` bigint(10) UNSIGNED NOT NULL COMMENT '总价，单位是分',
  `pay_type` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2：已支付；3：已完成；4：已取消；5：退款中；6：已退款',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE,
  INDEX `idx_shop_id` (`shop_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- 向商品表中插入预置数据
INSERT INTO `tb_goods` (`id`, `shop_id`, `name`, `price`, `description`, `images`, `stock`, `sold`, `status`, `create_time`, `update_time`) VALUES
(1, 1, '招牌烤鸭', 12800, '正宗北京烤鸭，肉质鲜嫩，色泽金黄，口感酥脆。', 'https://p0.meituan.net/biztone/163160492_1619867862517.jpeg,https://p0.meituan.net/bbia/7bfae9b0d805af7cd6eaa4d9f8c489c3333759.jpg', 100, 210, 1, NOW(), NOW()),
(2, 1, '北京烤鸭套餐', 29800, '招牌烤鸭+鸭骨汤+小菜+荷叶饼，经典搭配，回味无穷。', 'https://p0.meituan.net/bbia/0533fa096f831e3403a37d2117d234b7274527.jpg,https://qcloud.dpfile.com/pc/jiclIsCKmOI2arxJMSWLX0QY-YXkfnT3jM2LS0K-WDFfzlrGMEqgy6izFLqRMRWKTYGVDmosZWTLal1WbWRW3A.jpg', 50, 120, 1, NOW(), NOW()),
(3, 1, '鸭骨汤', 1800, '选用优质鸭骨熬制，汤色奶白，营养丰富。', 'https://qcloud.dpfile.com/pc/Rie9-39jUBwmG6xDpBTsWzF5-YQhUkCnxUMp5aK-aDwN-0QH6Z3qbQQV1ix_9Zb0TYGVDmosZWTLal1WbWRW3A.jpg', 200, 300, 1, NOW(), NOW()),
(4, 2, '招牌小笼包', 3800, '皮薄馅大，汤汁丰富，一口下去满口鲜香。', 'https://p0.meituan.net/biztone/694233_1619909157562.jpeg,https://qcloud.dpfile.com/pc/toQMJr7lhCeRaTTmJ-cQRHtS_-J-HDeNJTrTYBNsRHAGJKpO5N-C0jVUaGxBSfkgTYGVDmosZWTLal1WbWRW3A.jpg', 150, 500, 1, NOW(), NOW()),
(5, 2, '蟹黄汤包', 5800, '精选大闸蟹蟹黄制作，鲜香浓郁，回味无穷。', 'https://qcloud.dpfile.com/pc/J9PxUHZLTHacxYBZJzBHQHlYvf2n3bXvAJLdEEWZdXxLPQT-YN0JA-gyQwzBxYWwTYGVDmosZWTLal1WbWRW3A.jpg,https://qcloud.dpfile.com/pc/J9PxUHZLTHacxYBZJzBHQHlYvf2n3bXvAJLdEEWZdXxLPQT-YN0JA-gyQwzBxYWwTYGVDmosZWTLal1WbWRW3A.jpg', 80, 320, 1, NOW(), NOW()),
(6, 2, '南翔馒头', 2800, '外皮金黄酥脆，内馅鲜嫩多汁，上海经典小吃。', 'https://qcloud.dpfile.com/pc/Qe2PTT9hxilQWUQZ5bFpwH9ggYgGBfA8-S-THndxBGkFtPVVYso8S0KwMYwb2nTHTYGVDmosZWTLal1WbWRW3A.jpg', 100, 280, 1, NOW(), NOW()),
(7, 3, '招牌麻辣香锅', 6800, '麻辣鲜香，食材丰富，回味悠长。', 'https://qcloud.dpfile.com/pc/Qe2PTT9hxilQWUQZ5bFpwH9ggYgGBfA8-S-THndxBGkFtPVVYso8S0KwMYwb2nTHTYGVDmosZWTLal1WbWRW3A.jpg,https://qcloud.dpfile.com/pc/Qe2PTT9hxilQWUQZ5bFpwH9ggYgGBfA8-S-THndxBGkFtPVVYso8S0KwMYwb2nTHTYGVDmosZWTLal1WbWRW3A.jpg', 120, 450, 1, NOW(), NOW()),
(8, 3, '毛血旺', 5800, '麻辣鲜香，食材新鲜，口感丰富。', 'https://qcloud.dpfile.com/pc/J9PxUHZLTHacxYBZJzBHQHlYvf2n3bXvAJLdEEWZdXxLPQT-YN0JA-gyQwzBxYWwTYGVDmosZWTLal1WbWRW3A.jpg', 80, 320, 1, NOW(), NOW()),
(9, 3, '水煮牛肉', 6800, '鲜嫩牛肉，麻辣鲜香，辣而不燥。', 'https://qcloud.dpfile.com/pc/toQMJr7lhCeRaTTmJ-cQRHtS_-J-HDeNJTrTYBNsRHAGJKpO5N-C0jVUaGxBSfkgTYGVDmosZWTLal1WbWRW3A.jpg', 90, 380, 1, NOW(), NOW()),
(10, 4, '招牌奶茶', 1800, '精选锡兰红茶，搭配进口奶源，香浓顺滑。', 'https://qcloud.dpfile.com/pc/J9PxUHZLTHacxYBZJzBHQHlYvf2n3bXvAJLdEEWZdXxLPQT-YN0JA-gyQwzBxYWwTYGVDmosZWTLal1WbWRW3A.jpg', 200, 800, 1, NOW(), NOW()),
(11, 4, '珍珠奶茶', 2000, '经典珍珠奶茶，珍珠Q弹有嚼劲，奶茶香浓。', 'https://qcloud.dpfile.com/pc/toQMJr7lhCeRaTTmJ-cQRHtS_-J-HDeNJTrTYBNsRHAGJKpO5N-C0jVUaGxBSfkgTYGVDmosZWTLal1WbWRW3A.jpg', 180, 750, 1, NOW(), NOW()),
(12, 4, '芝士茉莉茶', 2200, '茉莉花茶搭配芝士奶盖，清香与浓郁的完美结合。', 'https://qcloud.dpfile.com/pc/Qe2PTT9hxilQWUQZ5bFpwH9ggYgGBfA8-S-THndxBGkFtPVVYso8S0KwMYwb2nTHTYGVDmosZWTLal1WbWRW3A.jpg', 150, 600, 1, NOW(), NOW()),
(13, 5, '招牌牛肉面', 2800, '精选牛腱肉，面条劲道，汤底浓郁。', 'https://qcloud.dpfile.com/pc/J9PxUHZLTHacxYBZJzBHQHlYvf2n3bXvAJLdEEWZdXxLPQT-YN0JA-gyQwzBxYWwTYGVDmosZWTLal1WbWRW3A.jpg', 100, 450, 1, NOW(), NOW()),
(14, 5, '红烧牛肉面', 3200, '红烧牛肉，入味多汁，搭配劲道面条。', 'https://qcloud.dpfile.com/pc/toQMJr7lhCeRaTTmJ-cQRHtS_-J-HDeNJTrTYBNsRHAGJKpO5N-C0jVUaGxBSfkgTYGVDmosZWTLal1WbWRW3A.jpg', 90, 400, 1, NOW(), NOW()),
(15, 5, '清汤牛肉面', 2600, '清淡鲜香，牛肉鲜嫩，面条爽滑。', 'https://qcloud.dpfile.com/pc/Qe2PTT9hxilQWUQZ5bFpwH9ggYgGBfA8-S-THndxBGkFtPVVYso8S0KwMYwb2nTHTYGVDmosZWTLal1WbWRW3A.jpg', 110, 420, 1, NOW(), NOW());