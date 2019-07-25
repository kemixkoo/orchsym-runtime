import React from 'react';
import { Modal, Input, Form } from 'antd';
import styles from '../application.less';

const FormItem = Form.Item;

class CreateApplication extends React.Component {
  // handleOk = () => {

  // }
  render() {
    const { form: { getFieldDecorator }, visible, handleCreateCancel } = this.props;
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
          title="创建应用"
          onCancel={handleCreateCancel}
          onOk={handleCreateCancel}
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

export default (Form.create()(CreateApplication));
