import React from 'react';
import { Row, Col, Button, Form, Table, message, Input } from 'antd';
import { connect } from 'dva';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';
import { FormattedMessage, formatMessage, getLocale } from 'umi-plugin-react/locale';
import EditableCell from '@/components/EditableCell';
import { EditableContext } from '@/utils/utils'
import IconFont from '@/components/IconFont';
import styles from './index.less';

const { Search } = Input;
@connect(({ controllerServices }) => ({
  controllerServicesList: controllerServices.controllerServicesList,
}))

class ControllerServices extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedRowKeys: [],
      editingKey: '',
      searchVal: '',
      pageNum: 1,
      pageSizeNum: 10,
      sortedField: 'NAME', // NAME(按照服务名排序)/TYPE(根据服务类型排序)/REFERENCING_COMPONENTS(根据引用该服务的组件的数量排序)/NONE(不排序)
      isDesc: true,
    };
    this.columns = [
      {
        title: formatMessage({ id: 'title.name' }),
        width: 200,
        dataIndex: 'name',
        key: 'name',
        editable: true,
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
      },
      {
        title: formatMessage({ id: 'service.title.scope' }),
        dataIndex: 'scope',
        key: 'scope',
      },
      {
        title: formatMessage({ id: 'service.title.refComponent' }),
        dataIndex: 'referencingComponents',
        key: 'refComponent',
        render: (text, record) => (
          <div>
            <span className={styles.badgeIcon}>
              <IconFont type="OS-iconqidong" />
              <span>{record.runningCount}</span>
            </span>
            <span className={styles.badgeIcon}>
              <IconFont type="OS-icontingzhi" />
              <span>{record.stoppedCount}</span>
            </span>
            <span className={styles.badgeIcon}>
              <IconFont type="OS-iconicon" />
              <span>{record.invalidCount}</span>
            </span>
            {/* <a className="ant-dropdown-link" href="#">
            更多
          </a> */}
          </div>
        ),
      },
      {
        title: formatMessage({ id: 'service.title.serviceStatus' }),
        dataIndex: 'additions.CREATED_TIMESTAMP',
        key: 'serviceStatus',
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
          ) : (null)
          // : (
          //   <span className={styles.operateMenu}>
          //     {/* {data.additions.IS_FAVORITE === 'true' ? (
          //   <Icon type="star" theme="filled" style={{ color: '#faad14' }} onClick={() => { this.collectTemp(data.id, false) }} />
          // ) : (<Icon type="star" theme="twoTone" onClick={() => { this.collectTemp(data.id, true) }} />
          //   )} */}
          //     <Dropdown overlay={menu} trigger={['click']}>
          //       <Icon type="ellipsis" key="ellipsis" style={{ marginLeft: '5px' }} />
          //     </Dropdown>
          //   </span>
          // );
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
    });
  }

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
    const { selectedRowKeys, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.state;
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
    const hasSelected = selectedRowKeys.length > 0;
    return (
      <PageHeaderWrapper>
        <div className={styles.templateWrapper}>
          <div className={styles.tableTopHeader}>
            <Row gutter={16} className={styles.bottomSpace}>
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
              <Col>
                <Search placeholder={formatMessage({ id: 'button.search' })} className={styles.Search} onChange={this.handleSearch} allowClear />
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
