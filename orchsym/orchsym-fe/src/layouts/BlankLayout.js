import React from 'react';
import { Layout } from 'antd';
import styles from './BlankLayout.less'

const { Content } = Layout;


export default ({ children }) => <Layout><Content className={styles.container}>{children}</Content></Layout>;
