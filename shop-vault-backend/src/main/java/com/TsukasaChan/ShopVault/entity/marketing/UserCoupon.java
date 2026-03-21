package com.TsukasaChan.ShopVault.entity.marketing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@TableName(value ="sms_user_coupon")
@Data
public class UserCoupon implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long activityId;

    private Long couponTemplateId;

    private Integer status;

    private LocalDateTime getTime;

    private LocalDateTime useTime;

    private LocalDateTime expireTime;

    private Long orderId;

    private BigDecimal orderAmount;

    private BigDecimal discountAmount;

    @TableField(exist = false)
    private CouponTemplate couponTemplate;
}