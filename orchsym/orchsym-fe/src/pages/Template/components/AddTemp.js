import React from 'react';
import { Button, Upload, Icon, Modal, Spin, Form, message } from 'antd';
import { connect } from 'dva';
import { formatMessage } from 'umi-plugin-react/locale';
// import { getToken } from '@/utils/authority';

// const { TextArea } = Input;
class AddTemp extends React.Component {
  state = {
    fileList: [],
    uploadLoading: false,
  };

  componentDidMount() {
  }

  handleSubmit = e => {
    const {
      dispatch,
      handleCancel,
      onStateChange,
      form: { validateFields, resetFields },
    } = this.props;
    const { fileList } = this.state
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        dispatch({
          type: 'template/fetchUploadTemp',
          payload: {
            file: fileList,
          },
          cb: res => {
            message.success(formatMessage({ id: 'result.success' }));
            onStateChange({
              pageNum: 1,
              searchVal: '',
            })
            this.freshList(1)
            resetFields();
            handleCancel();
          },
        });
      }
    });
  };

  freshList = (page) => {
    const { pageSizeNum, searchVal, sortedField, isDesc, dispatch } = this.props;
    dispatch({
      type: 'template/fetchCustomTemplates',
      payload: {
        page,
        pageSize: pageSizeNum,
        text: searchVal,
        sortedField,
        isDesc,
      },
    });
  }

  normFile = e => {
    if (Array.isArray(e)) {
      return e;
    }
    return e && e.fileList;
  };

  handleUpload = info => {
    if (info.file.status === 'uploading') {
      this.setState({ uploadLoading: true });
    }
    if (info.file.status === 'done') {
      this.setState({
        uploadLoading: false,
      });
    }
  };

  handleFileName = (rule, value, callback) => {
    if (value) {
      if (!(value[0].name.endsWith('.xml'))) {
        callback([new Error(formatMessage({ id: 'validation.file.format' }))]);
      } else {
        callback();
      }
    } else {
      callback();
    }
  };

  handleCancel = () => {
    const {
      handleCancel,
      form: { resetFields },
    } = this.props;
    resetFields();
    handleCancel();
  };

  render() {
    const {
      form: { getFieldDecorator },
      visible,
      loading,
    } = this.props;
    const { fileList, uploadLoading } = this.state
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
    const props = {
      name: 'file',
      showUploadList: true,
      onRemove: file => {
        this.setState(state => {
          const index = state.fileList.indexOf(file);
          const newFileList = state.fileList.slice();
          newFileList.splice(index, 1);
          return {
            fileList: newFileList,
          };
        });
      },
      beforeUpload: file => {
        this.setState({
          fileList: [file],
        });
        const isAccept = file.name.endsWith('.xml');
        if (!isAccept) {
          message.error(formatMessage({ id: 'validation.file.format' }));
        }
        return isAccept;
      },
      onChange: this.handleUpload,
    };
    return (
      <Modal
        visible={visible}
        title={formatMessage({ id: 'template.title.uploadTemp' })}
        onCancel={this.handleCancel}
        onOk={this.handleSubmit}
        okText={formatMessage({ id: 'button.submit' })}
        cancelText={formatMessage({ id: 'button.cancel' })}
      >
        <Spin spinning={loading || false}>
          <Form {...formItemLayout}>
            {/* <Form.Item label="模板名称">
              {getFieldDecorator('names', {
                rules: [
                  { required: true, message: '模板名称不能为空！' },
                  { max: 20, message: '模板名称不能超过20个字符！' },
                  { whitespace: true, message: '模板名称不能包含空格！' },
                ],
                // initialValue: editNodeId && nodeDetail.nodeName,
              })(<Input placeholder="请输入" />)}
            </Form.Item> */}
            {/* <Form.Item label="模板描述">
              {getFieldDecorator('descs', {
                rules: [
                  { required: true, message: '模板描述不能为空！' },
                  { required: false, max: 100, message: '模板描述不能超过100个字符！' },
                ],
                // initialValue: editNodeId && nodeDetail.nodeDesc,
              })(<TextArea placeholder="描述信息，可选填，最多100字符" rows={4} />)}
            </Form.Item> */}
            <Form.Item label={formatMessage({ id: 'form.template.selectFile' })} extra={`${formatMessage({ id: 'text.extra.extension' })}：.xml`}>
              {getFieldDecorator('file', {
                rules: [
                  { required: true, message: formatMessage({ id: 'validation.file.required' }) },
                  { validator: this.handleFileName },
                ],
                valuePropName: 'fileList',
                getValueFromEvent: this.normFile,
              })(
                <Upload {...props}>
                  <Button loading={uploadLoading} disabled={fileList.length === 1}>
                    <Icon type="upload" /> {formatMessage({ id: 'form.template.uploadFile' })}
                  </Button>
                </Upload>,
              )}
            </Form.Item>
          </Form>
        </Spin>
      </Modal>
    );
  }
}

export default connect(({ template, loading }) => ({
  template,
  loading: loading.effects['template/fetchUploadTemp'],
}))(Form.create()(AddTemp));
