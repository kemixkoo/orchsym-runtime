import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Icon, Alert, Breadcrumb } from 'antd';
import Link from 'umi/link';
import router from 'umi/router'
import Debounce from 'lodash-decorators/debounce';
import styles from './index.less';
import RightContent from './RightContent';

@connect(({ login, canvas }) => ({
  leftDays: login.leftDays,
  appDetails: canvas.appDetails,
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
      type: 'login/fetchLicenseWarn',
    });
    if (match && processGroupId) {
      dispatch({
        type: 'canvas/fetchDetailApplication',
        payload: processGroupId,
      });
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
    const { pstyle, match, collapsed, leftDays, appDetails: { component } } = this.props;
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

        {processGroupId && component ? (
          <Breadcrumb separator=">>" style={{ display: 'inline-block' }}>
            <Breadcrumb.Item>
              {processGroupId === component.id ? component.name :
                <Link to={url}>{component.name}</Link>
              }
            </Breadcrumb.Item>
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
