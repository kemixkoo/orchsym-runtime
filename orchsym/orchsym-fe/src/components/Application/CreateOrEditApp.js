import React from 'react';
import { connect } from 'dva';
import { Modal, Input, Form, Select } from 'antd';

const { TextArea } = Input;
const FormItem = Form.Item;
const { Option } = Select;

@connect(({ application }) => ({
  parentId: application.parentId,
  details: application.details,
}))
class CreateOrEditApp extends React.Component {
  // handleOk = () => {

  // }
  componentWillUnmount() {
    const { dispatch } = this.props
    dispatch({
      type: 'application/appendValue',
      payload: {
        details: {},
      },
    });
  }

  handleCreateEditOk = (e) => {
    const { parentId, details, dispatch, handleCreateEditCancel, form: { validateFields, resetFields } } = this.props;
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        if (Object.keys(details).length === 0) {
          dispatch({
            type: 'application/fetchAddApplication',
            payload: {
              values,
              parentId,
            },
          });
        } else {
          dispatch({
            type: 'application/fetchEditApplication',
            payload: {
              values,
              details,
            },
          })
        }
        handleCreateEditCancel();
      }
    });
    resetFields();
  }

  handleCancel = () => {
    const { handleCreateEditCancel, form: { resetFields } } = this.props;
    resetFields();
    handleCreateEditCancel();
  }

  render() {
    const {
      form: { getFieldDecorator },
      visible,
      title,
      details: { component },
    } = this.props;
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 4 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 18 },
      },
    };
    const tags = [
      <Option value="数据同步" key="数据同步">数据同步</Option>,
      <Option value="格式转换" key="格式转换">格式转换</Option>,
      <Option value="全量同步" key="全量同步">全量同步</Option>,
    ]

    return (

      <div>
        <Modal
          visible={visible}
          title={title}
          onCancel={this.handleCancel}
          onOk={this.handleCreateEditOk}
          okText="确定"
          cancelText="取消"
          destroyOnClose
        >
          <Form {...formItemLayout}>
            <FormItem label="应用名称">
              {getFieldDecorator('name', {
                rules: [{
                  required: true, message: '应用名称不能为空!',
                }],
                initialValue: component ? component.name : '',
              })(
                <Input />
              )}
            </FormItem>
            <FormItem label="应用描述">
              {getFieldDecorator('comments', {
                // rules: [{
                //   required: true, message: '请输入应用名称!',
                // }],
              })(
                <TextArea rows={4} />
              )}
            </FormItem>
            <FormItem label="标签设置">
              {getFieldDecorator('tags', {
                // rules: [{
                //   required: true, message: '请输入应用名称!',
                // }],
              })(
                <Select
                  mode="tags"
                  style={{ width: '100%' }}
                  onChange={this.handleSetTags}
                >
                  {tags}
                </Select>
              )}
            </FormItem>
          </Form>
        </Modal>
      </div>

    );
  }
}

export default (Form.create()(CreateOrEditApp));
