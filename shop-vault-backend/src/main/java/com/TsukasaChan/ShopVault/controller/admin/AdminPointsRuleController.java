package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.marketing.PointsRule;
import com.TsukasaChan.ShopVault.service.marketing.PointsRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/points-rules")
@RequiredArgsConstructor
public class AdminPointsRuleController extends BaseController {

    private final PointsRuleService pointsRuleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<PointsRule>> getAllRules() {
        return Result.success(pointsRuleService.getAllActiveRules());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PointsRule> getRuleById(@PathVariable Long id) {
        PointsRule rule = pointsRuleService.getById(id);
        if (rule == null) {
            return Result.error(404, "规则不存在");
        }
        return Result.success(rule);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PointsRule> createRule(@RequestBody PointsRule rule) {
        pointsRuleService.save(rule);
        return Result.success(rule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PointsRule> updateRule(@PathVariable Long id, @RequestBody PointsRule rule) {
        PointsRule existing = pointsRuleService.getById(id);
        if (existing == null) {
            return Result.error(404, "规则不存在");
        }
        rule.setId(id);
        pointsRuleService.updateById(rule);
        return Result.success(rule);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteRule(@PathVariable Long id) {
        PointsRule existing = pointsRuleService.getById(id);
        if (existing == null) {
            return Result.error(404, "规则不存在");
        }
        pointsRuleService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> toggleRuleStatus(@PathVariable Long id) {
        PointsRule existing = pointsRuleService.getById(id);
        if (existing == null) {
            return Result.error(404, "规则不存在");
        }
        existing.setIsActive(!existing.getIsActive());
        pointsRuleService.updateById(existing);
        return Result.success(existing.getIsActive() ? "规则已启用" : "规则已停用");
    }

    @PutMapping("/{id}/ratio")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updatePointsRatio(@PathVariable Long id, @RequestParam java.math.BigDecimal ratio) {
        PointsRule existing = pointsRuleService.getById(id);
        if (existing == null) {
            return Result.error(404, "规则不存在");
        }
        existing.setPointsRatio(ratio);
        pointsRuleService.updateById(existing);
        return Result.success("积分倍率更新成功");
    }
}
