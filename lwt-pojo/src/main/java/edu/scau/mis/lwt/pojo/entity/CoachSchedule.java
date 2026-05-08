package edu.scau.mis.lwt.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 教练排课实体类
 * 对应数据库表：coach_schedule
 * 记录教练设置的可用时间段（学生可预约的时间段）
 */
@Data
@TableName("coach_schedule")
public class CoachSchedule implements Serializable {

    /**
     * 排课ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 教练ID（外键关联sys_user表）
     */
    private Long coachId;

    /**
     * 场地ID（外键关联venue表）
     */
    private Long venueId;

    /**
     * 排课日期
     */
    private LocalDate scheduleDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 状态：0-不可用，1-可用（学生可预约）
     */
    private Integer status;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
