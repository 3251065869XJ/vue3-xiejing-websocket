import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    // 存储工号与对应上次推送时间的映射
    private final Map<String, LocalDateTime> lastUpdateTimeMap = new ConcurrentHashMap<>();

    public LocalDateTime getLastUpdateTime(String userAccount) {
        return lastUpdateTimeMap.get(userAccount);
    }

    public void updateLastUpdateTime(String userAccount, LocalDateTime updateTime) {
        lastUpdateTimeMap.put(userAccount, updateTime);
    }
}