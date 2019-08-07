import request from '@/utils/request';

export async function addApplication({ values, appId }) {
  const params = { values, position: { x: 0, y: 0 } }
  return request(`/studio/nifi-api/process-groups/${appId}/process-groups`, {
    method: 'POST',
    body: params,
  });
}

export async function detailApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params}`);
}

export async function editApplication({ values, appId }) {
  return request(`/studio/nifi-api/process-groups/${appId}`, {
    method: 'PUT',
    body: values,
  });
}
