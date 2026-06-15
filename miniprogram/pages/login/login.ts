// pages/login/login.ts

import { login, addBlacklist } from '../../services/api'

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

      // 4. 检查是否有待办操作
      setTimeout(async () => {
        // 优先处理待记录菜品
        const pendingRecord = app.globalData.pendingRecord
        app.globalData.pendingRecord = null
        if (pendingRecord) {
          wx.redirectTo({
            url: `/pages/record/record?foodId=${pendingRecord.foodId}&foodName=${encodeURIComponent(pendingRecord.foodName)}&category=${encodeURIComponent(pendingRecord.category)}&mealType=${encodeURIComponent(pendingRecord.mealType || '')}`
          })
          return
        }

        // 处理待拉黑
        const pendingBlacklist = app.globalData.pendingBlacklist
        let blacklistFailed = false
        if (pendingBlacklist) {
          try {
            await addBlacklist({ foodId: pendingBlacklist.foodId, reason: pendingBlacklist.reason })
            // 成功：写入 pendingResult 供首页 onShow 消费
            app.globalData.pendingResult = {
              type: 'blacklist',
              foodId: pendingBlacklist.foodId,
              foodName: pendingBlacklist.foodName || ''
            }
            app.globalData.pendingBlacklist = null
          } catch (e: any) {
            // 失败：保留 pendingBlacklist，显示错误，停留此页不跳转
            blacklistFailed = true
            this.setData({ errorMsg: '加入黑名单失败，请重试' })
          }
        }

        // 黑名单失败时停留登录页，允许重试
        if (blacklistFailed) return

        // 成功：返回上一页
        const pages = getCurrentPages()
        if (pages.length > 1) {
          wx.navigateBack()
        } else {
          wx.redirectTo({ url: '/pages/index/index' })
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

  // 返回首页（暂不登录，清除 pending 状态）
  goBack() {
    app.globalData.pendingRecord = null
    app.globalData.pendingBlacklist = null
    const pages = getCurrentPages()
    if (pages.length > 1) {
      wx.navigateBack()
    } else {
      wx.redirectTo({ url: '/pages/index/index' })
    }
  },

  // 页面卸载时清除 pending 状态
  onUnload() {
    app.globalData.pendingRecord = null
    app.globalData.pendingBlacklist = null
  }
})
