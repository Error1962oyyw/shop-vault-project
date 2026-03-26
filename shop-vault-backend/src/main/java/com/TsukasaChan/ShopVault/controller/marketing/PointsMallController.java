package com.TsukasaChan.ShopVault.controller.marketing;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.dto.PointsExchangeDto;
import com.TsukasaChan.ShopVault.entity.marketing.PointsProduct;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.service.marketing.PointsProductService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points-mall")
@RequiredArgsConstructor
public class PointsMallController {

    private final PointsProductService pointsProductService;
    private final UserService userService;

    @GetMapping("/products")
    public Result<List<PointsProduct>> getAvailableProducts() {
        List<PointsProduct> products = pointsProductService.getAvailableProducts();
        return Result.success(products);
    }

    @GetMapping("/products/{id}")
    public Result<PointsProduct> getProductDetail(@PathVariable Long id) {
        PointsProduct product = pointsProductService.getProductDetail(id);
        return Result.success(product);
    }

    @PostMapping("/exchange")
    public Result<String> exchangeProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PointsExchangeDto dto) {
        User user = userService.getByUsername(userDetails.getUsername());
        String result = pointsProductService.exchangeProduct(user.getId(), dto.getProductId());
        return Result.success(result);
    }
}
