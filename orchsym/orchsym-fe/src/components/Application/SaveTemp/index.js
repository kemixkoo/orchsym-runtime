import React from 'react';
import { Modal, Form, Input, Select, Checkbox } from 'antd';
import styles from '../application.less';

const FormItem = Form.Item;
const { Option } = Select;

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

  render() {
    const { isCheck } = this.state;
    const { form: { getFieldDecorator }, visible, handleSaveCancel } = this.props;
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
      <Modal
        key="saveTemp"
        visible={visible}
        title="存为模板"
        onCancel={handleSaveCancel}
        onOk={handleSaveCancel}
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
                {getFieldDecorator('tempName', {
                  rules: [{
                    required: true, message: '应用名称不能为空!',
                  }],
                })(
                  <Input />
                )}
              </FormItem>
              <FormItem label="模版描述">
                {getFieldDecorator('tempDescribe', {
                  // rules: [{
                  //   required: true, message: '请输入应用名称!',
                  // }],
                })(
                  <Input className={styles.describe} />
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
