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
  typeTags: string       // 逗号分隔：快餐,小吃,面食,火锅,烧烤,甜品
  cuisineTags: string    // 逗号分隔：家常菜,川菜,湘菜,粤菜,日料,西餐
  mealTypes: string      // 逗号分隔：早餐,午餐,晚餐,夜宵
  tasteTags: string
  priceLevel: number
  imageUrl: string
}

/** 推荐元数据选项项 */
export interface RecommendOptionItem {
  value: string
  label: string
  hint?: string
  sortOrder: number
}

/** 展示用选项（元数据 + 选中状态），用于页面渲染。
 *  元数据状态与页面选择状态分离：RecommendOptionItem 只表示元数据，
 *  DisplayOption 额外携带 selected，由 computeDisplay() 统一计算。 */
export interface DisplayOption extends RecommendOptionItem {
  selected: boolean
}

/** 推荐元数据 */
export interface RecommendOptionsData {
  mealTypes: RecommendOptionItem[]
  priceLevels: RecommendOptionItem[]
  tastes: RecommendOptionItem[]
  typeTags: RecommendOptionItem[]
  cuisineTags: RecommendOptionItem[]
}

/** 推荐参数 */
export interface RecommendParams {
  mealType?: string
  priceLevel?: string
  taste?: string
  typeTags?: string       // 逗号分隔
  cuisineTags?: string    // 逗号分隔
  excludeFoodIds?: string
}

/** 吃过记录响应 */
export interface EatRecordData {
  id: number
  foodId: number
  foodName: string
  mealType: string
  status: string          // 'DECIDED' | 'EATEN'
  rating: number | null
  note: string
  eatenAt: string | null
  decidedAt: string | null
  category: string
}

/** 吃过记录请求（旧接口兼容） */
export interface EatRecordRequest {
  foodId: number
  mealType: string
  rating?: number
  note?: string
}

/** 决定吃什么请求 */
export interface DecideRecordRequest {
  foodId: number
  mealType: string
}

/** 完成用餐请求 */
export interface CompleteRecordRequest {
  rating?: number
  note?: string
}

/** 修改评价请求 */
export interface ReviewRecordRequest {
  rating?: number
  note?: string
}

/** 餐段选项 */
export type MealType = '早餐' | '午餐' | '晚餐' | '夜宵'

/** 价格选项 */
export type PriceLevel = '15以内' | '15-25' | '25-40' | '40以上'

/** 口味选项（用户可选，不含"不限"和"重口"） */
export type Taste = '清淡' | '辣' | '不辣'

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

/** 待决定的菜品（未登录时暂存） */
export interface PendingDecision {
  foodId: number
  foodName: string
  category: string
  mealType: string
}

/** 筛选偏好（本地持久化，version=2） */
export interface RecommendFilterPreferences {
  version: number        // 2
  mealType: string
  priceLevel: string     // 空字符串 = 不限
  taste: string          // 空字符串 = 不限
  typeTags: string[]     // 最多3个
  cuisineTags: string[]  // 最多3个
}

/** V1 旧筛选偏好（用于迁移） */
export interface RecommendFilterPreferencesV1 {
  version: number        // 1
  mealType: string
  priceLevel: string
  taste: string
  categories: string[]
}

/** 当前就餐决定（本地存储，v2 含 recordId） */
export interface CurrentMealDecision {
  version: number
  recordId: number
  foodId: number
  foodName: string
  category: string
  mealType: string
  decidedAt: string
}
