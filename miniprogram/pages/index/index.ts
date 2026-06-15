// pages/index/index.ts

import { getRecommend, swapRecommend, addBlacklist } from '../../services/api'
import { RequestError } from '../../utils/request'
import { RecommendData, MealType, PriceLevel, Taste } from '../../types/index'
import { config } from '../../config/index'

const app = getApp<IApp>()

// 分类选项类型
interface CategoryOption {
  name: string
  selected: boolean
  disabled: boolean
}

// 常用分类
const MAIN_CATEGORIES = ['快餐', '小吃', '面食', '火锅']
// 更多分类
const MORE_CATEGORIES = ['西餐', '日料', '烧烤', '川菜', '家常菜', '粤菜', '湘菜', '甜品']
// 清淡时禁用的重口味分类
const HEAVY_CATEGORIES = ['火锅', '烧烤', '川菜', '湘菜']

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
    priceDisplay: '',
    loading: false,
    errorMsg: '',
    swapCount: 0,
    excludeFoodIds: [] as number[],
    maxSwapCount: config.maxSwapCount,
    showMoreCategories: false
  },

  onLoad() {
    this.refreshCategoryOptions()
  },

  // 刷新分类选项状态
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
    this.setData({
      selectedMealType: this.data.selectedMealType === value ? '' : value
    })
    this.resetRecommendState()
  },

  selectPriceLevel(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    this.setData({
      selectedPriceLevel: this.data.selectedPriceLevel === value ? '' : value as PriceLevel
    })
    this.resetRecommendState()
  },

  selectTaste(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as Taste
    const newTaste = this.data.selectedTaste === value ? '' : value

    // 选择清淡时，自动取消重口味分类
    let selectedCategories = [...this.data.selectedCategories]
    if (newTaste === '清淡') {
      const heavyCategories = ['火锅', '烧烤', '川菜', '湘菜']
      selectedCategories = selectedCategories.filter(c => !heavyCategories.includes(c))
    }

    this.setData({
      selectedTaste: newTaste,
      selectedCategories
    })
    this.refreshCategoryOptions()
    this.resetRecommendState()
  },

  // 选择分类
  selectCategory(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string

    // 清淡状态下禁止选择重口味分类
    if (this.data.selectedTaste === '清淡' && HEAVY_CATEGORIES.includes(value)) {
      return
    }

    let selected = [...this.data.selectedCategories]
    const index = selected.indexOf(value)
    if (index > -1) {
      selected.splice(index, 1)
    } else {
      if (selected.length >= 3) {
        wx.showToast({ title: '最多选择3个分类', icon: 'none' })
        return
      }
      selected.push(value)
    }

    this.setData({ selectedCategories: selected })
    this.refreshCategoryOptions()
    this.resetRecommendState()
  },

  // 点击"不限"
  selectNoLimit() {
    this.setData({ selectedCategories: [] })
    this.refreshCategoryOptions()
    this.resetRecommendState()
  },

  // 切换更多分类弹层
  toggleMoreCategories() {
    this.setData({ showMoreCategories: !this.data.showMoreCategories })
  },

  // 重置推荐状态
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

  eatIt() {
    if (!this.data.recommendResult) return

    const food = this.data.recommendResult.food

    if (!app.isLoggedIn()) {
      // 设置 pendingRecord 时清除 pendingBlacklist（互斥）
      app.globalData.pendingBlacklist = null
      app.globalData.pendingRecord = {
        foodId: food.id,
        foodName: food.name,
        category: food.category,
        mealType: this.data.selectedMealType
      }
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }

    this.goToRecord(food)
  },

  goToRecord(food: { id: number; name: string; category: string }) {
    wx.navigateTo({
      url: `/pages/record/record?foodId=${food.id}&foodName=${encodeURIComponent(food.name)}&category=${encodeURIComponent(food.category)}&mealType=${encodeURIComponent(this.data.selectedMealType || '')}`
    })
  },

  async addToBlacklist() {
    if (!this.data.recommendResult) return
    if (this.data.loading) return // 防止重复请求
    const food = this.data.recommendResult.food

    if (!app.isLoggedIn()) {
      // 设置 pendingBlacklist 时清除 pendingRecord（互斥）
      app.globalData.pendingRecord = null
      app.globalData.pendingBlacklist = { foodId: food.id, reason: '不喜欢' }
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }

    this.setData({ loading: true })
    try {
      await addBlacklist({ foodId: food.id, reason: '不喜欢' })
      wx.showToast({ title: '已加入黑名单', icon: 'success' })
      // 将当前 foodId 加入排除列表并清空结果
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
