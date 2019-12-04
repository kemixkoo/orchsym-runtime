import request from '@/utils/request';

export async function queryControllerServices(params) {
  return request('/studio/nifi-api/service/search', {
    method: 'POST',
    data: {
      text: params.text,
      page: params.page,
      pageSize: params.pageSize,
      sortedField: params.sortedField, // NAME(按照服务名排序)/TYPE(根据服务类型排序)/REFERENCING_COMPONENTS(根据引用该服务的组件的数量排序)/NONE(不排序)
      desc: params.desc,
      states: params.states,
      scopes: params.scopes,
    },
  })
}
// 控制器服务 旧版
// export async function queryControllerServices() {
//   return request('/studio/nifi-api/flow/controller/controller-services');
// }

// 详情
export async function queryDetailServices(serviceId) {
  return request(`/studio/nifi-api/controller-services/${serviceId}`)
}

// 重命名
export async function queryUpdateServices(body) {
  const { component: { id } } = body;
  return request(`/studio/nifi-api/controller-services/${id}`, {
    method: 'PUT',
    data: body,
  })
}

// 启用 禁止
export async function queryStateServices(body) {
  const { component: { id } } = body;
  return request(`/studio//nifi-api/controller-services/${id}`, {
    method: 'PUT',
    data: body,
  })
}

export async function queryMEnableServices(serviceIds) {
  return request('/studio/orchsym-pro-api/batch/services/enable', {
    method: 'PUT',
    data: serviceIds,
  })
}

export async function queryMDisableServices(serviceIds) {
  return request('/studio/orchsym-pro-api/batch/services/disable', {
    method: 'PUT',
    data: serviceIds,
  })
}
