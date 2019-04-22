import React from 'react';
import { formatMessage } from 'umi-plugin-react/locale';

export default () => (
  <div>
    {
      formatMessage({ id: 'page.template.remote.content' })
    }
  </div>
)
