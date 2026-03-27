package com.TsukasaChan.ShopVault.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品信息表
 */
@Data
@TableName("pms_product")
public class Product implements Serializable {


    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    @TableField(exist = false)
    private Long parentCategoryId;

    private String name;

    private String subTitle;

    private String mainImage;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private Integer stockWarning;

    /**
     * 状态 1:上架 0:下架
     */
    private Integer status;

    private Integer sales;

    @TableField(exist = false)
    private String description;

    /**
     * 商品详情(富文本)
     */
    private String detailHtml;
    
    /**
     * 商品详情图片(JSON数组)
     */
    private String detailImages;

    private LocalDateTime createTime;

    private Integer version;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private Integer cocoId;

    @TableField(exist = false)
    private String yoloLabel;
}
