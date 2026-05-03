package edu.scau.mis.lwt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.scau.mis.lwt.common.exception.BusinessException;
import edu.scau.mis.lwt.mapper.BookingMapper;
import edu.scau.mis.lwt.mapper.CoachScheduleMapper;
import edu.scau.mis.lwt.mapper.SysUserMapper;
import edu.scau.mis.lwt.mapper.VenueMapper;
import edu.scau.mis.lwt.pojo.dto.ScheduleDTO;
import edu.scau.mis.lwt.pojo.entity.Booking;
import edu.scau.mis.lwt.pojo.entity.CoachSchedule;
import edu.scau.mis.lwt.pojo.entity.SysUser;
import edu.scau.mis.lwt.pojo.entity.Venue;
import edu.scau.mis.lwt.pojo.vo.ScheduleVO;
import edu.scau.mis.lwt.service.CoachScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoachScheduleServiceImpl extends ServiceImpl<CoachScheduleMapper, CoachSchedule> implements CoachScheduleService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private VenueMapper venueMapper;

    @Override
    public void addSchedule(ScheduleDTO scheduleDTO) {
        LambdaQueryWrapper<CoachSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachSchedule::getCoachId, scheduleDTO.getCoachId())
               .eq(CoachSchedule::getScheduleDate, scheduleDTO.getScheduleDate())
               .eq(CoachSchedule::getVenueId, scheduleDTO.getVenueId())
               .eq(CoachSchedule::getStatus, 1)
               .and(w -> w
                   .between(CoachSchedule::getStartTime, scheduleDTO.getStartTime(), scheduleDTO.getEndTime())
                   .or()
                   .between(CoachSchedule::getEndTime, scheduleDTO.getStartTime(), scheduleDTO.getEndTime())
                   .or()
                   .le(CoachSchedule::getStartTime, scheduleDTO.getStartTime())
                   .ge(CoachSchedule::getEndTime, scheduleDTO.getEndTime()));

        if (count(wrapper) > 0) {
            throw new BusinessException(400, "该时段与已有排课冲突");
        }

        CoachSchedule schedule = new CoachSchedule();
        BeanUtils.copyProperties(scheduleDTO, schedule);
        schedule.setStatus(1);
        save(schedule);
    }

    @Override
    public List<ScheduleVO> getAvailableSchedules(LocalDate date, Long coachId) {
        LambdaQueryWrapper<CoachSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachSchedule::getScheduleDate, date)
               .eq(CoachSchedule::getStatus, 1);
        if (coachId != null) {
            wrapper.eq(CoachSchedule::getCoachId, coachId);
        }
        wrapper.orderByAsc(CoachSchedule::getStartTime);

        List<CoachSchedule> schedules = list(wrapper);
        return buildScheduleVOs(schedules);
    }

    @Override
    public List<ScheduleVO> getCoachSchedules(Long coachId, LocalDate date) {
        LambdaQueryWrapper<CoachSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CoachSchedule::getCoachId, coachId);
        if (date != null) {
            wrapper.eq(CoachSchedule::getScheduleDate, date);
        }
        wrapper.orderByAsc(CoachSchedule::getScheduleDate)
               .orderByAsc(CoachSchedule::getStartTime);

        return buildScheduleVOs(list(wrapper));
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        CoachSchedule schedule = getById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排课不存在");
        }

        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getCoachId, schedule.getCoachId())
               .eq(Booking::getScheduleDate, schedule.getScheduleDate())
               .eq(Booking::getStatus, "pending");
        if (bookingMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该时段已有学生预约，无法删除");
        }

        removeById(scheduleId);
    }

    private List<ScheduleVO> buildScheduleVOs(List<CoachSchedule> schedules) {
        return schedules.stream().map(schedule -> {
            ScheduleVO vo = new ScheduleVO();
            BeanUtils.copyProperties(schedule, vo);

            SysUser coach = sysUserMapper.selectById(schedule.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getNickname());
            }

            Venue venue = venueMapper.selectById(schedule.getVenueId());
            if (venue != null) {
                vo.setVenueName(venue.getName());
            }

            LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Booking::getCoachId, schedule.getCoachId())
                   .eq(Booking::getScheduleDate, schedule.getScheduleDate())
                   .in(Booking::getStatus, "pending", "confirmed");
            vo.setBookedCount(Math.toIntExact(bookingMapper.selectCount(wrapper)));

            return vo;
        }).collect(Collectors.toList());
    }
}
