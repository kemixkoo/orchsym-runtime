import React, { PureComponent } from 'react';
import { Table, Icon, Dropdown, Menu } from 'antd';
import { connect } from 'dva';
import { formatMessage, getLocale } from 'umi-plugin-react/locale';
import Ellipsis from '@/components/Ellipsis';
import moment from 'moment';
import styles from '../index.less';

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

  operateMenu = (item) => {
    console.log(item)
    const menu = (
      <Menu>
        <Menu.Item key="edit">
          {`${formatMessage({ id: 'button.edit' })}`}
        </Menu.Item>
        <Menu.Item key="collect">
          {`${formatMessage({ id: 'button.collect' })}`}
        </Menu.Item>
        <Menu.Item key="cancelCollect">
          {`${formatMessage({ id: 'button.cancelCollect' })}`}
        </Menu.Item>
        <Menu.Item key="download">
          {`${formatMessage({ id: 'button.download' })}`}
        </Menu.Item>
        <Menu.Item key="delete">
          {`${formatMessage({ id: 'button.delete' })}`}
        </Menu.Item>
      </Menu>
    );
    return (
      <span className={styles.operateMenu}>
        <Icon type="star" theme="filled" style={{ color: '#faad14' }} />
        {/* <Icon type="star" theme="twoTone" /> */}
        <Dropdown overlay={menu} trigger={['click']}>
          <Icon type="ellipsis" key="ellipsis" style={{ marginLeft: '5px' }} />
        </Dropdown>
      </span>
    )
  }

  render() {
    const { officialList: { results, totalSize },
      onSearchChange, pageNum, pageSizeNum, searchVal, sortedField, isDesc } = this.props;
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
        render: (text, record) => (this.operateMenu(record)),
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
              this.getAppList(page, pageSize, sortedField, isDesc, searchVal)
              onSearchChange({
                pageNum: page,
                pageSizeNum: pageSize,
              })
            },
            onShowSizeChange: (current, size) => {
              this.getAppList(current, size, sortedField, isDesc, searchVal)
              onSearchChange({
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
