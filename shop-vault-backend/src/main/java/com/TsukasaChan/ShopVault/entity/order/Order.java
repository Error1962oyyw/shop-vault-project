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
    public static final int ORDER_TYPE_VIP = 1;
    public static final int ORDER_TYPE_SVIP = 2;
    public static final int ORDER_TYPE_POINTS_EXCHANGE = 3;

    public static final int STATUS_PENDING_PAYMENT = 0;
    public static final int STATUS_PENDING_DELIVERY = 1;
    public static final int STATUS_PENDING_RECEIVE = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_CLOSED = 4;
    public static final int STATUS_AFTER_SALES = 5;

    public static final String PAYMENT_METHOD_BALANCE = "BALANCE";
    public static final String PAYMENT_METHOD_POINTS = "POINTS";
    public static final String PAYMENT_METHOD_DIRECT = "DIRECT";
    public static final String PAYMENT_METHOD_COMBO = "COMBO";
    public static final String PAYMENT_METHOD_ALIPAY = "ALIPAY";
    public static final String PAYMENT_METHOD_WECHAT = "WECHAT";

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer pointsAmount;

    private Integer status;

    private Integer orderType;

    private String paymentMethod;

    private String receiverSnapshot;

    private String trackingCompany;

    private String trackingNo;

    private LocalDateTime paymentTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime receiveTime;

    private LocalDateTime autoReceiveTime;

    private LocalDateTime expireTime;

    private Integer isExtended;

    private LocalDateTime closeTime;

    private String closeReason;

    private Integer pointsUsed;

    private BigDecimal pointsDiscount;

    private Long couponId;

    private BigDecimal couponDiscount;

    private BigDecimal vipDiscount;

    private Integer discountDisabled;

    private Long skuId;

    private Long relatedId;

    private Integer isPointsExchange;

    private Integer afterSalesDisabled;

    private String remark;

    @TableField(value = "is_deleted")
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String productImage;
}
