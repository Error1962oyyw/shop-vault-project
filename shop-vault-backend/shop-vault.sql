SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表 (sys_user)
-- 支持游客注册、会员管理、余额与积分
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(64) NOT NULL COMMENT '用户名',
                            `password` varchar(100) NOT NULL COMMENT '加密密码',
                            `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
                            `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                            `gender` tinyint(1) DEFAULT '0' COMMENT '性别: 0未知 1男 2女',
                            `birthday` date DEFAULT NULL COMMENT '生日',
                            `balance` decimal(10,2) DEFAULT '0.00' COMMENT '钱包余额(模拟支付用)',
                            `points` int(11) DEFAULT '0' COMMENT '当前积分',
                            `status` tinyint(1) DEFAULT '1' COMMENT '状态 1:正常 0:冻结',
                            `role` varchar(20) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                            `credit_score` int(11) DEFAULT '100' COMMENT '信誉分(影响售后审批)',
                            `is_first_login` TINYINT(1) DEFAULT 1 COMMENT '是否首次登录',
                            `preference_set` TINYINT(1) DEFAULT 0 COMMENT '是否已设置偏好',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 收货地址表 (sys_address)
-- ----------------------------
DROP TABLE IF EXISTS `sys_address`;
CREATE TABLE `sys_address` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                               `receiver_name` varchar(64) NOT NULL COMMENT '收货人姓名',
                               `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
                               `province` varchar(64) DEFAULT NULL COMMENT '省',
                               `city` varchar(64) DEFAULT NULL COMMENT '市',
                               `region` varchar(64) DEFAULT NULL COMMENT '区',
                               `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
                               `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认地址:0否 1是',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ----------------------------
-- 3. 商品分类表 (pms_category)
-- ----------------------------
DROP TABLE IF EXISTS `pms_category`;
CREATE TABLE `pms_category` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `name` varchar(64) NOT NULL COMMENT '分类名称 (如: 数码, 服装)',
                                `parent_id` bigint(20) DEFAULT '0' COMMENT '父分类ID (0为一级分类)',
                                `level` int(1) DEFAULT '1' COMMENT '层级 (1:一级 2:二级)',
                                `icon` varchar(255) DEFAULT NULL COMMENT '图标URL',
                                `sort` int(11) DEFAULT '0' COMMENT '排序权重',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ----------------------------
-- 4. YOLO视觉映射表 (sys_yolo_mapping)
-- [核心创新点]: 将YOLO识别出的英文标签映射到系统的商品分类
-- ----------------------------
DROP TABLE IF EXISTS `sys_yolo_mapping`;
CREATE TABLE `sys_yolo_mapping` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `yolo_label` varchar(64) NOT NULL COMMENT 'YOLO模型输出的标签 (如: cup, backpack)',
                                    `category_id` bigint(20) NOT NULL COMMENT '关联的系统分类ID',
                                    `confidence_threshold` decimal(4,2) DEFAULT '0.50' COMMENT '置信度阈值 (过滤低可信度识别)',
                                    `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用映射',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_yolo` (`yolo_label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI视觉标签映射表';

-- ----------------------------
-- 5. 商品信息表 (pms_product)
-- ----------------------------
DROP TABLE IF EXISTS `pms_product`;
CREATE TABLE `pms_product` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `category_id` bigint(20) NOT NULL COMMENT '分类ID',
                               `name` varchar(255) NOT NULL COMMENT '商品名称',
                               `sub_title` varchar(255) DEFAULT NULL COMMENT '副标题/卖点',
                               `main_image` varchar(500) NOT NULL COMMENT '主图',
                               `price` decimal(10,2) NOT NULL COMMENT '销售价格',
                               `stock` int(11) NOT NULL COMMENT '库存数量',
                               `stock_warning` int(11) DEFAULT '10' COMMENT '库存预警阈值',
                               `status` tinyint(1) DEFAULT '1' COMMENT '状态 1:上架 0:下架',
                               `sales` int(11) DEFAULT '0' COMMENT '销量 (用于热销推荐)',
                               `detail_html` text COMMENT '商品详情(富文本)',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `version` INT DEFAULT 0 COMMENT '版本号(乐观锁)',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ----------------------------
