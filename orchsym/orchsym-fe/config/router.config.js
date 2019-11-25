/**
 * @按照 umi 的路由实现，需要在此处配置页面的路由
 * @其中的 component 值是一个字符串，是相对于 src/pages/ 内部的路径
 * @authority 用来配置这个路由的权限
 * @配置中的 name 和菜单实际展示的不同，这是因为配置了全球化组件的原因。这里是国际化配置的 key，真正展示的菜单名可以在 /src/locales/zh-CN.js 里进行配置。
 * @hideInMenu 可以在菜单中不展示这个路由，包括子路由
 */
export default [
  // 用户登录、注册等，使用自带的 UserLayout 布局
  {
    path: '/canvas',
    component: '../layouts/CanvasLayout',
    Routes: ['src/pages/Authorized'],
    routes: [
      {
        path: '/canvas/:processGroupId',
        component: './Canvas',
        name: '画布',
      },
      {
        component: '404',
      },
    ],
  }, // 用户登录、注册等，使用自带的 UserLayout 布局
  {
    path: '/user',
    component: '../layouts/UserLayout',
    routes: [
      { path: '/user', redirect: '/user/login' },
      { path: '/user/login', name: 'login', component: './User/Login' },
      { path: '/user/register', name: 'register', component: './User/Register' },
      {
        path: '/user/register-result',
        name: 'register.result',
        component: './User/RegisterResult',
      },
      {
        component: '404',
      },
    ],
  },
  // 空白页做登录逻辑处理
  {
    path: '/blank',
    component: '../layouts/BlankLayout',
    routes: [
      {
        path: '/blank',
        name: 'index',
        component: './Blank/Index',
      },
    ],
  },
  // web app，使用自带的 BasicLayout 布局
  {
    path: '/',
    // 通用布局
    component: '../layouts/BasicLayout',
    Routes: ['src/pages/Authorized'],
    routes: [
      { path: '/', redirect: '/runtime' },
      // 应用
      {
        name: 'application',
        icon: 'OS-iconyingyongguanli',
        path: '/runtime',
        component: './Application',
      },
      // 模板
      {
        name: 'template',
        icon: 'OS-iconmoban',
        path: '/temp/:tab',
        component: './Template',
      },
      // 控制器服务
      {
        name: 'controllerServices',
        icon: 'OS-iconkongzhiqi',
        path: '/ControllerServices',
        component: './ControllerServices',
      },
      // 标签管理
      {
        name: 'tag',
        icon: 'OS-iconbiaoqian',
        path: '/tag',
        component: './Tag',
      },
      // 回收站
      {
        name: 'recycleBin',
        icon: 'OS-iconhuishouzhan',
        path: '/recycleBin',
        component: './RecycleBin',
      },
      // // 自带的结果页面
      // {
      //   name: 'result',
      //   icon: 'check-circle-o',
      //   path: '/result',
      //   // hideInMenu: true,
      //   routes: [
      //     // result
      //     {
      //       path: '/result/success',
      //       name: 'success',
      //       component: './Result/Success',
      //     },
      //     { path: '/result/fail', name: 'fail', component: './Result/Error' },
      //   ],
      // },
      // 401 403 404 500 等异常页面
      {
        name: 'exception',
        icon: 'warning',
        path: '/exception',
        hideInMenu: true,
        routes: [
          // exception
          {
            path: '/exception/403',
            name: 'not-permission',
            component: './Exception/403',
          },
          {
            path: '/exception/404',
            name: 'not-find',
            component: './Exception/404',
          },
          {
            path: '/exception/500',
            name: 'server-error',
            component: './Exception/500',
          },
          {
            path: '/exception/trigger',
            name: 'trigger',
            hideInMenu: true,
            component: './Exception/TriggerException',
          },
        ],
      },
      {
        component: '404',
      },

    ],
  },
];
