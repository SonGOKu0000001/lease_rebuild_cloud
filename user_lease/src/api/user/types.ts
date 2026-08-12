// 登录
export interface loginQueryInterface {
  // 邮箱账号
  email: string;
  // 	邮箱验证码
  code: string;
}
// 获取邮箱验证码
export interface SmsCodeQueryInterface {
  // 邮箱
  email: string;
}

// 用户信息
export interface UserInfoInterface {
  // 头像
  avatarUrl: string;
  // 用户名
  nickname: string;
}
// 用户state
export interface UserStateInterface {
  // 用户信息
  userInfo: UserInfoInterface | null;
  // token
  token: string | null;
  // 是否已认证
  isAuthenticated: boolean;
  // 是否已完成验证
  authChecked: boolean;
  // 是否正在校验中
  isValidating: boolean;
}
