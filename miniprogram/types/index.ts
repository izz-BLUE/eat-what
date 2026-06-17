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
  /** 菜品来源：DEFAULT-系统菜品，CUSTOM-用户自定义菜品 */
  source?: 'DEFAULT' | 'CUSTOM'
  /** 自定义菜品ID（source=CUSTOM 时有值） */
  customFoodId?: number | null
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
  excludeCustomFoodIds?: string
}

/** 吃过记录响应 */
export interface EatRecordData {
  id: number
  foodId: number
  /** 自定义食物ID（CUSTOM 来源时有值） */
  customFoodId?: number | null
  /** 食物来源：DEFAULT-系统菜品，CUSTOM-自定义菜品 */
  foodSource?: 'DEFAULT' | 'CUSTOM'
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
  foodId?: number
  /** 自定义食物ID（与 foodId 互斥，CUSTOM 来源时使用） */
  customFoodId?: number
  mealType: string
  rating?: number
  note?: string
}

/** 决定吃什么请求 */
export interface DecideRecordRequest {
  foodId?: number
  /** 自定义食物ID（与 foodId 互斥，CUSTOM 来源时使用） */
  customFoodId?: number
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
  /** DEFAULT 来源时必填，CUSTOM 时可空 */
  foodId?: number
  foodName: string
  category: string
  mealType: string
  /** 菜品来源 */
  source?: 'DEFAULT' | 'CUSTOM'
  /** 自定义菜品ID（source=CUSTOM 时必填） */
  customFoodId?: number
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
  /** DEFAULT 来源时必填，CUSTOM 时可空 */
  foodId?: number
  foodName: string
  category: string
  mealType: string
  decidedAt: string
  /** 以下为 v2→v3 新增，用于决定卡片展示；缺失时前端使用旧 category 兜底 */
  typeTags?: string
  cuisineTags?: string
  tasteTags?: string
  priceLevel?: number
  /** 菜品来源：缺失按 DEFAULT */
  source?: 'DEFAULT' | 'CUSTOM'
  /** 自定义菜品ID（source=CUSTOM 时有值） */
  customFoodId?: number
}

/** 反馈类型 */
export type FeedbackType = 'FEATURE' | 'BUG' | 'RECOMMENDATION' | 'UI' | 'OTHER'

/** 意见反馈请求 */
export interface FeedbackRequest {
  type: FeedbackType
  rating?: number
  content: string
  contact?: string
  page?: string
  systemInfo?: string
}

/** 意见反馈响应 */
export interface FeedbackResponse {
  id: number
  type: string
  rating: number | null
  content: string
  contact: string
  page: string
  systemInfo: string
  status: string
  createdAt: string
}

/** 创建自定义菜品请求 */
export interface CustomFoodCreateRequest {
  name: string
  typeTags: string[]       // 可选，与 cuisineTags 至少一个非空
  cuisineTags: string[]    // 可选，与 typeTags 至少一个非空
  mealTypes: string[]      // 必填，至少一个
  tasteTags: string[]      // 必填，至少一个
  priceLevel?: number      // 1-4，可选
}

/** 自定义菜品响应 */
export interface CustomFoodResponse {
  id: number
  name: string
  category: string         // 派生：cuisineTags[0] 或 typeTags[0]
  typeTags: string         // 逗号分隔
  cuisineTags: string      // 逗号分隔
  mealTypes: string        // 逗号分隔
  tasteTags: string        // 逗号分隔
  priceLevel: number | null
  enabled: boolean
  createdAt: string        // ISO datetime
  updatedAt: string        // ISO datetime
}

/** 推荐反馈原因枚举 */
export type RecommendationFeedbackReason =
  | 'RECENTLY_EATEN'   // 最近吃过
  | 'NOT_IN_MOOD'      // 今天不想吃这个
  | 'TOO_EXPENSIVE'    // 太贵了
  | 'TOO_HEAVY'        // 太油/太腻
  | 'WRONG_TASTE'      // 口味不合适
  | 'WRONG_CATEGORY'   // 类型不想吃
  | 'OTHER'            // 其他

/** 推荐反馈请求 */
export interface RecommendationFeedbackRequest {
  source: 'DEFAULT' | 'CUSTOM'
  foodId?: number
  customFoodId?: number
  foodName: string
  reason: RecommendationFeedbackReason
  mealType?: string
  priceLevel?: string
  taste?: string
  typeTags?: string
  cuisineTags?: string
}

/** 推荐反馈响应 */
export interface RecommendationFeedbackResponse {
  id: number
  reason: string
  createdAt: string
}
