import request from '@/utils/request';

export async function queryControllerServices(params) {
  return request('/studio/nifi-api/service/search-results', {
    // method: 'POST',
    params: {
      text: params.text,
      page: params.page,
      pageSize: params.pageSize,
      sortedField: params.filterTimeField, // NAME(按照服务名排序)/TYPE(根据服务类型排序)/REFERENCING_COMPONENTS(根据引用该服务的组件的数量排序)/NONE(不排序)
      isDesc: params.isDesc,
      deleted: params.deleted,
    },
  })
}
// 控制器服务 旧版
// export async function queryControllerServices() {
//   return request('/studio/nifi-api/flow/controller/controller-services');
// }
