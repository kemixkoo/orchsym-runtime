// import { stringify } from 'qs';
import request from '@/utils/request';

export async function queryApplication() {
  return request('/studio/nifi-api/flow/process-groups/root');
}
export async function updateAppState(params) {
  return request(`/studio/nifi-api/flow/process-groups/${params.id}`, {
    method: 'PUT',
    data: params, // state: RUNNING, STOPPED, ENABLED, DISABLED
  });
}
// 获取 client-id
export async function queryClientId() {
  return request('/studio/nifi-api/flow/client-id');
}
export async function queryCurrentUser() {
  return request('/studio/nifi-api/flow/current-user');
}
