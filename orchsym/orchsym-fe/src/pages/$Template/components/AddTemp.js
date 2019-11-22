import React from 'react';
import { Modal, Spin, Form, Input, Button, Upload, Icon, message, Select } from 'antd';
import { connect } from 'dva';
// import { getToken } from '@/utils/authority';

const { TextArea } = Input;
const { Option } = Select;
class AddTemp extends React.Component {
  state = {
    fileList: [],
    loading: false,
  };

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'confTemp/fetchTempType',
    });
  }

  handleSubmit = e => {
    const {
      dispatch,
      handleCancel,
      form: { validateFields, resetFields },
    } = this.props;
    e.preventDefault();
    validateFields((err, values) => {
      if (!err) {
        dispatch({
          type: 'confTemp/checkTempName',
          payload: {
            names: values.names,
          },
          cb: res => {
            console.log(res);
            if (res.code === '1') {
              dispatch({
                type: 'confTemp/fetchAddTemp',
                payload: {
                  queryData: {
                    names: values.names,
                    descs: values.descs,
                    type: values.type,
                  },
                  file: this.state.fileList,
                },
                cb: () => {
                  dispatch({
                    type: 'confTemp/fetchConfType',
                  });
                  resetFields();
                  handleCancel();
                },
              });
            } else {
              message.error(`${res.massage}`);
              resetFields();
              handleCancel();
            }
          },
        });
      }
    });
  };

  normFile = e => {
    if (Array.isArray(e)) {
      return e;
    }
    return e && e.fileList;
  };

  handleUpload = info => {
    if (info.file.status === 'uploading') {
      this.setState({ loading: true });
    }
    if (info.file.status === 'done') {
      this.setState({
        loading: false,
      });
    }
  };

  handleFileName = (rule, value, callback) => {
    if (!value) {
      callback([new Error('!')]);
    } else if (!(value[0].name.endsWith('.xml') || value[0].name.endsWith('.json'))) {
      callback([new Error('目前仅支持.xml/.json格式文件')]);
    } else {
      callback();
    }
    callback();
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
      title,
      handleCancel,
      tempType,
      loading,
    } = this.props;
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
      // action: '/api/template/upload?names=qqqq&descs=1111&type=1',
      // headers: {
      //   authorization: `Bearer ${getToken()}`,
      // },
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
        console.log(file);
        this.setState({
          fileList: [file],
        });
        const isAccept = file.name.endsWith('.xml') || file.name.endsWith('.json');
        // const isCsv = file.type === 'text/csv';
        if (!isAccept) {
          message.error('目前仅支持.xml/.json格式文件');
        }
        return isAccept;
      },
      onChange: this.handleUpload,
    };
    return (
      <Modal
        visible={visible}
        title={title}
        onCancel={this.handleCancel}
        onOk={this.handleSubmit}
        okText="确定"
        cancelText="取消"
      >
        <Spin spinning={loading || false}>
          <Form {...formItemLayout}>
            <Form.Item label="模板名称">
              {getFieldDecorator('names', {
                rules: [
                  { required: true, message: '模板名称不能为空！' },
                  { max: 20, message: '模板名称不能超过20个字符！' },
                  { whitespace: true, message: '模板名称不能包含空格！' },
                ],
                // initialValue: editNodeId && nodeDetail.nodeName,
              })(<Input placeholder="请输入" />)}
            </Form.Item>
            <Form.Item label="模板类型">
              {getFieldDecorator('type', {
                rules: [{ required: true, message: '模板类型不能为空！' }],
                // initialValue: editNodeId && nodeDetail.nodeDesc,
              })(
                <Select placeholder="请选择">
                  {tempType.map(item => (
                    <Option value={item.id}>{item.names}</Option>
                  ))}
                </Select>,
              )}
            </Form.Item>
            <Form.Item label="模板描述">
              {getFieldDecorator('descs', {
                rules: [
                  { required: true, message: '模板描述不能为空！' },
                  { required: false, max: 100, message: '模板描述不能超过100个字符！' },
                ],
                // initialValue: editNodeId && nodeDetail.nodeDesc,
              })(<TextArea placeholder="描述信息，可选填，最多100字符" rows={4} />)}
            </Form.Item>
            <Form.Item label="配置文件" extra="支持扩展名：.xml .json">
              {getFieldDecorator('file', {
                rules: [
                  { required: true, message: '配置文件不能为空' },
                  { validator: this.handleFileName },
                ],
                valuePropName: 'fileList',
                getValueFromEvent: this.normFile,
              })(
                <Upload {...props}>
                  <Button loading={this.state.loading} disabled={this.state.fileList.length === 1}>
                    <Icon type="upload" /> 上传文件
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

export default connect(({ confTemp, loading }) => ({
  tempType: confTemp.tempType,
  loading: loading.effects['confTemp/checkTempName'] || loading.effects['confTemp/fetchAddTemp'],
}))(Form.create()(AddTemp));
