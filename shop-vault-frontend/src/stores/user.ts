import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getProfile } from '@/api/user';
import type { UserInfo } from '@/types/api';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const userInfo = ref<UserInfo | null>(null);
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true');
  const isGuest = ref(localStorage.getItem('isGuest') === 'true');

  const setToken = (newToken: string) => {
    token.value = newToken;
    localStorage.setItem('token', newToken);
    isGuest.value = false;
    localStorage.removeItem('isGuest');
  };

  const setIsAdmin = (value: boolean) => {
    isAdmin.value = value;
    localStorage.setItem('isAdmin', String(value));
  };

  const setIsGuest = (value: boolean) => {
    isGuest.value = value;
    if (value) {
      localStorage.setItem('isGuest', 'true');
    } else {
      localStorage.removeItem('isGuest');
    }
  };

  const clearToken = () => {
    token.value = '';
    userInfo.value = null;
    isAdmin.value = false;
    isGuest.value = false;
    localStorage.removeItem('token');
    localStorage.removeItem('isAdmin');
    localStorage.removeItem('isGuest');
  };

  const fetchUserInfo = async () => {
    if (!token.value) {
      throw new Error('No token');
    }
    
    try {
      const res = await getProfile();
      userInfo.value = res;
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
