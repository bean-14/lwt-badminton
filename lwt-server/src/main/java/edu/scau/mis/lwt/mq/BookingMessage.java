package edu.scau.mis.lwt.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MQ 消息体 — 预约通知消息
 * <p>
 * 这个对象会被 RabbitTemplate 自动转成 JSON 字符串发送到 RabbitMQ，
 * 消费者收到后再从 JSON 转回 Java 对象。
 * <p>
 * 消息内容：
 * - bookingId：预约记录的 ID（用于幂等校验、查询详情）
 * - coachId：教练 ID（用于 WebSocket 推送、权限验证）
 * - studentId：学员 ID（用于 WebSocket 反向推送、微信通知）
 * <p>
 * 为什么只传 ID 不传全部信息？
 *   MQ 消息应该尽量精简，只传必要的 ID。
 *   消费者根据 ID 去数据库查询完整信息，避免 MQ 消息体过于臃肿，
 *   也保证数据一致性（如果消费者处理时数据已变更，以数据库为准）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingMessage {

    private Long bookingId;
    private Long coachId;
    private Long studentId;
}
