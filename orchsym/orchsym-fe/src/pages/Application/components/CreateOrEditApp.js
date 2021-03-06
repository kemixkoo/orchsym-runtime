/* eslint-disable react/prop-types */
import React from 'react';
import { connect } from 'dva';
import { Modal, Input, Form, Select, message } from 'antd';
import { formatMessage } from 'umi-plugin-react/locale';

const { TextArea } = Input;
const FormItem = Form.Item;
// const { Option } = Select;

@connect(({ application }) => ({
  // parentId: application.parentId,
  appDetails: application.appDetails,
}))
class CreateOrEditApp extends React.Component {
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
    const { editOrCope, appDetails, handleCreateEditCancel, form: { validateFields, resetFields } } = this.props;
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        if (Object.keys(appDetails).length === 0) {
          this.addFetch(values)
        } else if (editOrCope === 'EDIT') {
          this.editFetch(values)
        } else {
          this.copeFetch(values)
        }
        resetFields();
        handleCreateEditCancel();
      }
    });
  }

  addFetch = (values) => {
    const { onSearchChange, dispatch } = this.props;
    dispatch({
      type: 'application/fetchAddApplication',
      payload: {
        values,
        // parentId,
      },
      cb: () => {
        message.success(formatMessage({ id: 'result.success' }));
        onSearchChange({
          pageNum: 1,
          searchVal: '',
        })
        this.freshAppList(1)
      },
    });
  }

  editFetch = (values) => {
    const { pageNum, appDetails, dispatch } = this.props;
    dispatch({
      type: 'application/fetchEditApplication',
      payload: {
        values,
        appDetails,
      },
      cb: () => {
        message.success(formatMessage({ id: 'result.success' }));
        this.freshAppList(pageNum)
      },
    })
  }

  copeFetch = (values) => {
    const { pageNum, appDetails, dispatch } = this.props;
    dispatch({
      type: 'application/fetchCopeApplication',
      payload: {
        values,
        appDetails,
      },
      cb: () => {
        message.success(formatMessage({ id: 'result.success' }));
        this.freshAppList(pageNum)
      },
    })
  }

  freshAppList = (page) => {
    const { pageSizeNum, searchVal, sortedField, isDesc, dispatch } = this.props;
    dispatch({
      type: 'application/fetchApplication',
      payload: {
        page,
        pageSize: pageSizeNum,
        isDetail: true,
        sortedField,
        isDesc,
        searchVal,
      },
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
    const checkReName = (rule, value, callback) => {
      const { dispatch, appDetails, editOrCope } = this.props;
      if (value) {
        const queryData = {
          name: value,
          appId: '',
        }
        if (editOrCope === 'EDIT' && Object.keys(appDetails).length > 0) {
          queryData.appId = appDetails.id
        }
        dispatch({
          type: 'application/fetchValidationCheckName',
          payload: queryData,
          cb: (res) => {
            console.log(res)
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
    const componentName = v => {
      const { editOrCope } = this.props;
      let str = ''
      if (v) {
        if (editOrCope === 'COPE') {
          str = `${v.name}_copy`
        } else {
          str = v.name
        }
      }
      return str
    }
    return (
      <div>
        <Modal
          visible={visible}
          title={title}
          onCancel={this.handleCancel}
          onOk={this.handleCreateEditOk}
          okText={formatMessage({ id: 'button.submit' })}
          cancelText={formatMessage({ id: 'button.cancel' })}
          destroyOnClose
        >
          <Form {...formItemLayout}>
            <FormItem label={formatMessage({ id: 'form.application.appName' })}>
              {getFieldDecorator('name', {
                rules: [
                  { required: true, message: formatMessage({ id: 'validation.name.required' }) },
                  { max: 20, message: formatMessage({ id: 'validation.name.placeholder' }) },
                  { whitespace: true, message: formatMessage({ id: 'validation.name.required' }) },
                  { validator: checkReName },
                ],
                initialValue: componentName(component),
              })(
                <Input autoComplete="off" />
              )}
            </FormItem>
            <FormItem label={formatMessage({ id: 'form.application.appDescription' })}>
              {getFieldDecorator('comments', {
                rules: [{ required: false, max: 100, message: formatMessage({ id: 'validation.description.placeholder' }) }],
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
