import React from 'react';
import { Form, Table, message } from 'antd';
import { connect } from 'dva';
import { formatMessage, getLocale } from 'umi-plugin-react/locale';
import EditableCell from '@/components/EditableCell';
import Ellipsis from '@/components/Ellipsis';
import { EditableContext } from '@/utils/utils'
import moment from 'moment';
import OperateMenu from './OperateMenu';

@connect(({ template }) => ({
  customList: template.customList,
}))

class CustomList extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      editingKey: '',
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
        title: formatMessage({ id: 'title.description' }),
        dataIndex: 'description',
        key: 'description',
        render: (text, record) => (
          <Ellipsis tooltip length={23}>
            {text || '-'}
          </Ellipsis>
        ),
        rules: [{
          required: false, max: 100, message: formatMessage({ id: 'validation.description.placeholder' }),
        }],
        editable: true,
      },
      {
        title: formatMessage({ id: 'template.table.title.source' }),
        dataIndex: 'additions.SOURCE_TYPE',
        key: 'type',
        render: (text, record) => {
          if (text === 'SAVE_AS') {
            return formatMessage({ id: 'template.source.saved' })
          } else if (text === 'UPLOADED') {
            return formatMessage({ id: 'template.source.uploaded' })
          } else {
            return formatMessage({ id: 'template.source.unknown' })
          }
        },
      },
      {
        title: formatMessage({ id: 'template.table.title.operateUser' }),
        dataIndex: 'additions.CREATED_USER',
        key: 'user',
      },
      {
        title: formatMessage({ id: 'template.table.title.createTime' }),
        dataIndex: 'additions.CREATED_TIMESTAMP',
        key: 'time',
        render: (text, record) => (
          moment(Number(text)).format('YYYY-MM-DD HH:mm:ss')
        ),

      },
      {
        title: formatMessage({ id: 'title.operate' }),
        width: 150,
        render: (text, record) => {
          const { match } = this.props
          const { editingKey } = this.state;
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
          ) : (<OperateMenu match={match} data={record} editingKey={editingKey} edit={item => this.edit(item)} onFrechList={this.onFrechList} />);
        },
      },
    ];
  }

  componentDidMount() {
    const { pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
  }

  componentDidUpdate(prevProps, prevState) {
    const { onStateChange, pageNum, pageSizeNum, searchVal, sortedField, isDesc, isFresh } = this.props
    // 如果数据发生变化，则更新图表
    if (isFresh) {
      this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
      onStateChange({
        isFresh: false,
      })
    }
  }

  getList = (page, pageSize, sortedField, isDesc, text) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'template/fetchCustomTemplates',
      payload: {
        page,
        pageSize,
        text,
        sortedField,
        isDesc,
      },
    });
  }

  onFrechList = () => {
    const { pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
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

  onSelectChange = selectedRowKeys => {
    const { onStateChange } = this.props;
    onStateChange({
      selectedRowKeys,
    })
  };

  render() {
    const { form, customList: { results, totalSize }, selectedRowKeys,
      onStateChange, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props;
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
    return (
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
              onStateChange({
                pageNum: page,
                pageSizeNum: pageSize,
              })
            },
            onShowSizeChange: (current, size) => {
              this.getList(current, size, sortedField, isDesc, searchVal)
              onStateChange({
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
    );
  }
}
export default (Form.create()(CustomList));
