package com.TsukasaChan.ShopVault.service.marketing.impl;

import com.TsukasaChan.ShopVault.entity.marketing.BalanceRecord;
import com.TsukasaChan.ShopVault.mapper.marketing.BalanceRecordMapper;
import com.TsukasaChan.ShopVault.service.marketing.BalanceRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceRecordServiceImpl extends ServiceImpl<BalanceRecordMapper, BalanceRecord> implements BalanceRecordService {

    @Override
    public void recordBalanceChange(Long userId, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String type, String description, Long relatedId) {
        BalanceRecord record = new BalanceRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setType(type);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        record.setCreateTime(LocalDateTime.now());
        this.save(record);
        log.info("余额交易记录: userId={}, amount={}, type={}, description={}", userId, amount, type, description);
    }

    @Override
    public List<BalanceRecord> getUserBalanceRecords(Long userId) {
        return this.list(new LambdaQueryWrapper<BalanceRecord>()
                .eq(BalanceRecord::getUserId, userId)
                .orderByDesc(BalanceRecord::getCreateTime));
    }
}
