/**
 * 用在头部下拉选框的组件
 */
import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { Dropdown } from 'antd';
import classNames from 'classnames';
import styles from './index.less';

export default class HeaderDropdown extends PureComponent {
  render() {
    const { overlayClassName, ...props } = this.props;
    return (
      <Dropdown trigger={['click']} overlayClassName={classNames(styles.container, overlayClassName)} {...props} />
    );
  }
}
HeaderDropdown.propTypes = {
  overlayClassName: PropTypes.any,
}
