package com.TsukasaChan.ShopVault.service.system;

import com.TsukasaChan.ShopVault.entity.system.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface UserService extends IService<User> {
    void registerWithEmail(String email, String password);

    void updateProfile(Long userId, User updateInfo);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    void resetPassword(String email, String code, String newPassword);

    boolean isFirstLogin(Long userId);

    void completeFirstLogin(Long userId);

    void completeOnboarding(Long userId, List<Long> preferredCategoryIds);

    boolean hasCompletedOnboarding(Long userId);

    User getByUsername(String username);

    boolean updateBalanceWithLock(Long userId, BigDecimal amount);
}
