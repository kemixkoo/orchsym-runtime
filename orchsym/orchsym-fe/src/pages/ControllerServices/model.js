// import { message } from 'antd';
import { queryControllerServices } from '@/services/controllerServices';

export default {
  namespace: 'controllerServices',

  state: {
    controllerServicesList: [],
  },

  effects: {
    *fetchControllerServices({ payload }, { call, put }) {
      const response = yield call(queryControllerServices, payload);
      console.log(response)
      yield put({
        type: 'appendValue',
        payload: {
          controllerServicesList: response,
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
