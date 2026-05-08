package edu.scau.mis.lwt.controller;

import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.Result;
import edu.scau.mis.lwt.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户在线状态管理控制器
 * <p>
 * 为什么需要在线状态？
 *   学员预约后，系统尝试通过 WebSocket 实时推送给教练。
 *   但如果教练不在线（没打开页面），推送会失败。
 *   通过 Redis 中的 user_online:{userId} 标记，
 *   可以在推送前先判断用户是否在线，避免无效推送。
 * <p>
 * 在线状态的生命周期：
 *   1. 用户登录成功 → AuthController 自动标记在线
 *   2. 用户打开页面 → 前端主动调用 POST /user/online（可选）
 *   3. WebSocket 断开 → WebSocketEventListener 自动清除在线标记
 *   4. 用户关闭页面 → 前端调用 POST /user/offline（可选）
 *   5. 24 小时无活动 → Redis TTL 到期自动清除（兜底）
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 标记用户在线
     * 前端可以在以下时机调用：
     *   - 页面加载完成时（onLoad）
     *   - 用户切换回页面时（onShow）
     * Redis 操作：SET user_online:{userId} "1" EX 86400
     */
    @PostMapping("/online")
    public Result<Void> setOnline(@RequestAttribute("userId") Long userId) {
        notificationService.setUserOnline(userId);
        return Result.ok();
    }

    /**
     * 标记用户离线
     * 前端可以在以下时机调用：
     *   - 页面关闭时（onHide / onUnload）
     *   - 用户退出登录时
     * Redis 操作：DEL user_online:{userId}
     * <p>
     * 即使前端忘记调用，WebSocket 断开时也会自动清除。
     */
    @PostMapping("/offline")
    public Result<Void> setOffline(@RequestAttribute("userId") Long userId) {
        notificationService.setUserOffline(userId);
        return Result.ok();
    }
}
