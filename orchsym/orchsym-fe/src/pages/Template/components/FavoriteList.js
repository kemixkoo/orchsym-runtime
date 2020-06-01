import React from 'react';
import { Form, Table, message } from 'antd';
import { connect } from 'dva';
import { formatMessage, getLocale } from 'umi-plugin-react/locale';
import EditableCell from '@/components/EditableCell';
import Ellipsis from '@/components/Ellipsis';
import moment from 'moment';
import { EditableContext } from '@/utils/utils'
import OperateMenu from './OperateMenu';

@connect(({ template }) => ({
  favoriteList: template.favoriteList,
}))

class FavoriteList extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      editingKey: '',
    };
    this.columns = [
      {
        title: `${formatMessage({ id: 'title.name' })}`,
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
        title: `${formatMessage({ id: 'title.description' })}`,
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
        title: `${formatMessage({ id: 'title.type' })}`,
        dataIndex: 'additions.SOURCE_TYPE',
        key: 'type',
        render: (text, record) => (text === 'OFFICIAL' ? formatMessage({ id: 'template.tab.official' }) : formatMessage({ id: 'template.tab.customize' })),
      },
      {
        title: `${formatMessage({ id: 'title.create' })}/${formatMessage({ id: 'title.createTime' })}`,
        dataIndex: 'timestamp',
        key: 'time',
        render: (text, record) => {
          const time = moment(text).format('YYYY-MM-DD HH:mm:ss');
          return <span>{time}</span>;
        },
      },
      {
        title: `${formatMessage({ id: 'title.operate' })}`,
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
    const { onStateChange, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    // 如果数据发生变化，则更新图表
    if ((prevProps.searchVal !== searchVal)) {
      this.getList(1, pageSizeNum, sortedField, isDesc, searchVal)
      onStateChange({
        pageNum: 1,
      })
    }
  }

  getList = (page, pageSize, sortedField, isDesc, text) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'template/fetchCollectTemplates',
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
      //   const newData = [...dataSource];
      //   const index = newData.findIndex(item => key === item.key);
      //   if (index > -1) {
      //     const item = newData[index];
      //     newData.splice(index, 1, {
      //       ...item,
      //       ...row,
      //     });
      //     this.setState({ data: newData, editingKey: '' });
      //   } else {
      //     newData.push(row);
      //     this.setState({ data: newData, editingKey: '' });
      //   }
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
    const { form, favoriteList: { results, totalSize },
      onStateChange, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props;
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
export default (Form.create()(FavoriteList));
