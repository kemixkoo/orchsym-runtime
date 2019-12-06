import React from 'react';
import { Row, Col, Button, Form, Table, message, Input, Dropdown, Icon, Menu, Badge, Modal } from 'antd';
import { connect } from 'dva';
import { debounce } from 'lodash'
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import { FormattedMessage, formatMessage, getLocale } from 'umi-plugin-react/locale';
import EditableCell from '@/components/EditableCell';
import FilterDropdown from '@/components/FilterDropdown';
import moment from 'moment';
import MoveOrCope from './components/MoveOrCope';
import { EditableContext } from '@/utils/utils'
import IconFont from '@/components/IconFont';
import styles from './index.less';

const { Search } = Input;
const ButtonGroup = Button.Group;

@connect(({ controllerServices, loading, application }) => ({
  application,
  controllerServicesList: controllerServices.controllerServicesList,
  loading: loading.effects['controllerServices/fetchControllerServices'],
}))

class ControllerServices extends React.Component {
  constructor(props) {
    super(props);
    this.doSearchAjax = debounce(this.doSearchAjax, 800)
    this.state = {
      refreshTime: '',
      selectedRowKeys: [],
      editingKey: '',
      searchVal: '',
      pageNum: 1,
      pageSizeNum: 10,
      filteredInfo: null,
      sortedInfo: null,
      // 过滤作用域
      filterList: [],
      visible: false,
      // 移动复制
      copeVisible: false,
      modelState: '',
      nowService: '',
    };
    this.columns = [
      {
        title: formatMessage({ id: 'title.name' }),
        width: 200,
        dataIndex: 'name',
        key: 'NAME',
        editable: true,
        sorter: true,
        rules: [
          { required: true, message: formatMessage({ id: 'validation.name.required' }) },
          { max: 20, message: formatMessage({ id: 'validation.name.placeholder' }) },
          { whitespace: true, message: formatMessage({ id: 'validation.name.required' }) },
        ],
      },
      {
        title: formatMessage({ id: 'title.type' }),
        // width: 300,
        dataIndex: 'type',
        key: 'TYPE',
        sorter: true,
      },
      {
        title: formatMessage({ id: 'service.title.scope' }),
        width: 150,
        dataIndex: 'scope',
        key: 'scopes',
        filterDropdown: this.filterDropdownHandel,
        onFilterDropdownVisibleChange: this.onFilterVisibleChange,
        render: (text, record) => (text === 'ROOT' ? formatMessage({ id: 'text.global' }) : (text)),
      },
      {
        title: formatMessage({ id: 'service.title.refComponent' }),
        width: 200,
        dataIndex: 'referencingComponents',
        key: 'REFERENCING_COMPONENTS',
        sorter: true,
        render: (text, record) => (
          <div>
            <span className={styles.badgeIcon}>
              <IconFont type="OS-iconqidong" />
              <span>({text.RUNNING.length})</span>
            </span>
            <span className={styles.badgeIcon}>
              <IconFont type="OS-icontingzhi" />
              <span>({text.STOPPED.length})</span>
            </span>
            <span className={styles.badgeIcon}>
              <IconFont type="OS-iconicon" />
              <span>({text.INVALID.length})</span>
            </span>
          </div>
        ),
      },
      {
        title: formatMessage({ id: 'service.title.serviceStatus' }),
        width: 150,
        dataIndex: 'state',
        key: 'states',
        filters: [
          {
            text: formatMessage({ id: 'service.text.ENABLED' }),
            value: 'ENABLED',
          },
          {
            text: formatMessage({ id: 'service.text.DISABLED' }),
            value: 'DISABLED',
          },
          {
            text: formatMessage({ id: 'service.text.INVALID' }),
            value: 'INVALID',
          },
          {
            text: formatMessage({ id: 'service.text.ENABLING' }),
            value: 'ENABLING',
          },
        ],
        render: (text, record) => {
          if (record.validationStatus === 'VALID') {
            if (text === 'ENABLED') {
              return <Badge status="success" text={formatMessage({ id: 'service.text.ENABLED' })} />
            } else if (text === 'DISABLED') {
              return <Badge status="error" text={formatMessage({ id: 'service.text.DISABLED' })} />
            } else {
              return <IconFont type="OS-iconLoading" />
            }
          } else if (record.validationStatus === 'INVALID') {
            return <Badge status="warning" text={formatMessage({ id: 'service.text.INVALID' })} />
          } else {
            return <IconFont type="OS-iconLoading" />
          }
        },
      },
      {
        title: formatMessage({ id: 'title.operate' }),
        width: 120,
        render: (text, record) => {
          // const { match } = this.props
          // const { editingKey } = this.state;
          const editable = this.isEditing(record);
          return editable ? (
            <span>
              <EditableContext.Consumer>
                {form => (
                  <span>
                    <a
                      type="link"
                      onClick={() => this.save(form, record)}
                      style={{ marginRight: 8 }}
                    >
                      {formatMessage({ id: 'button.save' })}
                    </a>
                    <a
                      type="link"
                      onClick={() => this.cancel()}
                    >
                      {formatMessage({ id: 'button.cancel' })}
                    </a>
                  </span>
                )}
              </EditableContext.Consumer>
            </span>
          ) :
            (
              <span className={styles.operateMenu}>
                {record.state === 'DISABLED' && (<Icon type="lock" onClick={() => { this.stateHandel('ENABLED', record.id) }} />)}
                {record.state === 'ENABLED' && (<Icon type="unlock" onClick={() => { this.stateHandel('DISABLED', record.id) }} />)}
                <Icon type="setting" style={{ marginLeft: '10px' }} />
                <Dropdown overlay={this.menu(record)} trigger={['click']}>
                  <Icon type="ellipsis" key="ellipsis" style={{ marginLeft: '10px' }} />
                </Dropdown>
              </span>
            );
        },
      },
    ];
  }


