package edu.scau.mis.lwt.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleDTO implements Serializable {

    private Long coachId;

    private Long venueId;

    private LocalDate scheduleDate;

    private LocalTime startTime;

    private LocalTime endTime;
}
