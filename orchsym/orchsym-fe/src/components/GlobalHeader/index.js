import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Icon, Alert, Breadcrumb } from 'antd';
// import Link from 'umi/link';
import router from 'umi/router'
import Debounce from 'lodash-decorators/debounce';
import styles from './index.less';
import RightContent from './RightContent';
import AppPopover from './AppPopover';
import { getToken } from '@/utils/authority';
import { getExpiration } from '@/utils/utils';

@connect(({ application, global, login }) => ({
  login,
  leftDays: global.leftDays,
  appDetails: application.appDetails,
  groupsBreadcrumb: global.groupsBreadcrumb,
}))
class GlobalHeader extends PureComponent {
  componentWillMount() {
    const { dispatch, match } = this.props;
    const { params } = match
    const { processGroupId } = params
    dispatch({
      type: 'user/fetchCurrent',
    });
    dispatch({
      type: 'global/fetchLicenseWarn',
    });
    dispatch({
      type: 'global/fetchGetClientId',
    });
    dispatch({
      type: 'global/fetchValidDownApp',
    });
    if (match && processGroupId) {
      // dispatch({
      //   type: 'application/fetchDetailApplication',
      //   payload: processGroupId,
      // });
      this.getBreadcrumb(processGroupId)
    }
    // kc登录 刷新
    if (getToken()) {
      this.poll()
    }
  }

  componentDidUpdate(prevProps, prevState) {
    const { componentId } = this.props;
    // 如果数据发生变化，则更新图表
    if ((prevProps.componentId !== componentId)) {
      this.getBreadcrumb(componentId)
    }
  }

  componentWillUnmount() {
    this.triggerResizeEvent.cancel();
  }

  poll = () => {
    const { dispatch } = this.props;
    const oldJwt = localStorage.getItem('jwt');
    if (!oldJwt) {
      if (window.document.cookie > -1) {
        dispatch({
          type: 'login/fetchRefreshToken',
        });
      }
      return
    }
    const activeTime = getExpiration(oldJwt) - (new Date()).getTime();
    const interval = 30;
    const refreshLeftTime = interval * 1000 * 1.5;
    if (activeTime <= refreshLeftTime) {
      dispatch({
        type: 'login/fetchRefreshToken',
      });
    }
    setTimeout(() => {
      this.poll();
    }, 30 * 1000);
  }

  /* eslint-disable*/
  @Debounce(600)
  triggerResizeEvent() {
    // eslint-disable-line
    const event = document.createEvent('HTMLEvents');
    event.initEvent('resize', true, false);
    window.dispatchEvent(event);
  }

  getBreadcrumb = (id) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'global/fetchBreadcrumb',
      payload: id,
    });
  }

  toggle = () => {
    const { collapsed, onCollapse } = this.props;
    onCollapse(!collapsed);
    this.triggerResizeEvent();
  };

  toGo = () => {
    // const { dispatch } = this.props
    // dispatch({
    //   type: 'application/appendValue',
    //   payload: {
    //     appDetails: {},
    //   },
    // });
    router.push('/')
  };

  showBreadcrumb = () => {
    const { groupsBreadcrumb, componentId, componentIdChange } = this.props
    if (groupsBreadcrumb) {
      if (groupsBreadcrumb.length > 4) {
        return (
          <span>
            <Breadcrumb.Item>
              <a onClick={() => (componentIdChange(groupsBreadcrumb[0].id))}>{groupsBreadcrumb[0].name}</a>
            </Breadcrumb.Item>
            <Breadcrumb.Item>
              <Icon type="ellipsis" key="ellipsis" />
            </Breadcrumb.Item>
            <Breadcrumb.Item>
              <a onClick={() => (componentIdChange(groupsBreadcrumb[groupsBreadcrumb.length - 2].id))}>{groupsBreadcrumb[groupsBreadcrumb.length - 2].name}</a>
            </Breadcrumb.Item>
            <Breadcrumb.Item>
              {groupsBreadcrumb[groupsBreadcrumb.length - 1].name}
            </Breadcrumb.Item>
          </span>
        )
      }
      return (
        groupsBreadcrumb.map(item => (<Breadcrumb.Item key={item.id}>
          {(componentId === item.id) ? item.name :
            <a onClick={() => (componentIdChange(item.id))} className={styles.breadcrumblink} >{item.name}</a>
          }
        </Breadcrumb.Item>))
      )

    }
    //   <Breadcrumb.Item>
    //     {(processGroupId === component.id) ? component.name :
    //       <Link to={`/canvas/${processGroupId}/0`} target="_self">{component.name}</Link>
    //     }
    //   </Breadcrumb.Item>)
  }

  render() {
    const { pstyle, match, collapsed, leftDays, groupsBreadcrumb, componentId, componentIdChange } = this.props; // appDetails: { component }, 
    const { params } = match;
    const { processGroupId } = params;
    const onClose = e => {
      console.log(e, 'I was closed.');
    };
    const showDays = leftDays < 30
    return (
      <div className={styles.header} style={pstyle}>

        {processGroupId ? (null
          // <span className={styles.trigger} onClick={this.toGo}>
          //   <Icon type="left" />
          // </span>
        ) : (<span className={styles.trigger} onClick={this.toggle}>
          <Icon type={collapsed ? 'menu-unfold' : 'menu-fold'} />
        </span>)
        }
        {processGroupId ? (
          <div className={styles.appPopover} ><AppPopover /></div>
        ) : (null)}
        {processGroupId ? (
          <Breadcrumb separator=">>" style={{ display: 'inline-block' }}>
            {this.showBreadcrumb()}
          </Breadcrumb>) : (null)
        }
        {
          showDays ? (
            <Alert
              className={styles.alertBg}
              description={`License将在${leftDays}天后过期，为保证业务流程的运行正常，请及时更新，否则runtime将自动停止`}
              type={(leftDays < 7) ? 'error' : 'warning'}
              showIcon
              closable
              onClose={onClose}
            />
          ) : (null)
        }
        <RightContent {...this.props} />
      </div >
    );
  }
}
export default GlobalHeader
