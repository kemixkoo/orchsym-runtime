// import { message } from 'antd';
import { queryTemplates } from '@/services/Flow';

export default {
  namespace: 'template',

  state: {
    collectList: [],
  },

  effects: {
    *fetchTemplates({ payload }, { call, put }) {
      const response = yield call(queryTemplates);
      console.log(response)
      yield put({
        type: 'appendValue',
        payload: {
          collectList: response.templates,
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
