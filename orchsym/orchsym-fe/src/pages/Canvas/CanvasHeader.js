import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { formatMessage } from 'umi-plugin-react/locale';
import { Layout, message } from 'antd';
import { connect } from 'dva';
import router from 'umi/router';
import GlobalHeader from '@/components/GlobalHeader';
import TopNavHeader from '@/components/TopNavHeader';

const { Header } = Layout;

class HeaderView extends Component {
  state = {
    visible: true,
  };

  static getDerivedStateFromProps(props, state) {
    if (!props.autoHideHeader && !state.visible) {
      return {
        visible: true,
      };
    }
    return null;
  }

  componentDidMount() {
    document.addEventListener('scroll', this.handScroll, { passive: true });
  }

  componentWillUnmount() {
    document.removeEventListener('scroll', this.handScroll);
  }

  getHeadWidth = () => {
    const { collapsed, setting } = this.props;
    const { fixedHeader, layout } = setting;
    if (!fixedHeader || layout === 'topmenu') {
      return '100%';
    }
    return collapsed ? 'calc(100%)' : 'calc(100%)';
  };

  handleNoticeClear = type => {
    message.success(
      `${formatMessage({ id: 'component.noticeIcon.cleared' })} ${formatMessage({
        id: `component.globalHeader.${type}`,
      })}`
    );
    const { dispatch } = this.props;
    dispatch({
      type: 'global/clearNotices',
      payload: type,
    });
  };

  handleMenuClick = ({ key }) => {
    const { dispatch } = this.props;
    if (key === 'userCenter') {
      router.push('/account/center');
      return;
    }
    if (key === 'triggerError') {
      router.push('/exception/trigger');
      return;
    }
    if (key === 'userinfo') {
      router.push('/account/settings/base');
      return;
    }
    if (key === 'logout') {
      dispatch({
        type: 'login/logout',
      });
    }
  };

  handleNoticeVisibleChange = visible => {
    if (visible) {
      const { dispatch } = this.props;
      dispatch({
        type: 'global/fetchNotices',
      });
    }
  };

  handScroll = () => {
    const { autoHideHeader } = this.props;
    const { visible } = this.state;
    if (!autoHideHeader) {
      return;
    }
    const scrollTop = document.body.scrollTop + document.documentElement.scrollTop;
    if (!this.ticking) {
      this.ticking = true;
      requestAnimationFrame(() => {
        if (this.oldScrollTop > scrollTop) {
          this.setState({
            visible: true,
          });
        } else if (scrollTop > 300 && visible) {
          this.setState({
            visible: false,
          });
        } else if (scrollTop < 300 && !visible) {
          this.setState({
            visible: true,
          });
        }
        this.oldScrollTop = scrollTop;
        this.ticking = false;
      });
    }
  };

  render() {
    const { handleMenuCollapse, setting } = this.props;
    const { navTheme, layout } = setting;
    const { visible } = this.state;
    /**
     * 在 defaultSettings.js 里的 layout 字段，值可以是 sidemenu 或者 topmenu
     * 切换导航 tab 的显示模式
     * 默认导航 tab 在左侧，即 sidemenu 模式。 isTop 为 false，渲染的是 GlobalHeader 组件
     */
    const borderStyle = {
      borderBottom: '1px solid #eee',
      boxShadow: '0 0 0 rgba(0, 21, 41, 0)',
    };
    const isTop = layout === 'topmenu';
    const width = this.getHeadWidth();
    const HeaderDom = visible ? (
      <Header
        style={{ padding: 0, width, zIndex: 4 }}
        // className={fixedHeader ? styles.fixedHeader : ''}
      >
        {isTop ? (
          <TopNavHeader
            theme={navTheme}
            mode="horizontal"
            onCollapse={handleMenuCollapse}
            onNoticeClear={this.handleNoticeClear}
            onMenuClick={this.handleMenuClick}
            onNoticeVisibleChange={this.handleNoticeVisibleChange}
            pstyle={borderStyle}
            {...this.props}
          />
        ) : (
          <GlobalHeader
            onCollapse={handleMenuCollapse}
            onNoticeClear={this.handleNoticeClear}
            onMenuClick={this.handleMenuClick}
            onNoticeVisibleChange={this.handleNoticeVisibleChange}
            pstyle={borderStyle}
            {...this.props}
          />
        )}
      </Header>
    ) : null;
    return (
      <div component="" transitionName="fade">
        {HeaderDom}
      </div>
    );
  }
}

export default connect(({ user, global, setting, loading }) => ({
  currentUser: user.currentUser,
  collapsed: global.collapsed,
  fetchingMoreNotices: loading.effects['global/fetchMoreNotices'],
  fetchingNotices: loading.effects['global/fetchNotices'],
  notices: global.notices,
  setting,
}))(HeaderView);
HeaderView.propTypes = {
  collapsed: PropTypes.any,
  setting: PropTypes.any,
  dispatch: PropTypes.any,
  autoHideHeader: PropTypes.any,
  handleMenuCollapse: PropTypes.any,
}
