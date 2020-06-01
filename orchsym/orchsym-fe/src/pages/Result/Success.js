import React, { Fragment } from 'react';
import { formatMessage, FormattedMessage } from 'umi-plugin-react/locale';
import { Button, Card } from 'antd';
import Result from '@/components/Result';
import PageHeaderWrapper from '@/components/PageHeaderWrapper';

const extra = <div>额外的说明性内容</div>;

const actions = (
  <Fragment>
    <Button type="primary">
      <FormattedMessage id="app.result.success.btn-return" defaultMessage="Back to list" />
    </Button>
  </Fragment>
);

export default () => (
  <PageHeaderWrapper>
    <Card bordered={false}>
      <Result
        type="success"
        title={formatMessage({ id: 'app.result.success.title' })}
        description={formatMessage({ id: 'app.result.success.description' })}
        extra={extra}
        actions={actions}
        style={{ marginTop: 48, marginBottom: 16 }}
      />
    </Card>
  </PageHeaderWrapper>
);
