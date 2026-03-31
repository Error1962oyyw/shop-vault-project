-- =====================================================
-- Shop Vault 初始化数据
-- =====================================================

-- 1. 初始化商品分类数据
-- 基于 COCO 数据集的 YOLO 商品识别分类

-- 插入一级分类（大类）
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`) VALUES
(1, '超市/食品区', 0, 1, 'ShoppingCart', 1, 1),
(2, '家居百货/日用品', 0, 1, 'House', 2, 1),
(3, '服饰箱包', 0, 1, 'ShoppingBag', 3, 1),
(4, '数码电器', 0, 1, 'Monitor', 4, 1),
(5, '运动户外', 0, 1, 'Football', 5, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `sort` = VALUES(`sort`);

-- 插入二级分类（小类）- 超市/食品区 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(101, '香蕉', 1, 2, NULL, 1, 1, 'banana', 46),
(102, '苹果', 1, 2, NULL, 2, 1, 'apple', 47),
(103, '橘子/橙子', 1, 2, NULL, 3, 1, 'orange', 49),
(104, '西兰花', 1, 2, NULL, 4, 1, 'broccoli', 50),
(105, '胡萝卜', 1, 2, NULL, 5, 1, 'carrot', 51),
(106, '热狗/烤肠', 1, 2, NULL, 6, 1, 'hot dog', 52),
(107, '披萨', 1, 2, NULL, 7, 1, 'pizza', 53),
(108, '甜甜圈', 1, 2, NULL, 8, 1, 'donut', 54),
(109, '蛋糕', 1, 2, NULL, 9, 1, 'cake', 55),
(110, '瓶装水/饮料', 1, 2, NULL, 10, 1, 'bottle', 39),
(111, '三明治', 1, 2, NULL, 11, 1, 'sandwich', 48)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

-- 插入二级分类（小类）- 家居百货/日用品 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(201, '杯子', 2, 2, NULL, 1, 1, 'cup', 41),
(202, '碗', 2, 2, NULL, 2, 1, 'bowl', 45),
(203, '书籍', 2, 2, NULL, 3, 1, 'book', 73),
(204, '钟表', 2, 2, NULL, 4, 1, 'clock', 74),
(205, '花瓶', 2, 2, NULL, 5, 1, 'vase', 75),
(206, '剪刀', 2, 2, NULL, 6, 1, 'scissors', 76),
(207, '牙刷', 2, 2, NULL, 7, 1, 'toothbrush', 79),
(208, '盆栽', 2, 2, NULL, 8, 1, 'potted plant', 58),
(209, '雨伞', 2, 2, NULL, 9, 1, 'umbrella', 25)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

-- 插入二级分类（小类）- 服饰箱包 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(301, '双肩包', 3, 2, NULL, 1, 1, 'backpack', 24),
(302, '手提包', 3, 2, NULL, 2, 1, 'handbag', 26),
(303, '行李箱', 3, 2, NULL, 3, 1, 'suitcase', 28),
(304, '领带', 3, 2, NULL, 4, 1, 'tie', 27)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

-- 插入二级分类（小类）- 数码电器 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(401, '笔记本电脑', 4, 2, NULL, 1, 1, 'laptop', 63),
(402, '鼠标', 4, 2, NULL, 2, 1, 'mouse', 64),
(403, '键盘', 4, 2, NULL, 3, 1, 'keyboard', 66),
(404, '手机', 4, 2, NULL, 4, 1, 'cell phone', 67),
(405, '吹风机', 4, 2, NULL, 5, 1, 'hair drier', 78),
(406, '电视/显示器', 4, 2, NULL, 6, 1, 'tv', 62),
(407, '微波炉', 4, 2, NULL, 7, 1, 'microwave', 68),
(408, '烤面包机', 4, 2, NULL, 8, 1, 'toaster', 70),
(409, '冰箱', 4, 2, NULL, 9, 1, 'refrigerator', 72)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

-- 插入二级分类（小类）- 运动户外 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(501, '球类', 5, 2, NULL, 1, 1, 'sports ball', 32),
(502, '网球拍', 5, 2, NULL, 2, 1, 'tennis racket', 38),
(503, '滑板', 5, 2, NULL, 3, 1, 'skateboard', 36),
(504, '自行车', 5, 2, NULL, 4, 1, 'bicycle', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

-- 2. 初始化YOLO标签映射数据
-- 注意：这些映射与pms_category表中的二级分类ID对应
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
-- 超市/食品区
('banana', 101, 0.50, 1),
('apple', 102, 0.50, 1),
('orange', 103, 0.50, 1),
('broccoli', 104, 0.50, 1),
('carrot', 105, 0.50, 1),
('hot dog', 106, 0.50, 1),
('pizza', 107, 0.50, 1),
('donut', 108, 0.50, 1),
('cake', 109, 0.50, 1),
('bottle', 110, 0.50, 1),
('sandwich', 111, 0.50, 1),
-- 家居百货/日用品
('cup', 201, 0.50, 1),
('bowl', 202, 0.50, 1),
('book', 203, 0.50, 1),
('clock', 204, 0.50, 1),
('vase', 205, 0.50, 1),
('scissors', 206, 0.50, 1),
('toothbrush', 207, 0.50, 1),
('potted plant', 208, 0.50, 1),
('umbrella', 209, 0.50, 1),
-- 服饰箱包
('backpack', 301, 0.50, 1),
('handbag', 302, 0.50, 1),
('suitcase', 303, 0.50, 1),
('tie', 304, 0.50, 1),
-- 数码电器
('laptop', 401, 0.50, 1),
('mouse', 402, 0.50, 1),
('keyboard', 403, 0.50, 1),
('cell phone', 404, 0.50, 1),
('hair drier', 405, 0.50, 1),
('tv', 406, 0.50, 1),
('microwave', 407, 0.50, 1),
('toaster', 408, 0.50, 1),
('refrigerator', 409, 0.50, 1),
-- 运动户外
('sports ball', 501, 0.50, 1),
('tennis racket', 502, 0.50, 1),
('skateboard', 503, 0.50, 1),
('bicycle', 504, 0.50, 1)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`);

