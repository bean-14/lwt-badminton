package edu.scau.mis.lwt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.scau.mis.lwt.common.exception.BusinessException;
import edu.scau.mis.lwt.mapper.BookingMapper;
import edu.scau.mis.lwt.mapper.CoachScheduleMapper;
import edu.scau.mis.lwt.mapper.SysUserMapper;
import edu.scau.mis.lwt.mapper.VenueMapper;
import edu.scau.mis.lwt.pojo.entity.Booking;
import edu.scau.mis.lwt.pojo.entity.CoachSchedule;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.pojo.entity.Venue;
import edu.scau.mis.lwt.pojo.vo.BookingVO;
import edu.scau.mis.lwt.service.BookingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    @Autowired
    private CoachScheduleMapper coachScheduleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private VenueMapper venueMapper;

    @Override
    @Transactional
    public void book(Long studentId, Long scheduleId) {
        CoachSchedule schedule = coachScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排课不存在");
        }
        if (schedule.getStatus() != 1) {
            throw new BusinessException(400, "该时段不可预约");
        }

        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (student.getRemainingHours() == null || student.getRemainingHours() <= 0) {
            throw new BusinessException(400, "课时不足，请先充值");
        }

        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStudentId, studentId)
               .eq(Booking::getScheduleDate, schedule.getScheduleDate())
               .eq(Booking::getCoachId, schedule.getCoachId())
               .in(Booking::getStatus, "pending", "confirmed");
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "您已预约该教练该时段的课程");
        }

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getVenueId, schedule.getVenueId())
               .eq(Booking::getScheduleDate, schedule.getScheduleDate())
               .in(Booking::getStatus, "pending", "confirmed");
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "该场地该时段已被预约");
        }

        Booking booking = new Booking();
        booking.setStudentId(studentId);
        booking.setCoachId(schedule.getCoachId());
        booking.setVenueId(schedule.getVenueId());
        booking.setScheduleDate(schedule.getScheduleDate());
        booking.setStatus("pending");
        save(booking);
    }

    @Override
    @Transactional
    public void confirm(Long coachId, Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预约不存在");
        }
        if (!booking.getCoachId().equals(coachId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (!"pending".equals(booking.getStatus())) {
            throw new BusinessException(400, "该预约已处理");
        }

        booking.setStatus("confirmed");
        booking.setConfirmTime(LocalDateTime.now());
        updateById(booking);

        SysUser student = sysUserMapper.selectById(booking.getStudentId());
        if (student.getRemainingHours() != null && student.getRemainingHours() > 0) {
            student.setRemainingHours(student.getRemainingHours() - 1);
            sysUserMapper.updateById(student);
        }
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预约不存在");
        }
        if (!booking.getStudentId().equals(userId) && !booking.getCoachId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if ("confirmed".equals(booking.getStatus())) {
            throw new BusinessException(400, "已确认的预约无法取消");
        }

        booking.setStatus("cancelled");
        updateById(booking);
    }

    @Override
    public List<BookingVO> getStudentBookings(Long studentId, LocalDate date) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStudentId, studentId);
        if (date != null) {
            wrapper.eq(Booking::getScheduleDate, date);
        }
        wrapper.orderByDesc(Booking::getCreateTime);
        return buildBookingVOs(list(wrapper));
    }

    @Override
    public List<BookingVO> getCoachBookings(Long coachId, LocalDate date) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getCoachId, coachId);
        if (date != null) {
            wrapper.eq(Booking::getScheduleDate, date);
        }
        wrapper.orderByAsc(Booking::getScheduleDate);
        return buildBookingVOs(list(wrapper));
    }

    @Override
    public List<BookingVO> getHistory(Long studentId) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStudentId, studentId)
               .eq(Booking::getStatus, "confirmed")
               .orderByDesc(Booking::getCreateTime);
        return buildBookingVOs(list(wrapper));
    }

    private List<BookingVO> buildBookingVOs(List<Booking> bookings) {
        return bookings.stream().map(booking -> {
            BookingVO vo = new BookingVO();
            BeanUtils.copyProperties(booking, vo);

            SysUser student = sysUserMapper.selectById(booking.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getNickname());
            }

            SysUser coach = sysUserMapper.selectById(booking.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getNickname());
            }

            Venue venue = venueMapper.selectById(booking.getVenueId());
            if (venue != null) {
                vo.setVenueName(venue.getName());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
