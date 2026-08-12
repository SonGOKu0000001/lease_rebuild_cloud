import router from "@/router";
import { removeToken } from "@/utils/token";

export const useToLoginPage = () => {
  /**
   * @description: 跳转到登录页
   * @param path 跳转后的路径
   */
  const showToLoginPageDialog = (path: string = "/login") => {
    removeToken();
    router.replace(path);
  };
  return {
    showToLoginPageDialog
  };
};
