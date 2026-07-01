// config/index.ts - 配置文件

// ============================================================
// 部署模式：所有环境统一使用生产 API，确保审核/真机调试无网络问题
// - develop / trial / release → https://api.jintianchi.cn
//
// 开发时如需本地调试，临时把下面 DEVELOPMENT_BASE_URL 注入到 baseUrl 即可
// ============================================================
const PRODUCTION_BASE_URL = 'https://api.jintianchi.cn'  // 生产域名（HTTPS）
const DEVELOPMENT_BASE_URL = 'http://localhost:8080'      // 本地开发地址（发审版本不使用）

export const config = {
  // API 基础地址（当前统一使用生产地址，避免审核/真机调试走到 localhost）
  baseUrl: PRODUCTION_BASE_URL,

  // 请求超时时间（毫秒）
  timeout: 10000,

  // 最大连续换一个次数
  maxSwapCount: 10
}
