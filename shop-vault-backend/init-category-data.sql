-- =====================================================
-- 商品分类初始化脚本
-- 基于 COCO 数据集的 YOLO 商品识别分类
-- =====================================================

-- 清空现有数据（可选，如果需要重新初始化）
-- TRUNCATE TABLE pms_category;
-- TRUNCATE TABLE sys_yolo_mapping;

-- 插入一级分类（大类）
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`) VALUES
(1, '超市/食品区', 0, 1, 'ShoppingCart', 1, 1),
(2, '家居百货/日用品', 0, 1, 'House', 2, 1),
(3, '服饰箱包', 0, 1, 'ShoppingBag', 3, 1),
(4, '数码电器', 0, 1, 'Monitor', 4, 1),
(5, '运动户外', 0, 1, 'Football', 5, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

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
(111, '三明治', 1, 2, NULL, 11, 1, 'sandwich', 48)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

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
(209, '雨伞', 2, 2, NULL, 9, 1, 'umbrella', 25)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

-- 插入二级分类（小类）- 服饰箱包 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(301, '双肩包', 3, 2, NULL, 1, 1, 'backpack', 24),
(302, '手提包', 3, 2, NULL, 2, 1, 'handbag', 26),
(303, '行李箱', 3, 2, NULL, 3, 1, 'suitcase', 28),
(304, '领带', 3, 2, NULL, 4, 1, 'tie', 27)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

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
(409, '冰箱', 4, 2, NULL, 9, 1, 'refrigerator', 72)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

-- 插入二级分类（小类）- 运动户外 (含 COCO 编号和 YOLO 标签)
INSERT INTO `pms_category` (`id`, `name`, `parent_id`, `level`, `icon`, `sort`, `status`, `yolo_label`, `coco_id`) VALUES
(501, '球类', 5, 2, NULL, 1, 1, 'sports ball', 32),
(502, '网球拍', 5, 2, NULL, 2, 1, 'tennis racket', 38),
(503, '滑板', 5, 2, NULL, 3, 1, 'skateboard', 36),
(504, '自行车', 5, 2, NULL, 4, 1, 'bicycle', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `yolo_label` = VALUES(`yolo_label`), `coco_id` = VALUES(`coco_id`);

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
('sandwich', 111, 0.50, 1)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`);

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
('umbrella', 209, 0.50, 1)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`);

-- YOLO 映射数据 - 服饰箱包
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
('backpack', 301, 0.50, 1),
('handbag', 302, 0.50, 1),
('suitcase', 303, 0.50, 1),
('tie', 304, 0.50, 1)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`);

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
('refrigerator', 409, 0.50, 1)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`);

-- YOLO 映射数据 - 运动户外
INSERT INTO `sys_yolo_mapping` (`yolo_label`, `category_id`, `confidence_threshold`, `is_active`) VALUES
('sports ball', 501, 0.50, 1),
('tennis racket', 502, 0.50, 1),
('skateboard', 503, 0.50, 1),
('bicycle', 504, 0.50, 1)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`);

-- 验证插入结果
SELECT '一级分类数量:' AS info, COUNT(*) AS count FROM pms_category WHERE level = 1
UNION ALL
SELECT '二级分类数量:' AS info, COUNT(*) AS count FROM pms_category WHERE level = 2
UNION ALL
SELECT 'YOLO映射数量:' AS info, COUNT(*) AS count FROM sys_yolo_mapping;
