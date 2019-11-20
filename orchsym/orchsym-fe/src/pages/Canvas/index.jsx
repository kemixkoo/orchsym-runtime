import React, { Component } from 'react'
import { getCanvasUrl } from '@/utils/authority';
import Header from './CanvasHeader';
import styles from './index.less';

const logo = window.logoHref.companyLogoIndex
export default class Index extends Component {
  state = {
    componentName: '',
  };

  componentDidMount() {
    window.gotoCanvasApp = (d) => {
      console.log('processGroup', d)
      this.setState(
        {
          componentName: d.status.name,
        }
      )
      window.history.pushState({ title: d.status.name }, d.status.name, `https://${window.location.host}/canvas/${d.id}`);
    }
    console.log('this.canvasIframe', this.canvasIframe.contentWindow, this.canvasIframe.contentWindow.iframeEnterGroup)
  }

  render() {
    const { componentName } = this.state
    const { match, menuData } = this.props
    const { params } = match
    const { processGroupId } = params
    const src = `${getCanvasUrl()}?processGroupId=${processGroupId}&componentIds=`
    return (
      <div className={styles.canvasWrapper}>
        <Header
          componentName={componentName}
          menuData={menuData}
          handleMenuCollapse={this.handleMenuCollapse}
          logo={logo}
          {...this.props}
        />
        <iframe
          title="canvas"
          style={{ width: '100%', height: '100%', overflow: 'visible' }}
          src={src}
          ref={(R) => { this.canvasIframe = R }}
          width="100%"
          height="100%"
          scrolling="no"
          frameBorder="0"
        />
      </div>
    );
  }
}
