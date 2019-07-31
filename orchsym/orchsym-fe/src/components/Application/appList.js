import React, { PureComponent } from 'react';
import { Card, Menu, Icon, Dropdown, Badge, Divider, Tag, List } from 'antd';
import styles from './AppList.less';
import SaveTemp from './SaveTemp';
import IconFont from '@/components/IconFont';
import LogList from '../LogList';

export default class AppList extends PureComponent {
  state = {
    saveTempVisible: null,
    isError: true,
  };

  showSaveTemp = () => {
    this.setState({
      saveTempVisible: true,
    })
  }

  handleSaveCancel = () => {
    this.setState({
      saveTempVisible: false,
    })
  }


  getCarList = (item) => {
    const menu = (
      <Menu>
        <Menu.Item key="1">
          <Icon type="appstore" />
          进入应用
        </Menu.Item>
        <Menu.Item key="2" onClick={this.showEditModal}>
          <Icon type="edit" />
          编辑应用
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="3">
          <Icon type="play-square" />
          运行
        </Menu.Item>
        <Menu.Item key="4">
          <Icon type="stop" />
          停止
        </Menu.Item>
        <Menu.Item key="5">
          <Icon type="check-square" />
          启用
        </Menu.Item>
        <Menu.Item key="6">
          <Icon type="close-square" />
          禁用
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="7">
          <Icon type="copy" />
          复制
        </Menu.Item>
        <Menu.Item key="8">
          <Icon type="download" />
          下载
        </Menu.Item>
        <Menu.Item key="9">
          <Icon type="delete" />
          删除
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item key="10" onClick={this.showSaveTemp}>
          <Icon type="save" />
          存为模板
        </Menu.Item>
      </Menu>
    );

    const dropdown = (
      <Dropdown overlay={menu}>
        <a className="ant-dropdown-link" href="#">
          <Icon type="more" />
        </a>
      </Dropdown>
    );

    const menu2 = (
      <Menu style={{ fontSize: '12px' }}>
        <Menu.Item key="1">
          队列中
          <p className={styles.pStyle}>2（5.2 MB）</p>
        </Menu.Item>
        <Menu.Item key="2">
          输入
          <p className={styles.pStyle}>1（888.88 KB）</p>
        </Menu.Item>
        <Menu.Item key="3">
          输出
          <p className={styles.pStyle}>1（66 B）</p>
        </Menu.Item>
        <Menu.Item key="3">
          读 / 写
          <p className={styles.pStyle}>502 KB / 10.8 B</p>
        </Menu.Item>
      </Menu>
    );

    const dropdown2 = (
      <Dropdown overlay={menu2}>
        <div>
          <Badge count={0} dot className={styles.badgeIcon}>
            <Icon type="play-square" style={{ color: '#0f0' }} />
            <span>5</span>
          </Badge>
          <Badge count={0} dot className={styles.badgeIcon}>
            <Icon type="stop" style={{ color: '#f00' }} />
            <span>5</span>
          </Badge>
          <Badge count={0} dot className={styles.badgeIcon}>
            <Icon type="close-square" />
            <span>5</span>
          </Badge>
          {/* <a className="ant-dropdown-link" href="#">
            更多
          </a> */}
        </div>
      </Dropdown>
    );
    const { isError } = this.state;
    const isErrorCarName = isError ? `${styles.applicationCart} ${styles.errorApp}` : styles.applicationCart;
    return (
      // <Col xl={6} lg={6} md={12} sm={12} xs={24} style={{ marginBottom: 16 }}>
      <Card className={isErrorCarName} style={{ width: '100%', height: 165 }}>
        <Card.Meta
          title={
            <div>
              <IconFont type="iconAPIguanli" style={{ color: '#0087ff' }} className={styles.iconfont} />
              {/* <IconFont type="iconnaozhong" style={{ color: '#f28406' }} className={styles.iconfont} /> */}
              <span className={styles.cardTitle}>{item.name}</span>
            </div>}
          description={
            <p className={styles.lineEllipsis}>
              {item.describe}
            </p>
          }
        />
        <div className={styles.cardExtra}>{dropdown}</div>
        <div style={{ marginBottom: '10px' }}>
          <Tag color="blue">接口转换</Tag>
          <Tag color="blue">业务重组</Tag>
        </div>
        <Divider style={{ margin: 0 }} />
        <div className={styles.cardFoot}>
          {dropdown2}
        </div>
        <p className={styles.cardTime}>5小时前</p>
        {(isError) ? (
          <Dropdown overlay={<LogList />}>
            <span className={styles.triangle} />
          </Dropdown>
        ) : (null)}
      </Card>
      // </Col>
    );
  }

  render() {
    const appListData = [
      {
        name: '好应用会说话',
        describe: 'Unable to validate the access token.',
      },
      {
        name: '无法验证访问令牌',
        describe: 'UHover me, Click menu item.',
      },
      {
        name: '好应用会说话',
        describe: 'Unable to validate the access token.',
      },
      {
        name: '可以点击触发',
        describe: '支持 6 个弹出位置',
      },
      {
        name: '好应用会说话',
        describe: '默认是点击关闭菜单，可以关闭此功能.',
      },
      {
        name: 'Right Click on Me',
        describe: 'Menu.Item 必须设置唯一的 key 属性.',
      },
    ]
    // const Carlist = [];
    // appListData.forEach((item) => {
    //   Carlist.push(this.getCarList(item))
    // });
    const { saveTempVisible } = this.state;

    return (
      <div className={styles.infiniteContainer}>
        <List
          grid={{
            gutter: 16,
            xs: 1,
            sm: 2,
            md: 2,
            lg: 4,
            xl: 4,
            xxl: 4,
          }}
          dataSource={appListData}
          renderItem={item => (
            <List.Item>
              {this.getCarList(item)}
            </List.Item>
          )}
        />
        {/* {Carlist} */}
        <SaveTemp visible={saveTempVisible} handleSaveCancel={this.handleSaveCancel} />
      </div>
    );
  }
}
