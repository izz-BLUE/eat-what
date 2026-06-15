// pages/profile/profile.ts

const app = getApp<IApp>()

Page({
  data: {
    nickname: '',
    isLoggedIn: false,
    _loginRedirecting: false
  },

  onShow() {
    this.setData({
      nickname: app.globalData.nickname || '未设置昵称',
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
