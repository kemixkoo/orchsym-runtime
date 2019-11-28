// import { queryNotices } from '@/services/api';
import { validationDownApp } from '@/services/validation';
import { setClientId } from '@/utils/authority';
import { queryClientId, licenseWarn, queryBreadcrumb } from '@/services/studio';

export default {
  namespace: 'global',
  state: {
    canDownLoad: '',
    collapsed: false,
    notices: [],
    clientId: '',
    groupsBreadcrumb: [],
    // leftDays: '',
  },

  effects: {
    *fetchValidDownApp(_, { call, put }) {
      const response = yield call(validationDownApp);
      if (response) {
        yield put({
          type: 'appendValue',
          payload: {
            canDownLoad: true,
          },
        });
      } else {
        yield put({
          type: 'appendValue',
          payload: {
            canDownLoad: false,
          },
        });
      }
    },
    *fetchLicenseWarn(_, { call, put }) {
      const response = yield call(licenseWarn);
      if (response) {
        yield put({
          type: 'appendValue',
          payload: {
            leftDays: response.licSummary.leftDays,
          },
        })
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
    *fetchBreadcrumb({ payload }, { call, put }) {
      const response = yield call(queryBreadcrumb, payload);
      if (response) {
        yield put({
          type: 'appendValue',
          payload: {
            groupsBreadcrumb: response.groups.reverse(),
          },
        });
      }
    },

    // *fetchNotices(_, { call, put, select }) {
    //   const data = yield call(queryNotices);
    //   yield put({
    //     type: 'saveNotices',
    //     payload: data,
    //   });
    //   const unreadCount = yield select(
    //     state => state.global.notices.filter(item => !item.read).length
    //   );
    //   yield put({
    //     type: 'user/changeNotifyCount',
    //     payload: {
    //       totalCount: data.length,
    //       unreadCount,
    //     },
    //   });
    // },
    // *clearNotices({ payload }, { put, select }) {
    //   yield put({
    //     type: 'saveClearedNotices',
    //     payload,
    //   });
    //   const count = yield select(state => state.global.notices.length);
    //   const unreadCount = yield select(
    //     state => state.global.notices.filter(item => !item.read).length
    //   );
    //   yield put({
    //     type: 'user/changeNotifyCount',
    //     payload: {
    //       totalCount: count,
    //       unreadCount,
    //     },
    //   });
    // },
    // *changeNoticeReadState({ payload }, { put, select }) {
    //   const notices = yield select(state =>
    //     state.global.notices.map(item => {
    //       const notice = { ...item };
    //       if (notice.id === payload) {
    //         notice.read = true;
    //       }
    //       return notice;
    //     })
    //   );
    //   yield put({
    //     type: 'saveNotices',
    //     payload: notices,
    //   });
    //   yield put({
    //     type: 'user/changeNotifyCount',
    //     payload: {
    //       totalCount: notices.length,
    //       unreadCount: notices.filter(item => !item.read).length,
    //     },
    //   });
    // },
  },

  reducers: {
    appendValue(state, action) {
      return {
        ...state,
        ...action.payload,
      }
    },
    addClientId(state, { payload }) {
      setClientId(payload)
      return {
        ...state,
        clientId: payload,
      };
    },
    changeLayoutCollapsed(state, { payload }) {
      return {
        ...state,
        collapsed: payload,
      };
    },
    // saveNotices(state, { payload }) {
    //   return {
    //     ...state,
    //     notices: payload,
    //   };
    // },
    // saveClearedNotices(state, { payload }) {
    //   return {
    //     ...state,
    //     notices: state.notices.filter(item => item.type !== payload),
    //   };
    // },
  },

  subscriptions: {
    setup({ history }) {
      // Subscribe history(url) change, trigger `load` action if pathname is `/`
      return history.listen(({ pathname, search }) => {
        if (typeof window.ga !== 'undefined') {
          window.ga('send', 'pageview', pathname + search);
        }
      });
    },
  },
};
