package com.TsukasaChan.ShopVault.dto;

import lombok.Data;

@Data
public class TokenResponseDto {

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private Long refreshExpiresIn;

    private String tokenType;

    public static TokenResponseDto of(String accessToken, String refreshToken, Long expiresIn, Long refreshExpiresIn) {
        TokenResponseDto dto = new TokenResponseDto();
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(refreshToken);
        dto.setExpiresIn(expiresIn);
        dto.setRefreshExpiresIn(refreshExpiresIn);
        dto.setTokenType("Bearer");
        return dto;
    }
}
