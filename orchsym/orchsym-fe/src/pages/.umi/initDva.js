import dva from 'dva';
import createLoading from 'dva-loading';

const runtimeDva = window.g_plugins.mergeConfig('dva');
let app = dva({
  history: window.g_history,
  
  ...(runtimeDva.config || {}),
});

window.g_app = app;
app.use(createLoading());
(runtimeDva.plugins || []).forEach(plugin => {
  app.use(plugin);
});
app.use(require('/Users/guozhengzhong/Desktop/nifi/orchsym/orchsym-fe/node_modules/dva-immer/lib/index.js').default());
app.model({ namespace: 'global', ...(require('/Users/guozhengzhong/Desktop/nifi/orchsym/orchsym-fe/src/models/global.js').default) });
app.model({ namespace: 'login', ...(require('/Users/guozhengzhong/Desktop/nifi/orchsym/orchsym-fe/src/models/login.js').default) });
app.model({ namespace: 'menu', ...(require('/Users/guozhengzhong/Desktop/nifi/orchsym/orchsym-fe/src/models/menu.js').default) });
app.model({ namespace: 'project', ...(require('/Users/guozhengzhong/Desktop/nifi/orchsym/orchsym-fe/src/models/project.js').default) });
app.model({ namespace: 'setting', ...(require('/Users/guozhengzhong/Desktop/nifi/orchsym/orchsym-fe/src/models/setting.js').default) });
app.model({ namespace: 'user', ...(require('/Users/guozhengzhong/Desktop/nifi/orchsym/orchsym-fe/src/models/user.js').default) });
