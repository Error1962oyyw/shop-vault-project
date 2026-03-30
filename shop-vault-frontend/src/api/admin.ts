import request from '@/utils/request';
import type { 
  CouponTemplate, 
  CouponTemplateParams, 
  ProductSku, 
  SkuCreateParams,
  Spec,
  SpecCreateParams,
  YoloMapping,
  PageResult,
  AfterSales,
  AfterSalesHandleParams,
  ReturnLogisticsParams
} from '@/types/api';

export const getAdminCoupons = (params: { pageNum: number; pageSize: number }) => {
  return request<PageResult<CouponTemplate>>({
    url: '/api/admin/coupons',
    method: 'get',
    params
  });
};

export const createCoupon = (data: CouponTemplateParams) => {
  return request<CouponTemplate>({
    url: '/api/admin/coupons',
    method: 'post',
    data
  });
};

export const updateCoupon = (id: number, data: CouponTemplateParams) => {
  return request<CouponTemplate>({
    url: `/api/admin/coupons/${id}`,
    method: 'put',
    data
  });
};

export const deleteCoupon = (id: number) => {
  return request<void>({
    url: `/api/admin/coupons/${id}`,
    method: 'delete'
  });
};

export const updateCouponStatus = (id: number, status: number) => {
  return request<void>({
    url: `/api/admin/coupons/${id}/status`,
    method: 'put',
    params: { status }
  });
};

export const expireCoupons = () => {
  return request<void>({
    url: '/api/admin/coupons/expire',
    method: 'post'
  });
};

export const getProductSkus = (productId: number) => {
  return request<ProductSku[]>({
    url: `/api/admin/skus/product/${productId}`,
    method: 'get'
  });
};

export const getSkuById = (id: number) => {
  return request<ProductSku>({
    url: `/api/admin/skus/${id}`,
    method: 'get'
  });
};

export const getSkuByCode = (skuCode: string) => {
  return request<ProductSku>({
    url: `/api/admin/skus/code/${skuCode}`,
    method: 'get'
  });
};

export const createSku = (data: SkuCreateParams) => {
  return request<ProductSku>({
    url: '/api/admin/skus',
    method: 'post',
    data
  });
};

export const batchCreateSkus = (data: { productId: number; skus: SkuCreateParams[] }) => {
  return request<ProductSku[]>({
    url: '/api/admin/skus/batch',
    method: 'post',
    data
  });
};

export const updateSku = (id: number, data: SkuCreateParams) => {
  return request<ProductSku>({
    url: `/api/admin/skus/${id}`,
    method: 'put',
    data
  });
};

export const deleteSku = (id: number) => {
  return request<void>({
    url: `/api/admin/skus/${id}`,
    method: 'delete'
  });
};

export const deleteProductSkus = (productId: number) => {
  return request<void>({
    url: `/api/admin/skus/product/${productId}`,
    method: 'delete'
  });
};

export const deductSkuStock = (id: number, quantity: number) => {
  return request<boolean>({
    url: `/api/admin/skus/${id}/stock/deduct`,
    method: 'post',
    params: { quantity }
  });
};

export const addSkuStock = (id: number, quantity: number) => {
  return request<boolean>({
    url: `/api/admin/skus/${id}/stock/add`,
    method: 'post',
    params: { quantity }
  });
};

export const getSpecs = () => {
  return request<Spec[]>({
    url: '/api/admin/specs',
    method: 'get'
  });
};

export const getSpecById = (id: number) => {
  return request<Spec>({
    url: `/api/admin/specs/${id}`,
    method: 'get'
  });
};

export const createSpec = (data: SpecCreateParams) => {
  return request<Spec>({
    url: '/api/admin/specs',
    method: 'post',
    data
  });
};

export const updateSpec = (id: number, data: SpecCreateParams) => {
  return request<Spec>({
    url: `/api/admin/specs/${id}`,
    method: 'put',
    data
  });
};

export const deleteSpec = (id: number) => {
  return request<void>({
    url: `/api/admin/specs/${id}`,
    method: 'delete'
  });
};

