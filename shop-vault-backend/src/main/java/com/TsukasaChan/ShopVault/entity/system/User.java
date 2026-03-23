package com.TsukasaChan.ShopVault.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@TableName(value ="sys_user")
@Data
public class User implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    private Integer gender;

    private LocalDate birthday;

    private BigDecimal balance;

    private Integer points;

    private Integer status;

    private String role;

    private LocalDateTime createTime;

    private Integer creditScore;

    private Integer isFirstLogin;

    private Integer preferenceSet;
}