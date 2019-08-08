import React from 'react';
import { Modal, Input, Form, Select } from 'antd';

const { TextArea } = Input;
const FormItem = Form.Item;
const { Option } = Select;

class CreateOrEditApp extends React.Component {
  // handleOk = () => {

  // }
  handleCreateEditOk = (e) => {
    const { title, appId, dispatch, form: { validateFields } } = this.props;
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        if (title === '编辑应用') {
          dispatch({
            type: 'application/editApplication',
            payload: {
              values,
              appId,
            },
          })
        } else {
          dispatch({
            type: 'application/addApplication',
            payload: {
              values,
              appId,
            },
          })
        }
      }
    });
  }

  render() {
    const {
      form: { getFieldDecorator },
      visible,
      handleCreateEditCancel,
      title,
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
          onCancel={handleCreateEditCancel}
          onOk={this.handleCreateEditOk}
          okText="确定"
          cancelText="取消"
        >
          <Form {...formItemLayout}>
            <FormItem label="应用名称">
              {getFieldDecorator('appName', {
                rules: [{
                  required: true, message: '应用名称不能为空!',
                }],
              })(
                <Input />
              )}
            </FormItem>
            <FormItem label="应用描述">
              {getFieldDecorator('appDescribe', {
                // rules: [{
                //   required: true, message: '请输入应用名称!',
                // }],
              })(
                <TextArea rows={4} />
              )}
            </FormItem>
            <FormItem label="标签设置">
              {getFieldDecorator('appTagSet', {
                // rules: [{
                //   required: true, message: '请输入应用名称!',
                // }],
              })(
                <Select
                  mode="multiple"
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
