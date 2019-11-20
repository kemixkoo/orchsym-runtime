import request from '@/utils/request';
// 验证 是否可运行
export async function validationRunApp(id) {
  return request(`/studio/nifi-api/validation/snippet/${id}`);
}

// 验证 是否可删除
// export async function validationDeleteApp(id) {
//   return request(`/studio/nifi-api/stats/delete/${id}`);
// }
export async function validationDeleteApp(id) {
  return request(`/studio/nifi-api/application/${id}/verify_delete_status`);
}

// 验证 应用名称是否重名
export async function validationAppCheckName(param) {
  return request('/studio/nifi-api/application/check_name', {
    params: {
      name: param.name,
      appId: param.appId,
    },
  });
}
// 验证是否可下载
export async function validationDownApp() {
  return request('/studio/orchsym-api/helper/state');
}
