import React from 'react';
import { Icon } from 'antd';

export default class ListCard extends React.Component {
  state = {
    iconType: 'credit-card',
  }

  handleChange = () => {
    const { iconType } = this.state;
    this.setState({
      iconType: iconType === 'credit-card' ? 'unordered-list' : 'credit-card',
    })
  }

  render() {
    const { iconType } = this.state;
    return (
      <Icon
        type={iconType}
        onClick={this.handleChange}
        style={{ fontSize: '20px', margin: '5px 0' }}
      />
    );
  }
}
