package com.TsukasaChan.ShopVault.service.marketing;

import com.TsukasaChan.ShopVault.entity.marketing.BalanceRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface BalanceRecordService extends IService<BalanceRecord> {

    void recordBalanceChange(Long userId, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String type, String description, Long relatedId);

    List<BalanceRecord> getUserBalanceRecords(Long userId);
}
