import React from 'react';
// import { formatMessage } from 'umi-plugin-react/locale';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import Application from '@/components/Application';
// import IconFont from '@/components/IconFont'
import styles from './index.less';

const Index = () => {
  // const tabList = [
  //   {
  //     name: '应用列表',
  //     key: 'appList',
  //   },
  // ]
  return (
    <PageHeaderWrapper>
      <div className={styles.content}>
        <Application />
      </div>
    </PageHeaderWrapper>
  );
};

export default Index;
