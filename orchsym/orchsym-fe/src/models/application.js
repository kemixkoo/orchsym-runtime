import { createSnippets } from '@/services/studio';
import {
  querySearchApplication, updateAppEnable, updateAppDisable,
  showAppStatus, deleteApplication, copeApplication,
  detailApplication, editApplication, addApplication,
  createApplicationTemp, updateAppState,
} from '@/services/application';
import { validationRunApp, validationDeleteApp, validationAppCheckName, validationTempCheckName } from '@/services/validation';
import { downloadApplication } from '@/services/template';
import { getClientId } from '@/utils/authority';
import { download } from '@/utils/utils';
import { formatMessage } from 'umi-plugin-react/locale';
import { message } from 'antd';

export default {
  namespace: 'application',

  state: {
    applicationNameList: [],
    applicationList: [],
    parentId: '',
    appDetails: {},
    appStatus: {},
  },

  effects: {
    *fetchApplication({ payload, cb }, { call, put }) {
      const response = yield call(querySearchApplication, payload);
      if (payload.isDetail) {
        yield put({
          type: 'appendValue',
          payload: {
            applicationList: response,
            applicationNameList: [],
          },
        });
      } else {
        yield put({
          type: 'appendValue',
          payload: {
            applicationList: [],
            applicationNameList: response,
          },
        });
      }

      yield cb && cb(response)
    },
    // *fetchApplication({ payload }, { call, put }) {
    //   const response = yield call(queryApplication);
    //   yield put({
    //     type: 'appendValue',
    //     payload: {
    //       applicationList: response.processGroupFlow.flow.processGroups,
    //       parentId: response.processGroupFlow.id,
    //     },
    //   });
    // },
    *fetchDetailApplication({ payload }, { call, put }) {
      const response = yield call(detailApplication, payload);
      yield put({
        type: 'appendValue',
        payload: {
          appDetails: response,
        },
      });
    },
    *fetchEditApplication({ payload, cb }, { call, put }) {
      const { values: { name, tags, comments }, appDetails: { id, revision } } = payload;
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
      yield call(editApplication, params);
      yield cb && cb()
    },
    *fetchAddApplication({ payload, cb }, { call, put }) {
      const { values: { name, comments, tags } } = payload;
      const params = {
        // parentId,
        value: {
          component: {
            name,
            comments,
            tags,
            // position: { x: 0, y: 0 },
          },
          revision: {
            clientId: getClientId(),
            version: 0,
          },
        },
      }
      yield call(addApplication, params);
      yield cb && cb()
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
    // 状态变化
    * fetchUpdateAppState({ payload, cb }, { call, put }) {
      yield call(updateAppState, payload);
      yield cb && cb()
    },
    * fetchUpdateEnable({ payload, cb }, { call, put }) {
      yield call(updateAppEnable, payload);
      yield cb && cb()
    },
    * fetchUpdateDisable({ payload, cb }, { call, put }) {
      yield call(updateAppDisable, payload);
      yield cb && cb()
    },
    * fetchIsShowStatus({ payload, cb }, { call, put }) {
      const response = yield call(showAppStatus, payload);
      yield cb && cb(response)
    },
    * fetchDownloadApp({ payload, cb }, { call, put }) {
      const res = yield call(downloadApplication, payload.id);
      if (res) {
        download(res, `${payload.name}.xml`)
      }
      yield cb && cb(res)
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

    // 删除
    * fetchValidationDeleteApp({ payload, cb }, { call, put }) {
      const response = yield call(validationDeleteApp, payload);
      yield cb && cb(response)
    },

    * fetchDeleteApplication({ payload, cb }, { call, put }) {
      yield call(deleteApplication, payload);
      yield cb && cb()
    },

    // 复制
    * fetchCopeApplication({ payload, cb }, { call, put }) {
      const { values, appDetails: { id } } = payload;
      const body = {
        appId: id,
        ...values,
      }
      yield call(copeApplication, body);
      yield cb && cb()
    },
    // * fetchValidationDeleteApp({ payload, cb }, { call, put }) {
    //   try {
    //     yield call(validationDeleteApp, payload);
    //     yield cb && cb()
    //   } catch {
    //     yield put({
    //       type: 'fetchDeleteApplication',
    //       payload,
    //     });
    //   }
    // },

    // * fetchDeleteApplication({ payload }, { call, put }) {
    //   const queryData = {
    //     id: payload,
    //     clientId: getClientId(),
    //     version: 0,
    //   }
    //   const response = yield call(deleteApplication, queryData);
    //   if (response) {
    //     message.success(formatMessage({ id: 'result.success' }));
    //     yield put({
    //       type: 'fetchApplication',
    //     });
    //   }
    // },
    // * fetchCopeApplication({ payload }, { call, put }) {
    //   const queryDate = {
    //     id: payload.id,
    //     body: {
    //       originX: 0,
    //       originY: 0,
    //       snippetId: payload.snippetId,
    //     },
    //   }
    //   const response = yield call(copeApplication, queryDate);
    //   if (response) {
    //     message.success(formatMessage({ id: 'result.success' }));
    //     yield put({
    //       type: 'fetchApplication',
    //     });
    //   }
    // },

    // 验证名字重复
    * fetchValidationCheckName({ payload, cb }, { call, put }) {
      const response = yield call(validationAppCheckName, payload);
      yield cb && cb(response)
    },
    // 验证模板名字重复
    * fetchCheckTempName({ payload, cb }, { call, put }) {
      const response = yield call(validationTempCheckName, payload);
      yield cb && cb(response)
    },
    // 存为模板
    * fetchCreateAppTemp({ payload, cb }, { call, put }) {
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
        message.success(formatMessage({ id: 'result.success' }));
      }
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
