/**
 * request 网络请求工具
 * 更详细的api文档: https://bigfish.alipay.com/doc/api#request
 */
import { extend } from 'umi-request';
import { notification } from 'antd';
// import router from 'umi/router';
import { getToken } from '@/utils/authority';
import { logout } from '@/utils/utils';

// const codeMessage = {
//   200: '服务器成功返回请求的数据。',
//   201: '新建或修改数据成功。',
//   202: '一个请求已经进入后台排队（异步任务）。',
//   204: '删除数据成功。',
//   400: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
//   401: '用户没有权限（令牌、用户名、密码错误）。',
//   403: '用户得到授权，但是访问是被禁止的。',
//   404: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
//   406: '请求的格式不可得。',
//   410: '请求的资源被永久删除，且不会再得到的。',
//   422: '当创建一个对象时，发生一个验证错误。',
//   500: '服务器发生错误，请检查服务器。',
//   502: '网关错误。',
//   503: '服务不可用，服务器暂时过载或维护。',
//   504: '网关超时。',
// };

/**
 * 异常处理程序
 */
// const errorHandler = error => {
//   const { response = {} } = error;
//   const errortext = codeMessage[response.status] || response.statusText;
//   const { status, url } = response;
//
//   if (status === 401) {
//     notification.error({
//       message: '未登录或登录已过期，请重新登录。',
//     });
//     // @HACK
//     /* eslint-disable no-underscore-dangle */
//     window.g_app._store.dispatch({
//       type: 'login/logout',
//     });
//     return;
//   }
//   notification.error({
//     message: `请求错误 ${status}: ${url}`,
//     description: errortext,
//   });
//   // environment should not be used
//   console.log('error', response)
//   if (status === 403) {
//     return error;
//   }
//   // if (status <= 504 && status >= 500) {
//   //   router.push('/exception/500');
//   //   return;
//   // }
//   // if (status >= 404 && status < 422) {
//   //   router.push('/exception/404');
//   // }
// };

// const prefix = 'https://orchsym-studio.baishancloud.com/nifi-api';
/**
 * 配置request请求时的默认参数
 */
const request = extend({
  // errorHandler, // 默认错误处理
  credentials: 'include', // 默认请求是否带上cookie
});

request.interceptors.request.use((url, options) => {
  if (getToken()) {
    options.headers.Authorization = `Bearer ${getToken()}`;
  }
  if (url.indexOf('/download') > 0) { // 判断是否是模版下载
    console.log(options.headers)
    console.log(options.headers['content-disposition'])
    // const fileName = options.headers['content-disposition'].split('filename=')[1]
    // localStorage.setItem('fileName', fileName);
  }
  options.headers.Locale = localStorage.getItem('umi_locale') || 'zh-CN';
  return (
    {
      url,
      options: { ...options, interceptors: true },
    }
  );
});
// response拦截器, 处理response
request.interceptors.response.use((response, options) => {
  if (!response.ok) {
    if (response.status === 401) {
      // notification.error({
      //   message: '未登录或登录已过期，请重新登录。',
      // });
      logout();
    } else if (response.status === 403 || response.url.indexOf('/studio/nifi-api/access/oidc/exchange') > 0) { // 判断删除
      return response
    } else {
      response.text().then(data => {
        if (data) {
          notification.error({
            message: data,
          })
        }
      })
      return response
    }
  }
  // console.log('response++', response)
  return response
});
export default request;