export const getSpecValues = (specId: number) => {
  return request<{ id: number; specId: number; value: string; sort: number }[]>({
    url: `/api/admin/specs/${specId}/values`,
    method: 'get'
  });
};

export const addSpecValue = (specId: number, data: { value: string; sort?: number }) => {
  return request<{ id: number; specId: number; value: string; sort: number }>({
    url: `/api/admin/specs/${specId}/values`,
    method: 'post',
    data
  });
};

export const deleteSpecValue = (valueId: number) => {
  return request<void>({
    url: `/api/admin/specs/values/${valueId}`,
    method: 'delete'
  });
};

export const getYoloMappings = () => {
  return request<YoloMapping[]>({
    url: '/api/yolo-mapping/list',
    method: 'get'
  });
};

export const getActiveYoloMappings = () => {
  return request<YoloMapping[]>({
    url: '/api/yolo-mapping/active',
    method: 'get'
  });
};

export const getAvailableYoloLabels = () => {
  return request<string[]>({
    url: '/api/yolo-mapping/available-labels',
    method: 'get'
  });
};

export const addYoloMapping = (data: { yoloLabel: string; categoryId: number }) => {
  return request<string>({
    url: '/api/yolo-mapping/add',
    method: 'post',
    data
  });
};

export const updateYoloMapping = (data: { id: number; yoloLabel: string; categoryId: number }) => {
  return request<string>({
    url: '/api/yolo-mapping/update',
    method: 'put',
    data
  });
};

export const deleteYoloMapping = (id: number) => {
  return request<string>({
    url: `/api/yolo-mapping/${id}`,
    method: 'delete'
  });
};

export const toggleYoloMapping = (id: number) => {
  return request<string>({
    url: `/api/yolo-mapping/toggle/${id}`,
    method: 'put'
  });
};

export const handleAfterSales = (data: AfterSalesHandleParams) => {
  return request<string>({
    url: '/api/after-sales/handle',
    method: 'post',
    data
  });
};

export const confirmReturn = (params: { orderNo: string; isAgree: boolean; remark?: string }) => {
  return request<string>({
    url: '/api/after-sales/confirm-return',
    method: 'post',
    params
  });
};

export const cancelAfterSales = (orderNo: string) => {
  return request<string>({
    url: '/api/after-sales/cancel',
    method: 'post',
    params: { orderNo }
  });
};

export const fillReturnLogistics = (data: ReturnLogisticsParams) => {
  return request<string>({
    url: '/api/after-sales/return-logistics',
    method: 'post',
    data
  });
};

export const getAfterSalesDetail = (orderNo: string) => {
  return request<AfterSales>({
    url: `/api/after-sales/detail/${orderNo}`,
    method: 'get'
  });
};

export const getUserList = (params: { pageNum: number; pageSize: number; keyword?: string }) => {
  return request<PageResult<any>>({
    url: '/api/admin/users',
    method: 'get',
    params
  });
};

export const updateUserStatus = (userId: number, status: number) => {
  return request<string>({
    url: `/api/admin/users/${userId}/status`,
    method: 'put',
    params: { status }
  });
};

export interface Activity {
  id: number;
  name: string;
  startTime: string;
  endTime: string;
  type: number;
  activityType: number;
  discountRate: number;
  pointCost: number | null;
  productId: number | null;
  status: number;
  ruleExpression: string | null;
  pointsMultiplier: number | null;
  description: string | null;
  createTime: string | null;
  couponType: number;
  couponThreshold: number;
  couponAmount: number;
  couponTotal: number;
  couponReceived: number | null;
}

export interface PointsRule {
  id: number;
  ruleCode: string;
  ruleName: string;
  description: string | null;
  pointsValue: number | null;
  pointsRatio: number | null;
  ruleType: number;
  dailyLimit: number | null;
  isActive: boolean;
  sortOrder: number | null;
  createTime: string | null;
  updateTime: string | null;
}

export const getActivities = (params: { pageNum: number; pageSize: number; type?: number; status?: number }) => {
  return request<PageResult<Activity>>({
    url: '/api/admin/activities',
    method: 'get',
    params
  });
};

