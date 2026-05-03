package edu.scau.mis.lwt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.scau.mis.lwt.pojo.dto.ScheduleDTO;
import edu.scau.mis.lwt.pojo.entity.CoachSchedule;
import edu.scau.mis.lwt.pojo.vo.ScheduleVO;

import java.time.LocalDate;
import java.util.List;

public interface CoachScheduleService extends IService<CoachSchedule> {

    void addSchedule(ScheduleDTO scheduleDTO);

    List<ScheduleVO> getAvailableSchedules(LocalDate date, Long coachId);

    List<ScheduleVO> getCoachSchedules(Long coachId, LocalDate date);

    void deleteSchedule(Long scheduleId);
}
