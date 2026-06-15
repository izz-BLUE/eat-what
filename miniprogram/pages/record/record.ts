// pages/record/record.ts

import { eatFood } from '../../services/api'

Page({
  data: {
    foodId: 0,
    foodName: '',
    category: '',
    mealTypes: ['早餐', '午餐', '晚餐', '夜宵'],
    selectedMealType: '',
    rating: 0,
    note: '',
    submitting: false,
    submitted: false,
    errorMsg: ''
  },

  onLoad(options) {
    const foodId = Number(options.foodId) || 0
    if (!foodId) {
      wx.showToast({ title: '菜品信息无效', icon: 'none' })
      setTimeout(() => wx.redirectTo({ url: '/pages/index/index' }), 1500)
      return
    }
    this.setData({
      foodId,
      foodName: decodeURIComponent(options.foodName || ''),
      category: decodeURIComponent(options.category || ''),
      selectedMealType: decodeURIComponent(options.mealType || '')
    })
  },

  selectMealType(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    this.setData({
      selectedMealType: this.data.selectedMealType === value ? '' : value
    })
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
    // 校验
    if (!this.data.foodId || this.data.foodId <= 0) {
      wx.showToast({ title: '菜品信息无效', icon: 'none' })
      setTimeout(() => wx.redirectTo({ url: '/pages/index/index' }), 1500)
      return
    }
    if (!this.data.selectedMealType) {
      wx.showToast({ title: '请选择餐段', icon: 'none' })
      return
    }

    // 防止重复提交
    if (this.data.submitting || this.data.submitted) {
      return
    }

    this.setData({ submitting: true, errorMsg: '' })

    try {
      const data: any = {
        foodId: this.data.foodId,
        mealType: this.data.selectedMealType
      }

      if (this.data.rating > 0) {
        data.rating = this.data.rating
      }

      if (this.data.note) {
        data.note = this.data.note
      }

      await eatFood(data)

      this.setData({ submitted: true })
      wx.showToast({ title: '记录成功', icon: 'success' })
    } catch (err: any) {
      if (err.message === 'NEED_LOGIN') {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      this.setData({ errorMsg: err.message || '记录失败，请重试' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  goBack() {
    wx.navigateBack()
  }
})
