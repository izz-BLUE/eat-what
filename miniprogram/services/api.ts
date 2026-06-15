// services/api.ts - API 接口封装

import { get, post } from '../utils/request'
import {
  LoginData,
  RecommendData,
  RecommendParams,
  EatRecordData,
  EatRecordRequest
} from '../types/index'

/**
 * 微信登录
 */
export function login(code: string, nickname?: string, avatarUrl?: string) {
  return post<LoginData>('/api/v1/user/login', {
    code,
    nickname,
    avatarUrl
  })
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
 * 我就吃它
 */
export function eatFood(data: EatRecordRequest) {
  return post<EatRecordData>('/api/v1/record/eat', data)
}
