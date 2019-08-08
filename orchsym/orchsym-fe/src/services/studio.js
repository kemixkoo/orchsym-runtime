// import { stringify } from 'qs';
import request from '@/utils/request';

export async function queryApplication() {
  return request('/studio/nifi-api/flow/process-groups/root');
}

export async function fakeAccountLogin(params) {
  return request('/studio/nifi-api/access/token', {
    method: 'POST',
    requestType: 'form',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      Accept: 'text/plain',
    },
    data: params,
  });
}
// 复制 运行 创建模版前所需接口
export async function createSnippets(params) {
  return request('/studio/nifi-api/snippets', {
    method: 'PUT',
    body: params,
  });
}
