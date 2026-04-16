import com.example.service.DataUpdatePushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final DataUpdatePushService pushService;

    /**
     * 接收前端注册请求，处理 userAccount 和 areaId
     */
    @MessageMapping("/register")
    @SendToUser("/queue/registered")
    public Map<String, Object> registerUser(@Payload Map<String, String> payload) {
        String userAccount = payload.get("userAccount");
        String areaId = payload.get("areaId");

        log.info("收到用户注册请求：userAccount={}, areaId={}", userAccount, areaId);

        // 注册用户以接收定时推送
        pushService.registerUserForPush(userAccount, areaId);

        return Map.of(
                "status", "success",
                "message", "用户已注册成功，将开始接收数据更新推送",
                "userAccount", userAccount
        );
    }
}