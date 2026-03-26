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

@TableName(value ="oms_order")
@Data
public class Order implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int ORDER_TYPE_NORMAL = 0;
    public static final int ORDER_TYPE_POINTS = 1;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    private String receiverSnapshot;

    private String trackingCompany;

    private String trackingNo;

    private LocalDateTime paymentTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime receiveTime;

    private LocalDateTime createTime;

    private LocalDateTime autoReceiveTime;

    private Integer isExtended;

    private LocalDateTime closeTime;

    private Integer pointsUsed;

    private BigDecimal pointsDiscount;

    private Long couponId;

    private BigDecimal couponDiscount;

    private Long skuId;

    private Integer orderType;

    private Integer isPointsExchange;

    private Integer afterSalesDisabled;
}
