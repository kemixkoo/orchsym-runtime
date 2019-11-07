import React from 'react';
import { Icon, Dropdown, Menu } from 'antd';
import { FormattedMessage } from 'umi-plugin-react/locale';

const MenuItem = Menu.Item;
export default class SortApplication extends React.Component {
  state = {
    iconType: 'sort-ascending',
  }

  handleSort = ({ key }) => {
    if (key % 2 !== 0) {
      this.setState({
        iconType: 'sort-descending',
      })
    } else {
      this.setState({
        iconType: 'sort-ascending',
      })
    }
  }

  render() {
    const { iconType } = this.state;
    const menu = (
      <Menu onClick={this.handleSort}>
        <MenuItem key="1"><FormattedMessage id="page.application.nameSort" /></MenuItem>
        <MenuItem key="2"><FormattedMessage id="page.application.nameDesc" /></MenuItem>
        <MenuItem key="3"><FormattedMessage id="page.application.modifySort" /></MenuItem>
        <MenuItem key="4"><FormattedMessage id="page.application.modifyDesc" /></MenuItem>
      </Menu>
    );
    return (
      <Dropdown trigger={['click']} overlay={menu} placement="bottomCenter">
        <Icon
          type={iconType}
          style={{ fontSize: '20px', margin: '5px 0' }}
        />
      </Dropdown>
    );
  }
}
