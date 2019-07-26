import React, { PureComponent } from 'react';
import { Card, Menu, Icon, Dropdown, Badge, Row, Col, Button } from 'antd';
import styles from './application.less';
import CreateApplication from './CreateApplication';
import ApplicationSearch from './ApplicationSearch';
import EditApplication from './EditApplication';
import SortApplication from './SortApplication';
import SaveTemp from './SaveTemp';

export default class AppList extends PureComponent {
  state = {
    createAppVisible: null,
    editAppVisible: null,
    saveTempVisible: null,
  };

  showCreateModal = () => {
    this.setState({
      createAppVisible: true,
    })
  }

  handleCreateCancel = () => {
    this.setState({
      createAppVisible: false,
    })
  }

  showEditModal = () => {
    this.setState({
      editAppVisible: true,
    })
  }

  handleEditCancel = () => {
    this.setState({
      editAppVisible: false,
    })
  }

  showSaveTemp = () => {
    this.setState({
      saveTempVisible: true,
    })
  }

  handleSaveCancel = () => {
    this.setState({
      saveTempVisible: false,
    })
  }


  getCarList = (item) => {
    const menu = (
      <Menu>
        <Menu.Item key="1">
          <Icon type="appstore" />
          进入应用
        </Menu.Item>
        <Menu.Item key="2" onClick={this.showEditModal}>
          <Icon type="edit" />
          编辑应用
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="3">
          <Icon type="play-square" />
          运行
        </Menu.Item>
        <Menu.Item key="4">
          <Icon type="stop" />
          停止
        </Menu.Item>
        <Menu.Item key="5">
          <Icon type="check-square" />
          启用
        </Menu.Item>
        <Menu.Item key="6">
          <Icon type="close-square" />
          禁用
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="7">
          <Icon type="copy" />
          复制
        </Menu.Item>
        <Menu.Item key="8">
          <Icon type="download" />
          下载
        </Menu.Item>
        <Menu.Item key="9">
          <Icon type="delete" />
          删除
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="10" onClick={this.showSaveTemp}>
          <Icon type="save" />
          存为模板
        </Menu.Item>
      </Menu>
    );

    const dropdown = (
      <Dropdown overlay={menu}>
        <a className="ant-dropdown-link" href="#">
          <Icon type="more" />
        </a>
      </Dropdown>
    );

    const menu2 = (
      <Menu>
        <Menu.Item key="1">
          队列中
          <p className={styles.pStyle}>2（5.2 MB）</p>
        </Menu.Item>
        <Menu.Item key="2">
          输入
          <p className={styles.pStyle}>1（888.88 KB）</p>
        </Menu.Item>
        <Menu.Item key="3">
          输出
          <p className={styles.pStyle}>1（66 B）</p>
        </Menu.Item>
        <Menu.Item key="3">
          读 / 写
          <p className={styles.pStyle}>502 KB / 10.8 B</p>
        </Menu.Item>
      </Menu>
    );

    const dropdown2 = (
      <Dropdown overlay={menu2}>
        <a className="ant-dropdown-link" href="#">
          更多
        </a>
      </Dropdown>
    );
    return (
      <Col span={6} style={{ marginBottom: 16 }}>
        <Card title={item.name} extra={dropdown} style={{ width: '100%', height: 180 }}>
          <p>{item.describe}</p>
          <div>
            <Badge count={0} dot className={styles.badgeIcon}>
              <Icon type="play-square" className={styles.footIcon} style={{ color: '#0f0' }} />
              <span className={styles.spanSize}>5</span>
            </Badge>
            <Badge count={0} dot className={styles.badgeIcon}>
              <Icon type="stop" className={styles.footIcon} style={{ color: '#f00' }} />
              <span className={styles.spanSize}>5</span>
            </Badge>
            <Badge count={0} dot className={styles.badgeIcon}>
              <Icon type="close-square" className={styles.footIcon} />
              <span className={styles.spanSize}>5</span>
            </Badge>
            {dropdown2}
          </div>
        </Card>
      </Col>
    )
  }

  render() {
    // const {
    //   openKeys,
    //   theme,
    //   mode,
    //   location: { pathname },
    //   className,
    //   collapsed,
    // } = this.props;
    const appListData = [
      {
        name: '好应用会说话',
        describe: 'Unable to validate the access token.',
      },
      {
        name: '无法验证访问令牌',
        describe: 'UHover me, Click menu item.',
      },
      {
        name: '好应用会说话',
        describe: 'Unable to validate the access token.',
      },
      {
        name: '可以点击触发',
        describe: '支持 6 个弹出位置',
      },
      {
        name: '好应用会说话',
        describe: '默认是点击关闭菜单，可以关闭此功能.',
      },
      {
        name: 'Right Click on Me',
        describe: 'Menu.Item 必须设置唯一的 key 属性.',
      },
    ]
    const Carlist = [];
    appListData.forEach((item) => {
      Carlist.push(this.getCarList(item))
    });
    const { createAppVisible, editAppVisible, saveTempVisible } = this.state;

    return (
      <div>
        <Row gutter={16}>
          <Col span={3}>
            <Button type="primary" onClick={this.showCreateModal} className={styles.bottomSpace}>
              创建应用
            </Button>
          </Col>
          <Col span={11} />
          <Col span={9}>
            <ApplicationSearch />
          </Col>
          <Col span={1}>
            <SortApplication />
          </Col>
        </Row>
        <Row gutter={16}>
          {Carlist}
        </Row>
        <CreateApplication visible={createAppVisible} handleCreateCancel={this.handleCreateCancel} />
        <EditApplication visible={editAppVisible} handleEditCancel={this.handleEditCancel} />
        <SaveTemp visible={saveTempVisible} handleSaveCancel={this.handleSaveCancel} />
      </div>
    );
  }
}
