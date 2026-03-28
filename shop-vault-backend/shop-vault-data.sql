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
INSERT INTO `sms_points_rule` (`rule_code`, `rule_name`, `description`, `points_value`, `points_ratio`, `rule_type`, `daily_limit`, `is_active`, `sort_order`) VALUES
('SIGN_IN', '每日签到', '用户每日签到获得的积分奖励', 10, 1.00, 1, 1, 1, 1),
('PURCHASE', '购物奖励', '用户购买商品获得的积分奖励', 0, 1.00, 2, 0, 1, 2),
('REVIEW', '评价奖励', '用户评价商品获得的积分奖励', 5, 1.00, 3, 3, 1, 3),
('SHARE', '分享奖励', '用户分享商品获得的积分奖励', 2, 1.00, 4, 5, 1, 4)
ON DUPLICATE KEY UPDATE `points_value` = VALUES(`points_value`), `points_ratio` = VALUES(`points_ratio`), `daily_limit` = VALUES(`daily_limit`);

-- 5. 初始化积分商城商品数据
INSERT INTO `sms_points_product` (`name`, `description`, `type`, `points_cost`, `stock`, `daily_limit`, `original_price`, `sort_order`, `status`) VALUES
('精美手机支架', '小巧便携，稳固耐用', 1, 500, 100, 1, 29.00, 1, 1),
('创意马克杯', '高品质陶瓷，精美印花', 1, 800, 50, 1, 49.00, 2, 1),
('满50减10优惠券', '全场通用，无门槛限制', 2, 300, 200, 2, 10.00, 3, 1),
('满100减25优惠券', '全场通用，满100可用', 2, 600, 150, 1, 25.00, 4, 1),
('VIP月卡', '享受95折优惠，专属会员日活动', 3, 1000, 999, 0, 99.00, 5, 1),
('VIP年卡', '享受95折优惠，专属会员日活动，更多特权', 4, 10000, 999, 0, 999.00, 6, 1)
ON DUPLICATE KEY UPDATE `stock` = VALUES(`stock`);

-- 6. 初始化YOLO标签映射数据
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
('cup', 1, 0.50, 1),
('bottle', 1, 0.50, 1),
('backpack', 2, 0.50, 1),
('handbag', 2, 0.50, 1),
('cell phone', 3, 0.50, 1),
('laptop', 3, 0.50, 1),
('tv', 3, 0.50, 1),
('book', 4, 0.50, 1),
('chair', 5, 0.50, 1),
('couch', 5, 0.50, 1)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`);

-- 7. 初始化规格数据
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

-- 8. 初始化优惠券模板数据
INSERT INTO `sms_coupon_template` (`name`, `type`, `value`, `discount`, `min_amount`, `total_count`, `per_limit`, `scope_type`, `valid_type`, `valid_days`, `status`) VALUES
('新人专享券', 3, 10.00, NULL, 0, 1000, 1, 1, 2, 30, 1),
('满100减20', 1, 20.00, NULL, 100.00, 500, 2, 1, 2, 7, 1),
('满200减50', 1, 50.00, NULL, 200.00, 300, 1, 1, 2, 7, 1),
('8折优惠券', 2, NULL, 0.80, 50.00, 200, 1, 1, 2, 14, 1),
('满500减100', 1, 100.00, NULL, 500.00, 100, 1, 1, 2, 7, 1)
ON DUPLICATE KEY UPDATE `total_count` = VALUES(`total_count`);
