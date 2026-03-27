-- 积分规则配置初始化数据
-- 执行此脚本前请确保数据库已备份

-- 插入签到规则
INSERT INTO `sms_points_rule` (`rule_code`, `rule_name`, `description`, `points_value`, `points_ratio`, `rule_type`, `daily_limit`, `is_active`, `sort_order`, `create_time`, `update_time`) VALUES
('SIGN_IN', '每日签到', '用户每日签到获得的积分奖励', 10, 1.00, 1, 1, 1, 1, NOW(), NOW()),
('PURCHASE', '购物奖励', '用户购买商品获得的积分奖励', 0, 0.01, 2, 0, 1, 2, NOW(), NOW()),
('REVIEW', '评价奖励', '用户评价商品获得的积分奖励', 5, 1.00, 3, 3, 1, 3, NOW(), NOW()),
('SHARE', '分享奖励', '用户分享商品获得的积分奖励', 2, 1.00, 4, 5, 1, 4, NOW(), NOW())
ON DUPLICATE KEY UPDATE `points_value` = VALUES(`points_value`), `points_ratio` = VALUES(`points_ratio`), `daily_limit` = VALUES(`daily_limit`);

-- 验证插入结果
SELECT '积分规则数量:' AS info, COUNT(*) AS count FROM sms_points_rule;
SELECT * FROM sms_points_rule WHERE rule_type = 1;
