package edu.scau.mis.lwt.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 场地使用统计视图对象
 * 用于返回场地使用次数的统计结果
 */
@Data
public class VenueStatsVO implements Serializable {

    /**
     * 场地名称
     */
    private String venueName;

    /**
     * 使用次数（已确认的预约数）
     */
    private Long usageCount;

    /**
     * 场地位置
     */
    private String location;
}
