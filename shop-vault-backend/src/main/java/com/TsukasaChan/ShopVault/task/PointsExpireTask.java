package com.TsukasaChan.ShopVault.task;

import com.TsukasaChan.ShopVault.entity.marketing.PointsRecord;
import com.TsukasaChan.ShopVault.entity.system.MessagePush;
import com.TsukasaChan.ShopVault.entity.system.User;
import com.TsukasaChan.ShopVault.service.marketing.PointsRecordService;
import com.TsukasaChan.ShopVault.service.system.MessagePushService;
import com.TsukasaChan.ShopVault.service.system.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointsExpireTask {

    private final PointsRecordService pointsRecordService;
    private final UserService userService;
    private final MessagePushService messagePushService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void processExpiredPoints() {
        log.info("开始执行积分过期处理任务...");
        LocalDateTime now = LocalDateTime.now();

        List<PointsRecord> expiredRecords = pointsRecordService.list(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getIsExpired, false)
                .isNotNull(PointsRecord::getExpireTime)
                .lt(PointsRecord::getExpireTime, now)
                .gt(PointsRecord::getPoints, 0));

        if (expiredRecords.isEmpty()) {
            log.info("没有需要处理的过期积分");
            return;
        }

        Map<Long, Integer> userExpiredPoints = expiredRecords.stream()
                .collect(Collectors.groupingBy(PointsRecord::getUserId,
                        Collectors.summingInt(PointsRecord::getPoints)));

        for (Map.Entry<Long, Integer> entry : userExpiredPoints.entrySet()) {
            Long userId = entry.getKey();
            Integer expiredPoints = entry.getValue();

            User user = userService.getById(userId);
            if (user != null && user.getPoints() > 0) {
                int newPoints = Math.max(0, user.getPoints() - expiredPoints);
                user.setPoints(newPoints);
                userService.updateById(user);

                PointsRecord expireRecord = new PointsRecord();
                expireRecord.setUserId(userId);
                expireRecord.setPoints(-Math.min(expiredPoints, user.getPoints()));
                expireRecord.setType(PointsRecord.TYPE_EXPIRE);
                expireRecord.setDescription("积分过期自动扣除");
                expireRecord.setIsExpired(true);
                pointsRecordService.save(expireRecord);

                messagePushService.pushToUser(userId, 
                        MessagePush.TYPE_POINTS_EXPIRE,
                        "积分过期通知",
                        "您有" + expiredPoints + "积分已过期，当前积分余额：" + newPoints);

                log.info("用户 {} 过期积分 {} 点，剩余 {} 点", userId, expiredPoints, newPoints);
            }
        }

        pointsRecordService.update(new LambdaUpdateWrapper<PointsRecord>()
                .in(PointsRecord::getId, expiredRecords.stream().map(PointsRecord::getId).toList())
                .set(PointsRecord::getIsExpired, true));

        log.info("积分过期处理完成，共处理 {} 条记录", expiredRecords.size());
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void sendExpireReminder() {
        log.info("开始发送积分即将过期提醒...");
        LocalDateTime warningTime = LocalDateTime.now().plusDays(7);

        List<PointsRecord> soonExpireRecords = pointsRecordService.list(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getIsExpired, false)
                .isNotNull(PointsRecord::getExpireTime)
                .between(PointsRecord::getExpireTime, LocalDateTime.now(), warningTime)
                .gt(PointsRecord::getPoints, 0));

        Map<Long, List<PointsRecord>> userRecordsMap = soonExpireRecords.stream()
                .collect(Collectors.groupingBy(PointsRecord::getUserId));

        for (Map.Entry<Long, List<PointsRecord>> entry : userRecordsMap.entrySet()) {
            Long userId = entry.getKey();
            List<PointsRecord> records = entry.getValue();
            int totalPoints = records.stream().mapToInt(PointsRecord::getPoints).sum();
            LocalDateTime earliestExpire = records.stream()
                    .map(PointsRecord::getExpireTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(warningTime);

            messagePushService.pushToUser(userId,
                    MessagePush.TYPE_POINTS_EXPIRE,
                    "积分即将过期提醒",
                    "您有" + totalPoints + "积分将于" + earliestExpire.toLocalDate() + "过期，请尽快使用！");

            log.info("用户 {} 有 {} 积分即将在{}过期", userId, totalPoints, earliestExpire.toLocalDate());
        }

        log.info("积分过期提醒发送完成，共 {} 个用户", userRecordsMap.size());
    }
}
