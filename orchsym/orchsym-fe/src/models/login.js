import { routerRedux } from 'dva/router';
import { stringify } from 'qs';
import { fakeAccountLogin, licenseWarn } from '@/services/studio';
import { queryClientId } from '@/services/Flow';
import { setToken, setClientId } from '@/utils/authority';
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
    // *fetchAccessKerberos(_, { call, put }) {
    //   const response = yield call(accessKerberos);
    //   if (response) {
    //     console.log(response)
    //   }
    // },
    // *fetchAccessOidc(_, { call, put }) {
    //   try {
    //     const response = yield call(accessOidc);
    //     console.log(response)
    //     // yield call(validationDeleteApp, payload);
    //     // yield cb && cb()
    //   } catch {
    //     window.location.href = 'https://183.129.160.140:8443/runtime/login'
    //     // yield put({
    //     //   type: 'fetchDeleteApplication',
    //     //   payload,
    //     // });
    //   }
    //   // if (response) {
    //   //   console.log(response)
    //   // }
    // },
    *fetchLicenseWarn(_, { call, put }) {
      const response = yield call(licenseWarn);
      if (response) {
        console.log(response)
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
};
