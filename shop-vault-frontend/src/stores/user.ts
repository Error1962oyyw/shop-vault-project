import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getProfile } from '@/api/user';
import { secureStorage } from '@/utils/secureStorage';
import type { UserInfo } from '@/types/api';

export const useUserStore = defineStore('user', () => {
  const token = ref(secureStorage.getItem('token') || '');
  const userInfo = ref<UserInfo | null>(null);
  const isAdmin = ref(false);
  const isGuest = ref(false);

  const setToken = (newToken: string, refreshTokenValue?: string) => {
    token.value = newToken;
    userInfo.value = null;
    isAdmin.value = false;
    secureStorage.setItem('token', newToken);
    if (refreshTokenValue) {
      secureStorage.setItem('refreshToken', refreshTokenValue);
    }
    secureStorage.removeItem('isGuest');
    isGuest.value = false;
  };

  const setIsAdmin = (value: boolean) => {
    isAdmin.value = value;
  };

  const setIsGuest = (value: boolean) => {
    isGuest.value = value;
    if (value) {
      secureStorage.setItem('isGuest', 'true');
    } else {
      secureStorage.removeItem('isGuest');
    }
  };

  const clearToken = () => {
    token.value = '';
    userInfo.value = null;
    isAdmin.value = false;
    isGuest.value = false;
    secureStorage.removeItem('token');
    secureStorage.removeItem('refreshToken');
    secureStorage.removeItem('isGuest');
  };

  const fetchUserInfo = async () => {
    if (!token.value) {
      throw new Error('No token');
    }

    try {
      const res = await getProfile();
      userInfo.value = res;
      isAdmin.value = res.role === 'ADMIN';

      return res;
    } catch (error) {
      userInfo.value = null;
      throw error;
    }
  };

  const updateUserInfo = (updates: Partial<UserInfo>) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...updates };
    }
  };

  const enterGuestMode = () => {
    clearToken();
    setIsGuest(true);
  };

  const isLoggedIn = () => {
    return !!token.value;
  };

  return {
    token,
    userInfo,
    isAdmin,
    isGuest,
    setToken,
    setIsAdmin,
    setIsGuest,
    clearToken,
    fetchUserInfo,
    updateUserInfo,
    enterGuestMode,
    isLoggedIn
  };
});
