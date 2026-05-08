package edu.scau.mis.lwt.service;

/**
 * 通知服务接口
 * <p>
 * 负责两类通知的发送：
 * 1. WebSocket 实时推送（当用户在线时，直接推送到浏览器）
 * 2. 在线状态管理（通过 Redis 记录哪些用户在线，避免给离线用户发无用推送）
 * <p>
 * 这里的"通知"指的是内存中的实时推送（WebSocket），
 * 和 MQ（RabbitMQ）不同：MQ 是跨服务、可持久化、保证最终送达的异步消息。
 * 两者是互补关系：WebSocket 追求"快"，MQ 追求"稳"。
 */
public interface NotificationService {

    /**
     * 判断用户是否在线
     * 通过查询 Redis key "user_online:{userId}" 是否存在且值为 "1"。
     * 用户在以下情况被标记为在线：
     *   - 登录成功时（AuthController 设置）
     *   - 前端主动调用 POST /user/online（用户打开页面时）
     * 用户在以下情况被标记为离线：
     *   - WebSocket 连接断开时（WebSocketEventListener 自动清除）
     *   - 前端主动调用 POST /user/offline（用户关闭页面时）
     */
    boolean isUserOnline(Long userId);

    /**
     * 标记用户在线
     * 在 Redis 中写入 "user_online:{userId}" = "1"，有效期 1 天。
     */
    void setUserOnline(Long userId);

    /**
     * 标记用户离线
     * 从 Redis 中删除 "user_online:{userId}"。
     */
    void setUserOffline(Long userId);

    /**
     * 推送新预约通知给教练（WebSocket）
     * 向 STOMP 频道 /topic/coach/{coachId} 发送消息。
     * 教练前端如果订阅了此频道，会实时收到弹窗。
     */
    void notifyCoachNewBooking(Long coachId, Long bookingId, String studentName);

    /**
     * 推送确认成功通知给学员（WebSocket）
     * 向 STOMP 频道 /topic/student/{studentId} 发送消息。
     * 学员前端收到后可以自动刷新页面，显示"教练已确认"。
     */
    void notifyStudentConfirmed(Long studentId, Long bookingId, String coachName);
}
