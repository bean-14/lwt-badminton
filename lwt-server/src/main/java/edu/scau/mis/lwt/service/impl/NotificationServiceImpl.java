package edu.scau.mis.lwt.service.impl;

import edu.scau.mis.lwt.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 通知服务实现
 * <p>
 * 核心组件说明：
 * 1. StringRedisTemplate — 与 Redis 交互，存取字符串类型数据。
 *    这里用 Redis 记录用户在线状态（user_online:{userId}），
 *    以及 MQ 消费的幂等标识（notify_record:{bookingId}）。
 * 2. SimpMessagingTemplate — Spring 提供的 WebSocket 消息发送工具。
 *    向指定 STOMP 频道（如 /topic/coach/5）发送消息，
 *    所有订阅该频道的 WebSocket 客户端都会实时收到。
 * <p>
 * STOMP 订阅地址约定：
 *   - 教练端：/topic/coach/{coachId}    → 接收新预约通知
 *   - 学员端：/topic/student/{studentId} → 接收确认成功通知
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String ONLINE_PREFIX = "user_online:";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean isUserOnline(Long userId) {
        // Redis GET user_online:5 → 返回 "1" 表示在线，null 表示不在线
        String val = redisTemplate.opsForValue().get(ONLINE_PREFIX + userId);
        return "1".equals(val);
    }

    @Override
    public void setUserOnline(Long userId) {
        // Redis SET user_online:5 "1" EX 86400 → 写入在线标记，24小时后自动过期
        redisTemplate.opsForValue().set(ONLINE_PREFIX + userId, "1", 1, TimeUnit.DAYS);
    }

    @Override
    public void setUserOffline(Long userId) {
        // Redis DEL user_online:5 → 删除在线标记
        redisTemplate.delete(ONLINE_PREFIX + userId);
    }

    /**
     * 向教练推送新预约通知
     * convertAndSend 的作用：
     *   将消息（HashMap）自动转为 JSON，发送到指定 STOMP 频道。
     * 频道 /topic/coach/{coachId}：
     *   教练前端需订阅此频道，例如 Stomp.subscribe("/topic/coach/5", callback)。
     * 消息格式（前端收到的 JSON）：
     *   {"type":"BOOKING_NEW", "bookingId":1001, "studentName":"张三", "timestamp":...}
     */
    @Override
    public void notifyCoachNewBooking(Long coachId, Long bookingId, String studentName) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "BOOKING_NEW");
        message.put("bookingId", bookingId);
        message.put("studentName", studentName);
        message.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/coach/" + coachId, message);
        log.info("WebSocket pushed to coach {}: new booking {}", coachId, bookingId);
    }

    /**
     * 向学员推送确认通知
     * 教练确认预约后调用此方法，通知学员端"教练已确认，预约成功！"。
     * 学员前端收到后可以自动刷新页面数据。
     */
    @Override
    public void notifyStudentConfirmed(Long studentId, Long bookingId, String coachName) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "BOOKING_CONFIRMED");
        message.put("bookingId", bookingId);
        message.put("coachName", coachName);
        message.put("message", "教练已确认，预约成功！");
        message.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/student/" + studentId, message);
        log.info("WebSocket pushed to student {}: booking confirmed {}", studentId, bookingId);
    }
}
