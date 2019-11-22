// import { stringify } from 'qs';
import request from '@/utils/request';

export async function querySearchApplication(param) {
  return request('/studio/nifi-api/application/search-results', {
    method: 'GET',
    params: {
      q: param.q,
      sortedField: param.sortedField, // name, createdTime
      isDesc: param.isDesc,
      page: param.page,
      pageSize: param.pageSize,
      isDeleted: param.isDeleted,
      isDetail: param.isDetail,
      isEnabled: param.isEnabled,
      isRunning: param.isRunning,
      hasDataQueue: param.hasDataQueue,
      timeField: param.timeField, // createdTime
      beginTime: param.beginTime,
      endTime: param.endTime,
    },
  });
}

// 启用和禁用
export async function updateAppEnable(appId) {
  return request(`/studio/nifi-api/application/${appId}/enable`, {
    method: 'PUT',
  });
}
export async function updateAppDisable(appId) {
  return request(`/studio/nifi-api/application/${appId}/disable`, {
    method: 'PUT',
  });
}

// 应用状态
export async function updateAppState(params) {
  return request(`/studio/nifi-api/flow/process-groups/${params.id}`, {
    method: 'PUT',
    data: params, // state: RUNNING, STOPPED
  });
}
export async function showAppStatus(appId) {
  return request(`/studio/nifi-api/application/${appId}/status`);
}

// 应用逻辑删除
export async function deleteApplication(appId) {
  return request(`/studio/nifi-api/application/${appId}/logic_delete`, {
    method: 'DELETE',
  })
}

// 复制粘贴
export async function copeApplication(body) {
  return request('/studio/nifi-api/application/copy', {
    method: 'POST',
    data: body,
  });
}

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
