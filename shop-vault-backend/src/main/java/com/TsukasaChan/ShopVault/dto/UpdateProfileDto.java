package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileDto {

    private String nickname;

    private String phone;

    private String avatar;

    private Integer gender;

    private LocalDate birthday;
}
