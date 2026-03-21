import request from '@/utils/request';
import type { LoginParams, RegisterParams, TokenResponse, RefreshTokenParams, AdminLoginParams } from '@/types/api';
import { AUTH_API } from './constants';

export const login = (data: LoginParams) => {
  return request<TokenResponse>({
    url: AUTH_API.LOGIN,
    method: 'post',
    data
  });
};

export const adminLogin = (data: AdminLoginParams) => {
  return request<TokenResponse>({
    url: AUTH_API.ADMIN_LOGIN,
    method: 'post',
    data
  });
};

export const refreshToken = (data: RefreshTokenParams) => {
  return request<TokenResponse>({
    url: AUTH_API.REFRESH,
    method: 'post',
    data
  });
};

export const logout = (data: RefreshTokenParams) => {
  return request<void>({
    url: AUTH_API.LOGOUT,
    method: 'post',
    data
  });
};

export const sendCode = (email: string) => {
  return request<void>({
    url: AUTH_API.SEND_CODE,
    method: 'post',
    params: { email }
  });
};

export const register = (data: RegisterParams) => {
  return request<void>({
    url: AUTH_API.REGISTER,
    method: 'post',
    data
  });
};

export const resetPassword = (data: { email: string; code: string; newPassword: string }) => {
  return request<void>({
    url: AUTH_API.RESET_PASSWORD,
    method: 'post',
    data
  });
};
