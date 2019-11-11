import React, { PureComponent } from 'react';
import { Card, Menu, Icon, Dropdown, Divider, Tag, List, Modal, Tooltip } from 'antd';
import { connect } from 'dva';
import { formatMessage } from 'umi-plugin-react/locale';
import router from 'umi/router'
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
  loading: loading.effects['application/fetchApplication'] || loading.effects['application/fetchValidationRunApp'],
}))
class AppList extends PureComponent {
  state = {
    editVisible: null,
    createOrEdit: formatMessage({ id: 'page.application.editApp' }),
    saveTempVisible: null,
    appItem: {},
    errorData: [],
  };

  componentWillMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchApplication',
    });
  }

  goToApp = (item) => {
    // console.log('item', item)
    router.push(`/canvas/${item.id}`)
  }

  doubleGoToApp = (item) => {
    router.push(`/canvas/${item.id}`)
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
      appItem: item,
    })
  }

  handleSaveCancel = () => {
    this.setState({
      saveTempVisible: false,
    })
  }

  // 状态操作
  updateStates = (item, state) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchUpdateAppState',
      payload: {
        id: item.id,
        state,
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
          title: formatMessage({ id: 'app.result.delete.title' }),
          content: formatMessage({ id: 'app.result.delete.description' }),
          okText: 'Yes',
          okType: 'warning',
          cancelText: 'No',
          onOk() {
            dispatch({
              type: 'application/fetchDeleteApplication',
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
    const { errorData } = this.state;
    const menu = (
      <Menu>
        <Menu.Item key="1" onClick={() => { this.goToApp(item) }}>
          <IconFont type="OS-iconi-jr" />
          {`${formatMessage({ id: 'page.application.content.intoApp' })}`}
        </Menu.Item>
        <Menu.Item key="2" onClick={() => { this.showEditModal(item) }}>
          <IconFont type="OS-iconbianji" />
          {`${formatMessage({ id: 'page.application.editApp' })}`}
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="3" onClick={() => { this.createSnippets(item, 'RUNNING') }}>
          <IconFont type="OS-iconqidong" />
          {`${formatMessage({ id: 'page.application.content.running' })}`}
        </Menu.Item>
        <Menu.Item key="4" onClick={() => { this.updateStates(item, 'STOPPED') }}>
          <IconFont type="OS-icontingzhi" />
          {`${formatMessage({ id: 'page.application.content.stop' })}`}
        </Menu.Item>
        <Menu.Item key="5" onClick={() => { this.updateStates(item, 'ENABLED') }}>
          <IconFont type="OS-iconqiyong" />
          {`${formatMessage({ id: 'page.application.content.enable' })}`}
        </Menu.Item>
        <Menu.Item key="6" onClick={() => { this.updateStates(item, 'DISABLED') }}>
          <IconFont type="OS-iconjinyong" />
          {`${formatMessage({ id: 'page.application.content.disable' })}`}
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="7" onClick={() => { this.createSnippets(item, 'COPE') }}>
          <IconFont type="OS-iconfuzhi" />
          {`${formatMessage({ id: 'page.application.content.cope' })}`}
        </Menu.Item>
        <Menu.Item key="8" disabled>
          <IconFont type="OS-iconCell-Download" />
          {`${formatMessage({ id: 'page.application.content.download' })}`}
        </Menu.Item>
        <Menu.Item key="9" onClick={() => { this.deleteAppHandel(item.id) }}>
          <IconFont type="OS-iconshanchu" />
          {`${formatMessage({ id: 'page.application.content.delete' })}`}
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="10" disabled onClick={() => { this.showSaveTemp(item) }}>
          <IconFont type="OS-iconmoban" />
          {`${formatMessage({ id: 'page.application.content.saveTemp' })}`}
        </Menu.Item>
      </Menu>
    );

    const dropdown = (
      <Dropdown overlay={menu} trigger={['click']}>
        <a className="ant-dropdown-link" href="#">
          <Icon type="more" />
        </a>
      </Dropdown>
    );

    const menu2 = (
      <Menu style={{ fontSize: '12px' }}>
        <Menu.Item key="11">
          {`${formatMessage({ id: 'page.application.content.queued' })}`}
          <p className={styles.pStyle}>{item.status.aggregateSnapshot.queued}</p>
        </Menu.Item>
        {/* <Menu.Item key="12">
          输入
          <p className={styles.pStyle}>{item.status.aggregateSnapshot.input}&rarr;{item.inputPortCount}</p>
        </Menu.Item>
        <Menu.Item key="13">
          输出
          <p className={styles.pStyle}>{item.outputPortCount}&rarr;{item.status.aggregateSnapshot.output}</p>
        </Menu.Item> */}
        <Menu.Item key="14">
          {`${formatMessage({ id: 'page.application.content.readWrite' })}`}
          <p className={styles.pStyle}>{item.status.aggregateSnapshot.read} / {item.status.aggregateSnapshot.written}</p>
        </Menu.Item>
      </Menu>
    );

    const dropdown2 = (
      <Dropdown overlay={menu2}>
        <div>
          <span className={styles.badgeIcon}>
            <IconFont type="OS-iconqidong" />
            <span>{item.runningCount}</span>
          </span>
          <span className={styles.badgeIcon}>
            <IconFont type="OS-icontingzhi" />
            <span>{item.stoppedCount}</span>
          </span>
          <span className={styles.badgeIcon}>
            <IconFont type="OS-iconicon" />
            <span>{item.invalidCount}</span>
          </span>
          {/* <a className="ant-dropdown-link" href="#">
            更多
          </a> */}
        </div>
      </Dropdown>
    );
    const handleVisibleChange = flag => {
      if (flag) {
        this.setState({
          errorData: item.bulletins,
        });
      } else {
        this.setState({
          errorData: [],
        });
      }
    };

    const isError = item.bulletins.length > 0
    const isErrorCarName = isError ? `${styles.applicationCart} ${styles.errorApp}` : styles.applicationCart;
    return (
      // <Col xl={6} lg={6} md={12} sm={12} xs={24} style={{ marginBottom: 16 }}>
      <Card onDoubleClick={() => { this.doubleGoToApp(item) }} className={isErrorCarName} style={{ width: '100%', height: 165 }}>
        <Card.Meta
          title={
            <div>
              <IconFont type="OS-iconapi" style={{ fontSize: '20px', verticalAlign: 'baseline' }} />
              {/* <IconFont type="OS-icondingshirenwu" style={{ fontSize: '20px' }} /> */}
              <Tooltip title={item.component.name}>
                <span className={styles.cardTitle}>
                  {item.component.name}
                </span>
              </Tooltip>
            </div>}
          description={
            <Tooltip title={item.component.comments}>
              <p className={styles.lineEllipsis}>
                {(!item.component.comments) ? '该应用暂无描述' : item.component.comments}
              </p>
            </Tooltip>
          }
        />
        <div className={styles.cardExtra}>{dropdown}</div>
        <div className={styles.lineEllipsis} style={{ marginBottom: '10px' }}>
          {(!item.component.tags.length) ? '该应用暂无标签' : (item.component.tags.map((i) => (
            <Tag color="blue">{i}</Tag>
          )))}
        </div>
        <Divider style={{ margin: 0 }} />
        <div className={styles.cardFoot}>
          {dropdown2}
        </div>
        {/* <p className={styles.cardTime}>{this.formatMsgTime(item.status.statsLastRefreshed)}</p> */}
        {(isError) ? (
          <Dropdown trigger={['click']} onVisibleChange={handleVisibleChange} overlay={<LogList errorList={errorData} />}>
            <span className={styles.triangle} />
          </Dropdown>
        ) : (null)}
      </Card>
      // </Col>
    );
  }

  formatMsgTime = (timeSpan) => {
    let time = timeSpan.split(' ')[0];
    time = time.replace(/-/g, ':').replace(' ', ':');
    time = time.split(':');
    const dateTime = new Date(2019, 7, 12, time[0], time[1], time[2]);
    const year = dateTime.getFullYear();
    const month = dateTime.getMonth() + 1;
    const day = dateTime.getDate();
    const hour = dateTime.getHours();
    const minute = dateTime.getMinutes();
    // const second = dateTime.getSeconds();
    const now = new Date().getTime();
    const timeOld = new Date(2019, 7, 12, time[0], time[1], time[2]).getTime();
    let milliseconds = 0;
    let timeSpanStr;
    milliseconds = now - timeOld;
    if (milliseconds <= 1000 * 60 * 1) {
      timeSpanStr = '刚刚';
    } else if (1000 * 60 * 1 < milliseconds && milliseconds <= 1000 * 60 * 60) {
      timeSpanStr = `${Math.round((milliseconds / (1000 * 60)))}分钟前`;
    } else if (1000 * 60 * 60 * 1 < milliseconds && milliseconds <= 1000 * 60 * 60 * 24) {
      timeSpanStr = `${Math.round(milliseconds / (1000 * 60 * 60))}小时前`;
    } else if (1000 * 60 * 60 * 24 < milliseconds && milliseconds <= 1000 * 60 * 60 * 24 * 15) {
      timeSpanStr = `${Math.round(milliseconds / (1000 * 60 * 60 * 24))}天前`;
    } else if (milliseconds > 1000 * 60 * 60 * 24 * 15 && year === (new Date()).getFullYear()) {
      timeSpanStr = `${month}-${day} ${hour}:${minute}`;
    } else {
      timeSpanStr = `${year}-${month}-${day} ${hour}:${minute}`;
    }
    return timeSpanStr;
  };

  render() {
    const { saveTempVisible, editVisible, createOrEdit, appItem } = this.state;
    const { applicationList, details, loading } = this.props;

    return (
      <div className={styles.infiniteContainer}>
        <List
          loading={loading}
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
        <SaveTemp visible={saveTempVisible} handleSaveCancel={this.handleSaveCancel} appItem={appItem} />
        {
          editVisible && (
            <CreateOrEditApp
              visible={editVisible}
              handleCreateEditCancel={this.handleCreateEditCancel}
              title={createOrEdit}
              details={details}
            />
          )
        }
      </div>
    );
  }
}
export default AppList
