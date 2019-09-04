import React, { PureComponent } from 'react';
import { Layout } from 'antd';
import { connect } from 'dva';
// import { getToken } from '@/utils/authority';
import styles from './BlankLayout.less'

const { Content } = Layout;

class BlankLayout extends PureComponent {
  componentDidMount() {
    const {
      dispatch,
    } = this.props;
    if (!window.document.cookie) {
      // @HACK
      /* eslint-disable no-underscore-dangle */
      window.location.href = '/user/login'
      // eslint-disable-next-line no-undef
      // location.href = 'https://172.18.28.230:18443/runtime/login'
    } else {
      dispatch({
        type: 'login/fetchAccessOidc',
      });
    }
  }

  render() {
    const {
      children,
    } = this.props;
    return (
      <Layout>
        <Content className={styles.container}>{children}</Content>
      </Layout>
    )
  }
}
export default connect(({ login }) => ({
  login,
}))(props => <BlankLayout {...props} />);
