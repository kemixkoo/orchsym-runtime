import request from '@/utils/request';

export async function downloadApplication(appId) {
  return request(`/studio/orchsym-api/template/app/${appId}/download`, { getResponse: true })
    .then(({ data, response }) => {
      console.log(response)
      const fileName = response.headers['Content-Disposition'].split('filename=')[1]
      console.log(fileName)
      localStorage.setItem('fileName', fileName);
    });
}
