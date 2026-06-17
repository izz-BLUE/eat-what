// pages/profile/profile.ts

const app = getApp<IApp>()

Page({
  data: {
    nickname: '',
    avatarUrl: '',
    isLoggedIn: false,
    _loginRedirecting: false
  },

  onShow() {
    this.setData({
      nickname: app.globalData.nickname || '未设置昵称',
      avatarUrl: app.globalData.avatarUrl || '',
      isLoggedIn: app.isLoggedIn(),
      _loginRedirecting: false
    })
  },

  goToLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  goToHistory() {
    if (!this.checkLogin()) return
    wx.navigateTo({ url: '/pages/history/history' })
  },

  goToBlacklist() {
    if (!this.checkLogin()) return
    wx.navigateTo({ url: '/pages/blacklist/blacklist' })
  },

  goToDislike() {
    if (!this.checkLogin()) return
    wx.navigateTo({ url: '/pages/dislike/dislike' })
  },

  goToFeedback() {
    // 意见反馈允许匿名访问，不需要登录检查
    wx.navigateTo({ url: '/pages/feedback/feedback' })
  },

  goToPrivacy() {
    wx.navigateTo({ url: '/pages/legal/privacy/privacy' })
  },

  goToAgreement() {
    wx.navigateTo({ url: '/pages/legal/agreement/agreement' })
  },

  checkLogin(): boolean {
    if (!app.isLoggedIn()) {
      if (this.data._loginRedirecting) return false
      this.setData({ _loginRedirecting: true })
      wx.navigateTo({ url: '/pages/login/login' })
      return false
    }
    return true
  },

  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.clearLoginInfo()
          wx.redirectTo({ url: '/pages/index/index' })
        }
      }
    })
  }
})
