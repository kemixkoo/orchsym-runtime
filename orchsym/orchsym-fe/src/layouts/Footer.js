import React, { Fragment } from 'react';
import { Layout, Icon } from 'antd';
import GlobalFooter from '@/components/GlobalFooter';
// import styles from './Footer.less';

const { Footer } = Layout;
const FooterView = () => (
  <Footer style={{ padding: 0 }}>
    <GlobalFooter
      links={[
        {
          key: 'orchsym',
          title: 'Orchsym Studio',
          href: '',
          blankTarget: true,
        },
        {
          key: 'bitbucket',
          title: <Icon type="code" />,
          href: 'http://jr.baishancloud.com:7990/projects/DATA/repos/nifi/browse',
          blankTarget: true,
        },
        {
          key: 'Baishan Cloud',
          title: 'Baishan Cloud',
          href: '',
          blankTarget: true,
        },
      ]}
      copyright={
        <Fragment>
          Copyright <Icon type="copyright" /> 白山云科技-数聚蜂巢
        </Fragment>
      }
      // className={styles.bgColor}
    />
  </Footer>
);
export default FooterView;
