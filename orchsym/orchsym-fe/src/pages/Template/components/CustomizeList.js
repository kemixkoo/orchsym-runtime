import React from 'react';
import { Form, Table, Menu, Icon, Dropdown } from 'antd';
import { connect } from 'dva';
import { formatMessage, getLocale } from 'umi-plugin-react/locale';
import EditableCell from '@/components/EditableCell';
import Ellipsis from '@/components/Ellipsis';
import { EditableContext } from '@/utils/utils'
import styles from '../index.less';

@connect(({ template }) => ({
  customList: template.customList,
}))

class CustomizeList extends React.Component {
  constructor(props) {
    super(props);
    this.state = { editingKey: '' };
    this.columns = [
      {
        title: `${formatMessage({ id: 'title.name' })}`,
        dataIndex: 'name',
        key: 'name',
        // render: text => <a>{text}</a>,
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
      },
      // {
      //   title: '来源',
      //   dataIndex: 'type',
      // },
      // {
      //   title: '创建时间',
      //   dataIndex: 'time',
      // },
      {
        title: `${formatMessage({ id: 'title.operate' })}`,
        render: (text, record) => {
          // const { editingKey } = this.state;
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
          ) : (this.operateMenu(record));
        },
      },
    ];
  }

  componentDidMount() {
    const { pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
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

  isEditing = record => {
    const { editingKey } = this.state;
    return record.id === editingKey;
  }

  cancel = () => {
    this.setState({ editingKey: '' });
  };

  operateMenu = (item) => {
    const { editingKey } = this.state;
    console.log(item)
    // <a disabled={editingKey !== ''} onClick={() => this.edit(record.id)}>Edit</a>
    const menu = (
      <Menu>
        <Menu.Item key="edit" disabled={editingKey !== ''} onClick={() => this.edit(item.id)}>
          {`${formatMessage({ id: 'button.edit' })}`}
        </Menu.Item>
        <Menu.Item key="collect">
          {`${formatMessage({ id: 'button.collect' })}`}
        </Menu.Item>
        <Menu.Item key="cancelCollect">
          {`${formatMessage({ id: 'button.cancelCollect' })}`}
        </Menu.Item>
        <Menu.Item key="download">
          {`${formatMessage({ id: 'button.download' })}`}
        </Menu.Item>
        <Menu.Item key="delete">
          {`${formatMessage({ id: 'button.delete' })}`}
        </Menu.Item>
      </Menu>
    );
    return (
      <span className={styles.operateMenu}>
        <Icon type="star" theme="filled" style={{ color: '#faad14' }} />
        {/* <Icon type="star" theme="twoTone" /> */}
        <Dropdown overlay={menu} trigger={['click']}>
          <Icon type="ellipsis" key="ellipsis" style={{ marginLeft: '5px' }} />
        </Dropdown>
      </span>
    )
  }
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
    const { form, customList: { results, totalSize },
      onSearchChange, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props;
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
              this.getAppList(page, pageSize, sortedField, isDesc, searchVal)
              onSearchChange({
                pageNum: page,
                pageSizeNum: pageSize,
              })
            },
            onShowSizeChange: (current, size) => {
              this.getAppList(current, size, sortedField, isDesc, searchVal)
              onSearchChange({
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
export default (Form.create()(CustomizeList));
