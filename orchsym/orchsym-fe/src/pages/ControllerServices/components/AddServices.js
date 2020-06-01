import React from 'react';
import { Modal, Form, message, Input, Select, Table } from 'antd';
import { connect } from 'dva';
import { cloneDeep } from 'lodash';
import Ellipsis from '@/components/Ellipsis';
import { getClientId } from '@/utils/authority';
import { formatMessage } from 'umi-plugin-react/locale';
import styles from '../index.less';

const FormItem = Form.Item;
const { Option } = Select;
const { Search } = Input;
class AddServices extends React.Component {
  state = {
    sortedInfo: {},
    selectRow: null,
    typeList: [],
  };

  componentDidMount() {
    const { dispatch, serviceTypes } = this.props;
    this.setState({
      selectRow: serviceTypes[0],
      typeList: cloneDeep(serviceTypes),
    })
    dispatch({
      type: 'application/fetchApplication',
      payload: {
        page: 1,
        pageSize: -1,
      },
    });
  }

  handleSubmit = e => {
    const {
      dispatch,
      refreshList,
      form: { validateFields },
    } = this.props;
    const { selectRow } = this.state
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        const obj = {
          groupId: values.groupId,
          body: {
            revision:
            {
              clientId: getClientId(),
              version: 0,
            },
            component: {
              type: selectRow.type,
              bundle: selectRow.bundle,
            },
          },
        }
        dispatch({
          type: 'controllerServices/fetchAddServices',
          payload: obj,
          cb: res => {
            message.success(formatMessage({ id: 'result.success' }));
            refreshList();
            this.handleCancel();
          },
        });
      }
    });
  };

  handleCancel = () => {
    const {
      handleCancel,
      form: { resetFields },
    } = this.props;
    resetFields();
    handleCancel('ADD');
  };

  handleChange = (pagination, filters, sorter) => {
    this.setState({
      sortedInfo: sorter,
    });
  };

  render() {
    const {
      form: { getFieldDecorator },
      visible,
      applicationNameList: { results },
    } = this.props;
    const { sortedInfo, selectRow, typeList } = this.state
    const subValue = val => {
      return val.substring(val.lastIndexOf('.') + 1)
    }
    const columns = [
      {
        title: '类型',
        dataIndex: 'type',
        key: 'type',
        width: 170,
        sorter: (a, b) => {
          const text1 = subValue(a.type)
          const text2 = subValue(b.type)
          return text1.localeCompare(text2)
        },
        sortOrder: sortedInfo.columnKey === 'type' && sortedInfo.order,
        render: (text, record) => (
          <Ellipsis length={20}>{subValue(text)}</Ellipsis>
        ),
      },
      {
        title: '版本',
        dataIndex: 'bundle.version',
        key: 'version',
        width: 150,
        sorter: (a, b) => a.bundle.version.localeCompare(b.bundle.version),
        sortOrder: sortedInfo.columnKey === 'version' && sortedInfo.order,
      },
      {
        title: '标签',
        dataIndex: 'tags',
        key: 'tags',
        sorter: (a, b) => a.tags.join(',').localeCompare(b.tags.join(',')),
        sortOrder: sortedInfo.columnKey === 'tags' && sortedInfo.order,
        render: (text, record) => (
          <Ellipsis length={35}>{text.join(', ')}</Ellipsis>
        ),
      },
    ];

    const onRowHandle = obj => {
      this.setState({
        selectRow: obj,
      })
    }

    const setClassName = record => { // record代表表格行的内容，index代表行索引
      // 判断索引相等时添加行的高亮样式
      return record === selectRow ? `${styles.activeRow}` : '';
    }

    const handleSearch = e => {
      const { serviceTypes } = this.props;
      const newList = serviceTypes.filter(val => subValue(val.type.toLowerCase()).match(e.target.value.toLowerCase()));
      this.setState({
        typeList: newList,
      })
    }

    return (
      <Modal
        visible={visible}
        title={formatMessage({ id: 'service.title.addService' })}
        onCancel={this.handleCancel}
        onOk={this.handleSubmit}
        okText={formatMessage({ id: 'button.submit' })}
        cancelText={formatMessage({ id: 'button.cancel' })}
        width={700}
      >
        <Form layout="inline" style={{ marginBottom: '15px' }}>
          <FormItem label={formatMessage({ id: 'service.form.scope' })}>
            {getFieldDecorator('groupId', {
              rules: [
                { required: true, message: formatMessage({ id: 'validation.groupId.required' }) },
              ],
            })(
              <Select
                showSearch
                allowClear
                style={{ width: 200 }}
                optionFilterProp="children"
                filterOption={(input, option) =>
                  option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                }
              >
                <Option value="root">{formatMessage({ id: 'text.global' })}</Option>
                {results && results.map(item => (<Option value={item.id}>{item.name}</Option>))}
              </Select>

            )}
          </FormItem>
        </Form>
        <Search
          placeholder={formatMessage({ id: 'button.search' })}
          className={styles.filterSearch}
          onChange={handleSearch}
          allowClear
        />
        <Table
          onRow={record => {
            return {
              onClick: () => onRowHandle(record), // 点击行
            };
          }}

          columns={columns}
          dataSource={typeList}
          onChange={this.handleChange}
          size="small"
          scroll={{ y: 200 }}
          pagination={false}
          rowKey="type"
          rowClassName={setClassName}

        />
        {selectRow && (
          <div className={styles.serviceInfo}>
            <h3>{selectRow.type} {selectRow.bundle.version}</h3>
            <p>{selectRow.description}</p>
          </div>
        )}

      </Modal>
    );
  }
}

export default connect(({ controllerServices, application }) => ({
  controllerServices,
  serviceTypes: controllerServices.serviceTypes,
  applicationNameList: application.applicationNameList,
}))(Form.create()(AddServices));
