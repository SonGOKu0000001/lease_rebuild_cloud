// ? 全局不动配置项 只做导出不做修改
// * 高德地图 key（从 .env 读取，真实值存放在被忽略的 .env.local 中，见 .env.example）
export const AMAP_MAP_KEY = import.meta.env.VITE_AMAP_KEY || "";
// 高德地图 安全密匙
export const AMAP_MAP_SECRET_KEY = import.meta.env.VITE_AMAP_SECRET_KEY || "";

export const AMAP_MAP_SERVICE_HOST = import.meta.env.VITE_AMAP_SERVICE_HOST || "";
