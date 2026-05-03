package edu.scau.mis.lwt.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("coach_schedule")
public class CoachSchedule implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long coachId;

    private Long venueId;

    private LocalDate scheduleDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
