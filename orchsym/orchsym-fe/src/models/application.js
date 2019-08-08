import { createSnippets } from '@/services/studio';
import { queryApplication, updateAppState } from '@/services/Flow';
import { validationRunApp, validationDeleteApp } from '@/services/validation';
import { detailApplication, editApplication, addApplication,
  deleteApplication, copeApplication, createApplicationTemp } from '@/services/ProcessGroups';
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
      yield put({
        type: 'appendValue',
        payload: {
          applicationList: response.processGroupFlow.flow.processGroups,
        },
      });
    },
    *detailApplication({ payload }, { call, put }) {
      const response = yield call(detailApplication({ payload }));
      yield put({
        type: 'appendValue',
        payload: {
          details: response,
        },
      });
    },
    *editApplication({ payload }, { call, put }) {
      const response = yield call(editApplication({ payload }));
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
    *addApplication({ payload }, { call, put }) {
      const response = yield call(addApplication({ payload }));
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

    *createSnippets({ payload }, { call, put }) {
      const response = yield call(createSnippets, payload);
      if (response) {
        // message.success('创建应用成功！');
      }
      // yield put({
      //   type: 'appendValue',
      //   payload: {
      //     details: response,
      //   },
      // });
    },
    *updateAppState({ payload }, { call, put }) {
      const response = yield call(updateAppState, payload);
      if (response) {
        // message.success('成功！');
      }
      // yield put({
      //   type: 'appendValue',
      //   payload: {
      //     details: response,
      //   },
      // });
    },
    *validationRunApp({ payload }, { call, put }) {
      const response = yield call(validationRunApp, payload);
      if (response) {
        // message.success('创建应用成功！');
      }
      // yield put({
      //   type: 'appendValue',
      //   payload: {
      //     details: response,
      //   },
      // });
    },
    *validationDeleteApp({ payload }, { call, put }) {
      const response = yield call(validationDeleteApp, payload);
      if (response) {
        // message.success('创建应用成功！');
      }
      // yield put({
      //   type: 'appendValue',
      //   payload: {
      //     details: response,
      //   },
      // });
    },
    *deleteApplication({ payload }, { call, put }) {
      const response = yield call(deleteApplication, payload);
      if (response) {
        // message.success('创建应用成功！');
      }
      // yield put({
      //   type: 'appendValue',
      //   payload: {
      //     details: response,
      //   },
      // });
    },
    *copeApplication({ payload }, { call, put }) {
      const response = yield call(copeApplication, payload);
      if (response) {
        // message.success('创建应用成功！');
      }
      // yield put({
      //   type: 'appendValue',
      //   payload: {
      //     details: response,
      //   },
      // });
    },
    *createApplicationTemp({ payload }, { call, put }) {
      const response = yield call(createApplicationTemp, payload);
      if (response) {
        // message.success('创建应用成功！');
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
