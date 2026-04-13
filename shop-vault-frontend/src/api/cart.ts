import request from '@/utils/request';
import type { CartItem } from '@/types/api';

export const getCartList = () => {
  return request<CartItem[]>({
    url: '/api/cart/list',
    method: 'get'
  });
};

export const addToCart = (data: { productId: number; quantity: number; skuId?: number }) => {
  return request<void>({
    url: '/api/cart/add',
    method: 'post',
    data
  });
};

export const updateCartQuantity = (id: number, quantity: number) => {
  return request<void>({
    url: `/api/cart/update-quantity/${id}`,
    method: 'put',
    params: { quantity }
  });
};

export const deleteCartItems = (ids: number[]) => {
  return request<void>({
    url: '/api/cart/delete',
    method: 'delete',
    data: ids
  });
};
