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
  return request(`/studio/nifi-api/controller-services/${id}`, {
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

// 删除
export async function queryDeleteServices(serviceId) {
  return request(`/studio/nifi-api/service/${serviceId}/logic_delete`, {
    method: 'DELETE',
  })
}

export async function queryMDeleteServices(serviceIds) {
  return request('/studio/orchsym-pro-api/batch/services/logic-delete', {
    method: 'DELETE',
    data: serviceIds,
  })
}

// 移动 复制
export async function queryCopeServices(body) {
  return request(`/studio/nifi-api/service/${body.id}/copy`, {
    method: 'POST',
    data: body.values,
  })
}

export async function queryMoveServices(body) {
  return request(`/studio/nifi-api/service/${body.id}/move`, {
    method: 'PUT',
    data: body.values,
  })
}

export async function queryMCopeServices(body) {
  return request('/studio/orchsym-pro-api/batch/services/copy', {
    method: 'POST',
    data: body,
  })
}
export async function queryMMoveeServices(body) {
  return request('/studio/orchsym-pro-api/batch/services/move', {
    method: 'PUT',
    data: body,
  })
}

// 新建
export async function queryServiceTypes() {
  return request('/studio/nifi-api/flow/controller-service-types')
}
export async function queryAddServices(obj) {
  return request(`/studio/nifi-api/process-groups/${obj.groupId}/controller-services`, {
    method: 'POST',
    data: obj.body,
  })
}

// 配置
export async function querySingleService(serviceId) {
  return request(`/studio/nifi-api/controller-services/${serviceId}`)
}
export async function queryUpdateServiceConfig(obj) {
  return request(`/studio/nifi-api/controller-services/${obj.serviceId}`, {
    method: 'PUT',
    data: obj.body,
  })
}
