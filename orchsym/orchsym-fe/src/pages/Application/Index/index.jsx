import React from 'react';
// import { formatMessage } from 'umi-plugin-react/locale';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import Apilist from '@/components/Application/appList';
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
        <Apilist />
      </div>
    </PageHeaderWrapper>
  );
};

export default Index;
