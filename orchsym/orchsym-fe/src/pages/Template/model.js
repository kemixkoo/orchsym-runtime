// import { message } from 'antd';
import {
  queryOfficialTemplates, queryCollectTemplates, queryCustomTemplates,
  queryDownloadMTemplates, queryDownloadToken, queryDownloadTemplate,
  editTemplate, uploadTemplate, collectTemplate, cancelCollectTemplate,
  deletedMTemplates, deleteTemplate,
} from '@/services/template';
import { download } from '@/utils/utils';

export default {
  namespace: 'template',

  state: {
    favoriteList: [],
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
          favoriteList: response,
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
    // 编辑
    * fetchEditTemplate({ payload, cb }, { call, put }) {
      yield call(editTemplate, payload);
      yield cb && cb()
    },
    // 上传
    *fetchUploadTemp({ payload, cb }, { call, put }) {
      yield call(uploadTemplate, payload);
      yield cb && cb();
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
          const response = yield call(queryDownloadTemplate, { ...payload, downloadToken: token });
          if (response) {
            download(response, `${payload.name}.xml`)
          }
        }
      }
    },
    // 删除
    *fetchDeleteTemplates({ payload, cb }, { call, put }) {
      if (payload.type === 'multiple') {
        yield call(deletedMTemplates, payload);
      } else {
        yield call(deleteTemplate, payload);
      }
      yield cb && cb();
    },
    // 收藏
    *fetchCollectTemp({ payload, cb }, { call, put }) {
      if (payload.state) {
        yield call(collectTemplate, payload.id);
      } else {
        yield call(cancelCollectTemplate, payload.id);
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
