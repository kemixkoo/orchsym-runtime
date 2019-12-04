// import { message } from 'antd';
import {
  queryControllerServices,
  queryDetailServices, queryUpdateServices,
  queryMEnableServices, queryMDisableServices, queryStateServices,
} from '@/services/controllerServices';

export default {
  namespace: 'controllerServices',

  state: {
    controllerServicesList: [],
    detailServices: {},
  },

  effects: {
    *fetchControllerServices({ payload, cb }, { call, put }) {
      const response = yield call(queryControllerServices, payload);
      yield put({
        type: 'appendValue',
        payload: {
          controllerServicesList: response,
        },
      });
      yield cb && cb()
    },

    // 重命名  配置
    *fetchDetailServices({ payload, cb }, { call, put }) {
      const response = yield call(queryDetailServices, payload);
      yield put({
        type: 'appendValue',
        payload: {
          detailServices: response,
        },
      });
      yield cb && cb(response)
    },

    *fetchUpdateServices({ payload, cb }, { call, put }) {
      console.log(payload)
      yield call(queryUpdateServices, payload);
      yield cb && cb()
    },

    // 起停
    *fetchStateUpdateServices({ payload, cb }, { call, put }) {
      if (payload.type === 'multiple') {
        if (payload.state === 'enable') {
          yield call(queryMEnableServices, payload.serviceIds);
        } else {
          yield call(queryMDisableServices, payload.serviceIds);
        }
      } else {
        yield call(queryStateServices, payload.value);
      }
      yield cb && cb();
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
