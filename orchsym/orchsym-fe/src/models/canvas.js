import { detailApplication } from '@/services/ProcessGroups';
import { queryApplication } from '@/services/Flow';

export default {
  namespace: 'canvas',

  state: {
    applicationList: [],
    parentId: '',
    appDetails: {},
  },

  effects: {
    *fetchApplication({ payload, cb }, { call, put }) {
      const response = yield call(queryApplication);
      yield put({
        type: 'appendValue',
        payload: {
          applicationList: response.processGroupFlow.flow.processGroups,
          parentId: response.processGroupFlow.id,
        },
      });
      yield cb && cb(response.processGroupFlow.flow.processGroups)
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
