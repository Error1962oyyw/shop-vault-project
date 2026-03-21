package com.TsukasaChan.ShopVault.entity.marketing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sms_points_record")
public class PointsRecord implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer points;

    private Integer balanceAfter;

    private String type;

    private String description;

    private Long relatedId;

    private LocalDateTime expireTime;

    private Boolean isExpired;

    private LocalDateTime createTime;

    public static final String TYPE_SIGN_IN = "SIGN_IN";
    public static final String TYPE_PURCHASE = "PURCHASE";
    public static final String TYPE_REVIEW = "REVIEW";
    public static final String TYPE_EXCHANGE = "EXCHANGE";
    public static final String TYPE_REFUND_DEDUCT = "REFUND_DEDUCT";
    public static final String TYPE_EXPIRE = "EXPIRE";
    public static final String TYPE_ADMIN_ADJUST = "ADMIN_ADJUST";
}
