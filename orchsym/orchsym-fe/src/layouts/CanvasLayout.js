import React, { lazy } from 'react';
// Suspense
import PropTypes from 'prop-types';
import DocumentTitle from 'react-document-title';
import { connect } from 'dva';
// import logo from '../assets/logo.svg';
// import Footer from './Footer';
// import Header from './CanvasHeader';
import getPageTitle from '@/utils/getPageTitle';
import styles from './CanvasLayout.less';

// lazy load SettingDrawer
const SettingDrawer = lazy(() => import('@/components/SettingDrawer'));

// const logo = window.logoHref.companyLogoIndex
// const smallLogo = window.logoHref.companyLogoIndexMix

class BasicLayout extends React.Component {
  componentDidMount() {
    const {
      dispatch,
      route: { routes, path, authority },
    } = this.props;
    dispatch({
      type: 'user/fetchCurrent',
    });
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
    return {
      paddingLeft: '0px',
      height: 'calc(100% - 0px)',
      overflow: 'hidden',
    };
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
      children,
      location: { pathname },
      // menuData,
      breadcrumbNameMap,
    } = this.props;

    // const isTop = PropsLayout === 'topmenu';
    const layout = (
      <div
        className={styles.basicContainer}
        style={{
          ...this.getLayoutStyle(),
        }}
      >
        {/* <Header
          menuData={menuData}
          handleMenuCollapse={this.handleMenuCollapse}
          logo={logo}
          {...this.props}
        /> */}
        <div className={styles.basicContent}>
          {children}
        </div>
      </div>
    );
    return (
      <DocumentTitle title={getPageTitle(pathname, breadcrumbNameMap)}>
        {layout}
      </DocumentTitle>
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
  // fixSiderbar: PropTypes.any,
  // collapsed: PropTypes.any,
  // layout: PropTypes.any,
  children: PropTypes.any,
  // menuData: PropTypes.any,
}
