import React, { PureComponent } from 'react';
import { Card, Menu, Icon, Dropdown, Badge, Divider, Tag, List, Modal } from 'antd';
import { connect } from 'dva';
import styles from './AppList.less';
import SaveTemp from './SaveTemp';
import IconFont from '@/components/IconFont';
import LogList from '../LogList';
import CreateOrEditApp from './CreateOrEditApp';

const { confirm } = Modal;
@connect(({ application, loading }) => ({
  applicationList: application.applicationList,
  details: application.details,
  snippet: application.snippet,
  loading: loading.effects['application/fetchApplication'],
}))
class AppList extends PureComponent {
  state = {
    editVisible: null,
    createOrEdit: '编辑应用',
    appId: null,
    saveTempVisible: null,
    isError: true,
  };

  componentWillMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchApplication',
    });
  }

  showEditModal = (item) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchDetailApplication',
      payload: item.id,
    });
    this.setState({
      editVisible: true,
    });
  }

  handleCreateEditCancel = () => {
    this.setState({
      editVisible: false,
    })
  }

  showSaveTemp = (item) => {
    this.setState({
      saveTempVisible: true,
      appId: item.id,
      appItem: item,
    })
  }

  handleSaveCancel = () => {
    this.setState({
      saveTempVisible: false,
    })
  }

  // 状态操作
  updateStates = (item, status) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchUpdateAppState',
      payload: {
        id: item.id,
        state: status,
      },
    });
  }

  // 创建snippets
  createSnippets = (item, state) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchCreateSnippets',
      payload: item,
      cb: (res) => {
        if (state === 'RUNNING') { // 运行
          dispatch({
            type: 'application/fetchValidationRunApp',
            payload: {
              id: item.id,
              snippetId: res.id,
            },
          });
        } else if (state === 'COPE') { // 复制
          dispatch({
            type: 'application/fetchCopeApplication',
            payload: {
              id: item.id,
              snippetId: res.id,
            },
          });
        }
      },
    });
  }

  // 删除
  deleteAppHandel = (id) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchValidationDeleteApp',
      payload: id,
      cb: () => {
        confirm({
          title: '提示',
          content: '删除后不可撤销，确定删除所选组件或模块吗？',
          okText: 'Yes',
          okType: 'danger',
          cancelText: 'No',
          onOk() {
            dispatch({
              type: 'application/fetchCopeApplication',
              payload: id,
            });
          },
          onCancel() {
            console.log('Cancel');
          },
        });
      },
    });
  }

  getCarList = (item) => {
    // const { setAppParams } = this.props;
    // setAppParams(item.component.parentId);
    const menu = (
      <Menu>
        <Menu.Item key="1">
          <Icon type="appstore" />
          进入应用
        </Menu.Item>
        <Menu.Item key="2" onClick={() => { this.showEditModal(item) }}>
          <Icon type="edit" />
          编辑应用
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="3" onClick={() => { this.createSnippets(item, 'RUNNING') }}>
          <Icon type="play-square" />
          运行
        </Menu.Item>
        <Menu.Item key="4" onClick={() => { this.updateStates(item, 'STOPPED') }}>
          <Icon type="stop" />
          停止
        </Menu.Item>
        <Menu.Item key="5" onClick={() => { this.updateStates(item, 'ENABLED') }}>
          <Icon type="check-square" />
          启用
        </Menu.Item>
        <Menu.Item key="6" onClick={() => { this.updateStates(item, 'DISABLED') }}>
          <Icon type="close-square" />
          禁用
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="7" onClick={() => { this.createSnippets(item, 'COPE') }}>
          <Icon type="copy" />
          复制
        </Menu.Item>
        <Menu.Item key="8">
          <Icon type="download" />
          下载
        </Menu.Item>
        <Menu.Item key="9" onClick={() => { this.deleteAppHandel(item.id) }}>
          <Icon type="delete" />
          删除
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="10" onClick={() => { this.showSaveTemp(item) }}>
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
      <Menu style={{ fontSize: '12px' }}>
        <Menu.Item key="11">
          队列中
          <p className={styles.pStyle}>2（5.2 MB）</p>
        </Menu.Item>
        <Menu.Item key="12">
          输入
          <p className={styles.pStyle}>1（888.88 KB）</p>
        </Menu.Item>
        <Menu.Item key="13">
          输出
          <p className={styles.pStyle}>1（66 B）</p>
        </Menu.Item>
        <Menu.Item key="14">
          读 / 写
          <p className={styles.pStyle}>502 KB / 10.8 B</p>
        </Menu.Item>
      </Menu>
    );

    const dropdown2 = (
      <Dropdown overlay={menu2}>
        <div>
          <Badge count={0} dot className={styles.badgeIcon}>
            <Icon type="play-square" style={{ color: '#0f0' }} />
            <span>{item.runningCount}</span>
          </Badge>
          <Badge count={0} dot className={styles.badgeIcon}>
            <Icon type="stop" style={{ color: '#f00' }} />
            <span>{item.stoppedCount}</span>
          </Badge>
          <Badge count={0} dot className={styles.badgeIcon}>
            <Icon type="close-square" />
            <span>{item.disabledCount}</span>
          </Badge>
          {/* <a className="ant-dropdown-link" href="#">
            更多
          </a> */}
        </div>
      </Dropdown>
    );
    const { isError } = this.state;
    const isErrorCarName = isError ? `${styles.applicationCart} ${styles.errorApp}` : styles.applicationCart;
    return (
      // <Col xl={6} lg={6} md={12} sm={12} xs={24} style={{ marginBottom: 16 }}>
      <Card className={isErrorCarName} style={{ width: '100%', height: 165 }}>
        <Card.Meta
          title={
            <div>
              <IconFont type="iconAPIguanli" style={{ color: '#0087ff' }} className={styles.iconfont} />
              {/* <IconFont type="iconnaozhong" style={{ color: '#f28406' }} className={styles.iconfont} /> */}
              <span className={styles.cardTitle}>{item.status.name}</span>
            </div>}
          description={
            <p className={styles.lineEllipsis}>
              {(!item.describe) ? '该应用暂无描述' : item.describe}
            </p>
          }
        />
        <div className={styles.cardExtra}>{dropdown}</div>
        <div style={{ marginBottom: '10px' }}>
          {(!item.component.tags.length) ? '该应用暂无标签' : (item.component.tags.map((i) => (
            <Tag color="blue">{i}</Tag>
          )))}
        </div>
        <Divider style={{ margin: 0 }} />
        <div className={styles.cardFoot}>
          {dropdown2}
        </div>
        <p className={styles.cardTime}>{item.status.statsLastRefreshed}</p>
        {(isError) ? (
          <Dropdown overlay={<LogList />}>
            <span className={styles.triangle} />
          </Dropdown>
        ) : (null)}
      </Card>
      // </Col>
    );
  }

  render() {
    // const Carlist = [];
    // appListData.forEach((item) => {
    //   Carlist.push(this.getCarList(item))
    // });
    const { saveTempVisible, editVisible, createOrEdit, appId, appItem } = this.state;
    const { applicationList } = this.props;

    return (
      <div className={styles.infiniteContainer}>
        <List
          grid={{
            gutter: 16,
            xs: 1,
            sm: 2,
            md: 2,
            lg: 4,
            xl: 4,
            xxl: 4,
          }}
          dataSource={applicationList}
          renderItem={item => (
            <List.Item key={item.id}>
              {this.getCarList(item)}
            </List.Item>
          )}
        />
        {/* {Carlist} */}
        <SaveTemp visible={saveTempVisible} handleSaveCancel={this.handleSaveCancel} appItem={appItem} />
        <CreateOrEditApp
          visible={editVisible}
          handleCreateEditCancel={this.handleCreateEditCancel}
          title={createOrEdit}
          appId={appId}
        />
      </div>
    );
  }
}
export default AppList
