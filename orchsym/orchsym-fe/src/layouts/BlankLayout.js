import React, { PureComponent } from 'react';
import { Layout } from 'antd';
import { connect } from 'dva';
// import { getToken } from '@/utils/authority';
import styles from './BlankLayout.less'

const { Content } = Layout;

class BlankLayout extends PureComponent {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'login/fetchAccessOidc',
    });
    // console.log(window.document.cookie)
    // window.location.href = '/user/login'
  }

  render() {
    const { children } = this.props;
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
