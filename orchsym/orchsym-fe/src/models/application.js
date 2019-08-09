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
    },
    *fetchAddApplication({ payload }, { call, put }) {
      const response = yield call(addApplication, payload);
      if (response) {
        message.success('创建应用成功！');
      }
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
      console.log(response)
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
        message.success('更新成功！');
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
    },
    * fetchDeleteApplication({ payload }, { call, put }) {
      const response = yield call(deleteApplication, payload);
      if (response) {
        // message.success('删除应用成功！');
      }
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
        message.success('存为模板成功！');
      }
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
