import React from 'react';
import { formatMessage } from 'umi-plugin-react/locale';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import IconFont from '@/components/IconFont'
import styles from './Index.less';

const Index = () => {
  return (
    <PageHeaderWrapper title={formatMessage({ id: 'page.blank.index.title' })}>
      <div className={styles.color}>
        {formatMessage({ id: 'page.blank.index' })}
        <IconFont type="studioicondisplay-code" />
      </div>
    </PageHeaderWrapper>
  );
};

export default Index;
