import request from '@/utils/request';

export async function downloadApplication(appId) {
  return request(`/studio/orchsym-api/template/app/${appId}/download`)
}
export async function queryCollectTemplates(param) {
  return request('/studio/orchsym-pro-api/favorites/templates/search', {
    // method: 'POST',
    params: {
      text: param.text,
      sortedField: param.sortedField, // name, createdTime, modifiedTime, uploadedTime;
      isDesc: param.isDesc,
      page: param.page,
      pageSize: param.pageSize,
      filterTimeField: param.filterTimeField,
      beginTime: param.beginTime,
      endTime: param.endTime,
      tags: param.tags,
      deleted: param.deleted,
      templateType: param.templateType,
    },
  })
}
export async function queryOfficialTemplates(param) {
  return request('/studio/orchsym-api/template/official/search', {
    // method: 'POST',
    params: {
      text: param.text,
      sortedField: param.sortedField, // name, createdTime, modifiedTime, uploadedTime;
      isDesc: param.isDesc,
      page: param.page,
      pageSize: param.pageSize,
      filterTimeField: param.filterTimeField,
      beginTime: param.beginTime,
      endTime: param.endTime,
      tags: param.tags,
    },
  })
}
export async function queryCustomTemplates(param) {
  return request('/studio/nifi-api/orchsym-template/custom/search', {
    // method: 'POST',
    params: {
      text: param.text,
      sortedField: param.sortedField, // name, createdTime, modifiedTime, uploadedTime;
      isDesc: param.isDesc,
      page: param.page,
      pageSize: param.pageSize,
      filterTimeField: param.filterTimeField,
      beginTime: param.beginTime,
      endTime: param.endTime,
      tags: param.tags,
      deleted: param.deleted,
      templateType: param.templateType,
    },
  })
}
// 获得模板列表 旧版
// export async function queryTemplates() {
//   return request('/studio/nifi-api/flow/templates');
// }

// 单个下载 先获得token再下载
export async function queryDownloadToken() {
  return request('/studio/nifi-api/access/download-token', {
    method: 'POST',
  })
}

export async function queryDownloadTemplate(param) {
  return request(`/studio/nifi-api/templates/${param.templateId}/download?access_token=${param.downloadToken}`)
}
// 批量下载
export async function queryDownloadMTemplates(param) {
  return request('/studio/orchsym-pro-api/batch/templates/download', {
    // method: 'POST',
    params: {
      templateIds: param.templateIds,
    },
  })
}
// 编辑
export async function editTemplate(body) {
  return request('/studio/nifi-api/orchsym-template/edit', {
    method: 'PUT',
    data: body,
  });
}
// 上传
export async function uploadTemplate(params) {
  console.log(params)
  const { file } = params;
  const formData = new FormData();
  formData.append('template', file[0]);
  return request('/studio/nifi-api/process-groups/root/templates/upload', {
    method: 'POST',
    data: formData,
  });
}
// 收藏
export async function collectTemplate(templateId) {
  return request(`/studio/orchsym-pro-api/favorites/templates/${templateId}`, {
    method: 'POST',
  });
}
export async function cancelCollectTemplate(templateId) {
  return request(`/studio/orchsym-pro-api/favorites/templates/${templateId}`, {
    method: 'DELETE',
  });
}

// 删除
export async function deleteTemplate(param) {
  return request(`/studio/nifi-api/orchsym-template/${param.templateId}/logic_delete`, {
    method: 'DELETE',
  })
}
// 批量下载
export async function deletedMTemplates(param) {
  return request('/studio/orchsym-pro-api/batch/templates/download', {
    method: 'DELETE',
    params: {
      templateIds: param.templateIds,
    },
  })
}
