import request from '@/utils/request';
import type { UserInfo, Address } from '@/types/api';
import { USER_API, ADDRESS_API } from './constants';

export const getProfile = () => {
  return request<UserInfo>({
    url: USER_API.PROFILE,
    method: 'get'
  });
};

export const updateProfile = (data: Partial<UserInfo>) => {
  return request<string>({
    url: USER_API.PROFILE,
    method: 'put',
    data
  });
};

export interface PasswordUpdateParams {
  email: string;
  code: string;
  newPassword: string;
}

export const updatePassword = (data: PasswordUpdateParams) => {
  return request<string>({
    url: USER_API.PASSWORD,
    method: 'put',
    data
  });
};

export const getAddressList = () => {
  return request<Address[]>({
    url: ADDRESS_API.LIST,
    method: 'get'
  });
};

export const addAddress = (data: Partial<Address>) => {
  return request<string>({
    url: ADDRESS_API.ADD,
    method: 'post',
    data
  });
};

export const updateAddress = (data: Partial<Address>) => {
  return request<string>({
    url: ADDRESS_API.UPDATE,
    method: 'put',
    data
  });
};

export const deleteAddress = (id: number) => {
  return request<string>({
    url: ADDRESS_API.DELETE(id),
    method: 'delete'
  });
};

export const setDefaultAddress = (id: number) => {
  return request<string>({
    url: ADDRESS_API.SET_DEFAULT(id),
    method: 'put'
  });
};

export const uploadAvatar = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request<string>({
    url: '/api/user/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export interface BalanceRecord {
  id: number;
  userId: number;
  amount: number;
  balanceBefore: number;
  balanceAfter: number;
  type: string;
  description: string;
  relatedId: number | null;
  createTime: string;
}

export const rechargeBalance = (amount: number) => {
  return request<{ balance: number; amount: number }>({
    url: '/api/user/balance/recharge',
    method: 'post',
    data: { amount }
  });
};

export const getBalanceRecords = () => {
  return request<BalanceRecord[]>({
    url: '/api/user/balance/records',
    method: 'get'
  });
};
