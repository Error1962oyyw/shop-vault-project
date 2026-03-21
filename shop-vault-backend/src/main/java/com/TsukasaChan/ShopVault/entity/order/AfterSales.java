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

@Data
@TableName(value ="oms_after_sales")
public class AfterSales implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Integer type;

    private String reason;

    private String description;

    private String images;

    private Integer status;

    private BigDecimal refundAmount;

    private Integer refundPoints;

    private String returnLogisticsCompany;

    private String returnLogisticsNo;

    private LocalDateTime returnLogisticsTime;

    private String merchantReply;

    private LocalDateTime merchantHandleTime;

    private String adminRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static final int TYPE_REFUND_ONLY = 1;
    public static final int TYPE_RETURN_REFUND = 2;
    public static final int TYPE_EXCHANGE = 3;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_RETURNING = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_CANCELLED = 5;
}
