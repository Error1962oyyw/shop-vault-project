package com.TsukasaChan.ShopVault.service.order;

import com.TsukasaChan.ShopVault.entity.order.PaymentRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface PaymentRecordService extends IService<PaymentRecord> {

    PaymentRecord createPaymentRecord(Long orderId, String orderNo, Long userId, 
            String paymentMethod, BigDecimal amount, Integer pointsAmount);

    PaymentRecord getByOrderNo(String orderNo);

    boolean updatePaymentSuccess(Long paymentId, String thirdPartyNo);

    boolean updatePaymentFailed(Long paymentId, String errorMessage);

    boolean updatePaymentRefunded(Long paymentId, BigDecimal refundAmount);
}
