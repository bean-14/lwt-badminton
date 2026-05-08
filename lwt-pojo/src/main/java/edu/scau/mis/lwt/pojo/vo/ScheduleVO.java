package edu.scau.mis.lwt.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排课视图对象
 * 用于返回给前端展示排课详细信息
 * 包含排课信息以及关联的教练、场地的名称，以及已预约人数
 */
@Data
public class ScheduleVO implements Serializable {

    /**
     * 排课ID
     */
    private Long id;

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 教练昵称（用于前端显示）
     */
    private String coachName;

    /**
     * 场地ID
     */
    private Long venueId;

    /**
     * 场地名称（用于前端显示）
     */
    private String venueName;

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
     * 已预约人数（用于显示该时段的热度）
     */
    private Integer bookedCount;

    /**
     * 状态：0-不可用，1-可用（学生可预约）
     */
    private Integer status;
}
