import request from '@/utils/request';

// 创建
export async function addApplication({ values, parentId }) {
  const { name } = values;
  const params = {
    component: {
      name,
      position: { x: 0, y: 0 },
    },
    revision: {
      clientId: '2c94334a-31e3-1c01-d76d-f0fabeb6653a',
      version: 0,
    },
  }
  return request(`/studio/nifi-api/process-groups/${parentId}/process-groups`, {
    method: 'POST',
    data: params,
  });
}

// 详情
export async function detailApplication(params) {
  return request(`/studio/nifi-api/process-groups/${params}`);
}

// 编辑
export async function editApplication({ values, appId, revision }) {
  const { name } = values;
  const params = {
    component: {
      id: appId,
      name,
      comments: '',
    },
    revision,
  }
  return request(`/studio/nifi-api/process-groups/${appId}`, {
    method: 'PUT',
    data: params,
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

// 存为模版
export async function createApplicationTemp({ id, snippetId, values }) {
  const body = {
    snippetId,
    description: values.description,
    name: values.name,
  }
  return request(`/studio/nifi-api/process-groups/${id}/templates`, {
    method: 'POST',
    data: body,
  });
}
