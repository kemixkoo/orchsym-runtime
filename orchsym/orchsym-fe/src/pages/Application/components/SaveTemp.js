/* eslint-disable react/prop-types */
import React from 'react';
import { Modal, Form, Input, Select, Radio } from 'antd';
import { connect } from 'dva';
import { formatMessage } from 'umi-plugin-react/locale';

const { TextArea } = Input;

const FormItem = Form.Item;
const { Option } = Select;

@connect(({ template }) => ({
  customList: template.customList,
}))
class SaveTemp extends React.Component {
  state = {
    isCheck: 1,
    selTemplate: null,
  }

  componentDidMount() {
  }

  handleCheckBox = e => {
    this.setState({
      isCheck: e.target.value,
    })
  }

  handleSaveTemp = (e) => {
    e.preventDefault();
    const { isCheck, selTemplate } = this.state
    const { dispatch, appItem, handleSaveCancel, form: { validateFields, resetFields } } = this.props;
    validateFields((err, values) => {
      if (!err) {
        if (isCheck === 1) {
          dispatch({
            type: 'template/fetchSaveTemplate',
            payload: {
              appId: appItem.id,
              overwrite: false,
              ...values,
            },
          });
        } else if (isCheck === 2) {
          dispatch({
            type: 'template/fetchSaveTemplate',
            payload: {
              appId: appItem.id,
              overwrite: true,
              ...selTemplate,
            },
          });
        }
        resetFields();
        handleSaveCancel();
      }
    });
  }

  handleCancel = () => {
    const { handleSaveCancel, form: { resetFields } } = this.props;
    this.setState({
      isCheck: 1,
      selTemplate: null,
    })
    resetFields();
    handleSaveCancel();
  }

  render() {
    const { isCheck } = this.state;
    const { form: { getFieldDecorator }, visible, customList: { results } } = this.props;
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
    // const tags = [
    // <Option value="数据同步" key="数据同步">数据同步</Option>,
    // <Option value="格式转换" key="格式转换">格式转换</Option>,
    // <Option value="全量同步" key="全量同步">全量同步</Option>,
    // ]
    const onTemplateChange = (value) => {
      this.setState({
        selTemplate: results[value],
      })
    }

    const checkReName = (rule, value, callback) => {
      const { dispatch } = this.props;
      if (value) {
        const queryData = {
          name: value,
          templateId: '',
        }
        dispatch({
          type: 'application/fetchCheckTempName',
          payload: queryData,
          cb: (res) => {
            if (res.isValid) {
              callback();
            } else {
              callback([new Error(formatMessage({ id: 'validation.name.duplicate' }))]);
            }
          },
        });
      } else {
        callback();
      }
    }
    return (
      <Modal
        visible={visible}
        title={formatMessage({ id: 'title.saveTemp' })}
        onCancel={this.handleCancel}
        onOk={this.handleSaveTemp}
        okText={formatMessage({ id: 'button.submit' })}
        cancelText={formatMessage({ id: 'button.cancel' })}
      >
        <div style={{ textAlign: 'center', marginBottom: '10px' }}>
          <Radio.Group onChange={this.handleCheckBox} value={isCheck}>
            <Radio value={1}>{formatMessage({ id: 'form.template.tempNew' })}</Radio>
            <Radio value={2}>{formatMessage({ id: 'form.template.tempOverwrite' })}</Radio>
          </Radio.Group>
        </div>
        <Form {...formItemLayout}>
          {/* <FormItem style={{ paddingLeft: '10px' }}>
            {getFieldDecorator('checkbox', {
              valuePropName: 'checked',
              initialValue: false,
              rules: [{
                validator: this.handleCheckBox,
              }],
            })(
              <Checkbox>{formatMessage({ id: 'form.template.tempOverwrite' })}</Checkbox>
            )}
          </FormItem> */}
          {(isCheck === 2) ? (
            <FormItem label={formatMessage({ id: 'form.template.selectTemp' })}>
              {getFieldDecorator('appTagSelect', {
                rules: [{
                  required: true, message: formatMessage({ id: 'validation.name.required' }),
                }],
              })(
                <Select
                  showSearch
                  style={{ width: 200 }}
                  onChange={onTemplateChange}
                  filterOption={(input, option) =>
                    option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                  }
                >
                  {results.map((item, index) => (<Option key={item.id} value={index}>{item.name}</Option>))}
                </Select>,
              )}
            </FormItem>
          ) : (
            <div>
              <FormItem label={formatMessage({ id: 'form.template.tempName' })}>
                {getFieldDecorator('name', {
                  rules: [
                    { required: true, message: formatMessage({ id: 'validation.name.required' }) },
                    { max: 20, message: formatMessage({ id: 'validation.name.placeholder' }) },
                    { whitespace: true, message: formatMessage({ id: 'validation.name.required' }) },
                    { validator: checkReName },
                  ],
                })(
                  <Input autoComplete="off" />
                )}
              </FormItem>
              <FormItem label={formatMessage({ id: 'form.template.tempDescription' })}>
                {getFieldDecorator('description', {
                  rules: [{
                    required: false, max: 100, message: formatMessage({ id: 'validation.description.placeholder' }),
                  }],
                })(
                  <TextArea rows={4} />
                )}
              </FormItem>
              {/* <FormItem label="标签设置">
    {getFieldDecorator('appTagSet', {
      rules: [{
        required: true, message: '请输入应用名称!',
      }],
    })(
      <Select
        mode="multiple"
        style={{ width: '100%' }}
        onChange={this.handleSetTags}
      >
        {tags}
      </Select>
    )}
  </FormItem> */}
            </div>
          )}
        </Form>
      </Modal>
    );
  }
}

export default (Form.create()(SaveTemp));
