import React from 'react';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import { Input, Row, Col, Button, Dropdown, Menu, Icon } from 'antd';
import { connect } from 'dva';
import { debounce } from 'lodash'
import { formatMessage, FormattedMessage } from 'umi-plugin-react/locale';
import CreateOrEditApp from './components/CreateOrEditApp';
import AppList from './components/AppList';
// import IconFont from '@/components/IconFont'
import styles from './index.less';

const { Search } = Input;
// const InputGroup = Input.Group;
// const { Option } = Select;
const MenuItem = Menu.Item;

class Application extends React.Component {
  constructor() {
    super()
    this.doSearchAjax = debounce(this.doSearchAjax, 500)
  }

  state = {
    createAppVisible: null,
    createOrEdit: formatMessage({ id: 'page.application.createApp' }),
    // selectValue: '名称',
    selectedKeys: 'modifyDesc',
    iconType: 'sort-ascending',
    pageNum: 1,
    pageSizeNum: 12,
    searchVal: '',
    sortedField: 'createdTime',
    isDesc: true,
  };

  // 搜索
  handleSearch = e => {
    this.doSearchAjax(e.target.value)
  }

  doSearchAjax = value => {
    this.setState({
      searchVal: value,
    });
  }

  handleSort = ({ key }) => {
    this.setState({
      selectedKeys: key,
    })
    if (key === 'nameSort') {
      this.setState({
        iconType: 'sort-descending',
        sortedField: 'name',
        isDesc: false,
      })
    } else if (key === 'nameDesc') {
      this.setState({
        iconType: 'sort-ascending',
        sortedField: 'name',
        isDesc: true,
      })
    } else if (key === 'modifySort') {
      this.setState({
        iconType: 'sort-descending',
        sortedField: 'createdTime',
        isDesc: false,
      })
    } else if (key === 'modifyDesc') {
      this.setState({
        iconType: 'sort-ascending',
        sortedField: 'createdTime',
        isDesc: true,
      })
    }
  }

  showCreateModal = () => {
    this.setState({
      createAppVisible: true,
    })
  };

  handleCreateEditCancel = () => {
    this.setState({
      createAppVisible: false,
    })
  };

  getHeadWidth = () => {
    const { collapsed } = this.props;
    return collapsed ? 'calc(100% - 120px)' : 'calc(100% - 230px)';
  };
  // handlePreSelect = (value) => {
  //   this.setState({
  //     selectValue: value,
  //   })
  // }

  onSearchChange = (obj) => {
    Object.keys(obj).forEach((key) => {
      this.setState({ [key]: obj[key] })
    })
  }

  render() {
    const { createAppVisible, createOrEdit, selectedKeys, iconType,
      searchVal, sortedField, isDesc, pageSizeNum, pageNum } = this.state;
    const menu = (
      <Menu onClick={this.handleSort} selectedKeys={[selectedKeys]}>
        <MenuItem key="nameSort"><FormattedMessage id="page.application.nameSort" /></MenuItem>
        <MenuItem key="nameDesc"><FormattedMessage id="page.application.nameDesc" /></MenuItem>
        <MenuItem key="modifySort"><FormattedMessage id="page.application.modifySort" /></MenuItem>
        <MenuItem key="modifyDesc"><FormattedMessage id="page.application.modifyDesc" /></MenuItem>
      </Menu>
    );
    // const tags = [
    // <Option value="全部" key="全部">全部</Option>,
    // <Option value="数据同步" key="数据同步">数据同步</Option>,
    // <Option value="格式转换" key="格式转换">格式转换</Option>,
    // <Option value="全量同步" key="全量同步">全量同步</Option>,
    // ]
    const width = this.getHeadWidth();
    return (
      <PageHeaderWrapper>
        <div className={styles.applicationHeader} style={{ width }}>
          <Row gutter={16} className={styles.bottomSpace}>
            <Col span={3}>
              <Button type="primary" onClick={this.showCreateModal}>
                <FormattedMessage id="page.application.createApp" />
              </Button>
            </Col>
            <Col span={20}>
              <Search placeholder={formatMessage({ id: 'page.application.search' })} className={styles.Search} onChange={this.handleSearch} allowClear />
              {/* <div className={styles.applicationRight}> */}
              {/* <div className={styles.search}> */}
              {/* <InputGroup compact>
                      <Select style={{ width: '70px' }} defaultValue="名称" onChange={this.handlePreSelect}>
                        <Option value="标签" key="标签">{formatMessage({ id: 'page.application.search.tag' })}</Option>
                        <Option value="名称" key="名称">{formatMessage({ id: 'page.application.search.name' })}</Option>
                      </Select>
                      {(selectValue === '标签') ? (
                        <span>
                          <Select
                            className={styles.searchSelect}
                            mode="multiple"
                            style={{ width: '250px' }}
                            onChange={this.handleSufSelect}
                          >
                            {tags}
                          </Select>
                        </span>
                      ) : (<Search className={styles.searchInput} onChange={this.handleSearch} allowClear />)
                      }
                    </InputGroup> */}
              {/* </div> */}
              {/* </div> */}
            </Col>
            <Col span={1}>
              <Dropdown trigger={['click']} overlay={menu} placement="bottomCenter">
                <Icon
                  type={iconType}
                  style={{ fontSize: '20px', margin: '5px 0' }}
                />
              </Dropdown>
            </Col>
          </Row>
        </div>
        <AppList
          onSearchChange={this.onSearchChange}
          pageNum={pageNum}
          pageSizeNum={pageSizeNum}
          searchVal={searchVal}
          sortedField={sortedField}
          isDesc={isDesc}
        />
        <CreateOrEditApp
          onSearchChange={this.onSearchChange}
          visible={createAppVisible}
          handleCreateEditCancel={this.handleCreateEditCancel}
          title={createOrEdit}
          pageNum={pageNum}
          pageSizeNum={pageSizeNum}
          searchVal={searchVal}
          sortedField={sortedField}
          isDesc={isDesc}
        />
      </PageHeaderWrapper>
    );
  }
}
export default connect(({ global }) => ({
  collapsed: global.collapsed,
}))(Application);
