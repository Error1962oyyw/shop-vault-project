-- 修复数据库表结构问题

-- 1. 创建缺失的 sys_message_push 表
DROP TABLE IF EXISTS `sys_message_push`;
CREATE TABLE `sys_message_push` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` varchar(50) NOT NULL COMMENT '消息类型: SYSTEM/POINTS_EXPIRE/ORDER_STATUS/PROMOTION',
    `title` varchar(200) NOT NULL COMMENT '消息标题',
    `content` text COMMENT '消息内容',
    `target_user_id` bigint(20) DEFAULT NULL COMMENT '目标用户ID (NULL表示广播)',
    `target_role` varchar(50) DEFAULT NULL COMMENT '目标角色 (USER/ADMIN)',
    `status` tinyint(1) DEFAULT 0 COMMENT '状态: 0待发送 1已发送 2已读 3发送失败',
    `send_time` datetime DEFAULT NULL COMMENT '发送时间',
    `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
    `retry_count` int(11) DEFAULT 0 COMMENT '重试次数',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_target_user` (`target_user_id`),
    KEY `idx_target_role` (`target_role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息推送表';

-- 2. 修复 sms_points_record 表结构
-- 先备份原有数据（如果存在）
-- 然后重建表结构

DROP TABLE IF EXISTS `sms_points_record`;
CREATE TABLE `sms_points_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `points` int(11) NOT NULL COMMENT '变动数量 (正数为增，负数为减)',
    `balance_after` int(11) DEFAULT 0 COMMENT '变动后余额',
    `type` varchar(50) NOT NULL COMMENT '类型: SIGN_IN/PURCHASE/REVIEW/REFUND_DEDUCT/EXPIRE/ADMIN_ADJUST',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `related_id` bigint(20) DEFAULT NULL COMMENT '关联ID (订单ID等)',
    `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
    `is_expired` tinyint(1) DEFAULT 0 COMMENT '是否已过期',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_expire` (`user_id`, `expire_time`, `is_expired`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分变动记录表';
