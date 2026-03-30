-- =====================================================
-- Shop Vault 初始化数据
-- =====================================================

-- 1. 初始化管理员用户
-- 用户名: admin, 密码: admin123
INSERT INTO `sys_user`
(`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `balance`, `points`, `status`, `role`, `credit_score`, `is_first_login`, `preference_set`)
VALUES
('admin', '$2a$10$rsmJCJ5xdNAQu.ER6XgSOOKV0C1cXMD1Huv7U5QvRU19mwzJqyYgu', '系统管理员', NULL, NULL, 'admin@sv.com', 0.00, 0, 1, 'ADMIN', 100, 0, 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- 2. 初始化测试用户
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

-- 3. 初始化用户VIP信息
INSERT IGNORE INTO `sms_user_vip_info` (`user_id`, `vip_level`, `discount_rate`, `total_vip_days`)
SELECT `id`, 0, 1.00, 0 FROM `sys_user`;

-- 4. 初始化积分规则数据
-- 规则类型: 1签到 2消费 3评价 4分享 5首购
-- 购物积分计算: 消费金额 × 100 × 会员倍率(VIP:1.25 SVIP:1.5) × 会员日倍率
INSERT INTO `sms_points_rule` (`rule_code`, `rule_name`, `description`, `points_value`, `points_ratio`, `rule_type`, `daily_limit`, `is_active`, `sort_order`) VALUES
('SIGN_IN', '每日签到', '用户每日签到获得的积分奖励', 10, 1.00, 1, 1, 1, 1),
('PURCHASE', '购物奖励', '用户购买商品获得的积分奖励，1元=100积分', 0, 100.00, 2, 0, 1, 2),
('REVIEW', '评价奖励', '用户评价商品获得的积分奖励', 5, 1.00, 3, 3, 1, 3),
('SHARE', '分享奖励', '用户分享商品获得的积分奖励', 2, 1.00, 4, 5, 1, 4)
ON DUPLICATE KEY UPDATE `points_value` = VALUES(`points_value`), `points_ratio` = VALUES(`points_ratio`), `daily_limit` = VALUES(`daily_limit`), `description` = VALUES(`description`);

-- 5. 初始化YOLO标签映射数据
-- 注意：这些映射与pms_category表中的二级分类ID对应
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
-- 超市/食品区
('banana', 101, 0.50, 1),
('apple', 102, 0.50, 1),
('bottle', 110, 0.50, 1),
-- 家居百货/日用品
('cup', 201, 0.50, 1),
('bowl', 202, 0.50, 1),
('book', 203, 0.50, 1),
-- 服饰箱包
('backpack', 301, 0.50, 1),
('handbag', 302, 0.50, 1),
-- 数码电器
('cell phone', 404, 0.50, 1),
('laptop', 401, 0.50, 1),
('tv', 406, 0.50, 1)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`);

-- 6. 初始化规格数据
INSERT INTO `pms_spec` (`name`, `sort_order`) VALUES
('颜色', 1),
('尺寸', 2),
('版本', 3)
ON DUPLICATE KEY UPDATE `sort_order` = VALUES(`sort_order`);

INSERT INTO `pms_spec_value` (`spec_id`, `value`, `sort_order`) VALUES
(1, '红色', 1),
(1, '蓝色', 2),
(1, '黑色', 3),
(1, '白色', 4),
(2, 'S', 1),
(2, 'M', 2),
(2, 'L', 3),
(2, 'XL', 4),
(3, '标准版', 1),
(3, '豪华版', 2),
(3, '旗舰版', 3)
ON DUPLICATE KEY UPDATE `sort_order` = VALUES(`sort_order`);

-- 7. 初始化优惠券模板数据
INSERT INTO `sms_coupon_template` (`name`, `type`, `value`, `discount`, `min_amount`, `total_count`, `per_limit`, `scope_type`, `valid_type`, `valid_days`, `status`) VALUES
('新人专享券', 3, 10.00, NULL, 0, 1000, 1, 1, 2, 30, 1),
('满100减20', 1, 20.00, NULL, 100.00, 500, 2, 1, 2, 7, 1),
('满200减50', 1, 50.00, NULL, 200.00, 300, 1, 1, 2, 7, 1),
('8折优惠券', 2, NULL, 0.80, 50.00, 200, 1, 1, 2, 14, 1),
('满500减100', 1, 100.00, NULL, 500.00, 100, 1, 1, 2, 7, 1)
ON DUPLICATE KEY UPDATE `total_count` = VALUES(`total_count`);