-- 6. 购物车表 (oms_cart_item)
-- ----------------------------
DROP TABLE IF EXISTS `oms_cart_item`;
CREATE TABLE `oms_cart_item` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                 `product_id` bigint(20) NOT NULL COMMENT '商品ID',
                                 `quantity` int(11) DEFAULT '1' COMMENT '购买数量',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `sku_id` BIGINT COMMENT 'SKU ID',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ----------------------------
-- 7. 订单主表 (oms_order)
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `order_no` varchar(64) NOT NULL COMMENT '订单编号(唯一)',
                             `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                             `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
                             `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额 (扣除优惠后)',
                             `status` int(2) DEFAULT '0' COMMENT '状态: 0待付款 1待发货 2待收货 3已完成(待评价) 4已关闭 5售后中',
                             `receiver_snapshot` text COMMENT '收货人信息快照(JSON格式，防止地址修改影响旧订单)',
                             `tracking_company` varchar(64) DEFAULT NULL COMMENT '物流公司',
                             `tracking_no` varchar(64) DEFAULT NULL COMMENT '物流单号',
                             `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
                             `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
                             `receive_time` datetime DEFAULT NULL COMMENT '确认收货时间',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `auto_receive_time` datetime DEFAULT NULL COMMENT '自动收货时间(发货后+10天)',
                             `is_extended` tinyint(1) DEFAULT '0' COMMENT '是否已延长收货: 0否 1是',
                             `close_time` datetime DEFAULT NULL COMMENT '订单关闭时间',
                             `points_used` INT DEFAULT 0 COMMENT '使用积分数量',
                             `points_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分抵扣金额',
                             `coupon_id` BIGINT COMMENT '使用的优惠券ID',
                             `coupon_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券抵扣金额',
                             `sku_id` BIGINT COMMENT 'SKU ID(如果购买的是SKU商品)',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- ----------------------------
-- 8. 订单明细表 (oms_order_item)
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
                                  `order_no` varchar(64) DEFAULT NULL COMMENT '订单编号',
                                  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
                                  `product_name` varchar(255) NOT NULL COMMENT '商品名称',
                                  `product_img` varchar(500) DEFAULT NULL COMMENT '商品图片',
                                  `product_price` decimal(10,2) NOT NULL COMMENT '购买时的单价',
                                  `quantity` int(11) DEFAULT '1' COMMENT '购买数量',
                                  `sku_id` BIGINT COMMENT 'SKU ID',
                                  `spec_json` VARCHAR(500) COMMENT '规格JSON快照',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品详情表';

-- ----------------------------
-- 9. 积分记录表 (sms_points_record)
-- [核心创新点]: 实现“交易+会员”闭环，记录积分的获取与消耗
-- ----------------------------
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

-- ----------------------------
-- 10. 会员活动/积分商城表 (sms_activity)
-- ----------------------------
DROP TABLE IF EXISTS `sms_activity`;
CREATE TABLE `sms_activity` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `name` varchar(100) NOT NULL COMMENT '活动名称 (如: 2月会员日)',
                                `start_time` datetime NOT NULL COMMENT '开始时间',
                                `end_time` datetime NOT NULL COMMENT '结束时间',
                                `type` tinyint(1) DEFAULT '1' COMMENT '类型: 1折扣活动 2积分兑换商品',
                                `discount_rate` decimal(3,2) DEFAULT '1.00' COMMENT '折扣率 (如0.85)',
                                `point_cost` int(11) DEFAULT '0' COMMENT '兑换所需积分',
                                `product_id` bigint(20) DEFAULT NULL COMMENT '关联商品ID (若是兑换活动)',
                                `status` tinyint(1) DEFAULT '1' COMMENT '状态 1启用 0停用',
                                `rule_expression` varchar(255) DEFAULT NULL COMMENT '规则表达式(如: 8代表每月8号, 4代表周四)',
                                `points_multiplier` decimal(4,2) DEFAULT '1.00' COMMENT '积分获取倍率(如 2.0 代表双倍积分)',
                                `description` VARCHAR(500) COMMENT '活动描述',
                                `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动与积分商城表';

