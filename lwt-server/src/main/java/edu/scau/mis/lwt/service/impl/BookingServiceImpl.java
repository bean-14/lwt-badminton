package edu.scau.mis.lwt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.scau.mis.lwt.common.config.RabbitMQConfig;
import edu.scau.mis.lwt.common.exception.BusinessException;
import edu.scau.mis.lwt.mapper.BookingMapper;
import edu.scau.mis.lwt.mapper.CoachScheduleMapper;
import edu.scau.mis.lwt.mapper.SysUserMapper;
import edu.scau.mis.lwt.mapper.VenueMapper;
import edu.scau.mis.lwt.mq.BookingMessage;
import edu.scau.mis.lwt.pojo.entity.Booking;
import edu.scau.mis.lwt.pojo.entity.CoachSchedule;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.pojo.entity.Venue;
import edu.scau.mis.lwt.pojo.vo.BookingVO;
import edu.scau.mis.lwt.pojo.vo.CoachStatsVO;
import edu.scau.mis.lwt.pojo.vo.StudentStatsVO;
import edu.scau.mis.lwt.pojo.vo.VenueStatsVO;
import edu.scau.mis.lwt.service.BookingService;
import edu.scau.mis.lwt.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl extends ServiceImpl<BookingMapper, Booking> implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Autowired
    private CoachScheduleMapper coachScheduleMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private NotificationService notificationService;

    private final RabbitTemplate rabbitTemplate;

    public BookingServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public Long book(Long studentId, Long scheduleId) {
        String lockKey = "booking_lock:" + studentId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(60));
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(400, "操作太频繁，请稍后再试");
        }

        try {
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
                   .in(Booking::getStatus, "pending", "confirmed")
                   .lt(Booking::getStartTime, schedule.getEndTime())
                   .gt(Booking::getEndTime, schedule.getStartTime());
            if (count(wrapper) > 0) {
                throw new BusinessException(400, "该场地该时段已被预约");
            }

            Booking booking = new Booking();
            booking.setStudentId(studentId);
            booking.setCoachId(schedule.getCoachId());
            booking.setVenueId(schedule.getVenueId());
            booking.setScheduleDate(schedule.getScheduleDate());
            booking.setStartTime(schedule.getStartTime());
            booking.setEndTime(schedule.getEndTime());
            booking.setStatus("pending");
            save(booking);

            Long bookingId = booking.getId();
            Long coachId = schedule.getCoachId();

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                if (notificationService.isUserOnline(coachId)) {
                                    SysUser student = sysUserMapper.selectById(studentId);
                                    String studentName = student != null ? student.getNickname() : "未知";
                                    notificationService.notifyCoachNewBooking(coachId, bookingId, studentName);
                                }
                            } catch (Exception e) {
                                log.warn("WebSocket push to coach {} failed, MQ will handle", coachId, e);
                            }

                            try {
                                rabbitTemplate.convertAndSend(
                                        RabbitMQConfig.EXCHANGE_BOOKING,
                                        RabbitMQConfig.ROUTING_BOOKING_NEW,
                                        new BookingMessage(bookingId, coachId, studentId)
                                );
                                log.info("MQ message sent for bookingId: {}", bookingId);
                            } catch (Exception e) {
                                log.error("Failed to send MQ message for bookingId: {}", bookingId, e);
                            }
                        }
                    }
            );

            return bookingId;

        } catch (Exception e) {
            redisTemplate.delete(lockKey);
            throw e;
        }
    }

    @Override
    @Transactional
    public void confirm(Long coachId, Long bookingId) {
        String lockKey = "booking_update:" + bookingId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(30));
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(400, "该预约正在处理中，请稍后重试");
        }

        try {
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

            int updated = sysUserMapper.deductHours(booking.getStudentId(), 1);
            if (updated == 0) {
                throw new BusinessException(400, "扣减课时失败，学生课时不足");
            }

            Long studentId = booking.getStudentId();

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                SysUser coach = sysUserMapper.selectById(coachId);
                                String coachName = coach != null ? coach.getNickname() : "未知";
                                notificationService.notifyStudentConfirmed(studentId, bookingId, coachName);
                                log.info("Confirmation pushed to student {} via WebSocket", studentId);
                            } catch (Exception e) {
                                log.warn("WebSocket push to student {} failed", studentId, e);
                            }
                        }

                        @Override
                        public void afterCompletion(int status) {
                            redisTemplate.delete(lockKey);
                        }
                    }
            );

        } catch (RuntimeException e) {
            redisTemplate.delete(lockKey);
            throw e;
        }
    }

    @Override
    @Transactional
    public void reject(Long coachId, Long bookingId) {
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

        booking.setStatus("rejected");
        updateById(booking);
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
    @Transactional
    public void leave(Long studentId, Long bookingId, String reason) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预约不存在");
        }
        if (!booking.getStudentId().equals(studentId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (!"confirmed".equals(booking.getStatus())) {
            throw new BusinessException(400, "只能对已确认的预约请假");
        }

        booking.setLeaveReason(reason);
        booking.setLeaveTime(LocalDateTime.now());

        LocalDate today = LocalDate.now();
        if (booking.getScheduleDate().isAfter(today)) {
            SysUser student = sysUserMapper.selectById(studentId);
            if (student != null) {
                student.setRemainingHours(student.getRemainingHours() + 1);
                sysUserMapper.updateById(student);
            }
            booking.setStatus("cancelled");
        } else {
            booking.setStatus("leave_pending");
        }

        updateById(booking);
    }

    @Override
    @Transactional
    public void handleLeave(Long coachId, Long bookingId, String action) {
        Booking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预约不存在");
        }
        if (!booking.getCoachId().equals(coachId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (!"leave_pending".equals(booking.getStatus())) {
            throw new BusinessException(400, "该预约没有待处理的请假申请");
        }

        if ("approve".equals(action)) {
            SysUser student = sysUserMapper.selectById(booking.getStudentId());
            if (student != null) {
                student.setRemainingHours(student.getRemainingHours() + 1);
                sysUserMapper.updateById(student);
            }
            booking.setStatus("cancelled");
        } else if ("reject".equals(action)) {
            booking.setStatus("confirmed");
        } else {
            throw new BusinessException(400, "无效操作，仅支持 approve/reject");
        }

        updateById(booking);
    }

    @Transactional
    public void processExpiredBookings() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStatus, "confirmed")
               .and(w -> w.lt(Booking::getScheduleDate, today)
                          .or(w2 -> w2.eq(Booking::getScheduleDate, today)
                                      .isNotNull(Booking::getEndTime)
                                      .lt(Booking::getEndTime, now)));
        List<Booking> expiredConfirmed = list(wrapper);
        for (Booking b : expiredConfirmed) {
            b.setStatus("completed");
        }
        updateBatchById(expiredConfirmed);

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStatus, "pending")
               .and(w -> w.lt(Booking::getScheduleDate, today)
                          .or(w2 -> w2.eq(Booking::getScheduleDate, today)
                                      .isNotNull(Booking::getEndTime)
                                      .lt(Booking::getEndTime, now)));
        List<Booking> expiredPending = list(wrapper);
        for (Booking b : expiredPending) {
            b.setStatus("no_confirm");
        }
        updateBatchById(expiredPending);

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStatus, "leave_pending")
               .and(w -> w.lt(Booking::getScheduleDate, today)
                          .or(w2 -> w2.eq(Booking::getScheduleDate, today)
                                      .isNotNull(Booking::getEndTime)
                                      .lt(Booking::getEndTime, now)));
        List<Booking> expiredLeave = list(wrapper);
        for (Booking b : expiredLeave) {
            b.setStatus("completed");
        }
        updateBatchById(expiredLeave);

        if (!expiredConfirmed.isEmpty() || !expiredPending.isEmpty() || !expiredLeave.isEmpty()) {
            log.info("Processed expired bookings: {} confirmed→completed, {} pending→no_confirm, {} leave_pending→completed",
                    expiredConfirmed.size(), expiredPending.size(), expiredLeave.size());
        }
    }

    @Override
    public List<BookingVO> getStudentBookings(Long studentId, LocalDate date) {
        processExpiredBookings();
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
        processExpiredBookings();
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
        processExpiredBookings();
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getStudentId, studentId)
               .eq(Booking::getStatus, "completed")
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

    @Override
    public List<VenueStatsVO> getVenueStats(LocalDate startDate, LocalDate endDate) {
        List<VenueStatsVO> result = baseMapper.getVenueStats(startDate, endDate);
        log.info("getVenueStats result: {}", result);
        return result;
    }

    @Override
    public List<CoachStatsVO> getCoachStats(LocalDate startDate, LocalDate endDate) {
        return baseMapper.getCoachStats(startDate, endDate);
    }

    @Override
    public List<StudentStatsVO> getStudentStats(LocalDate startDate, LocalDate endDate) {
        return baseMapper.getStudentStats(startDate, endDate);
    }
}
