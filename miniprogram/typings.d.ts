/// <reference path="./node_modules/miniprogram-api-typings/types/wx/index.d.ts" />

interface PendingResult {
  type: 'blacklist' | 'decision'
  foodId: number
  foodName: string
  source?: 'DEFAULT' | 'CUSTOM'
  customFoodId?: number
}

interface IApp {
  globalData: {
    token: string
    userId: number
    nickname: string
    avatarUrl: string
    pendingDecision: any
    pendingBlacklist: any
    pendingResult: PendingResult | null
    pendingRedirect?: string
  }
  saveLoginInfo(data: { token: string; userId: number; nickname: string; avatarUrl: string }): void
  clearLoginInfo(): void
  isLoggedIn(): boolean
}
