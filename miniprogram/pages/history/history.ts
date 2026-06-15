// pages/history/history.ts

import { getEatRecords } from '../../services/api'
import { RequestError } from '../../utils/request'

const app = getApp<IApp>()

interface HistoryItem {
  id: number
  foodName: string
  mealType: string
  rating: number
  note: string
  eatenAt: string
  hasRating: boolean
  hasNote: boolean
}

Page({
  data: {
    records: [] as HistoryItem[],
    loading: true,
    errorMsg: '',
    _loginRedirecting: false
  },

  onShow() {
    if (!app.isLoggedIn()) {
      this.setData({ loading: false })
      return
    }
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
        rating: r.rating || 0,
        note: r.note || '',
        eatenAt: r.eatenAt ? r.eatenAt.replace('T', ' ').substring(0, 16) : '-',
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

  _loginRedirecting: false as boolean
})
