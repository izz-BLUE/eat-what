// pages/index/index.ts

import { getRecommend, swapRecommend, addBlacklist, decideFood, getRecord, cancelDecisionRecord, submitRecommendationFeedback } from '../../services/api'
import { RequestError } from '../../utils/request'
import { RecommendData, MealType, RecommendFilterPreferences, RecommendFilterPreferencesV1, CurrentMealDecision, RecommendOptionsData, DisplayOption } from '../../types/index'
import { config } from '../../config/index'
import { fetchRecommendOptions, getFallbackOptions } from '../../services/recommend-options'

const app = getApp<IApp>()

// ============ 公共常量 ============

const VALID_MEAL_TYPES = ['早餐', '午餐', '晚餐', '夜宵']

// ============ 今日胃口选项 ============

const APPETITE_OPTIONS = [
  { value: '随便来点', label: '随便来点' },
  { value: '清爽点', label: '清爽点' },
  { value: '重口味', label: '重口味' },
  { value: '顶饱点', label: '顶饱点' },
  { value: '省钱吃', label: '省钱吃' },
  { value: '解馋点', label: '解馋点' },
]

// ============ 就餐决定本地存储 ============

const DECISION_KEY = 'currentMealDecision'
const DECISION_VERSION = 2

function sanitizeMealDecision(raw: unknown): CurrentMealDecision | null {
  if (!raw || typeof raw !== 'object') return null
  const d = raw as Record<string, unknown>

  if (d.version === 1) {
    try { wx.removeStorageSync(DECISION_KEY) } catch (e) { /* ignore */ }
    return null
  }

  if (d.version !== DECISION_VERSION) return null

  const recordId = typeof d.recordId === 'number' ? d.recordId : Number(d.recordId)
  if (!(Number.isInteger(recordId) && recordId > 0)) return null

  // 按来源分支校验：CUSTOM 要求 customFoodId，DEFAULT 要求 foodId
  const source = d.source === 'CUSTOM' ? 'CUSTOM' as const : undefined
  if (source === 'CUSTOM') {
    const customFoodId = typeof d.customFoodId === 'number' ? d.customFoodId : Number(d.customFoodId)
    if (!(Number.isInteger(customFoodId) && customFoodId > 0)) return null
  } else {
    const foodId = typeof d.foodId === 'number' ? d.foodId : Number(d.foodId)
    if (!(Number.isInteger(foodId) && foodId > 0)) return null
  }

  if (typeof d.foodName !== 'string' || !d.foodName.trim()) return null

  if (typeof d.category !== 'string' || !d.category.trim()) return null

  const mealType = typeof d.mealType === 'string' ? d.mealType : ''
  if (!VALID_MEAL_TYPES.includes(mealType)) return null

  const decidedAt = typeof d.decidedAt === 'string' ? d.decidedAt : ''
  if (!decidedAt || isNaN(Date.parse(decidedAt))) return null

  // foodId：DEFAULT 必填，CUSTOM 可选
  const foodId = typeof d.foodId === 'number' && Number.isInteger(d.foodId) && d.foodId > 0 ? d.foodId : undefined

  return {
    version: DECISION_VERSION,
    recordId,
    foodId,
    foodName: d.foodName.trim(),
    category: d.category.trim(),
    mealType,
    decidedAt,
    // v3 新增字段：严格校验类型后再放行
    typeTags: typeof d.typeTags === 'string' && d.typeTags.trim() ? d.typeTags.trim() : undefined,
    cuisineTags: typeof d.cuisineTags === 'string' && d.cuisineTags.trim() ? d.cuisineTags.trim() : undefined,
    tasteTags: typeof d.tasteTags === 'string' && d.tasteTags.trim() ? d.tasteTags.trim() : undefined,
    priceLevel: typeof d.priceLevel === 'number' && Number.isInteger(d.priceLevel) && d.priceLevel >= 1 && d.priceLevel <= 4 ? d.priceLevel : undefined,
    // 自定义菜字段：缺失按 DEFAULT，不清缓存
    source,
    customFoodId: typeof d.customFoodId === 'number' && Number.isInteger(d.customFoodId) && d.customFoodId > 0 ? d.customFoodId : undefined
  }
}

// ============ 筛选偏好本地持久化（V2） ============

const STORAGE_KEY = 'recommendFilterPrefs'
const STORAGE_VERSION_V2 = 2
const MAX_CATEGORY_SELECTIONS = 3

function getDefaultMealType(): string {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 11) return '早餐'
  if (hour >= 11 && hour < 14) return '午餐'
  if (hour >= 14 && hour < 21) return '晚餐'
  return '夜宵'
}

/**
 * V1→V2 迁移：将旧格式转换为新格式
 */
