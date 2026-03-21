package com.TsukasaChan.ShopVault.controller.system;

import com.TsukasaChan.ShopVault.common.Result;
import com.TsukasaChan.ShopVault.websocket.WebSocketEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketStatusController {

    private final WebSocketEventListener eventListener;

    @GetMapping("/online-count")
    public Result<Integer> getOnlineCount() {
        return Result.success(eventListener.getOnlineCount());
    }
}
