import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getProfile } from '@/api/user';
import type { UserInfo } from '@/types/api';

export const useUserStore = defineStore('user', () => {
  const token = ref(sessionStorage.getItem('token') || '');
  const userInfo = ref<UserInfo | null>(null);
  const isAdmin = ref(sessionStorage.getItem('isAdmin') === 'true');
  const isGuest = ref(sessionStorage.getItem('isGuest') === 'true');

  const setToken = (newToken: string) => {
    token.value = newToken;
    userInfo.value = null;
    isAdmin.value = false;
    sessionStorage.setItem('token', newToken);
    sessionStorage.removeItem('isAdmin');
    sessionStorage.removeItem('isGuest');
    isGuest.value = false;
  };

  const setIsAdmin = (value: boolean) => {
    isAdmin.value = value;
    sessionStorage.setItem('isAdmin', String(value));
  };

  const setIsGuest = (value: boolean) => {
    isGuest.value = value;
    if (value) {
      sessionStorage.setItem('isGuest', 'true');
    } else {
      sessionStorage.removeItem('isGuest');
    }
  };

  const clearToken = () => {
    token.value = '';
    userInfo.value = null;
    isAdmin.value = false;
    isGuest.value = false;
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('isAdmin');
    sessionStorage.removeItem('isGuest');
  };

  const fetchUserInfo = async () => {
    if (!token.value) {
      throw new Error('No token');
    }
    
    try {
      const res = await getProfile();
      userInfo.value = res;
      isAdmin.value = res.role === 'ADMIN';
      sessionStorage.setItem('isAdmin', String(isAdmin.value));
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
