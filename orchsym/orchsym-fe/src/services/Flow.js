// import { stringify } from 'qs';
import request from '@/utils/request';

export async function queryApplication() {
  return request('/studio/nifi-api/flow/process-groups/root');
}
export async function updateAppState(params) {
  return request(`/studio/nifi-api/flow/process-groups/${params.id}`, {
    method: 'PUT',
    data: params.body, // state: RUNNING, STOPPED, ENABLED, DISABLED
  });
}
