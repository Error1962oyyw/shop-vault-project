package com.TsukasaChan.ShopVault.entity.marketing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sms_points_product")
public class PointsProduct implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int TYPE_SMALL_PRODUCT = 1;
    public static final int TYPE_COUPON = 2;
    public static final int TYPE_VIP_MONTHLY = 3;
    public static final int TYPE_VIP_YEARLY = 4;

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String image;

    private String images;

    private String mainImage;

    private String thumbnail;

    private Integer type;

    private Integer pointsCost;

    private Integer stock;

    private Integer dailyLimit;

    private Integer totalLimit;

    private BigDecimal originalPrice;

    private Long relatedId;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer remainStock;
}
