package edu.scau.mis.lwt.controller;

import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.Result;
import edu.scau.mis.lwt.pojo.dto.LoginDTO;
import edu.scau.mis.lwt.pojo.dto.RegisterDTO;
import edu.scau.mis.lwt.pojo.entity.SysUser;
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

    /**
     * 用户登录接口
     * @param loginDTO 登录信息（用户名、密码）
     * @return 包含JWT令牌的用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        return Result.ok(sysUserService.login(loginDTO));
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
