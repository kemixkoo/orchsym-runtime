import React from 'react';
import { Input, Row, Col, Button, Modal } from 'antd';
import { connect } from 'dva';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import router from 'umi/router'
import { debounce } from 'lodash'
import { formatMessage, FormattedMessage } from 'umi-plugin-react/locale';
import CollectList from './components/CollectList';
import CustomizeList from './components/CustomizeList';
import OfficialList from './components/OfficialList';
import AddTemp from './components/AddTemp';
import styles from './index.less';

const { Search } = Input;
const { confirm } = Modal;

@connect(({ template }) => ({
  template,
}))
class Template extends React.Component {
  constructor() {
    super()
    this.doSearchAjax = debounce(this.doSearchAjax, 500)
  }

  state = {
    createTempVisible: null,
    tabActiveKey: '',
    selectedRowKeys: [],
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

  onStateChange = (obj) => {
    Object.keys(obj).forEach((key) => {
      this.setState({ [key]: obj[key] })
    })
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

  showUploadModal = () => {
    this.setState({
      createTempVisible: true,
    })
  };

  handleCreateEditCancel = () => {
    this.setState({
      createTempVisible: false,
    })
  };

  deleteTemps = (id) => {
    const { selectedRowKeys } = this.state;
    const { dispatch, onFrechList } = this.props;
    confirm({
      title: formatMessage({ id: 'template.delete.title' }),
      content: formatMessage({ id: 'template.delete.description' }),
      okText: 'Yes',
      okType: 'warning',
      cancelText: 'No',
      onOk() {
        dispatch({
          type: 'template/fetchDeleteTemplates',
          payload: {
            templateIds: selectedRowKeys,
            type: 'multiple',
          },
          cb: () => {
            onFrechList()
          },
        })
      },
      onCancel() {
        console.log('Cancel');
      },
    });
  }

  // 下载
  downloadTemps = () => {
    console.log(this.props)
    const { selectedRowKeys } = this.state;
    const { dispatch } = this.props;
    const templateIds = selectedRowKeys.join(',')
    dispatch({
      type: 'template/fetchDownloadTemplates',
      payload: {
        templateIds,
        type: 'multiple',
      },
    });
  }

  render() {
    const { match } = this.props
    const { createTempVisible, tabActiveKey, searchVal, selectedRowKeys,
      sortedField, isDesc, pageSizeNum, pageNum } = this.state;
    const tabList = [
      {
        tab: formatMessage({ id: 'button.collect' }),
        key: 'collect',
      },
      {
        tab: formatMessage({ id: 'template.tab.official' }),
        key: 'official',
      },
      {
        tab: formatMessage({ id: 'template.tab.customize' }),
        key: 'customize',
      },
    ]
    const hasSelected = selectedRowKeys.length > 0;
    return (
      <PageHeaderWrapper tabActiveKey={tabActiveKey} tabList={tabList} onTabChange={this.onTabChange}>
        <div className={styles.templateWrapper}>
          <div className={styles.tableTopHeader}>
            <Row gutter={16} className={styles.bottomSpace}>
              {tabActiveKey === 'customize' &&
                (
                  <Col span={12}>
                    <Button type="primary" style={{ marginRight: '10px' }} onClick={this.showUploadModal}>
                      <FormattedMessage id="button.upload" />
                    </Button>
                    <Button disabled={!hasSelected} style={{ marginRight: '10px' }} onClick={this.downloadTemps}>
                      <FormattedMessage id="button.download" />
                    </Button>
                    <Button disabled={!hasSelected} onClick={this.deleteTemps}>
                      <FormattedMessage id="button.delete" />
                    </Button>
                  </Col>
                )
              }
              <Col span={tabActiveKey === 'customize' ? 12 : 24}>
                <Search placeholder={formatMessage({ id: 'button.search' })} className={styles.Search} onChange={this.handleSearch} allowClear />
              </Col>
            </Row>
          </div>
          {tabActiveKey === 'collect' && (
            <CollectList
              match={match}
              onStateChange={this.onStateChange}
              pageNum={pageNum}
              pageSizeNum={pageSizeNum}
              searchVal={searchVal}
              sortedField={sortedField}
              isDesc={isDesc}
            />
          )}
          {tabActiveKey === 'official' && (
            <OfficialList
              match={match}
              onStateChange={this.onStateChange}
              pageNum={pageNum}
              pageSizeNum={pageSizeNum}
              searchVal={searchVal}
              sortedField={sortedField}
              isDesc={isDesc}
            />
          )}
          {tabActiveKey === 'customize' && (
            <CustomizeList
              match={match}
              selectedRowKeys={selectedRowKeys}
              onStateChange={this.onStateChange}
              pageNum={pageNum}
              pageSizeNum={pageSizeNum}
              searchVal={searchVal}
              sortedField={sortedField}
              isDesc={isDesc}
            />
          )}
        </div>
        {createTempVisible && (
          <AddTemp
            visible={createTempVisible}
            handleCancel={this.handleCreateEditCancel}
            onStateChange={this.onStateChange}
            pageNum={pageNum}
            pageSizeNum={pageSizeNum}
            searchVal={searchVal}
            sortedField={sortedField}
            isDesc={isDesc}
          />
        )}

      </PageHeaderWrapper>
    );
  }
}
export default Template
