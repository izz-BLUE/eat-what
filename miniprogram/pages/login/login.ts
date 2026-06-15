// pages/login/login.ts

import { login, addBlacklist } from '../../services/api'

const app = getApp<IApp>()

Page({
  data: {
    loading: false,
    errorMsg: ''
  },

  async handleLogin() {
    // 防止重复点击
    if (this.data.loading) return
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

      // 4. 优先处理待记录菜品
      const pendingRecord = app.globalData.pendingRecord
      app.globalData.pendingRecord = null
      if (pendingRecord) {
        wx.showToast({ title: '登录成功', icon: 'success' })
        wx.redirectTo({
          url: `/pages/record/record?foodId=${pendingRecord.foodId}&foodName=${encodeURIComponent(pendingRecord.foodName)}&category=${encodeURIComponent(pendingRecord.category)}&mealType=${encodeURIComponent(pendingRecord.mealType || '')}`
        })
        return // 已导航，不继续 setData
      }

      // 5. 处理待拉黑
      const pendingBlacklist = app.globalData.pendingBlacklist
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
          // 失败：保留 pendingBlacklist，停留登录页允许重试
          this.setData({ loading: false, errorMsg: e.message || '加入黑名单失败，请重试' })
          return // 不导航，不继续
        }
        // 成功：导航回首页
        wx.showToast({ title: '登录成功', icon: 'success' })
        const pages = getCurrentPages()
        if (pages.length > 1) {
          wx.navigateBack()
        } else {
          wx.redirectTo({ url: '/pages/index/index' })
        }
        return
      }

      // 6. 普通登录：返回上一页
      wx.showToast({ title: '登录成功', icon: 'success' })
      const pages = getCurrentPages()
      if (pages.length > 1) {
        wx.navigateBack()
      } else {
        wx.redirectTo({ url: '/pages/index/index' })
      }
    } catch (err: any) {
      this.setData({ loading: false, errorMsg: err.message || '登录失败，请重试' })
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
