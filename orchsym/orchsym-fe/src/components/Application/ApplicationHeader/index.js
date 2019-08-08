import React, { PureComponent } from 'react';
import { Row, Col, Button } from 'antd';
import CreateApplication from '../CreateApplication';
import ApplicationSearch from './ApplicationSearch';
import SortApplication from './SortApplication';
import styles from './index.less';
// import IconFont from '@/components/IconFont';

export default class AppList extends PureComponent {
  state = {
    createAppVisible: null,
  };

  showCreateModal = () => {
    this.setState({
      createAppVisible: true,
    })
  };

  handleCreateCancel = () => {
    this.setState({
      createAppVisible: false,
    })
  };

  getHeadWidth = () => {
    const { collapsed } = this.props;
    return collapsed ? 'calc(100% - 80px)' : 'calc(100% - 185px)';
  };

  render() {
    const { createAppVisible } = this.state;
    const width = this.getHeadWidth();
    return (
      <div style={{ width }} className={styles.applicationHeader}>
        <Row gutter={16} className={styles.bottomSpace}>
          <Col span={3}>
            <Button type="primary" onClick={this.showCreateModal}>
              创建应用
            </Button>
          </Col>
          <Col span={20}>
            <div className={styles.applicationRight}>
              <ApplicationSearch />
            </div>
          </Col>
          <Col span={1}>
            <SortApplication />
          </Col>
        </Row>
        <CreateApplication visible={createAppVisible} handleCreateCancel={this.handleCreateCancel} />
      </div>
    );
  }
}
