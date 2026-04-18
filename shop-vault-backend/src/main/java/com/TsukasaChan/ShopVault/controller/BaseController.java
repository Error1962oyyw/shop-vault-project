package com.TsukasaChan.ShopVault.controller;

import com.TsukasaChan.ShopVault.common.SecurityUtils;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.service.system.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseController {

    protected UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    protected User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录或登录已过期");
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("找不到当前登录用户信息");
        }
        return user;
    }

    protected Long getCurrentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录或登录已过期");
        }
        return userId;
    }

    protected Long getOptionalUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}
