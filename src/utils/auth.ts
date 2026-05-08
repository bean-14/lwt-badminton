import Cookies from "js-cookie";
import { storageLocal } from "@pureadmin/utils";

export interface DataInfo<T> {
  /** JWT token */
  accessToken?: string;
  /** token过期时间戳 */
  expires: T;
  /** 用户ID */
  userId?: number;
  /** 用户类型 */
  userType?: string;
  /** 昵称 */
  nickname?: string;
  /** 角色列表（兼容模板） */
  roles?: Array<string>;
}

export const userKey = "user-info";
export const TokenKey = "authorized-token";
export const multipleTabsKey = "multiple-tabs";

/** 获取token（仅从cookie读取，不暴露到localStorage） */
export function getToken(): DataInfo<number> | undefined {
  const cookie = Cookies.get(TokenKey);
  return cookie ? JSON.parse(cookie) : undefined;
}

/**
 * 设置token及用户信息
 * token仅存cookie（SameSite=Strict防CSRF），localStorage只存非敏感信息
 */
export function setToken(data: {
  token: string;
  userId: number;
  userType: string;
  nickname?: string;
}) {
  const expires = Date.now() + 24 * 60 * 60 * 1000;
  const cookieString = JSON.stringify({
    accessToken: data.token,
    expires
  });
  // 仅cookie存储token，过期时间与后端JWT一致（1天）
  Cookies.set(TokenKey, cookieString, {
    expires: 1,
    sameSite: "strict"
  });
  Cookies.set(multipleTabsKey, "true", {
    expires: 1,
    sameSite: "strict"
  });

  // localStorage只存非敏感信息（不含token）
  storageLocal().setItem(userKey, {
    expires,
    userId: data.userId,
    userType: data.userType,
    nickname: data.nickname || "",
    roles: [data.userType]
  });
}

/** 删除token及用户信息 */
export function removeToken() {
  Cookies.remove(TokenKey);
  Cookies.remove(multipleTabsKey);
  storageLocal().removeItem(userKey);
}

/** 格式化token（jwt格式） */
export const formatToken = (token: string): string => {
  return "Bearer " + token;
};

/** 头像存储key前缀 */
const AVATAR_KEY = "user-avatar-";

/** 保存头像（base64存localStorage） */
export function setAvatar(userId: number, base64: string) {
  storageLocal().setItem(AVATAR_KEY + userId, base64);
}

/** 获取头像 */
export function getAvatar(userId: number): string | null {
  return storageLocal().getItem(AVATAR_KEY + userId);
}

/** 删除头像 */
export function removeAvatar(userId: number) {
  storageLocal().removeItem(AVATAR_KEY + userId);
}

/** 是否有按钮级别的权限（兼容模板） */
export const hasPerms = (_value: string | Array<string>): boolean => {
  return true;
};
