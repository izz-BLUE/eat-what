// pages/feedback/feedback.ts
import { submitFeedback } from '../../services/api'
import { FeedbackType } from '../../types/index'

const TYPE_OPTIONS: { value: FeedbackType; label: string }[] = [
  { value: 'FEATURE', label: '功能建议' },
  { value: 'BUG', label: '问题反馈' },
  { value: 'RECOMMENDATION', label: '推荐不准' },
  { value: 'UI', label: '界面体验' },
  { value: 'OTHER', label: '其他' }
]

Page({
  data: {
    typeOptions: TYPE_OPTIONS,
    type: 'FEATURE' as FeedbackType,
    rating: 0,
    content: '',
    contact: '',
    submitting: false,
    errorMsg: '',
    contentLength: 0,
    maxContentLength: 500,
    maxContactLength: 100
  },

  selectType(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as FeedbackType
    this.setData({ type: value })
  },

  setRating(e: WechatMiniprogram.TouchEvent) {
    const value = Number(e.currentTarget.dataset.value)
    this.setData({ rating: this.data.rating === value ? 0 : value })
  },

  onContentInput(e: WechatMiniprogram.Input) {
    const content = e.detail.value || ''
    this.setData({ content, contentLength: content.length })
  },

  onContactInput(e: WechatMiniprogram.Input) {
    this.setData({ contact: e.detail.value || '' })
  },

  async handleSubmit() {
    if (this.data.submitting) return

    const content = this.data.content.trim()
    if (!content) {
      wx.showToast({ title: '请填写反馈内容', icon: 'none' })
      return
    }
    if (content.length < 5) {
      wx.showToast({ title: '反馈内容至少5个字', icon: 'none' })
      return
    }
    if (content.length > this.data.maxContentLength) {
      wx.showToast({ title: `内容不能超过${this.data.maxContentLength}字`, icon: 'none' })
      return
    }

    this.setData({ submitting: true, errorMsg: '' })

    try {
      // 采集微信环境信息
      let systemInfo = ''
      try {
        const info = wx.getSystemInfoSync()
        systemInfo = JSON.stringify({
          model: info.model,
          system: info.system,
          platform: info.platform,
          version: info.version,
          SDKVersion: info.SDKVersion
        })
      } catch (e) { /* ignore */ }

      await submitFeedback({
        type: this.data.type,
        rating: this.data.rating > 0 ? this.data.rating : undefined,
        content,
        contact: this.data.contact.trim() || undefined,
        page: '/pages/feedback/feedback',
        systemInfo: systemInfo || undefined
      })

      wx.showToast({ title: '已收到反馈', icon: 'success' })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (err: any) {
      this.setData({ submitting: false, errorMsg: err.message || '提交失败，请稍后再试' })
    }
  }
})
