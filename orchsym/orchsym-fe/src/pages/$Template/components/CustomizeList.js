import React from 'react';
import { Form, Table } from 'antd';
import { connect } from 'dva';
import EditableCell from '@/components/EditableCell';
import { EditableContext } from '@/utils/utils'
// import Ellipsis from '@/components/Ellipsis';

@connect(({ template }) => ({
  collectList: template.collectList,
}))

class CustomizeList extends React.Component {
  constructor(props) {
    super(props);
    this.state = { editingKey: '' };
    this.columns = [
      {
        title: '名称',
        dataIndex: 'component.name',
        editable: true,
      },
      {
        title: '类型',
        dataIndex: 'component.type',
      },
      // {
      //   title: '作用域',
      //   dataIndex: 'type',
      // },
      // {
      //   title: '引用组件',
      //   dataIndex: 'time',
      // },
      // {
      //   title: '服务状态',
      //   dataIndex: 'time',
      // },
      {
        title: '操作',
        render: (text, record) => {
          const { editingKey } = this.state;
          const editable = this.isEditing(record);
          return editable ? (
            <span>
              <EditableContext.Consumer>
                {form => (
                  <a
                    onClick={() => this.save(form, record.key)}
                    style={{ marginRight: 8 }}
                  >
                    Save
                  </a>
                )}
              </EditableContext.Consumer>
              {/* <Popconfirm title="Sure to cancel?" onConfirm={() => this.cancel(record.key)}>
                <a>Cancel</a>
              </Popconfirm> */}
            </span>
          ) : (
            <a disabled={editingKey !== ''} onClick={() => this.edit(record.id)}>Edit</a>
          );
        },
      },
    ];
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'controllerServices/fetchControllerServices',
    });
  }

  isEditing = record => {
    const { editingKey } = this.state;
    return record.id === editingKey;
  }

  cancel = () => {
    this.setState({ editingKey: '' });
  };

  // save(form, key) {
  //   form.validateFields((error, row) => {
  //     if (error) {
  //       return;
  //     }
  //     const newData = [...dataSource];
  //     const index = newData.findIndex(item => key === item.key);
  //     if (index > -1) {
  //       const item = newData[index];
  //       newData.splice(index, 1, {
  //         ...item,
  //         ...row,
  //       });
  //       this.setState({ data: newData, editingKey: '' });
  //     } else {
  //       newData.push(row);
  //       this.setState({ data: newData, editingKey: '' });
  //     }
  //   });
  // }

  edit(key) {
    this.setState({ editingKey: key });
  }

  render() {
    const { form, collectList } = this.props;
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
          record: record.component,
          // inputType: col.dataIndex === 'age' ? 'number' : 'text',
          dataIndex: col.dataIndex.split('.')[1],
          // title: col.title,
          editing: this.isEditing(record),
        }),
      };
    });

    return (
      <EditableContext.Provider value={form}>
        <Table
          components={components}
          dataSource={collectList}
          columns={columns}
          rowKey="id"
          rowClassName="editable-row"
          pagination={{
            onChange: this.cancel,
          }}
        />
      </EditableContext.Provider>
    );
  }
}
export default (Form.create()(CustomizeList));
