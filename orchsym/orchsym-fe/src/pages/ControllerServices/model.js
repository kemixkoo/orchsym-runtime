// import { message } from 'antd';
import {
  queryControllerServices,
  queryDetailServices, queryUpdateServices,
  queryMEnableServices, queryMDisableServices, queryStateServices,
  queryDeleteServices, queryMDeleteServices,
  queryCopeServices, queryMoveServices, queryMCopeServices, queryMMoveeServices,
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
    // 删除
    *fetchDeleteServices({ payload, cb }, { call, put }) {
      if (payload.type === 'multiple') {
        yield call(queryMDeleteServices, payload.id);
      } else {
        yield call(queryDeleteServices, payload.serviceIds);
      }
      yield cb && cb();
    },
    // 复制移动
    *fetchCopeServices({ payload, cb }, { call, put }) {
      if (payload.id) {
        if (payload.state === 'COPE') {
          yield call(queryCopeServices, payload);
        } else {
          yield call(queryMoveServices, payload);
        }
      } else if (payload.state === 'COPE') {
        yield call(queryMCopeServices, payload.values);
      } else {
        yield call(queryMMoveeServices, payload.values);
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
