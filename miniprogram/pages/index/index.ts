// pages/index/index.ts

import { getRecommend, swapRecommend, addBlacklist, decideFood, getRecord, cancelDecisionRecord } from '../../services/api'
import { RequestError } from '../../utils/request'
import { RecommendData, MealType, PriceLevel, Taste, RecommendFilterPreferences, CurrentMealDecision } from '../../types/index'
import { config } from '../../config/index'

const app = getApp<IApp>()

// ============ 公共常量 ============

const VALID_MEAL_TYPES = ['早餐', '午餐', '晚餐', '夜宵']

// ============ 就餐决定本地存储 ============

const DECISION_KEY = 'currentMealDecision'
const DECISION_VERSION = 2

function sanitizeMealDecision(raw: unknown): CurrentMealDecision | null {
  if (!raw || typeof raw !== 'object') return null
  const d = raw as Record<string, unknown>

  // v1 旧格式（无 recordId）→ 自动清理
  if (d.version === 1) {
    try { wx.removeStorageSync(DECISION_KEY) } catch (e) { /* ignore */ }
    return null
  }

  if (d.version !== DECISION_VERSION) return null

  // recordId 必须为正整数
  const recordId = typeof d.recordId === 'number' ? d.recordId : Number(d.recordId)
  if (!(Number.isInteger(recordId) && recordId > 0)) return null

  // foodId 必须为正整数
  const foodId = typeof d.foodId === 'number' ? d.foodId : Number(d.foodId)
  if (!(Number.isInteger(foodId) && foodId > 0)) return null

  // foodName 非空
  if (typeof d.foodName !== 'string' || !d.foodName.trim()) return null

  // category 非空
  if (typeof d.category !== 'string' || !d.category.trim()) return null

  // mealType 合法枚举
  const mealType = typeof d.mealType === 'string' ? d.mealType : ''
  if (!VALID_MEAL_TYPES.includes(mealType)) return null

  // decidedAt 有效时间
  const decidedAt = typeof d.decidedAt === 'string' ? d.decidedAt : ''
  if (!decidedAt || isNaN(Date.parse(decidedAt))) return null

  return {
    version: DECISION_VERSION,
    recordId,
    foodId,
    foodName: d.foodName.trim(),
    category: d.category.trim(),
    mealType,
    decidedAt
  }
}

// ============ 筛选偏好本地持久化 ============

const STORAGE_KEY = 'recommendFilterPrefs'
const STORAGE_VERSION = 1
const VALID_PRICE_LEVELS = ['15以内', '15-25', '25-40', '不限']
const VALID_TASTES = ['不限', '清淡', '重口', '辣', '不辣']
const VALID_CATEGORIES = ['快餐', '小吃', '面食', '火锅', '西餐', '日料', '烧烤', '川菜', '家常菜', '粤菜', '湘菜', '甜品']
const HEAVY_CATEGORIES = ['火锅', '烧烤', '川菜', '湘菜']

function getDefaultMealType(): string {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 11) return '早餐'
  if (hour >= 11 && hour < 14) return '午餐'
  if (hour >= 14 && hour < 21) return '晚餐'
  return '夜宵'
}

function sanitizeFilterPreferences(raw: unknown): RecommendFilterPreferences | null {
  if (!raw || typeof raw !== 'object') return null
  const prefs = raw as Record<string, unknown>
  if (prefs.version !== STORAGE_VERSION) return null

  const mealType = typeof prefs.mealType === 'string' && VALID_MEAL_TYPES.includes(prefs.mealType)
    ? prefs.mealType : getDefaultMealType()
  const priceLevel = typeof prefs.priceLevel === 'string' && VALID_PRICE_LEVELS.includes(prefs.priceLevel)
    ? prefs.priceLevel : '不限'
  const taste = typeof prefs.taste === 'string' && VALID_TASTES.includes(prefs.taste)
    ? prefs.taste : '不限'

  let categories: string[] = []
  if (Array.isArray(prefs.categories)) {
    categories = [...new Set(
      (prefs.categories as unknown[]).filter(
        (c): c is string => typeof c === 'string' && VALID_CATEGORIES.includes(c)
      )
    )].slice(0, 3)
  }

  // 清淡时自动删除重口味分类
  if (taste === '清淡') {
    categories = categories.filter(c => !HEAVY_CATEGORIES.includes(c))
  }

  return { version: STORAGE_VERSION, mealType, priceLevel, taste, categories }
}

