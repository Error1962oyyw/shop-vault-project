import request from '@/utils/request';
import type { Order, PageResult, AfterSales, ApplyAfterSalesParams } from '@/types/api';

export const getOrderList = (params?: { status?: number }) => {
  return request<Order[]>({
    url: '/api/order/list',
    method: 'get',
    params
  });
};

export const getOrderDetail = (orderNo: string) => {
  return request<Order>({
    url: `/api/order/detail/${orderNo}`,
    method: 'get'
  });
};

export const buyNow = (data: { productId: number; quantity: number; userCouponId?: number }) => {
  return request<string>({
    url: '/api/order/buy-now',
    method: 'post',
    data
  });
};

export const cartCheckout = (data: { cartItemIds: number[]; addressId: number; remark?: string; userCouponId?: number }) => {
  return request<string>({
    url: '/api/order/cart-checkout',
    method: 'post',
    data
  });
};

export const payOrder = (orderNo: string) => {
  return request<void>({
    url: `/api/order/pay/${orderNo}`,
    method: 'post'
  });
};

export const shipOrder = (orderNo: string) => {
  return request<void>({
    url: `/api/order/ship/${orderNo}`,
    method: 'post'
  });
};

export const receiveOrder = (orderNo: string) => {
  return request<void>({
    url: `/api/order/receive/${orderNo}`,
    method: 'post'
  });
};

export const extendOrder = (orderNo: string) => {
  return request<void>({
    url: `/api/order/extend/${orderNo}`,
    method: 'post'
  });
};

export const cancelOrder = (orderNo: string) => {
  return request<void>({
    url: `/api/order/cancel/${orderNo}`,
    method: 'post'
  });
};

export const applyAfterSales = (data: ApplyAfterSalesParams) => {
  return request<void>({
    url: '/api/after-sales/apply',
    method: 'post',
    data
  });
};

export const getMyAfterSalesList = () => {
  return request<AfterSales[]>({
    url: '/api/after-sales/my-list',
    method: 'get'
  });
};

export const resolveAfterSales = (data: { id: number; status: number; refundAmount?: number; remark?: string }) => {
  return request<void>({
    url: '/api/after-sales/resolve',
    method: 'post',
    data
  });
};

export const getAdminAfterSalesList = () => {
  return request<AfterSales[]>({
    url: '/api/after-sales/admin-list',
    method: 'get'
  });
};

export const getAdminOrderList = (params: { status?: number; pageNum: number; pageSize: number }) => {
  return request<PageResult<Order>>({
    url: '/api/order/admin/list',
    method: 'get',
    params
  });
};
