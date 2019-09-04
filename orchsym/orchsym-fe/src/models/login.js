import { routerRedux } from 'dva/router';
import pathToRegexp from 'path-to-regexp'
import { stringify } from 'qs';
import { fakeAccountLogin, accessOidc, licenseWarn } from '@/services/studio';
import { queryClientId } from '@/services/Flow';
import { setToken, setClientId, getToken } from '@/utils/authority';
import { getPageQuery } from '@/utils/utils';
import { reloadAuthorized } from '@/utils/Authorized';

export default {

  state: {
    token: '',
    clientId: '',
  },

  effects: {
    *login({ payload }, { call, put }) {
      const response = yield call(fakeAccountLogin, payload);
      yield put({
        type: 'changeLoginStatus',
        payload: response,
      });
      if (response) {
        reloadAuthorized();
        const urlParams = new URL(window.location.href);
        const params = getPageQuery();
        let { redirect } = params;
        if (redirect) {
          const redirectUrlParams = new URL(redirect);
          if (redirectUrlParams.origin === urlParams.origin) {
            redirect = redirect.substr(urlParams.origin.length);
            if (redirect.match(/^\/.*#/)) {
              redirect = redirect.substr(redirect.indexOf('#') + 1);
            }
          } else {
            redirect = null;
          }
        }
        yield put(routerRedux.replace(redirect || '/'));
      }
    },

    *fetchGetClientId(_, { call, put }) {
      const response = yield call(queryClientId);
      if (response) {
        yield put({
          type: 'addClientId',
          payload: response,
        });
      }
    },
    *fetchAccessOidc(_, { call, put }) {
      const response = yield call(accessOidc);
      if (response) {
        yield put({
          type: 'changeLoginStatus',
          payload: response,
        });
        yield put(
          routerRedux.replace({
            pathname: '/',
          })
        );
      }
    },
    *fetchLicenseWarn(_, { call, put }) {
      const response = yield call(licenseWarn);
      if (response) {
        console.log(response)
      }
    },
    *checkSSOLoginStatus({ payload }, { select, put }) {
      if (!getToken()) {
        yield put(routerRedux.push('/blank'))
      }
    },
    *logout(_, { put }) {
      yield put({
        type: 'changeLoginStatus',
        payload: {
          status: false,
          currentAuthority: 'guest',
        },
      });
      reloadAuthorized();
      // redirect
      if (window.location.pathname !== '/user/login') {
        yield put(
          routerRedux.replace({
            pathname: '/user/login',
            search: stringify({
              redirect: window.location.href,
            }),
          })
        );
      }
    },
  },

  reducers: {
    changeLoginStatus(state, { payload }) {
      setToken(payload);
      return {
        ...state,
        token: payload,
      };
    },
    addClientId(state, { payload }) {
      setClientId(payload)
      return {
        ...state,
        clientId: payload,
      };
    },
  },
  subscriptions: {
    setup({ dispatch, history }) {
      return history.listen(({ pathname }) => {
        const match = pathToRegexp('/blank').exec(pathname)
        if (!match) {
          dispatch({
            type: 'checkSSOLoginStatus',
            payload: {
              pathname,
            },
          })
        }
      })
    },
  },
};
