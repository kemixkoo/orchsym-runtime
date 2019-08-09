import { createSnippets } from '@/services/studio';
import { queryApplication, updateAppState } from '@/services/Flow';
import { validationRunApp, validationDeleteApp } from '@/services/validation';
import {
  detailApplication, editApplication, addApplication,
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
    *fetchDetailApplication({ payload }, { call, put }) {
      const response = yield call(detailApplication({ payload }));
      yield put({
        type: 'appendValue',
        payload: {
          details: response,
        },
      });
    },
    *fetchEditApplication({ payload }, { call, put }) {
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
    *fetchAddApplication({ payload }, { call, put }) {
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

    *fetchCreateSnippets({ payload, cb }, { call, put }) {
      const queryData = {
        snippet: {
          parentGroupId: payload.component.parentGroupId,
          processors: {},
          funnels: {},
          inputPorts: {},
          outputPorts: {},
          remoteProcessGroups: {},
          processGroups: {},
          connections: {},
          labels: {},
        },
      }
      queryData.snippet.processGroups[payload.id] = {
        clientId: '2c94336b-31e3-1c01-62e3-503bb4f0c1ef',
        version: 0,
      }
      const response = yield call(createSnippets, queryData);
      yield put({
        type: 'appendValue',
        payload: {
          snippet: response.snippet,
        },
      })
      yield cb && cb(response.snippet)
    },
    * fetchUpdateAppState({ payload }, { call, put }) {
      const response = yield call(updateAppState, payload);
      if (response) {
        // message.success('成功！');
      }
    },
    * fetchValidationRunApp({ payload }, { call, put }) {
      const response = yield call(validationRunApp, payload.snippetId);
      if (response) {
        const stateDate = {
          id: payload.id,
          state: 'RUNNING',
        }
        yield call(updateAppState, stateDate);
      }
    },
    * fetchValidationDeleteApp({ payload }, { call, put }) {
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
    * fetchDeleteApplication({ payload }, { call, put }) {
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
    * fetchCopeApplication({ payload }, { call, put }) {
      const queryDate = {
        id: payload.id,
        body: {
          originX: 0,
          originY: 0,
          snippetId: payload.snippetId,
        },
      }
      const response = yield call(copeApplication, queryDate);
      if (response) {
        // message.success('创建应用成功！');
      }
    },
    * fetchCreateAppTemp({ payload }, { call, put }) {
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
