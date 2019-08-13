import React, { PureComponent } from 'react';
import ApplicationHeader from './ApplicationHeader';
import AppList from './AppList';
// import IconFont from '@/components/IconFont';

export default class Application extends PureComponent {
  render() {
    return (
      <div>
        <ApplicationHeader />
        <AppList />
      </div>
    );
  }
}
