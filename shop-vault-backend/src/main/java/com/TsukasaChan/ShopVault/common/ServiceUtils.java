package com.TsukasaChan.ShopVault.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.function.Consumer;

public final class ServiceUtils {

    private ServiceUtils() {
    }

    public static <T> PageResult<T> queryPage(IService<T> service, Integer pageNum, Integer pageSize,
            Consumer<LambdaQueryWrapper<T>> wrapperConsumer) {
        Page<T> page = QueryHelper.createPage(pageNum, pageSize);
        LambdaQueryWrapper<T> wrapper = QueryHelper.build();
        if (wrapperConsumer != null) {
            wrapperConsumer.accept(wrapper);
        }
        Page<T> result = service.page(page, wrapper);
        return PageResultUtils.fromPage(result);
    }

    public static <T> List<T> queryList(IService<T> service, Consumer<LambdaQueryWrapper<T>> wrapperConsumer) {
        LambdaQueryWrapper<T> wrapper = QueryHelper.build();
        if (wrapperConsumer != null) {
            wrapperConsumer.accept(wrapper);
        }
        return service.list(wrapper);
    }

    public static <T> long queryCount(IService<T> service, Consumer<LambdaQueryWrapper<T>> wrapperConsumer) {
        LambdaQueryWrapper<T> wrapper = QueryHelper.build();
        if (wrapperConsumer != null) {
            wrapperConsumer.accept(wrapper);
        }
        return service.count(wrapper);
    }

    public static <T> boolean exists(IService<T> service, Consumer<LambdaQueryWrapper<T>> wrapperConsumer) {
        return queryCount(service, wrapperConsumer) > 0;
    }
}
