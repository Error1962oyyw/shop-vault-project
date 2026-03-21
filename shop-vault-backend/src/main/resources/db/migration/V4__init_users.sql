-- 初始化管理员用户和测试用户
-- 注意：密码需要使用BCrypt加密

-- 管理员用户
-- 用户名: admin, 密码: admin123
-- BCrypt加密后的密码
INSERT INTO `sys_user` 
(`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `balance`, `points`, `status`, `role`, `create_time`, `credit_score`, `is_first_login`, `preference_set`)
VALUES 
('admin', '$2a$10$rsmJCJ5xdNAQu.ER6XgSOOKV0C1cXMD1Huv7U5QvRU19mwzJqyYgu', '系统管理员', NULL, NULL, 'admin@shopvault.com', 0.00, 0, 1, 'ADMIN', NOW(), 100, 0, 1)
ON DUPLICATE KEY UPDATE `password` = '$2a$10$rsmJCJ5xdNAQu.ER6XgSOOKV0C1cXMD1Huv7U5QvRU19mwzJqyYgu';

-- 测试用户
-- 用户名: test, 密码: 123456
-- BCrypt加密后的密码
INSERT INTO `sys_user` 
(`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `balance`, `points`, `status`, `role`, `create_time`, `credit_score`, `is_first_login`, `preference_set`)
VALUES 
('test', '$2a$10$mpebqUiv7ByT4yqTBxdEh.MyMQaxR5NBI5GgkC/ngukQxY3C71vK6', '测试用户', NULL, NULL, 'test@shopvault.com', 100.00, 100, 1, 'USER', NOW(), 100, 0, 1)
ON DUPLICATE KEY UPDATE `password` = '$2a$10$mpebqUiv7ByT4yqTBxdEh.MyMQaxR5NBI5GgkC/ngukQxY3C71vK6';

-- 用户: user, 密码: 123
-- BCrypt加密后的密码
INSERT INTO `sys_user` 
(`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `balance`, `points`, `status`, `role`, `create_time`, `credit_score`, `is_first_login`, `preference_set`)
VALUES 
('user', '$2a$10$wz6gHO7bWr7SZuP/04STOOJfkhGUGMAXTukw1OjWKGPUZjxeAuey2', '普通用户', NULL, NULL, 'user@shopvault.com', 50.00, 50, 1, 'USER', NOW(), 100, 0, 1)
ON DUPLICATE KEY UPDATE `password` = '$2a$10$wz6gHO7bWr7SZuP/04STOOJfkhGUGMAXTukw1OjWKGPUZjxeAuey2';
