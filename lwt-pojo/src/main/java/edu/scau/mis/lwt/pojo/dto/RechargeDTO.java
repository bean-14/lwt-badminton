package edu.scau.mis.lwt.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 课时充值数据传输对象
 * 用于管理员为学生充值课时的请求参数
 */
@Data
public class RechargeDTO implements Serializable {

    /**
     * 学生用户ID（需要充值的用户）
     */
    private Long userId;

    /**
     * 充值课时数（正整数）
     */
    private Integer hours;
}
