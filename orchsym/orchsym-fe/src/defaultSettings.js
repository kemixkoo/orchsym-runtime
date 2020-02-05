module.exports = {
  navTheme: 'dark', // theme for nav menu
  primaryColor: '#1890FF', // primary color of ant design
  layout: 'sidemenu', // nav menu position: sidemenu or topmenu
  contentWidth: 'Fluid', // layout of content: Fluid or Fixed, only works when layout is topmenu
  fixedHeader: true, // sticky header
  autoHideHeader: false, // auto hide header
  fixSiderbar: true, // sticky siderbar
  menu: {
    disableLocal: false,
  },
  title: 'Orchsym Studio',
  // 注意：如果需要图标多色，Iconfont 图标项目里要进行批量去色处理
  // Usage: https://github.com/ant-design/ant-design-pro/pull/3517 //at.alicdn.com/t/font_1148837_vj9atcf8qq.js at.alicdn.com/t/font_8d5l8fzk5b87iudi.js
  iconfontUrl: '//at.alicdn.com/t/font_1148837_vj9atcf8qq.js',
};
