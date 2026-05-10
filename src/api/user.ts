import { http } from "@/utils/http";

/** 登录响应 */
export type LoginResult = {
  token: string;
  userId: number;
  userType: string;
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

/** 登录 */
export const loginApi = (data: { username: string; password: string }) => {
  return http.request<LoginResult>("post", "/auth/login", { data });
};

/** 注册 */
export const registerApi = (data: {
  username: string;
  password: string;
  nickname?: string;
  phone?: string;
  userType: string;
}) => {
  return http.request<null>("post", "/auth/register", { data });
};

/** 获取当前用户信息 */
export const getUserInfoApi = () => {
  return http.request<UserInfo>("get", "/auth/info");
};

/** 标记用户在线 */
export const setOnlineApi = () => {
  return http.request<null>("post", "/user/online");
};

/** 标记用户离线 */
export const setOfflineApi = () => {
  return http.request<null>("post", "/user/offline");
};
