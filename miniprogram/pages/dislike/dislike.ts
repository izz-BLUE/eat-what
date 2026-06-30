// pages/dislike/dislike.ts

import { getDislikes, addDislike, removeDislike } from '../../services/api'
import { RequestError } from '../../utils/request'
import { DislikeData, RecommendOptionsData, RecommendOptionItem } from '../../types/index'
import { fetchRecommendOptions, getFallbackOptions } from '../../services/recommend-options'

const app = getApp<IApp>()

const DAY_OPTIONS = [
  { label: '1天', value: 1 },
  { label: '3天', value: 3 },
  { label: '7天', value: 7 },
  { label: '30天', value: 30 }
]

Page({
  data: {
    items: [] as DislikeData[],
    typeTagOptions: [] as RecommendOptionItem[],
    cuisineTagOptions: [] as RecommendOptionItem[],
    dayOptions: DAY_OPTIONS,
    selectedCategory: '',
    selectedDays: 3,
    loading: true,
    notLoggedIn: false,
    adding: false,
    removing: 0,
    errorMsg: '',
    _loginRedirecting: false
  },

  onLoad() {
    // 立即用 fallback 渲染分类选项
    const fallback = getFallbackOptions()
    this.setData({
      typeTagOptions: fallback.typeTags,
      cuisineTagOptions: fallback.cuisineTags
    })
    // 异步拉取后端元数据
    this.loadOptions()
  },

  onShow() {
    if (!app.isLoggedIn()) {
      this.setData({ loading: false, notLoggedIn: true, errorMsg: '', items: [] })
      return
    }
    this.setData({ notLoggedIn: false })
    this._loginRedirecting = false
    this.loadData()
  },

  async loadOptions() {
    try {
      const fresh = await fetchRecommendOptions()
      this.setData({
        typeTagOptions: fresh.typeTags,
        cuisineTagOptions: fresh.cuisineTags
      })
    } catch (e) {
      // fallback 已渲染
    }
  },

  async loadData() {
    this.setData({ loading: true, errorMsg: '' })
    try {
      const items = await getDislikes()
      this.setData({ items })
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

  selectCategory(e: any) {
    const val = e.currentTarget.dataset.value
    this.setData({ selectedCategory: this.data.selectedCategory === val ? '' : val })
  },

  selectDays(e: any) {
    this.setData({ selectedDays: Number(e.currentTarget.dataset.value) })
  },

  async handleAdd() {
    if (!this.data.selectedCategory) {
      wx.showToast({ title: '请选择分类', icon: 'none' })
      return
    }
    if (this.data.adding) return
    this.setData({ adding: true })

    try {
      await addDislike({ category: this.data.selectedCategory, days: this.data.selectedDays })
      wx.showToast({ title: '已添加', icon: 'success' })
      this.loadData()
    } catch (err: any) {
      if (err instanceof RequestError && err.code === 1003) {
        if (!this._loginRedirecting) {
          this._loginRedirecting = true
          wx.navigateTo({ url: '/pages/login/login' })
        }
        return
      }
      wx.showToast({ title: err.message || '添加失败', icon: 'none' })
    } finally {
      this.setData({ adding: false })
    }
  },

  async handleRemove(e: any) {
    const id = e.currentTarget.dataset.id
    const cat = e.currentTarget.dataset.cat
    if (this.data.removing) return

    wx.showModal({
      title: '解除不想吃',
      content: `确定解除「${cat}」的限制吗？`,
      success: async (res) => {
        if (!res.confirm) return
        this.setData({ removing: id })
        try {
          await removeDislike(id)
          wx.showToast({ title: '已解除', icon: 'success' })
          this.loadData()
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

  _loginRedirecting: false as boolean,

  onShareAppMessage() {
    return {
      title: '饭团今天吃什么，帮你快速决定今天吃啥',
      path: '/pages/index/index'
    }
  },

  onShareTimeline() {
    return {
      title: '饭团今天吃什么，帮你快速决定今天吃啥'
    }
  }
})
