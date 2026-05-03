package edu.scau.mis.lwt.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排课数据传输对象
 * 用于教练设置可用时间段的请求参数
 */
@Data
public class ScheduleDTO implements Serializable {

    /**
     * 教练ID（通常是当前登录的教练）
     */
    private Long coachId;

    /**
     * 场地ID（该时间段使用的场地）
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
}
