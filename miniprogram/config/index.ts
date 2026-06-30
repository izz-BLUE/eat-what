// config/index.ts - 配置文件

// ============================================================
// 部署模式：根据小程序运行环境自动切换 baseUrl
// - 开发版 (develop) → 使用本地开发地址 http://localhost:8080
// - 体验版/正式版 (trial/release) → 使用 PRODUCTION_BASE_URL
//
// 上线前只需：替换 PRODUCTION_BASE_URL 为备案 HTTPS 域名
// ============================================================
const PRODUCTION_BASE_URL = 'https://api.jintianchi.cn'  // 生产域名（HTTPS）

function resolveBaseUrl(): string {
  try {
    const envVersion = wx.getAccountInfoSync().miniProgram.envVersion
    if (envVersion === 'develop') {
      return 'http://localhost:8080'
    }
    // trial 和 release 都使用生产地址
    return PRODUCTION_BASE_URL
  } catch {
    // wx 不可用时 fallback 到开发地址（如自动化测试环境）
  }
  return 'http://localhost:8080'
}

export const config = {
  // API 基础地址（自动切换，无需手动改开关）
  baseUrl: resolveBaseUrl(),

  // 请求超时时间（毫秒）
  timeout: 10000,

  // 最大连续换一个次数
  maxSwapCount: 10
}
