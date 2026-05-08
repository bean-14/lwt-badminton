package edu.scau.mis.lwt.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 教练上课统计视图对象
 * 用于返回教练上课次数的统计结果
 */
@Data
public class CoachStatsVO implements Serializable {

    /**
     * 教练ID
     */
    private Long coachId;

    /**
     * 教练昵称
     */
    private String coachName;

    /**
     * 上课次数（已确认的预约数）
     */
    private Long classCount;
}
