package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.dto.EmailLoginDto;
import com.TsukasaChan.ShopVault.dto.EmailRegisterDto;
import com.TsukasaChan.ShopVault.dto.RefreshTokenDto;
import com.TsukasaChan.ShopVault.dto.ResetPasswordDto;
import com.TsukasaChan.ShopVault.dto.TokenResponseDto;
import com.TsukasaChan.ShopVault.infrastructure.VerificationService;
import com.TsukasaChan.ShopVault.security.JwtUtils;
import com.TsukasaChan.ShopVault.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final VerificationService verificationService;

    @LogOperation(module = "系统安全", action = "用户前台登录")
    @PostMapping("/login")
    public Result<TokenResponseDto> userLogin(@RequestBody EmailLoginDto dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return Result.error(403, "管理员请从后台入口登录！");

        String realUsername = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
        String accessToken = jwtUtils.generateToken(realUsername);
        String refreshToken = jwtUtils.generateRefreshToken(realUsername);

        return Result.success(TokenResponseDto.of(
                accessToken, 
                refreshToken, 
                jwtUtils.getExpiration(), 
                jwtUtils.getRefreshExpiration()
        ));
    }

    @LogOperation(module = "系统安全", action = "管理员后台登录")
    @PostMapping("/admin/login")
    public Result<TokenResponseDto> adminLogin(@RequestBody EmailLoginDto dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) return Result.error(403, "权限不足，非管理员账号！");

        String realUsername = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
        String accessToken = jwtUtils.generateToken(realUsername);
        String refreshToken = jwtUtils.generateRefreshToken(realUsername);

        return Result.success(TokenResponseDto.of(
                accessToken, 
                refreshToken, 
                jwtUtils.getExpiration(), 
                jwtUtils.getRefreshExpiration()
        ));
    }

    @PostMapping("/refresh")
    public Result<TokenResponseDto> refreshToken(@RequestBody RefreshTokenDto dto) {
        if (!jwtUtils.validateRefreshToken(dto.getRefreshToken())) {
            return Result.error(401, "Refresh Token无效或已过期");
        }

        String newAccessToken = jwtUtils.refreshAccessToken(dto.getRefreshToken());
        String newRefreshToken = jwtUtils.getNewRefreshToken(dto.getRefreshToken());

        if (newAccessToken == null || newRefreshToken == null) {
            return Result.error(401, "Token刷新失败，请重新登录");
        }

        return Result.success(TokenResponseDto.of(
                newAccessToken, 
                newRefreshToken, 
                jwtUtils.getExpiration(), 
                jwtUtils.getRefreshExpiration()
        ));
    }

    @PostMapping("/logout")
    @LogOperation(module = "系统安全", action = "用户登出")
    public Result<String> logout(@RequestBody RefreshTokenDto dto) {
        if (dto.getRefreshToken() != null) {
            jwtUtils.invalidateRefreshToken(dto.getRefreshToken());
        }
        return Result.success("登出成功");
    }

    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestParam String email) {
        verificationService.sendVerificationCode(email);
        return Result.success("验证码已发送至邮箱，请注意查收");
    }

    @LogOperation(module = "系统安全", action = "新用户邮箱注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody EmailRegisterDto dto) {
        if (verificationService.isCodeInvalid(dto.getEmail(), dto.getCode())) {
            return Result.error(400, "验证码错误或已过期");
        }
        userService.registerWithEmail(dto.getEmail(), dto.getPassword());
        return Result.success("注册成功！");
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody ResetPasswordDto dto) {
        userService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return Result.success("密码重置成功，请重新登录");
    }
}
