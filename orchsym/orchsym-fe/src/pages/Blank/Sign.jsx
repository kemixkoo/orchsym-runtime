import React from 'react';
import { formatMessage } from 'umi-plugin-react/locale';
import { H1 } from './styled'

const Sign = () => {
  return <H1>{formatMessage({ id: 'page.blank.sign' })}</H1>;
};

export default Sign;
