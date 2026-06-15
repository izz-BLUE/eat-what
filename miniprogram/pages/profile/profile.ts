// pages/profile/profile.ts

const app = getApp<IApp>()

Page({
  data: {
    nickname: '',
    isLoggedIn: false
  },

  onShow() {
    this.setData({
      nickname: app.globalData.nickname || '未设置昵称',
      isLoggedIn: app.isLoggedIn()
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
