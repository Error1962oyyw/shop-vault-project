package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.annotation.LogOperation;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.dto.EmailLoginDto;
import com.TsukasaChan.ShopVault.dto.EmailRegisterDto;
import com.TsukasaChan.ShopVault.dto.RefreshTokenDto;
import com.TsukasaChan.ShopVault.dto.ResetPasswordDto;
import com.TsukasaChan.ShopVault.dto.TokenResponseDto;
import com.TsukasaChan.ShopVault.infrastructure.VerificationService;
import com.TsukasaChan.ShopVault.security.CustomUserDetails;
import com.TsukasaChan.ShopVault.security.JwtUtils;
import com.TsukasaChan.ShopVault.security.LoginSecurityService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final VerificationService verificationService;
    private final LoginSecurityService loginSecurityService;
    private final StringRedisTemplate redisTemplate;

    private static final String REGISTER_RATE_PREFIX = "rate:register:";
    private static final String RESET_RATE_PREFIX = "rate:reset:";
    private static final int RATE_LIMIT_PER_MINUTE = 5;

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }

    private void checkRateLimit(String prefix, String key) {
        String rateKey = prefix + key;
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count != null && count == 1) {
            redisTemplate.expire(rateKey, 60, TimeUnit.SECONDS);
        }
        if (count != null && count > RATE_LIMIT_PER_MINUTE) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }
    }

    @LogOperation(module = "系统安全", action = "用户前台登录")
    @PostMapping("/login")
    public Result<TokenResponseDto> userLogin(@RequestBody EmailLoginDto dto) {
        try {
            if (loginSecurityService.isAccountLocked(dto.getEmail())) {
                return Result.error(423, "账户已锁定，请稍后再试");
            }

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );

            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) return Result.error(403, "管理员请从后台入口登录！");

            loginSecurityService.recordLoginSuccess(dto.getEmail());

            String realUsername = ((CustomUserDetails) auth.getPrincipal()).getUsername();
            String accessToken = jwtUtils.generateToken(realUsername);
            String refreshToken = jwtUtils.generateRefreshToken(realUsername);

            return Result.success(TokenResponseDto.of(
                    accessToken,
                    refreshToken,
                    jwtUtils.getExpiration(),
                    jwtUtils.getRefreshExpiration()
            ));
        } catch (UsernameNotFoundException e) {
            loginSecurityService.recordLoginFailure(dto.getEmail());
            return Result.error(401, "邮箱或密码错误");
        } catch (DisabledException e) {
            return Result.error(403, "您的账户已被暂停");
        } catch (BadCredentialsException e) {
            loginSecurityService.recordLoginFailure(dto.getEmail());
            return Result.error(401, "邮箱或密码错误");
        }
    }

    @LogOperation(module = "系统安全", action = "管理员后台登录")
    @PostMapping("/admin/login")
    public Result<TokenResponseDto> adminLogin(@RequestBody EmailLoginDto dto) {
        try {
            if (loginSecurityService.isAccountLocked(dto.getEmail())) {
                return Result.error(423, "账户已锁定，请稍后再试");
            }

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );

            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) return Result.error(403, "权限不足，非管理员账号！");

            loginSecurityService.recordLoginSuccess(dto.getEmail());

            String realUsername = ((CustomUserDetails) auth.getPrincipal()).getUsername();
            String accessToken = jwtUtils.generateToken(realUsername);
            String refreshToken = jwtUtils.generateRefreshToken(realUsername);

            return Result.success(TokenResponseDto.of(
                    accessToken,
                    refreshToken,
                    jwtUtils.getExpiration(),
                    jwtUtils.getRefreshExpiration()
            ));
        } catch (UsernameNotFoundException e) {
            loginSecurityService.recordLoginFailure(dto.getEmail());
            return Result.error(401, "邮箱或密码错误");
        } catch (DisabledException e) {
            return Result.error(403, "您的账户已被暂停");
        } catch (BadCredentialsException e) {
            loginSecurityService.recordLoginFailure(dto.getEmail());
            return Result.error(401, "邮箱或密码错误");
        }
    }

    @PostMapping("/refresh")
    public Result<TokenResponseDto> refreshToken(@RequestBody RefreshTokenDto dto) {
        if (!jwtUtils.validateRefreshToken(dto.getRefreshToken())) {
            return Result.error(401, "Refresh Token无效或已过期");
        }

        JwtUtils.RefreshResult refreshResult = jwtUtils.refreshTokens(dto.getRefreshToken());

        if (refreshResult == null) {
            return Result.error(401, "Token刷新失败，请重新登录");
        }

        return Result.success(TokenResponseDto.of(
                refreshResult.accessToken(),
                refreshResult.refreshToken(),
                jwtUtils.getExpiration(),
                jwtUtils.getRefreshExpiration()
        ));
    }

    @PostMapping("/logout")
    @LogOperation(module = "系统安全", action = "用户登出")
    public Result<String> logout(@RequestBody RefreshTokenDto dto, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            jwtUtils.blacklistToken(accessToken);
        }
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
    public Result<String> register(@Valid @RequestBody EmailRegisterDto dto, HttpServletRequest request) {
        checkRateLimit(REGISTER_RATE_PREFIX, getClientIp(request));
        if (verificationService.isCodeInvalid(dto.getEmail(), dto.getCode())) {
            return Result.error(400, "验证码错误或已过期");
        }
        userService.registerWithEmail(dto.getEmail(), dto.getPassword());
        return Result.success("注册成功！");
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDto dto, HttpServletRequest request) {
        checkRateLimit(RESET_RATE_PREFIX, getClientIp(request));
        userService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return Result.success("密码重置成功，请重新登录");
    }
}
