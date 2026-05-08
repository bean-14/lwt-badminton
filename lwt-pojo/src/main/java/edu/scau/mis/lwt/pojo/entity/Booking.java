package edu.scau.mis.lwt.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程预约实体类
 * 对应数据库表：booking
 * 记录学生预约教练课程的详细信息
 */
@Data
@TableName("booking")
public class Booking implements Serializable {

    /**
     * 预约ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID（外键关联sys_user表）
     */
    private Long studentId;

    /**
     * 教练ID（外键关联sys_user表）
     */
    private Long coachId;

    /**
     * 场地ID（外键关联venue表）
     */
    private Long venueId;

    /**
     * 预约日期（排课日期）
     */
    private LocalDate scheduleDate;

    /**
     * 预约状态：pending-待确认，confirmed-已确认，cancelled-已取消
     */
    private String status;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 确认时间（教练确认预约时设置）
     */
    private LocalDateTime confirmTime;
}
