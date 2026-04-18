import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useUserStore } from '@/stores/user';
import { refreshToken as refreshTokenApi } from '@/api/auth';
import { secureStorage } from '@/utils/secureStorage';

interface ApiError {
  response?: {
    data?: {
      msg?: string;
      message?: string;
      code?: number;
    };
    status?: number;
  };
  message?: string;
}

interface ApiResponse<T = unknown> {
  code: number;
  msg?: string;
  message?: string;
  data: T;
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
});

let isRedirecting = false;
let isRefreshing = false;
let refreshSubscribers: Array<(token: string) => void> = [];

function onRefreshed(token: string) {
  refreshSubscribers.forEach(cb => cb(token));
  refreshSubscribers = [];
}

function addRefreshSubscriber(cb: (token: string) => void) {
  refreshSubscribers.push(cb);
}

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = secureStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, msg, message, data } = response.data;
    const errorMsg = message || msg;

    if (code === 0 || code === 200) {
      return data as AxiosResponse['data'];
    } else {
      const error: ApiError = {
        response: {
          data: { msg: errorMsg, code }
        },
        message: errorMsg || '请求失败'
      };
      return Promise.reject(error);
    }
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const apiError: ApiError = {
      message: error.message
    };

    if (error.response) {
      const status = error.response.status;
      const data = error.response.data as { msg?: string; message?: string } | undefined;
      const msg = data?.message || data?.msg;

      if (status === 401 && !originalRequest._retry && !isAuthEndpoint(originalRequest.url || '')) {
        const storedRefreshToken = secureStorage.getItem('refreshToken');
        if (storedRefreshToken && !isRefreshing) {
          originalRequest._retry = true;
          isRefreshing = true;
          try {
            const result = await refreshTokenApi({ refreshToken: storedRefreshToken });
            const newAccessToken = result.accessToken;
            const newRefreshToken = result.refreshToken;
            secureStorage.setItem('token', newAccessToken);
            secureStorage.setItem('refreshToken', newRefreshToken);
            onRefreshed(newAccessToken);
            isRefreshing = false;
            originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
            return service(originalRequest);
          } catch (refreshError) {
            isRefreshing = false;
            refreshSubscribers = [];
            handleUnauthorized(originalRequest.url, '登录已过期，请重新登录');
            return Promise.reject(refreshError);
          }
        } else if (isRefreshing) {
          return new Promise((resolve) => {
            addRefreshSubscriber((token: string) => {
              originalRequest.headers['Authorization'] = `Bearer ${token}`;
              resolve(service(originalRequest));
            });
          });
        }
        handleUnauthorized(originalRequest.url, msg || '');
        return Promise.reject(apiError);
      }

      switch (status) {
        case 403:
          apiError.response = { data: { msg: msg || '权限不足' } };
          apiError.message = msg || '权限不足';
          break;
        case 404:
          apiError.response = { data: { msg: msg || '请求的资源不存在' } };
          apiError.message = msg || '请求的资源不存在';
          break;
        case 423:
          apiError.response = { data: { msg: msg || '账户已锁定' } };
          apiError.message = msg || '账户已锁定';
          break;
        case 500:
          apiError.response = { data: { msg: msg || '服务器内部错误' } };
          apiError.message = msg || '服务器内部错误';
          break;
        case 502:
        case 503:
        case 504:
          apiError.response = { data: { msg: '服务暂时不可用，请稍后重试' } };
          apiError.message = '服务暂时不可用，请稍后重试';
          break;
        default:
          apiError.response = { data: { msg: msg || `请求失败 (${status})` } };
          apiError.message = msg || `请求失败 (${status})`;
      }
    } else if (error.code === 'ECONNABORTED') {
      apiError.response = { data: { msg: '请求超时，请检查网络连接' } };
      apiError.message = '请求超时，请检查网络连接';
    } else if (!navigator.onLine) {
      apiError.response = { data: { msg: '网络连接已断开' } };
      apiError.message = '网络连接已断开';
    } else {
      apiError.response = { data: { msg: '无法连接到服务器' } };
      apiError.message = '无法连接到服务器';
    }

    return Promise.reject(apiError);
  }
);

function isAuthEndpoint(url: string): boolean {
  return url.includes('/api/auth/login') || url.includes('/api/auth/admin/login') || url.includes('/api/auth/refresh');
}

function handleUnauthorized(url?: string, errorMsg?: string) {
  if (isAuthEndpoint(url || '')) {
    return;
  }

  if (errorMsg && (errorMsg.includes('暂停') || errorMsg.includes('禁用') || errorMsg.includes('Disabled') || errorMsg.includes('suspended'))) {
    return;
  }

  if (!isRedirecting) {
    isRedirecting = true;
    const userStore = useUserStore();
    userStore.clearToken();
    userStore.setIsGuest(true);

    ElMessage.warning('请先登录');

    const currentPath = router.currentRoute.value.fullPath;
    const isLoginPage = router.currentRoute.value.path === '/login' ||
                        router.currentRoute.value.path === '/admin/login';

    if (!isLoginPage) {
      router.push({
        path: '/login',
        query: { redirect: currentPath }
      }).finally(() => {
        isRedirecting = false;
      });
    } else {
      isRedirecting = false;
    }
  }
}

interface RequestConfig {
  url: string;
  method?: 'get' | 'post' | 'put' | 'delete' | 'patch';
  data?: unknown;
  params?: unknown;
  headers?: Record<string, string>;
  timeout?: number;
  signal?: AbortSignal;
}

export default function request<T>(config: RequestConfig): Promise<T> {
  return service(config) as Promise<T>;
}

export { service as axiosInstance };