-- ----------------------------
-- 11. 商品评价表 (pms_comment)
-- [数据联动]: 评价数据用于协同过滤算法分析
-- ----------------------------
DROP TABLE IF EXISTS `pms_comment`;
CREATE TABLE `pms_comment` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `order_id` bigint(20) NOT NULL COMMENT '订单ID',
                               `product_id` bigint(20) NOT NULL COMMENT '商品ID',
                               `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                               `star` decimal(2,1) DEFAULT '5.0' COMMENT '星级 1.0-5.0',
                               `content` text COMMENT '评价内容',
                               `images` text COMMENT '评价图片(JSON数组)',
                               `audit_status` tinyint(1) DEFAULT '1' COMMENT '状态: 1正常展示 2被管理员删除',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `likes` int(11) DEFAULT '0' COMMENT '点赞数',
                               `dislikes` int(11) DEFAULT '0' COMMENT '点踩数',
                               `is_reported` tinyint(1) DEFAULT '0' COMMENT '是否被举报: 0否 1是',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- [新增] 12. 操作日志表 (sys_log)
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `user_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
                           `username` varchar(64) DEFAULT NULL COMMENT '操作人用户名',
                           `role` varchar(20) DEFAULT NULL COMMENT '角色(USER/ADMIN)',
                           `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
                           `action` varchar(255) DEFAULT NULL COMMENT '具体操作描述',
                           `ip_address` varchar(50) DEFAULT NULL COMMENT '操作IP',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- [新增] 13. 客服聊天表 (sys_chat_message)
-- ----------------------------
DROP TABLE IF EXISTS `sys_chat_message`;
CREATE TABLE `sys_chat_message` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `sender_id` bigint(20) NOT NULL COMMENT '发送方ID (用户或客服)',
                                    `receiver_id` bigint(20) NOT NULL COMMENT '接收方ID (客服或用户)',
                                    `order_no` varchar(64) DEFAULT NULL COMMENT '关联的订单编号(可选)',
                                    `content` text NOT NULL COMMENT '聊天内容/图片URL',
                                    `msg_type` tinyint(1) DEFAULT '1' COMMENT '消息类型: 1文字 2图片 3系统消息',
                                    `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读: 0未读 1已读',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售前/售后客服聊天表';

-- ----------------------------
-- [新增] 14. 售后服务表 (oms_after_sales)
-- ----------------------------
DROP TABLE IF EXISTS `oms_after_sales`;
CREATE TABLE `oms_after_sales` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `order_no` varchar(64) NOT NULL COMMENT '关联订单号',
                                   `user_id` bigint(20) NOT NULL COMMENT '申请人',
                                   `reason` varchar(500) NOT NULL COMMENT '申请售后的原因/诉求',
                                   `images` text COMMENT '凭证图片(JSON数组)',
                                   `status` tinyint(1) DEFAULT '0' COMMENT '状态: 0待商家处理 1商家同意 2商家拒绝 3已完成 4已撤销',
                                   `merchant_reply` varchar(500) DEFAULT NULL COMMENT '商家的回复/拒绝理由',
                                   `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT '实际退款金额',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间',
                                   `type` INT DEFAULT 1 COMMENT '售后类型:1仅退款2退货退款3换货',
                                   `description` VARCHAR(500) COMMENT '问题描述',
                                   `refund_points` INT COMMENT '需扣回的积分',
                                   `return_logistics_company` VARCHAR(50) COMMENT '退货物流公司',
                                   `return_logistics_no` VARCHAR(100) COMMENT '退货物流单号',
                                   `return_logistics_time` DATETIME COMMENT '退货物流填写时间',
                                   `merchant_handle_time` DATETIME COMMENT '商家处理时间',
                                   `admin_remark` VARCHAR(500) COMMENT '管理员备注',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_after_sales_order` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后服务记录表';

-- 15. 新增：商品收藏表
DROP TABLE IF EXISTS `pms_favorite`;
CREATE TABLE `pms_favorite` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                `product_id` bigint(20) NOT NULL COMMENT '商品ID',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户商品收藏表';

