package edu.scau.mis.lwt.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 场地实体类
 * 对应数据库表：venue
 * 记录运动场地的信息（如：球场1、球场2等）
 */
@Data
@TableName("venue")
public class Venue implements Serializable {

    /**
     * 场地ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 场地名称（如：篮球场1号、网球场A等）
     */
    private String name;

    /**
     * 场地位置（如：体育馆一楼、室外场地等）
     */
    private String location;

    /**
     * 场地状态：0-不可用，1-可用
     */
    private Integer status;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
