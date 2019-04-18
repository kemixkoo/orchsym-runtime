import React from 'react';
import { formatMessage } from 'umi-plugin-react/locale';
import styles from './index.less';

const Index = () => {
  return (
    <div className={styles.color}>{formatMessage({ id: 'page.blank.index' })}</div>
  );
};

export default Index;
