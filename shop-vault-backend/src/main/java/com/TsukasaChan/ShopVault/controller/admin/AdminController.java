package com.TsukasaChan.ShopVault.controller.admin;

import com.TsukasaChan.ShopVault.common.PageResult;
import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.controller.BaseController;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.TsukasaChan.ShopVault.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController extends BaseController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Map<String, Object>>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword
    ) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword)
            );
        }

        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> pageResult = userService.page(page, wrapper);

        PageResult<Map<String, Object>> result = new PageResult<>();
        result.setRecords(pageResult.getRecords().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("nickname", user.getNickname());
            map.put("email", user.getEmail());
            map.put("phone", user.getPhone());
            map.put("avatar", user.getAvatar());
            map.put("role", user.getRole());
            map.put("status", user.getStatus());
            map.put("points", user.getPoints());
            map.put("balance", user.getBalance());
            map.put("createTime", user.getCreateTime());
            return map;
        }).toList());
        result.setTotal(pageResult.getTotal());
        result.setSize(pageResult.getSize());
        result.setCurrent(pageResult.getCurrent());

        return Result.success(result);
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status
    ) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if ("ADMIN".equals(user.getRole())) {
            return Result.error("无法修改管理员账号状态");
        }

        user.setStatus(status);
        userService.updateById(user);
        return Result.success(status == 1 ? "用户已启用" : "用户已暂停");
    }

    @PostMapping("/users/{userId}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> adjustUserData(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> params
    ) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Integer pointsChange = params.get("pointsChange") != null
            ? ((Number) params.get("pointsChange")).intValue() : 0;
        BigDecimal balanceChange = params.get("balanceChange") != null
            ? new BigDecimal(params.get("balanceChange").toString()) : BigDecimal.ZERO;

        if (pointsChange != 0) {
            int updated = userService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<User>()
                            .setSql("points = GREATEST(0, points + " + pointsChange + ")")
                            .eq(User::getId, userId));
            if (updated == 0) {
                return Result.error("积分调整失败");
            }
        }

        if (balanceChange.compareTo(BigDecimal.ZERO) != 0) {
            int updated = userService.getBaseMapper().update(null,
                    new LambdaUpdateWrapper<User>()
                            .setSql("balance = GREATEST(0, balance + {0})", balanceChange)
                            .eq(User::getId, userId));
            if (updated == 0) {
                return Result.error("余额调整失败");
            }
        }

        return Result.success("调整成功");
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteUser(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            return Result.error("无法删除管理员账号");
        }
        userService.removeById(userId);
        return Result.success("用户删除成功");
    }
}
