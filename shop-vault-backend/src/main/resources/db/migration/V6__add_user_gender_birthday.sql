-- 添加 sys_user 表缺失的 gender 和 birthday 字段

-- 添加 gender 字段（性别：0未知 1男 2女）
ALTER TABLE `sys_user` 
ADD COLUMN `gender` TINYINT DEFAULT 0 COMMENT '性别: 0未知 1男 2女' AFTER `email`;

-- 添加 birthday 字段（生日）
ALTER TABLE `sys_user` 
ADD COLUMN `birthday` DATE DEFAULT NULL COMMENT '生日' AFTER `gender`;
