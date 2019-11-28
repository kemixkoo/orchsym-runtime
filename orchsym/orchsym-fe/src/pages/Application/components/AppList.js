/* eslint-disable react/prop-types */
import React, { PureComponent } from 'react';
import {
  Popover, Card, Menu, Icon, Dropdown, Divider,
  Tag, List, Modal, Tooltip, message,
} from 'antd';
import { connect } from 'dva';
import { cloneDeep } from 'lodash'
import { formatMessage, getLocale } from 'umi-plugin-react/locale';
import router from 'umi/router'
import moment from 'moment';
import styles from './AppList.less';
import SaveTemp from './SaveTemp';
import IconFont from '@/components/IconFont';
import LogList from '@/components/LogList';
import CreateOrEditApp from './CreateOrEditApp';

const { confirm } = Modal;
@connect(({ global, application, loading }) => ({
  canDownLoad: global.canDownLoad,
  applicationList: application.applicationList,
  Details: application.Details,
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
    editOrCope: '',
  };

  componentWillMount() {
    const { pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    this.getAppList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
  }

  componentDidUpdate(prevProps, prevState) {
    const { onSearchChange, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    // 如果数据发生变化，则更新图表
    if ((prevProps.searchVal !== searchVal) || (prevProps.sortedField !== sortedField) || (prevProps.isDesc !== isDesc)) {
      this.getAppList(1, pageSizeNum, sortedField, isDesc, searchVal)
      onSearchChange({
        pageNum: 1,
      })
    }
  }

  getAppList = (page, pageSize, sortedField, isDesc, q) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchApplication',
      payload: {
        page,
        pageSize,
        isDetail: true,
        q,
        sortedField,
        isDesc,
      },
    });
  }

  goToApp = (item) => {
    // console.log('item', item)
    router.push(`/canvas/${item.id}`)
  }

  doubleGoToApp = (item) => {
    item.component.additions && item.component.additions.IS_ENABLED === 'true' && router.push(`/canvas/${item.id}`)
  }

  showEditModal = (item, state) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchDetailApplication',
      payload: item.id,
    });
    this.setState({
      editVisible: true,
      editOrCope: state,
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
    const { dispatch, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props;
    if (state === 'ENABLED') {
      dispatch({
        type: 'application/fetchUpdateEnable',
        payload: item.id,
        cb: () => {
          message.success(formatMessage({ id: 'result.success' }));
          this.getAppList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
        },
      });
    } else if (state === 'DISABLED') {
      dispatch({
        type: 'application/fetchUpdateDisable',
        payload: item.id,
        cb: () => {
          message.success(formatMessage({ id: 'result.success' }));
          this.getAppList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
        },
      });
    } else {
      dispatch({
        type: 'application/fetchUpdateAppState',
        payload: {
          id: item.id,
          state,
        },
        cb: () => {
          message.success(formatMessage({ id: 'result.success' }));
          this.getAppList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
        },
      });
    }
  }

  // 创建snippets
  createSnippets = (item, state) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchcreateSnippets',
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
        }
      },
    });
  }
  // else if (state === 'COPE') { // 复制
  //   dispatch({
  //     type: 'application/fetchCopeApplication',
  //     payload: {
  //       id: item.id,
  //       snippetId: res.id,
  //     },
  //   });
  // }

  // 删除
  deleteAppHandel = (id) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchValidationDeleteApp',
      payload: id,
      cb: (res) => {
        const that = this
        if (res.canDelete) {
          confirm({
            title: formatMessage({ id: 'application.delete.title' }),
            content: formatMessage({ id: 'application.delete.description' }),
            okText: 'Yes',
            okType: 'warning',
            cancelText: 'No',
            onOk() {
              that.deleteFetch(id)
            },
            onCancel() {
              console.log('Cancel');
            },
          });
        } else {
          confirm({
            title: formatMessage({ id: 'application.delete.title' }),
            content: formatMessage({ id: 'application.delete.description' }),
            okText: 'Yes',
            okType: 'warning',
            cancelText: 'No',
            onOk() {
              that.deleteFetch(id)
            },
            onCancel() {
              console.log('Cancel');
            },
          });
        }
      },
    });
  }

  deleteFetch = (id) => {
    const { dispatch, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props;
    dispatch({
      type: 'application/fetchDeleteApplication',
      payload: id,
      cb: () => {
        message.success(formatMessage({ id: 'result.success' }));
        this.getAppList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
      },
    })
  }

  // 下载
  downloadApp = (appId, name) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchDownloadApp',
      payload: {
        id: appId,
        name,
      },
    });
  }

  getCarList = (item, index) => {
    const { errorData } = this.state;
    const { canDownLoad } = this.props;
    const menu = item.component.additions && item.component.additions.IS_ENABLED === 'true' ? (
      <Menu>
        <Menu.Item key="1" onClick={() => { this.goToApp(item) }}>
          <IconFont type="OS-iconi-jr" />
          {`${formatMessage({ id: 'page.application.content.intoApp' })}`}
        </Menu.Item>
        <Menu.Item key="2" onClick={() => { this.showEditModal(item, 'EDIT') }}>
          <IconFont type="OS-iconbianji" />
          {`${formatMessage({ id: 'page.application.editApp' })}`}
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="3" disabled={!item.canRun} onClick={() => { this.createSnippets(item, 'RUNNING') }}>
          <IconFont type="OS-iconqidong" />
          {`${formatMessage({ id: 'page.application.content.running' })}`}
        </Menu.Item>
        <Menu.Item key="4" disabled={!item.canStop} onClick={() => { this.updateStates(item, 'STOPPED') }}>
          <IconFont type="OS-icontingzhi" />
          {`${formatMessage({ id: 'page.application.content.stop' })}`}
        </Menu.Item>
        <Menu.Item key="5" disabled={!item.canEnable} onClick={() => { this.updateStates(item, 'ENABLED') }}>
          <IconFont type="OS-iconqiyong" />
          {`${formatMessage({ id: 'button.enable' })}`}
        </Menu.Item>
        <Menu.Item key="6" disabled={!item.canDisable} onClick={() => { this.updateStates(item, 'DISABLED') }}>
          <IconFont type="OS-iconjinyong" />
          {`${formatMessage({ id: 'button.disable' })}`}
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="7" onClick={() => { this.showEditModal(item, 'COPE') }}>
          <IconFont type="OS-iconfuzhi" />
          {`${formatMessage({ id: 'page.application.content.cope' })}`}
        </Menu.Item>
        <Menu.Item key="8" disabled={!canDownLoad} onClick={() => { this.downloadApp(item.id, item.component.name) }}>
          <IconFont type="OS-iconCell-Download" />
          {`${formatMessage({ id: 'button.download' })}`}
        </Menu.Item>
        <Menu.Item key="9" onClick={() => { this.deleteAppHandel(item.id) }}>
          <IconFont type="OS-iconshanchu" />
          {`${formatMessage({ id: 'button.delete' })}`}
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="10" onClick={() => { this.showSaveTemp(item) }}>
          <IconFont type="OS-iconmoban" />
          {`${formatMessage({ id: 'title.saveTemp' })}`}
        </Menu.Item>
      </Menu>
    ) : (
      <Menu>
        <Menu.Item key="5" disabled={!item.canEnable} onClick={() => { this.updateStates(item, 'ENABLED') }}>
          <IconFont type="OS-iconqiyong" />
          {`${formatMessage({ id: 'button.enable' })}`}
        </Menu.Item>
        <Menu.Item key="9" onClick={() => { this.deleteAppHandel(item.id) }}>
          <IconFont type="OS-iconshanchu" />
          {`${formatMessage({ id: 'button.delete' })}`}
        </Menu.Item>
      </Menu>
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

    const appMenuChange = visible => {
      const { applicationList, dispatch } = this.props
      if (visible) {
        dispatch({
          type: 'application/fetchIsShowStatus',
          payload: item.id,
          cb: (res) => {
            const resultsCopy = cloneDeep(applicationList)
            Object.assign(resultsCopy.results[index], res)
            dispatch({
              type: 'application/appendValue',
              payload: {
                applicationList: resultsCopy,
              },
            })
          },
        });
      }
    };

    const formatMsgTime = (timeSpan) => {
      let timeSpanStr;
      if (timeSpan) {
        timeSpanStr = moment(Number(timeSpan)).fromNow();
      }
      // time = time.replace(/-/g, ':').replace(' ', ':');
      // time = time.split(':');
      // const dateTime = new Date(2019, 7, 12, time[0], time[1], time[2]);
      // const year = dateTime.getFullYear();
      // const month = dateTime.getMonth() + 1;
      // const day = dateTime.getDate();
      // const hour = dateTime.getHours();
      // const minute = dateTime.getMinutes();
      // // const second = dateTime.getSeconds();
      // const now = new Date().getTime();
      // const timeOld = new Date(2019, 7, 12, time[0], time[1], time[2]).getTime();
      // let milliseconds = 0;
      // moment("20120620", "YYYYMMDD").fromNow(); // 7 年前
      // moment().startOf('day').fromNow();        // 15 小时前
      // moment().endOf('day').fromNow();          // 9 小时内
      // moment(Number(timeSpan)).startOf('hour').fromNow();       // 33 分钟前
      // milliseconds = now - timeOld;
      // if (milliseconds <= 1000 * 60 * 1) {
      //   timeSpanStr = '刚刚';
      // } else if (1000 * 60 * 1 < milliseconds && milliseconds <= 1000 * 60 * 60) {
      //   timeSpanStr = `${Math.round((milliseconds / (1000 * 60)))}分钟前`;
      // } else if (1000 * 60 * 60 * 1 < milliseconds && milliseconds <= 1000 * 60 * 60 * 24) {
      //   timeSpanStr = `${Math.round(milliseconds / (1000 * 60 * 60))}小时前`;
      // } else if (1000 * 60 * 60 * 24 < milliseconds && milliseconds <= 1000 * 60 * 60 * 24 * 15) {
      //   timeSpanStr = `${Math.round(milliseconds / (1000 * 60 * 60 * 24))}天前`;
      // } else if (milliseconds > 1000 * 60 * 60 * 24 * 15 && year === (new Date()).getFullYear()) {
      //   timeSpanStr = `${month}-${day} ${hour}:${minute}`;
      // } else {
      //   timeSpanStr = `${year}-${month}-${day} ${hour}:${minute}`;
      // }
      return timeSpanStr;
    };
    const tagContent = <div className={styles.tagContent}>{item.component.tags.map((i) => (<Tag color="blue" key={i}>{i}</Tag>))}</div>
    const isError = item.bulletins.length > 0
    const isErrorCarName = isError ? `${styles.errorApp}` : ''
    const isDownApp = item.component.additions && item.component.additions.IS_ENABLED === 'true' ? '' : `${styles.disableApp}`
    return (
      <Card onDoubleClick={() => { this.doubleGoToApp(item) }} className={`${styles.applicationCart} ${isErrorCarName} ${isDownApp}`} style={{ width: '100%', height: 165 }}>
        <Card.Meta
          title={
            <div>
              <IconFont type="OS-iconapi" style={{ fontSize: '20px', verticalAlign: 'baseline' }} />
              {/* <IconFont type="OS-icondingshirenwu" style={{ fontSize: '20px' }} /> */}
              <Tooltip title={item.component.name} placement="topLeft">
                <span className={styles.cardTitle}>
                  {item.component.name}
                </span>
              </Tooltip>
            </div>}
          description={
            <Tooltip title={item.component.comments} placement="topLeft">
              <p className={styles.lineEllipsis}>
                {(!item.component.comments) ? '该应用暂无描述' : item.component.comments}
              </p>
            </Tooltip>
          }
        />
        <div className={styles.cardExtra}>
          <Dropdown overlay={menu} trigger={['click']} onVisibleChange={appMenuChange}>
            <a className="ant-dropdown-link" href="#">
              <Icon type="more" />
            </a>
          </Dropdown>
        </div>
        <div style={{ marginBottom: '10px' }}>
          {(!item.component.tags.length) ? '该应用暂无标签' :
            (<Popover placement="topLeft" content={tagContent}><div className={styles.lineEllipsis}>{(item.component.tags.map((i) => (<Tag color="blue" key={i}>{i}</Tag>)))}</div></Popover>)
          }
        </div>
        <Divider style={{ margin: 0 }} />
        <div className={styles.cardFoot}>
          {dropdown2}
        </div>
        <p className={styles.cardTime}>{formatMsgTime(item.component.additions.MODIFIED_TIMESTAMP)}</p>
        {
          (isError) ? (
            <Dropdown trigger={['click']} onVisibleChange={handleVisibleChange} overlay={<LogList errorList={errorData} />}>
              <span className={styles.triangle} />
            </Dropdown>
          ) : (null)
        }
      </Card>
    );
  }

  render() {
    const { saveTempVisible, editVisible, createOrEdit, appItem, editOrCope } = this.state;
    const { applicationList: { results, totalSize }, appDetails, loading,
      onSearchChange, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props;

    const showTotal = (total) => {
      if (getLocale() === 'zh-CN') {
        return `共 ${total} 个`;
      } else {
        return `Total ${total} items`;
      }
    }

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
            xxl: 6,
          }}
          dataSource={results}
          renderItem={(item, index) => (
            <List.Item key={item.id}>
              {this.getCarList(item, index)}
            </List.Item>
          )}
          pagination={{
            onChange: (page, pageSize) => {
              this.getAppList(page, pageSize, sortedField, isDesc, searchVal)
              onSearchChange({
                pageNum: page,
                pageSizeNum: pageSize,
              })
            },
            onShowSizeChange: (current, size) => {
              this.getAppList(current, size, sortedField, isDesc, searchVal)
              onSearchChange({
                pageNum: current,
                pageSizeNum: size,
              })
            },
            current: pageNum,
            pageSize: pageSizeNum,
            total: totalSize,
            showTotal,
            pageSizeOptions: ['12', '24'],
            showSizeChanger: true,
            showQuickJumper: true,

          }}
        />
        <SaveTemp visible={saveTempVisible} handleSaveCancel={this.handleSaveCancel} appItem={appItem} />
        {
          editVisible && (
            <CreateOrEditApp
              editOrCope={editOrCope}
              visible={editVisible}
              handleCreateEditCancel={this.handleCreateEditCancel}
              title={createOrEdit}
              details={appDetails}
              onSearchChange={this.onSearchChange}
              pageNum={pageNum}
              pageSizeNum={pageSizeNum}
              searchVal={searchVal}
              sortedField={sortedField}
              isDesc={isDesc}
            />
          )
        }
      </div>
    );
  }
}
export default AppList
