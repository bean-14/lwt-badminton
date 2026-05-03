package edu.scau.mis.lwt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.scau.mis.lwt.pojo.dto.LoginDTO;
import edu.scau.mis.lwt.pojo.dto.RechargeDTO;
import edu.scau.mis.lwt.pojo.dto.RegisterDTO;
import edu.scau.mis.lwt.pojo.entity.SysUser;

import java.util.List;
import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    Map<String, Object> login(LoginDTO loginDTO);

    void register(RegisterDTO registerDTO);

    SysUser getUserInfo(Long userId);

    void recharge(RechargeDTO rechargeDTO);

    List<SysUser> getCoaches();

    List<SysUser> getStudents();
}
