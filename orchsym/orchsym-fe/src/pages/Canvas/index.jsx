import React, { Component } from 'react'
import { getCanvasUrl } from '@/utils/authority';
import Header from './CanvasHeader';
import styles from './index.less';

const logo = window.logoHref.companyLogoIndex
export default class Index extends Component {
  render() {
    const { match, menuData } = this.props
    const { params } = match
    const { processGroupId } = params
    const src = `${getCanvasUrl()}?processGroupId=${processGroupId}&componentIds=`
    return (
      <div className={styles.canvasWrapper}>
        <Header
          menuData={menuData}
          handleMenuCollapse={this.handleMenuCollapse}
          logo={logo}
          {...this.props}
        />
        <iframe
          title="canvas"
          style={{ width: '100%', height: '100%', overflow: 'visible' }}
          src={src}
          // ref={(R) => { this.canvasIframe = R }}
          width="100%"
          height="100%"
          scrolling="no"
          frameBorder="0"
        />
      </div>
    );
  }
}
