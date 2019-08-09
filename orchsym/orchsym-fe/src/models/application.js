import { queryApplication } from '@/services/studio';
import { detailApplication, editApplication, addApplication } from '@/services/ProcessGroups';
import { message } from 'antd';

export default {
  namespace: 'application',

  state: {
    notice: [],
    details: [],
  },

  effects: {
    *fetchApplication({ payload }, { call, put }) {
      const response = yield call(queryApplication);
      console.log(response);
      yield put({
        type: 'appendValue',
        payload: {
          applicationList: response.processGroupFlow.flow.processGroups,
          parentId: response.processGroupFlow.flow.processGroups[0].component.parentGroupId,
        },
      });
    },
    *fetchDetailApplication({ payload }, { call, put }) {
      const response = yield call(detailApplication, payload);
      console.log(response);
      yield put({
        type: 'appendValue',
        payload: {
          details: response.status,
          revision: response.revision,
        },
      });
    },
    *fetchEditApplication({ payload }, { call, put }) {
      const response = yield call(editApplication, payload);
      if (response) {
        message.success('编辑应用成功！');
      }
      // yield put({
      //   type: 'appendValue',
      //   payload: {
      //     details: response,
      //   },
      // });
    },
    *fetchAddApplication({ payload }, { call, put }) {
      const response = yield call(addApplication, payload);
      if (response) {
        message.success('创建应用成功！');
      }
      // yield put({
      //   type: 'appendValue',
      //   payload: {
      //     details: response,
      //   },
      // });
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
