package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.marketing.Activity;
import com.TsukasaChan.ShopVault.service.marketing.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController extends BaseController {

    private final ActivityService activityService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Activity>> getActivityList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(activityService.getActivityPage(pageNum, pageSize, type, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Activity> getActivityById(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error(404, "活动不存在");
        }
        return Result.success(activity);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Activity> createActivity(@RequestBody Activity activity) {
        activityService.save(activity);
        return Result.success(activity);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Activity> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        Activity existing = activityService.getById(id);
        if (existing == null) {
            return Result.error(404, "活动不存在");
        }
        activity.setId(id);
        activityService.updateById(activity);
        return Result.success(activity);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteActivity(@PathVariable Long id) {
        Activity existing = activityService.getById(id);
        if (existing == null) {
            return Result.error(404, "活动不存在");
        }
        activityService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateActivityStatus(@PathVariable Long id, @RequestParam Integer status) {
        Activity existing = activityService.getById(id);
        if (existing == null) {
            return Result.error(404, "活动不存在");
        }
        existing.setStatus(status);
        activityService.updateById(existing);
        return Result.success(status == 1 ? "活动已启用" : "活动已停用");
    }

    @GetMapping("/member-days")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Activity>> getMemberDayActivities() {
        return Result.success(activityService.getMemberDayActivities());
    }
}
