package edu.scau.mis.lwt.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("booking")
public class Booking implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long coachId;

    private Long venueId;

    private LocalDate scheduleDate;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime confirmTime;
}
