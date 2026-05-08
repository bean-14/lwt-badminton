import { http } from "@/utils/http";

/** 场地信息 */
export type Venue = {
  id?: number;
  name: string;
  location?: string;
  status?: number;
};

/** 用户信息 */
export type UserInfo = {
  id: number;
  username: string;
  nickname: string;
  phone: string;
  userType: string;
  remainingHours: number;
  status: number;
};

/** 仪表盘统计 */
export type DashboardData = {
  totalBookings: number;
  venueStats: Record<string, number>;
  coachStats: Record<string, number>;
};

/** 获取场地列表 */
export const getVenuesApi = () => {
  return http.request<Venue[]>("get", "/admin/venues");
};

/** 新增场地 */
export const addVenueApi = (data: Venue) => {
  return http.request<null>("post", "/admin/venue", { data });
};

/** 更新场地 */
export const updateVenueApi = (data: Venue) => {
  return http.request<null>("put", "/admin/venue", { data });
};

/** 查看教练列表 */
export const getCoachesApi = () => {
  return http.request<UserInfo[]>("get", "/admin/coaches");
};

/** 查看学生列表 */
export const getStudentsApi = () => {
  return http.request<UserInfo[]>("get", "/admin/students");
};

/** 新增用户 */
export const addUserApi = (data: {
  username: string;
  password: string;
  nickname?: string;
  phone?: string;
  userType: string;
}) => {
  return http.request<null>("post", "/admin/user", { data });
};

/** 课时充值 */
export const rechargeApi = (data: { userId: number; hours: number }) => {
  return http.request<null>("post", "/admin/recharge", { data });
};

/** 获取仪表盘数据 */
export const getDashboardApi = (params?: {
  startDate?: string;
  endDate?: string;
}) => {
  return http.request<DashboardData>("get", "/admin/dashboard", { params });
};

/** 场地使用统计 */
export type VenueStatsItem = {
  venueName: string;
  location: string;
  usageCount: number;
};

/** 教练上课统计 */
export type CoachStatsItem = {
  coachId: number;
  coachName: string;
  classCount: number;
};

/** 学生上课统计 */
export type StudentStatsItem = {
  studentId: number;
  studentName: string;
  classCount: number;
};

/** 场地使用统计 */
export const getVenueStatsApi = (params?: {
  startDate?: string;
  endDate?: string;
}) => {
  return http.request<VenueStatsItem[]>("get", "/admin/stats/venue", {
    params
  });
};

/** 教练上课统计 */
export const getCoachStatsApi = (params?: {
  startDate?: string;
  endDate?: string;
}) => {
  return http.request<CoachStatsItem[]>("get", "/admin/stats/coach", {
    params
  });
};

/** 学生上课统计 */
export const getStudentStatsApi = (params?: {
  startDate?: string;
  endDate?: string;
}) => {
  return http.request<StudentStatsItem[]>("get", "/admin/stats/student", {
    params
  });
};
