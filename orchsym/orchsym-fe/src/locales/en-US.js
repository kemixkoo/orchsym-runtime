import exception from './en-US/exception';
import globalHeader from './en-US/globalHeader';
import login from './en-US/login';
import menu from './en-US/menu';
import monitor from './en-US/monitor';
import result from './en-US/result';
import settingDrawer from './en-US/settingDrawer';
import settings from './en-US/settings';
import component from './en-US/component';
import form from './en-US/form';

// add test
import blank from './en-US/blank';

// 应用页面
import application from './en-US/application';
// 模版页面
import template from './en-US/template';
// 编排页面
import orchestrations from './en-US/orchestrations';

export default {
  'navBar.lang': 'Languages',
  'layout.user.link.help': 'Help',
  'layout.user.link.privacy': 'Privacy',
  'layout.user.link.terms': 'Terms',
  'app.home.introduce': 'introduce',
  ...exception,
  ...globalHeader,
  ...login,
  ...menu,
  ...monitor,
  ...result,
  ...settingDrawer,
  ...settings,
  ...component,
  ...form,
  ...blank,
  ...application,
  ...template,
  ...orchestrations,
};
