import React from 'react';
import { List } from 'antd';
import styles from './index.less';

export default function NoticeList({
  data = [],
  onClick,
  onClear,
  title,
  locale,
  emptyText,
  emptyImage,
}) {
  if (data.length === 0) {
    return (
      <div className={styles.notFound}>
        {emptyImage ? <img src={emptyImage} alt="not found" /> : null}
        <div>{emptyText || locale.emptyText}</div>
      </div>
    );
  }
  return (
    <div>
      <List className={styles.list}>
        {data.map((item, i) => {
          return (
            <List.Item key={item.key || i} onClick={() => onClick(item)}>
              <List.Item.Meta
                className={styles.meta}
                title={
                  <div className={styles.title}>
                    {item.title}
                    <div className={styles.extra}>{item.extra}</div>
                  </div>
                }
                description={
                  <div>
                    <div className={styles.description} title={item.description}>
                      {item.description}
                    </div>
                    <div className={styles.datetime}>{item.datetime}</div>
                  </div>
                }
              />
            </List.Item>
          );
        })}
      </List>
    </div>
  );
}
