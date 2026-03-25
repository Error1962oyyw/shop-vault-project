package com.TsukasaChan.ShopVault.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PageUtils {

    private PageUtils() {
    }

    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        return result;
    }

    public static <T, R> PageResult<R> toPageResult(IPage<T> page, Function<T, R> converter) {
        PageResult<R> result = new PageResult<>();
        List<R> convertedRecords = page.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());
        result.setRecords(convertedRecords);
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        return result;
    }

    public static <T> PageResult<T> emptyPageResult() {
        PageResult<T> result = new PageResult<>();
        result.setRecords(List.of());
        result.setTotal(0L);
        result.setSize(10L);
        result.setCurrent(1L);
        return result;
    }
}
