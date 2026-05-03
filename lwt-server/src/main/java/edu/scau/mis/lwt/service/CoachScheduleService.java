package edu.scau.mis.lwt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.scau.mis.lwt.pojo.dto.ScheduleDTO;
import edu.scau.mis.lwt.pojo.entity.CoachSchedule;
import edu.scau.mis.lwt.pojo.vo.ScheduleVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 教练排课服务接口
 * 定义教练排课相关的业务逻辑方法
 * 继承MyBatis-Plus的IService，获得基础CRUD能力
 */
public interface CoachScheduleService extends IService<CoachSchedule> {

    /**
     * 添加排课时间段
     * @param scheduleDTO 排课信息（教练ID、场地ID、日期、时间等）
     */
    void addSchedule(ScheduleDTO scheduleDTO);

    /**
     * 获取可用的排课列表（学生查看可预约的课程）
     * @param date 可选，按日期筛选
     * @param coachId 可选，按教练筛选
     * @return 可用排课VO列表
     */
    List<ScheduleVO> getAvailableSchedules(LocalDate date, Long coachId);

    /**
     * 获取教练自己的排课列表
     * @param coachId 教练ID
     * @param date 可选，按日期筛选
     * @return 排课VO列表
     */
    List<ScheduleVO> getCoachSchedules(Long coachId, LocalDate date);

    /**
     * 删除排课时间段
     * @param scheduleId 排课ID
     */
    void deleteSchedule(Long scheduleId);
}