  componentDidMount() {
    this.getList()
  }

  getList = (params = {}) => {
    const { dispatch } = this.props;
    const { pageNum, pageSizeNum, searchVal, sortedInfo, filteredInfo } = this.state
    dispatch({
      type: 'controllerServices/fetchControllerServices',
      payload: {
        page: pageNum,
        pageSize: pageSizeNum,
        text: searchVal,
        sortedField: sortedInfo && sortedInfo.columnKey,
        desc: (sortedInfo && sortedInfo.order !== 'ascend') || true,
        ...filteredInfo,
        ...params,
      },
      cb: () => {
        this.setState({
          refreshTime: moment(new Date()).format('HH:mm:ss'),
        })
      },
    });
  }

  handleTableChange = (pagination, filters, sorter) => {
    this.setState({
      pageNum: pagination.current,
      pageSizeNum: pagination.pageSize,
      filteredInfo: filters,
      sortedInfo: sorter,
    })
    this.getList({
      page: pagination.current,
      pageSize: pagination.pageSize,
      sortedField: sorter.columnKey,
      desc: sorter.order !== 'ascend',
      ...filters,
    })
  };

  // 搜索
  handleSearch = e => {
    this.doSearchAjax(e.target.value)
  }

  doSearchAjax = value => {
    this.getList({ page: 1, text: value })
    this.setState({
      pageNum: 1,
      searchVal: value,
    });
  }

  menu = (item) => (
    <Menu style={{ width: '80px' }}>
      {item && (
        <Menu.Item key="rename" disabled={item.state !== 'DISABLED'} onClick={() => this.edit(item.id)}>
          {`${formatMessage({ id: 'service.button.rename' })}`}
        </Menu.Item>
      )}
      <Menu.Item key="copeTo" onClick={() => { this.showCopeModal('COPE', item) }}>
        {`${formatMessage({ id: 'service.button.copeTo' })}`}
      </Menu.Item>
      <Menu.Item key="moveTo" disabled={item.state !== 'DISABLED'} onClick={() => { this.showCopeModal('MOVE', item) }}>
        {`${formatMessage({ id: 'service.button.moveTo' })}`}
      </Menu.Item>
      <Menu.Item key="delete" disabled={item && item.state !== 'DISABLED'} onClick={() => { this.deleteHandel(item) }}>
        {`${formatMessage({ id: 'button.delete' })}`}
      </Menu.Item>
    </Menu>
  );

