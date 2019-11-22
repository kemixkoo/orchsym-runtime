import request from '@/utils/request';

export async function downloadApplication(appId) {
  return request(`/studio/orchsym-api/template/app/${appId}/download`)
}

export async function queryOfficialTemplates(param) {
  console.log(param)
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
// 获得模板列表 旧版
// export async function queryTemplates() {
//   return request('/studio/nifi-api/flow/templates');
// }
