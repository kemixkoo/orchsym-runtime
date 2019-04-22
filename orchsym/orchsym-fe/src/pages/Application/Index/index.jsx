import React from 'react';
import { formatMessage } from 'umi-plugin-react/locale';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import IconFont from '@/components/IconFont'
import styles from './index.less';

const Index = () => {
  return (
    <PageHeaderWrapper title={formatMessage({ id: 'page.application.content.title' })}>
      <div className={styles.content}>
        {formatMessage({ id: 'page.application.content' })}
        <IconFont type="studioicondisplay-code" />
      </div>
    </PageHeaderWrapper>
  );
};

export default Index;
