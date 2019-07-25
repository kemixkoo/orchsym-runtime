import React from 'react';
import { Icon } from 'antd';

export default class SortApplication extends React.Component {
  state = {
    iconType: 'sort-ascending',
  }

  handleSort = () => {
    const { iconType } = this.state;
    this.setState({
      iconType: iconType === 'sort-ascending' ? 'sort-descending' : 'sort-ascending',
    })
  }

  render() {
    const { iconType } = this.state;
    return (
      <Icon
        type={iconType}
        onClick={this.handleSort}
        style={{ fontSize: '20px', margin: '5px 0' }}
      />
    );
  }
}
