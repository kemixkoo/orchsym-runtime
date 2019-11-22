import React, { PureComponent } from 'react';
import { Input, Row, Col, Button } from 'antd';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import router from 'umi/router'
import { debounce } from 'lodash'
import { formatMessage, FormattedMessage } from 'umi-plugin-react/locale';
import CollectList from './components/CollectList';
import CustomizeList from './components/CustomizeList';
import OfficialList from './components/OfficialList';
import styles from './index.less';

const { Search } = Input;
class Template extends PureComponent {
  constructor() {
    super()
    this.doSearchAjax = debounce(this.doSearchAjax, 500)
  }

  state = {
    tabActiveKey: '',
    // 列表传参
    pageNum: 1,
    pageSizeNum: 10,
    searchVal: '',
    sortedField: 'createdTime',
    isDesc: true,
  };

  componentDidMount() {
    const { match } = this.props
    const { tab } = match.params;
    const tabKey = (!tab || tab === ':tab') ? 'collect' : tab;
    this.onTabChange(tabKey)
  }

  onTabChange = key => {
    this.setState(
      {
        tabActiveKey: key,
      }
    )
    router.replace(`/temp/${key}`)
  }

  // 搜索
  handleSearch = e => {
    this.doSearchAjax(e.target.value)
  }

  doSearchAjax = value => {
    this.setState({
      searchVal: value,
    });
  }

  onSearchChange = (obj) => {
    Object.keys(obj).forEach((key) => {
      this.setState({ [key]: obj[key] })
    })
  }

  render() {
    const { tabActiveKey, searchVal, sortedField, isDesc, pageSizeNum, pageNum } = this.state;
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
                <Search placeholder={formatMessage({ id: 'page.application.search' })} className={styles.Search} onChange={this.handleSearch} allowClear />
              </Col>
            </Row>
          </div>
          {tabActiveKey === 'collect' && (
            <CollectList
              onSearchChange={this.onSearchChange}
              pageNum={pageNum}
              pageSizeNum={pageSizeNum}
              searchVal={searchVal}
              sortedField={sortedField}
              isDesc={isDesc}
            />
          )
          }
          {tabActiveKey === 'official' && (
            <OfficialList
              onSearchChange={this.onSearchChange}
              pageNum={pageNum}
              pageSizeNum={pageSizeNum}
              searchVal={searchVal}
              sortedField={sortedField}
              isDesc={isDesc}
            />
          )
          }
          {tabActiveKey === 'customize' && (
            <CustomizeList
              onSearchChange={this.onSearchChange}
              pageNum={pageNum}
              pageSizeNum={pageSizeNum}
              searchVal={searchVal}
              sortedField={sortedField}
              isDesc={isDesc}
            />
          )
          }
        </div>
      </PageHeaderWrapper>
    );
  }
}
export default Template
