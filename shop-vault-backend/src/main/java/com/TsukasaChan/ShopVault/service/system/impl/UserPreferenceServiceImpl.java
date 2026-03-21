package com.TsukasaChan.ShopVault.service.system.impl;

import com.TsukasaChan.ShopVault.entity.system.UserPreference;
import com.TsukasaChan.ShopVault.mapper.system.UserPreferenceMapper;
import com.TsukasaChan.ShopVault.service.system.UserPreferenceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserPreferences(Long userId, List<Long> categoryIds) {
        this.remove(new LambdaQueryWrapper<UserPreference>().eq(UserPreference::getUserId, userId));
        
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<UserPreference> preferences = categoryIds.stream()
                    .map(categoryId -> {
                        UserPreference pref = new UserPreference();
                        pref.setUserId(userId);
                        pref.setCategoryId(categoryId);
                        pref.setCreateTime(LocalDateTime.now());
                        return pref;
                    })
                    .collect(Collectors.toList());
            this.saveBatch(preferences);
        }
    }

    @Override
    public List<Long> getUserPreferenceCategoryIds(Long userId) {
        return this.list(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId))
                .stream()
                .map(UserPreference::getCategoryId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPreference(Long userId) {
        return this.count(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId)) > 0;
    }
}
