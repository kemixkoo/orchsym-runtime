<h1 align="center">Orchsym Studio</h1>
<h5>数聚蜂巢项目集成编排平台前端项目</h5>

> 基于 ANT DESIGN PRO v2.3.1

<div align="center">

[![Build With Umi](https://img.shields.io/badge/build%20with-umi-028fe4.svg?style=flat-square)](http://umijs.org/)
- - -
![](http://nifi.apache.org/assets/images/flow.png)

</div>

## 项目结构

```
- config                       # umi 配置，包含路由，构建等配置
- mock                         # mock数据。此目录下所有的 .js 文件（包括 _ 前缀的）都会被解析为 mock 文件。具体使用参考 umi 文档
- public                       # 本地开发服务器根目录
  - favicon
- src                          # 源码目录
  - assets                     # 本地静态资源、图片等
  - components                 # 公共组件。不与 store 进行 connect 发生耦合。
    - _utils                   # 一些组件内用到的公用方法
    - Authorized               # 自带的权限校验
    - Exception                # 自带的异常页面组件，在 pages/Exception 中使用
    - GlobalFooter             # 页面头部
    - GlobalFooter             # 页脚
    - HeaderDropdown           # 页头中的下拉组件。用户中心和选择语言使用到
    - HeaderSearch             # 页面头部中的搜索框组件
    - IconFont                 # 字体图标。在iconfont官网创建项目，基于"项目"维度来使用应该会很方便（正在尝试ing）
    - Login                    # 登录
    - NoticeIcon               # 站内信（消息通知）
    - PageHeaderWraper         # 自带的页面面包屑及内容辩题，需要面包屑可引入
    - PageLoading              # loading
    - Result                   # 结果组件。pages中的结果页面有用到
    - SelectLang               # 多语言切换
    - SettingDrawer            # 抽屉组件。原型确定后，无此交互可删除
    - SiderMenu                # 左侧的导航tab组件
    - TopNavHeader             # 顶部导航模式时的顶部导航条，原型确定后，无此需求可删除
  - layouts                    # 页面布局
    - BasicLayout              # 基础布局
    - BlankLayout              # 空布局
    - UserLayout               # 登录页面布局
  - locales                    # 多语言包，国际化资源（要想多语言显示，必须在这里面写页面的静态内容！！！）
  - models                     # model 分两类，一是全局 model，二是页面 model。此处的为全局 dva model，所有页面都可引用；页面 model 不能被其他页面所引用。推荐全局数据（如 global、userInfo等）写到这里，每个页面自身的数据在自己的 pages 目录下新建 model 文件夹，保持全局 model 的轻量。这样动态加载才会生效，对于性能优化有很好的效果。具体参考umi文档-model 注册一节
  - pages                      # 其中就是我们创建的每个页面及其业务代码
    - .umi                     # dev 临时目录，需添加到 .gitignore
    - Blank                    # 新增的空白页面
    - Exception                # 自带的 404 等异常页面（components中的 Exception）
    - Result                   # 自带的结果页面，成功失败两种模板（components中的 Result）
    - User                     # 用户登录页面
    - Authorized.js            # 权限校验
    - document.ejs             # html 模版，挂载 react 根实例
  - services                   # 异步数据 API 接口定义
  - utils                      # fetch、权限、工具函数、正则定义
  - global.less                # 全局样式
  - global.js                  # 全局 JS
```

## 页面模板

```
- 结果
  - 成功页
  - 失败页
- 异常
  - 403 无权限
  - 404 找不到
  - 500 服务器出错
- 帐户
  - 登录
  - 注册
  - 注册成功
```

## 开发
```bash
$ npm install
$ npm start || npm run dev
# 访问 http://localhost:9001
```
## 编码规范（大家一起逐步完善）
##### 国际化支持
- 由于项目支持多语言模式，所以编码时不能直接在页面中输入静态内容，需要分别去到`/src/locales/zh-CN.js` 和 `/src/locales/en-US.js`等语言包里根据唯一的`key`值进行匹配。
- 可参考`/config/router.config.js`里的路由项配置。其`key`值为每个路由的`name`属性值。通过`menu`这个"命名空间"保证唯一性。
- 在日常的编码中，尽量通过`/src/pages/`内部页面的维度来确定命名空间，这样只要保证不出现同名文件夹，就不会产生冲突。
- - -
##### 样式处理
- 支持`less`和`styled-components` 两种写法 
- - -
##### 字体图标
- iconfont 的 symbol 格式（svg）。使用方法如下：
- 可使用 GitHub 或者新浪微博登录iconfont官网，将自己的用户名发送给项目发起者，加入项目组。
- 在 iconfont 网站上找到自己需要的某个图标后，先添加至购物车，然后在购物车中添加至项目，在 symbol 模式下重新生成js链接，将此 cdn 链接替换`/src/defaultSettings.js`里的`iconfontUrl`属性值
- 在页面中直接引用`<IconFont style="" />`组件，将icon名赋值成为组件的type属值即可
- 在`config/router.config.js`里将icon名直接改成`icon`属性值即可
## 支持环境

现代浏览器及 `IE11`。**推荐使用新版本 `Chrome` 浏览器**

| [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/edge/edge_48x48.png" alt="IE / Edge" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>IE / Edge | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/firefox/firefox_48x48.png" alt="Firefox" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Firefox | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/chrome/chrome_48x48.png" alt="Chrome" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Chrome | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/safari/safari_48x48.png" alt="Safari" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Safari | [<img src="https://raw.githubusercontent.com/alrra/browser-logos/master/src/opera/opera_48x48.png" alt="Opera" width="24px" height="24px" />](http://godban.github.io/browsers-support-badges/)</br>Opera |
| --------- | --------- | --------- | --------- | --------- |
| IE11, Edge| last 2 versions| last 2 versions| last 2 versions| last 2 versions

## 参与人员
- 产品：
- FE：
- BE：
- QA：
