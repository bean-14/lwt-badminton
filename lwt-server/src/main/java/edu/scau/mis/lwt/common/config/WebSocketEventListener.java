package edu.scau.mis.lwt.common.config;

import edu.scau.mis.lwt.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket 断开事件监听器
 * <p>
 * 当用户关闭浏览器、切换页面或网络断开时，WebSocket 连接会断开。
 * 此时应该把用户在 Redis 中的"在线状态"清除，避免后续预约判断出错
 * （比如学员预约时检查教练在线，结果教练早就断线了，Redis 里还标记着"在线"）。
 */
@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * 监听 WebSocket 断开事件
     * SessionDisconnectEvent 是 Spring 在 WebSocket 连接断开时自动发布的事件。
     * 这里通过 event.getUser() 拿到断开的用户 ID，然后从 Redis 清除在线标记。
     */
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        String userId = event.getUser() != null ? event.getUser().getName() : null;
        if (userId != null) {
            notificationService.setUserOffline(Long.valueOf(userId));
            log.info("User {} disconnected from WebSocket", userId);
        }
    }
}
