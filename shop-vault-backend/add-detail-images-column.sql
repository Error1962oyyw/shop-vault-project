-- 添加商品详情图片字段到商品表
-- 执行此脚本前请确保数据库已备份

-- 添加 detail_images 字段
ALTER TABLE `pms_product` 
ADD COLUMN `detail_images` TEXT COMMENT '商品详情图片(JSON数组)' AFTER `detail_html`;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'pms_product' 
AND COLUMN_NAME = 'detail_images';
