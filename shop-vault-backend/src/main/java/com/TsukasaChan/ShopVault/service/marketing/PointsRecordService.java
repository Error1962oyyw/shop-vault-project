package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PointsRecordService extends IService<PointsRecord> {

    void addPoints(Long userId, Integer points, String type, String description, Long relatedId);

    void addPointsWithExpiry(Long userId, Integer points, String type, String description, Long relatedId, int expireDays);

    Integer signIn(Long userId);

    boolean todaySigned(Long userId);
}
