package edu.scau.mis.lwt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.scau.mis.lwt.pojo.entity.Booking;
import edu.scau.mis.lwt.pojo.vo.BookingVO;
import edu.scau.mis.lwt.pojo.vo.CoachStatsVO;
import edu.scau.mis.lwt.pojo.vo.StudentStatsVO;
import edu.scau.mis.lwt.pojo.vo.VenueStatsVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 * 定义课程预约相关的业务逻辑方法
 * 继承MyBatis-Plus的IService，获得基础CRUD能力
 */
public interface BookingService extends IService<Booking> {

    /**
     * 学生预约课程
     * @param studentId 学生ID
     * @param scheduleId 课程安排ID
     * @return 预约记录ID
     */
    Long book(Long studentId, Long scheduleId);

    /**
     * 教练确认预约（扣减学生课时）
     * @param coachId 教练ID
     * @param bookingId 预约ID
     */
    void confirm(Long coachId, Long bookingId);

    /**
     * 教练拒绝预约
     * @param coachId 教练ID
     * @param bookingId 预约ID
     */
    void reject(Long coachId, Long bookingId);

    /**
     * 取消预约
     * @param userId 用户ID（学生或教练）
     * @param bookingId 预约ID
     */
    void cancel(Long userId, Long bookingId);

    /**
     * 学生申请请假
     * @param studentId 学生ID
     * @param bookingId 预约ID
     * @param reason 请假原因
     */
    void leave(Long studentId, Long bookingId, String reason);

    /**
     * 教练处理请假申请
     * @param coachId 教练ID
     * @param bookingId 预约ID
     * @param action approve 同意 / reject 拒绝
     */
    void handleLeave(Long coachId, Long bookingId, String action);

    /**
     * 获取学生的预约记录
     * @param studentId 学生ID
     * @param date 可选，按日期筛选
     * @return 预约记录VO列表
     */
    List<BookingVO> getStudentBookings(Long studentId, LocalDate date);

    /**
     * 获取教练的课程预约记录
     * @param coachId 教练ID
     * @param date 可选，按日期筛选
     * @return 预约记录VO列表
     */
    List<BookingVO> getCoachBookings(Long coachId, LocalDate date);

    /**
     * 获取学生的历史预约（已完成的课程）
     * @param studentId 学生ID
     * @return 已确认的预约记录VO列表
     */
    List<BookingVO> getHistory(Long studentId);

    /**
     * 统计各场地使用次数
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 场地使用统计列表
     */
    List<VenueStatsVO> getVenueStats(LocalDate startDate, LocalDate endDate);

    /**
     * 统计各教练上课次数
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 教练上课统计列表
     */
    List<CoachStatsVO> getCoachStats(LocalDate startDate, LocalDate endDate);

    /**
     * 统计各学生上课次数
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 学生上课统计列表
     */
    List<StudentStatsVO> getStudentStats(LocalDate startDate, LocalDate endDate);
}
