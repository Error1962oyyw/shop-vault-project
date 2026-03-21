package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.dto.DashboardStatsDto;
import com.TsukasaChan.ShopVault.manager.DashboardManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardManager dashboardManager;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public Result<DashboardStatsDto> getStats() {
        return Result.success(dashboardManager.getFullStats());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/realtime")
    public Result<DashboardStatsDto> getRealtimeStats() {
        return Result.success(dashboardManager.getFullStats());
    }
}
