import request from '@/utils/request';
import type { MessagePush, MessagePushParams } from '@/types/api';

export const getMyMessages = () => {
  return request<MessagePush[]>({
    url: '/api/messages/my',
    method: 'get'
  });
};

export const markMessageRead = (id: number) => {
  return request<string>({
    url: `/api/messages/read/${id}`,
    method: 'post'
  });
};

export const pushToUser = (userId: number, params: MessagePushParams) => {
  return request<string>({
    url: `/api/messages/push/user/${userId}`,
    method: 'post',
    params
  });
};

export const pushToRole = (role: string, params: MessagePushParams) => {
  return request<string>({
    url: `/api/messages/push/role/${role}`,
    method: 'post',
    params
  });
};

export const pushToAll = (params: MessagePushParams) => {
  return request<string>({
    url: '/api/messages/push/all',
    method: 'post',
    params
  });
};

export const retryFailedMessages = () => {
  return request<string>({
    url: '/api/messages/retry-failed',
    method: 'post'
  });
};

export const getOnlineCount = () => {
  return request<number>({
    url: '/api/websocket/online-count',
    method: 'get'
  });
};
