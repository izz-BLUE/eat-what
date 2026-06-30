// pages/legal/agreement/agreement.ts

Page({
  data: {},

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
