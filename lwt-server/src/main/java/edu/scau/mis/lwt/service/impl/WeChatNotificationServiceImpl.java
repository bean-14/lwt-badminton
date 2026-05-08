package edu.scau.mis.lwt.service.impl;

import edu.scau.mis.lwt.mq.BookingMessage;
import edu.scau.mis.lwt.service.WeChatNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信服务通知实现
 * <p>
 * 调用微信公众平台 API 发送订阅消息。
 * 接口文档：https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/subscribe-message/subscribeMessage.send.html
 * <p>
 * 注意：
 * - 实际使用时需要先在微信小程序中让用户授权订阅消息（wx.requestSubscribeMessage）
 * - 用户授权后获得的模板 ID 需要和这里配置的一致
 * - access_token 有 2 小时有效期，生产环境建议缓存到 Redis
 */
@Service
public class WeChatNotificationServiceImpl implements WeChatNotificationService {

    private static final Logger log = LoggerFactory.getLogger(WeChatNotificationServiceImpl.class);

    /**
     * 微信订阅消息发送接口地址
     * 通过 POST 请求向指定用户的微信发送模板消息。
     */
    private static final String WECHAT_SUBSCRIBE_URL =
            "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";

    /**
     * 微信小程序的 AppID（从 application.yml 读取）
     */
    @Value("${wechat.app-id:}")
    private String appId;

    /**
     * 微信小程序的 AppSecret（从 application.yml 读取）
     */
    @Value("${wechat.app-secret:}")
    private String appSecret;

    /**
     * 模板消息 ID（从 application.yml 读取）
     * 需要在微信公众平台申请并获得审核通过。
     */
    @Value("${wechat.template-id:}")
    private String templateId;

    /**
     * RestTemplate：Spring 提供的 HTTP 客户端工具
     * 用于调用微信的 REST API（发送 GET/POST 请求）。
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送微信订阅消息
     * <p>
     * 如果 application.yml 中 wechat 配置为空，则走 mock 模式（只打日志，不真实调用）。
     * 这是为了方便本地开发：不需要配微信也能跑通完整流程。
     * <p>
     * 调用流程：
     *   1. 用 appId + appSecret 获取 access_token（调用微信接口获取临时令牌）
     *   2. 用 access_token + 模板消息参数 调用发送接口
     *   3. 检查返回结果（errcode == 0 表示成功）
     * <p>
     * 如果微信接口返回错误或网络超时，这里会抛出 RuntimeException，
     * 上层的 MQ 消费者会捕获并触发重试机制。
     */
    @Override
    public void sendBookingNotification(BookingMessage message) {
        if (appId.isEmpty() || appSecret.isEmpty() || templateId.isEmpty()) {
            log.warn("WeChat config not fully set (app-id/app-secret/template-id), skipping actual API call");
            log.info("[MOCK] Would send WeChat notification for bookingId={}, coachId={}, studentId={}",
                    message.getBookingId(), message.getCoachId(), message.getStudentId());
            return;
        }

        String accessToken = fetchAccessToken();
        String url = WECHAT_SUBSCRIBE_URL + "?access_token=" + accessToken;

        Map<String, Object> body = buildRequestBody(message);
        String response = restTemplate.postForObject(url, body, String.class);

        log.info("WeChat API response for booking {}: {}", message.getBookingId(), response);

        if (response != null && response.contains("\"errcode\":0")) {
            log.info("WeChat notification sent successfully for bookingId: {}", message.getBookingId());
        } else {
            log.error("WeChat API returned error for bookingId: {}, response: {}", message.getBookingId(), response);
            throw new RuntimeException("WeChat API error: " + response);
        }
    }

    /**
     * 获取微信 access_token
     * <p>
     * access_token 是调用微信 API 的接口调用凭证（类似 JWT 令牌），
     * 有效期 2 小时。每次调用微信接口都需要带上。
     * <p>
     * 获取方式：用 appId + appSecret 调用微信的 token 接口。
     * 生产环境建议将 access_token 缓存到 Redis，避免每次发消息都重新获取。
     */
    private String fetchAccessToken() {
        String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                + appId + "&secret=" + appSecret;
        Map<String, Object> response = restTemplate.getForObject(tokenUrl, Map.class);
        if (response != null && response.containsKey("access_token")) {
            return (String) response.get("access_token");
        }
        throw new RuntimeException("Failed to fetch WeChat access_token: " + response);
    }

    /**
     * 构建微信模板消息的请求体
     * <p>
     * 微信模板消息的格式要求：
     * {
     *   "touser": "用户微信的 openId",
     *   "template_id": "模板 ID",
     *   "page": "点击消息跳转的小程序页面",
     *   "data": {
     *     "thing1": { "value": "..." },  // 模板中的变量占位符
     *     "number2": { "value": "..." }
     *   }
     * }
     * <p>
     * 注意：touser 需要替换为学员在微信小程序中的 openId。
     * 本项目中先用 "OPENID" 占位，实际使用时需从数据库查询学员的 openId。
     */
    private Map<String, Object> buildRequestBody(BookingMessage message) {
        Map<String, Object> body = new HashMap<>();
        body.put("touser", "OPENID");
        body.put("template_id", templateId);
        body.put("page", "pages/booking/detail?id=" + message.getBookingId());

        Map<String, Object> data = new HashMap<>();
        data.put("thing1", Map.of("value", "您有一个新预约"));
        data.put("number2", Map.of("value", String.valueOf(message.getBookingId())));
        data.put("thing3", Map.of("value", "等待教练确认"));
        body.put("data", data);

        return body;
    }
}
