package com.TsukasaChan.ShopVault.service.system;

import com.TsukasaChan.ShopVault.entity.system.UserPreference;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserPreferenceService extends IService<UserPreference> {

    void saveUserPreferences(Long userId, List<Long> categoryIds);

    List<Long> getUserPreferenceCategoryIds(Long userId);

    boolean hasPreference(Long userId);
}
