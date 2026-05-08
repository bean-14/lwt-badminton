package edu.scau.mis.lwt.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket / STOMP 配置
 * <p>
 * 什么是 WebSocket？
 *   WebSocket 是浏览器与服务器之间的全双工通信协议，服务器可以主动向客户端推送消息。
 *   传统 HTTP 只能由客户端发起请求，而 WebSocket 让服务器也能"主动说话"。
 * <p>
 * 什么是 STOMP？
 *   STOMP 是基于 WebSocket 上层的消息协议，类似"聊天室"的订阅/发布模型。
 *   客户端订阅一个"频道"（如 /topic/coach/123），
 *   服务端向该频道发消息，所有订阅者都能收到。
 * <p>
 * 流程示例：
 *   教练登录后，前端 STOMP 连接到 ws://localhost:8080/ws，
 *   订阅 /topic/coach/{coachId}。
 *   当学员预约时，后端通过 SimpMessagingTemplate
 *   向 /topic/coach/{coachId} 发消息，教练端即时弹窗。
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理（即消息路由规则）
     * enableSimpleBroker("/topic")：
     *   开启一个简单的内存消息代理，所有以 /topic 开头的目的地
     *   都会广播给所有订阅的客户端。
     *   例：订阅 /topic/coach/5 就能收到发给教练 5 的消息。
     * setApplicationDestinationPrefixes("/app")：
     *   客户端发送消息到服务端时的前缀（本项目中前端不需要发消息到后端，保留配置供扩展）。
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 注册 STOMP 端点，客户端通过此地址建立 WebSocket 连接。
     * addEndpoint("/ws")：WebSocket 连接地址。
     * setAllowedOriginPatterns("*")：允许跨域（开发环境）。
     * withSockJS()：启用 SockJS 回退，如果浏览器不支持原生 WebSocket，
     *               自动降级为 HTTP 轮询等方式，保证兼容性。
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
