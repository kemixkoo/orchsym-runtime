import { routerRedux } from 'dva/router';
import pathToRegexp from 'path-to-regexp'
// import { stringify } from 'qs';
import { fakeAccountLogin, accessOidc, refreshToken } from '@/services/studio';
import { setToken, getToken } from '@/utils/authority';
import { getPageQuery, logout } from '@/utils/utils';
import { reloadAuthorized } from '@/utils/Authorized';

export default {

  state: {
    token: '',
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
    *fetchAccessOidc(_, { call, put }) {
      try {
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
      } catch {
        window.location.href = '/user/login'
      }
    },
    *fetchRefreshToken(payload, { call, put }) {
      const response = yield call(refreshToken);
      if (response) {
        yield put({
          type: 'changeLoginStatus',
          payload: response,
        });
      } else {
        logout();
      }
    },
    *checkSSOLoginStatus({ payload }, { select, put }) {
      if (!getToken()) { //  || !window.document.cookie
        yield put(routerRedux.push('/blank'))
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
