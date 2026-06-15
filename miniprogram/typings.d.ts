/// <reference path="./node_modules/miniprogram-api-typings/types/wx/index.d.ts" />

interface PendingResult {
  type: 'blacklist' | 'record'
  foodId: number
  foodName: string
}

interface IApp {
  globalData: {
    token: string
    userId: number
    nickname: string
    avatarUrl: string
    pendingRecord: any
    pendingBlacklist: any
    pendingResult: PendingResult | null
  }
  saveLoginInfo(data: { token: string; userId: number; nickname: string; avatarUrl: string }): void
  clearLoginInfo(): void
  isLoggedIn(): boolean
}
