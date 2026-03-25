package com.TsukasaChan.ShopVault.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.function.Consumer;

public final class QueryHelper {

    private QueryHelper() {
    }

    public static <T> Page<T> createPage(Integer pageNum, Integer pageSize) {
        int pn = pageNum != null && pageNum > 0 ? pageNum : 1;
        int ps = pageSize != null && pageSize > 0 ? pageSize : 10;
        return new Page<>(pn, ps);
    }

    public static <T> LambdaQueryWrapper<T> build() {
        return new LambdaQueryWrapper<>();
    }

    public static <T> LambdaQueryWrapper<T> build(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        consumer.accept(wrapper);
        return wrapper;
    }

    public static <T> LambdaQueryWrapper<T> eqIfPresent(LambdaQueryWrapper<T> wrapper, Object value, 
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> column) {
        if (value != null) {
            if (value instanceof String && ((String) value).isBlank()) {
                return wrapper;
            }
            wrapper.eq(column, value);
        }
        return wrapper;
    }

    public static <T> LambdaQueryWrapper<T> likeIfPresent(LambdaQueryWrapper<T> wrapper, String value,
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> column) {
        if (value != null && !value.isBlank()) {
            wrapper.like(column, value);
        }
        return wrapper;
    }

    public static <T> LambdaQueryWrapper<T> inIfPresent(LambdaQueryWrapper<T> wrapper, 
            java.util.Collection<?> values,
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> column) {
        if (values != null && !values.isEmpty()) {
            wrapper.in(column, values);
        }
        return wrapper;
    }
}
