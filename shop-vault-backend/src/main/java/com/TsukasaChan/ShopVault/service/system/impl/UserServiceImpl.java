package com.TsukasaChan.ShopVault.service.system.impl;

import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.infrastructure.VerificationService;
import com.TsukasaChan.ShopVault.mapper.system.UserMapper;
import com.TsukasaChan.ShopVault.service.system.UserPreferenceService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final UserPreferenceService userPreferenceService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void registerWithEmail(String email, String password) {
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (count > 0) {
            throw new RuntimeException("该邮箱已被注册");
        }

        User user = new User();
        user.setUsername("sv_user_" + cn.hutool.core.util.RandomUtil.randomNumbers(8));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname("小铺用户_" + cn.hutool.core.util.RandomUtil.randomString(4));
        user.setRole("USER");
        user.setPoints(0);
        user.setBalance(new BigDecimal("0.00"));
        user.setCreditScore(100);
        user.setIsFirstLogin(1);
        user.setPreferenceSet(0);

        this.save(user);
    }

    @Override
    public void updateProfile(Long userId, User updateInfo) {
        User user = getById(userId);

        if (updateInfo.getNickname() != null) {
            user.setNickname(updateInfo.getNickname());
        }
        if (updateInfo.getAvatar() != null) {
            user.setAvatar(updateInfo.getAvatar());
        }
        if (updateInfo.getEmail() != null) {
            user.setEmail(updateInfo.getEmail());
        }
        if (updateInfo.getPhone() != null) {
            user.setPhone(updateInfo.getPhone());
        }
        if (updateInfo.getGender() != null) {
            user.setGender(updateInfo.getGender());
        }
        if (updateInfo.getBirthday() != null) {
            user.setBirthday(updateInfo.getBirthday());
        }

        updateById(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        if (verificationService.isCodeInvalid(email, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new RuntimeException("该邮箱尚未注册");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public boolean isFirstLogin(Long userId) {
        User user = getById(userId);
        return user != null && user.getIsFirstLogin() != null && user.getIsFirstLogin() == 1;
    }

    @Override
    public void completeFirstLogin(Long userId) {
        User user = getById(userId);
        if (user != null) {
            user.setIsFirstLogin(0);
            updateById(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOnboarding(Long userId, List<Long> preferredCategoryIds) {
        if (preferredCategoryIds != null && !preferredCategoryIds.isEmpty()) {
            userPreferenceService.saveUserPreferences(userId, preferredCategoryIds);
        }
        
        User user = getById(userId);
        if (user != null) {
            user.setIsFirstLogin(0);
            user.setPreferenceSet(1);
            updateById(user);
        }
    }

    @Override
    public boolean hasCompletedOnboarding(Long userId) {
        User user = getById(userId);
        return user != null && user.getPreferenceSet() != null && user.getPreferenceSet() == 1;
    }

    @Override
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
}
