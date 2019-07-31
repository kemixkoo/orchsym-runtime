import React from '@/components/logList/node_modules/react';
import { connect } from '@/components/logList/node_modules/dva';
import classNames from '@/components/logList/node_modules/classnames';
import { List, Row, Col } from '@/components/logList/node_modules/antd';
import styles from './index.less';

class LogList extends React.Component {
  componentWillMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'global/fetchNotices',
    })
  }

  render() {
    const { notices } = this.props;
    return (
      <List className={styles.list}>
        {notices.map((item, i) => {
          const itemCls = classNames(styles.item, {
            [styles.read]: item.read,
          });
          // eslint-disable-next-line no-nested-ternary
          // const leftIcon = item.avatar ? (
          //   typeof item.avatar === 'string' ? (
          //     <Avatar className={styles.avatar} src={item.avatar} />
          //   ) : (
          //     <span className={styles.iconElement}>{item.avatar}</span>
          //   )
          // ) : null;

          return (
            <List.Item className={itemCls} key={item.key || i}>
              <List.Item.Meta
                className={styles.meta}
                title={
                  <Row>
                    <Col span={6}>
                      <span className={styles.datetime}>{item.datetime}</span>
                    </Col>
                    <Col span={4}>
                      <span className={styles.extra}>{item.extra}</span>
                    </Col>
                    <Col span={14}>
                      <span className={styles.title}>{item.title}</span>
                    </Col>
                  </Row>
                }
                description={
                  <div>
                    <div className={styles.description} title={item.description}>
                      {item.description}
                    </div>
                  </div>
                }
              />
            </List.Item>
          );
        })}
      </List>
    );
  }
}
export default connect(({ global }) => ({
  notices: global.notices,
}))(LogList);
