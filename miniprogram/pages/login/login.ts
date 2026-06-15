// pages/login/login.ts

import { login, addBlacklist, decideFood } from '../../services/api'
import { CurrentMealDecision } from '../../types/index'

const app = getApp<IApp>()

const DECISION_KEY = 'currentMealDecision'
const DECISION_VERSION = 2

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

      // 4. 优先处理待决定菜品（新流程）
      const pendingDecision = app.globalData.pendingDecision
      app.globalData.pendingDecision = null
      if (pendingDecision) {
        try {
          const record = await decideFood({
            foodId: pendingDecision.foodId,
            mealType: pendingDecision.mealType
          })

          // 保存本地决定
          const decision: CurrentMealDecision = {
            version: DECISION_VERSION,
            recordId: record.id,
            foodId: pendingDecision.foodId,
            foodName: pendingDecision.foodName,
            category: pendingDecision.category,
            mealType: pendingDecision.mealType,
            decidedAt: new Date().toISOString()
          }
          try {
            wx.setStorageSync(DECISION_KEY, decision)
          } catch (e) { /* ignore */ }

          // 写入 pendingResult 供首页 onShow 消费
          app.globalData.pendingResult = {
            type: 'decision',
            foodId: pendingDecision.foodId,
            foodName: pendingDecision.foodName || ''
          }
        } catch (e: any) {
          // 决定失败：保留 pendingDecision，停留登录页允许重试
          this.setData({ loading: false, errorMsg: e.message || '决定失败，请重试' })
          app.globalData.pendingDecision = pendingDecision // 恢复待办
          return
        }
        wx.showToast({ title: '登录成功', icon: 'success' })
        const pages = getCurrentPages()
        if (pages.length > 1) {
          wx.navigateBack()
        } else {
          wx.redirectTo({ url: '/pages/index/index' })
        }
        return
      }

      // 5. 处理待拉黑
      const pendingBlacklist = app.globalData.pendingBlacklist
      if (pendingBlacklist) {
        try {
          await addBlacklist({ foodId: pendingBlacklist.foodId, reason: pendingBlacklist.reason })
          app.globalData.pendingResult = {
            type: 'blacklist',
            foodId: pendingBlacklist.foodId,
            foodName: pendingBlacklist.foodName || ''
          }
          app.globalData.pendingBlacklist = null
        } catch (e: any) {
          this.setData({ loading: false, errorMsg: e.message || '加入黑名单失败，请重试' })
          return
        }
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
    app.globalData.pendingDecision = null
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
    app.globalData.pendingDecision = null
    app.globalData.pendingBlacklist = null
  }
})
