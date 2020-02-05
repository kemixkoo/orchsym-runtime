import React from 'react';
import { Modal, Form, message, Input, Table, Tabs, Icon, Tooltip, Select } from 'antd';
import { connect } from 'dva';
import { getClientId } from '@/utils/authority';
import { formatMessage, getLocale } from 'umi-plugin-react/locale';
import styles from '../index.less';

const { TabPane } = Tabs;
const FormItem = Form.Item;
const { TextArea } = Input;
const { Option } = Select;
class ConfigServices extends React.Component {
  state = {
    subVisible: false,
    descriptorsList: [],
    propertyObj: {},
    setValueVisible: false,
    propertyName: '',
    editObj: '',
    editId: null,
  };

  componentDidMount() {
    const { dispatch, nowService } = this.props;
    dispatch({
      type: 'controllerServices/fetchSingleService',
      payload: nowService.id,
      cb: res => {
        const obj = res.descriptors
        const referencedServiceDescriptors = []
        Object.keys(obj).forEach(key => {
          Object.assign(obj[key], { propertyValue: res.properties[key] })
          referencedServiceDescriptors.push(obj[key])
        })
        console.log(referencedServiceDescriptors)
        this.setState({
          descriptorsList: referencedServiceDescriptors,
        })
      },
    });
  }

  handleSubmit = () => {
    const {
      dispatch,
      refreshList,
      nowService,
      configService,
    } = this.props;
    const { propertyObj } = this.state
    const obj = {
      serviceId: nowService.id,
      body: {
        revision:
        {
          clientId: getClientId(),
          version: 0,
        },
        component: {
          id: configService.id,
          name: configService.name,
          comments: configService.comments,
          properties: propertyObj,
        },
      },
    }
    dispatch({
      type: 'controllerServices/fetchUpdateServiceConfig',
      payload: obj,
      cb: res => {
        message.success(formatMessage({ id: 'result.success' }));
        refreshList();
        this.handleCancel();
      },
    });
  };

  handleCancel = (val) => {
    const {
      handleCancel,
      form: { resetFields },
    } = this.props;
    resetFields();
    if (val === 'Property') {
      this.setState({
        subVisible: false,
      });
    } else if (val === 'setValue') {
      this.setState({
        setValueVisible: false,
        editId: null,
      });
    } else {
      handleCancel('CONFIG');
    }
  };

  addProperty = () => {
    this.setState({
      subVisible: true,
    });
  }

