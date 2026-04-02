import request from '@/utils/request';
import type { Order, OrderDetail, PageResult, AfterSales } from '@/types/api';

export const createOrder = (data: {
  orderType?: number;
  productId?: number;
  skuId?: number;
  quantity?: number;
  addressId?: number;
  couponId?: number;
  pointsUsed?: number;
  paymentMethod?: string;
  remark?: string;
}) => {
  return request<{ orderId: number; orderNo: string }>({
    url: '/api/orders/create',
    method: 'post',
    data
  });
};

export const createVipOrder = (data: { vipType: number; paymentMethod: string }) => {
  return request<{ orderId: number; orderNo: string; payAmount: number }>({
    url: '/api/orders/vip',
    method: 'post',
    data
  });
};

export const createPointsExchangeOrder = (data: { productId: number; quantity?: number }) => {
  return request<{ orderId: number; orderNo: string; pointsAmount: number }>({
    url: '/api/orders/points-exchange',
    method: 'post',
    data
  });
};

export const getUserOrders = (params: { status?: number; page?: number; size?: number }) => {
  return request<PageResult<OrderDetail>>({
    url: '/api/orders',
    method: 'get',
    params
  });
};

export const getOrderDetail = (orderId: number) => {
  return request<OrderDetail>({
    url: `/api/orders/${orderId}`,
    method: 'get'
  });
};

export const payOrder = (orderId: number, data: { paymentMethod: string }) => {
  return request<{ orderId: number; status: number }>({
    url: `/api/orders/${orderId}/pay`,
    method: 'post',
    data
  });
};

export const cancelOrder = (orderId: number, data?: { reason?: string }) => {
  return request<void>({
    url: `/api/orders/${orderId}/cancel`,
    method: 'post',
    data
  });
};

export const cartCheckout = (data: { addressId: number; couponId?: number; pointsUsed?: number; remark?: string }) => {
  return request<{ orderId: number; orderNo: string }>({
    url: '/api/orders/checkout',
    method: 'post',
    data
  });
};

export const buyNow = (data: { productId: number; skuId?: number; quantity: number; addressId: number; couponId?: number; pointsUsed?: number; remark?: string }) => {
  return request<{ orderId: number; orderNo: string }>({
    url: '/api/orders/buy-now',
    method: 'post',
    data
  });
};

export const getAdminOrderList = (params: { status?: number; page?: number; size?: number; orderNo?: string }) => {
  return request<PageResult<Order>>({
    url: '/api/admin/orders',
    method: 'get',
    params
  });
};

export const shipOrder = (orderNo: string, data: { trackingCompany: string; trackingNo: string }) => {
  return request<void>({
    url: `/api/admin/orders/${orderNo}/ship`,
    method: 'post',
    data
  });
};

export const getAdminAfterSalesList = (params?: { status?: number }) => {
  return request<AfterSales[]>({
    url: '/api/admin/after-sales',
    method: 'get',
    params
  });
};

export const resolveAfterSales = (data: { id: number; status: number; refundAmount?: number }) => {
  return request<void>({
    url: `/api/admin/after-sales/${data.id}/resolve`,
    method: 'post',
    data
  });
};
