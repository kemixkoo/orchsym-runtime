import React from 'react';
import { Modal, Form, message, Input, Select } from 'antd';
import { connect } from 'dva';
import { formatMessage } from 'umi-plugin-react/locale';

const FormItem = Form.Item;
const { Option } = Select;
class MoveOrCope extends React.Component {
  state = {
  };

  componentDidMount() {
    const { dispatch } = this.props;
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
      refreshList,
      dispatch,
      handleCancel,
      form: { validateFields, resetFields },
      nowService,
      selectedRowKeys,
      modelState,
    } = this.props;
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        let body = {}
        if (nowService) {
          body = {
            id: nowService.id,
            values,
            state: modelState,
          }
        } else {
          body = {
            values: {
              controllerServiceIds: selectedRowKeys,
              ...values,
            },
            state: modelState,
          }
        }
        dispatch({
          type: 'controllerServices/fetchCopeServices',
          payload: body,
          cb: res => {
            message.success(formatMessage({ id: 'result.success' }));
            refreshList();
            resetFields();
            handleCancel();
          },
        });
      }
    });
  };

  handleCancel = () => {
    const {
      handleCopeCancel,
      form: { resetFields },
    } = this.props;
    resetFields();
    handleCopeCancel();
  };

  render() {
    const {
      form: { getFieldDecorator },
      visible, modelState,
      applicationNameList: { results },
    } = this.props;
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 6 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 18 },
      },
    };
    return (
      <Modal
        visible={visible}
        title={modelState === 'COPE' ? formatMessage({ id: 'service.title.copeTo' }) : formatMessage({ id: 'service.title.moveTo' })}
        onCancel={this.handleCancel}
        onOk={this.handleSubmit}
        okText={formatMessage({ id: 'button.submit' })}
        cancelText={formatMessage({ id: 'button.cancel' })}
      >
        <Form {...formItemLayout}>
          {modelState === 'COPE' && (
            <FormItem label={formatMessage({ id: 'service.form.name' })}>
              {getFieldDecorator('name', {
                rules: [
                  { required: true, message: formatMessage({ id: 'validation.name.required' }) },
                  { max: 20, message: formatMessage({ id: 'validation.name.placeholder' }) },
                  { whitespace: true, message: formatMessage({ id: 'validation.name.required' }) },
                ],
              })(
                <Input autoComplete="off" />
              )}
            </FormItem>
          )}

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
                <Option value="root">ROOT</Option>
                {results && results.map(item => (<Option value={item.id}>{item.name}</Option>))}
              </Select>

            )}
          </FormItem>
        </Form>
      </Modal>
    );
  }
}

export default connect(({ controllerServices, application }) => ({
  controllerServices,
  applicationNameList: application.applicationNameList,
}))(Form.create()(MoveOrCope));
