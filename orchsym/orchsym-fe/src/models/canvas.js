import { detailApplication } from '@/services/ProcessGroups';
import { querySearchApplication } from '@/services/application';

export default {
  namespace: 'canvas',

  state: {
    applicationList: [],
    appDetails: {},
  },

  effects: {
    *fetchApplication({ payload, cb }, { call, put }) {
      const response = yield call(querySearchApplication, payload);
      yield put({
        type: 'appendValue',
        payload: {
          applicationList: response,
        },
      });
      yield cb && cb(response)
    },
    *fetchDetailApplication({ payload }, { call, put }) {
      const response = yield call(detailApplication, payload);
      yield put({
        type: 'appendValue',
        payload: {
          appDetails: response,
        },
      });
    },
  },

  reducers: {
    appendValue(state, action) {
      return {
        ...state,
        ...action.payload,
      }
    },
  },
};
