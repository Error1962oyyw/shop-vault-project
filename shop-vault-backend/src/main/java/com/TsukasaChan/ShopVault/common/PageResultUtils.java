package com.TsukasaChan.ShopVault.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public final class PageResultUtils {

    private PageResultUtils() {
    }

    public static <T> PageResult<T> fromPage(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        return result;
    }

    public static <T> PageResult<T> of(List<T> records, long total, long size, long current) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setSize(size);
        result.setCurrent(current);
        return result;
    }

    public static <T> PageResult<T> empty(long size, long current) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(List.of());
        result.setTotal(0L);
        result.setSize(size);
        result.setCurrent(current);
        return result;
    }
}
