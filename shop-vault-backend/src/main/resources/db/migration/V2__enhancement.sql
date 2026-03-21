-- =============================================
-- V2 增强功能数据库迁移脚本
-- =============================================

-- 1. 用户偏好分类表
CREATE TABLE IF NOT EXISTS `sys_user_preference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `category_id` BIGINT NOT NULL COMMENT '偏好分类ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    UNIQUE KEY `uk_user_category` (`user_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好分类表';

-- 2. 积分规则配置表
CREATE TABLE IF NOT EXISTS `sms_points_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `description` VARCHAR(500) COMMENT '规则描述',
    `points_value` INT DEFAULT 0 COMMENT '固定积分值',
    `points_ratio` DECIMAL(10,4) COMMENT '积分比例',
    `rule_type` INT NOT NULL COMMENT '规则类型:1签到2消费3评价4分享5首购',
    `daily_limit` INT DEFAULT 0 COMMENT '每日限制次数',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则配置表';

-- 3. 积分记录表增强
ALTER TABLE `sms_points_record` 
ADD COLUMN IF NOT EXISTS `expire_time` DATETIME COMMENT '过期时间',
ADD COLUMN IF NOT EXISTS `is_expired` TINYINT(1) DEFAULT 0 COMMENT '是否已过期';

-- 4. 售后表增强
ALTER TABLE `oms_after_sales`
ADD COLUMN IF NOT EXISTS `type` INT DEFAULT 1 COMMENT '售后类型:1仅退款2退货退款3换货',
ADD COLUMN IF NOT EXISTS `description` VARCHAR(500) COMMENT '问题描述',
ADD COLUMN IF NOT EXISTS `refund_points` INT COMMENT '需扣回的积分',
ADD COLUMN IF NOT EXISTS `return_logistics_company` VARCHAR(50) COMMENT '退货物流公司',
ADD COLUMN IF NOT EXISTS `return_logistics_no` VARCHAR(100) COMMENT '退货物流单号',
ADD COLUMN IF NOT EXISTS `return_logistics_time` DATETIME COMMENT '退货物流填写时间',
ADD COLUMN IF NOT EXISTS `merchant_handle_time` DATETIME COMMENT '商家处理时间',
ADD COLUMN IF NOT EXISTS `admin_remark` VARCHAR(500) COMMENT '管理员备注';

-- 5. 订单表增强(积分支付)
ALTER TABLE `oms_order`
ADD COLUMN IF NOT EXISTS `points_used` INT DEFAULT 0 COMMENT '使用积分数量',
ADD COLUMN IF NOT EXISTS `points_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分抵扣金额';

-- 6. 商品表增强(乐观锁)
ALTER TABLE `pms_product`
ADD COLUMN IF NOT EXISTS `version` INT DEFAULT 0 COMMENT '版本号(乐观锁)';

-- 7. 用户表增强
ALTER TABLE `sys_user`
ADD COLUMN IF NOT EXISTS `is_first_login` TINYINT(1) DEFAULT 1 COMMENT '是否首次登录',
ADD COLUMN IF NOT EXISTS `preference_set` TINYINT(1) DEFAULT 0 COMMENT '是否已设置偏好';

-- 8. 初始化积分规则数据
INSERT INTO `sms_points_rule` (`rule_code`, `rule_name`, `description`, `points_value`, `points_ratio`, `rule_type`, `daily_limit`, `is_active`, `sort_order`) VALUES
('SIGN_IN', '每日签到', '每日签到获得积分', 10, NULL, 1, 1, 1, 1),
('PURCHASE_REWARD', '购物奖励', '购物消费获得积分,1元=100积分', NULL, 100.0000, 2, 0, 1, 2),
('REWARD_REVIEW', '评价奖励', '完成商品评价获得积分', 20, NULL, 3, 5, 1, 3),
('FIRST_PURCHASE', '首购奖励', '首次购物额外奖励', 100, NULL, 5, 0, 1, 4),
('SHARE_REWARD', '分享奖励', '分享商品获得积分', 5, NULL, 4, 3, 1, 5);

-- 9. 初始化YOLO标签映射数据
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
('couch', 5, 0.50, 1);

-- 10. 索引优化
CREATE INDEX IF NOT EXISTS `idx_order_status_create` ON `oms_order` (`status`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_order_user_status` ON `oms_order` (`user_id`, `status`);
CREATE INDEX IF NOT EXISTS `idx_after_sales_status` ON `oms_after_sales` (`status`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_points_record_user_expire` ON `sms_points_record` (`user_id`, `expire_time`, `is_expired`);
CREATE INDEX IF NOT EXISTS `idx_user_preference_user` ON `sys_user_preference` (`user_id`);
