import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { debounce } from 'lodash'
import { Checkbox, Icon, Input } from 'antd';
import Ellipsis from '@/components/Ellipsis';
import styles from './index.less';

@connect(({ application, loading }) => ({
  loading:
    loading.effects['application/fetchApplication'],
}))
class FilterDropdown extends PureComponent {
  constructor() {
    super()
    this.doSearchAjax = debounce(this.doSearchAjax, 800)
  }

  state = {
    value: '',
  };

  componentWillUnmount() {
    this.setState({ value: '' })
  }

  // 搜索
  onSearchChange = e => {
    this.setState({ value: e.target.value })
    this.doSearchAjax(e.target.value)
  }

  doSearchAjax = value => {
    const { searchValue } = this.props
    searchValue(value)
  }

  render() {
    const { filterList, selectedKeys, setSelKeys } = this.props
    const { value } = this.state
    return (
      <div style={{ padding: 8 }}>
        <Input
          // style={{ width: '100px' }}
          value={value}
          onChange={this.onSearchChange}
          allowClear
          prefix={<Icon type="search" style={{ color: 'rgba(0,0,0,.25)' }} />}
        />
        <div className={styles.groupScrollbar}>
          <Checkbox.Group value={selectedKeys} onChange={val => setSelKeys(val)}>
            <ul>
              {filterList.map(item =>
                (<li key={item.value}><Checkbox value={item.value}><Ellipsis length={10}>{item.name}</Ellipsis></Checkbox></li>))}
            </ul>
          </Checkbox.Group>
        </div>
      </div>

    );
  }
}
export default FilterDropdown
