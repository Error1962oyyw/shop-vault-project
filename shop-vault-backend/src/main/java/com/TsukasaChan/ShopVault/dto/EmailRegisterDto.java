package com.TsukasaChan.ShopVault.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmailRegisterDto {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 128, message = "密码长度必须在8-128个字符之间")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
    private String nickname;
}
