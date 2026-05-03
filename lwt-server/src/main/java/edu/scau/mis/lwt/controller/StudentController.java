package edu.scau.mis.lwt.controller;

import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.R;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.pojo.vo.BookingVO;
import edu.scau.mis.lwt.pojo.vo.ScheduleVO;
import edu.scau.mis.lwt.service.BookingService;
import edu.scau.mis.lwt.service.CoachScheduleService;
import edu.scau.mis.lwt.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController extends BaseController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CoachScheduleService coachScheduleService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/schedules")
    public R<List<ScheduleVO>> getAvailableSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long coachId) {
        return R.ok(coachScheduleService.getAvailableSchedules(date, coachId));
    }

    @PostMapping("/book/{scheduleId}")
    public R<Void> book(@RequestAttribute("userId") Long studentId, @PathVariable Long scheduleId) {
        bookingService.book(studentId, scheduleId);
        return R.ok();
    }

    @GetMapping("/bookings")
    public R<List<BookingVO>> getMyBookings(
            @RequestAttribute("userId") Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(bookingService.getStudentBookings(studentId, date));
    }

    @GetMapping("/history")
    public R<List<BookingVO>> getHistory(@RequestAttribute("userId") Long studentId) {
        return R.ok(bookingService.getHistory(studentId));
    }

    @PostMapping("/cancel/{bookingId}")
    public R<Void> cancel(@RequestAttribute("userId") Long studentId, @PathVariable Long bookingId) {
        bookingService.cancel(studentId, bookingId);
        return R.ok();
    }

    @GetMapping("/coaches")
    public R<List<SysUser>> getCoaches() {
        return R.ok(sysUserService.getCoaches());
    }

    @GetMapping("/info")
    public R<SysUser> getMyInfo(@RequestAttribute("userId") Long userId) {
        return R.ok(sysUserService.getUserInfo(userId));
    }
}
