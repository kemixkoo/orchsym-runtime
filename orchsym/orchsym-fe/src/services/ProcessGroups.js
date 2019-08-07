import request from '@/utils/request';

export async function addApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}/process-groups`, {
    method: 'POST',
    body: params.body,
  });
}

export async function detailApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}`);
}

export async function editApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}`, {
    method: 'PUT',
    body: params.body,
  });
}
