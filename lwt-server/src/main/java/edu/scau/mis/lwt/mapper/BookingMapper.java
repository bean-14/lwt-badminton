package edu.scau.mis.lwt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.scau.mis.lwt.pojo.entity.Booking;
import edu.scau.mis.lwt.pojo.vo.CoachStatsVO;
import edu.scau.mis.lwt.pojo.vo.StudentStatsVO;
import edu.scau.mis.lwt.pojo.vo.VenueStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约数据访问层
 * 提供预约相关的数据库操作，包括统计查询
 * 统计SQL定义在 BookingMapper.xml 中
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {

    /**
     * 统计各场地使用次数
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 场地使用统计列表
     */
    List<VenueStatsVO> getVenueStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计各教练上课次数
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 教练上课统计列表
     */
    List<CoachStatsVO> getCoachStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计各学生上课次数
     * @param startDate 可选，统计开始日期
     * @param endDate 可选，统计结束日期
     * @return 学生上课统计列表
     */
    List<StudentStatsVO> getStudentStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