  handlePropertyOk = e => {
    const {
      form: { validateFields },
    } = this.props;

    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        this.setState({
          subVisible: false,
          setValueVisible: true,
          propertyName: values.propertyName,
        })
      }
    });
  }

  handleSetValueOk = e => {
    const {
      form: { validateFields },
    } = this.props;
    const { propertyObj, propertyName, descriptorsList, editId } = this.state
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        propertyObj[propertyName] = values.propertyValue
        if (editId === null) {
          descriptorsList.unshift(
            {
              name: propertyName,
              displayName: propertyName,
              propertyValue: values.propertyValue,
              description: '',
              required: false,
              sensitive: false,
              dynamic: true,
              supportsEl: false,
            }
          )
        } else {
          descriptorsList[editId].propertyValue = values.propertyValue
        }
        this.setState({
          setValueVisible: false,
          propertyObj,
          descriptorsList,
        })
      }
    });
  }

  deletePropertyHandle = (name, index) => {
    const { descriptorsList, propertyObj } = this.state
    descriptorsList.splice(index, 1)
    propertyObj[name] = null
    this.setState({
      descriptorsList,
      propertyObj,
    })
  }

  editPropertyHandle = (text, record, index) => {
    this.setState({
      propertyName: record.displayName,
      editObj: record,
      editId: index,
      setValueVisible: true,
    })
  }

  render() {
    const {
      form: { getFieldDecorator },
      visible,
      nowService,
    } = this.props;
    const { subVisible, descriptorsList, setValueVisible, editObj } = this.state
    const columns = [
      {
        title: formatMessage({ id: 'service.table.property' }),
        dataIndex: 'displayName',
        key: 'displayName',
        width: 200,
        render: (text, record) => (
          <div className={styles.tooltip}>{text}
            <Tooltip title={record.description}>
              <Icon type="question-circle" className={styles.tooltipIcon} />
            </Tooltip>
          </div>
        ),
      },
      {
        title: formatMessage({ id: 'service.table.value' }),
        dataIndex: 'propertyValue',
        key: 'propertyValue',
        render: (text, record, index) => {
          if (nowService.state !== 'ENABLED') {
            return (
              <div onClick={() => this.editPropertyHandle(text, record, index)}>{tableValue(text, record)} </div>
            )
          } else {
            return tableValue(text, record)
          }
        },
      },
      {
        title: formatMessage({ id: 'title.operate' }),
        dataIndex: 'operate',
        key: 'operate',
        width: 100,
        render: (text, record, index) => {
          return (record.dynamic && (nowService.state !== 'ENABLED')) && (<Icon type="delete" onClick={() => this.deletePropertyHandle(record.displayName, index)} />)
        },
      },
    ];

    const tableValue = (text, record) => {
      if (record.sensitive) {
        return <span className={styles.noData}>Sensitive value set</span>
      } else if (text) {
        return text
      } else {
        return <span className={styles.noData}> no value set</span>
      }
    }

    const getLeftValue = () => {
      if (getLocale() === 'zh-CN') {
        return '153px';
      } else {
        return '263px';
      }
    }
    return (
      <Modal
        visible={visible}
        bodyStyle={{ paddingTop: '10px' }}
        title={formatMessage({ id: 'service.title.configService' })}
        onCancel={this.handleCancel}
        onOk={this.handleSubmit}
        okButtonProps={{ disabled: nowService.state === 'ENABLED' }}
        okText={formatMessage({ id: 'button.submit' })}
        cancelText={formatMessage({ id: 'button.cancel' })}
        width={700}
      >
        <span className={styles.titleConfigId} style={{ left: getLeftValue() }}>{nowService.id}</span>
        <Tabs defaultActiveKey="1">
          <TabPane tab={formatMessage({ id: 'service.config.property' })} key="1">
            {nowService.state !== 'ENABLED' && (<Icon type="plus-square" className={styles.addProperty} onClick={() => { this.addProperty() }} />)}
            <Table
              bordered
              columns={columns}
              dataSource={descriptorsList}
              size="small"
              scroll={{ y: 200 }}
              pagination={false}
              rowKey="type"

            />
          </TabPane>
          <TabPane tab={formatMessage({ id: 'service.config.about' })} key="2">
            <div> </div>
          </TabPane>
        </Tabs>
        {
          subVisible && (
            <Modal
              title={formatMessage({ id: 'service.config.addProperty' })}
              visible={subVisible}
              onOk={this.handlePropertyOk}
              onCancel={() => this.handleCancel('Property')}
            >
              <Form>
                <FormItem label={formatMessage({ id: 'service.config.propertyName' })}>
                  {getFieldDecorator('propertyName', {
                    rules: [
                      { required: true, message: formatMessage({ id: 'validation.propertyName.required' }) },
                    ],
                  })(
                    <Input />
                  )}
                </FormItem>
              </Form>
            </Modal>
          )
        }
        {
          setValueVisible && (
            <Modal
              // title="Basic Modal"
              visible={setValueVisible}
              onOk={this.handleSetValueOk}
              onCancel={() => this.handleCancel('setValue')}
            >
              <div style={{ marginTop: 20 }}>
                <Form>
                  {editObj.allowableValues && editObj.allowableValues.length > 0 ? (
                    <FormItem>
                      {getFieldDecorator('propertyValue', {
                        initialValue: editObj.propertyValue || '',
                      })(
                        <Select
                          style={{ width: '100%' }}
                        >
                          <Option value="">no value set</Option>
                          {editObj.allowableValues.map(item => <Option key={item.allowableValue.displayName} value={item.allowableValue.value}>{item.allowableValue.displayName}</Option>)}
                        </Select>
                      )}
                    </FormItem>
                  ) :
                    (
                      <FormItem>
                        {getFieldDecorator('propertyValue', {
                          // rules: [
                          //   { required: true, message: formatMessage({ id: 'validation.propertyValue.required' }) },
                          // ],
                          initialValue: editObj.propertyValue || '',
                        })(
                          <TextArea autosize={{ minRows: 3, maxRows: 5 }} />
                        )}
                      </FormItem>
                    )}
                </Form>
              </div>

            </Modal>
          )
        }
      </Modal>
    );
  }
}

export default connect(({ controllerServices, application }) => ({
  controllerServices,
  configService: controllerServices.configService,
}))(Form.create()(ConfigServices));
