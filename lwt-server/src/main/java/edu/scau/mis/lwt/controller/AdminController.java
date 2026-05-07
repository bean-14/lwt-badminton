package edu.scau.mis.lwt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.Result;
import edu.scau.mis.lwt.mapper.BookingMapper;
import edu.scau.mis.lwt.pojo.dto.RechargeDTO;
import edu.scau.mis.lwt.pojo.entity.Booking;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.pojo.entity.Venue;
import edu.scau.mis.lwt.pojo.vo.BookingVO;
import edu.scau.mis.lwt.pojo.vo.CoachStatsVO;
import edu.scau.mis.lwt.pojo.vo.StudentStatsVO;
import edu.scau.mis.lwt.pojo.vo.VenueStatsVO;
import edu.scau.mis.lwt.service.BookingService;
import edu.scau.mis.lwt.service.SysUserService;
import edu.scau.mis.lwt.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员控制器
 * 处理管理员相关的所有操作：场地管理、用户管理、课时充值、数据统计等
 * 所有接口都需要JWT认证，且只能由管理员角色访问
 */
@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingMapper bookingMapper;

    /**
     * 查看所有场地列表
     * @return 场地列表
     */
    @GetMapping("/venues")
    public Result<List<Venue>> listVenues() {
        return Result.ok(venueService.list());
    }

    /**
     * 添加新场地
     * @param venue 场地信息（名称、位置等）
     * @return 成功响应
     */
    @PostMapping("/venue")
    public Result<Void> addVenue(@RequestBody Venue venue) {
        venue.setStatus(1); // 默认状态为可用
        venueService.save(venue);
        return Result.ok();
    }

    /**
     * 更新场地信息
     * @param venue 场地信息（包含ID）
     * @return 成功响应
     */
    @PutMapping("/venue")
    public Result<Void> updateVenue(@RequestBody Venue venue) {
        venueService.updateById(venue);
        return Result.ok();
    }

    /**
     * 查看所有教练列表
     * @return 教练用户列表
     */
    @GetMapping("/coaches")
    public Result<List<SysUser>> listCoaches() {
        return Result.ok(sysUserService.getCoaches());
    }

    /**
     * 查看所有学生列表
     * @return 学生用户列表
     */
    @GetMapping("/students")
    public Result<List<SysUser>> listStudents() {
        return Result.ok(sysUserService.getStudents());
    }

    /**
     * 添加新用户（教练或学生）
     * @param user 用户信息（用户名、密码、用户类型等）
     * @return 成功响应
     */
    @PostMapping("/user")
    public Result<Void> addUser(@RequestBody SysUser user) {
        sysUserService.save(user);
        return Result.ok();
    }

    /**
     * 为学生充值课时
     * @param rechargeDTO 充值信息（学生ID、充值课时数）
     * @return 成功响应
     */
    @PostMapping("/recharge")
    public Result<Void> recharge(@RequestBody RechargeDTO rechargeDTO) {
        sysUserService.recharge(rechargeDTO);
        return Result.ok();
    }

    /**
     * 获取数据统计面板信息
     * 包含：总预约数、各场地预约统计、各教练预约统计
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 包含统计数据的Map
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> dashboard = new HashMap<>();

        // 查询已确认的预约记录
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStatus, "confirmed");
        if (startDate != null) {
            wrapper.ge(Booking::getScheduleDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Booking::getScheduleDate, endDate);
        }

        List<Booking> confirmedBookings = bookingMapper.selectList(wrapper);
        dashboard.put("totalBookings", confirmedBookings.size());

        // 转换为VO并统计
        List<BookingVO> bookingVOs = confirmedBookings.stream().map(b -> {
            BookingVO vo = new BookingVO();
            BeanUtils.copyProperties(b, vo);
            SysUser student = sysUserService.getById(b.getStudentId());
            if (student != null) vo.setStudentName(student.getNickname());
            SysUser coach = sysUserService.getById(b.getCoachId());
            if (coach != null) vo.setCoachName(coach.getNickname());
            return vo;
        }).collect(Collectors.toList());

        // 按场地统计预约数
        Map<String, Long> venueStats = bookingVOs.stream()
                .collect(Collectors.groupingBy(BookingVO::getVenueName, Collectors.counting()));
        dashboard.put("venueStats", venueStats);

        // 按教练统计预约数
        Map<String, Long> coachStats = bookingVOs.stream()
                .collect(Collectors.groupingBy(BookingVO::getCoachName, Collectors.counting()));
        dashboard.put("coachStats", coachStats);

        return Result.ok(dashboard);
    }

    /**
     * 场地使用统计
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 各场地使用次数列表（按使用次数降序）
     */
    @GetMapping("/stats/venue")
    public Result<List<VenueStatsVO>> getVenueStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(bookingService.getVenueStats(startDate, endDate));
    }

    /**
     * 教练上课统计
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 各教练上课次数列表（按上课次数降序）
     */
    @GetMapping("/stats/coach")
    public Result<List<CoachStatsVO>> getCoachStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(bookingService.getCoachStats(startDate, endDate));
    }

    /**
     * 学生上课统计
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 各学生上课次数列表（按上课次数降序）
     */
    @GetMapping("/stats/student")
    public Result<List<StudentStatsVO>> getStudentStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(bookingService.getStudentStats(startDate, endDate));
    }
}
