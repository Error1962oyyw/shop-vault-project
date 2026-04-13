package com.TsukasaChan.ShopVault.service.order;

import com.TsukasaChan.ShopVault.dto.AfterSalesApplyDto;
import com.TsukasaChan.ShopVault.dto.AfterSalesHandleDto;
import com.TsukasaChan.ShopVault.dto.ReturnLogisticsDto;
import com.TsukasaChan.ShopVault.entity.order.AfterSales;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AfterSalesService extends IService<AfterSales> {

    void applyAfterSales(AfterSalesApplyDto dto, Long userId);

    void handleAfterSales(AfterSalesHandleDto dto);

    void submitReturnLogistics(ReturnLogisticsDto dto, Long userId);

    void confirmReturn(String orderNo, boolean isAgree, String remark);

    void cancelAfterSales(String orderNo, Long userId);

    List<AfterSales> getMyAfterSalesList(Long userId);

    List<AfterSales> getAllAfterSalesList();

    List<AfterSales> getAfterSalesByStatus(Integer status);
}
