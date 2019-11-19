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
