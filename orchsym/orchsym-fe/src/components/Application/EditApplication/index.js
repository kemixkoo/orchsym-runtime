import React from 'react';
import { Modal, Input, Form } from 'antd';
import styles from '../application.less';

const FormItem = Form.Item;

class EditApplication extends React.Component {
  render() {
    const { form: { getFieldDecorator }, visible, handleEditCancel } = this.props;
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

    return (
      <div>
        <Modal
          visible={visible}
          title="编辑应用"
          onCancel={handleEditCancel}
          onOk={handleEditCancel}
          okText="确定"
          cancelText="取消"
        >
          <Form {...formItemLayout}>
            <FormItem label="应用名称">
              {getFieldDecorator('appName', {
                rules: [{
                  required: true, message: '请输入应用名称!',
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
                <Input className={styles.describe} />
              )}
            </FormItem>
            <FormItem label="标签设置">
              {getFieldDecorator('appTagSet', {
                // rules: [{
                //   required: true, message: '请输入应用名称!',
                // }],
              })(
                <Input />
              )}
            </FormItem>
          </Form>
        </Modal>
      </div>
    );
  }
}

export default (Form.create()(EditApplication));
