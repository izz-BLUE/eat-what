// services/recommend-options.ts
// 推荐选项元数据：加载、缓存、校验、fallback
// 首页和 dislike 页面复用，禁止各自复制缓存逻辑

import { getRecommendOptions } from './api'
import { RecommendOptionsData, RecommendOptionItem } from '../types/index'

// ============ 缓存 ============

const CACHE_KEY = 'recommendOptions'
const CACHE_TTL_MS = 24 * 60 * 60 * 1000 // 24 小时

interface CachedOptions {
  data: RecommendOptionsData
  fetchedAt: number
}

// ============ Fallback（与后端 RecommendDict 一致） ============

const FALLBACK: RecommendOptionsData = {
  mealTypes: [
    { value: '早餐', label: '早餐', sortOrder: 1 },
    { value: '午餐', label: '午餐', sortOrder: 2 },
    { value: '晚餐', label: '晚餐', sortOrder: 3 },
    { value: '夜宵', label: '夜宵', sortOrder: 4 },
  ],
  priceLevels: [
    { value: '15以内', label: '15元内', hint: '参考价位', sortOrder: 1 },
    { value: '15-25', label: '15-25元', hint: '参考价位', sortOrder: 2 },
    { value: '25-40', label: '25-40元', hint: '参考价位', sortOrder: 3 },
    { value: '40以上', label: '40元以上', hint: '参考价位', sortOrder: 4 },
  ],
  tastes: [
    { value: '清淡', label: '清淡', sortOrder: 1 },
    { value: '辣', label: '辣', hint: '含麻辣', sortOrder: 2 },
    { value: '不辣', label: '不辣', sortOrder: 3 },
  ],
  typeTags: [
    { value: '快餐', label: '快餐', sortOrder: 1 },
    { value: '小吃', label: '小吃', sortOrder: 2 },
    { value: '面食', label: '面食', sortOrder: 3 },
    { value: '火锅', label: '火锅', sortOrder: 4 },
    { value: '烧烤', label: '烧烤', sortOrder: 5 },
    { value: '甜品', label: '甜品', sortOrder: 6 },
  ],
  cuisineTags: [
    { value: '家常菜', label: '家常菜', sortOrder: 1 },
    { value: '川菜', label: '川菜', sortOrder: 2 },
    { value: '湘菜', label: '湘菜', sortOrder: 3 },
    { value: '粤菜', label: '粤菜', sortOrder: 4 },
    { value: '日料', label: '日料', sortOrder: 5 },
    { value: '西餐', label: '西餐', sortOrder: 6 },
  ],
}

// ============ 校验 ============

/**
 * 简易校验：确保各维度是非空数组且包含 value 字段
 */
function isValidOptionsData(data: unknown): data is RecommendOptionsData {
  if (!data || typeof data !== 'object') return false
  const d = data as Record<string, unknown>
  const keys: (keyof RecommendOptionsData)[] = ['mealTypes', 'priceLevels', 'tastes', 'typeTags', 'cuisineTags']
  for (const key of keys) {
    const arr = d[key]
    if (!Array.isArray(arr) || arr.length === 0) return false
    for (const item of arr) {
      if (!item || typeof item !== 'object' || typeof (item as any).value !== 'string') return false
    }
  }
  return true
}

// ============ 公共 API ============

/**
 * 获取推荐选项元数据
 * - 优先返回缓存（24h 内有效）
 * - 缓存过期或不存在时从后端拉取
 * - 后端失败或响应损坏时返回 fallback
 * - 成功后更新缓存
 */
export async function fetchRecommendOptions(): Promise<RecommendOptionsData> {
  // 1. 检查缓存
  const cached = loadCachedOptions()
  if (cached) return cached.data

  // 2. 尝试从后端获取
  try {
    const fresh = await getRecommendOptions()
    if (isValidOptionsData(fresh)) {
      saveCachedOptions(fresh)
      return fresh
    }
    // 响应格式损坏，使用 fallback
    console.warn('[recommend-options] 后端响应格式不正确，使用 fallback')
    return FALLBACK
  } catch (err) {
    console.warn('[recommend-options] 获取元数据失败，使用 fallback:', err)
    return FALLBACK
  }
}

/**
 * 同步获取选项（立即返回，可能为 fallback）
 * 用于页面初始化时立即渲染，不等待网络
 */
export function getFallbackOptions(): RecommendOptionsData {
  const cached = loadCachedOptions()
  return cached ? cached.data : FALLBACK
}

/**
 * 合并后端选项：先从 fallback 渲染，再异步更新
 * 返回最终的选项数据和是否由后端提供
 */
export function mergeOptions(
  fallback: RecommendOptionsData,
  fresh: RecommendOptionsData
): RecommendOptionsData {
  // 以 fresh 为主；如果 fresh 校验失败，keep fallback
  return fresh
}

// ============ 内部 ============

function loadCachedOptions(): CachedOptions | null {
  try {
    const raw = wx.getStorageSync(CACHE_KEY)
    if (!raw) return null
    let obj: unknown = raw
    if (typeof raw === 'string') {
      try { obj = JSON.parse(raw) } catch (e) { return null }
    }
    if (!obj || typeof obj !== 'object') return null
    const c = obj as Record<string, unknown>
    if (!isValidOptionsData(c.data)) return null
    const fetchedAt = typeof c.fetchedAt === 'number' ? c.fetchedAt : 0
    // 检查 TTL
    if (Date.now() - fetchedAt > CACHE_TTL_MS) return null
    return { data: c.data as RecommendOptionsData, fetchedAt }
  } catch (e) {
    return null
  }
}

function saveCachedOptions(data: RecommendOptionsData) {
  const cached: CachedOptions = { data, fetchedAt: Date.now() }
  try {
    wx.setStorageSync(CACHE_KEY, cached)
  } catch (e) {
    // 存储满，静默忽略
  }
}
