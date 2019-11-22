// import { message } from 'antd';
import {
  queryOfficialTemplates, queryCollectTemplates, queryCustomTemplates,
  queryDownloadMTemplates, queryDownloadToken, queryDownloadTemplate,
} from '@/services/template';
import { download } from '@/utils/utils';

export default {
  namespace: 'template',

  state: {
    collectList: [],
    officialList: [],
    customList: [],
  },

  effects: {
    *fetchOfficialTemplates({ payload }, { call, put }) {
      const response = yield call(queryOfficialTemplates, payload);
      yield put({
        type: 'appendValue',
        payload: {
          officialList: response,
        },
      });
    },
    *fetchCollectTemplates({ payload }, { call, put }) {
      const response = yield call(queryCollectTemplates, payload);
      yield put({
        type: 'appendValue',
        payload: {
          collectList: response,
        },
      });
    },
    *fetchCustomTemplates({ payload }, { call, put }) {
      const response = yield call(queryCustomTemplates, payload);
      yield put({
        type: 'appendValue',
        payload: {
          customList: response,
        },
      });
    },
    // 下载
    *fetchDownloadTemplates({ payload }, { call, put }) {
      if (payload.type === 'multiple') {
        const response = yield call(queryDownloadMTemplates, payload);
        if (response) {
          download(response, 'templates.zip')
        }
      } else {
        const token = yield call(queryDownloadToken);
        if (token) {
          const response = yield call(queryDownloadTemplate, { downloadToken: token }, ...payload);
          if (response) {
            download(response, `${payload.name}.xml`)
          }
        }
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
