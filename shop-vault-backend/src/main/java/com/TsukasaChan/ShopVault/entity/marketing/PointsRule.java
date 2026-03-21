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
@TableName("sms_points_rule")
public class PointsRule implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleCode;

    private String ruleName;

    private String description;

    private Integer pointsValue;

    private BigDecimal pointsRatio;

    private Integer ruleType;

    private Integer dailyLimit;

    private Boolean isActive;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static final int TYPE_SIGN_IN = 1;
    public static final int TYPE_PURCHASE = 2;
    public static final int TYPE_REVIEW = 3;
    public static final int TYPE_SHARE = 4;
    public static final int TYPE_FIRST_PURCHASE = 5;
}
