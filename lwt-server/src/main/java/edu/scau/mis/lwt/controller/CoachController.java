package edu.scau.mis.lwt.controller;

import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.R;
import edu.scau.mis.lwt.pojo.dto.ScheduleDTO;
import edu.scau.mis.lwt.pojo.vo.BookingVO;
import edu.scau.mis.lwt.pojo.vo.ScheduleVO;
import edu.scau.mis.lwt.service.BookingService;
import edu.scau.mis.lwt.service.CoachScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/coach")
public class CoachController extends BaseController {

    @Autowired
    private CoachScheduleService coachScheduleService;

    @Autowired
    private BookingService bookingService;

    @PostMapping("/schedule")
    public R<Void> addSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        coachScheduleService.addSchedule(scheduleDTO);
        return R.ok();
    }

    @GetMapping("/schedules")
    public R<List<ScheduleVO>> getMySchedules(
            @RequestAttribute("userId") Long coachId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(coachScheduleService.getCoachSchedules(coachId, date));
    }

    @DeleteMapping("/schedule/{scheduleId}")
    public R<Void> deleteSchedule(@PathVariable Long scheduleId) {
        coachScheduleService.deleteSchedule(scheduleId);
        return R.ok();
    }

    @GetMapping("/bookings")
    public R<List<BookingVO>> getBookings(
            @RequestAttribute("userId") Long coachId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(bookingService.getCoachBookings(coachId, date));
    }

    @PostMapping("/confirm/{bookingId}")
    public R<Void> confirm(@RequestAttribute("userId") Long coachId, @PathVariable Long bookingId) {
        bookingService.confirm(coachId, bookingId);
        return R.ok();
    }
}
