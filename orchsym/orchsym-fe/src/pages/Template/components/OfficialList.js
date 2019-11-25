import React, { PureComponent } from 'react';
import { Table } from 'antd';
import { connect } from 'dva';
import { formatMessage, getLocale } from 'umi-plugin-react/locale';
import Ellipsis from '@/components/Ellipsis';
import moment from 'moment';
import OperateMenu from './OperateMenu';

@connect(({ template }) => ({
  officialList: template.officialList,
}))
class OfficialList extends PureComponent {
  state = {
  };


  componentDidMount() {
    const { pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
  }

  componentDidUpdate(prevProps, prevState) {
    const { onStateChange, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    // 如果数据发生变化，则更新图表
    if ((prevProps.searchVal !== searchVal)) {
      this.getList(1, pageSizeNum, sortedField, isDesc, searchVal)
      onStateChange({
        pageNum: 1,
      })
    }
  }

  getList = (page, pageSize, sortedField, isDesc, text) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'template/fetchOfficialTemplates',
      payload: {
        page,
        pageSize,
        text,
        sortedField,
        isDesc,
      },
    });
  }

  onFrechList = () => {
    const { pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props
    this.getList(pageNum, pageSizeNum, sortedField, isDesc, searchVal)
  }

  render() {
    const { officialList: { results, totalSize }, onFrechList,
      onStateChange, pageNum, pageSizeNum, searchVal, sortedField, isDesc, match } = this.props;
    const columns = [
      {
        title: `${formatMessage({ id: 'title.name' })}`,
        dataIndex: 'name',
        key: 'name',
        // render: text => <a>{text}</a>,
      },
      {
        title: `${formatMessage({ id: 'title.description' })}`,
        dataIndex: 'description',
        key: 'description',
        render: (text, record) => (
          <Ellipsis tooltip length={23}>
            {text || '-'}
          </Ellipsis>
        ),
      },
      {
        title: `${formatMessage({ id: 'title.createTime' })}`,
        dataIndex: 'timestamp',
        key: 'time',
        render: (text, record) => {
          const time = moment(text).format('YYYY-MM-DD HH:mm:ss');
          return <span>{time}</span>;
        },
      },
      {
        title: `${formatMessage({ id: 'title.operate' })}`,
        key: 'operate',
        render: (text, record) => (<OperateMenu match={match} data={record} onFrechList={onFrechList} />),
      },
    ];

    const showTotal = (total) => {
      if (getLocale() === 'zh-CN') {
        return `共 ${total} 个`;
      } else {
        return `Total ${total} items`;
      }
    }
    return (
      <div>
        <Table
          columns={columns}
          dataSource={results}
          rowKey="id"
          pagination={{
            size: 'small',
            onChange: (page, pageSize) => {
              this.getList(page, pageSize, sortedField, isDesc, searchVal)
              onStateChange({
                pageNum: page,
                pageSizeNum: pageSize,
              })
            },
            onShowSizeChange: (current, size) => {
              this.getList(current, size, sortedField, isDesc, searchVal)
              onStateChange({
                pageNum: current,
                pageSizeNum: size,
              })
            },
            current: pageNum,
            pageSize: pageSizeNum,
            total: totalSize,
            showTotal,
            pageSizeOptions: ['10', '20'],
            showSizeChanger: true,
            showQuickJumper: true,
          }}
        // loading={loading}
        />
      </div>
    );
  }
}
export default OfficialList
