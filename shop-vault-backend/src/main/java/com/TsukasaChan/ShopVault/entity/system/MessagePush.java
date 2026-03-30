package com.TsukasaChan.ShopVault.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_message_push")
public class MessagePush implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String title;

    private String content;

    private Long targetUserId;

    private String targetRole;

    private Integer status;

    private LocalDateTime sendTime;

    private LocalDateTime readTime;

    private Integer retryCount;

    private String errorMessage;

    private String linkUrl;

    private Long relatedId;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_READ = 2;
    public static final int STATUS_FAILED = 3;

    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_POINTS_EXPIRE = "POINTS_EXPIRE";
    public static final String TYPE_ORDER_STATUS = "ORDER_STATUS";
    public static final String TYPE_PROMOTION = "PROMOTION";
    
    public static final String TYPE_ADMIN_PURCHASE = "ADMIN_PURCHASE";
    public static final String TYPE_ADMIN_AFTER_SALES = "ADMIN_AFTER_SALES";
    public static final String TYPE_ADMIN_COMMENT = "ADMIN_COMMENT";
    public static final String TYPE_ADMIN_CHAT = "ADMIN_CHAT";
}
