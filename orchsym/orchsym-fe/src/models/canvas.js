import { detailApplication } from '@/services/ProcessGroups';

export default {
  namespace: 'canvas',

  state: {
    appDetails: {},
  },

  effects: {
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
