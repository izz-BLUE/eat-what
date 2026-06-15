// app.ts
App({
  globalData: {
    token: '',
    userId: 0,
    nickname: '',
    avatarUrl: ''
  },

  onLaunch() {
    // 尝试从本地存储恢复登录状态
    const token = wx.getStorageSync('token')
    const userId = wx.getStorageSync('userId')
    const nickname = wx.getStorageSync('nickname')
    const avatarUrl = wx.getStorageSync('avatarUrl')

    if (token) {
      this.globalData.token = token
      this.globalData.userId = userId || 0
      this.globalData.nickname = nickname || ''
      this.globalData.avatarUrl = avatarUrl || ''
    }
  },

  // 保存登录信息
  saveLoginInfo(data: { token: string; userId: number; nickname: string; avatarUrl: string }) {
    this.globalData.token = data.token
    this.globalData.userId = data.userId
    this.globalData.nickname = data.nickname
    this.globalData.avatarUrl = data.avatarUrl

    wx.setStorageSync('token', data.token)
    wx.setStorageSync('userId', data.userId)
    wx.setStorageSync('nickname', data.nickname)
    wx.setStorageSync('avatarUrl', data.avatarUrl)
  },

  // 清除登录信息
  clearLoginInfo() {
    this.globalData.token = ''
    this.globalData.userId = 0
    this.globalData.nickname = ''
    this.globalData.avatarUrl = ''

    wx.removeStorageSync('token')
    wx.removeStorageSync('userId')
    wx.removeStorageSync('nickname')
    wx.removeStorageSync('avatarUrl')
  },

  // 检查是否已登录
  isLoggedIn(): boolean {
    return !!this.globalData.token
  }
})
