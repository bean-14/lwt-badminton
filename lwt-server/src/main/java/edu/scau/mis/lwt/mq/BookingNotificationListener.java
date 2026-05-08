package edu.scau.mis.lwt.mq;

import edu.scau.mis.lwt.service.WeChatNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ 消费者 — 异步通知处理器（Phase 2）
 * <p>
 * 这个类监听 booking.notify.queue 队列，负责发送微信服务通知。
 * 它是预约流程中的"异步第二环"：
 * - Phase 1（BookingServiceImpl.book）：写入 DB → 发 MQ 消息 → 立即返回
 * - Phase 2（本类）：收到 MQ 消息 → 发送微信通知
 * <p>
 * 为什么要异步发微信？
 *   微信 API 可能因网络问题变慢或超时，如果在预约接口里同步调用，
 *   学员要等微信返回才能看到"预约成功"，体验很差。
 *   异步化后，预约接口毫秒级返回，微信发送由后台慢慢处理。
 * <p>
 * 消息可靠性保证：
 *   1. 幂等性（Redis） — 防止 MQ 重复投递导致发两次微信
 *   2. 重试机制 — 微信接口失败后，Spring 自动重试 3 次（指数退避）
 *   3. 死信队列 — 重试 3 次仍然失败，消息转入 DLQ，人工处理
 */
@Component
@RabbitListener(queues = "booking.notify.queue")
public class BookingNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(BookingNotificationListener.class);

    /**
     * Redis 幂等键前缀
     * Key 格式：notify_record:{bookingId}
     * Value 格式："success"（表示已处理成功）
     * TTL：24 小时（一天内的重复消息都被过滤）
     */
    private static final String NOTIFY_RECORD_PREFIX = "notify_record:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WeChatNotificationService weChatNotificationService;

    /**
     * 处理预约通知消息
     * <p>
     * 执行流程：
     * 1. 幂等性检查：查询 Redis notify_record:{bookingId}
     *    - 存在 → 已处理过，直接返回（MQ 自动 ACK）
     *    - 不存在 → 继续处理
     * 2. 发送微信服务通知
     *    - 成功 → 写入 Redis notify_record:{bookingId}=success（标记已处理）
     *    - 失败 → 抛出异常 → Spring 重试
     * <p>
     * 注意：Redis 标记只在发送成功后才写入，这样重试时可以重新发送。
     * 如果先写入 Redis 再发微信，失败后虽然删除 Redis key，
     * 但万一删除失败（Redis 宕机），key 残留导致重复消息被跳过（通知丢失）。
     * "先成功、再标记"的策略优先保证"不丢失"，而非"不重复"。
     */
    @RabbitHandler
    public void handleBookingNew(BookingMessage message) {
        log.info("MQ consumer received message: {}", message);

        String recordKey = NOTIFY_RECORD_PREFIX + message.getBookingId();

        // 1. 幂等性校验：查询 Redis 是否已处理过
        String processed = redisTemplate.opsForValue().get(recordKey);
        if (processed != null) {
            log.info("Booking {} already processed (status: {}), ACK and skip", message.getBookingId(), processed);
            return;
        }

        // 2. 发送微信服务通知
        //    注意：此处不在发送前 SET Redis，只有发送成功后才记录。
        //    这样如果发送失败抛异常，MQ 重试时可以重新尝试发送。
        try {
            weChatNotificationService.sendBookingNotification(message);
        } catch (Exception e) {
            log.error("Failed to send WeChat notification for bookingId: {}, will retry (attempts left)",
                    message.getBookingId(), e);
            // 抛出异常 → Spring 触发重试机制
            // 配置：最多重试 3 次（含首次），间隔 3s → 6s → 10s
            // 3 次均失败 → 消息进入死信队列 booking.notify.dlq
            throw e;
        }

        // 3. 发送成功，记录幂等 Token，有效期 24 小时
        //    SET notify_record:{bookingId} "success" EX 86400
        redisTemplate.opsForValue().set(recordKey, "success", 1, TimeUnit.DAYS);
        log.info("Booking notification completed successfully for bookingId: {}", message.getBookingId());
    }
}
