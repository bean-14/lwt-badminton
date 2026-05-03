package edu.scau.mis.lwt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.scau.mis.lwt.common.base.BaseController;
import edu.scau.mis.lwt.common.result.R;
import edu.scau.mis.lwt.mapper.BookingMapper;
import edu.scau.mis.lwt.pojo.dto.RechargeDTO;
import edu.scau.mis.lwt.pojo.entity.Booking;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.pojo.entity.Venue;
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

    @GetMapping("/venues")
    public R<List<Venue>> listVenues() {
        return R.ok(venueService.list());
    }

    @PostMapping("/venue")
    public R<Void> addVenue(@RequestBody Venue venue) {
        venue.setStatus(1);
        venueService.save(venue);
        return R.ok();
    }

    @PutMapping("/venue")
    public R<Void> updateVenue(@RequestBody Venue venue) {
        venueService.updateById(venue);
        return R.ok();
    }

    @GetMapping("/coaches")
    public R<List<SysUser>> listCoaches() {
        return R.ok(sysUserService.getCoaches());
    }

    @GetMapping("/students")
    public R<List<SysUser>> listStudents() {
        return R.ok(sysUserService.getStudents());
    }

    @PostMapping("/user")
    public R<Void> addUser(@RequestBody SysUser user) {
        sysUserService.save(user);
        return R.ok();
    }

    @PostMapping("/recharge")
    public R<Void> recharge(@RequestBody RechargeDTO rechargeDTO) {
        sysUserService.recharge(rechargeDTO);
        return R.ok();
    }

    @GetMapping("/dashboard")
    public R<Map<String, Object>> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> dashboard = new HashMap<>();

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

        List<edu.scau.mis.lwt.pojo.vo.BookingVO> bookingVOs = confirmedBookings.stream().map(b -> {
            edu.scau.mis.lwt.pojo.vo.BookingVO vo = new edu.scau.mis.lwt.pojo.vo.BookingVO();
            BeanUtils.copyProperties(b, vo);
            SysUser student = sysUserService.getById(b.getStudentId());
            if (student != null) vo.setStudentName(student.getNickname());
            SysUser coach = sysUserService.getById(b.getCoachId());
            if (coach != null) vo.setCoachName(coach.getNickname());
            return vo;
        }).collect(Collectors.toList());

        Map<String, Long> venueStats = bookingVOs.stream()
                .collect(Collectors.groupingBy(edu.scau.mis.lwt.pojo.vo.BookingVO::getVenueName, Collectors.counting()));
        dashboard.put("venueStats", venueStats);

        Map<String, Long> coachStats = bookingVOs.stream()
                .collect(Collectors.groupingBy(edu.scau.mis.lwt.pojo.vo.BookingVO::getCoachName, Collectors.counting()));
        dashboard.put("coachStats", coachStats);

        return R.ok(dashboard);
    }
}
