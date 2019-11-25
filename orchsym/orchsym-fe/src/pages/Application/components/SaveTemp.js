/* eslint-disable react/prop-types */
import React from 'react';
import { Modal, Form, Input, Select, Checkbox } from 'antd';
import { connect } from 'dva';
import { formatMessage } from 'umi-plugin-react/locale';

const { TextArea } = Input;

const FormItem = Form.Item;
// const { Option } = Select;

@connect(({ application }) => ({
  appDetails: application.appDetails,
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
    // const tags = [
    // <Option value="数据同步" key="数据同步">数据同步</Option>,
    // <Option value="格式转换" key="格式转换">格式转换</Option>,
    // <Option value="全量同步" key="全量同步">全量同步</Option>,
    // ]
    const onChange = (value) => {
      console.log(`selected ${value}`);
    }

    const onBlur = () => {
      console.log('blur');
    }

    const onFocus = () => {
      console.log('focus');
    }

    const onSearch = (val) => {
      console.log('search:', val);
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
        <Form {...formItemLayout}>
          <FormItem style={{ paddingLeft: '10px' }}>
            {getFieldDecorator('checkbox', {
              valuePropName: 'checked',
              initialValue: false,
              rules: [{
                validator: this.handleCheckBox,
              }],
            })(
              <Checkbox>{formatMessage({ id: 'form.template.tempOverwrite' })}</Checkbox>
            )}
          </FormItem>
          {(isCheck) ? (
            <FormItem label={formatMessage({ id: 'form.template.selectTemp' })}>
              {getFieldDecorator('appTagSelect', {
                rules: [{
                  required: true, message: formatMessage({ id: 'validation.name.required' }),
                }],
              })(
                <Select
                  showSearch
                  style={{ width: 200 }}
                  placeholder="Select a person"
                  optionFilterProp="children"
                  onChange={onChange}
                  onFocus={onFocus}
                  onBlur={onBlur}
                  onSearch={onSearch}
                  filterOption={(input, option) =>
                    option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                  }
                >
                  {/* <Option value="jack">Jack</Option>
                  <Option value="lucy">Lucy</Option>
                  <Option value="tom">Tom</Option> */}
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
