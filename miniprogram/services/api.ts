// services/api.ts - API 接口封装

import { get, post, del, put } from '../utils/request'
import {
  LoginData,
  RecommendData,
  RecommendParams,
  EatRecordData,
  EatRecordRequest,
  DecideRecordRequest,
  CompleteRecordRequest,
  ReviewRecordRequest,
  BlacklistData,
  BlacklistAddRequest,
  DislikeData,
  DislikeAddRequest
} from '../types/index'

/**
 * 微信登录
 */
export function login(code: string, nickname?: string, avatarUrl?: string) {
  return post<LoginData>('/api/v1/user/login', { code, nickname, avatarUrl })
}

/**
 * 一键推荐
 */
export function getRecommend(params?: RecommendParams) {
  return get<RecommendData>('/api/v1/recommend', params)
}

/**
 * 换一个
 */
export function swapRecommend(params?: RecommendParams) {
  return get<RecommendData>('/api/v1/recommend/swap', params)
}

/**
 * 决定吃什么（创建 DECIDED 记录）
 */
export function decideFood(data: DecideRecordRequest) {
  return post<EatRecordData>('/api/v1/record/decide', data)
}

/**
 * 完成用餐（DECIDED → EATEN）
 */
export function completeRecord(recordId: number, data: CompleteRecordRequest) {
  return post<EatRecordData>(`/api/v1/record/${recordId}/complete`, data)
}

/**
 * 修改已吃记录的评价
 */
export function reviewRecord(recordId: number, data: ReviewRecordRequest) {
  return put<EatRecordData>(`/api/v1/record/${recordId}/review`, data)
}

/**
 * 取消决定（删除 DECIDED 记录）
 */
export function cancelDecisionRecord(recordId: number) {
  return del<void>(`/api/v1/record/${recordId}/decision`)
}

/**
 * 获取单条记录详情
 */
export function getRecord(recordId: number) {
  return get<EatRecordData>(`/api/v1/record/${recordId}`)
}

/**
 * 我就吃它（旧接口，保留兼容）
 */
export function eatFood(data: EatRecordRequest) {
  return post<EatRecordData>('/api/v1/record/eat', data)
}

/**
 * 获取吃过记录列表
 */
export function getEatRecords(limit: number = 50) {
  return get<EatRecordData[]>('/api/v1/record/list', { limit })
}

/**
 * 获取黑名单列表
 */
export function getBlacklist() {
  return get<BlacklistData[]>('/api/v1/blacklist/list')
}

/**
 * 加入黑名单
 */
export function addBlacklist(data: BlacklistAddRequest) {
  return post<BlacklistData>('/api/v1/blacklist/add', data)
}

/**
 * 移出黑名单
 */
export function removeBlacklist(blacklistId: number) {
  return del<void>(`/api/v1/blacklist/${blacklistId}`)
}

/**
 * 获取不想吃列表
 */
export function getDislikes() {
  return get<DislikeData[]>('/api/v1/dislike/list')
}

/**
 * 添加不想吃
 */
export function addDislike(data: DislikeAddRequest) {
  return post<DislikeData>('/api/v1/dislike/add', data)
}

/**
 * 解除不想吃
 */
export function removeDislike(dislikeId: number) {
  return del<void>(`/api/v1/dislike/${dislikeId}`)
}
