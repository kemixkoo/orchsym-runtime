import React, { Component } from 'react'; // Fragment
import PropTypes from 'prop-types';
// import { formatMessage } from 'umi-plugin-react/locale';
import { connect } from 'dva';
// import Link from 'umi/link';
// import { Icon } from 'antd';
// import GlobalFooter from '@/components/GlobalFooter';
import DocumentTitle from 'react-document-title';
import SelectLang from '@/components/SelectLang';
import styles from './UserLayout.less';
// import logo from '../assets/login-logo.svg';
import getPageTitle from '@/utils/getPageTitle';


// const links = [{
//   key: 'company',
//   title: '白山云',
//   href: 'https://www.baishancloud.com/zh/',
// }, {
//   key: 'product',
//   title: '数聚蜂巢',
//   href: 'https://www.baishancloud.com/tech/orchsym/',
// }, {
//   key: 'version',
//   title: window.version ? window.version : '2.0',
// }];

// const copyright = (
//   <Fragment>
//     Copyright <Icon type="copyright" /> 2017-2019 数聚蜂巢
//   </Fragment>
// );

class UserLayout extends Component {
  componentDidMount() {
    const {
      dispatch,
      route: { routes, authority },
    } = this.props;
    dispatch({
      type: 'menu/getMenuData',
      payload: { routes, authority },
    });
  }

  render() {
    const {
      children,
      location: { pathname },
      breadcrumbNameMap,
    } = this.props;
    return (
      <DocumentTitle title={getPageTitle(pathname, breadcrumbNameMap)}>
        <div className={styles.container}>
          <div className={styles.lang} style={{ color: '#ffffff' }}>
            <SelectLang />
          </div>
          <div className={styles.content}>
            {/* <div className={styles.top}>
              <div className={styles.header}>
                <Link to="/">
                  <img alt="logo" className={styles.logo} src={logo} />
                </Link>
              </div>
            </div> */}
            {children}
          </div>
          {/* <GlobalFooter links={links} copyright={copyright} /> */}
        </div>
      </DocumentTitle>
    );
  }
}

export default connect(({ menu: menuModel }) => ({
  menuData: menuModel.menuData,
  breadcrumbNameMap: menuModel.breadcrumbNameMap,
}))(UserLayout);

UserLayout.propTypes = {
  dispatch: PropTypes.func,
  route: PropTypes.any,
  children: PropTypes.any,
  location: PropTypes.any,
  breadcrumbNameMap: PropTypes.any,
}
