// utils/request.ts - 统一请求封装

import { config } from '../config/index'
import { ApiResponse } from '../types/index'

const app = getApp<IApp>()

interface RequestOptions {
  method: 'GET' | 'POST' | 'DELETE'
  url: string
  data?: any
  showLoading?: boolean
}

/**
 * 业务错误，包含 code 和 message
 */
export class RequestError extends Error {
  code: number
  constructor(code: number, message: string) {
    super(message)
    this.code = code
    this.name = 'RequestError'
  }
}

/**
 * 统一请求方法
 */
export async function request<T = any>(options: RequestOptions): Promise<T> {
  const { method, url, data, showLoading = false } = options

  if (showLoading) {
    wx.showLoading({ title: '加载中...', mask: true })
  }

  return new Promise((resolve, reject) => {
    // 构建请求头
    const header: Record<string, string> = {
      'Content-Type': 'application/json'
    }

    // 自动添加 token
    const token = app.globalData.token
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    wx.request({
      url: `${config.baseUrl}${url}`,
      method,
      data,
      header,
      timeout: config.timeout,
      success: (res) => {
        if (showLoading) {
          wx.hideLoading()
        }

        const response = res.data as ApiResponse<T>

        // 检查响应格式
        if (!response || typeof response.code !== 'number') {
          reject(new RequestError(-1, '响应格式错误'))
          return
        }

        // 业务成功
        if (response.code === 0) {
          resolve(response.data)
          return
        }

        // 未登录：清理本地 token，抛出 NEED_LOGIN 错误
        if (response.code === 1003) {
          app.clearLoginInfo()
          reject(new RequestError(1003, 'NEED_LOGIN'))
          return
        }

        // 其他业务错误
        reject(new RequestError(response.code, response.message || '请求失败'))
      },
      fail: (err) => {
        if (showLoading) {
          wx.hideLoading()
        }

        if (err.errMsg?.includes('timeout')) {
          reject(new Error('请求超时，请检查网络'))
        } else {
          reject(new Error('网络错误，请检查网络连接'))
        }
      }
    })
  })
}

/**
 * GET 请求
 */
export function get<T = any>(url: string, data?: any): Promise<T> {
  return request<T>({ method: 'GET', url, data })
}

/**
 * POST 请求
 */
export function post<T = any>(url: string, data?: any): Promise<T> {
  return request<T>({ method: 'POST', url, data })
}

/**
 * DELETE 请求
 */
export function del<T = any>(url: string, data?: any): Promise<T> {
  return request<T>({ method: 'DELETE', url, data })
}
