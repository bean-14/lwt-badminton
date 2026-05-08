package edu.scau.mis.lwt.controller;

import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.Result;
import edu.scau.mis.lwt.pojo.dto.LoginDTO;
import edu.scau.mis.lwt.pojo.dto.RegisterDTO;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.service.NotificationService;
import edu.scau.mis.lwt.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册和获取用户信息等公开接口（无需JWT认证）
 */
@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 用户登录接口
     * <p>
     * 登录成功后自动在 Redis 中标记用户在线（user_online:{userId}）。
     * 这样后续学员预约时，系统可以判断教练是否在线，
     * 从而决定是否走 WebSocket 实时推送。
     * <p>
     * 在线状态也会在前端打开页面时通过 POST /user/online 更新，
     * 以及 WebSocket 断开时由 WebSocketEventListener 自动清除。
     *
     * @param loginDTO 登录信息（用户名、密码）
     * @return 包含JWT令牌的用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        Map<String, Object> result = sysUserService.login(loginDTO);
        // 登录成功，在 Redis 中标记用户在线
        // Redis SET user_online:{userId} "1" EX 86400
        Object userIdObj = result.get("userId");
        if (userIdObj != null) {
            notificationService.setUserOnline(Long.valueOf(userIdObj.toString()));
        }
        return Result.ok(result);
    }

    /**
     * 用户注册接口
     * @param registerDTO 注册信息（用户名、密码、用户类型等）
     * @return 成功响应
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return Result.ok();
    }

    /**
     * 获取当前登录用户信息
     * @param userId 从JWT解析出的用户ID（由拦截器设置）
     * @return 用户详细信息
     */
    @GetMapping("/info")
    public Result<SysUser> getUserInfo(@RequestAttribute("userId") Long userId) {
        return Result.ok(sysUserService.getUserInfo(userId));
    }
}
