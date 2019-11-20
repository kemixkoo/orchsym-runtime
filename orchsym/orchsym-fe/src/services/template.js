import request from '@/utils/request';

export async function downloadApplication(appId) {
  return request(`/studio/orchsym-api/template/app/${appId}/download`, { getResponse: true });
}
