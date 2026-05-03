package edu.scau.mis.lwt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.scau.mis.lwt.pojo.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
}
