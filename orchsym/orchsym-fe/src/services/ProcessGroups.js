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

// 删除
export async function deleteApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}?version=${params.version}?clientId=${params.clientId}`, {
    method: 'DELETE',
  })
}

// 复制粘贴
export async function copeApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}/snippet-instance`, {
    method: 'PUT',
    body: params.body,
  });
}

// 创建模版
export async function CreateApplicationTemp(params) {
  return request(`/studio/nifi-api/process-groups/${params.id}/templates`, {
    method: 'POST',
    body: params.body,
  });
}
