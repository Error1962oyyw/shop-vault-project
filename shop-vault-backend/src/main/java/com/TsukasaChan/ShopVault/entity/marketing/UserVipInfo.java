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
@TableName("sms_user_vip_info")
public class UserVipInfo implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    public static final int LEVEL_NORMAL = 0;
    public static final int LEVEL_VIP = 1;
    public static final int LEVEL_SVIP = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer vipLevel;

    private BigDecimal discountRate;

    private LocalDateTime vipExpireTime;

    private Integer totalVipDays;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
