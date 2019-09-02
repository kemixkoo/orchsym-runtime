// https://umijs.org/config/
import os from 'os';
import pageRoutes from './router.config';
import webpackPlugin from './plugin.config';
import defaultSettings from '../src/defaultSettings';
import slash from 'slash2';

const { primaryColor } = defaultSettings;
// preview.pro.ant.design only do not use in your production ; preview.pro.ant.design 专用环境变量，请不要在你的项目中使用它。
const { ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION, TEST } = process.env;

const plugins = [
  [
    'umi-plugin-react',
    {
      antd: true,
      dva: {
        hmr: true,
        immer: true,
      },
      locale: {
        enable: true, // default false
        default: 'zh-CN', // default zh-CN
        baseNavigator: true, // default true, when it is true, will use `navigator.language` overwrite default
      },
      dynamicImport: {
        loadingComponent: './components/PageLoading/index',
        webpackChunkName: true,
        level: 3,
      },
      ...(!TEST && os.platform() === 'darwin'
        ? {
          dll: {
            include: ['dva', 'dva/router', 'dva/saga', 'dva/fetch'],
            exclude: ['@babel/runtime', 'netlify-lambda'],
          },
          hardSource: false,
        }
        : {}),
    },
  ],
];

export default {
  // add for transfer to umi
  plugins,
  devServer: {
    https: true,
  },
  define: {
    ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION:
      ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION || '', // preview.pro.ant.design only do not use in your production ; preview.pro.ant.design 专用环境变量，请不要在你的项目中使用它。
  },
  treeShaking: true,
  targets: {
    ie: 11,
  },
  // 路由配置
  routes: pageRoutes,
  // Theme for antd
  // https://ant.design/docs/react/customize-theme-cn
  theme: {
    'primary-color': primaryColor,
  },
  proxy: {
    '/nifi-api/': {
      target: 'https://172.18.28.230:18443/nifi-api/',
      secure: false,
      changeOrigin: true,
      pathRewrite: { '^/nifi-api': '' },
      cookieDomainRewrite: '',
      autoRewrite: true,
      headers: {
        'X-ProxyScheme': 'https',
        'X-ProxyHost': '127.0.0.1',
        'X-ProxyPort': 9001,
        'X-ProxyContextPath': '/',
      },
    },
    '/studio/': {
      target: 'https://172.18.28.230:18443',
      secure: false,
      changeOrigin: true,
      pathRewrite: { '^/studio': '' },
      cookieDomainRewrite: '',
      headers: {
        'X-ProxyScheme': 'https',
        'X-ProxyHost': '127.0.0.1',
        'X-ProxyPort': 9001,
        'X-ProxyContextPath': '/',
      },
    },
    '/user/login': {
      target: 'https://172.18.28.230:18443/runtime/login',
      secure: false,
      changeOrigin: true,
      pathRewrite: { '^/user/login': '' },
      cookieDomainRewrite: '',
      autoRewrite: true,
      headers: {
        'X-ProxyScheme': 'https',
        'X-ProxyHost': '127.0.0.1',
        'X-ProxyPort': 9001,
        'X-ProxyContextPath': '/',
      },
    },
  },
  ignoreMomentLocale: true,
  lessLoaderOptions: {
    javascriptEnabled: true,
  },
  disableRedirectHoist: true,
  cssLoaderOptions: {
    modules: true,
    getLocalIdent: (context, localIdentName, localName) => {
      if (
        context.resourcePath.includes('node_modules') ||
        context.resourcePath.includes('ant.design.pro.less') ||
        context.resourcePath.includes('global.less')
      ) {
        return localName;
      }
      const match = context.resourcePath.match(/src(.*)/);
      if (match && match[1]) {
        const antdProPath = match[1].replace('.less', '');
        const arr = slash(antdProPath)
          .split('/')
          .map(a => a.replace(/([A-Z])/g, '-$1'))
          .map(a => a.toLowerCase());
        return `antd-pro${arr.join('-')}-${localName}`.replace(/--/g, '-');
      }
      return localName;
    },
  },
  manifest: {
    basePath: '/',
  },
  copy: [{
    from: 'iconfont',
    to: 'iconfont',
  }],
  chainWebpack: webpackPlugin,
};
