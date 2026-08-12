import {
  createRouter,
  createWebHashHistory,
  type RouteLocationNormalized
} from "vue-router";
import routes from "./routes";
import { useCachedViewStoreHook } from "@/store/modules/cachedView";
import NProgress from "@/utils/progress";
import setPageTitle from "@/utils/set-page-title";
import { useUserStore } from "@/store/modules/user";
import { getToken } from "@/utils/token";

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export interface toRouteType extends RouteLocationNormalized {
  meta: {
    title?: string;
    noCache?: boolean;
  };
}

router.beforeEach(async (to: toRouteType, from, next) => {
  NProgress.start();
  // 解决路由缓存导致的 keep-alive 组件不刷新的问题
  if (to.name === "Login") {
    useCachedViewStoreHook().delAllCachedViews();
  }
  // 路由缓存
  useCachedViewStoreHook().addCachedView(to);
  // 页面 title
  setPageTitle(to.meta.title);
  // 登录页直接放行
  if (to.name === "Login") {
    next();
    return;
  }
  // 无 token 跳转登录
  if (!getToken()) {
    next({ path: "/login" });
    return;
  }
  // 未验证则等待后端验证结果
  const userStore = useUserStore();
  if (!userStore.authChecked) {
    await userStore.validateToken();
  }
  // 验证失败跳转登录
  if (!userStore.isAuthenticated) {
    next({ path: "/login" });
    return;
  }
  next();
});

router.afterEach(() => {
  NProgress.done();
});

export default router;
