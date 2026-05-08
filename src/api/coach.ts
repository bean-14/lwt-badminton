import { http } from "@/utils/http";

/** 排课VO（与student中的ScheduleVO一致） */
export type ScheduleVO = {
  id: number;
  coachId: number;
  coachName: string;
  venueId: number;
  venueName: string;
  scheduleDate: string;
  startTime: string;
  endTime: string;
  bookedCount: number;
  status: number;
};

/** 预约记录VO */
export type BookingVO = {
  id: number;
  studentId: number;
  studentName: string;
  coachId: number;
  coachName: string;
  venueId: number;
  venueName: string;
  scheduleDate: string;
  status: string;
  createTime: string;
  confirmTime: string;
};

/** 设置可预约时段 */
export const addScheduleApi = (data: {
  coachId: number;
  venueId: number;
  scheduleDate: string;
  startTime: string;
  endTime: string;
}) => {
  return http.request<null>("post", "/coach/schedule", { data });
};

/** 查看我的排课 */
export const getMySchedulesApi = (params?: { date?: string }) => {
  return http.request<ScheduleVO[]>("get", "/coach/schedules", { params });
};

/** 删除排课时段 */
export const deleteScheduleApi = (scheduleId: number) => {
  return http.request<null>("delete", `/coach/schedule/${scheduleId}`);
};

/** 查看预约列表 */
export const getCoachBookingsApi = (params?: { date?: string }) => {
  return http.request<BookingVO[]>("get", "/coach/bookings", { params });
};

/** 确认预约 */
export const confirmBookingApi = (bookingId: number) => {
  return http.request<null>("post", `/coach/confirm/${bookingId}`);
};
