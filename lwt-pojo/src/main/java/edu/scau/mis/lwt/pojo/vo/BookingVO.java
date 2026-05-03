package edu.scau.mis.lwt.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingVO implements Serializable {

    private Long id;

    private Long studentId;

    private String studentName;

    private Long coachId;

    private String coachName;

    private Long venueId;

    private String venueName;

    private LocalDate scheduleDate;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime confirmTime;
}
