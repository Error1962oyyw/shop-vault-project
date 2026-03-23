import request from '@/utils/request';
import type { ChatMessage } from '@/types/api';

export interface ChatUser {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
}

export const sendMessage = (data: { content: string }) => {
  return request<void>({
    url: '/api/chat/send',
    method: 'post',
    data
  });
};

export const sendImageMessage = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request<string>({
    url: '/api/chat/send-image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const getChatHistory = () => {
  return request<ChatMessage[]>({
    url: '/api/chat/history',
    method: 'get'
  });
};

export const adminReply = (data: { receiverId: number; content: string }) => {
  return request<void>({
    url: '/api/chat/admin/reply',
    method: 'post',
    data
  });
};

export const adminReplyImage = (userId: number, file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('userId', userId.toString());
  return request<string>({
    url: '/api/chat/admin/reply-image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const getAdminChatHistory = (userId: number) => {
  return request<ChatMessage[]>({
    url: `/api/chat/admin/history/${userId}`,
    method: 'get'
  });
};

export const getChatUsers = () => {
  return request<ChatUser[]>({
    url: '/api/chat/admin/users',
    method: 'get'
  });
};
