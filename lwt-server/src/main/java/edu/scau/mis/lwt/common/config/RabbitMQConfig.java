package edu.scau.mis.lwt.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置
 * <p>
 * 什么是 RabbitMQ？
 *   消息队列就像"邮局"：生产者把消息交给邮局（Exchange），
 *   邮局按规则（Routing Key）分发给对应的信箱（Queue），
 *   消费者从信箱取走消息处理。
 *   这样发送方和接收方不需要同时在线，系统更稳定。
 * <p>
 * 为什么用 RabbitMQ 而不是直接发 HTTP？
 *   如果直接调用微信 API 发通知，微信服务器挂了会导致学员预约接口卡死。
 *   用 MQ 后，预约接口只管写入 DB 然后发一条 MQ 消息就立即返回，
 *   耗时操作（发微信）由后台消费者异步处理，即使微信失败还能自动重试。
 * <p>
 * 架构说明：
 *   生产者（BookingServiceImpl）→ Exchange → Queue → 消费者（BookingNotificationListener）
 *                                           ↓ (失败超过重试次数)
 *                                        Dead Letter Queue（死信队列，人工处理）
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 交换机（Exchange）：消息路由器
     * 生产者发的消息都先到 Exchange，由它按规则分发给 Queue。
     * 这里使用 TopicExchange（主题交换机），
     * 可以根据 Routing Key 的模糊匹配分发到不同 Queue。
     */
    public static final String EXCHANGE_BOOKING = "booking.topic.exchange";

    /**
     * 队列（Queue）：消息的"信箱"
     * 消费者监听这个队列，有新消息就取出来处理。
     */
    public static final String QUEUE_BOOKING_NOTIFY = "booking.notify.queue";

    /**
     * 路由键（Routing Key）：消息的路由地址
     * 生产者发送消息时指定这个 key，Exchange 根据 key 把消息投递到对应的 Queue。
     */
    public static final String ROUTING_BOOKING_NEW = "notify.booking.new";

    /**
     * 死信交换机（DLX - Dead Letter Exchange）
     * 当消息重试次数用尽仍然失败，会被投递到这个交换机。
     */
    public static final String DLX_BOOKING = "booking.dlx.exchange";

    /**
     * 死信队列（DLQ - Dead Letter Queue）
     * 从死信交换机过来的消息（即重试耗尽的消息）进入此队列。
     * DeadLetterListener 监听此队列，记录错误日志，等待人工干预。
     */
    public static final String DLQ_BOOKING_NOTIFY = "booking.notify.dlq";

    public static final String ROUTING_DLQ = "notify.booking.dlq";

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(EXCHANGE_BOOKING, true, false);
    }

    /**
     * 主队列：接收预约通知消息
     * x-dead-letter-exchange：指定当消息处理失败时的"转交"交换机（死信交换机）。
     * x-dead-letter-routing-key：转交时使用的路由键。
     * 这意味着：消费者抛出异常 → Spring 重试 3 次 → 依然失败 → 消息自动进入死信队列。
     */
    @Bean
    public Queue bookingNotifyQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_NOTIFY)
                .withArgument("x-dead-letter-exchange", DLX_BOOKING)
                .withArgument("x-dead-letter-routing-key", ROUTING_DLQ)
                .build();
    }

    @Bean
    public Binding bookingNotifyBinding() {
        return BindingBuilder.bind(bookingNotifyQueue())
                .to(bookingExchange())
                .with(ROUTING_BOOKING_NEW);
    }

    /**
     * 死信交换机（DLX）
     * 接收重试耗尽的消息，将其路由到死信队列。
     */
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX_BOOKING, true, false);
    }

    /**
     * 死信队列（DLQ）
     * 存储重试耗尽的消息，由 DeadLetterListener 消费，
     * 记录日志供管理员人工排查。
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_BOOKING_NOTIFY).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(ROUTING_DLQ);
    }

    /**
     * 消息转换器：JSON 序列化
     * RabbitMQ 默认使用 Java 序列化，不够直观。
     * 改成 Jackson2JsonMessageConverter 后，消息体就是 JSON 字符串，
     * 跨语言调试更方便（如直接用 RabbitMQ 管理后台查看消息内容）。
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate 使用 JSON 转换器
     * RabbitTemplate 是 Spring 提供的 MQ 发送工具类。
     * 设了 messageConverter 后，调用 convertAndSend 时
     * 会自动把对象转成 JSON 字符串发送。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
