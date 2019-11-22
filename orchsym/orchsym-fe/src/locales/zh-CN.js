import common from './zh-CN/common';
import exception from './zh-CN/exception';
import form from './zh-CN/form';
import globalHeader from './zh-CN/globalHeader';
import login from './zh-CN/login';
// 左侧导航组件
import menu from './zh-CN/menu';
import result from './zh-CN/result';
import settingDrawer from './zh-CN/settingDrawer';
import settings from './zh-CN/settings';
import validation from './zh-CN/validation';

// add test
import blank from './zh-CN/blank';
// 应用页面
import application from './zh-CN/application';
// 模板页面
import template from './zh-CN/template';

export default {
  'navBar.lang': '语言',
  'layout.user.link.help': '帮助',
  'layout.user.link.privacy': '隐私',
  'layout.user.link.terms': '条款',
  'app.home.introduce': '介绍',
  ...common,
  ...exception,
  ...globalHeader,
  ...login,
  ...menu,
  ...result,
  ...settingDrawer,
  ...settings,
  ...form,
  ...validation,
  ...blank,
  ...application,
  ...template,
};
