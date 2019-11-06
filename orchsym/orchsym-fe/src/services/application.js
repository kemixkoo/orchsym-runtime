// import { stringify } from 'qs';
import request from '@/utils/request';

export async function querySearchApplication(param) {
  return request('/studio/nifi-api/application/app/search-results', {
    method: 'GET',
    params: {
      q: param.q,
      sortedField: param.sortedField,
      isDesc: param.isDesc,
      page: param.page,
      pageSize: param.pageSize,
    },
  });
}
