-- =============================================
-- V7 添加活动表缺失字段
-- =============================================

ALTER TABLE `sms_activity`
ADD COLUMN IF NOT EXISTS `description` VARCHAR(500) COMMENT '活动描述' AFTER `points_multiplier`;

ALTER TABLE `sms_activity`
ADD COLUMN IF NOT EXISTS `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
