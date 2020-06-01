import * as React from 'react';

export interface EllipsisProps {
  tooltip?: boolean;
  length?: number;
  lines?: number;
}

export default class Ellipsis extends React.PureComponent<EllipsisProps, any> {}
