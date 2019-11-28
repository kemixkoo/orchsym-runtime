import React from 'react';
import { Row, Col, Button, Form, Table, message, Input, Dropdown, Icon, Menu, Badge } from 'antd';
import { connect } from 'dva';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import { FormattedMessage, formatMessage, getLocale } from 'umi-plugin-react/locale';
import EditableCell from '@/components/EditableCell';
import { EditableContext } from '@/utils/utils'
import IconFont from '@/components/IconFont';
import moment from 'moment';
import styles from './index.less';

const { Search } = Input;
const ButtonGroup = Button.Group;

@connect(({ controllerServices }) => ({
  controllerServicesList: controllerServices.controllerServicesList,
}))

class ControllerServices extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      refreshTime: '',
      selectedRowKeys: [],
      editingKey: '',
      searchVal: '',
      pageNum: 1,
      pageSizeNum: 10,
      // filteredInfo: null,
      // sortedInfo: null,
    };
    this.columns = [
      {
        title: formatMessage({ id: 'title.name' }),
        width: 200,
        dataIndex: 'name',
        key: 'name',
        editable: true,
        sorter: true,
        rules: [
          { required: true, message: formatMessage({ id: 'validation.name.required' }) },
          { max: 20, message: formatMessage({ id: 'validation.name.placeholder' }) },
          { whitespace: true, message: formatMessage({ id: 'validation.name.required' }) },
          { validator: this.checkReName },
        ],
      },
      {
        title: formatMessage({ id: 'title.type' }),
        dataIndex: 'type',
        key: 'type',
        sorter: true,
      },
      {
        title: formatMessage({ id: 'service.title.scope' }),
        dataIndex: 'scope',
        key: 'scope',
        filters: this.scopeList,
      },
      {
        title: formatMessage({ id: 'service.title.refComponent' }),
        dataIndex: 'referencingComponents',
        key: 'refComponent',
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
        dataIndex: 'state',
        key: 'state',
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
        width: 150,
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
          ) : (
            <span className={styles.operateMenu}>
              {record.state === 'DISABLED' && (<a><Icon type="lock" /></a>)}
              {record.state === 'ENABLED' && (<a><Icon type="unlock" /></a>)}
              <a><Icon type="setting" style={{ marginLeft: '8px' }} /></a>
              <Dropdown overlay={this.menu(record)} trigger={['click']}>
                <Icon type="ellipsis" key="ellipsis" style={{ marginLeft: '8px' }} />
              </Dropdown>
            </span>
          );
        },
      },
    ];
  }


  componentDidMount() {
    const { pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.state
    this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
  }

  getList = (page, pageSize, sortedField, isDesc, text) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'controllerServices/fetchControllerServices',
      payload: {
        page,
        pageSize,
        text,
        sortedField,
        isDesc,
      },
      cb: () => {
        this.setState({
          refreshTime: moment(new Date()).format('HH:mm:ss'),
        })
      },
    });
  }

  handleTableChange = (pagination, filters, sorter) => {
    // const pager = { ...this.state.pagination };
    // pager.current = pagination.current;
    // this.setState({
    //   pagination: pager,
    // });
    // this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
  };

  menu = (item) => (
    <Menu>
      {item && (
        <Menu.Item key="rename" onClick={() => { this.deleteTempHandel() }}>
          {`${formatMessage({ id: 'service.button.rename' })}`}
        </Menu.Item>
      )}
      <Menu.Item key="copeTo" onClick={() => { this.deleteTempHandel() }}>
        {`${formatMessage({ id: 'service.button.copeTo' })}`}
      </Menu.Item>
      <Menu.Item key="moveTo" onClick={() => { this.deleteTempHandel() }}>
        {`${formatMessage({ id: 'service.button.moveTo' })}`}
      </Menu.Item>
      <Menu.Item key="delete" onClick={() => { this.deleteTempHandel() }}>
        {`${formatMessage({ id: 'button.delete' })}`}
      </Menu.Item>
    </Menu>
  );

  isEditing = record => {
    const { editingKey } = this.state;
    return record.id === editingKey;
  }

  cancel = () => {
    this.setState({ editingKey: '' });
  };

  save = (form, record) => {
    const { dispatch, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props;
    form.validateFields((error, row) => {
      if (error) {
        return;
      }
      dispatch({
        type: 'template/fetchEditTemplate',
        payload: {
          id: record.id,
          ...row,
        },
        cb: () => {
          this.cancel()
          message.success(formatMessage({ id: 'result.success' }));
          this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
        },
      });
    });
  }

  edit = (key) => {
    this.setState({ editingKey: key });
  }

  checkReName = (rule, value, callback) => {
    const { editingKey } = this.state;
    const { dispatch } = this.props;
    if (value) {
      const queryData = {
        name: value,
        templateId: editingKey,
      }
      dispatch({
        type: 'application/fetchCheckTempName',
        payload: queryData,
        cb: (res) => {
          if (res.isValid) {
            callback();
          } else {
            callback([new Error(formatMessage({ id: 'validation.name.duplicate' }))]);
          }
        },
      });
    } else {
      callback();
    }
  }

  render() {
    const { selectedRowKeys, pageNum, pageSizeNum, searchVal, sortedField, isDesc, refreshTime } = this.state;
    const { form, controllerServicesList: { results, totalSize } } = this.props;
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
    // const hasSelected = selectedRowKeys.length > 0;
    return (
      <PageHeaderWrapper>
        <div className={styles.contentWrapper}>
          <div className={styles.tableTopHeader}>
            <Row gutter={16} className={styles.bottomSpace}>
              <Col span={12}>
                <Button type="primary" style={{ marginRight: '10px' }} onClick={this.showUploadModal}>
                  <FormattedMessage id="button.create" />
                </Button>
                <ButtonGroup>
                  <Button>{formatMessage({ id: 'button.enable' })}</Button>
                  <Button>{formatMessage({ id: 'button.disable' })}</Button>
                  <Dropdown overlay={this.menu} trigger={['click']}>
                    <Button>
                      <Icon type="ellipsis" key="ellipsis" style={{ marginLeft: '5px' }} />
                    </Button>
                  </Dropdown>
                </ButtonGroup>
              </Col>
              <Col>
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
              rowSelection={rowSelection}
              components={components}
              dataSource={results}
              columns={columns}
              rowKey="id"
              rowClassName="editable-row"
              onChange={this.handleTableChange}
              pagination={{
                size: 'small',
                onChange: (page, pageSize) => {
                  this.getList(page, pageSize, sortedField, isDesc, searchVal)
                  this.setState({
                    pageNum: page,
                    pageSizeNum: pageSize,
                  })
                },
                onShowSizeChange: (current, size) => {
                  this.getList(current, size, sortedField, isDesc, searchVal)
                  this.setState({
                    pageNum: current,
                    pageSizeNum: size,
                  })
                },
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
      </PageHeaderWrapper>
    );
  }
}
export default (Form.create()(ControllerServices));
