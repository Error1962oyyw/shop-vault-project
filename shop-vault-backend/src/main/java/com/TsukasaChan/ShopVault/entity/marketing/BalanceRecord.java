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
@TableName("sms_balance_record")
public class BalanceRecord implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private String type;

    private String description;

    private Long relatedId;

    private LocalDateTime createTime;

    public static final String TYPE_RECHARGE = "RECHARGE";
    public static final String TYPE_PURCHASE = "PURCHASE";
    public static final String TYPE_VIP_PURCHASE = "VIP_PURCHASE";
    public static final String TYPE_REFUND = "REFUND";
    public static final String TYPE_ADMIN_ADJUST = "ADMIN_ADJUST";
    public static final String TYPE_WITHDRAW = "WITHDRAW";
}
