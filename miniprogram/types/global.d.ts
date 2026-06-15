// types/global.d.ts - 全局类型声明

interface IApp {
  globalData: {
    token: string
    userId: number
    nickname: string
    avatarUrl: string
  }
  saveLoginInfo(data: { token: string; userId: number; nickname: string; avatarUrl: string }): void
  clearLoginInfo(): void
  isLoggedIn(): boolean
}