  // 过滤作用域
  filterDropdownHandel = ({ setSelectedKeys, selectedKeys, confirm, clearFilters }) => {
    const { filterList, visible } = this.state
    const setSelKeys = (val) => {
      setSelectedKeys(val)
    }
    return (
      <div style={{ width: '160px' }}>
        {visible && (
          <div>
            <FilterDropdown selectedKeys={selectedKeys} setSelKeys={setSelKeys} filterList={filterList} searchValue={this.searchValue} />
            <div className={styles.filterButton}>
              <Button type="link" onClick={() => confirm()}>{formatMessage({ id: 'button.submit' })}</Button>
              <Button type="link" onClick={() => clearFilters()}>{formatMessage({ id: 'button.reset' })}</Button>
            </div>
          </div>
        )}
      </div>
    )
  }

  onFilterVisibleChange = visible => {
    this.setState({ visible })
    if (visible) {
      this.getScopes()
    }
  }

  searchValue = (val) => {
    this.getScopes(val)
  }

  getScopes = (val) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'application/fetchApplication',
      payload: {
        q: val,
        page: 1,
        pageSize: -1,
      },
      cb: (res) => {
        const list = []
        res.results.map(v => list.push({ value: v.id, name: v.name }))
        this.setState({
          filterList: [
            { value: 'root', name: formatMessage({ id: 'text.global' }) },
            ...list,
          ],
        })
      },
    });
  }
  // 重命名

  edit = (key) => {
    this.setState({ editingKey: key });
  }

  isEditing = record => {
    const { editingKey } = this.state;
    return record.id === editingKey;
  }

  cancel = () => {
    this.setState({ editingKey: '' });
  };

  save = (form, record) => {
    const { dispatch } = this.props;
    form.validateFields((error, row) => {
      if (error) {
        return;
      }
      dispatch({
        type: 'controllerServices/fetchDetailServices',
        payload: record.id,
        cb: (res) => {
          const { revision } = res
          const body = {
            component: {
              id: record.id,
              ...row,
            },
            revision,
          }
          dispatch({
            type: 'controllerServices/fetchUpdateServices',
            payload: body,
            cb: () => {
              this.cancel()
              message.success(formatMessage({ id: 'result.success' }));
              this.getList()
            },
          });
        },
      });
    });
  }

  onSelectChange = selectedRowKeys => {
    this.setState({
      selectedRowKeys,
    })
  };

  // 起停
  stateHandel = (state, val) => {
    const { selectedRowKeys } = this.state;
    const { dispatch } = this.props;
    if (val === 'multiple') {
      dispatch({
        type: 'controllerServices/fetchStateUpdateServices',
        payload: {
          state,
          serviceIds: selectedRowKeys,
          type: val,
        },
        cb: () => {
          message.success(formatMessage({ id: 'result.success' }));
          this.getList()
          this.setState({
            selectedRowKeys: [],
          })
        },
      })
    } else {
      dispatch({
        type: 'controllerServices/fetchDetailServices',
        payload: val,
        cb: (res) => {
          const { revision } = res
          const body = {
            component: {
              id: val,
              state,
            },
            revision,
          }
          dispatch({
            type: 'controllerServices/fetchStateUpdateServices',
            payload: {
              value: body,
            },
            cb: () => {
              message.success(formatMessage({ id: 'result.success' }));
              this.getList()
            },
          });
        },
      });
    }
  }

  deleteHandel = (item) => {
    const { selectedRowKeys } = this.state;
    const { dispatch } = this.props;
    const { confirm } = Modal;
    const that = this
    confirm({
      title: formatMessage({ id: 'service.delete.title' }),
      content: formatMessage({ id: 'service.delete.description' }),
      okText: 'Yes',
      okType: 'warning',
      cancelText: 'No',
      onOk() {
        let body = {}
        if (item) {
          body = { id: item.id }
        } else {
          body = {
            serviceIds: selectedRowKeys,
            type: 'multiple',
          }
        }
        dispatch({
          type: 'controllerServices/fetchDeleteServices',
          payload: body,
          cb: () => {
            message.success(formatMessage({ id: 'result.success' }));
            that.getList()
          },
        })
      },
      onCancel() {
        console.log('Cancel');
      },
    });
  }

  // 复制 移动
  showCopeModal = (state, item) => {
    this.setState({
      copeVisible: true,
      modelState: state,
      nowService: item,
    });
  }

  handleCopeCancel = () => {
    this.setState({
      copeVisible: false,
    })
  }

  refreshList = () => {
    this.getList({ page: 1 })
    this.setState({
      pageNum: 1,
    });
  }

  render() {
    const { selectedRowKeys, pageNum, pageSizeNum, refreshTime, copeVisible, modelState, nowService } = this.state;
    const { form, controllerServicesList: { results, totalSize }, loading } = this.props;
    const rowSelection = {
      selectedRowKeys,
      onChange: this.onSelectChange,
    };
    const components = {
      body: {
        cell: EditableCell,
      },
    };

    const columns = this.columns.map(col => {
      if (!col.editable) {
        return col;
      }
      return {
        ...col,
        onCell: record => ({
          record,
          // inputType: col.dataIndex === 'age' ? 'number' : 'text',
          dataIndex: col.dataIndex,
          // title: col.title,
          rules: col.rules,
          editing: this.isEditing(record),
        }),
      };
    });

    const showTotal = (total) => {
      if (getLocale() === 'zh-CN') {
        return `共 ${total} 个`;
      } else {
        return `Total ${total} items`;
      }
    }
    const hasSelected = selectedRowKeys.length > 0;
    return (
      <PageHeaderWrapper>
        <div className={styles.contentWrapper}>
          <div className={styles.tableTopHeader}>
            <Row gutter={16} className={styles.bottomSpace}>
              <Col span={12}>
                <Button type="primary" style={{ marginRight: '10px' }}>
                  <FormattedMessage id="button.create" />
                </Button>
                <ButtonGroup>
                  <Button disabled={!hasSelected} onClick={() => { this.stateHandel('enable', 'multiple') }}>{formatMessage({ id: 'button.enable' })}</Button>
                  <Button disabled={!hasSelected} onClick={() => { this.stateHandel('disable', 'multiple') }}>{formatMessage({ id: 'button.disable' })}</Button>
                  <Dropdown disabled={!hasSelected} overlay={this.menu} trigger={['click']}>
                    <Button>
                      <Icon type="ellipsis" key="ellipsis" />
                    </Button>
                  </Dropdown>
                </ButtonGroup>
              </Col>
              <Col span={12}>
                <div className={styles.headerRight}>
                  <span style={{ marginRight: '8px' }}>
                    <Icon
                      type="redo"
                      rotate={180}
                      style={{ marginRight: 10 }}
                    // onClick={this.handleRefreshEnv}
                    />
                    最后更新：{refreshTime}
                  </span>
                  <Search placeholder={formatMessage({ id: 'button.search' })} className={styles.Search} onChange={this.handleSearch} allowClear />
                </div>
              </Col>
            </Row>
          </div>
          <EditableContext.Provider value={form}>
            <Table
              scroll={{ y: 400 }}
              loading={loading}
              rowSelection={rowSelection}
              components={components}
              dataSource={results}
              columns={columns}
              rowKey="id"
              rowClassName="editable-row"
              onChange={this.handleTableChange}
              pagination={{
                size: 'small',
                current: pageNum,
                pageSize: pageSizeNum,
                total: totalSize,
                showTotal,
                pageSizeOptions: ['10', '20'],
                showSizeChanger: true,
                showQuickJumper: true,
              }}
            />
          </EditableContext.Provider>
        </div>
        {
          copeVisible && (
            <MoveOrCope
              refreshList={this.refreshList}
              handleCopeCancel={this.handleCopeCancel}
              visible={copeVisible}
              modelState={modelState}
              selectedRowKeys={selectedRowKeys}
              nowService={nowService}
            />
          )
        }
      </PageHeaderWrapper>
    );
  }
}
export default (Form.create()(ControllerServices));
