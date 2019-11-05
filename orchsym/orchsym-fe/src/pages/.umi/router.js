import React from 'react';
import { Router as DefaultRouter, Route, Switch } from 'react-router-dom';
import dynamic from 'umi/dynamic';
import renderRoutes from 'umi/lib/renderRoutes';
import history from '@tmp/history';
import RendererWrapper0 from '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/.umi/LocaleWrapper.jsx';
import _dvaDynamic from 'dva/dynamic';

const Router = require('dva/router').routerRedux.ConnectedRouter;

const routes = [
  {
    path: '/canvas',
    component: __IS_BROWSER
      ? _dvaDynamic({
          component: () =>
            import(/* webpackChunkName: "layouts__CanvasLayout" */ '../../layouts/CanvasLayout'),
          LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
            .default,
        })
      : require('../../layouts/CanvasLayout').default,
    Routes: [require('../Authorized').default],
    routes: [
      {
        path: '/canvas/:processGroupId',
        component: __IS_BROWSER
          ? _dvaDynamic({
              component: () =>
                import(/* webpackChunkName: "p__Canvas" */ '../Canvas'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../Canvas').default,
        name: '画布',
        exact: true,
      },
      {
        component: __IS_BROWSER
          ? _dvaDynamic({
              component: () =>
                import(/* webpackChunkName: "p__404" */ '../404'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../404').default,
        exact: true,
      },
      {
        component: () =>
          React.createElement(
            require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js')
              .default,
            { pagesPath: 'src/pages', hasRoutesInConfig: true },
          ),
      },
    ],
  },
  {
    path: '/user',
    component: __IS_BROWSER
      ? _dvaDynamic({
          component: () =>
            import(/* webpackChunkName: "layouts__UserLayout" */ '../../layouts/UserLayout'),
          LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
            .default,
        })
      : require('../../layouts/UserLayout').default,
    routes: [
      {
        path: '/user',
        redirect: '/user/login',
        exact: true,
      },
      {
        path: '/user/login',
        name: 'login',
        component: __IS_BROWSER
          ? _dvaDynamic({
              app: require('@tmp/dva').getApp(),
              models: () => [
                import(/* webpackChunkName: 'p__User__models__register.js' */ '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/User/models/register.js').then(
                  m => {
                    return { namespace: 'register', ...m.default };
                  },
                ),
              ],
              component: () =>
                import(/* webpackChunkName: "p__User__Login" */ '../User/Login'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../User/Login').default,
        exact: true,
      },
      {
        path: '/user/register',
        name: 'register',
        component: __IS_BROWSER
          ? _dvaDynamic({
              app: require('@tmp/dva').getApp(),
              models: () => [
                import(/* webpackChunkName: 'p__User__models__register.js' */ '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/User/models/register.js').then(
                  m => {
                    return { namespace: 'register', ...m.default };
                  },
                ),
              ],
              component: () =>
                import(/* webpackChunkName: "p__User__Register" */ '../User/Register'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../User/Register').default,
        exact: true,
      },
      {
        path: '/user/register-result',
        name: 'register.result',
        component: __IS_BROWSER
          ? _dvaDynamic({
              app: require('@tmp/dva').getApp(),
              models: () => [
                import(/* webpackChunkName: 'p__User__models__register.js' */ '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/User/models/register.js').then(
                  m => {
                    return { namespace: 'register', ...m.default };
                  },
                ),
              ],
              component: () =>
                import(/* webpackChunkName: "p__User__RegisterResult" */ '../User/RegisterResult'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../User/RegisterResult').default,
        exact: true,
      },
      {
        component: __IS_BROWSER
          ? _dvaDynamic({
              component: () =>
                import(/* webpackChunkName: "p__404" */ '../404'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../404').default,
        exact: true,
      },
      {
        component: () =>
          React.createElement(
            require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js')
              .default,
            { pagesPath: 'src/pages', hasRoutesInConfig: true },
          ),
      },
    ],
  },
  {
    path: '/blank',
    component: __IS_BROWSER
      ? _dvaDynamic({
          component: () =>
            import(/* webpackChunkName: "layouts__BlankLayout" */ '../../layouts/BlankLayout'),
          LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
            .default,
        })
      : require('../../layouts/BlankLayout').default,
    routes: [
      {
        path: '/blank',
        name: 'index',
        component: __IS_BROWSER
          ? _dvaDynamic({
              component: () =>
                import(/* webpackChunkName: "p__Blank__Index" */ '../Blank/Index'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../Blank/Index').default,
        exact: true,
      },
      {
        component: () =>
          React.createElement(
            require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js')
              .default,
            { pagesPath: 'src/pages', hasRoutesInConfig: true },
          ),
      },
    ],
  },
  {
    path: '/',
    component: __IS_BROWSER
      ? _dvaDynamic({
          component: () =>
            import(/* webpackChunkName: "layouts__BasicLayout" */ '../../layouts/BasicLayout'),
          LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
            .default,
        })
      : require('../../layouts/BasicLayout').default,
    Routes: [require('../Authorized').default],
    routes: [
      {
        path: '/',
        redirect: '/runtime',
        exact: true,
      },
      {
        name: 'application',
        icon: 'OS-iconyingyongguanli',
        path: '/runtime',
        component: __IS_BROWSER
          ? _dvaDynamic({
              app: require('@tmp/dva').getApp(),
              models: () => [
                import(/* webpackChunkName: 'p__Application__model.js' */ '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/Application/model.js').then(
                  m => {
                    return { namespace: 'model', ...m.default };
                  },
                ),
              ],
              component: () =>
                import(/* webpackChunkName: "p__Application" */ '../Application'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../Application').default,
        exact: true,
      },
      {
        name: 'template',
        icon: 'OS-iconmoban',
        path: '/template',
        routes: [
          {
            name: 'local',
            path: '/template/local',
            component: __IS_BROWSER
              ? _dvaDynamic({
                  component: () =>
                    import(/* webpackChunkName: "p__Template__Local" */ '../Template/Local'),
                  LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                    .default,
                })
              : require('../Template/Local').default,
            exact: true,
          },
          {
            name: 'remote',
            path: '/template/remote',
            component: __IS_BROWSER
              ? _dvaDynamic({
                  component: () =>
                    import(/* webpackChunkName: "p__Template__Remote" */ '../Template/Remote'),
                  LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                    .default,
                })
              : require('../Template/Remote').default,
            exact: true,
          },
          {
            component: () =>
              React.createElement(
                require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js')
                  .default,
                { pagesPath: 'src/pages', hasRoutesInConfig: true },
              ),
          },
        ],
      },
      {
        name: 'orchestrations',
        path: '/orchestrations',
        icon: 'share-alt',
        component: __IS_BROWSER
          ? _dvaDynamic({
              component: () =>
                import(/* webpackChunkName: "p__Orchestrations__Index" */ '../Orchestrations/Index'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../Orchestrations/Index').default,
        exact: true,
      },
      {
        name: 'result',
        icon: 'check-circle-o',
        path: '/result',
        routes: [
          {
            path: '/result/success',
            name: 'success',
            component: __IS_BROWSER
              ? _dvaDynamic({
                  component: () =>
                    import(/* webpackChunkName: "p__Result__Success" */ '../Result/Success'),
                  LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                    .default,
                })
              : require('../Result/Success').default,
            exact: true,
          },
          {
            path: '/result/fail',
            name: 'fail',
            component: __IS_BROWSER
              ? _dvaDynamic({
                  component: () =>
                    import(/* webpackChunkName: "p__Result__Error" */ '../Result/Error'),
                  LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                    .default,
                })
              : require('../Result/Error').default,
            exact: true,
          },
          {
            component: () =>
              React.createElement(
                require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js')
                  .default,
                { pagesPath: 'src/pages', hasRoutesInConfig: true },
              ),
          },
        ],
      },
      {
        name: 'exception',
        icon: 'warning',
        path: '/exception',
        hideInMenu: true,
        routes: [
          {
            path: '/exception/403',
            name: 'not-permission',
            component: __IS_BROWSER
              ? _dvaDynamic({
                  app: require('@tmp/dva').getApp(),
                  models: () => [
                    import(/* webpackChunkName: 'p__Exception__models__error.js' */ '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/Exception/models/error.js').then(
                      m => {
                        return { namespace: 'error', ...m.default };
                      },
                    ),
                  ],
                  component: () =>
                    import(/* webpackChunkName: "p__Exception__403" */ '../Exception/403'),
                  LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                    .default,
                })
              : require('../Exception/403').default,
            exact: true,
          },
          {
            path: '/exception/404',
            name: 'not-find',
            component: __IS_BROWSER
              ? _dvaDynamic({
                  app: require('@tmp/dva').getApp(),
                  models: () => [
                    import(/* webpackChunkName: 'p__Exception__models__error.js' */ '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/Exception/models/error.js').then(
                      m => {
                        return { namespace: 'error', ...m.default };
                      },
                    ),
                  ],
                  component: () =>
                    import(/* webpackChunkName: "p__Exception__404" */ '../Exception/404'),
                  LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                    .default,
                })
              : require('../Exception/404').default,
            exact: true,
          },
          {
            path: '/exception/500',
            name: 'server-error',
            component: __IS_BROWSER
              ? _dvaDynamic({
                  app: require('@tmp/dva').getApp(),
                  models: () => [
                    import(/* webpackChunkName: 'p__Exception__models__error.js' */ '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/Exception/models/error.js').then(
                      m => {
                        return { namespace: 'error', ...m.default };
                      },
                    ),
                  ],
                  component: () =>
                    import(/* webpackChunkName: "p__Exception__500" */ '../Exception/500'),
                  LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                    .default,
                })
              : require('../Exception/500').default,
            exact: true,
          },
          {
            path: '/exception/trigger',
            name: 'trigger',
            hideInMenu: true,
            component: __IS_BROWSER
              ? _dvaDynamic({
                  app: require('@tmp/dva').getApp(),
                  models: () => [
                    import(/* webpackChunkName: 'p__Exception__models__error.js' */ '/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/pages/Exception/models/error.js').then(
                      m => {
                        return { namespace: 'error', ...m.default };
                      },
                    ),
                  ],
                  component: () =>
                    import(/* webpackChunkName: "p__Exception__TriggerException" */ '../Exception/TriggerException'),
                  LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                    .default,
                })
              : require('../Exception/TriggerException').default,
            exact: true,
          },
          {
            component: () =>
              React.createElement(
                require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js')
                  .default,
                { pagesPath: 'src/pages', hasRoutesInConfig: true },
              ),
          },
        ],
      },
      {
        component: __IS_BROWSER
          ? _dvaDynamic({
              component: () =>
                import(/* webpackChunkName: "p__404" */ '../404'),
              LoadingComponent: require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/src/components/PageLoading/index')
                .default,
            })
          : require('../404').default,
        exact: true,
      },
      {
        component: () =>
          React.createElement(
            require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js')
              .default,
            { pagesPath: 'src/pages', hasRoutesInConfig: true },
          ),
      },
    ],
  },
  {
    component: () =>
      React.createElement(
        require('/Users/ivy/project/baishan/nifi/orchsym/orchsym-fe/node_modules/umi-build-dev/lib/plugins/404/NotFound.js')
          .default,
        { pagesPath: 'src/pages', hasRoutesInConfig: true },
      ),
  },
];
window.g_routes = routes;
const plugins = require('umi/_runtimePlugin');
plugins.applyForEach('patchRoutes', { initialValue: routes });

export { routes };

export default class RouterWrapper extends React.Component {
  unListen = () => {};

  constructor(props) {
    super(props);

    // route change handler
    function routeChangeHandler(location, action) {
      plugins.applyForEach('onRouteChange', {
        initialValue: {
          routes,
          location,
          action,
        },
      });
    }
    this.unListen = history.listen(routeChangeHandler);
    routeChangeHandler(history.location);
  }

  componentWillUnmount() {
    this.unListen();
  }

  render() {
    const props = this.props || {};
    return (
      <RendererWrapper0>
        <Router history={history}>{renderRoutes(routes, props)}</Router>
      </RendererWrapper0>
    );
  }
}
