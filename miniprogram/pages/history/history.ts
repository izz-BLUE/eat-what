// pages/history/history.ts

import { getEatRecords, cancelDecisionRecord } from '../../services/api'
import { RequestError } from '../../utils/request'

const app = getApp<IApp>()

interface HistoryItem {
  id: number
  foodName: string
  mealType: string
  status: string
  rating: number
  note: string
  eatenAt: string | null
  decidedAt: string | null
  hasRating: boolean
  hasNote: boolean
}

Page({
  data: {
    records: [] as HistoryItem[],
    loading: true,
    notLoggedIn: false,
    errorMsg: '',
    _loginRedirecting: false
  },

  onShow() {
    if (!app.isLoggedIn()) {
      this.setData({ loading: false, notLoggedIn: true, errorMsg: '', records: [] })
      return
    }
    this.setData({ notLoggedIn: false })
    this._loginRedirecting = false
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true, errorMsg: '' })
    try {
      const records = await getEatRecords(50)
      const formatted = records.map(r => ({
        id: r.id,
        foodName: r.foodName || '-',
        mealType: r.mealType || '-',
        status: r.status || 'EATEN',
        rating: r.rating || 0,
        note: r.note || '',
        eatenAt: r.eatenAt ? r.eatenAt.replace('T', ' ').substring(0, 16) : null,
        decidedAt: r.decidedAt ? r.decidedAt.replace('T', ' ').substring(0, 16) : null,
        hasRating: !!r.rating,
        hasNote: !!r.note
      }))
      this.setData({ records: formatted })
    } catch (err: any) {
      if (err instanceof RequestError && err.code === 1003) {
        if (!this._loginRedirecting) {
          this._loginRedirecting = true
          wx.navigateTo({ url: '/pages/login/login' })
        }
        return
      }
      this.setData({ errorMsg: err.message || '加载失败' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onPullDownRefresh() {
    this.loadData().then(() => wx.stopPullDownRefresh()).catch(() => wx.stopPullDownRefresh())
  },

  goToLogin() {
    if (this._loginRedirecting) return
    this._loginRedirecting = true
    wx.navigateTo({ url: '/pages/login/login' })
  },

  /** 去完成用餐（跳转 record 页） */
  handleComplete(e: WechatMiniprogram.TouchEvent) {
    const id = Number(e.currentTarget.dataset.id)
    const item = this.data.records.find(r => r.id === id)
    if (!item) return
    wx.navigateTo({
      url: `/pages/record/record?recordId=${item.id}&foodName=${encodeURIComponent(item.foodName)}&mealType=${encodeURIComponent(item.mealType)}`
    })
  },

  /** 修改评价（跳转 record 页，加载原值） */
  handleEditReview(e: WechatMiniprogram.TouchEvent) {
    const id = Number(e.currentTarget.dataset.id)
    const item = this.data.records.find(r => r.id === id)
    if (!item) return
    // 跳转 record 页复用完成流程，但预填原评分和备注
    wx.navigateTo({
      url: `/pages/record/record?recordId=${item.id}&foodName=${encodeURIComponent(item.foodName)}&mealType=${encodeURIComponent(item.mealType)}&mode=review`
    })
  },

  /** 取消决定 */
  async handleCancelDecision(e: WechatMiniprogram.TouchEvent) {
    const id = Number(e.currentTarget.dataset.id)
    const item = this.data.records.find(r => r.id === id)
    if (!item) return

    wx.showModal({
      title: '取消决定',
      content: `确定要取消「${item.foodName}」的决定吗？`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await cancelDecisionRecord(item.id)

          // 同步清除匹配 recordId 的本地决定
          try {
            const raw = wx.getStorageSync('currentMealDecision')
            if (raw) {
              let obj: any = raw
              if (typeof raw === 'string') {
                try { obj = JSON.parse(raw) } catch (e) { obj = null }
              }
              if (obj && obj.recordId === item.id) {
                wx.removeStorageSync('currentMealDecision')
              }
            }
          } catch (e) { /* ignore */ }

          wx.showToast({ title: '已取消', icon: 'success' })
          this.loadData()
        } catch (err: any) {
          wx.showToast({ title: err.message || '操作失败', icon: 'none' })
        }
      }
    })
  },

  _loginRedirecting: false as boolean
})