-- 16. 新增：用户行为轨迹表 (为后续完善的协同过滤算法提供数据支持)
DROP TABLE IF EXISTS `sys_user_behavior`;
CREATE TABLE `sys_user_behavior` (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                     `product_id` bigint(20) NOT NULL COMMENT '商品ID',
                                     `behavior_type` tinyint(1) NOT NULL COMMENT '行为: 1点击 2收藏 3加购物车 4购买',
                                     `weight` int(11) NOT NULL COMMENT '权重分值 (如点击1分，购买5分)',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为轨迹表';

-- 17. 用户优惠券/福利领取表 (sms_user_coupon)
DROP TABLE IF EXISTS `sms_user_coupon`;
CREATE TABLE `sms_user_coupon` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                   `activity_id` bigint(20) NOT NULL COMMENT '关联的活动ID(即优惠券模板)',
                                   `status` tinyint(1) DEFAULT '0' COMMENT '状态: 0未使用 1已使用 2已过期',
                                   `get_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
                                   `use_time` datetime DEFAULT NULL COMMENT '使用时间',
                                   `expire_time` datetime NOT NULL COMMENT '过期时间',
                                   `coupon_template_id` BIGINT COMMENT '优惠券模板ID',
                                   `order_id` BIGINT COMMENT '使用的订单ID',
                                   `order_amount` DECIMAL(10,2) COMMENT '订单金额',
                                   `discount_amount` DECIMAL(10,2) COMMENT '优惠金额',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券领取记录表';

-- 18. 用户偏好分类表
DROP TABLE IF EXISTS `sys_user_preference`;
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

-- 19. 积分规则配置表
DROP TABLE IF EXISTS `sms_points_rule`;
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

-- 20. 商品规格表
DROP TABLE IF EXISTS `pms_spec`;
CREATE TABLE IF NOT EXISTS `pms_spec` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '规格名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表';

-- 21. 商品规格值表
DROP TABLE IF EXISTS `pms_spec_value`;
CREATE TABLE IF NOT EXISTS `pms_spec_value` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `spec_id` BIGINT NOT NULL COMMENT '规格ID',
    `value` VARCHAR(100) NOT NULL COMMENT '规格值',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_spec_id` (`spec_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格值表';

-- 22. 商品SKU表
DROP TABLE IF EXISTS `pms_product_sku`;
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

-- 23. 优惠券模板表
DROP TABLE IF EXISTS `sms_coupon_template`;
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

-- 24. 创建缺失的 sys_message_push 表
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

-- 1. 初始化积分规则数据
INSERT INTO `sms_points_rule` (`rule_code`, `rule_name`, `description`, `points_value`, `points_ratio`, `rule_type`, `daily_limit`, `is_active`, `sort_order`) VALUES
('SIGN_IN', '每日签到', '每日签到获得积分', 10, NULL, 1, 1, 1, 1),
('PURCHASE_REWARD', '购物奖励', '购物消费获得积分,1元=100积分', NULL, 100.0000, 2, 0, 1, 2),
('REWARD_REVIEW', '评价奖励', '完成商品评价获得积分', 20, NULL, 3, 5, 1, 3),
('FIRST_PURCHASE', '首购奖励', '首次购物额外奖励', 100, NULL, 5, 0, 1, 4),
('SHARE_REWARD', '分享奖励', '分享商品获得积分', 5, NULL, 4, 3, 1, 5);

-- 2. 初始化YOLO标签映射数据
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




-- 3. 索引优化
CREATE INDEX `idx_order_status_create` ON `oms_order` (`status`, `create_time`);
CREATE INDEX `idx_order_user_status` ON `oms_order` (`user_id`, `status`);
CREATE INDEX `idx_after_sales_status` ON `oms_after_sales` (`status`, `create_time`);
CREATE INDEX `idx_points_record_user_expire` ON `sms_points_record` (`user_id`, `expire_time`, `is_expired`);
CREATE INDEX `idx_user_preference_user` ON `sys_user_preference` (`user_id`);
-- 11. 索引优化
CREATE INDEX `idx_product_sku_product` ON `pms_product_sku` (`product_id`, `status`);
CREATE INDEX `idx_coupon_template_status` ON `sms_coupon_template` (`status`, `valid_start_time`, `valid_end_time`);
CREATE INDEX `idx_user_coupon_user_status` ON `sms_user_coupon` (`user_id`, `status`, `expire_time`);



