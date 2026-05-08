package edu.scau.mis.lwt.controller;

import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.Result;
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

/**
 * 教练控制器
 * 处理教练相关的所有操作：设置可用时间、查看预约、确认课程等
 * 所有接口都需要JWT认证，且只能操作教练自己的数据
 */
@RestController
@RequestMapping("/coach")
public class CoachController extends BaseController {

    @Autowired
    private CoachScheduleService coachScheduleService;

    @Autowired
    private BookingService bookingService;

    /**
     * 添加可用时间段（教练设置自己可预约的时间）
     * @param scheduleDTO 排课信息（日期、开始时间、结束时间、场地等）
     * @return 成功响应
     */
    @PostMapping("/schedule")
    public Result<Void> addSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        coachScheduleService.addSchedule(scheduleDTO);
        return Result.ok();
    }

    /**
     * 查看我的排课安排
     * @param coachId 教练ID（从JWT解析）
     * @param date 可选，按日期筛选
     * @return 排课安排列表
     */
    @GetMapping("/schedules")
    public Result<List<ScheduleVO>> getMySchedules(
            @RequestAttribute("userId") Long coachId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(coachScheduleService.getCoachSchedules(coachId, date));
    }

    /**
     * 删除排课时间段
     * @param scheduleId 排课ID
     * @return 成功响应
     */
    @DeleteMapping("/schedule/{scheduleId}")
    public Result<Void> deleteSchedule(@PathVariable Long scheduleId) {
        coachScheduleService.deleteSchedule(scheduleId);
        return Result.ok();
    }

    /**
     * 启用/禁用排课
     * @param coachId 教练ID（从JWT解析）
     * @param scheduleId 排课ID
     * @param enable true=启用，false=禁用
     * @return 成功响应
     */
    @PutMapping("/schedule/{scheduleId}/toggle")
    public Result<Void> toggleSchedule(
            @RequestAttribute("userId") Long coachId,
            @PathVariable Long scheduleId,
            @RequestParam boolean enable) {
        coachScheduleService.toggleSchedule(scheduleId, coachId, enable);
        return Result.ok();
    }

    /**
     * 查看我的课程预约
     * @param coachId 教练ID（从JWT解析）
     * @param date 可选，按日期筛选
     * @return 预约记录列表
     */
    @GetMapping("/bookings")
    public Result<List<BookingVO>> getBookings(
            @RequestAttribute("userId") Long coachId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(bookingService.getCoachBookings(coachId, date));
    }

    /**
     * 确认预约（扣减学生课时）
     * @param coachId 教练ID（从JWT解析）
     * @param bookingId 预约ID
     * @return 成功响应
     */
    @PostMapping("/confirm/{bookingId}")
    public Result<Void> confirm(@RequestAttribute("userId") Long coachId, @PathVariable Long bookingId) {
        bookingService.confirm(coachId, bookingId);
        return Result.ok();
    }

    /**
     * 拒绝预约
     * @param coachId 教练ID（从JWT解析）
     * @param bookingId 预约ID
     * @return 成功响应
     */
    @PostMapping("/reject/{bookingId}")
    public Result<Void> reject(@RequestAttribute("userId") Long coachId, @PathVariable Long bookingId) {
        bookingService.reject(coachId, bookingId);
        return Result.ok();
    }

    /**
     * 处理学生请假申请
     * @param coachId 教练ID（从JWT解析）
     * @param bookingId 预约ID
     * @param action approve=同意并退还课时, reject=拒绝
     * @return 成功响应
     */
    @PostMapping("/handle-leave/{bookingId}")
    public Result<Void> handleLeave(@RequestAttribute("userId") Long coachId,
                                    @PathVariable Long bookingId,
                                    @RequestParam String action) {
        bookingService.handleLeave(coachId, bookingId, action);
        return Result.ok();
    }
}
