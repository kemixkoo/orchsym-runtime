import React, { lazy } from 'react';
// Suspense
import { Layout } from 'antd';
import PropTypes from 'prop-types';
import DocumentTitle from 'react-document-title';
import { connect } from 'dva';
// import logo from '../assets/logo.svg';
import Footer from './Footer';
import Header from './Header';
import Context from './MenuContext';
import SiderMenu from '@/components/SiderMenu';
import getPageTitle from '@/utils/getPageTitle';
import styles from './BasicLayout.less';

// lazy load SettingDrawer
const SettingDrawer = lazy(() => import('@/components/SettingDrawer'));

const { Content } = Layout;
const logo = window.logoHref.companyLogoIndex
const smallLogo = window.logoHref.companyLogoIndexMix

class BasicLayout extends React.Component {
  componentDidMount() {
    const {
      dispatch,
      route: { routes, path, authority },
    } = this.props;
    dispatch({
      type: 'setting/getSetting',
    });
    dispatch({
      type: 'menu/getMenuData',
      payload: { routes, path, authority },
    });
  }

  getContext() {
    const { location, breadcrumbNameMap } = this.props;
    return {
      location,
      breadcrumbNameMap,
    };
  }

  getLayoutStyle = () => {
    const { fixSiderbar, collapsed, layout } = this.props;
    if (fixSiderbar && layout !== 'topmenu') {
      return {
        paddingLeft: collapsed ? '80px' : '185px',
      };
    }
    return null;
  };

  handleMenuCollapse = collapsed => {
    const { dispatch } = this.props;
    dispatch({
      type: 'global/changeLayoutCollapsed',
      payload: collapsed,
    });
  };

  renderSettingDrawer = () => {
    // Do not render SettingDrawer in production
    // unless it is deployed in preview.pro.ant.design as demo
    // preview.pro.ant.design only do not use in your production ; preview.pro.ant.design 专用环境变量，请不要在你的项目中使用它。
    if (
      process.env.NODE_ENV === 'production' &&
      ANT_DESIGN_PRO_ONLY_DO_NOT_USE_IN_YOUR_PRODUCTION !== 'site'
    ) {
      return null;
    }
    return <SettingDrawer />;
  };

  render() {
    const {
      navTheme,
      layout: PropsLayout,
      children,
      location: { pathname },
      menuData,
      breadcrumbNameMap,
      fixedHeader,
    } = this.props;
    // const onClose = e => {
    //   console.log(e, 'I was closed.');
    // };
    const isTop = PropsLayout === 'topmenu';
    const contentStyle = !fixedHeader ? { paddingTop: 0 } : {};
    const layout = (
      <Layout>
        {/* <Alert
          className={styles.alertBg}
          // message="Warning"
          description="License将在yy天后过期，为保证业务流程的运行正常，请及时更新，否则runtime将自动停止"
          type="warning"
          showIcon
          closable
          onClose={onClose}
        /> */}
        {isTop ? null : (
          <SiderMenu
            logo={logo}
            smallLogo={smallLogo}
            theme={navTheme}
            onCollapse={this.handleMenuCollapse}
            menuData={menuData}
            {...this.props}
          />
        )}
        <Layout
          className={styles.basicContainer}
          style={{
            ...this.getLayoutStyle(),
            minHeight: '100vh',
          }}
        >
          <Header
            menuData={menuData}
            handleMenuCollapse={this.handleMenuCollapse}
            logo={logo}
            {...this.props}
          />
          <div className={styles.basicContent}>
            <Content className={styles.content} style={contentStyle}>
              {children}
            </Content>
          </div>
          <Footer
            className={styles.basicFooter}
          />
        </Layout>
      </Layout>
    );
    return (
      <React.Fragment>
        <DocumentTitle title={getPageTitle(pathname, breadcrumbNameMap)}>
          <div>
            <Context.Provider value={this.getContext()}>{layout}</Context.Provider>
          </div>
        </DocumentTitle>
        {/* <Suspense fallback={null}>{this.renderSettingDrawer()}</Suspense> */}
      </React.Fragment>
    );
  }
}

export default connect(({ global, setting, menu: menuModel }) => ({
  collapsed: global.collapsed,
  layout: setting.layout,
  menuData: menuModel.menuData,
  breadcrumbNameMap: menuModel.breadcrumbNameMap,
  ...setting,
}))(props => <BasicLayout {...props} />);

BasicLayout.propTypes = {
  dispatch: PropTypes.func,
  route: PropTypes.any,
  location: PropTypes.any,
  breadcrumbNameMap: PropTypes.any,
  fixSiderbar: PropTypes.any,
  collapsed: PropTypes.any,
  layout: PropTypes.any,
  navTheme: PropTypes.any,
  children: PropTypes.any,
  fixedHeader: PropTypes.any,
  menuData: PropTypes.any,
}
