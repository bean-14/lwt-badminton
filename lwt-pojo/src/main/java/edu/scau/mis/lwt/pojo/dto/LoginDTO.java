package edu.scau.mis.lwt.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录数据传输对象
 * 用于接收用户登录请求的参数
 */
@Data
public class LoginDTO implements Serializable {

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（明文，后端会进行加密验证）
     */
    private String password;
}
