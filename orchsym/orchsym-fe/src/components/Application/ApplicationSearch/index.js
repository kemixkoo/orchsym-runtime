import React from 'react';
import { Input, Select, Button } from 'antd';
import styles from '../application.less';

const InputGroup = Input.Group;
const { Option } = Select;

export default class ApplicationSearch extends React.Component {
  state = {
    selectValue: '标签',
  }

  handlePreSelect = (value) => {
    this.setState({
      selectValue: value,
    })
  }

  render() {
    const { selectValue } = this.state;
    const tags = [
      <Option value="全部">全部</Option>,
      <Option value="数据同步">数据同步</Option>,
      <Option value="格式转换">格式转换</Option>,
      <Option value="全量同步">全量同步</Option>,
    ]
    return (
      <div className={styles.search}>
        <InputGroup compact>
          <Select defaultValue="标签" onChange={this.handlePreSelect}>
            <Option value="标签">标签</Option>
            <Option value="名称">名称</Option>
          </Select>
          {(selectValue === '标签') ? (
            <Select
              mode="multiple"
              style={{ width: '250px', borderRadius: '0' }}
              onChange={this.handleSufSelect}
            >
              {tags}
            </Select>
          ) : (
            <Input style={{ width: '250px', borderRadius: '0' }} />
          )}
        </InputGroup>
        <Button type="primary" className={styles.searchButton}>搜索</Button>
      </div>
    );
  }
}
