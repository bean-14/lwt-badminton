package edu.scau.mis.lwt.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleVO implements Serializable {

    private Long id;

    private Long coachId;

    private String coachName;

    private Long venueId;

    private String venueName;

    private LocalDate scheduleDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer bookedCount;

    private Integer status;
}
