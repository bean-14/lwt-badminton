package edu.scau.mis.lwt.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 学生上课统计视图对象
 * 用于返回学生上课次数的统计结果
 */
@Data
public class StudentStatsVO implements Serializable {

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生昵称
     */
    private String studentName;

    /**
     * 上课次数（已确认的预约数）
     */
    private Long classCount;
}
