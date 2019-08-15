import { queryCurrentUser } from '@/services/Flow';

export default {
  namespace: 'user',
  state: {
    currentUser: {},
  },

  effects: {
    *fetchCurrent(_, { call, put }) {
      const response = yield call(queryCurrentUser);
      yield put({
        type: 'saveCurrentUser',
        payload: response,
      });
    },
  },

  reducers: {
    saveCurrentUser(state, action) {
      return {
        ...state,
        currentUser: action.payload || {},
      };
    },
    changeNotifyCount(state, action) {
      return {
        ...state,
        currentUser: {
          ...state.currentUser,
          notifyCount: action.payload.totalCount,
          unreadCount: action.payload.unreadCount,
        },
      };
    },
  },
};
