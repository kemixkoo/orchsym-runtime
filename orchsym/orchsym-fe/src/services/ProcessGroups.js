import request from '@/utils/request';

export async function addApplication({ values, appId }) {
  const params = { values, position: { x: 0, y: 0 } }
  return request(`/studio/nifi-api/process-groups/${appId}/process-groups`, {
    method: 'POST',
    data: params,
  });
}

export async function detailApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params}`);
}

export async function editApplication({ values, appId }) {
  return request(`/studio/nifi-api/process-groups/${appId}`, {
    method: 'PUT',
    data: values,
  });
}

// 删除
export async function deleteApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}?version=${params.version}?clientId=${params.clientId}`, {
    method: 'DELETE',
  })
}

// 复制粘贴
export async function copeApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}/snippet-instance`, {
    method: 'POST',
    data: params.body,
  });
}

// 创建模版
export async function createApplicationTemp(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}/templates`, {
    method: 'POST',
    data: params.body,
  });
}
