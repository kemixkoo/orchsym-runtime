import React from 'react';
import { Modal, Form, Input, Select, Checkbox } from 'antd';
import { connect } from 'dva';

const { TextArea } = Input;

const FormItem = Form.Item;
// const { Option } = Select;

@connect(({ application }) => ({
  details: application.details,
}))
class SaveTemp extends React.Component {
  state = {
    isCheck: false,
  }

  handleCheckBox = (rule, value, callback) => {
    this.setState({
      isCheck: value,
    })
    callback();
  }

  handleSaveTemp = (e) => {
    e.preventDefault();
    const { dispatch, appItem, handleSaveCancel, form: { validateFields, resetFields } } = this.props;
    validateFields((err, values) => {
      if (!err) {
        console.log('!err')
        dispatch({
          type: 'application/fetchCreateSnippets',
          payload: appItem,
          cb: (res) => {
            dispatch({
              type: 'application/fetchCreateAppTemp',
              payload: {
                id: appItem.id,
                snippetId: res.id,
                values,
              },
            });
          },
        });
        handleSaveCancel();
      }
    });
    resetFields();
  }

  handleCancel = () => {
    const { handleSaveCancel, form: { resetFields } } = this.props;
    resetFields();
    handleSaveCancel();
  }

  render() {
    const { isCheck } = this.state;
    const { form: { getFieldDecorator }, visible } = this.props;
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
      // <Option value="数据同步" key="数据同步">数据同步</Option>,
      // <Option value="格式转换" key="格式转换">格式转换</Option>,
      // <Option value="全量同步" key="全量同步">全量同步</Option>,
    ]
    return (
      <Modal
        visible={visible}
        title="存为模板"
        onCancel={this.handleCancel}
        onOk={this.handleSaveTemp}
        okText="确定"
        cancelText="取消"
      >
        <Form {...formItemLayout}>
          <FormItem style={{ paddingLeft: '10px' }}>
            {getFieldDecorator('checkbox', {
              valuePropName: 'checked',
              initialValue: false,
              rules: [{
                validator: this.handleCheckBox,
              }],
            })(
              <Checkbox>覆盖已有模版</Checkbox>
            )}
          </FormItem>
          {(isCheck) ? (
            <FormItem label="选择模板">
              {getFieldDecorator('appTagSelect', {
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
          ) : (
            <div>
              <FormItem label="模版名称">
                {getFieldDecorator('name', {
                  rules: [{
                    required: true, message: '应用名称不能为空!',
                  }],
                })(
                  <Input autocomplete="off" />
                )}
              </FormItem>
              <FormItem label="模版描述">
                {getFieldDecorator('description', {
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
            </div>
          )}
        </Form>
      </Modal>
    );
  }
}

export default (Form.create()(SaveTemp));
