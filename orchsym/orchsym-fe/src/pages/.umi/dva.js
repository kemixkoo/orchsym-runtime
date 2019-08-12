import dva from 'dva';
import { Component } from 'react';
import createLoading from 'dva-loading';
import history from '@tmp/history';

let app = null;

export function _onCreate() {
  const plugins = require('umi/_runtimePlugin');
  const runtimeDva = plugins.mergeConfig('dva');
  app = dva({
    history,
    
    ...(runtimeDva.config || {}),
    ...(window.g_useSSR ? { initialState: window.g_initialData } : {}),
  });
  
  app.use(createLoading());
  (runtimeDva.plugins || []).forEach(plugin => {
    app.use(plugin);
  });
  app.use(require('/Users/carmenlulu/workspace/白山云/react/nifi/orchsym/orchsym-fe/node_modules/dva-immer/dist/index.js')());
  app.model({ namespace: 'application', ...(require('/Users/carmenlulu/workspace/白山云/react/nifi/orchsym/orchsym-fe/src/models/application.js').default) });
app.model({ namespace: 'global', ...(require('/Users/carmenlulu/workspace/白山云/react/nifi/orchsym/orchsym-fe/src/models/global.js').default) });
app.model({ namespace: 'login', ...(require('/Users/carmenlulu/workspace/白山云/react/nifi/orchsym/orchsym-fe/src/models/login.js').default) });
app.model({ namespace: 'menu', ...(require('/Users/carmenlulu/workspace/白山云/react/nifi/orchsym/orchsym-fe/src/models/menu.js').default) });
app.model({ namespace: 'project', ...(require('/Users/carmenlulu/workspace/白山云/react/nifi/orchsym/orchsym-fe/src/models/project.js').default) });
app.model({ namespace: 'setting', ...(require('/Users/carmenlulu/workspace/白山云/react/nifi/orchsym/orchsym-fe/src/models/setting.js').default) });
app.model({ namespace: 'user', ...(require('/Users/carmenlulu/workspace/白山云/react/nifi/orchsym/orchsym-fe/src/models/user.js').default) });
  return app;
}

export function getApp() {
  return app;
}

export class _DvaContainer extends Component {
  render() {
    const app = getApp();
    app.router(() => this.props.children);
    return app.start()();
  }
}
