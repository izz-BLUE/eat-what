// pages/blacklist/blacklist.ts

import { getBlacklist, removeBlacklist } from '../../services/api'
import { RequestError } from '../../utils/request'

const app = getApp<IApp>()

interface BlacklistItem {
  id: number
  foodName: string
  category: string
  reason: string
  createdAt: string
}

Page({
  data: {
    items: [] as BlacklistItem[],
    loading: true,
    errorMsg: '',
    removing: 0,
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
      const items = await getBlacklist()
      const formatted = items.map(r => ({
        id: r.id,
        foodName: r.foodName || '-',
        category: r.category || '-',
        reason: r.reason || '',
        createdAt: r.createdAt ? r.createdAt.replace('T', ' ').substring(0, 16) : '-'
      }))
      this.setData({ items: formatted })
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

  handleRemove(e: any) {
    const id = e.currentTarget.dataset.id
    const name = e.currentTarget.dataset.name
    if (this.data.removing) return

    wx.showModal({
      title: '移出黑名单',
      content: `确定将「${name}」移出黑名单吗？`,
      success: async (res) => {
        if (!res.confirm) return
        this.setData({ removing: id })
        try {
          await removeBlacklist(id)
          this.loadData()
          wx.showToast({ title: '已移出', icon: 'success' })
        } catch (err: any) {
          if (err instanceof RequestError && err.code === 1003) {
            if (!this._loginRedirecting) {
              this._loginRedirecting = true
              wx.navigateTo({ url: '/pages/login/login' })
            }
            return
          }
          wx.showToast({ title: err.message || '操作失败', icon: 'none' })
        } finally {
          this.setData({ removing: 0 })
        }
      }
    })
  },

  goToLogin() {
    if (this._loginRedirecting) return
    this._loginRedirecting = true
    wx.navigateTo({ url: '/pages/login/login' })
  },

  _loginRedirecting: false as boolean
})
