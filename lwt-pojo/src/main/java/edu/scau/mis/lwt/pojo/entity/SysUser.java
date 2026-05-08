package edu.scau.mis.lwt.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 对应数据库表：sys_user
 * 包含学生、教练、管理员三种用户类型
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    /**
     * 用户ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 用户昵称
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

    /**
     * 剩余课时（仅学生有）
     */
    private Integer remainingHours;

    /**
     * 账号状态：0-禁用，1-正常
     */
    private Integer status;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
