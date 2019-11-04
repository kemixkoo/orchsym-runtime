import { message } from 'antd';
import { createSnippets } from '@/services/studio';
import { queryApplication, updateAppState } from '@/services/Flow';
import { validationRunApp, validationDeleteApp, validationAppCheckName } from '@/services/validation';
import {
  detailApplication, editApplication, addApplication,
  deleteApplication, copeApplication, createApplicationTemp,
} from '@/services/ProcessGroups';
import { getClientId } from '@/utils/authority';
import { formatMessage } from 'umi-plugin-react/locale';
// import { notification } from "antd/lib/index";

export default {
  namespace: 'application',

  state: {
    applicationList: [],
    parentId: '',
    details: {},
  },

  effects: {
    *fetchApplication({ payload }, { call, put }) {
      const response = yield call(queryApplication);
      yield put({
        type: 'appendValue',
        payload: {
          applicationList: response.processGroupFlow.flow.processGroups,
          parentId: response.processGroupFlow.id,
        },
      });
    },
    *fetchDetailApplication({ payload }, { call, put }) {
      const response = yield call(detailApplication, payload);
      yield put({
        type: 'appendValue',
        payload: {
          details: response,
        },
      });
    },
    *fetchEditApplication({ payload }, { call, put }) {
      const { values: { name, tags, comments }, details: { id, revision } } = payload;
      const params = {
        value: {
          component: {
            id,
            name,
            comments,
            tags,
          },
          revision,
        },
      }
      const response = yield call(editApplication, params);
      if (response) {
        message.success(formatMessage({ id: 'app.result.success' }));
        yield put({
          type: 'fetchApplication',
        });
      }
    },
    *fetchAddApplication({ payload }, { call, put }) {
      const { values: { name, comments, tags }, parentId } = payload;
      const params = {
        parentId,
        value: {
          component: {
            name,
            comments,
            tags,
            position: { x: 0, y: 0 },
          },
          revision: {
            clientId: getClientId(),
            version: 0,
          },
        },
      }
      const response = yield call(addApplication, params);
      if (response) {
        message.success(formatMessage({ id: 'app.result.success' }));
        yield put({
          type: 'fetchApplication',
        });
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
        clientId: getClientId(),
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
        message.success(formatMessage({ id: 'app.result.success' }));
        yield put({
          type: 'fetchApplication',
        });
      }
    },
    * fetchValidationRunApp({ payload }, { call, put }) {
      const response = yield call(validationRunApp, payload.snippetId);
      if (response) {
        yield put({
          type: 'fetchUpdateAppState',
          payload: {
            id: payload.id,
            state: 'RUNNING',
          },
        });
      }
    },
    * fetchValidationDeleteApp({ payload, cb }, { call, put }) {
      try {
        yield call(validationDeleteApp, payload);
        yield cb && cb()
      } catch {
        yield put({
          type: 'fetchDeleteApplication',
          payload,
        });
      }
    },
    * fetchDeleteApplication({ payload }, { call, put }) {
      const queryData = {
        id: payload,
        clientId: getClientId(),
        version: 0,
      }
      const response = yield call(deleteApplication, queryData);
      if (response) {
        message.success(formatMessage({ id: 'app.result.success' }));
        yield put({
          type: 'fetchApplication',
        });
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
        message.success(formatMessage({ id: 'app.result.success' }));
        yield put({
          type: 'fetchApplication',
        });
      }
    },
    * fetchCreateAppTemp({ payload }, { call, put }) {
      const { snippetId, values, id } = payload;
      const params = {
        id,
        body: {
          snippetId,
          description: values.description,
          name: values.name,
        },
      }
      const errorHandler = error => {
        const { response = {} } = error;
        console.log('error--', response)
      };
      const response = yield call(createApplicationTemp, params, errorHandler);
      if (response) {
        message.success(formatMessage({ id: 'app.result.success' }));
        yield put({
          type: 'fetchApplication',
        });
      }
    },
    * fetchValidationCheckName({ payload, cb }, { call, put }) {
      const response = yield call(validationAppCheckName, payload);
      yield cb && cb(response)
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
