package com.TsukasaChan.ShopVault.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@TableName(value ="pms_favorite")
@Data
public class Favorite implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long productId;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String productImage;

    @TableField(exist = false)
    private BigDecimal price;
}