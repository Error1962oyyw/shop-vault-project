import request from '@/utils/request';
import type { PointsRecord, CouponTemplate, UserCoupon, MemberDay, Activity } from '@/types/api';

export const signIn = () => {
  return request<{ points: number }>({
    url: '/api/marketing/sign-in',
    method: 'post'
  });
};

export const todaySigned = () => {
  return request<boolean>({
    url: '/api/marketing/points/today-signed',
    method: 'get'
  });
};

export const exchangePoints = (activityId: number) => {
  return request<void>({
    url: `/api/activity/exchange/${activityId}`,
    method: 'post'
  });
};

export const getAvailableCoupons = () => {
  return request<CouponTemplate[]>({
    url: '/api/coupons/available',
    method: 'get'
  });
};

export const getCouponDetail = (id: number) => {
  return request<CouponTemplate>({
    url: `/api/coupons/${id}`,
    method: 'get'
  });
};

export const receiveCoupon = (id: number) => {
  return request<boolean>({
    url: `/api/coupons/${id}/receive`,
    method: 'post'
  });
};

export const getMyCoupons = (status?: number) => {
  return request<UserCoupon[]>({
    url: '/api/coupons/my',
    method: 'get',
    params: { status }
  });
};

export const getMyAvailableCoupons = () => {
  return request<UserCoupon[]>({
    url: '/api/coupons/my/available',
    method: 'get'
  });
};

export const getApplicableCoupons = (params: { orderAmount: number; categoryId?: number; productId?: number }) => {
  return request<UserCoupon[]>({
    url: '/api/coupons/my/applicable',
    method: 'get',
    params
  });
};

export const getBestCoupon = (params: { orderAmount: number; categoryId?: number; productId?: number }) => {
  return request<UserCoupon>({
    url: '/api/coupons/my/best',
    method: 'get',
    params
  });
};

export const useCoupon = (id: number, params: { orderId: number; orderAmount: number }) => {
  return request<boolean>({
    url: `/api/coupons/${id}/use`,
    method: 'post',
    params
  });
};

export const getPointsRecords = () => {
  return request<PointsRecord[]>({
    url: '/api/marketing/points/records',
    method: 'get'
  });
};

export const getCurrentMemberDay = () => {
  return request<MemberDay>({
    url: '/api/member-day/current',
    method: 'get'
  });
};

export const getUpcomingMemberDays = () => {
  return request<Activity[]>({
    url: '/api/member-day/upcoming',
    method: 'get'
  });
};

export const getMemberDayInfo = (id: number) => {
  return request<MemberDay>({
    url: `/api/member-day/info/${id}`,
    method: 'get'
  });
};

export const checkMemberDay = () => {
  return request<boolean>({
    url: '/api/member-day/check',
    method: 'get'
  });
};

export const getAvailableActivities = () => {
  return request<Activity[]>({
    url: '/api/activity/coupons/available',
    method: 'get'
  });
};

export const claimActivityCoupon = (activityId: number) => {
  return request<string>({
    url: `/api/activity/coupons/claim/${activityId}`,
    method: 'post'
  });
};
