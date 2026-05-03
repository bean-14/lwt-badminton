package edu.scau.mis.lwt.controller;

import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.R;
import edu.scau.mis.lwt.pojo.dto.LoginDTO;
import edu.scau.mis.lwt.pojo.dto.RegisterDTO;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        return R.ok(sysUserService.login(loginDTO));
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody RegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return R.ok();
    }

    @GetMapping("/info")
    public R<SysUser> getUserInfo(@RequestAttribute("userId") Long userId) {
        return R.ok(sysUserService.getUserInfo(userId));
    }
}
