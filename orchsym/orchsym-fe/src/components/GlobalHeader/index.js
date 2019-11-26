import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Icon, Alert, Breadcrumb } from 'antd';
import Link from 'umi/link';
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
      dispatch({
        type: 'application/fetchDetailApplication',
        payload: processGroupId,
      });
    }
    // kc登录 刷新
    if (getToken()) {
      setInterval(() => {
        const oldJwt = localStorage.getItem('jwt');
        if (!oldJwt) {
          if (window.document.cookie > -1) {
            dispatch({
              type: 'login/fetchRefreshToken',
            });
          }
        }
        const activeTime = getExpiration(oldJwt) - (new Date()).getTime();
        const interval = 30;
        const refreshLeftTime = interval * 1000 * 1.5;
        if (activeTime <= refreshLeftTime) {
          dispatch({
            type: 'login/fetchRefreshToken',
          });
        }
      }, 60000);
    }
  }

  componentWillUnmount() {
    this.triggerResizeEvent.cancel();
  }


  /* eslint-disable*/
  @Debounce(600)
  triggerResizeEvent() {
    // eslint-disable-line
    const event = document.createEvent('HTMLEvents');
    event.initEvent('resize', true, false);
    window.dispatchEvent(event);
  }
  toggle = () => {
    const { collapsed, onCollapse } = this.props;
    onCollapse(!collapsed);
    this.triggerResizeEvent();
  };

  toGo = () => {
    router.push('/')
  };

  render() {
    const { pstyle, match, collapsed, leftDays, appDetails: { component }, componentName } = this.props;
    const { params } = match;
    const { processGroupId } = params;
    const onClose = e => {
      console.log(e, 'I was closed.');
    };
    const showDays = leftDays < 30
    const url = `/canvas/${processGroupId}`
    return (
      <div className={styles.header} style={pstyle}>

        {processGroupId ? (
          <span className={styles.trigger} onClick={this.toGo}>
            <Icon type="left" />
          </span>) : (<span className={styles.trigger} onClick={this.toggle}>
            <Icon type={collapsed ? 'menu-unfold' : 'menu-fold'} />
          </span>)
        }
        {processGroupId ? (<div className={styles.appPopover} ><AppPopover /></div>) : (null)}
        {processGroupId && component ? (
          <Breadcrumb separator=">>" style={{ display: 'inline-block' }}>
            <Breadcrumb.Item>
              {(processGroupId === component.id) && !componentName ? component.name :
                <Link to={url} target="_self">{component.name}</Link>
              }
            </Breadcrumb.Item>
            {componentName ? (<Breadcrumb.Item>
              {componentName}
            </Breadcrumb.Item>) : (null)}

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
