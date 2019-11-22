// import { message } from 'antd';
import { queryOfficialTemplates } from '@/services/template';

export default {
  namespace: 'template',

  state: {
    collectList: [],
    officialList: [],
  },

  effects: {
    *fetchOfficialTemplates({ payload }, { call, put }) {
      const response = yield call(queryOfficialTemplates, payload);
      yield put({
        type: 'appendValue',
        payload: {
          officialList: response.templates,
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
