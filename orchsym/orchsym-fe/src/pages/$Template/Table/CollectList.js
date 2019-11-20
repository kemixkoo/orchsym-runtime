import React, { PureComponent } from 'react';
import { Table } from 'antd';
import { connect } from 'dva';
import Ellipsis from '@/components/Ellipsis';
import moment from 'moment';
// import styles from './index.less';

@connect(({ template }) => ({
  collectList: template.collectList,
}))
class CollectList extends PureComponent {
  state = {
  };


  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'template/fetchTemplates',
    });
  }

  render() {
    const { collectList } = this.props;
    // const dataSource = [
    //   {
    //     key: '1',
    //     name: '胡彦斌',
    //     descs: 32,
    //     type: '西湖区湖底公园1号',
    //     time: '10:10:10',
    //   },
    //   {
    //     key: '2',
    //     name: '胡彦斌',
    //     descs: 32,
    //     type: '西湖区湖底公园1号',
    //     time: '10:10:10',
    //   },
    // ];
    const columns = [
      {
        title: '名称',
        dataIndex: 'template.name',
        key: 'name',
        // render: text => <a>{text}</a>,
      },
      {
        title: '描述',
        dataIndex: 'template.description',
        key: 'description',
        render: (text, record) => (
          <Ellipsis tooltip length={23}>
            {text || '-'}
          </Ellipsis>
        ),
      },
      {
        title: '类型',
        dataIndex: 'type',
        key: 'type',
      },
      {
        title: '创建/发布时间',
        dataIndex: 'template.timestamp',
        key: 'time',
        render: (text, record) => {
          const time = moment(text).format('YYYY-MM-DD HH:mm:ss');
          return <span>{time}</span>;
        },
      },
      {
        title: '操作',
        key: 'operate',
        // render: (text, record) => {
        //   return <Dropdown overlay={menu} trigger={['click']}><a className="ant-dropdown-link" href="#"><Icon type="ellipsis" key="ellipsis" /></a></Dropdown>;
        // },
      },
    ];
    return (
      <div>
        <Table
          columns={columns}
          dataSource={collectList}
          rowKey="id"
          pagination={{
            size: 'small',
            defaultPageSize: 10,
            total: 20,
            showTotal: () => `共${20}个`,
            current: 1,
            onChange: this.changePage,
            showQuickJumper: true,
            showSizeChanger: true,
          }}
        // loading={loading}
        />
      </div>
    );
  }
}
export default CollectList
