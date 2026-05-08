package edu.scau.mis.lwt.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约视图对象
 * 用于返回给前端展示预约详细信息
 * 包含预约信息以及关联的学生、教练、场地的名称
 */
@Data
public class BookingVO implements Serializable {

    /**
     * 预约ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生昵称（用于前端显示）
     */
    private String studentName;

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
     * 预约日期
     */
    private LocalDate scheduleDate;

    /**
     * 预约状态：pending-待确认，confirmed-已确认，cancelled-已取消
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 确认时间（教练确认预约时设置）
     */
    private LocalDateTime confirmTime;
}
