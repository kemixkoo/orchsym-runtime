import React from 'react';
import { Router as DefaultRouter, Route, Switch } from 'react-router-dom';
import dynamic from 'umi/dynamic';
import renderRoutes from 'umi/_renderRoutes';
import RendererWrapper0 from 'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/.umi/LocaleWrapper.jsx'
import _dvaDynamic from 'dva/dynamic'

let Router = require('dva/router').routerRedux.ConnectedRouter;

let routes = [
  {
    "path": "/canvas",
    "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "layouts__CanvasLayout" */'../../layouts/CanvasLayout'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
    "Routes": [require('../Authorized').default],
    "routes": [
      {
        "path": "/canvas/:processGroupId",
        "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__Canvas" */'../Canvas'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "name": "画布",
        "exact": true
      },
      {
        "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__404" */'../404'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "component": () => React.createElement(require('E:/bitbucket/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js').default, { pagesPath: 'src/pages', hasRoutesInConfig: true })
      }
    ]
  },
  {
    "path": "/user",
    "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "layouts__UserLayout" */'../../layouts/UserLayout'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
    "routes": [
      {
        "path": "/user",
        "redirect": "/user/login",
        "exact": true
      },
      {
        "path": "/user/login",
        "name": "login",
        "component": _dvaDynamic({
  app: window.g_app,
models: () => [
  import(/* webpackChunkName: 'p__User__models__register.js' */'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/User/models/register.js').then(m => { return { namespace: 'register',...m.default}})
],
  component: () => import(/* webpackChunkName: "p__User__Login" */'../User/Login'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "path": "/user/register",
        "name": "register",
        "component": _dvaDynamic({
  app: window.g_app,
models: () => [
  import(/* webpackChunkName: 'p__User__models__register.js' */'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/User/models/register.js').then(m => { return { namespace: 'register',...m.default}})
],
  component: () => import(/* webpackChunkName: "p__User__Register" */'../User/Register'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "path": "/user/register-result",
        "name": "register.result",
        "component": _dvaDynamic({
  app: window.g_app,
models: () => [
  import(/* webpackChunkName: 'p__User__models__register.js' */'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/User/models/register.js').then(m => { return { namespace: 'register',...m.default}})
],
  component: () => import(/* webpackChunkName: "p__User__RegisterResult" */'../User/RegisterResult'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__404" */'../404'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "component": () => React.createElement(require('E:/bitbucket/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js').default, { pagesPath: 'src/pages', hasRoutesInConfig: true })
      }
    ]
  },
  {
    "path": "/blank",
    "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "layouts__BlankLayout" */'../../layouts/BlankLayout'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
    "routes": [
      {
        "path": "/blank",
        "name": "index",
        "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__Blank__Index" */'../Blank/Index'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "component": () => React.createElement(require('E:/bitbucket/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js').default, { pagesPath: 'src/pages', hasRoutesInConfig: true })
      }
    ]
  },
  {
    "path": "/",
    "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "layouts__BasicLayout" */'../../layouts/BasicLayout'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
    "Routes": [require('../Authorized').default],
    "routes": [
      {
        "path": "/",
        "redirect": "/runtime",
        "exact": true
      },
      {
        "name": "application",
        "icon": "OS-iconyingyongguanli",
        "path": "/runtime",
        "component": _dvaDynamic({
  app: window.g_app,
models: () => [
  import(/* webpackChunkName: 'p__Application__model.js' */'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/Application/model.js').then(m => { return { namespace: 'model',...m.default}})
],
  component: () => import(/* webpackChunkName: "p__Application" */'../Application'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "name": "template",
        "icon": "OS-iconmoban",
        "path": "/template",
        "routes": [
          {
            "name": "local",
            "path": "/template/local",
            "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__Template__Local" */'../Template/Local'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
            "exact": true
          },
          {
            "name": "remote",
            "path": "/template/remote",
            "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__Template__Remote" */'../Template/Remote'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
            "exact": true
          },
          {
            "component": () => React.createElement(require('E:/bitbucket/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js').default, { pagesPath: 'src/pages', hasRoutesInConfig: true })
          }
        ]
      },
      {
        "name": "orchestrations",
        "path": "/orchestrations",
        "icon": "share-alt",
        "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__Orchestrations__Index" */'../Orchestrations/Index'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "name": "result",
        "icon": "check-circle-o",
        "path": "/result",
        "routes": [
          {
            "path": "/result/success",
            "name": "success",
            "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__Result__Success" */'../Result/Success'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
            "exact": true
          },
          {
            "path": "/result/fail",
            "name": "fail",
            "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__Result__Error" */'../Result/Error'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
            "exact": true
          },
          {
            "component": () => React.createElement(require('E:/bitbucket/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js').default, { pagesPath: 'src/pages', hasRoutesInConfig: true })
          }
        ]
      },
      {
        "name": "exception",
        "icon": "warning",
        "path": "/exception",
        "hideInMenu": true,
        "routes": [
          {
            "path": "/exception/403",
            "name": "not-permission",
            "component": _dvaDynamic({
  app: window.g_app,
models: () => [
  import(/* webpackChunkName: 'p__Exception__models__error.js' */'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/Exception/models/error.js').then(m => { return { namespace: 'error',...m.default}})
],
  component: () => import(/* webpackChunkName: "p__Exception__403" */'../Exception/403'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
            "exact": true
          },
          {
            "path": "/exception/404",
            "name": "not-find",
            "component": _dvaDynamic({
  app: window.g_app,
models: () => [
  import(/* webpackChunkName: 'p__Exception__models__error.js' */'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/Exception/models/error.js').then(m => { return { namespace: 'error',...m.default}})
],
  component: () => import(/* webpackChunkName: "p__Exception__404" */'../Exception/404'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
            "exact": true
          },
          {
            "path": "/exception/500",
            "name": "server-error",
            "component": _dvaDynamic({
  app: window.g_app,
models: () => [
  import(/* webpackChunkName: 'p__Exception__models__error.js' */'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/Exception/models/error.js').then(m => { return { namespace: 'error',...m.default}})
],
  component: () => import(/* webpackChunkName: "p__Exception__500" */'../Exception/500'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
            "exact": true
          },
          {
            "path": "/exception/trigger",
            "name": "trigger",
            "hideInMenu": true,
            "component": _dvaDynamic({
  app: window.g_app,
models: () => [
  import(/* webpackChunkName: 'p__Exception__models__error.js' */'E:/bitbucket/nifi/orchsym/orchsym-fe/src/pages/Exception/models/error.js').then(m => { return { namespace: 'error',...m.default}})
],
  component: () => import(/* webpackChunkName: "p__Exception__TriggerException" */'../Exception/TriggerException'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
            "exact": true
          },
          {
            "component": () => React.createElement(require('E:/bitbucket/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js').default, { pagesPath: 'src/pages', hasRoutesInConfig: true })
          }
        ]
      },
      {
        "component": _dvaDynamic({
  
  component: () => import(/* webpackChunkName: "p__404" */'../404'),
  LoadingComponent: require('E:/bitbucket/nifi/orchsym/orchsym-fe/src/components/PageLoading/index').default,
}),
        "exact": true
      },
      {
        "component": () => React.createElement(require('E:/bitbucket/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js').default, { pagesPath: 'src/pages', hasRoutesInConfig: true })
      }
    ]
  },
  {
    "component": () => React.createElement(require('E:/bitbucket/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js').default, { pagesPath: 'src/pages', hasRoutesInConfig: true })
  }
];
window.g_routes = routes;
window.g_plugins.applyForEach('patchRoutes', { initialValue: routes });

// route change handler
function routeChangeHandler(location, action) {
  window.g_plugins.applyForEach('onRouteChange', {
    initialValue: {
      routes,
      location,
      action,
    },
  });
}
window.g_history.listen(routeChangeHandler);
routeChangeHandler(window.g_history.location);

export default function RouterWrapper() {
  return (
<RendererWrapper0>
          <Router history={window.g_history}>
      { renderRoutes(routes, {}) }
    </Router>
        </RendererWrapper0>
  );
}
