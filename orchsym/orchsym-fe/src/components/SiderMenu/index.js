import React from 'react';
import PropTypes from 'prop-types';
import SiderMenu from './SiderMenu';
import { getFlatMenuKeys } from './SiderMenuUtils';

const SiderMenuWrapper = React.memo(props => {
  const { menuData } = props;
  const flatMenuKeys = getFlatMenuKeys(menuData);
  return <SiderMenu {...props} flatMenuKeys={flatMenuKeys} />;
});

export default SiderMenuWrapper;
SiderMenuWrapper.propTypes = {
  menuData: PropTypes.any,
}
