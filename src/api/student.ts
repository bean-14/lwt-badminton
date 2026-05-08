import { http } from "@/utils/http";

/** 可预约时段 */
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

/** 预约记录 */
export type BookingVO = {
  id: number;
  studentId: number;
  studentName: string;
  coachId: number;
  coachName: string;
  venueId: number;
  venueName: string;
  scheduleDate: string;
  startTime?: string;
  endTime?: string;
  status: string;
  createTime: string;
  confirmTime: string;
  leaveReason?: string;
  leaveTime?: string;
};

/** 教练信息 */
export type CoachInfo = {
  id: number;
  username: string;
  nickname: string;
  phone: string;
  userType: string;
};

/** 我的信息（含剩余课时） */
export type StudentInfo = {
  id: number;
  username: string;
  nickname: string;
  phone: string;
  remainingHours: number;
};

/** 查看可预约时段 */
export const getSchedulesApi = (params?: {
  date?: string;
  coachId?: number;
}) => {
  return http.request<ScheduleVO[]>("get", "/student/schedules", { params });
};

/** 预约课程 */
export const bookApi = (scheduleId: number) => {
  return http.request<null>("post", `/student/book/${scheduleId}`);
};

/** 查看我的预约 */
export const getMyBookingsApi = (params?: { date?: string }) => {
  return http.request<BookingVO[]>("get", "/student/bookings", { params });
};

/** 查看上课历史 */
export const getHistoryApi = () => {
  return http.request<BookingVO[]>("get", "/student/history");
};

/** 取消预约 */
export const cancelBookingApi = (bookingId: number) => {
  return http.request<null>("post", `/student/cancel/${bookingId}`);
};

/** 申请请假 */
export const leaveApi = (bookingId: number, reason: string) => {
  return http.request<null>("post", `/student/leave/${bookingId}`, {
    params: { reason }
  });
};

/** 查看所有教练 */
export const getCoachesApi = () => {
  return http.request<CoachInfo[]>("get", "/student/coaches");
};

/** 查看个人信息 */
export const getMyInfoApi = () => {
  return http.request<StudentInfo>("get", "/student/info");
};
