package edu.scau.mis.lwt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.scau.mis.lwt.common.exception.BusinessException;
import edu.scau.mis.lwt.common.exception.ErrorCode;
import edu.scau.mis.lwt.common.utils.JwtUtils;
import edu.scau.mis.lwt.mapper.RechargeRecordMapper;
import edu.scau.mis.lwt.mapper.SysUserMapper;
import edu.scau.mis.lwt.pojo.dto.LoginDTO;
import edu.scau.mis.lwt.pojo.dto.RechargeDTO;
import edu.scau.mis.lwt.pojo.dto.RegisterDTO;
import edu.scau.mis.lwt.pojo.entity.RechargeRecord;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = getOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        String encryptPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!encryptPassword.equals(user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        String token = JwtUtils.generateToken(user.getId(), user.getUsername());
        stringRedisTemplate.opsForValue().set("user:token:" + user.getId(), token, 1, TimeUnit.DAYS);

        return Map.of("token", token, "userId", user.getId(), "userType", user.getUserType());
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, registerDTO.getUsername());
        if (count(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXIST);
        }

        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));
        user.setNickname(registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setUserType(registerDTO.getUserType());
        user.setRemainingHours(0);
        user.setStatus(1);
        save(user);
    }

    @Override
    public SysUser getUserInfo(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void recharge(RechargeDTO rechargeDTO) {
        SysUser user = getById(rechargeDTO.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }
        if (!"student".equals(user.getUserType())) {
            throw new BusinessException(400, "只能为学生充值");
        }

        user.setRemainingHours(user.getRemainingHours() + rechargeDTO.getHours());
        updateById(user);

        RechargeRecord record = new RechargeRecord();
        record.setUserId(rechargeDTO.getUserId());
        record.setHours(rechargeDTO.getHours());
        record.setAmount(BigDecimal.ZERO);
        rechargeRecordMapper.insert(record);
    }

    @Override
    public List<SysUser> getCoaches() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserType, "coach")
               .eq(SysUser::getStatus, 1);
        return list(wrapper);
    }

    @Override
    public List<SysUser> getStudents() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserType, "student")
               .eq(SysUser::getStatus, 1);
        return list(wrapper);
    }
}
