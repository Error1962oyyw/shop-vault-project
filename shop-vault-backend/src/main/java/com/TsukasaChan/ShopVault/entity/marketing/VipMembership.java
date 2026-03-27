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
@TableName("sms_vip_membership")
public class VipMembership implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int TYPE_MONTHLY = 1;
    public static final int TYPE_YEARLY = 2;
    public static final int TYPE_SVIP_YEARLY = 3;

    public static final int LEVEL_VIP = 1;
    public static final int LEVEL_SVIP = 2;

    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_EXPIRED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type;

    private Integer vipLevel;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private Integer pointsCost;

    private String source;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private UserVipInfo userVipInfo;
}
