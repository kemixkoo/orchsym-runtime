import { stringify } from 'qs';
import request from '@/utils/request';

export async function queryProjectNotice() {
  return request('/api/project/notice');
}

export async function fakeAccountLogin(params) {
  return request('/api/login/account', {
    method: 'POST',
    data: params,
  });
}

export async function fakeRegister(params) {
  return request('/api/register', {
    method: 'POST',
    data: params,
  });
}

export async function queryNotices(params = {}) {
  return request(`/api/notices?${stringify(params)}`);
}

// 获取验证码
export async function getFakeCaptcha(mobile) {
  return request(`/api/captcha?mobile=${mobile}`);
}

// 框架自带
export async function query() {
  return request('/api/users');
}

export async function queryCurrent() {
  return request('/api/currentUser');
}

export default async function queryError(code) {
  return request(`/api/${code}`);
}
