import { defineStore } from "pinia";
import {
  store,
  router,
  resetRouter,
  routerArrays,
  storageLocal
} from "../utils";
import { useMultiTagsStoreHook } from "./multiTags";
import { loginApi, getUserInfoApi } from "@/api/user";
import { type DataInfo, setToken, removeToken, userKey } from "@/utils/auth";

export const useUserStore = defineStore({
  id: "pure-user",
  state: () => ({
    avatar: "",
    userId: storageLocal().getItem<DataInfo<number>>(userKey)?.userId ?? 0,
    username: "",
    nickname: storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "",
    userType: storageLocal().getItem<DataInfo<number>>(userKey)?.userType ?? "",
    roles:
      storageLocal().getItem<DataInfo<number>>(userKey)?.roles ??
      ([] as Array<string>),
    isRemembered: false,
    loginDay: 7
  }),
  actions: {
    SET_USERNAME(username: string) {
      this.username = username;
    },
    SET_NICKNAME(nickname: string) {
      this.nickname = nickname;
    },
    SET_USERID(userId: number) {
      this.userId = userId;
    },
    SET_USERTYPE(userType: string) {
      this.userType = userType;
      this.roles = [userType];
    },
    /** 登录 */
    async loginByUsername(data: { username: string; password: string }) {
      const result = await loginApi(data);
      setToken({
        token: result.token,
        userId: result.userId,
        userType: result.userType
      });
      this.SET_USERID(result.userId);
      this.SET_USERTYPE(result.userType);
      return result;
    },
    /** 获取用户信息 */
    async getUserInfo() {
      const info = await getUserInfoApi();
      this.SET_USERNAME(info.username);
      this.SET_NICKNAME(info.nickname || "");
      // 更新localStorage中的昵称
      const stored = storageLocal().getItem<DataInfo<number>>(userKey);
      if (stored) {
        stored.nickname = info.nickname || "";
        storageLocal().setItem(userKey, stored);
      }
      return info;
    },
    /** 前端登出 */
    logOut() {
      this.userId = 0;
      this.username = "";
      this.nickname = "";
      this.userType = "";
      this.roles = [];
      removeToken();
      useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
      resetRouter();
      router.push("/login");
    }
  }
});

export function useUserStoreHook() {
  return useUserStore(store);
}
