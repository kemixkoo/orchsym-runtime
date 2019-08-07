import React, { PureComponent } from 'react';
import { Row, Col, Button } from 'antd';
import CreateOrEditApp from '../CreateOrEditApp';
import ApplicationSearch from './ApplicationSearch';
import SortApplication from './SortApplication';
import styles from './index.less';
// import IconFont from '@/components/IconFont';

export default class AppList extends PureComponent {
  state = {
    createAppVisible: null,
    createOrEdit: '创建应用',
  };

  showCreateModal = () => {
    this.setState({
      createAppVisible: true,
    })
  };

  handleCreateEditCancel = () => {
    this.setState({
      createAppVisible: false,
    })
  };

  render() {
    const { createAppVisible, createOrEdit } = this.state;
    return (
      <div>
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
        <CreateOrEditApp visible={createAppVisible} handleCreateEditCancel={this.handleCreateEditCancel} title={createOrEdit} />
      </div>
    );
  }
}
