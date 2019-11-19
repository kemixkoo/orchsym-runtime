import React, { PureComponent } from 'react';
import { Input } from 'antd';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import router from 'umi/router'
import CollectList from './Table/CollectList';
import styles from './index.less';

const { Search } = Input;
class Template extends PureComponent {
  state = {
    tabActiveKey: '',
    searchVal: '',
  };

  componentDidMount() {
    console.log(this.props)
    const { match } = this.props
    const { tab } = match.params;
    const tabKey = !tab ? 'collect' : tab;
    this.onTabChange(tabKey)
  }

  onTabChange = key => {
    console.log(key)
    this.setState(
      {
        tabActiveKey: key,
      }
    )
    router.replace(`/template/${key}`)
  }

  render() {
    const { tabActiveKey, searchVal } = this.state
    const tabList = [
      {
        tab: '收藏',
        key: 'collect',
      },
      {
        tab: '官方',
        key: 'official',
      },
      {
        tab: '自定义',
        key: 'customize',
      },
    ]

    return (
      <PageHeaderWrapper tabActiveKey={tabActiveKey} tabList={tabList} onTabChange={this.onTabChange}>
        <div className={styles.templateWrapper}>
          <div className={styles.tableTopHeader}>
            <Search
              placeholder="搜索"
              className={styles.Search}
              onSearch={this.handleSearch}
              onChange={this.handleSearchText}
              value={searchVal}
              allowClear
            />
          </div>
          {tabActiveKey === 'collect' && <CollectList />}
        </div>
      </PageHeaderWrapper>
    );
  }
}
export default Template
