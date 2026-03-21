-- =============================================
-- V3 SKU和优惠券增强功能数据库迁移脚本
-- =============================================

-- 1. 商品规格表
CREATE TABLE IF NOT EXISTS `pms_spec` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '规格名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表';

-- 2. 商品规格值表
CREATE TABLE IF NOT EXISTS `pms_spec_value` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `spec_id` BIGINT NOT NULL COMMENT '规格ID',
    `value` VARCHAR(100) NOT NULL COMMENT '规格值',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_spec_id` (`spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格值表';

-- 3. 商品SKU表
CREATE TABLE IF NOT EXISTS `pms_product_sku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `sku_code` VARCHAR(100) NOT NULL COMMENT 'SKU编码',
    `spec_json` VARCHAR(500) COMMENT '规格JSON: [{"specId":1,"specName":"颜色","valueId":1,"value":"红色"}]',
    `price` DECIMAL(10,2) NOT NULL COMMENT 'SKU价格',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `stock_warning` INT DEFAULT 10 COMMENT '库存预警值',
    `image` VARCHAR(500) COMMENT 'SKU图片',
    `status` INT DEFAULT 1 COMMENT '状态:1启用0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- 4. 优惠券模板表
CREATE TABLE IF NOT EXISTS `sms_coupon_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `type` INT NOT NULL COMMENT '类型:1满减券2折扣券3无门槛券',
    `value` DECIMAL(10,2) COMMENT '优惠金额(满减券/无门槛券)',
    `discount` DECIMAL(3,2) COMMENT '折扣率(折扣券,如0.8表示8折)',
    `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
    `max_discount` DECIMAL(10,2) COMMENT '最大优惠金额(折扣券)',
    `total_count` INT DEFAULT 0 COMMENT '发放总量',
    `used_count` INT DEFAULT 0 COMMENT '已使用数量',
    `per_limit` INT DEFAULT 1 COMMENT '每人限领数量',
    `scope_type` INT DEFAULT 1 COMMENT '适用范围:1全场2指定分类3指定商品',
    `scope_ids` VARCHAR(500) COMMENT '适用范围ID列表(逗号分隔)',
    `valid_type` INT DEFAULT 1 COMMENT '有效期类型:1固定时间2领取后天数',
    `valid_start_time` DATETIME COMMENT '有效期开始时间',
    `valid_end_time` DATETIME COMMENT '有效期结束时间',
    `valid_days` INT COMMENT '领取后有效天数',
    `status` INT DEFAULT 1 COMMENT '状态:1启用0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 5. 用户优惠券表增强
ALTER TABLE `sms_user_coupon`
ADD COLUMN IF NOT EXISTS `coupon_template_id` BIGINT COMMENT '优惠券模板ID',
ADD COLUMN IF NOT EXISTS `order_id` BIGINT COMMENT '使用的订单ID',
ADD COLUMN IF NOT EXISTS `order_amount` DECIMAL(10,2) COMMENT '订单金额',
ADD COLUMN IF NOT EXISTS `discount_amount` DECIMAL(10,2) COMMENT '优惠金额';

-- 6. 订单表增强(优惠券)
ALTER TABLE `oms_order`
ADD COLUMN IF NOT EXISTS `coupon_id` BIGINT COMMENT '使用的优惠券ID',
ADD COLUMN IF NOT EXISTS `coupon_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券抵扣金额',
ADD COLUMN IF NOT EXISTS `sku_id` BIGINT COMMENT 'SKU ID(如果购买的是SKU商品)';

-- 7. 购物车表增强
ALTER TABLE `oms_cart_item`
ADD COLUMN IF NOT EXISTS `sku_id` BIGINT COMMENT 'SKU ID';

-- 8. 订单商品表增强
ALTER TABLE `oms_order_item`
ADD COLUMN IF NOT EXISTS `sku_id` BIGINT COMMENT 'SKU ID',
ADD COLUMN IF NOT EXISTS `spec_json` VARCHAR(500) COMMENT '规格JSON快照';

-- 9. 初始化规格数据
INSERT INTO `pms_spec` (`name`, `sort_order`) VALUES
('颜色', 1),
('尺寸', 2),
('版本', 3);

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
(3, '旗舰版', 3);

-- 10. 初始化优惠券模板数据
INSERT INTO `sms_coupon_template` (`name`, `type`, `value`, `discount`, `min_amount`, `total_count`, `per_limit`, `scope_type`, `valid_type`, `valid_days`, `status`) VALUES
('新人专享券', 3, 10.00, NULL, 0, 1000, 1, 1, 2, 30, 1),
('满100减20', 1, 20.00, NULL, 100.00, 500, 2, 1, 2, 7, 1),
('满200减50', 1, 50.00, NULL, 200.00, 300, 1, 1, 2, 7, 1),
('8折优惠券', 2, NULL, 0.80, 50.00, 200, 1, 1, 2, 14, 1),
('满500减100', 1, 100.00, NULL, 500.00, 100, 1, 1, 2, 7, 1);

-- 11. 索引优化
CREATE INDEX IF NOT EXISTS `idx_product_sku_product` ON `pms_product_sku` (`product_id`, `status`);
CREATE INDEX IF NOT EXISTS `idx_coupon_template_status` ON `sms_coupon_template` (`status`, `valid_start_time`, `valid_end_time`);
CREATE INDEX IF NOT EXISTS `idx_user_coupon_user_status` ON `sms_user_coupon` (`user_id`, `status`, `expire_time`);
