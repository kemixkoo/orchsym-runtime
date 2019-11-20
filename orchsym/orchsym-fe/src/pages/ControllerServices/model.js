// import { message } from 'antd';
import { queryControllerServices } from '@/services/Flow';

export default {
  namespace: 'controllerServices',

  state: {
    controllerServicesList: [],
  },

  effects: {
    *fetchControllerServices({ payload }, { call, put }) {
      const response = yield call(queryControllerServices);
      console.log(response)
      yield put({
        type: 'appendValue',
        payload: {
          controllerServicesList: response.controllerServices,
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
