import request from '@/utils/request'

export interface VipInfo {
  vipLevel: number
  discountRate: number
  vipExpireTime: string
  totalVipDays: number
  isVip: boolean
  remainingDays: number
}

export interface VipMembership {
  id: number
  type: number
  startTime: string
  endTime: string
  status: number
  pointsCost: number
  source: string
  createTime: string
}

export interface PointsProduct {
  id: number
  name: string
  description: string
  image: string
  images: string
  mainImage: string
  thumbnail: string
  type: number
  pointsCost: number
  stock: number
  dailyLimit: number
  totalLimit: number
  originalPrice: number
  relatedId: number | null
  sortOrder: number
  status: number
  createTime: string
  updateTime: string
  remainStock: number
}

export function getVipInfo() {
  return request<VipInfo>({
    url: '/api/vip/info',
    method: 'get'
  })
}

export function exchangeVip(vipType: number) {
  return request<string>({
    url: '/api/vip/exchange',
    method: 'post',
    data: { vipType }
  })
}

export function getVipHistory() {
  return request<VipMembership[]>({
    url: '/api/vip/history',
    method: 'get'
  })
}

export function checkVip() {
  return request<boolean>({
    url: '/api/vip/check',
    method: 'get'
  })
}

export function getPointsProducts() {
  return request<PointsProduct[]>({
    url: '/api/points-mall/products',
    method: 'get'
  })
}

export function getPointsProductDetail(id: number) {
  return request<PointsProduct>({
    url: `/api/points-mall/products/${id}`,
    method: 'get'
  })
}

export function exchangePointsProduct(productId: number) {
  return request<string>({
    url: '/api/points-mall/exchange',
    method: 'post',
    data: { productId }
  })
}
