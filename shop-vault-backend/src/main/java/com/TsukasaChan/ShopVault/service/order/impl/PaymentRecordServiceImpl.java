package com.TsukasaChan.ShopVault.service.order.impl;

import com.TsukasaChan.ShopVault.entity.order.PaymentRecord;
import com.TsukasaChan.ShopVault.mapper.order.PaymentRecordMapper;
import com.TsukasaChan.ShopVault.service.order.PaymentRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentRecordService {

    @Override
    public PaymentRecord createPaymentRecord(Long orderId, String orderNo, Long userId, 
            String paymentMethod, BigDecimal amount, Integer pointsAmount) {
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(orderId);
        record.setOrderNo(orderNo);
        record.setUserId(userId);
        record.setPaymentNo("PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        record.setPaymentMethod(paymentMethod);
        record.setAmount(amount);
        record.setPointsAmount(pointsAmount != null ? pointsAmount : 0);
        record.setStatus(PaymentRecord.STATUS_PENDING);
        record.setCreateTime(LocalDateTime.now());
        save(record);
        return record;
    }

    @Override
    public PaymentRecord getByOrderNo(String orderNo) {
        return lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo)
                .orderByDesc(PaymentRecord::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public boolean updatePaymentSuccess(Long paymentId, String thirdPartyNo) {
        PaymentRecord record = getById(paymentId);
        if (record == null) {
            return false;
        }
        record.setStatus(PaymentRecord.STATUS_SUCCESS);
        record.setThirdPartyNo(thirdPartyNo);
        record.setPaidTime(LocalDateTime.now());
        return updateById(record);
    }

    @Override
    public boolean updatePaymentFailed(Long paymentId, String errorMessage) {
        PaymentRecord record = getById(paymentId);
        if (record == null) {
            return false;
        }
        record.setStatus(PaymentRecord.STATUS_FAILED);
        record.setErrorMessage(errorMessage);
        return updateById(record);
    }

    @Override
    public boolean updatePaymentRefunded(Long paymentId, BigDecimal refundAmount) {
        PaymentRecord record = getById(paymentId);
        if (record == null) {
            return false;
        }
        record.setStatus(PaymentRecord.STATUS_REFUNDED);
        record.setRefundAmount(refundAmount);
        record.setRefundTime(LocalDateTime.now());
        return updateById(record);
    }
}
