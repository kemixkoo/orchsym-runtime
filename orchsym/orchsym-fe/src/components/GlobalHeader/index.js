import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Icon, Alert } from 'antd';
import Debounce from 'lodash-decorators/debounce';
import styles from './index.less';
import RightContent from './RightContent';

@connect(({ login }) => ({
  leftDays: login.leftDays,
}))
class GlobalHeader extends PureComponent {
  componentWillMount() {
    const {
      dispatch,
    } = this.props;
    dispatch({
      type: 'user/fetchCurrent',
    });
    dispatch({
      type: 'login/fetchLicenseWarn',
    });
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
  render() {
    const { collapsed, leftDays } = this.props;
    const onClose = e => {
      console.log(e, 'I was closed.');
    };
    const showDays = leftDays < 30
    return (
      <div className={styles.header}>
        <span className={styles.trigger} onClick={this.toggle}>
          <Icon type={collapsed ? 'menu-unfold' : 'menu-fold'} />
        </span>
        {showDays ? (
          <Alert
            className={styles.alertBg}
            description={`License将在${leftDays}天后过期，为保证业务流程的运行正常，请及时更新，否则runtime将自动停止`}
            type={(leftDays < 7) ? 'error' : 'warning'}
            showIcon
            closable
            onClose={onClose}
          />
        ): (null)}
        <RightContent {...this.props} />
      </div>
    );
  }
}
export default GlobalHeader
