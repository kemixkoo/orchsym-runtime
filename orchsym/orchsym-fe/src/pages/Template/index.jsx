import React from 'react';
import { Input, Row, Col, Button, Modal, Upload, message } from 'antd';
import { connect } from 'dva';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import router from 'umi/router'
import { debounce } from 'lodash'
import { formatMessage, FormattedMessage } from 'umi-plugin-react/locale';
import FavoriteList from './components/FavoriteList';
import CustomList from './components/CustomList';
import OfficialList from './components/OfficialList';
import { getToken } from '@/utils/authority';
// import AddTemp from './components/AddTemp';
import styles from './index.less';

const { Search } = Input;
const { confirm } = Modal;

@connect(({ template }) => ({
  template,
}))
class Template extends React.Component {
  constructor() {
    super()
    this.doSearchAjax = debounce(this.doSearchAjax, 800)
  }

  state = {
    isFresh: false,
    uploadLoading: false,
    // createTempVisible: null,
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
    const tabKey = (!tab || tab === ':tab') ? 'favorite' : tab;
    this.onTabChange(tabKey)
  }

  onTabChange = key => {
    this.setState(
      {
        tabActiveKey: key,
      }
    )
    router.replace(`/template/${key}`)
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
      pageNum: 1,
      isFresh: true,
    });
  }

  // showUploadModal = () => {
  //   this.setState({
  //     createTempVisible: true,
  //   })
  // };

  // handleCreateEditCancel = () => {
  //   this.setState({
  //     createTempVisible: false,
  //   })
  // };

  deleteTemps = (id) => {
    const { selectedRowKeys } = this.state;
    const { dispatch } = this.props;
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
            this.setState({
              isFresh: true,
            })
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
    const { isFresh, uploadLoading, tabActiveKey, searchVal, selectedRowKeys,
      sortedField, isDesc, pageSizeNum, pageNum } = this.state;
    const tabList = [
      {
        tab: formatMessage({ id: 'template.tab.favorite' }),
        key: 'favorite',
      },
      {
        tab: formatMessage({ id: 'template.tab.official' }),
        key: 'official',
      },
      {
        tab: formatMessage({ id: 'template.tab.customize' }),
        key: 'custom',
      },
    ]
    const hasSelected = selectedRowKeys.length > 0;
    const props = {
      name: 'template',
      showUploadList: false,
      action: '/studio/nifi-api/process-groups/root/templates/upload',
      headers: {
        authorization: `Bearer ${getToken()}`,
      },
      beforeUpload: file => {
        const isAccept = file.name.endsWith('.xml');
        if (!isAccept) {
          message.error(formatMessage({ id: 'validation.file.format' }));
        }
        return isAccept;
      },
      onChange: (info) => {
        console.log(this.props)
        if (info.file.status === 'uploading') {
          this.setState({ uploadLoading: true });
        }
        if (info.file.status === 'done') {
          this.setState({
            pageNum: 1,
            isFresh: true,
            uploadLoading: false,
          });
        }
        if (info.file.status === 'error') {
          message.error(info.file.response);
          this.setState({
            uploadLoading: false,
          });
        }
      },
    };
    return (
      <PageHeaderWrapper tabActiveKey={tabActiveKey} tabList={tabList} onTabChange={this.onTabChange}>
        <div className={styles.templateWrapper}>
          <div className={styles.tableTopHeader}>
            <Row gutter={16} className={styles.bottomSpace}>
              {tabActiveKey === 'custom' &&
                (
                  <Col span={12}>
                    <Upload {...props}>
                      <Button loading={uploadLoading} type="primary" style={{ marginRight: '10px' }}>
                        <FormattedMessage id="button.upload" />
                      </Button>
                    </Upload>
                    <Button disabled={!hasSelected} style={{ marginRight: '10px' }} onClick={this.downloadTemps}>
                      <FormattedMessage id="button.download" />
                    </Button>
                    <Button disabled={!hasSelected} onClick={this.deleteTemps}>
                      <FormattedMessage id="button.delete" />
                    </Button>
                  </Col>
                )
              }
              <Col span={tabActiveKey === 'custom' ? 12 : 24}>
                <Search placeholder={formatMessage({ id: 'button.search' })} className={styles.Search} onChange={this.handleSearch} allowClear />
              </Col>
            </Row>
          </div>
          {tabActiveKey === 'favorite' && (
            <FavoriteList
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
          {tabActiveKey === 'custom' && (
            <CustomList
              isFresh={isFresh}
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
        {/* {createTempVisible && (
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
        )} */}

      </PageHeaderWrapper>
    );
  }
}
export default Template
