// types/index.ts - 类型定义

/** 统一响应结构 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/** 登录响应 */
export interface LoginData {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  nickname: string
  avatarUrl: string
}

/** 推荐响应 */
export interface RecommendData {
  food: FoodInfo
  score: number
  reasons: string[]
}

/** 菜品信息 */
export interface FoodInfo {
  id: number
  name: string
  category: string
  tasteTags: string
  priceLevel: number
  imageUrl: string
}

/** 吃过记录响应 */
export interface EatRecordData {
  id: number
  foodId: number
  foodName: string
  mealType: string
  rating: number | null
  note: string
  eatenAt: string
}

/** 推荐参数 */
export interface RecommendParams {
  mealType?: string
  priceLevel?: string
  taste?: string
  excludeFoodIds?: string
  categories?: string
}

/** 吃过记录请求 */
export interface EatRecordRequest {
  foodId: number
  mealType: string
  rating?: number
  note?: string
}

/** 餐段选项 */
export type MealType = '早餐' | '午餐' | '晚餐' | '夜宵'

/** 价格选项 */
export type PriceLevel = '15以内' | '15-25' | '25-40' | '不限'

/** 口味选项 */
export type Taste = '不限' | '清淡' | '重口' | '辣' | '不辣'

/** 黑名单条目 */
export interface BlacklistData {
  id: number
  foodId: number
  foodName: string
  category: string
  reason: string
  createdAt: string
}

/** 加入黑名单请求 */
export interface BlacklistAddRequest {
  foodId: number
  reason: string
}

/** 不想吃条目 */
export interface DislikeData {
  id: number
  category: string
  expiresAt: string
  createdAt: string
}

/** 添加不想吃请求 */
export interface DislikeAddRequest {
  category: string
  days: number
}

/** 待处理操作 */
export interface PendingBlacklist {
  foodId: number
  foodName: string
  reason: string
}
