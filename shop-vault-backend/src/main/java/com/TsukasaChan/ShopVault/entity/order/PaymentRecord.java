package com.TsukasaChan.ShopVault.entity.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@TableName("oms_payment_record")
@Data
public class PaymentRecord implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_REFUNDED = 4;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String paymentNo;

    private String paymentMethod;

    private BigDecimal amount;

    private Integer pointsAmount;

    private Integer status;

    private String thirdPartyNo;

    private String errorMessage;

    private LocalDateTime paidTime;

    private LocalDateTime refundTime;

    private BigDecimal refundAmount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
