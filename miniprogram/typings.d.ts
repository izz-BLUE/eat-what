/// <reference path="./node_modules/miniprogram-api-typings/types/wx/index.d.ts" />

interface IApp {
  globalData: {
    token: string
    userId: number
    nickname: string
    avatarUrl: string
    pendingRecord: any
  }
  saveLoginInfo(data: { token: string; userId: number; nickname: string; avatarUrl: string }): void
  clearLoginInfo(): void
  isLoggedIn(): boolean
}
