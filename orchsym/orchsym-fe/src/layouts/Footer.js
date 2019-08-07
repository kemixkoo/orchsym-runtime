import React, { Fragment } from 'react';
import { Layout, Icon } from 'antd';
import GlobalFooter from '@/components/GlobalFooter';
// import styles from './Footer.less';

const { Footer } = Layout;
const FooterView = () => (
  <Footer style={{ padding: 0 }}>
    <GlobalFooter
      links={window.globalFooterInfo}
      copyright={
        <Fragment>
          Copyright <Icon type="copyright" /> {window.copyright}
        </Fragment>
      }
    // className={styles.bgColor}
    />
  </Footer>
);
export default FooterView;
