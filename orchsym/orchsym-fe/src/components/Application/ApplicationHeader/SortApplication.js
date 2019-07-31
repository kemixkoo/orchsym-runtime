import React from 'react';
import { Icon, Dropdown, Menu } from 'antd';

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
        <MenuItem key="1">名称升序</MenuItem>
        <MenuItem key="2">名称降序</MenuItem>
        <MenuItem key="3">编辑时间升序</MenuItem>
        <MenuItem key="4">编辑时间降序</MenuItem>
      </Menu>
    );
    return (
      <Dropdown overlay={menu} placement="bottomCenter">
        <Icon
          type={iconType}
          style={{ fontSize: '20px', margin: '5px 0' }}
        />
      </Dropdown>
    );
  }
}
