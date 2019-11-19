import React from 'react';
import { connect } from 'dva';
import { Modal, Input, Form, Select } from 'antd';
import { formatMessage } from 'umi-plugin-react/locale';

const { TextArea } = Input;
const FormItem = Form.Item;
// const { Option } = Select;

@connect(({ application }) => ({
  // parentId: application.parentId,
  appDetails: application.appDetails,
}))
class CreateOrEditApp extends React.Component {
  // handleOk = () => {

  // }
  componentWillUnmount() {
    const { dispatch } = this.props
    dispatch({
      type: 'application/appendValue',
      payload: {
        appDetails: {},
      },
    });
  }

  handleCreateEditOk = (e) => {
    const { appDetails, dispatch, handleCreateEditCancel, form: { validateFields, resetFields } } = this.props;
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        const queryData = {
          name: values.name,
          appId: '',
        }
        if (Object.keys(appDetails).length > 0) {
          queryData.appId = appDetails.id
        }
        dispatch({
          type: 'application/fetchValidationCheckName',
          payload: queryData,
          cb: (res) => {
            if (res.isValid) {
              if (Object.keys(appDetails).length === 0) {
                dispatch({
                  type: 'application/fetchAddApplication',
                  payload: {
                    values,
                    // parentId,
                  },
                });
              } else {
                dispatch({
                  type: 'application/fetchEditApplication',
                  payload: {
                    values,
                    appDetails,
                  },
                })
              }
              resetFields();
              handleCreateEditCancel();
            } else {
              Modal.error({
                title: formatMessage({ id: 'model.warning' }),
                content: formatMessage({ id: 'validation.appName.duplicate' }),
              });
            }
          },
        });
      }
    });
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
      appDetails: { component },
    } = this.props;
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
    };
    const tags = [
      // <Option value="数据同步" key="数据同步">数据同步</Option>,
      // <Option value="格式转换" key="格式转换">格式转换</Option>,
      // <Option value="全量同步" key="全量同步">全量同步</Option>,
    ]
    // const validTag = (rule, value, callback) => {
    //   if (value && value.length > 3) {
    //     callback([new Error(formatMessage({ id: 'validation.tag.placeholder1' }))]);
    //   }
    //   if (value) {
    //     value.forEach(item => {
    //       if (item.length > 5) {
    //         return callback([new Error(formatMessage({ id: 'validation.tag.placeholder2' }))]);
    //       }
    //     })
    //   }
    //   callback();
    // }
    return (

      <div>
        <Modal
          visible={visible}
          title={title}
          onCancel={this.handleCancel}
          onOk={this.handleCreateEditOk}
          okText={formatMessage({ id: 'form.submit' })}
          cancelText={formatMessage({ id: 'form.cancel' })}
          destroyOnClose
        >
          <Form {...formItemLayout}>
            <FormItem label={formatMessage({ id: 'form.application.appName' })}>
              {getFieldDecorator('name', {
                rules: [
                  { required: true, message: formatMessage({ id: 'validation.appName.required' }) },
                  { max: 20, message: formatMessage({ id: 'validation.appName.placeholder' }) },
                  { whitespace: true, message: formatMessage({ id: 'validation.appName.required' }) },
                ],
                initialValue: component ? component.name : '',
              })(
                <Input autocomplete="off" />
              )}
            </FormItem>
            <FormItem label={formatMessage({ id: 'form.application.appDescription' })}>
              {getFieldDecorator('comments', {
                rules: [{ required: false, max: 100, message: formatMessage({ id: 'validation.appDescription.placeholder' }) }],
                initialValue: component ? component.comments : '',
              })(
                <TextArea rows={4} />
              )}
            </FormItem>
            <FormItem label={formatMessage({ id: 'form.application.appTag' })}>
              {getFieldDecorator('tags', {
                // rules: [
                //   { validator: validTag },
                // ],
                initialValue: component && component.tags && component.tags.length > 0 ? component.tags : [],
              })(
                <Select
                  mode="tags"
                  style={{ width: '100%' }}
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
