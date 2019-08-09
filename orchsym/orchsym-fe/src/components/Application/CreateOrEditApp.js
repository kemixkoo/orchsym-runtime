import React from 'react';
import { connect } from 'dva';
import { Modal, Input, Form, Select } from 'antd';

const { TextArea } = Input;
const FormItem = Form.Item;
const { Option } = Select;

@connect(({ application }) => ({
  parentId: application.parentId,
  details: application.details,
  revision: application.revision,
}))
class CreateOrEditApp extends React.Component {
  // handleOk = () => {

  // }
  handleCreateEditOk = (e) => {
    const { title, parentId, appId, revision, dispatch, handleCreateEditCancel, form: { validateFields, resetFields } } = this.props;
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        if (title === '编辑应用') {
          dispatch({
            type: 'application/fetchEditApplication',
            payload: {
              values,
              appId,
              revision,
            },
          })
        } else {
          dispatch({
            type: 'application/fetchAddApplication',
            payload: {
              values,
              parentId,
            },
          });
        }
        handleCreateEditCancel();
      }
    });
    resetFields();
  }

  render() {
    const {
      form: { getFieldDecorator },
      visible,
      handleCreateEditCancel,
      title,
      details,
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
      <Option value="数据同步">数据同步</Option>,
      <Option value="格式转换">格式转换</Option>,
      <Option value="全量同步">全量同步</Option>,
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
              {getFieldDecorator('name', {
                rules: [{
                  required: true, message: '应用名称不能为空!',
                }],
                initialValue: title === '编辑应用' ? details.name : '',
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
                  showArrow="true"
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
