// import { stringify } from 'qs';
import request from '@/utils/request';

export async function queryApplication() {
  return request('/studio/nifi-api/flow/process-groups/root');
}
export async function updateAppState(params) {
  return request(`/studio/nifi-api/flow/process-groups/${params.id}`, {
    method: 'PUT',
    data: params, // state: RUNNING, STOPPED
  });
}
// 获取 client-id
export async function queryClientId() {
  return request('/studio/nifi-api/flow/client-id');
}
export async function queryCurrentUser() {
  return request('/studio/nifi-api/flow/current-user');
}

// 获得模版列表
export async function queryTemplates() {
  return request('/studio/nifi-api/flow/templates');
}
// 控制器服务
export async function queryControllerServices() {
  return request('/studio/nifi-api/flow/controller/controller-services');
}
