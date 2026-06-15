// pages/record/record.ts

import { getRecord, completeRecord, reviewRecord } from '../../services/api'

const app = getApp<IApp>()

Page({
  data: {
    recordId: 0,
    foodName: '',
    category: '',
    mealType: '',        // 只读展示，来自服务端
    status: '',          // 'DECIDED' | 'EATEN'，来自 GET /record/{id}
    rating: 0,
    note: '',
    submitting: false,
    submitted: false,
    errorMsg: '',
    loading: true        // 加载详情中
  },

  async onLoad(options) {
    const recordId = Number(options.recordId) || 0
    if (!recordId) {
      wx.showToast({ title: '记录信息无效', icon: 'none' })
      setTimeout(() => wx.redirectTo({ url: '/pages/index/index' }), 1500)
      return
    }
    this.setData({ recordId })

    // 调服务端获取真实详情（不信任 URL 参数）
    try {
      const record = await getRecord(recordId)
      this.setData({
        foodName: record.foodName,
        category: record.category,
        mealType: record.mealType,
        status: record.status,
        rating: record.rating || 0,
        note: record.note || '',
        loading: false
      })
    } catch (err: any) {
      if (err.message === 'NEED_LOGIN') {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      wx.showToast({ title: err.message || '加载失败', icon: 'none' })
      setTimeout(() => wx.redirectTo({ url: '/pages/index/index' }), 1500)
    }
  },

  setRating(e: WechatMiniprogram.TouchEvent) {
    const value = Number(e.currentTarget.dataset.value)
    this.setData({
      rating: this.data.rating === value ? 0 : value
    })
  },

  onNoteInput(e: WechatMiniprogram.Input) {
    this.setData({ note: e.detail.value })
  },

  async submitRecord() {
    if (!this.data.recordId || this.data.recordId <= 0) {
      wx.showToast({ title: '记录信息无效', icon: 'none' })
      setTimeout(() => wx.redirectTo({ url: '/pages/index/index' }), 1500)
      return
    }

    // 防止重复提交
    if (this.data.submitting || this.data.submitted) {
      return
    }

    this.setData({ submitting: true, errorMsg: '' })

    try {
      const data: any = {}
      if (this.data.rating > 0) {
        data.rating = this.data.rating
      }
      if (this.data.note) {
        data.note = this.data.note
      }

      // 根据服务端状态选择接口：DECIDED → complete, EATEN → review
      if (this.data.status === 'DECIDED') {
        await completeRecord(this.data.recordId, data)
      } else {
        await reviewRecord(this.data.recordId, data)
      }

      // 清除匹配 recordId 的本地决定
      try {
        const raw = wx.getStorageSync('currentMealDecision')
        if (raw) {
          let obj: any = raw
          if (typeof raw === 'string') {
            try { obj = JSON.parse(raw) } catch (e) { obj = null }
          }
          if (obj && obj.recordId === this.data.recordId) {
            wx.removeStorageSync('currentMealDecision')
          }
        }
      } catch (e) { /* ignore */ }

      // 清除匹配 recordId 的 pendingDecision
      if (app.globalData.pendingDecision) {
        app.globalData.pendingDecision = null
      }

      this.setData({ submitted: true })
      wx.showToast({ title: this.data.status === 'DECIDED' ? '记录成功' : '评价已更新', icon: 'success' })
    } catch (err: any) {
      if (err.message === 'NEED_LOGIN') {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      this.setData({ errorMsg: err.message || '提交失败，请重试' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  goBack() {
    wx.navigateBack()
  }
})
