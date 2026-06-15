// pages/login/login.ts

import { login } from '../../services/api'

const app = getApp<IApp>()

Page({
  data: {
    loading: false,
    errorMsg: ''
  },

  async handleLogin() {
    this.setData({ loading: true, errorMsg: '' })

    try {
      // 1. 获取微信登录 code
      const loginResult = await this.wxLogin()
      const code = loginResult.code

      // 2. 调用后端登录接口
      const result = await login(code)

      // 3. 保存登录信息
      app.saveLoginInfo({
        token: result.token,
        userId: result.userId,
        nickname: result.nickname,
        avatarUrl: result.avatarUrl
      })

      wx.showToast({ title: '登录成功', icon: 'success' })

      // 4. 返回上一页（如果有的话）
      setTimeout(() => {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          wx.navigateBack()
        } else {
          // 没有上一页，跳转到首页
          wx.switchTab({ url: '/pages/index/index' })
        }
      }, 1500)
    } catch (err: any) {
      this.setData({ errorMsg: err.message || '登录失败，请重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  wxLogin(): Promise<{ code: string }> {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (res.code) {
            resolve({ code: res.code })
          } else {
            reject(new Error('获取登录凭证失败'))
          }
        },
        fail: (err) => {
          reject(new Error(err.errMsg || '微信登录失败'))
        }
      })
    })
  },

  // 返回首页
  goBack() {
    const pages = getCurrentPages()
    if (pages.length > 1) {
      wx.navigateBack()
    } else {
      wx.switchTab({ url: '/pages/index/index' })
    }
  }
})
