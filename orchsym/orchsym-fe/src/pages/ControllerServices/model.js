// import { message } from 'antd';
import { queryControllerServices } from '@/services/controllerServices';

export default {
  namespace: 'controllerServices',

  state: {
    controllerServicesList: [],
  },

  effects: {
    *fetchControllerServices({ payload, cb }, { call, put }) {
      console.log(payload)
      const response = yield call(queryControllerServices, payload);
      yield put({
        type: 'appendValue',
        payload: {
          controllerServicesList: response,
        },
      });
      yield cb && cb()
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
