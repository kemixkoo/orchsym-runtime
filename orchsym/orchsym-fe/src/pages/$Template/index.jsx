import React, { PureComponent } from 'react';
import { Input, Row, Col, Button } from 'antd';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import router from 'umi/router'
import { formatMessage, FormattedMessage } from 'umi-plugin-react/locale';
import CollectList from './components/CollectList';
import CustomizeList from './components/CustomizeList';
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
            <Row gutter={16} className={styles.bottomSpace}>
              {tabActiveKey === 'customize' &&
                (
                  <Col span={12}>
                    <Button type="primary" style={{ marginRight: '10px' }} onClick={this.showCreateModal}>
                      <FormattedMessage id="button.upload" />
                    </Button>
                    <Button style={{ marginRight: '10px' }} onClick={this.showCreateModal}>
                      <FormattedMessage id="button.download" />
                    </Button>
                    <Button onClick={this.showCreateModal}>
                      <FormattedMessage id="button.delete" />
                    </Button>
                  </Col>
                )

              }
              <Col span={tabActiveKey === 'customize' ? 12 : 24}>
                <Search
                  placeholder={formatMessage({ id: 'page.application.search' })}
                  className={styles.Search}
                  onSearch={this.handleSearch}
                  onChange={this.handleSearchText}
                  value={searchVal}
                  allowClear
                />
              </Col>
            </Row>
          </div>
          {tabActiveKey === 'collect' && <CollectList />}
          {tabActiveKey === 'customize' && <CustomizeList />}
        </div>
      </PageHeaderWrapper>
    );
  }
}
export default Template
