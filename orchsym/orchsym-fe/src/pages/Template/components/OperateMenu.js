import React from 'react';
import { Menu, Icon, Dropdown, Modal, message } from 'antd';
import { connect } from 'dva';
import { formatMessage } from 'umi-plugin-react/locale';
import styles from '../index.less';

const { confirm } = Modal;
@connect(({ template }) => ({
  template,
}))
class operateMenu extends React.Component {
  collectTemp = (id, state) => {
    const { dispatch, onFrechList } = this.props;
    dispatch({
      type: 'template/fetchCollectTemp',
      payload: {
        id,
        state,
      },
      cb: () => {
        message.success(formatMessage({ id: 'result.success' }));
        onFrechList()
      },
    });
  }

  deleteTempHandel = (id) => {
    const { dispatch, onFrechList } = this.props;
    confirm({
      title: formatMessage({ id: 'template.delete.title' }),
      content: formatMessage({ id: 'template.delete.description' }),
      okText: formatMessage({ id: 'button.yes' }),
      okType: 'warning',
      cancelText: formatMessage({ id: 'button.no' }),
      onOk() {
        dispatch({
          type: 'template/fetchDeleteTemplates',
          payload: {
            templateId: id,
            name,
          },
          cb: () => {
            message.success(formatMessage({ id: 'result.success' }));
            onFrechList()
          },
        })
      },
      onCancel() {
        console.log('Cancel');
      },
    });
  }

  // 下载
  downloadTemp = (templateId, name) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'template/fetchDownloadTemplates',
      payload: {
        templateId,
        name,
      },
    });
  }

  render() {
    const { data, edit, editingKey } = this.props;
    const { match } = this.props
    const { tab } = match.params;
    const tabKey = (!tab || tab === ':tab') ? 'favorite' : tab;
    const menu = (
      <Menu style={{ width: '80px' }}>
        {(data.additions.SOURCE_TYPE === 'OFFICIAL' || tabKey === 'official') ? (null) : (
          <Menu.Item key="edit" disabled={editingKey !== ''} onClick={() => edit(data.id)}>
            {`${formatMessage({ id: 'button.edit' })}`}
          </Menu.Item>
        )}
        {(data.additions.IS_FAVORITE === 'true' || tabKey === 'favorite') ? (
          <Menu.Item key="cancelCollect" onClick={() => { this.collectTemp(data.id, false) }}>
            {`${formatMessage({ id: 'button.cancelCollect' })}`}
          </Menu.Item>
        ) : (
          <Menu.Item key="favorite" onClick={() => { this.collectTemp(data.id, true) }}>
            {`${formatMessage({ id: 'button.favorite' })}`}
          </Menu.Item>
        )}
        <Menu.Item key="download" onClick={() => { this.downloadTemp(data.id, data.name) }}>
          {`${formatMessage({ id: 'button.download' })}`}
        </Menu.Item>
        {(data.additions.SOURCE_TYPE === 'OFFICIAL' || tabKey === 'official') ? (null) : (
          <Menu.Item key="delete" onClick={() => { this.deleteTempHandel(data.id) }}>
            {`${formatMessage({ id: 'button.delete' })}`}
          </Menu.Item>
        )}
      </Menu>
    );
    return (
      <span className={styles.operateMenu}>
        {(data.additions.IS_FAVORITE === 'true' || tabKey === 'favorite') ? (
          <Icon type="star" theme="filled" style={{ color: '#faad14' }} onClick={() => { this.collectTemp(data.id, false) }} />
        ) : (<Icon type="star" onClick={() => { this.collectTemp(data.id, true) }} />)}
        <Dropdown overlay={menu} trigger={['click']}>
          <Icon type="ellipsis" key="ellipsis" style={{ marginLeft: '8px' }} />
        </Dropdown>
      </span>
    )
  }
}
export default operateMenu;
