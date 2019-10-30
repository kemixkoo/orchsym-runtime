import React, { PureComponent } from 'react';
import { connect } from 'dva';
import Link from 'umi/link';
import { debounce } from 'lodash'
import { Popover, Icon, Input, Menu, Spin } from 'antd';
import IconFont from '@/components/IconFont';
import Ellipsis from '@/components/Ellipsis';
import styles from './index.less';

@connect(({ canvas, loading }) => ({
  applicationList: canvas.applicationList,
  loading:
    loading.effects['canvas/fetchApplication'],
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
    searchValue: '',
  };

  // 搜索
  onSearchChange = e => {
    this.setState({
      searchValue: e.target.value,
    });
    this.doSearchAjax(e.target.value)
  }

  doSearchAjax = value => {
    const { dispatch, applicationList } = this.props;
    if (value) {
      const list = []
      applicationList.forEach(item => {
        if (item.component.name.indexOf(value) > 0) {
          list.push(item)
        }
      })
      this.setState({
        appList: list,
      });
    } else {
      dispatch({
        type: 'canvas/fetchApplication',
        cb: (res) => {
          this.setState({
            appList: res,
          });
        },
      });
    }
  }

  handleVisibleChange = visible => {
    const { dispatch } = this.props;
    this.setState({ visible });
    if (visible) {
      dispatch({
        type: 'canvas/fetchApplication',
        cb: (res) => {
          this.setState({
            appList: res,
          });
        },
      });
    } else {
      this.setState({
        searchValue: '',
      });
    }
  };

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


  render() {
    const { loading } = this.props;
    const { visible, onMouseId, appList, searchValue } = this.state;

    const appMenu = (
      <Spin spinning={loading || false}>
        <Menu className={styles.appMenu}>
          {appList && appList.length > 0 ? (appList.map(item => (
            <Menu.Item
              key={item.id}
              onMouseEnter={() => this.handleEnter(item.id)}
              onMouseLeave={() => this.handleLeave(item.id)}
            >
              <Link to={`/canvas/${item.id}`} target="blank">
                <IconFont type="OS-iconapi" />
                <Ellipsis tooltip length={13}>
                  { item.component.name }
                </Ellipsis>
                {onMouseId === item.id ?
                  (<span className={styles.appMenuIcon}><IconFont type="OS-iconai37" /></span>)
                  : (null)}
              </Link>
            </Menu.Item>
          ))) : (<div style={{ textAlign: 'center' }}>暂无数据</div>)}
        </Menu>
      </Spin>
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
          <p className={styles.title}>全部</p>
          {appMenu}
        </div>
      </div>
    );

    return (
      <Popover
        placement="bottomLeft"
        content={content}
        trigger="click"
        visible={visible}
        onVisibleChange={this.handleVisibleChange}
      >
        <Icon type="caret-down" style={{ padding: '0 10px' }} />
      </Popover>
    );
  }
}
export default AppPopover
