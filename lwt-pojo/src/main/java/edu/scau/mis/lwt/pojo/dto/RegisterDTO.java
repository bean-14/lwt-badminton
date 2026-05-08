package edu.scau.mis.lwt.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 注册数据传输对象
 * 用于接收用户注册请求的参数
 */
@Data
public class RegisterDTO implements Serializable {

    /**
     * 用户名（登录账号，唯一）
     */
    private String username;

    /**
     * 密码（明文，后端会加密存储）
     */
    private String password;

    /**
     * 用户昵称（显示名称）
     */
    private String nickname;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户类型：student-学生，coach-教练，admin-管理员
     */
    private String userType;
}
