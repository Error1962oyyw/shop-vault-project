package com.TsukasaChan.ShopVault.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Result<String>> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("密码错误: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "邮箱或密码错误"));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Result<String>> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.warn("用户不存在: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "邮箱或密码错误"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Result<String>> handleDisabledException(DisabledException e) {
        log.warn("账户已暂停: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, "您的账户已被暂停"));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Result<String>> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException e) {
        Throwable cause = unwrapCause(e);
        
        if (cause instanceof UsernameNotFoundException) {
            log.warn("认证失败-用户不存在: {}", cause.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "邮箱或密码错误"));
        }
        if (cause instanceof DisabledException) {
            log.warn("认证失败-账户暂停: {}", cause.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, "您的账户已被暂停"));
        }
        if (cause instanceof BadCredentialsException) {
            log.warn("认证失败-密码错误: {}", cause.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "邮箱或密码错误"));
        }
        
        log.error("认证服务异常: ", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, "登录失败，请重试"));
    }

    private Throwable unwrapCause(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    @ExceptionHandler({AuthorizationDeniedException.class, org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<Result<String>> handleAccessDeniedException(Exception e) {
        log.warn("权限拦截: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error(403, "对不起，您的权限不足，无法访问该接口！"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<String>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", errorMsg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, errorMsg));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<String>> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", errorMsg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, errorMsg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<String>> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error(500, "操作失败，请稍后再试"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e) {
        log.error("系统发生异常: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error(500, "服务器内部错误，请稍后再试"));
    }
}