function migrateV1ToV2(v1: RecommendFilterPreferencesV1, options: RecommendOptionsData): RecommendFilterPreferences {
  // 收集 typeTag/cuisineTag value 集合用于校验
  const typeTagValues = new Set(options.typeTags.map(t => t.value))
  const cuisineTagValues = new Set(options.cuisineTags.map(t => t.value))

  // mealType
  const mealType = VALID_MEAL_TYPES.includes(v1.mealType) ? v1.mealType : getDefaultMealType()

  // priceLevel：不限→空字符串，40以上不可从V1迁移（V1无此选项），legacy 不限→空
  let priceLevel = v1.priceLevel || ''
  if (priceLevel === '不限' || priceLevel === '') {
    priceLevel = ''
  } else if (!['15以内', '15-25', '25-40', '40以上'].includes(priceLevel)) {
    priceLevel = ''
  }

  // taste：不限/重口→空字符串
  let taste = v1.taste || ''
  if (taste === '不限' || taste === '重口' || taste === '') {
    taste = ''
  } else if (!['清淡', '辣', '不辣'].includes(taste)) {
    taste = ''
  }

  // categories 分发
  const typeTags: string[] = []
  const cuisineTags: string[] = []
  if (Array.isArray(v1.categories)) {
    for (const c of v1.categories) {
      if (typeof c !== 'string') continue
      if (typeTagValues.has(c)) {
        typeTags.push(c)
      } else if (cuisineTagValues.has(c)) {
        cuisineTags.push(c)
      }
      // 非法值删除（不迁移）
    }
  }

  // 去重
  const merged = [...new Set([...typeTags, ...cuisineTags])]
  if (merged.length > MAX_CATEGORY_SELECTIONS) {
    // 保留前3个
    return {
      version: STORAGE_VERSION_V2,
      mealType,
      priceLevel,
      taste,
      typeTags: typeTags.slice(0, MAX_CATEGORY_SELECTIONS),
      cuisineTags: cuisineTags.slice(0, MAX_CATEGORY_SELECTIONS - typeTags.length)
    }
  }

  return {
    version: STORAGE_VERSION_V2,
    mealType,
    priceLevel,
    taste,
    typeTags: [...new Set(typeTags)],
    cuisineTags: [...new Set(cuisineTags)]
  }
}

function sanitizeFilterPreferencesV2(raw: unknown, options: RecommendOptionsData): RecommendFilterPreferences | null {
  if (!raw || typeof raw !== 'object') return null
  const prefs = raw as Record<string, unknown>

  // V1→V2 migration
  if (prefs.version === 1) {
    const v1 = raw as unknown as RecommendFilterPreferencesV1
    return migrateV1ToV2(v1, options)
  }

  if (prefs.version !== STORAGE_VERSION_V2) return null

  const typeTagValues = new Set(options.typeTags.map(t => t.value))
  const cuisineTagValues = new Set(options.cuisineTags.map(t => t.value))

  const mealType = typeof prefs.mealType === 'string' && VALID_MEAL_TYPES.includes(prefs.mealType)
    ? prefs.mealType : getDefaultMealType()

  const priceLevel = typeof prefs.priceLevel === 'string'
    && (prefs.priceLevel === '' || ['15以内', '15-25', '25-40', '40以上'].includes(prefs.priceLevel))
    ? prefs.priceLevel : ''

  const taste = typeof prefs.taste === 'string'
    && (prefs.taste === '' || ['清淡', '辣', '不辣'].includes(prefs.taste))
    ? prefs.taste : ''

  let typeTags: string[] = []
  let cuisineTags: string[] = []
  if (Array.isArray(prefs.typeTags)) {
    typeTags = [...new Set(
      (prefs.typeTags as unknown[]).filter(
        (t): t is string => typeof t === 'string' && typeTagValues.has(t)
      )
    )]
  }
  if (Array.isArray(prefs.cuisineTags)) {
    cuisineTags = [...new Set(
      (prefs.cuisineTags as unknown[]).filter(
        (t): t is string => typeof t === 'string' && cuisineTagValues.has(t)
      )
    )]
  }

  // 合计不超过 3
  const total = typeTags.length + cuisineTags.length
  if (total > MAX_CATEGORY_SELECTIONS) {
    // 保留前3，优先保留 typeTags
    const keep = MAX_CATEGORY_SELECTIONS - typeTags.length
    if (keep > 0) {
      cuisineTags = cuisineTags.slice(0, keep)
    } else {
      typeTags = typeTags.slice(0, MAX_CATEGORY_SELECTIONS)
      cuisineTags = []
    }
  }

  return { version: STORAGE_VERSION_V2, mealType, priceLevel, taste, typeTags, cuisineTags }
}

// ============ 展示选项计算（纯函数，不读写 this.data） ============

interface DisplayData {
  mealTypes: DisplayOption[]
  priceLevels: DisplayOption[]
  tastes: DisplayOption[]
  typeTagOptions: DisplayOption[]
  cuisineTagOptions: DisplayOption[]
  categoryNoLimitSelected: boolean
  priceNoLimitSelected: boolean
  tasteNoLimitSelected: boolean
}

/**
 * 根据当前选择状态，为元数据每个选项计算 selected 标志。
 * 纯函数：不修改入参，不读取页面 this.data，返回全新对象。
 *
 * 规则：
 * - typeOptions[i].selected = selectedTypeTags.includes(typeOptions[i].value)
 * - cuisineOptions[i].selected = selectedCuisineTags.includes(cuisineOptions[i].value)
 * - categoryNoLimitSelected = typeTags.length===0 && cuisineTags.length===0
 * - priceNoLimitSelected = selectedPriceLevel===''
 * - tasteNoLimitSelected = selectedTaste===''
 */