export const getActivityById = (id: number) => {
  return request<Activity>({
    url: `/api/admin/activities/${id}`,
    method: 'get'
  });
};

export const createActivity = (data: Partial<Activity>) => {
  return request<Activity>({
    url: '/api/admin/activities',
    method: 'post',
    data
  });
};

export const updateActivity = (id: number, data: Partial<Activity>) => {
  return request<Activity>({
    url: `/api/admin/activities/${id}`,
    method: 'put',
    data
  });
};

export const deleteActivity = (id: number) => {
  return request<void>({
    url: `/api/admin/activities/${id}`,
    method: 'delete'
  });
};

export const updateActivityStatus = (id: number, status: number) => {
  return request<string>({
    url: `/api/admin/activities/${id}/status`,
    method: 'put',
    params: { status }
  });
};

export const getMemberDayActivities = () => {
  return request<Activity[]>({
    url: '/api/admin/activities/member-days',
    method: 'get'
  });
};

export const getPointsRules = () => {
  return request<PointsRule[]>({
    url: '/api/admin/points-rules',
    method: 'get'
  });
};

export const getPointsRuleById = (id: number) => {
  return request<PointsRule>({
    url: `/api/admin/points-rules/${id}`,
    method: 'get'
  });
};

export const createPointsRule = (data: Partial<PointsRule>) => {
  return request<PointsRule>({
    url: '/api/admin/points-rules',
    method: 'post',
    data
  });
};

export const updatePointsRule = (id: number, data: Partial<PointsRule>) => {
  return request<PointsRule>({
    url: `/api/admin/points-rules/${id}`,
    method: 'put',
    data
  });
};

export const deletePointsRule = (id: number) => {
  return request<void>({
    url: `/api/admin/points-rules/${id}`,
    method: 'delete'
  });
};

export const togglePointsRuleStatus = (id: number) => {
  return request<string>({
    url: `/api/admin/points-rules/${id}/toggle`,
    method: 'put'
  });
};

export const updatePointsRatio = (id: number, ratio: number) => {
  return request<string>({
    url: `/api/admin/points-rules/${id}/ratio`,
    method: 'put',
    params: { ratio }
  });
};

export interface VipUser {
  id: number;
  userId: number;
  vipLevel: number;
  discountRate: number;
  vipExpireTime: string | null;
  totalVipDays: number;
  createTime: string;
  updateTime: string;
  username?: string;
  nickname?: string;
  avatar?: string;
}

export const getVipUsers = (params: { pageNum: number; pageSize: number; vipLevel?: number }) => {
  return request<PageResult<VipUser>>({
    url: '/api/admin/vip/users',
    method: 'get',
    params
  });
};

export const getVipUserInfo = (userId: number) => {
  return request<VipUser>({
    url: `/api/admin/vip/users/${userId}`,
    method: 'get'
  });
};

export const extendVip = (userId: number, days: number) => {
  return request<string>({
    url: `/api/admin/vip/users/${userId}/extend`,
    method: 'put',
    params: { days }
  });
};

export const updateVipLevel = (userId: number, level: number, days?: number | null) => {
  const params: Record<string, any> = { level }
  if (days !== null && days !== undefined) {
    params.days = days
  }
  return request<string>({
    url: `/api/admin/vip/users/${userId}/level`,
    method: 'put',
    params
  });
};

export interface AdminMessage {
  id: number;
  type: string;
  title: string;
  content: string;
  targetRole: string;
  status: number;
  sendTime: string;
  readTime: string | null;
  linkUrl: string | null;
  relatedId: number | null;
  createTime: string;
}

export const getAdminMessages = () => {
  return request<AdminMessage[]>({
    url: '/api/admin/messages',
    method: 'get'
  });
};

export const getAdminUnreadCount = () => {
  return request<{ count: number }>({
    url: '/api/admin/messages/unread-count',
    method: 'get'
  });
};

export const markMessageAsRead = (id: number) => {
  return request<string>({
    url: `/api/admin/messages/read/${id}`,
    method: 'post'
  });
};

export const markAllMessagesAsRead = () => {
  return request<string>({
    url: '/api/admin/messages/read-all',
    method: 'post'
  });
};
