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
import edu.scau.mis.lwt.pojo.vo.CoachStatsVO;
import edu.scau.mis.lwt.pojo.vo.StudentStatsVO;
import edu.scau.mis.lwt.pojo.vo.VenueStatsVO;
import edu.scau.mis.lwt.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 * 处理所有课程预约相关的业务逻辑
 * 包括预约、确认、取消、查询等功能
 */
@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Autowired
    private CoachScheduleMapper coachScheduleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private VenueMapper venueMapper;

    /**
     * 学生预约课程
     * 包含完整的冲突检测逻辑：
     * 1. 检查排课是否存在且可用
     * 2. 检查学生是否存在且有足够课时
     * 3. 检查学生是否重复预约同一教练同一时段
     * 4. 检查场地是否已被预约
     * @param studentId 学生ID
     * @param scheduleId 课程安排ID
     */
    @Override
    @Transactional
    public void book(Long studentId, Long scheduleId) {
        // 1. 检查排课是否存在且可用
        CoachSchedule schedule = coachScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排课不存在");
        }
        if (schedule.getStatus() != 1) {
            throw new BusinessException(400, "该时段不可预约");
        }

        // 2. 检查学生是否存在且有足够课时
        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (student.getRemainingHours() == null || student.getRemainingHours() <= 0) {
            throw new BusinessException(400, "课时不足，请先充值");
        }

        // 3. 检查学生是否重复预约同一教练同一时段
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStudentId, studentId)
               .eq(Booking::getScheduleDate, schedule.getScheduleDate())
               .eq(Booking::getCoachId, schedule.getCoachId())
               .in(Booking::getStatus, "pending", "confirmed");
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "您已预约该教练该时段的课程");
        }

        // 4. 检查场地是否已被预约
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getVenueId, schedule.getVenueId())
               .eq(Booking::getScheduleDate, schedule.getScheduleDate())
               .in(Booking::getStatus, "pending", "confirmed");
        if (count(wrapper) > 0) {
            throw new BusinessException(400, "该场地该时段已被预约");
        }

        // 创建预约记录
        Booking booking = new Booking();
        booking.setStudentId(studentId);
        booking.setCoachId(schedule.getCoachId());
        booking.setVenueId(schedule.getVenueId());
        booking.setScheduleDate(schedule.getScheduleDate());
        booking.setStatus("pending");
        save(booking);
    }

    /**
     * 教练确认预约（扣减课时）
     * @param coachId 教练ID（用于验证权限）
     * @param bookingId 预约ID
     */
    @Override
    @Transactional
    public void confirm(Long coachId, Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预约不存在");
        }
        // 验证操作权限：只能确认自己的预约
        if (!booking.getCoachId().equals(coachId)) {
            throw new BusinessException(403, "无权操作");
        }
        // 只能确认待处理的预约
        if (!"pending".equals(booking.getStatus())) {
            throw new BusinessException(400, "该预约已处理");
        }

        // 更新预约状态为已确认
        booking.setStatus("confirmed");
        booking.setConfirmTime(LocalDateTime.now());
        updateById(booking);

        // 扣减学生课时
        SysUser student = sysUserMapper.selectById(booking.getStudentId());
        if (student.getRemainingHours() != null && student.getRemainingHours() > 0) {
            student.setRemainingHours(student.getRemainingHours() - 1);
            sysUserMapper.updateById(student);
        }
    }

    /**
     * 取消预约
     * @param userId 用户ID（学生或教练）
     * @param bookingId 预约ID
     */
    @Override
    @Transactional
    public void cancel(Long userId, Long bookingId) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预约不存在");
        }
        // 验证权限：只有学生本人或教练可以取消
        if (!booking.getStudentId().equals(userId) && !booking.getCoachId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        // 已确认的预约无法取消
        if ("confirmed".equals(booking.getStatus())) {
            throw new BusinessException(400, "已确认的预约无法取消");
        }

        booking.setStatus("cancelled");
        updateById(booking);
    }

    /**
     * 获取学生的预约记录
     * @param studentId 学生ID
     * @param date 可选，按日期筛选
     * @return 预约记录列表（按创建时间倒序）
     */
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

    /**
     * 获取教练的课程预约记录
     * @param coachId 教练ID
     * @param date 可选，按日期筛选
     * @return 预约记录列表（按预约日期正序）
     */
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

    /**
     * 获取学生的历史预约（已完成的课程）
     * @param studentId 学生ID
     * @return 已确认的预约记录列表
     */
    @Override
    public List<BookingVO> getHistory(Long studentId) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStudentId, studentId)
               .eq(Booking::getStatus, "confirmed")
               .orderByDesc(Booking::getCreateTime);
        return buildBookingVOs(list(wrapper));
    }

    /**
     * 构建BookingVO列表（关联查询教练、学生、场地信息）
     * @param bookings 预约实体列表
     * @return 包含详细信息的BookingVO列表
     */
    private List<BookingVO> buildBookingVOs(List<Booking> bookings) {
        return bookings.stream().map(booking -> {
            BookingVO vo = new BookingVO();
            BeanUtils.copyProperties(booking, vo);

            // 查询学生昵称
            SysUser student = sysUserMapper.selectById(booking.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getNickname());
            }

            // 查询教练昵称
            SysUser coach = sysUserMapper.selectById(booking.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getNickname());
            }

            // 查询场地名称
            Venue venue = venueMapper.selectById(booking.getVenueId());
            if (venue != null) {
                vo.setVenueName(venue.getName());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 统计各场地使用次数
     */
    @Override
    public List<VenueStatsVO> getVenueStats(LocalDate startDate, LocalDate endDate) {
        List<VenueStatsVO> result = baseMapper.getVenueStats(startDate, endDate);
        log.info("getVenueStats result: {}", result);
        return result;
    }

    /**
     * 统计各教练上课次数
     */
    @Override
    public List<CoachStatsVO> getCoachStats(LocalDate startDate, LocalDate endDate) {
        return baseMapper.getCoachStats(startDate, endDate);
    }

    /**
     * 统计各学生上课次数
     */
    @Override
    public List<StudentStatsVO> getStudentStats(LocalDate startDate, LocalDate endDate) {
        return baseMapper.getStudentStats(startDate, endDate);
    }
}
