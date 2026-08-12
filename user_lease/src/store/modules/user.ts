import { defineStore } from "pinia";
import type {
  loginQueryInterface,
  UserInfoInterface,
  UserStateInterface
} from "@/api/user/types";
import { getUserInfo, login, refreshToken as refreshTokenApi } from "@/api/user";
import { getToken, removeToken, setToken } from "@/utils/token";

let refreshTimerId: ReturnType<typeof setTimeout> | null = null;
let validationPromise: Promise<boolean> | null = null;

export const useUserStore = defineStore({
  id: "app-user",
  state: (): UserStateInterface => ({
    token: null,
    userInfo: null,
    isAuthenticated: false,
    authChecked: false,
    isValidating: false
  }),
  actions: {
    // setToken
    setToken(token: string) {
      this.token = token;
    },
    // login
    async LoginAction(params: loginQueryInterface) {
      const { data } = await login(params);
      setToken(data);
      await this.GetInfoAction();
      this.authChecked = true;
      this.isAuthenticated = true;
      this.scheduleRefresh();
    },
    // setUserInfo
    setUserInfo(userInfo: UserInfoInterface) {
      this.userInfo = userInfo;
    },
    async GetInfoAction() {
      const { data } = await getUserInfo();
      this.setUserInfo(data);
    },
    async validateToken() {
      if (!getToken()) {
        this.authChecked = true;
        this.isAuthenticated = false;
        return false;
      }
      if (this.authChecked) {
        return this.isAuthenticated;
      }
      if (validationPromise) {
        return validationPromise;
      }
      this.isValidating = true;
      validationPromise = this.GetInfoAction()
        .then(() => {
          this.authChecked = true;
          this.isAuthenticated = true;
          return true;
        })
        .catch(() => {
          this.authChecked = true;
          this.isAuthenticated = false;
          return false;
        })
        .finally(() => {
          this.isValidating = false;
          validationPromise = null;
        });
      return validationPromise;
    },
    scheduleRefresh() {
      if (!this.isAuthenticated) return;
      if (refreshTimerId) {
        clearTimeout(refreshTimerId);
        refreshTimerId = null;
      }
      const token = getToken();
      if (!token) return;
      let exp: number;
      try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        exp = payload.exp;
      } catch {
        return;
      }
      const now = Math.floor(Date.now() / 1000);
      const remaining = exp - now;
      const refreshIn = Math.max((remaining - 1200) * 1000, 0);
      refreshTimerId = setTimeout(async () => {
        try {
          const { data: newToken } = await refreshTokenApi();
          setToken(newToken);
          this.scheduleRefresh();
        } catch {
          // 刷新失败，静默放弃，等自然过期后拦截器兜底
        }
      }, refreshIn);
    },
    cancelRefresh() {
      if (refreshTimerId) {
        clearTimeout(refreshTimerId);
        refreshTimerId = null;
      }
    },
    async Logout() {
      this.cancelRefresh();
      this.resetUserStore();
      removeToken();
    },
    resetUserStore() {
      this.token = null;
      this.userInfo = null;
      this.isAuthenticated = false;
      this.authChecked = false;
      this.isValidating = false;
    }
  },
  persist: {
    paths: ["token", "userInfo"]
  }
});
