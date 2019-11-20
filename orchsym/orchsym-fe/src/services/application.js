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
