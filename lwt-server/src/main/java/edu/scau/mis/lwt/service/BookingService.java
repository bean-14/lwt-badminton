package edu.scau.mis.lwt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.scau.mis.lwt.pojo.entity.Booking;
import edu.scau.mis.lwt.pojo.vo.BookingVO;

import java.time.LocalDate;
import java.util.List;

public interface BookingService extends IService<Booking> {

    void book(Long studentId, Long scheduleId);

    void confirm(Long coachId, Long bookingId);

    void cancel(Long userId, Long bookingId);

    List<BookingVO> getStudentBookings(Long studentId, LocalDate date);

    List<BookingVO> getCoachBookings(Long coachId, LocalDate date);

    List<BookingVO> getHistory(Long studentId);
}
