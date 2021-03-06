import React, { PureComponent } from 'react';
import { connect } from 'dva';
import Link from 'umi/link';
import router from 'umi/router'
import { debounce } from 'lodash'
import { formatMessage } from 'umi-plugin-react/locale';
import { Popover, Icon, Input, Menu, Spin, Button } from 'antd';
import IconFont from '@/components/IconFont';
import Ellipsis from '@/components/Ellipsis';
import styles from './index.less';

const ButtonGroup = Button.Group;
@connect(({ application, loading }) => ({
  loading:
    loading.effects['application/fetchApplication'],
}))
class AppPopover extends PureComponent {
  constructor() {
    super()
    this.doSearchAjax = debounce(this.doSearchAjax, 500)
  }

  state = {
    visible: false,
    onMouseId: '',
    appList: [],
    topList: [],
    searchValue: '',
  };

  componentDidUpdate(prevProps, prevState) {
    const { closePop, changeState } = this.props;
    // 如果数据发生变化，则更新图表
    if ((prevProps.closePop !== closePop)) {
      this.handleVisibleChange(false)
      changeState({ closePop: false })
    }
  }

  // 搜索
  onSearchChange = e => {
    this.setState({
      searchValue: e.target.value,
    });
    this.doSearchAjax(e.target.value)
  }

  doSearchAjax = value => {
    if (value) {
      this.setState({
        topList: [],
      });
      this.fetchApplication(value, 'name', 'false', -1)
    } else {
      this.fetchApplication('', 'modifiedTime', 'true', 3)
      this.fetchApplication('', 'name', 'false', -1)
    }
  }

  handleVisibleChange = visible => {
    this.setState({ visible });
    if (visible) {
      this.fetchApplication('', 'modifiedTime', 'true', 3)
      this.fetchApplication('', 'name', 'false', -1)
    } else {
      this.setState({
        searchValue: '',
      });
    }
  };

  fetchApplication = (q, sortedField, isDesc, pageSize) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchApplication',
      payload: {
        q,
        sortedField,
        isDesc,
        page: 1,
        pageSize,
      },
      cb: (res) => {
        if (sortedField === 'modifiedTime') {
          this.setState({
            topList: res.results,
          });
        } else {
          this.setState({
            appList: res.results,
          });
        }
      },
    });
  }

  handleEnter = id => {
    this.setState({
      onMouseId: id,
    });
  };

  handleLeave = id => {
    this.setState({
      onMouseId: '',
    });
  };


  toGo = () => {
    router.push('/')
  };

  render() {
    const { loading, componentIdChange } = this.props;
    const { visible, onMouseId, appList, searchValue, topList } = this.state;

    const appMenu = (
      <Spin spinning={loading || false}>
        <Menu className={styles.appMenu}>
          {appList && appList.length > 0 ? (appList.map(item => (
            <Menu.Item
              selectable={false}
              key={item.id}
              onMouseEnter={() => this.handleEnter(item.id)}
              onMouseLeave={() => this.handleLeave(item.id)}
            >
              {/* <Link to={`/canvas/${item.id}/0`} target="_self"> */}
              <span onClick={() => (componentIdChange(item.id))}>
                <IconFont type="OS-iconapi" />
                <Ellipsis tooltip length={13}>
                  {item.name}
                </Ellipsis>
              </span>
              {/* </Link> */}
              {onMouseId === item.id ?
                (<span className={styles.appMenuIcon}><Link to={`/canvas/${item.id}/0`} target="_blank"><IconFont type="OS-iconai37" /></Link></span>)
                : (null)}

            </Menu.Item>
          ))) : (<div style={{ textAlign: 'center' }}>{`${formatMessage({ id: 'result.empty' })}`}</div>)}
        </Menu>
      </Spin>
    );
    const topMenu = (
      <Menu className={styles.appMenu}>
        {topList.map(item => (
          <Menu.Item
            selectable={false}
            key={item.id}
            onMouseEnter={() => this.handleEnter(item.id)}
            onMouseLeave={() => this.handleLeave(item.id)}
          >
            {/* <Link to={`/canvas/${item.id}/0`} target="_self"> */}
            <span onClick={() => (componentIdChange(item.id))}>
              <IconFont type="OS-iconapi" />
              <Ellipsis tooltip length={13}>
                {item.name}
              </Ellipsis>
            </span>
            {/* </Link> */}
            {onMouseId === item.id ?
              (<span className={styles.appMenuIcon}><Link to={`/canvas/${item.id}/0`} target="_blank"><IconFont type="OS-iconai37" /></Link></span>)
              : (null)}

          </Menu.Item>
        ))}
      </Menu>
    );
    const content = (
      <div className={styles.appPopoverWrapper}>
        <div className={styles.searchInput}>
          <Input
            placeholder="搜索应用"
            value={searchValue}
            onChange={this.onSearchChange}
            allowClear
            prefix={<Icon type="search" style={{ color: 'rgba(0,0,0,.25)' }} />}
          />
        </div>
        <div className={styles.listScrollbar}>
          {topList && topList.length > 0 ? (<div><p className={styles.title}>最近</p>{topMenu}<p className={styles.title}>全部</p></div>) : (null)}
          {appMenu}
        </div>
      </div>
    );
    return (
      <div className={styles.buttonGroups}>
        <ButtonGroup>
          <Button onClick={this.toGo}><Icon type="left" /></Button>
          <Popover
            placement="bottomLeft"
            content={content}
            trigger="click"
            visible={visible}
            onVisibleChange={this.handleVisibleChange}
          >
            <Button style={{ width: '30px', padding: 0 }}><Icon type="caret-down" /></Button>
          </Popover>
        </ButtonGroup>
      </div>

    );
  }
}
export default AppPopover