function computeDisplay(
  options: RecommendOptionsData,
  selectedMealType: string,
  selectedPriceLevel: string,
  selectedTaste: string,
  selectedTypeTags: string[],
  selectedCuisineTags: string[]
): DisplayData {
  return {
    mealTypes: options.mealTypes.map(m => ({
      ...m,
      selected: m.value === selectedMealType
    })),
    priceLevels: options.priceLevels.map(p => ({
      ...p,
      selected: p.value === selectedPriceLevel
    })),
    tastes: options.tastes.map(t => ({
      ...t,
      selected: t.value === selectedTaste
    })),
    typeTagOptions: options.typeTags.map(t => ({
      ...t,
      selected: selectedTypeTags.includes(t.value)
    })),
    cuisineTagOptions: options.cuisineTags.map(c => ({
      ...c,
      selected: selectedCuisineTags.includes(c.value)
    })),
    categoryNoLimitSelected: selectedTypeTags.length === 0 && selectedCuisineTags.length === 0,
    priceNoLimitSelected: selectedPriceLevel === '',
    tasteNoLimitSelected: selectedTaste === ''
  }
}

Page({
  data: {
    // 选项数据（由 recommend-options 服务提供）
    options: null as RecommendOptionsData | null,
    appetiteOptions: APPETITE_OPTIONS as DisplayOption[],
    mealTypes: [] as DisplayOption[],
    priceLevels: [] as DisplayOption[],
    tastes: [] as DisplayOption[],
    typeTagOptions: [] as DisplayOption[],
    cuisineTagOptions: [] as DisplayOption[],
    categoryNoLimitSelected: true,
    priceNoLimitSelected: true,
    tasteNoLimitSelected: true,

    // 用户选择
    selectedMealType: '' as string,
    selectedAppetite: '' as string,          // 空=未选择今日胃口
    selectedPriceLevel: '' as string,      // 空=不限
    selectedTaste: '' as string,            // 空=不限
    selectedTypeTags: [] as string[],
    selectedCuisineTags: [] as string[],

    // 推荐状态
    recommendResult: null as RecommendData | null,
    mealDecision: null as CurrentMealDecision | null,
    decisionTimeDisplay: '',
    priceDisplay: '',
    loading: false,
    errorMsg: '',
    swapCount: 0,
    excludeFoodIds: [] as number[],
    excludeCustomFoodIds: [] as number[],
    swapExhausted: false,               // 换一个 2002 空状态
    filtersCollapsed: false,            // 筛选区是否折叠
    filtersSummary: '',                 // 折叠时显示的筛选摘要
    maxSwapCount: config.maxSwapCount,
    disliking: false,                   // 不喜欢反馈进行中，防重复点击
    _navigatingToRecord: false,
    _reselectionInProgress: false,
    _categoryTotalExceeded: false
  },

  onLoad() {
    // 1. 获取 fallback 元数据
    const fallback = getFallbackOptions()
    const defaultMealType = getDefaultMealType()

    // 2. 加载偏好（可能触发 V1→V2 迁移）
    const prefs = this.loadFilterPreferences(fallback)

    // 3. 确定初始选择值（无偏好时 typeTags/cuisineTags 为空 → 只高亮"不限"）
    const initMealType = prefs ? prefs.mealType : defaultMealType
    const initPriceLevel = prefs ? prefs.priceLevel : ''
    const initTaste = prefs ? prefs.taste : ''
    const initTypeTags = prefs ? prefs.typeTags : []
    const initCuisineTags = prefs ? prefs.cuisineTags : []

    // 4. 一次 setData：初始值 + 计算好的 display
    this.setData({
      options: fallback,
      selectedMealType: initMealType,
      selectedPriceLevel: initPriceLevel,
      selectedTaste: initTaste,
      selectedTypeTags: initTypeTags,
      selectedCuisineTags: initCuisineTags,
      ...computeDisplay(fallback, initMealType, initPriceLevel, initTaste, initTypeTags, initCuisineTags)
    })

    this.restoreMealDecision()

    // 5. 异步拉取后端元数据（返回后用当前选择重建 display，不重置选中状态）
    this.loadRecommendOptions()
  },

  onShow() {
    this.setData({ _navigatingToRecord: false, _reselectionInProgress: false })

    const result = app.globalData.pendingResult
    if (result) {
      app.globalData.pendingResult = null
      if (result.type === 'blacklist') {
        this.setData({ recommendResult: null, swapCount: 0, swapExhausted: false })
        if (typeof result.foodId === 'number' && !this.data.excludeFoodIds.includes(result.foodId)) {
          this.data.excludeFoodIds.push(result.foodId!)
        }
        wx.showToast({ title: '已加入黑名单', icon: 'success' })
      } else if (result.type === 'decision') {
        wx.showToast({ title: '已决定', icon: 'success' })
      }
    }
    this.restoreMealDecision()
  },

  async loadRecommendOptions() {
    try {
      const fresh = await fetchRecommendOptions()
      // 基于当前选择 + 新元数据重建 display，不把所有选项重置成选中
      this.setData({
        options: fresh,
        ...computeDisplay(
          fresh,
          this.data.selectedMealType,
          this.data.selectedPriceLevel,
          this.data.selectedTaste,
          this.data.selectedTypeTags,
          this.data.selectedCuisineTags
        )
      })
    } catch (e) {
      // fallback 已渲染，不阻塞
    }
  },

  // ============ 筛选偏好持久化 ============

  loadFilterPreferences(options: RecommendOptionsData): RecommendFilterPreferences | null {
    try {
      const raw: unknown = wx.getStorageSync(STORAGE_KEY)
      if (!raw) return null
      let obj: unknown = raw
      if (typeof raw === 'string') {
        try { obj = JSON.parse(raw) } catch (e) { return null }
      }
      const prefs = sanitizeFilterPreferencesV2(obj, options)
      // 如果迁移成功（V1→V2），立即覆盖保存
      if (prefs && prefs.version === STORAGE_VERSION_V2) {
        this.saveFilterPreferencesDirect(prefs)
      }
      return prefs
    } catch (e) {
      return null
    }
  },

  saveFilterPreferencesDirect(prefs: RecommendFilterPreferences) {
    try {
      wx.setStorageSync(STORAGE_KEY, prefs)
    } catch (e) {
      // 存储满，静默忽略
    }
  },

  saveFilterPreferences(nextState?: Partial<{
    selectedMealType: string
    selectedPriceLevel: string
    selectedTaste: string
    selectedTypeTags: string[]
    selectedCuisineTags: string[]
  }>) {
    const prefs: RecommendFilterPreferences = {
      version: STORAGE_VERSION_V2,
      mealType: nextState?.selectedMealType ?? this.data.selectedMealType,
      priceLevel: nextState?.selectedPriceLevel ?? this.data.selectedPriceLevel,
      taste: nextState?.selectedTaste ?? this.data.selectedTaste,
      typeTags: nextState?.selectedTypeTags ?? this.data.selectedTypeTags,
      cuisineTags: nextState?.selectedCuisineTags ?? this.data.selectedCuisineTags
    }
    this.saveFilterPreferencesDirect(prefs)
  },

  // ============ 选择事件 ============

  selectMealType(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    // 餐段不允许取消选择
    if (this.data.selectedMealType === value) return
    if (!this.data.options) return

    const display = computeDisplay(
      this.data.options, value,
      this.data.selectedPriceLevel, this.data.selectedTaste,
      this.data.selectedTypeTags, this.data.selectedCuisineTags
    )
    this.setData({
      selectedMealType: value,
      ...display,
      recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], errorMsg: '', swapExhausted: false
    })
    this.saveFilterPreferences({ selectedMealType: value })
  },

  selectPriceLevel(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    // 点击已选中的 → 切回"不限"(空)；点击"不限"(空) → 保持"不限"
    const next = this.data.selectedPriceLevel === value ? '' : value
    if (!this.data.options) return

    const display = computeDisplay(
      this.data.options, this.data.selectedMealType, next,
      this.data.selectedTaste,
      this.data.selectedTypeTags, this.data.selectedCuisineTags
    )
    this.setData({
      selectedPriceLevel: next,
      ...display,
      recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], errorMsg: '', swapExhausted: false
    })
    this.saveFilterPreferences({ selectedPriceLevel: next })
  },

  selectTaste(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    // 点击已选中的 → 切回"不限"(空)
    const next = this.data.selectedTaste === value ? '' : value
    if (!this.data.options) return

    const display = computeDisplay(
      this.data.options, this.data.selectedMealType, this.data.selectedPriceLevel,
      next,
      this.data.selectedTypeTags, this.data.selectedCuisineTags
    )
    this.setData({
      selectedTaste: next,
      ...display,
      recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], errorMsg: '', swapExhausted: false
    })
    this.saveFilterPreferences({ selectedTaste: next })
  },

  /** 选择/取消食物类型（toggle，合计最多3） */
  selectTypeTag(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    const nextTypeTags = [...this.data.selectedTypeTags]
    const index = nextTypeTags.indexOf(value)
    if (index > -1) {
      nextTypeTags.splice(index, 1)
    } else {
      const total = nextTypeTags.length + this.data.selectedCuisineTags.length
      if (total >= MAX_CATEGORY_SELECTIONS) {
        wx.showToast({ title: `最多选择${MAX_CATEGORY_SELECTIONS}个偏好分类`, icon: 'none' })
        return
      }
      nextTypeTags.push(value)
    }
    if (!this.data.options) return

    // 先计算 display → 一次 setData → 再保存
    const display = computeDisplay(
      this.data.options,
      this.data.selectedMealType, this.data.selectedPriceLevel, this.data.selectedTaste,
      nextTypeTags, this.data.selectedCuisineTags
    )
    this.setData({
      selectedTypeTags: nextTypeTags,
      ...display,
      recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], errorMsg: '', swapExhausted: false
    })
    this.saveFilterPreferences({ selectedTypeTags: nextTypeTags })
  },

  /** 选择/取消菜系（toggle，合计最多3） */
  selectCuisineTag(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    const nextCuisineTags = [...this.data.selectedCuisineTags]
    const index = nextCuisineTags.indexOf(value)
    if (index > -1) {
      nextCuisineTags.splice(index, 1)
    } else {
      const total = this.data.selectedTypeTags.length + nextCuisineTags.length
      if (total >= MAX_CATEGORY_SELECTIONS) {
        wx.showToast({ title: `最多选择${MAX_CATEGORY_SELECTIONS}个偏好分类`, icon: 'none' })
        return
      }
      nextCuisineTags.push(value)
    }
    if (!this.data.options) return

    const display = computeDisplay(
      this.data.options,
      this.data.selectedMealType, this.data.selectedPriceLevel, this.data.selectedTaste,
      this.data.selectedTypeTags, nextCuisineTags
    )
    this.setData({
      selectedCuisineTags: nextCuisineTags,
      ...display,
      recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], errorMsg: '', swapExhausted: false
    })
    this.saveFilterPreferences({ selectedCuisineTags: nextCuisineTags })
  },

  /** 清空分类选择（不限） */
  selectCategoryNoLimit() {
    if (!this.data.options) return

    const display = computeDisplay(
      this.data.options,
      this.data.selectedMealType, this.data.selectedPriceLevel, this.data.selectedTaste,
      [], []
    )
    this.setData({
      selectedTypeTags: [],
      selectedCuisineTags: [],
      ...display,
      recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], errorMsg: '', swapExhausted: false
    })
    this.saveFilterPreferences({ selectedTypeTags: [], selectedCuisineTags: [] })
  },

  // ============ 今日胃口 ============

  selectAppetite(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    if (!this.data.options) return

    // 点击已选中的 → 撤销该胃口设置过的筛选项
    if (this.data.selectedAppetite === value) {
      this.revertAppetite(value)
      return
    }

    let newTaste = this.data.selectedTaste
    let newPriceLevel = this.data.selectedPriceLevel
    let newTypeTags = [...this.data.selectedTypeTags]
    let newCuisineTags = [...this.data.selectedCuisineTags]

    switch (value) {
      case '随便来点':
        // 放宽条件：清空 taste / priceLevel / typeTags / cuisineTags，保留 mealType
        newTaste = ''
        newPriceLevel = ''
        newTypeTags = []
        newCuisineTags = []
        break
      case '清爽点':
        newTaste = '清淡'
        break
      case '重口味':
        newTaste = '辣'
        break
      case '顶饱点':
        newTypeTags = ['快餐', '面食']
        newCuisineTags = ['家常菜']
        break
      case '省钱吃':
        newPriceLevel = '15以内'
        break
      case '解馋点':
        newTypeTags = ['小吃', '烧烤', '火锅']
        break
    }

    const display = computeDisplay(
      this.data.options,
      this.data.selectedMealType,
      newPriceLevel,
      newTaste,
      newTypeTags,
      newCuisineTags
    )

    this.setData({
      selectedAppetite: value,
      selectedPriceLevel: newPriceLevel,
      selectedTaste: newTaste,
      selectedTypeTags: newTypeTags,
      selectedCuisineTags: newCuisineTags,
      ...display,
      recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], errorMsg: '', swapExhausted: false
    })

    this.saveFilterPreferences({
      selectedPriceLevel: newPriceLevel,
      selectedTaste: newTaste,
      selectedTypeTags: newTypeTags,
      selectedCuisineTags: newCuisineTags
    })
  },

  /** 撤销某个今日胃口曾设置的筛选条件 */
  revertAppetite(appetite: string) {
    if (!this.data.options) return

    let newTaste = this.data.selectedTaste
    let newPriceLevel = this.data.selectedPriceLevel
    let newTypeTags = [...this.data.selectedTypeTags]
    let newCuisineTags = [...this.data.selectedCuisineTags]

    switch (appetite) {
      case '清爽点':
        if (newTaste === '清淡') newTaste = ''
        break
      case '重口味':
        if (newTaste === '辣') newTaste = ''
        break
      case '省钱吃':
        if (newPriceLevel === '15以内') newPriceLevel = ''
        break
      case '顶饱点':
        newTypeTags = newTypeTags.filter(t => !['快餐', '面食'].includes(t))
        newCuisineTags = newCuisineTags.filter(c => c !== '家常菜')
        break
      case '解馋点':
        newTypeTags = newTypeTags.filter(t => !['小吃', '烧烤', '火锅'].includes(t))
        break
      case '随便来点':
        // 随便来点仅清空筛选，撤销时无法恢复，保持当前筛选不变
        break
    }

    const display = computeDisplay(
      this.data.options,
      this.data.selectedMealType,
      newPriceLevel,
      newTaste,
      newTypeTags,
      newCuisineTags
    )

    this.setData({
      selectedAppetite: '',
      selectedPriceLevel: newPriceLevel,
      selectedTaste: newTaste,
      selectedTypeTags: newTypeTags,
      selectedCuisineTags: newCuisineTags,
      ...display,
      recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], errorMsg: '', swapExhausted: false
    })

    this.saveFilterPreferences({
      selectedPriceLevel: newPriceLevel,
      selectedTaste: newTaste,
      selectedTypeTags: newTypeTags,
      selectedCuisineTags: newCuisineTags
    })
  },

  // ============ 筛选区折叠 ============

  computeFiltersSummary(): string {
    const parts: string[] = []
    if (this.data.selectedMealType) parts.push(this.data.selectedMealType)
    if (this.data.selectedAppetite) parts.push(this.data.selectedAppetite)
    if (this.data.selectedPriceLevel) {
      const priceLabel: Record<string, string> = {
        '15以内': '15元内', '15-25': '15-25元', '25-40': '25-40元', '40以上': '40元以上'
      }
      parts.push(priceLabel[this.data.selectedPriceLevel] || this.data.selectedPriceLevel)
    }
    if (this.data.selectedTaste) parts.push(this.data.selectedTaste)
    if (this.data.selectedTypeTags.length > 0) parts.push(this.data.selectedTypeTags.join('/'))
    if (this.data.selectedCuisineTags.length > 0) parts.push(this.data.selectedCuisineTags.join('/'))
    return parts.length > 0 ? parts.join(' · ') : '未设置筛选条件'
  },

  toggleFiltersCollapsed() {
    const next = !this.data.filtersCollapsed
    const set: Record<string, any> = { filtersCollapsed: next }
    if (next) {
      set.filtersSummary = this.computeFiltersSummary()
    }
    this.setData(set)
  },

  scrollToResult() {
    wx.pageScrollTo({ selector: '#recommend-result-anchor', duration: 300 })
  },

  // ============ 推荐与换一个 ============

  resetRecommendState() {
    this.setData({
      recommendResult: null,
      swapCount: 0,
      excludeFoodIds: [],
      excludeCustomFoodIds: [],
      errorMsg: '',
      swapExhausted: false
    })
  },

  /** 构建请求参数：使用 typeTags/cuisineTags，不再使用 categories */
  buildRequestParams(): Record<string, string> {
    const params: Record<string, string> = {}
    if (this.data.selectedMealType) params.mealType = this.data.selectedMealType
    // priceLevel 空=不限，不发送
    if (this.data.selectedPriceLevel) params.priceLevel = this.data.selectedPriceLevel
    // taste 空=不限，不发送
    if (this.data.selectedTaste) params.taste = this.data.selectedTaste
    // typeTags
    if (this.data.selectedTypeTags.length > 0) {
      params.typeTags = this.data.selectedTypeTags.join(',')
    }
    // cuisineTags
    if (this.data.selectedCuisineTags.length > 0) {
      params.cuisineTags = this.data.selectedCuisineTags.join(',')
    }
    // excludeCustomFoodIds 在 swapRecommend 中按需设置
    return params
  },

  async getRecommend(retryCount = 0) {
    this.setData({ loading: true, errorMsg: '', recommendResult: null, swapCount: 0, excludeFoodIds: [], excludeCustomFoodIds: [], swapExhausted: false })

    try {
      const params = this.buildRequestParams()
      const result = await getRecommend(params)
      const priceDisplay = this.getPriceDisplay(result.food.priceLevel)
      this.setData({
        recommendResult: result,
        priceDisplay,
        filtersCollapsed: true,
        filtersSummary: this.computeFiltersSummary()
      }, () => this.scrollToResult())
    } catch (err: any) {
      if (err instanceof RequestError) {
        if (err.code === 1003 && retryCount < 1) {
          await this.getRecommend(retryCount + 1)
          return
        }
        if (err.code === 2002) {
          this.setData({ errorMsg: '当前条件没有合适菜品，请调整筛选条件', recommendResult: null })
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
      // 按钮已 disabled，兜底不发送请求
      return
    }

    if (this.data.recommendResult) {
      const food = this.data.recommendResult.food
      const source = food.source || 'DEFAULT'
      if (source === 'CUSTOM' && typeof food.customFoodId === 'number') {
        const id = food.customFoodId
        if (!this.data.excludeCustomFoodIds.includes(id)) {
          this.data.excludeCustomFoodIds.push(id)
        }
      } else {
        const currentFoodId = food.id
        if (!this.data.excludeFoodIds.includes(currentFoodId)) {
          this.data.excludeFoodIds.push(currentFoodId)
        }
      }
    }

    this.setData({ loading: true, errorMsg: '', swapExhausted: false })

    try {
      const params = this.buildRequestParams()
      if (this.data.excludeFoodIds.length > 0) {
        params.excludeFoodIds = [...new Set(this.data.excludeFoodIds)].join(',')
      }
      if (this.data.excludeCustomFoodIds.length > 0) {
        params.excludeCustomFoodIds = [...new Set(this.data.excludeCustomFoodIds)].join(',')
      }

      const result = await swapRecommend(params)
      const priceDisplay = this.getPriceDisplay(result.food.priceLevel)
      this.setData({
        recommendResult: result,
        priceDisplay,
        swapCount: this.data.swapCount + 1
      }, () => this.scrollToResult())
    } catch (err: any) {
      if (err instanceof RequestError) {
        if (err.code === 1003 && retryCount < 1) {
          await this.swapRecommend(retryCount + 1)
          return
        }
        if (err.code === 2002) {
          // 无更多候选 → 显示独立空状态
          this.setData({
            recommendResult: null,
            swapExhausted: true,
            errorMsg: ''
          })
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

  /** 重置已看过（清空 excludeFoodIds + swapCount，保留筛选条件） */
  async resetSwapState() {
    this.setData({
      loading: true,
      errorMsg: '',
      swapExhausted: false,
      swapCount: 0,
      excludeFoodIds: [],
      excludeCustomFoodIds: [],
      recommendResult: null
    })

    try {
      const params = this.buildRequestParams()
      const result = await getRecommend(params)
      const priceDisplay = this.getPriceDisplay(result.food.priceLevel)
      this.setData({
        recommendResult: result,
        priceDisplay,
        filtersCollapsed: true,
        filtersSummary: this.computeFiltersSummary()
      }, () => this.scrollToResult())
    } catch (err: any) {
      if (err instanceof RequestError) {
        if (err.code === 2002) {
          this.setData({ errorMsg: '当前条件没有合适菜品，请调整筛选条件' })
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
      default: return '参考价位未知'
    }
  },

  // ============ 我就吃它 ============

  eatIt() {
    if (!this.data.recommendResult) return
    const food = this.data.recommendResult.food

    wx.showModal({
      title: '确定就吃这个？',
      content: `今天就吃「${food.name}」`,
      success: (res) => {
        if (!res.confirm) return

        if (!app.isLoggedIn()) {
          const source = food.source || 'DEFAULT'
          app.globalData.pendingDecision = {
            foodId: source === 'CUSTOM' ? undefined : food.id,
            foodName: food.name,
            category: food.category,
            mealType: this.data.selectedMealType || '',
            source,
            customFoodId: source === 'CUSTOM' && typeof food.customFoodId === 'number' ? food.customFoodId : undefined
          }
          app.globalData.pendingBlacklist = null
          wx.navigateTo({ url: '/pages/login/login' })
          return
        }

        this.executeDecide(food)
      }
    })
  },

  async executeDecide(food: { id: number; name: string; category: string; typeTags?: string; cuisineTags?: string; tasteTags?: string; priceLevel?: number; source?: 'DEFAULT' | 'CUSTOM'; customFoodId?: number | null }) {
    this.setData({ loading: true })
    try {
      const source = food.source || 'DEFAULT'
      const decideParams: any = {
        mealType: this.data.selectedMealType || ''
      }
      if (source === 'CUSTOM' && typeof food.customFoodId === 'number') {
        decideParams.customFoodId = food.customFoodId
      } else {
        decideParams.foodId = food.id
      }
      const result = await decideFood(decideParams)

      const decidedAt = new Date().toISOString()
      const decision: CurrentMealDecision = {
        version: DECISION_VERSION,
        recordId: result.id,
        foodId: source === 'CUSTOM' ? undefined : food.id,
        foodName: food.name,
        category: food.category,
        mealType: this.data.selectedMealType || '',
        decidedAt,
        typeTags: food.typeTags,
        cuisineTags: food.cuisineTags,
        tasteTags: food.tasteTags,
        priceLevel: food.priceLevel,
        source: source as 'DEFAULT' | 'CUSTOM',
        customFoodId: source === 'CUSTOM' && typeof food.customFoodId === 'number' ? food.customFoodId : undefined
      }
      this.saveMealDecision(decision)
      this.setData({
        mealDecision: decision,
        decisionTimeDisplay: this.formatLocalTime(decidedAt),
        recommendResult: null,
        swapExhausted: false
      })
      wx.showToast({ title: '已决定', icon: 'success' })
    } catch (err: any) {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  handleFinishEating() {
    if (this.data._navigatingToRecord) return
    if (!this.data.mealDecision) return
    this.setData({ _navigatingToRecord: true })
    const d = this.data.mealDecision

    wx.navigateTo({
      url: `/pages/record/record?recordId=${d.recordId}&foodName=${encodeURIComponent(d.foodName)}&category=${encodeURIComponent(d.category)}&mealType=${encodeURIComponent(d.mealType)}&source=${d.source || 'DEFAULT'}${typeof d.customFoodId === 'number' ? '&customFoodId=' + d.customFoodId : ''}`,
      fail: () => {
        this.setData({ _navigatingToRecord: false })
      }
    })
  },

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
        const d = this.data.mealDecision
        if (d && app.isLoggedIn()) {
          try {
            await cancelDecisionRecord(d.recordId)
          } catch (e) {
            // 服务端取消失败不影响本地
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
          excludeCustomFoodIds: [],
          errorMsg: '',
          swapExhausted: false,
          filtersCollapsed: false,
          filtersSummary: ''
        })
      },
      fail: () => {
        this.setData({ _reselectionInProgress: false })
      }
    })
  },

  // ============ 就餐决定持久化 ============

  async restoreMealDecision() {
    const decision = this.loadMealDecision()
    if (!decision) {
      this.setData({ mealDecision: null, decisionTimeDisplay: '' })
      return
    }

    if (app.isLoggedIn()) {
      try {
        const record = await getRecord(decision.recordId)
        if (record.status !== 'DECIDED') {
          this.clearMealDecision()
          this.setData({ mealDecision: null, decisionTimeDisplay: '' })
          return
        }
        decision.foodName = record.foodName
        decision.category = record.category
        decision.mealType = record.mealType
      } catch (e) {
        // 网络错误，保留本地
      }
    }

    this.setData({
      mealDecision: decision,
      decisionTimeDisplay: this.formatLocalTime(decision.decidedAt)
    })
  },

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
    } catch (e) { /* ignore */ }
  },

  clearMealDecision() {
    try { wx.removeStorageSync(DECISION_KEY) } catch (e) { /* ignore */ }
  },

  // ============ 黑名单 ============

  async addToBlacklist() {
    if (!this.data.recommendResult) return
    if (this.data.loading) return
    const food = this.data.recommendResult.food

    // 自定义菜不能加入默认黑名单，去自定义菜列表删除即可
    if (food.source === 'CUSTOM') {
      wx.showToast({ title: '自定义菜可在自定义菜列表删除', icon: 'none' })
      return
    }

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
      this.setData({ recommendResult: null, swapCount: 0, swapExhausted: false })
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

  // ============ 不喜欢这次推荐 ============

  handleDislikeRecommendation() {
    if (!this.data.recommendResult) return
    if (this.data.disliking) return

    const food = this.data.recommendResult.food
    const source = food.source || 'DEFAULT'
    const reasonMap: Record<number, string> = {
      0: 'RECENTLY_EATEN',
      1: 'NOT_IN_MOOD',
      2: 'TOO_EXPENSIVE',
      3: 'TOO_HEAVY',
      4: 'WRONG_TASTE',
      5: 'WRONG_CATEGORY',
      6: 'OTHER'
    }

    wx.showActionSheet({
      itemList: ['最近吃过', '今天不想吃这个', '太贵了', '太油/太腻', '口味不合适', '类型不想吃', '其他'],
      success: (res) => {
        const reason = reasonMap[res.tapIndex]
        this.doDislikeRecommendation(food, source, reason)
      }
    })
  },

  async doDislikeRecommendation(
    food: { id: number; name: string; source?: string; customFoodId?: number | null;
            typeTags?: string; cuisineTags?: string; tasteTags?: string; priceLevel?: number },
    source: string,
    reason: string
  ) {
    if (this.data.disliking) return
    this.setData({ loading: true, disliking: true })

    // 1. 先把当前菜加入排除列表（去重；不管提交成功与否，立刻换一个）
    if (source === 'CUSTOM' && typeof food.customFoodId === 'number') {
      this.data.excludeCustomFoodIds = [...new Set([...this.data.excludeCustomFoodIds, food.customFoodId])]
    } else {
      this.data.excludeFoodIds = [...new Set([...this.data.excludeFoodIds, food.id])]
    }

    // 2. 提交反馈（非阻塞，失败不影响换推荐）
    try {
      await submitRecommendationFeedback({
        source: source as 'DEFAULT' | 'CUSTOM',
        foodId: source === 'DEFAULT' ? food.id : undefined,
        customFoodId: source === 'CUSTOM' && typeof food.customFoodId === 'number' ? food.customFoodId : undefined,
        foodName: food.name,
        reason: reason as any,
        mealType: this.data.selectedMealType || undefined,
        priceLevel: this.getPriceDisplay(food.priceLevel || 0),
        taste: food.tasteTags || undefined,
        typeTags: food.typeTags || undefined,
        cuisineTags: food.cuisineTags || undefined
      })
      wx.showToast({ title: '已记录，换一个试试', icon: 'none' })
    } catch (_e) {
      // 反馈提交失败不阻断换一个
      wx.showToast({ title: '已换一个', icon: 'none' })
    }

    // 3. 自动换一个
    try {
      const params = this.buildRequestParams()
      if (this.data.excludeFoodIds.length > 0) {
        params.excludeFoodIds = this.data.excludeFoodIds.join(',')
      }
      if (this.data.excludeCustomFoodIds.length > 0) {
        params.excludeCustomFoodIds = this.data.excludeCustomFoodIds.join(',')
      }

      const result = await swapRecommend(params)
      const priceDisplay = this.getPriceDisplay(result.food.priceLevel)
      this.setData({
        recommendResult: result,
        priceDisplay,
        swapCount: this.data.swapCount + 1
      }, () => this.scrollToResult())
    } catch (err: any) {
      if (err instanceof RequestError && err.code === 2002) {
        this.setData({ recommendResult: null, swapExhausted: true, errorMsg: '' })
      }
    } finally {
      this.setData({ loading: false, disliking: false })
    }
  },

  // ============ 重置 ============

  handleReset() {
    wx.showModal({
      title: '重置筛选',
      content: '确定要重置所有筛选条件吗？',
      success: (res) => {
        if (!res.confirm) return
        try { wx.removeStorageSync(STORAGE_KEY) } catch (e) { /* ignore */ }
        const opts = this.data.options || getFallbackOptions()
        const defaultMealType = getDefaultMealType()
        this.setData({
          selectedMealType: defaultMealType,
          selectedAppetite: '',
          selectedPriceLevel: '',
          selectedTaste: '',
          selectedTypeTags: [],
          selectedCuisineTags: [],
          ...computeDisplay(opts, defaultMealType, '', '', [], []),
          filtersSummary: '',
          recommendResult: null,
          swapCount: 0,
          excludeFoodIds: [],
          excludeCustomFoodIds: [],
          errorMsg: '',
          swapExhausted: false
        })
      }
    })
  },

  // ============ 导航 ============

  goToCustomFood() {
    if (!app.isLoggedIn()) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      app.globalData.pendingRedirect = '/pages/custom-food/custom-food'
      setTimeout(() => {
        wx.navigateTo({ url: '/pages/login/login' })
      }, 300)
      return
    }
    wx.navigateTo({ url: '/pages/custom-food/custom-food' })
  },

  goToProfile() {
    wx.navigateTo({ url: '/pages/profile/profile' })
  }
})