// ============ 分类选项 ============

interface CategoryOption {
  name: string
  selected: boolean
  disabled: boolean
}

const MAIN_CATEGORIES = ['快餐', '小吃', '面食', '火锅']
const MORE_CATEGORIES = ['西餐', '日料', '烧烤', '川菜', '家常菜', '粤菜', '湘菜', '甜品']

Page({
  data: {
    mealTypes: ['早餐', '午餐', '晚餐', '夜宵'] as MealType[],
    priceLevels: [
      { label: '15元内', value: '15以内' },
      { label: '15-25元', value: '15-25' },
      { label: '25-40元', value: '25-40' },
      { label: '不限', value: '不限' }
    ],
    tastes: ['不限', '清淡', '重口', '辣', '不辣'] as Taste[],
    commonCategoryOptions: [] as CategoryOption[],
    moreCategoryOptions: [] as CategoryOption[],
    selectedMealType: '' as MealType | '',
    selectedPriceLevel: '' as PriceLevel | '',
    selectedTaste: '不限' as Taste | '',
    selectedCategories: [] as string[],
    recommendResult: null as RecommendData | null,
    mealDecision: null as CurrentMealDecision | null,
    decisionTimeDisplay: '',
    priceDisplay: '',
    loading: false,
    errorMsg: '',
    swapCount: 0,
    excludeFoodIds: [] as number[],
    maxSwapCount: config.maxSwapCount,
    showMoreCategories: false,
    _navigatingToRecord: false,
    _reselectionInProgress: false
  },

  onLoad() {
    const prefs = this.loadFilterPreferences()
    if (prefs) {
      this.setData({
        selectedMealType: prefs.mealType as MealType,
        selectedPriceLevel: prefs.priceLevel as PriceLevel,
        selectedTaste: prefs.taste as Taste,
        selectedCategories: prefs.categories
      })
    } else {
      this.setData({ selectedMealType: getDefaultMealType() as MealType })
    }
    this.refreshCategoryOptions()
    this.restoreMealDecision()
  },

  onShow() {
    // 重置防重复标志
    this.setData({ _navigatingToRecord: false, _reselectionInProgress: false })

    // 消费登录后的待处理结果
    const result = app.globalData.pendingResult
    if (result) {
      app.globalData.pendingResult = null
      if (result.type === 'blacklist') {
        this.setData({ recommendResult: null, swapCount: 0 })
        if (!this.data.excludeFoodIds.includes(result.foodId)) {
          this.data.excludeFoodIds.push(result.foodId)
        }
        wx.showToast({ title: '已加入黑名单', icon: 'success' })
      } else if (result.type === 'decision') {
        wx.showToast({ title: '已决定', icon: 'success' })
      }
    }
    // 重新检查决定状态
    this.restoreMealDecision()
  },

  refreshCategoryOptions() {
    const selected = this.data.selectedCategories
    const isLight = this.data.selectedTaste === '清淡'
    const commonCategoryOptions = MAIN_CATEGORIES.map(name => ({
      name,
      selected: selected.includes(name),
      disabled: isLight && HEAVY_CATEGORIES.includes(name)
    }))
    const moreCategoryOptions = MORE_CATEGORIES.map(name => ({
      name,
      selected: selected.includes(name),
      disabled: isLight && HEAVY_CATEGORIES.includes(name)
    }))
    this.setData({ commonCategoryOptions, moreCategoryOptions })
  },

  selectMealType(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as MealType
    const next = this.data.selectedMealType === value ? '' : value
    this.setData({ selectedMealType: next })
    this.resetRecommendState()
    this.saveFilterPreferences({ selectedMealType: next })
  },

  selectPriceLevel(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    const next = (this.data.selectedPriceLevel === value ? '' : value) as PriceLevel
    this.setData({ selectedPriceLevel: next })
    this.resetRecommendState()
    this.saveFilterPreferences({ selectedPriceLevel: next })
  },

  selectTaste(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as Taste
    const nextTaste = this.data.selectedTaste === value ? '' : value

    let nextCategories = [...this.data.selectedCategories]
    if (nextTaste === '清淡') {
      nextCategories = nextCategories.filter(c => !HEAVY_CATEGORIES.includes(c))
    }

    this.setData({
      selectedTaste: nextTaste,
      selectedCategories: nextCategories
    })
    this.refreshCategoryOptions()
    this.resetRecommendState()
    this.saveFilterPreferences({ selectedTaste: nextTaste, selectedCategories: nextCategories })
  },

  selectCategory(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string

    if (this.data.selectedTaste === '清淡' && HEAVY_CATEGORIES.includes(value)) {
      return
    }

    let next = [...this.data.selectedCategories]
    const index = next.indexOf(value)
    if (index > -1) {
      next.splice(index, 1)
    } else {
      if (next.length >= 3) {
        wx.showToast({ title: '最多选择3个分类', icon: 'none' })
        return
      }
      next.push(value)
    }

    this.setData({ selectedCategories: next })
    this.refreshCategoryOptions()
    this.resetRecommendState()
    this.saveFilterPreferences({ selectedCategories: next })
  },

  selectNoLimit() {
    this.setData({ selectedCategories: [] })
    this.refreshCategoryOptions()
    this.resetRecommendState()
    this.saveFilterPreferences({ selectedCategories: [] })
  },

  toggleMoreCategories() {
    this.setData({ showMoreCategories: !this.data.showMoreCategories })
  },

  // ============ 筛选偏好持久化 ============

  loadFilterPreferences(): RecommendFilterPreferences | null {
    try {
      const raw: unknown = wx.getStorageSync(STORAGE_KEY)
      if (!raw) return null
      let obj: unknown = raw
      if (typeof raw === 'string') {
        try { obj = JSON.parse(raw) } catch (e) { return null }
      }
      return sanitizeFilterPreferences(obj)
    } catch (e) {
      return null
    }
  },

  saveFilterPreferences(nextState?: {
    selectedMealType?: string
    selectedPriceLevel?: string
    selectedTaste?: string
    selectedCategories?: string[]
  }) {
    const prefs = {
      version: STORAGE_VERSION,
      mealType: nextState?.selectedMealType ?? this.data.selectedMealType,
      priceLevel: nextState?.selectedPriceLevel ?? this.data.selectedPriceLevel,
      taste: nextState?.selectedTaste ?? this.data.selectedTaste,
      categories: nextState?.selectedCategories ?? this.data.selectedCategories
    }
    try {
      wx.setStorageSync(STORAGE_KEY, prefs)
    } catch (e) {
      // 存储满，静默忽略
    }
  },

  handleReset() {
    wx.showModal({
      title: '重置筛选',
      content: '确定要重置所有筛选条件吗？',
      success: (res) => {
        if (!res.confirm) return
        try { wx.removeStorageSync(STORAGE_KEY) } catch (e) { /* ignore */ }
        this.setData({
          selectedMealType: getDefaultMealType() as MealType,
          selectedPriceLevel: '' as PriceLevel,
          selectedTaste: '不限' as Taste,
          selectedCategories: [],
          recommendResult: null,
          swapCount: 0,
          excludeFoodIds: [],
          errorMsg: '',
          showMoreCategories: false
        })
        this.refreshCategoryOptions()
        wx.showToast({ title: '已重置', icon: 'success' })
      }
    })
  },

  resetRecommendState() {
    this.setData({
      recommendResult: null,
      swapCount: 0,
      excludeFoodIds: [],
      errorMsg: ''
    })
  },

  async getRecommend(retryCount = 0) {
    this.setData({ loading: true, errorMsg: '', recommendResult: null, swapCount: 0, excludeFoodIds: [] })

    try {
      const params: Record<string, string> = {}
      if (this.data.selectedMealType) params.mealType = this.data.selectedMealType
      if (this.data.selectedPriceLevel) params.priceLevel = this.data.selectedPriceLevel
      if (this.data.selectedTaste) params.taste = this.data.selectedTaste
      if (this.data.selectedCategories.length > 0) params.categories = this.data.selectedCategories.join(',')

      const result = await getRecommend(params)
      const priceDisplay = this.getPriceDisplay(result.food.priceLevel)
      this.setData({ recommendResult: result, priceDisplay })
    } catch (err: any) {
      if (err instanceof RequestError) {
        if (err.code === 1003 && retryCount < 1) {
          await this.getRecommend(retryCount + 1)
          return
        }
        if (err.code === 2002) {
          this.setData({ errorMsg: '当前条件没有合适菜品，请调整分类或口味' })
        } else if (err.code === 1003) {
          this.setData({ errorMsg: '登录已过期，请重新登录后享受个性化推荐' })
        } else {
          this.setData({ errorMsg: err.message || '推荐失败，请重试' })
        }
      } else {
        this.setData({ errorMsg: err.message || '网络异常，请重试' })
      }
    } finally {
      this.setData({ loading: false })
    }
  },

  async swapRecommend(retryCount = 0) {
    if (this.data.swapCount >= config.maxSwapCount) {
      wx.showToast({ title: '没有更多推荐了', icon: 'none' })
      return
    }

    if (this.data.recommendResult) {
      const currentFoodId = this.data.recommendResult.food.id
      if (!this.data.excludeFoodIds.includes(currentFoodId)) {
        this.data.excludeFoodIds.push(currentFoodId)
      }
    }

    this.setData({ loading: true, errorMsg: '' })

    try {
      const params: Record<string, string> = {}
      if (this.data.selectedMealType) params.mealType = this.data.selectedMealType
      if (this.data.selectedPriceLevel) params.priceLevel = this.data.selectedPriceLevel
      if (this.data.selectedTaste) params.taste = this.data.selectedTaste
      if (this.data.selectedCategories.length > 0) params.categories = this.data.selectedCategories.join(',')
      if (this.data.excludeFoodIds.length > 0) params.excludeFoodIds = [...new Set(this.data.excludeFoodIds)].join(',')

      const result = await swapRecommend(params)
      const priceDisplay = this.getPriceDisplay(result.food.priceLevel)
      this.setData({
        recommendResult: result,
        priceDisplay,
        swapCount: this.data.swapCount + 1
      })
    } catch (err: any) {
      if (err instanceof RequestError) {
        if (err.code === 1003 && retryCount < 1) {
          await this.swapRecommend(retryCount + 1)
          return
        }
        if (err.code === 2002) {
          this.setData({ errorMsg: '当前条件没有合适菜品，请调整分类或口味' })
        } else {
          this.setData({ errorMsg: err.message || '推荐失败，请重试' })
        }
      } else {
        this.setData({ errorMsg: err.message || '网络异常，请重试' })
      }
    } finally {
      this.setData({ loading: false })
    }
  },

  getPriceDisplay(priceLevel: number): string {
    switch (priceLevel) {
      case 1: return '15元以内'
      case 2: return '15-25元'
      case 3: return '25-40元'
      case 4: return '40元以上'
      default: return '价格面议'
    }
  },

  /** 我就吃它：确认后调用后端 decide 接口，未登录则暂存待登录后执行 */
  eatIt() {
    if (!this.data.recommendResult) return
    const food = this.data.recommendResult.food

    wx.showModal({
      title: '确定就吃这个？',
      content: `今天就吃「${food.name}」`,
      success: (res) => {
        if (!res.confirm) return

        if (!app.isLoggedIn()) {
          // 未登录：暂存决定，跳转登录
          app.globalData.pendingDecision = {
            foodId: food.id,
            foodName: food.name,
            category: food.category,
            mealType: this.data.selectedMealType || ''
          }
          app.globalData.pendingBlacklist = null
          wx.navigateTo({ url: '/pages/login/login' })
          return
        }

        this.executeDecide(food)
      }
    })
  },

  /** 调用后端 decide 接口，保存返回 recordId 到本地 */
  async executeDecide(food: { id: number; name: string; category: string }) {
    this.setData({ loading: true })
    try {
      const result = await decideFood({
        foodId: food.id,
        mealType: this.data.selectedMealType || ''
      })

      const decidedAt = new Date().toISOString()
      const decision: CurrentMealDecision = {
        version: DECISION_VERSION,
        recordId: result.id,
        foodId: food.id,
        foodName: food.name,
        category: food.category,
        mealType: this.data.selectedMealType || '',
        decidedAt
      }
      this.saveMealDecision(decision)
      this.setData({
        mealDecision: decision,
        decisionTimeDisplay: this.formatLocalTime(decidedAt),
        recommendResult: null
      })
      wx.showToast({ title: '已决定', icon: 'success' })
    } catch (err: any) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  /** 吃完了，记录一下：携带 recordId 跳转 record 页 */
  handleFinishEating() {
    if (this.data._navigatingToRecord) return
    if (!this.data.mealDecision) return
    this.setData({ _navigatingToRecord: true })
    const d = this.data.mealDecision

    wx.navigateTo({
      url: `/pages/record/record?recordId=${d.recordId}&foodName=${encodeURIComponent(d.foodName)}&category=${encodeURIComponent(d.category)}&mealType=${encodeURIComponent(d.mealType)}`,
      fail: () => {
        this.setData({ _navigatingToRecord: false })
      }
    })
  },

  /** 重新选择：二次确认，调用后端取消决定 + 清除本地状态 */
  handleReselect() {
    if (this.data._reselectionInProgress) return
    this.setData({ _reselectionInProgress: true })
    wx.showModal({
      title: '重新选择',
      content: '确定要重新选择吗？当前决定将被清除。',
      success: async (res) => {
        if (!res.confirm) {
          this.setData({ _reselectionInProgress: false })
          return
        }
        // 如果已登录且有 recordId，调用后端取消
        const d = this.data.mealDecision
        if (d && app.isLoggedIn()) {
          try {
            await cancelDecisionRecord(d.recordId)
          } catch (e) {
            // 服务端取消失败不影响本地清除（可能记录已不存在）
          }
        }
        this.clearMealDecision()
        this.setData({
          _reselectionInProgress: false,
          mealDecision: null,
          decisionTimeDisplay: '',
          recommendResult: null,
          swapCount: 0,
          excludeFoodIds: [],
          errorMsg: ''
        })
      },
      fail: () => {
        this.setData({ _reselectionInProgress: false })
      }
    })
  },

  // ============ 就餐决定持久化 ============

  /** 恢复决定状态，并校验服务端是否仍为 DECIDED */
  async restoreMealDecision() {
    const decision = this.loadMealDecision()
    if (!decision) {
      this.setData({ mealDecision: null, decisionTimeDisplay: '' })
      return
    }

    // 已登录 → 校验服务端状态
    if (app.isLoggedIn()) {
      try {
        const record = await getRecord(decision.recordId)
        if (record.status !== 'DECIDED') {
          // 服务端已不是 DECIDED → 清除本地
          this.clearMealDecision()
          this.setData({ mealDecision: null, decisionTimeDisplay: '' })
          return
        }
        // 同步最新数据
        decision.foodName = record.foodName
        decision.category = record.category
        decision.mealType = record.mealType
      } catch (e) {
        // 网络错误 → 保留本地（降级可用），下次 onShow 再校验
      }
    }

    this.setData({
      mealDecision: decision,
      decisionTimeDisplay: this.formatLocalTime(decision.decidedAt)
    })
  },

  /** ISO UTC 时间 → 本地时间字符串 */
  formatLocalTime(isoString: string): string {
    try {
      const d = new Date(isoString)
      const pad = (n: number) => n.toString().padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    } catch (e) {
      return isoString
    }
  },

  loadMealDecision(): CurrentMealDecision | null {
    try {
      const raw: unknown = wx.getStorageSync(DECISION_KEY)
      if (!raw) return null
      let obj: unknown = raw
      if (typeof raw === 'string') {
        try { obj = JSON.parse(raw) } catch (e) { return null }
      }
      const result = sanitizeMealDecision(obj)
      if (!result) {
        try { wx.removeStorageSync(DECISION_KEY) } catch (e) { /* ignore */ }
      }
      return result
    } catch (e) {
      return null
    }
  },

  saveMealDecision(decision: CurrentMealDecision) {
    try {
      wx.setStorageSync(DECISION_KEY, decision)
    } catch (e) {
      // 存储满，静默忽略
    }
  },

  clearMealDecision() {
    try { wx.removeStorageSync(DECISION_KEY) } catch (e) { /* ignore */ }
  },

  async addToBlacklist() {
    if (!this.data.recommendResult) return
    if (this.data.loading) return
    const food = this.data.recommendResult.food

    if (!app.isLoggedIn()) {
      app.globalData.pendingDecision = null
      app.globalData.pendingBlacklist = { foodId: food.id, foodName: food.name, reason: '不喜欢' }
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }

    this.setData({ loading: true })
    try {
      await addBlacklist({ foodId: food.id, reason: '不喜欢' })
      wx.showToast({ title: '已加入黑名单', icon: 'success' })
      if (!this.data.excludeFoodIds.includes(food.id)) {
        this.data.excludeFoodIds.push(food.id)
      }
      this.setData({ recommendResult: null, swapCount: 0 })
    } catch (err: any) {
      if (err instanceof RequestError && err.code === 1003) {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      wx.showToast({ title: err.message || '操作失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  goToProfile() {
    wx.navigateTo({ url: '/pages/profile/profile' })
  }
})
