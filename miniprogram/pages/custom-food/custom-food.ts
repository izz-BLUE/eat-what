// pages/custom-food/custom-food.ts

import { createCustomFood, listCustomFoods, deleteCustomFood } from '../../services/api'
import { RequestError } from '../../utils/request'
import { CustomFoodResponse, RecommendOptionsData, RecommendOptionItem, DisplayOption } from '../../types/index'
import { fetchRecommendOptions, getFallbackOptions } from '../../services/recommend-options'

const app = getApp<IApp>()

/** 价格选项（硬编码，值对应后端 priceLevel 1-4） */
const PRICE_LEVEL_OPTIONS = [
  { value: 1, label: '15元以内' },
  { value: 2, label: '15-25元' },
  { value: 3, label: '25-40元' },
  { value: 4, label: '40以上' }
]

Page({
  data: {
    // --- 表单 ---
    name: '',
    selectedMealTypes: [] as string[],
    selectedTasteTags: [] as string[],
    selectedTypeTags: [] as string[],
    selectedCuisineTags: [] as string[],
    selectedPriceLevel: 0 as number,   // 0 = 未选

    // --- 标签元数据 ---
    mealTypeOptions: [] as DisplayOption[],
    tasteTagOptions: [] as DisplayOption[],
    typeTagOptions: [] as DisplayOption[],
    cuisineTagOptions: [] as DisplayOption[],
    priceLevelOptions: PRICE_LEVEL_OPTIONS,

    // --- 校验 ---
    nameError: '',
    mealTypeError: '',
    tasteTagError: '',
    categoryError: '',

    // --- 列表 ---
    items: [] as CustomFoodResponse[],

    // --- UI 状态 ---
    loading: true,
    notLoggedIn: false,
    errorMsg: '',
    submitting: false,
    removing: 0,          // 正在删除的 item id，0 = 无
    _loginRedirecting: false
  },

  onLoad() {
    // 立即用 fallback 渲染标签选项
    const fallback = getFallbackOptions()
    this._optionsMeta = {
      mealTypes: fallback.mealTypes,
      tastes: fallback.tastes,
      typeTags: fallback.typeTags,
      cuisineTags: fallback.cuisineTags
    }
    this._syncAllSelected()
    // 异步拉取后端最新元数据
    this.loadOptions()
  },

  onShow() {
    if (!app.isLoggedIn()) {
      this.setData({ loading: false, notLoggedIn: true, errorMsg: '', items: [] })
      return
    }
    this.setData({ notLoggedIn: false })
    this._loginRedirecting = false
    this.loadData()
  },

  /** 异步加载最新标签元数据 */
  async loadOptions() {
    try {
      const fresh = await fetchRecommendOptions()
      this._optionsMeta = {
        mealTypes: fresh.mealTypes,
        tastes: fresh.tastes,
        typeTags: fresh.typeTags,
        cuisineTags: fresh.cuisineTags
      }
      this._syncAllSelected()
    } catch (_e) {
      // fallback 已渲染，静默失败
    }
  },

  /** 加载自定义菜品列表 */
  async loadData() {
    this.setData({ loading: true, errorMsg: '' })
    try {
      const items = await listCustomFoods()
      this.setData({ items })
    } catch (err: any) {
      if (err instanceof RequestError && err.code === 1003) {
        if (!this._loginRedirecting) {
          this._loginRedirecting = true
          wx.navigateTo({ url: '/pages/login/login' })
        }
        return
      }
      this.setData({ errorMsg: err.message || '加载失败' })
    } finally {
      this.setData({ loading: false })
    }
  },

  onPullDownRefresh() {
    this.loadData().then(() => wx.stopPullDownRefresh()).catch(() => wx.stopPullDownRefresh())
  },

  // ==================== 表单交互 ====================

  onNameInput(e: WechatMiniprogram.Input) {
    this.setData({ name: e.detail.value || '', nameError: '' })
  },

  toggleMealType(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    const arr = [...this.data.selectedMealTypes]
    const idx = arr.indexOf(value)
    if (idx > -1) { arr.splice(idx, 1) } else { arr.push(value) }
    this.setData({ selectedMealTypes: arr, mealTypeError: '' })
    this._syncAllSelected()
  },

  toggleTasteTag(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    const arr = [...this.data.selectedTasteTags]
    const idx = arr.indexOf(value)
    if (idx > -1) { arr.splice(idx, 1) } else { arr.push(value) }
    this.setData({ selectedTasteTags: arr, tasteTagError: '' })
    this._syncAllSelected()
  },

  toggleTypeTag(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    const arr = [...this.data.selectedTypeTags]
    const idx = arr.indexOf(value)
    if (idx > -1) { arr.splice(idx, 1) } else { arr.push(value) }
    this.setData({ selectedTypeTags: arr, categoryError: '' })
    this._syncAllSelected()
  },

  toggleCuisineTag(e: WechatMiniprogram.TouchEvent) {
    const value = e.currentTarget.dataset.value as string
    const arr = [...this.data.selectedCuisineTags]
    const idx = arr.indexOf(value)
    if (idx > -1) { arr.splice(idx, 1) } else { arr.push(value) }
    this.setData({ selectedCuisineTags: arr, categoryError: '' })
    this._syncAllSelected()
  },

  selectPriceLevel(e: WechatMiniprogram.TouchEvent) {
    const value = Number(e.currentTarget.dataset.value)
    this.setData({ selectedPriceLevel: this.data.selectedPriceLevel === value ? 0 : value })
  },

  // ==================== 提交 ====================

  async handleSubmit() {
    if (this.data.submitting) return

    // 校验
    const name = (this.data.name || '').trim()
    const nameError = !name ? '请输入菜品名称' : (name.length > 30 ? '名称不能超过30个字' : '')
    const mealTypeError = this.data.selectedMealTypes.length === 0 ? '请至少选择一个餐段' : ''
    const tasteTagError = this.data.selectedTasteTags.length === 0 ? '请至少选择一个口味' : ''
    const hasCategory = this.data.selectedTypeTags.length > 0 || this.data.selectedCuisineTags.length > 0
    const categoryError = !hasCategory ? '请至少选择一个食物类型或菜系' : ''

    if (nameError || mealTypeError || tasteTagError || categoryError) {
      this.setData({ nameError, mealTypeError, tasteTagError, categoryError })
      return
    }

    this.setData({ submitting: true, errorMsg: '' })

    try {
      await createCustomFood({
        name,
        typeTags: this.data.selectedTypeTags,
        cuisineTags: this.data.selectedCuisineTags,
        mealTypes: this.data.selectedMealTypes,
        tasteTags: this.data.selectedTasteTags,
        priceLevel: this.data.selectedPriceLevel > 0 ? this.data.selectedPriceLevel : undefined
      })
      wx.showToast({ title: '添加成功', icon: 'success' })

      // 重置表单
      this.setData({
        name: '',
        selectedMealTypes: [],
        selectedTasteTags: [],
        selectedTypeTags: [],
        selectedCuisineTags: [],
        selectedPriceLevel: 0,
        nameError: '',
        mealTypeError: '',
        tasteTagError: '',
        categoryError: ''
      })
      this._syncAllSelected()

      // 刷新列表
      this.loadData()
    } catch (err: any) {
      if (err instanceof RequestError && err.code === 1003) {
        if (!this._loginRedirecting) {
          this._loginRedirecting = true
          wx.navigateTo({ url: '/pages/login/login' })
        }
        return
      }
      wx.showToast({ title: err.message || '添加失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  // ==================== 删除 ====================

  handleDelete(e: WechatMiniprogram.TouchEvent) {
    const id = Number(e.currentTarget.dataset.id)
    const name = e.currentTarget.dataset.name as string
    if (this.data.removing) return

    wx.showModal({
      title: '删除自定义菜',
      content: `确定删除「${name}」吗？删除后将不再推荐。`,
      success: async (res) => {
        if (!res.confirm) return
        this.setData({ removing: id })
        try {
          await deleteCustomFood(id)
          wx.showToast({ title: '已删除', icon: 'success' })
          this.loadData()
        } catch (err: any) {
          if (err instanceof RequestError && err.code === 1003) {
            if (!this._loginRedirecting) {
              this._loginRedirecting = true
              wx.navigateTo({ url: '/pages/login/login' })
            }
            return
          }
          wx.showToast({ title: err.message || '删除失败', icon: 'none' })
        } finally {
          this.setData({ removing: 0 })
        }
      }
    })
  },

  goToLogin() {
    if (this._loginRedirecting) return
    this._loginRedirecting = true
    wx.navigateTo({ url: '/pages/login/login' })
  },

  _optionsMeta: {
    mealTypes: [] as RecommendOptionItem[],
    tastes: [] as RecommendOptionItem[],
    typeTags: [] as RecommendOptionItem[],
    cuisineTags: [] as RecommendOptionItem[]
  },

  /** 将原始选项与当前选中值同步，生成带 selected 字段的选项数组 */
  _syncAllSelected() {
    const selectedMealTypes = this.data.selectedMealTypes as string[]
    const selectedTasteTags = this.data.selectedTasteTags as string[]
    const selectedTypeTags = this.data.selectedTypeTags as string[]
    const selectedCuisineTags = this.data.selectedCuisineTags as string[]
    this.setData({
      mealTypeOptions: this._optionsMeta.mealTypes.map(o => ({ ...o, selected: selectedMealTypes.indexOf(o.value) > -1 })),
      tasteTagOptions: this._optionsMeta.tastes.map(o => ({ ...o, selected: selectedTasteTags.indexOf(o.value) > -1 })),
      typeTagOptions: this._optionsMeta.typeTags.map(o => ({ ...o, selected: selectedTypeTags.indexOf(o.value) > -1 })),
      cuisineTagOptions: this._optionsMeta.cuisineTags.map(o => ({ ...o, selected: selectedCuisineTags.indexOf(o.value) > -1 }))
    })
  },

  _loginRedirecting: false as boolean
})