-- 3. 初始化用户数据
-- 用户名: admin, 密码: admin123
INSERT INTO `sys_user`
(`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `balance`, `points`, `status`, `role`, `credit_score`, `is_first_login`, `preference_set`)
VALUES
('admin', '$2a$10$rsmJCJ5xdNAQu.ER6XgSOOKV0C1cXMD1Huv7U5QvRU19mwzJqyYgu', '系统管理员', NULL, NULL, 'admin@sv.com', 0.00, 0, 1, 'ADMIN', 100, 0, 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- 用户名: test, 密码: 123456
INSERT INTO `sys_user`
(`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `balance`, `points`, `status`, `role`, `credit_score`, `is_first_login`, `preference_set`)
VALUES
('test', '$2a$10$mpebqUiv7ByT4yqTBxdEh.MyMQaxR5NBI5GgkC/ngukQxY3C71vK6', '测试用户', NULL, NULL, 'test@sv.com', 100.00, 100, 1, 'USER', 100, 0, 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- 用户名: user, 密码: 123
INSERT INTO `sys_user`
(`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `balance`, `points`, `status`, `role`, `credit_score`, `is_first_login`, `preference_set`)
VALUES
('user', '$2a$10$wz6gHO7bWr7SZuP/04STOOJfkhGUGMAXTukw1OjWKGPUZjxeAuey2', '普通用户', NULL, NULL, 'user@sv.com', 50.00, 50, 1, 'USER', 100, 0, 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- 4. 初始化用户VIP信息
INSERT IGNORE INTO `sms_user_vip_info` (`user_id`, `vip_level`, `discount_rate`, `total_vip_days`)
SELECT `id`, 0, 1.00, 0 FROM `sys_user`;

-- 5. 初始化积分规则数据
-- 规则类型: 1签到 2消费 3评价 4分享 5首购
-- 购物积分计算: 消费金额 × 100 × 会员倍率(VIP:1.25 SVIP:1.5) × 会员日倍率
INSERT INTO `sms_points_rule` (`rule_code`, `rule_name`, `description`, `points_value`, `points_ratio`, `rule_type`, `daily_limit`, `is_active`, `sort_order`) VALUES
('SIGN_IN', '每日签到', '用户每日签到获得的积分奖励', 10, 1.00, 1, 1, 1, 1),
('PURCHASE', '购物奖励', '用户购买商品获得的积分奖励，1元=100积分', 0, 100.00, 2, 0, 1, 2),
('REVIEW', '评价奖励', '用户评价商品获得的积分奖励', 5, 1.00, 3, 3, 1, 3),
('SHARE', '分享奖励', '用户分享商品获得的积分奖励', 2, 1.00, 4, 5, 1, 4),
('FIRST_PURCHASE', '首购奖励', '用户首次购买商品获得的额外积分奖励', 50, 1.00, 5, 1, 1, 5)
ON DUPLICATE KEY UPDATE `points_value` = VALUES(`points_value`), `points_ratio` = VALUES(`points_ratio`), `daily_limit` = VALUES(`daily_limit`), `description` = VALUES(`description`);

-- 6. 初始化优惠券模板数据
INSERT INTO `sms_coupon_template` (`name`, `type`, `value`, `discount`, `min_amount`, `total_count`, `per_limit`, `scope_type`, `valid_type`, `valid_days`, `status`) VALUES
('新人专享券', 3, 10.00, NULL, 0, 1000, 1, 1, 2, 30, 1),
('满100减20', 1, 20.00, NULL, 100.00, 500, 2, 1, 2, 7, 1),
('满200减50', 1, 50.00, NULL, 200.00, 300, 1, 1, 2, 7, 1),
('8折优惠券', 2, NULL, 0.80, 50.00, 200, 1, 1, 2, 14, 1),
('满500减100', 1, 100.00, NULL, 500.00, 100, 1, 1, 2, 7, 1)
ON DUPLICATE KEY UPDATE `total_count` = VALUES(`total_count`);

-- 7. 初始化会员日活动数据
INSERT INTO `sms_activity` (`id`, `name`, `start_time`, `end_time`, `type`, `discount_rate`, `status`, `rule_expression`, `points_multiplier`, `activity_type`, `description`) VALUES
(1, '每月8号会员日', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL (8 - DAY(CURDATE())) DAY), '%Y-%m-%d 00:00:00'), DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL (8 - DAY(CURDATE())) DAY), '%Y-%m-%d 23:59:59'), 1, 0.90, 1, '8', 2.00, 1, '每月8号会员日全场9折，积分双倍'),
(2, '每周四特惠日', DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL (4 - WEEKDAY(CURDATE())) DAY), '%Y-%m-%d 00:00:00'), DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL (4 - WEEKDAY(CURDATE())) DAY), '%Y-%m-%d 23:59:59'), 1, 0.95, 1, '4', 1.50, 1, '每周四会员专享95折，积分1.5倍')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);