package edu.scau.mis.lwt.service;

import edu.scau.mis.lwt.mq.BookingMessage;

/**
 * 微信服务通知接口
 * <p>
 * 调用微信公众平台的"订阅消息"接口，向学员发送服务通知。
 * 微信订阅消息类似于"服务通知"，会在用户的微信消息列表中展示。
 * <p>
 * 这是在 WebSocket 推送之外的第二重保障：
 * 如果学员不在线（没打开页面，收不到 WebSocket），
 * 微信消息可以作为离线通知触达用户。
 * <p>
 * 注意：使用前需要在微信公众平台申请模板消息权限，
 * 并在 application.yml 中配置 app-id、app-secret、template-id。
 */
public interface WeChatNotificationService {

    /**
     * 发送新预约的通知给教练（微信服务通知）
     * @param message 包含 bookingId, coachId, studentId
     */
    void sendBookingNotification(BookingMessage message);
}
