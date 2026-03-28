SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- Shop Vault 数据库表结构
-- =====================================================

-- ----------------------------
-- 1. 用户表 (sys_user)
-- 支持游客注册、会员管理、余额与积分
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '加密密码',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `gender` TINYINT(1) DEFAULT 0 COMMENT '性别: 0未知 1男 2女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '钱包余额(模拟支付用)',
    `points` INT DEFAULT 0 COMMENT '当前积分',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态 1:正常 0:冻结',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `credit_score` INT DEFAULT 100 COMMENT '信誉分(影响售后审批)',
    `is_first_login` TINYINT(1) DEFAULT 1 COMMENT '是否首次登录',
    `preference_set` TINYINT(1) DEFAULT 0 COMMENT '是否已设置偏好',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 2. 收货地址表 (sys_address)
-- ----------------------------
DROP TABLE IF EXISTS `sys_address`;
CREATE TABLE `sys_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province` VARCHAR(64) DEFAULT NULL COMMENT '省',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '市',
    `region` VARCHAR(64) DEFAULT NULL COMMENT '区',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认地址:0否 1是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ----------------------------
-- 3. 商品分类表 (pms_category)
-- ----------------------------
DROP TABLE IF EXISTS `pms_category`;
CREATE TABLE `pms_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(64) NOT NULL COMMENT '分类名称 (如: 数码, 服装)',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID (0为一级分类)',
    `level` INT DEFAULT 1 COMMENT '层级 (1:一级 2:二级)',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
    `sort` INT DEFAULT 0 COMMENT '排序权重',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    `yolo_label` VARCHAR(64) DEFAULT NULL COMMENT 'YOLO标签(二级分类对应)',
    `coco_id` INT DEFAULT NULL COMMENT 'COCO数据集编号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`),
    KEY `idx_yolo_label` (`yolo_label`),
    KEY `idx_coco_id` (`coco_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ----------------------------
-- 4. YOLO视觉映射表 (sys_yolo_mapping)
-- [核心创新点]: 将YOLO识别出的英文标签映射到系统的商品分类
-- ----------------------------
DROP TABLE IF EXISTS `sys_yolo_mapping`;
CREATE TABLE `sys_yolo_mapping` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `yolo_label` VARCHAR(64) NOT NULL COMMENT 'YOLO模型输出的标签 (如: cup, backpack)',
    `category_id` BIGINT NOT NULL COMMENT '关联的系统分类ID',
    `confidence_threshold` DECIMAL(4,2) DEFAULT 0.50 COMMENT '置信度阈值 (过滤低可信度识别)',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用映射',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_yolo_label` (`yolo_label`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI视觉标签映射表';

-- ----------------------------
-- 5. 商品信息表 (pms_product)
-- ----------------------------
DROP TABLE IF EXISTS `pms_product`;
CREATE TABLE `pms_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `sub_title` VARCHAR(255) DEFAULT NULL COMMENT '副标题/卖点',
    `main_image` VARCHAR(500) NOT NULL COMMENT '主图',
    `price` DECIMAL(10,2) NOT NULL COMMENT '销售价格',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `stock_warning` INT DEFAULT 10 COMMENT '库存预警阈值',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态 1:上架 0:下架',
    `sales` INT DEFAULT 0 COMMENT '销量 (用于热销推荐)',
    `detail_html` TEXT COMMENT '商品详情(富文本)',
    `detail_images` TEXT COMMENT '商品详情图片(JSON数组)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT DEFAULT 0 COMMENT '版本号(乐观锁)',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sales` (`sales`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ----------------------------
-- 6. 商品规格表 (pms_spec)
-- ----------------------------
DROP TABLE IF EXISTS `pms_spec`;
CREATE TABLE `pms_spec` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '规格名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表';

-- ----------------------------
-- 7. 商品规格值表 (pms_spec_value)
-- ----------------------------
DROP TABLE IF EXISTS `pms_spec_value`;
CREATE TABLE `pms_spec_value` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `spec_id` BIGINT NOT NULL COMMENT '规格ID',
    `value` VARCHAR(100) NOT NULL COMMENT '规格值',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_spec_id` (`spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格值表';

-- ----------------------------
-- 8. 商品SKU表 (pms_product_sku)
-- ----------------------------
DROP TABLE IF EXISTS `pms_product_sku`;
CREATE TABLE `pms_product_sku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `sku_code` VARCHAR(100) NOT NULL COMMENT 'SKU编码',
    `spec_json` VARCHAR(500) COMMENT '规格JSON: [{"specId":1,"specName":"颜色","valueId":1,"value":"红色"}]',
    `price` DECIMAL(10,2) NOT NULL COMMENT 'SKU价格',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `stock_warning` INT DEFAULT 10 COMMENT '库存预警值',
    `image` VARCHAR(500) COMMENT 'SKU图片',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态:1启用0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_product_status` (`product_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- ----------------------------
-- 9. 购物车表 (oms_cart_item)
-- ----------------------------
DROP TABLE IF EXISTS `oms_cart_item`;
CREATE TABLE `oms_cart_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `sku_id` BIGINT DEFAULT NULL COMMENT 'SKU ID',
    `quantity` INT DEFAULT 1 COMMENT '购买数量',
    `selected` TINYINT(1) DEFAULT 1 COMMENT '是否选中: 0否 1是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ----------------------------
-- 10. 订单主表 (oms_order)
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号(唯一)',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额 (扣除优惠后)',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0待付款 1待发货 2待收货 3已完成 4已关闭 5售后中',
    `receiver_snapshot` TEXT COMMENT '收货人信息快照(JSON格式)',
    `tracking_company` VARCHAR(64) DEFAULT NULL COMMENT '物流公司',
    `tracking_no` VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `receive_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
    `auto_receive_time` DATETIME DEFAULT NULL COMMENT '自动收货时间(发货后+10天)',
    `is_extended` TINYINT(1) DEFAULT 0 COMMENT '是否已延长收货: 0否 1是',
    `close_time` DATETIME DEFAULT NULL COMMENT '订单关闭时间',
    `points_used` INT DEFAULT 0 COMMENT '使用积分数量',
    `points_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分抵扣金额',
    `coupon_id` BIGINT DEFAULT NULL COMMENT '使用的优惠券ID',
    `coupon_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券抵扣金额',
    `vip_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'VIP折扣金额',
    `sku_id` BIGINT DEFAULT NULL COMMENT 'SKU ID',
    `order_type` TINYINT DEFAULT 0 COMMENT '订单类型: 0普通订单 1积分兑换订单',
    `is_points_exchange` TINYINT DEFAULT 0 COMMENT '是否积分兑换: 0否 1是',
    `after_sales_disabled` TINYINT DEFAULT 0 COMMENT '售后是否禁用: 0否 1是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_status_create` (`status`, `create_time`),
    KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- ----------------------------
-- 11. 订单明细表 (oms_order_item)
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单编号',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `product_img` VARCHAR(500) DEFAULT NULL COMMENT '商品图片',
    `product_price` DECIMAL(10,2) NOT NULL COMMENT '购买时的单价',
    `quantity` INT DEFAULT 1 COMMENT '购买数量',
    `sku_id` BIGINT DEFAULT NULL COMMENT 'SKU ID',
    `spec_json` VARCHAR(500) COMMENT '规格JSON快照',
    `is_points_exchange` TINYINT DEFAULT 0 COMMENT '是否积分兑换商品: 0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品详情表';

-- ----------------------------
-- 12. 售后服务表 (oms_after_sales)
-- ----------------------------
DROP TABLE IF EXISTS `oms_after_sales`;
CREATE TABLE `oms_after_sales` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单号',
    `user_id` BIGINT NOT NULL COMMENT '申请人',
    `reason` VARCHAR(500) NOT NULL COMMENT '申请售后的原因/诉求',
    `images` TEXT COMMENT '凭证图片(JSON数组)',
    `status` TINYINT(1) DEFAULT 0 COMMENT '状态: 0待商家处理 1商家同意 2商家拒绝 3已完成 4已撤销',
    `merchant_reply` VARCHAR(500) DEFAULT NULL COMMENT '商家的回复/拒绝理由',
    `refund_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '实际退款金额',
    `type` TINYINT DEFAULT 1 COMMENT '售后类型:1仅退款2退货退款3换货',
    `description` VARCHAR(500) COMMENT '问题描述',
    `refund_points` INT DEFAULT NULL COMMENT '需扣回的积分',
    `return_logistics_company` VARCHAR(50) COMMENT '退货物流公司',
    `return_logistics_no` VARCHAR(100) COMMENT '退货物流单号',
    `return_logistics_time` DATETIME COMMENT '退货物流填写时间',
    `merchant_handle_time` DATETIME COMMENT '商家处理时间',
    `admin_remark` VARCHAR(500) COMMENT '管理员备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_status_create` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后服务记录表';

-- ----------------------------
-- 13. 商品评价表 (pms_comment)
-- ----------------------------
DROP TABLE IF EXISTS `pms_comment`;
CREATE TABLE `pms_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `star` DECIMAL(2,1) DEFAULT 5.0 COMMENT '星级 1.0-5.0',
    `content` TEXT COMMENT '评价内容',
    `images` TEXT COMMENT '评价图片(JSON数组)',
    `audit_status` TINYINT(1) DEFAULT 1 COMMENT '状态: 1正常展示 2被管理员删除',
    `likes` INT DEFAULT 0 COMMENT '点赞数',
    `dislikes` INT DEFAULT 0 COMMENT '点踩数',
    `is_reported` TINYINT(1) DEFAULT 0 COMMENT '是否被举报: 0否 1是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- ----------------------------
-- 14. 商品收藏表 (pms_favorite)
-- ----------------------------
DROP TABLE IF EXISTS `pms_favorite`;
CREATE TABLE `pms_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户商品收藏表';

-- ----------------------------
-- 15. 积分记录表 (sms_points_record)
-- ----------------------------
DROP TABLE IF EXISTS `sms_points_record`;
CREATE TABLE `sms_points_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `points` INT NOT NULL COMMENT '变动数量 (正数为增，负数为减)',
    `balance_after` INT DEFAULT 0 COMMENT '变动后余额',
    `type` VARCHAR(50) NOT NULL COMMENT '类型: SIGN_IN/PURCHASE/REVIEW/EXCHANGE/REFUND_DEDUCT/EXPIRE/ADMIN_ADJUST',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID (订单ID等)',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `is_expired` TINYINT(1) DEFAULT 0 COMMENT '是否已过期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_user_expire` (`user_id`, `expire_time`, `is_expired`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分变动记录表';

-- ----------------------------
-- 16. 积分规则配置表 (sms_points_rule)
-- ----------------------------
DROP TABLE IF EXISTS `sms_points_rule`;
CREATE TABLE `sms_points_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `description` VARCHAR(500) COMMENT '规则描述',
    `points_value` INT DEFAULT 0 COMMENT '固定积分值',
    `points_ratio` DECIMAL(10,4) COMMENT '积分比例',
    `rule_type` TINYINT NOT NULL COMMENT '规则类型:1签到2消费3评价4分享5首购',
    `daily_limit` INT DEFAULT 0 COMMENT '每日限制次数',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_rule_type` (`rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分规则配置表';

-- ----------------------------
-- 17. VIP会员记录表 (sms_vip_membership)
-- ----------------------------
DROP TABLE IF EXISTS `sms_vip_membership`;
CREATE TABLE `sms_vip_membership` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` TINYINT NOT NULL COMMENT 'VIP类型: 1月卡 2年卡 3SVIP年卡',
    `vip_level` TINYINT DEFAULT 1 COMMENT 'VIP等级: 1VIP 2SVIP',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0未激活 1激活中 2已过期',
    `points_cost` INT DEFAULT NULL COMMENT '消耗积分',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '来源: POINTS_EXCHANGE/PURCHASE/GIFT',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status_end_time` (`status`, `end_time`),
    KEY `idx_vip_level` (`vip_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP会员记录表';

-- ----------------------------
-- 18. 用户VIP信息表 (sms_user_vip_info)
-- ----------------------------
DROP TABLE IF EXISTS `sms_user_vip_info`;
CREATE TABLE `sms_user_vip_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `vip_level` TINYINT DEFAULT 0 COMMENT 'VIP等级: 0普通 1VIP 2SVIP',
    `discount_rate` DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率(如0.95表示95折)',
    `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP过期时间',
    `total_vip_days` INT DEFAULT 0 COMMENT '累计VIP天数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户VIP信息表';

-- ----------------------------
-- 19. 积分商城商品表 (sms_points_product)
-- ----------------------------
DROP TABLE IF EXISTS `sms_points_product`;
CREATE TABLE `sms_points_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` VARCHAR(500) COMMENT '商品描述',
    `image` VARCHAR(500) COMMENT '商品图片',
    `images` TEXT COMMENT '商品图片列表(JSON数组)',
    `main_image` VARCHAR(500) COMMENT '主图URL',
    `thumbnail` VARCHAR(500) COMMENT '缩略图URL',
    `type` TINYINT NOT NULL COMMENT '类型: 1小商品 2优惠券 3VIP月卡 4VIP年卡',
    `points_cost` INT NOT NULL COMMENT '所需积分',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `daily_limit` INT DEFAULT 0 COMMENT '每日兑换限制(0不限)',
    `total_limit` INT DEFAULT 0 COMMENT '总兑换限制(0不限)',
    `original_price` DECIMAL(10,2) COMMENT '原价',
    `related_id` BIGINT COMMENT '关联ID(商品ID/优惠券模板ID)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_status` (`type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分商城商品表';

-- ----------------------------
-- 20. 优惠券模板表 (sms_coupon_template)
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_template`;
CREATE TABLE `sms_coupon_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `type` TINYINT NOT NULL COMMENT '类型:1满减券2折扣券3无门槛券',
    `value` DECIMAL(10,2) COMMENT '优惠金额(满减券/无门槛券)',
    `discount` DECIMAL(3,2) COMMENT '折扣率(折扣券,如0.8表示8折)',
    `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
    `max_discount` DECIMAL(10,2) COMMENT '最大优惠金额(折扣券)',
    `total_count` INT DEFAULT 0 COMMENT '发放总量',
    `used_count` INT DEFAULT 0 COMMENT '已使用数量',
    `per_limit` INT DEFAULT 1 COMMENT '每人限领数量',
    `scope_type` TINYINT DEFAULT 1 COMMENT '适用范围:1全场2指定分类3指定商品',
    `scope_ids` VARCHAR(500) COMMENT '适用范围ID列表(逗号分隔)',
    `valid_type` TINYINT DEFAULT 1 COMMENT '有效期类型:1固定时间2领取后天数',
    `valid_start_time` DATETIME COMMENT '有效期开始时间',
    `valid_end_time` DATETIME COMMENT '有效期结束时间',
    `valid_days` INT COMMENT '领取后有效天数',
    `status` TINYINT DEFAULT 1 COMMENT '状态:1启用0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_status_valid` (`status`, `valid_start_time`, `valid_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券模板表';

-- ----------------------------
-- 21. 用户优惠券表 (sms_user_coupon)
-- ----------------------------
DROP TABLE IF EXISTS `sms_user_coupon`;
CREATE TABLE `sms_user_coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `coupon_template_id` BIGINT NOT NULL COMMENT '优惠券模板ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0未使用 1已使用 2已过期',
    `get_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `order_id` BIGINT DEFAULT NULL COMMENT '使用的订单ID',
    `order_amount` DECIMAL(10,2) COMMENT '订单金额',
    `discount_amount` DECIMAL(10,2) COMMENT '优惠金额',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_template_id` (`coupon_template_id`),
    KEY `idx_user_status_expire` (`user_id`, `status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券领取记录表';

-- ----------------------------
-- 22. 会员活动表 (sms_activity)
-- ----------------------------
DROP TABLE IF EXISTS `sms_activity`;
CREATE TABLE `sms_activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `type` TINYINT DEFAULT 1 COMMENT '类型: 1会员日活动 2积分兑换商品',
    `discount_rate` DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率',
    `point_cost` INT DEFAULT 0 COMMENT '兑换所需积分',
    `product_id` BIGINT DEFAULT NULL COMMENT '关联商品ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0停用',
    `rule_expression` VARCHAR(255) COMMENT '规则表达式',
    `points_multiplier` DECIMAL(4,2) DEFAULT 1.00 COMMENT '积分获取倍率',
    `description` VARCHAR(500) COMMENT '活动描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_status` (`type`, `status`),
    KEY `idx_start_end` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动表';

-- ----------------------------
-- 23. 用户行为轨迹表 (sys_user_behavior)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_behavior`;
CREATE TABLE `sys_user_behavior` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `behavior_type` TINYINT NOT NULL COMMENT '行为: 1点击 2收藏 3加购物车 4购买',
    `weight` INT NOT NULL COMMENT '权重分值',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_behavior_type` (`behavior_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为轨迹表';

-- ----------------------------
-- 24. 用户偏好分类表 (sys_user_preference)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_preference`;
CREATE TABLE `sys_user_preference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `category_id` BIGINT NOT NULL COMMENT '偏好分类ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_category` (`user_id`, `category_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好分类表';

-- ----------------------------
-- 25. 客服聊天表 (sys_chat_message)
-- ----------------------------
DROP TABLE IF EXISTS `sys_chat_message`;
CREATE TABLE `sys_chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送方ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收方ID',
    `order_no` VARCHAR(64) COMMENT '关联的订单编号',
    `content` TEXT NOT NULL COMMENT '聊天内容',
    `msg_type` TINYINT DEFAULT 1 COMMENT '消息类型: 1文字 2图片 3系统消息',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服聊天表';

-- ----------------------------
-- 26. 消息推送表 (sys_message_push)
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_push`;
CREATE TABLE `sys_message_push` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(50) NOT NULL COMMENT '消息类型',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT COMMENT '消息内容',
    `target_user_id` BIGINT COMMENT '目标用户ID (NULL表示广播)',
    `target_role` VARCHAR(50) COMMENT '目标角色',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0待发送 1已发送 2已读 3发送失败',
    `send_time` DATETIME COMMENT '发送时间',
    `read_time` DATETIME COMMENT '阅读时间',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `error_message` VARCHAR(500) COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_target_user` (`target_user_id`),
    KEY `idx_target_role` (`target_role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息推送表';

-- ----------------------------
-- 27. 操作日志表 (sys_log)
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT COMMENT '操作人ID',
    `username` VARCHAR(64) COMMENT '操作人用户名',
    `role` VARCHAR(20) COMMENT '角色(USER/ADMIN)',
    `module` VARCHAR(50) COMMENT '操作模块',
    `action` VARCHAR(255) COMMENT '具体操作描述',
    `ip_address` VARCHAR(50) COMMENT '操作IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 初始化数据：商品分类体系与 YOLO 映射
-- 基于 COCO 数据集的 YOLO 商品识别分类
-- =====================================================

-- 插入一级分类（大类）
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`) VALUES
(1, '超市/食品区', 0, 1, 'ShoppingCart', 1, 1),
(2, '家居百货/日用品', 0, 1, 'House', 2, 1),
(3, '服饰箱包', 0, 1, 'ShoppingBag', 3, 1),
(4, '数码电器', 0, 1, 'Monitor', 4, 1),
(5, '运动户外', 0, 1, 'Football', 5, 1);

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
(111, '三明治', 1, 2, NULL, 11, 1, 'sandwich', 48);

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
(209, '雨伞', 2, 2, NULL, 9, 1, 'umbrella', 25);

-- 插入二级分类（小类）- 服饰箱包 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(301, '双肩包', 3, 2, NULL, 1, 1, 'backpack', 24),
(302, '手提包', 3, 2, NULL, 2, 1, 'handbag', 26),
(303, '行李箱', 3, 2, NULL, 3, 1, 'suitcase', 28),
(304, '领带', 3, 2, NULL, 4, 1, 'tie', 27);

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
(409, '冰箱', 4, 2, NULL, 9, 1, 'refrigerator', 72);

-- 插入二级分类（小类）- 运动户外 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(501, '球类', 5, 2, NULL, 1, 1, 'sports ball', 32),
(502, '网球拍', 5, 2, NULL, 2, 1, 'tennis racket', 38),
(503, '滑板', 5, 2, NULL, 3, 1, 'skateboard', 36),
(504, '自行车', 5, 2, NULL, 4, 1, 'bicycle', 1);

-- YOLO 映射数据 - 超市/食品区
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
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
('sandwich', 111, 0.50, 1);

-- YOLO 映射数据 - 家居百货/日用品
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
('cup', 201, 0.50, 1),
('bowl', 202, 0.50, 1),
('book', 203, 0.50, 1),
('clock', 204, 0.50, 1),
('vase', 205, 0.50, 1),
('scissors', 206, 0.50, 1),
('toothbrush', 207, 0.50, 1),
('potted plant', 208, 0.50, 1),
('umbrella', 209, 0.50, 1);

-- YOLO 映射数据 - 服饰箱包
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
('backpack', 301, 0.50, 1),
('handbag', 302, 0.50, 1),
('suitcase', 303, 0.50, 1),
('tie', 304, 0.50, 1);

-- YOLO 映射数据 - 数码电器
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
('laptop', 401, 0.50, 1),
('mouse', 402, 0.50, 1),
('keyboard', 403, 0.50, 1),
('cell phone', 404, 0.50, 1),
('hair drier', 405, 0.50, 1),
('tv', 406, 0.50, 1),
('microwave', 407, 0.50, 1),
('toaster', 408, 0.50, 1),
('refrigerator', 409, 0.50, 1);

-- YOLO 映射数据 - 运动户外
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
('sports ball', 501, 0.50, 1),
('tennis racket', 502, 0.50, 1),
('skateboard', 503, 0.50, 1),
('bicycle', 504, 0.50, 1);
