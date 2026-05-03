package edu.scau.mis.lwt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.scau.mis.lwt.pojo.dto.LoginDTO;
import edu.scau.mis.lwt.pojo.dto.RechargeDTO;
import edu.scau.mis.lwt.pojo.dto.RegisterDTO;
import edu.scau.mis.lwt.pojo.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 * 定义用户相关的业务逻辑方法
 * 继承MyBatis-Plus的IService，获得基础CRUD能力
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户登录
     * @param loginDTO 登录信息（用户名、密码）
     * @return 包含JWT令牌和用户信息的Map
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 用户注册
     * @param registerDTO 注册信息（用户名、密码、用户类型等）
     */
    void register(RegisterDTO registerDTO);

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户实体对象
     */
    SysUser getUserInfo(Long userId);

    /**
     * 为学生充值课时
     * @param rechargeDTO 充值信息（学生ID、充值课时数）
     */
    void recharge(RechargeDTO rechargeDTO);

    /**
     * 获取所有教练列表
     * @return 教练用户列表
     */
    List<SysUser> getCoaches();

    /**
     * 获取所有学生列表
     * @return 学生用户列表
     */
    List<SysUser> getStudents();
}
