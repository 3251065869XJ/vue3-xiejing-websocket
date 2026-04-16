import com.example.entity.DataRecord;
import com.example.repository.DataRecordRepository;
import com.example.manager.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataUpdatePushService {

    private final DataRecordRepository dataRecordRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SessionManager sessionManager;

    /**
     * 每 30 秒执行一次数据更新检查
     * fixedRate = 30000 表示两次任务开始执行的时间间隔为 30 秒
     */
    @Scheduled(fixedRate = 30000)
    public void checkAndPushUpdates() {
        // 注意：实际场景中需要从在线用户列表中获取所有需要检查的用户
        // 这里演示单个用户推送逻辑，前端连接时会发送 userAccount 和 areaId
        log.info("开始执行定时数据更新检查...");
    }

    /**
     * 根据前端传入的参数检查并推送更新
     * 该方法由前端 WebSocket 消息触发注册
     */
    public void registerUserForPush(String userAccount, String areaId) {
        LocalDateTime latestUpdateTime = dataRecordRepository
                .findLatestUpdateTime(userAccount, areaId);

        LocalDateTime lastKnownTime = sessionManager.getLastUpdateTime(userAccount);

        if (latestUpdateTime != null) {
            if (lastKnownTime == null || latestUpdateTime.isAfter(lastKnownTime)) {
                // 数据有更新，推送消息
                Map<String, Object> message = new HashMap<>();
                message.put("type", "DATA_UPDATED");
                message.put("userAccount", userAccount);
                message.put("areaId", areaId);
                message.put("latestUpdateTime", latestUpdateTime.toString());
                message.put("message", "您有新的数据更新，请刷新查看");

                // 向指定用户推送点对点消息
                messagingTemplate.convertAndSendToUser(
                        userAccount,
                        "/queue/updates",
                        message
                );

                // 更新记录的最新时间
                sessionManager.updateLastUpdateTime(userAccount, latestUpdateTime);
                log.info("向用户 {} 推送更新通知，最新更新时间：{}", userAccount, latestUpdateTime);
            }
        }
    }
}