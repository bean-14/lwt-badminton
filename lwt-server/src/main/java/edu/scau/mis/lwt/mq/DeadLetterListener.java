package edu.scau.mis.lwt.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 死信队列消费者
 * <p>
 * 什么是死信队列（DLQ）？
 *   当一条消息被消费者反复处理失败（超过最大重试次数），
 *   它会被投递到一个特殊的队列——死信队列。
 *   死信队列里的消息不会被自动丢弃，而是等待管理员人工排查。
 * <p>
 * 什么情况下会进入死信队列？
 *   微信 API 连续 3 次都返回错误（如 access_token 过期、网络不通等）。
 *   MQ 重试 3 次都失败后，消息从 booking.notify.queue 转移到 booking.notify.dlq。
 * <p>
 * 管理员看到死信队列的日志后，应当：
 *   1. 检查微信配置（app-id、app-secret、template-id）是否正确
 *   2. 检查微信服务是否正常
 *   3. 修复问题后，可以手动将消息重新投递到原队列，或直接在数据库中补发通知
 */
@Component
@RabbitListener(queues = "booking.notify.dlq")
public class DeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);

    @RabbitHandler
    public void handleDeadLetter(BookingMessage message) {
        log.error("====== DEAD LETTER QUEUE ======");
        log.error("Booking notification failed after all retries: bookingId={}, coachId={}, studentId={}",
                message.getBookingId(), message.getCoachId(), message.getStudentId());
        log.error("This message requires manual intervention: check WeChat API status, then retry or notify admin.");
        log.error("====== END ======");
    }
}
