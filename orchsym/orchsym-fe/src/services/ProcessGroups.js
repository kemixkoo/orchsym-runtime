import request from '@/utils/request';

// 创建
export async function addApplication(params) {
  return request('/studio/nifi-api/process-groups/root/process-groups', {
    method: 'POST',
    data: params.value,
  });
}

// 详情
export async function detailApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params}`);
}

// 编辑
export async function editApplication(params) {
  const { value: { component: { id } } } = params;
  return request(`/studio/nifi-api/process-groups/${id}`, {
    method: 'PUT',
    data: params.value,
  });
}

// 删除
// export async function deleteApplication(param) {
//   return request(`/studio/nifi-api/process-groups/${param.id}`, {
//     method: 'DELETE',
//     params: {
//       version: param.version,
//       clientId: param.clientId,
//     },
//   })
// }

// 复制粘贴
// export async function copeApplication(params) {
//   return request(`/studio/nifi-api/process-groups/${params.id}/snippet-instance`, {
//     method: 'POST',
//     data: params.body,
//   });
// }

// 存为模版
export async function createApplicationTemp(params, errorHandler) {
  return request(`/studio/nifi-api/process-groups/${params.id}/templates`, {
    errorHandler,
    method: 'POST',
    data: params.body,
  });
}
