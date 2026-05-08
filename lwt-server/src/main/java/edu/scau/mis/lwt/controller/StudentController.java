package edu.scau.mis.lwt.controller;

import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.Result;
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

/**
 * 学生控制器
 * 处理学生相关的所有操作：查看教练、预约课程、查看预约记录等
 * 所有接口都需要JWT认证，且只能操作学生自己的数据
 */
@RestController
@RequestMapping("/student")
public class StudentController extends BaseController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CoachScheduleService coachScheduleService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 查看可用课程安排
     * @param date 可选，指定日期（格式：yyyy-MM-dd）
     * @param coachId 可选，指定教练ID
     * @return 可用的课程安排列表（包含教练、场地、时间等信息）
     */
    @GetMapping("/schedules")
    public Result<List<ScheduleVO>> getAvailableSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long coachId) {
        return Result.ok(coachScheduleService.getAvailableSchedules(date, coachId));
    }

    /**
     * 预约课程
     * @param studentId 学生ID（从JWT解析，由拦截器设置）
     * @param scheduleId 课程安排ID
     * @return 成功响应
     * 注意：会进行冲突检测（课时余额、时间冲突、场地冲突等）
     */
    @PostMapping("/book/{scheduleId}")
    public Result<Void> book(@RequestAttribute("userId") Long studentId, @PathVariable Long scheduleId) {
        bookingService.book(studentId, scheduleId);
        return Result.ok();
    }

    /**
     * 查看我的预约记录
     * @param studentId 学生ID（从JWT解析）
     * @param date 可选，按日期筛选
     * @return 预约记录列表（包含教练、场地、状态等信息）
     */
    @GetMapping("/bookings")
    public Result<List<BookingVO>> getMyBookings(
            @RequestAttribute("userId") Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(bookingService.getStudentBookings(studentId, date));
    }

    /**
     * 查看历史预约（已完成的课程）
     * @param studentId 学生ID（从JWT解析）
     * @return 历史预约记录列表
     */
    @GetMapping("/history")
    public Result<List<BookingVO>> getHistory(@RequestAttribute("userId") Long studentId) {
        return Result.ok(bookingService.getHistory(studentId));
    }

    /**
     * 取消预约
     * @param studentId 学生ID（从JWT解析）
     * @param bookingId 预约ID
     * @return 成功响应
     * 注意：已确认的预约无法取消
     */
    @PostMapping("/cancel/{bookingId}")
    public Result<Void> cancel(@RequestAttribute("userId") Long studentId, @PathVariable Long bookingId) {
        bookingService.cancel(studentId, bookingId);
        return Result.ok();
    }

    /**
     * 申请请假
     * @param studentId 学生ID（从JWT解析）
     * @param bookingId 预约ID
     * @param reason 请假原因
     * @return 成功响应
     */
    @PostMapping("/leave/{bookingId}")
    public Result<Void> leave(@RequestAttribute("userId") Long studentId,
                              @PathVariable Long bookingId,
                              @RequestParam String reason) {
        bookingService.leave(studentId, bookingId, reason);
        return Result.ok();
    }

    /**
     * 查看所有教练列表
     * @return 教练用户信息列表
     */
    @GetMapping("/coaches")
    public Result<List<SysUser>> getCoaches() {
        return Result.ok(sysUserService.getCoaches());
    }

    /**
     * 查看我的信息（包含剩余课时）
     * @param userId 用户ID（从JWT解析）
     * @return 用户详细信息
     */
    @GetMapping("/info")
    public Result<SysUser> getMyInfo(@RequestAttribute("userId") Long userId) {
        return Result.ok(sysUserService.getUserInfo(userId));
    }
}
