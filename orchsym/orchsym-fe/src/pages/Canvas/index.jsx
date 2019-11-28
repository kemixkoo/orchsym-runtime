import React, { Component } from 'react'
import { getCanvasUrl } from '@/utils/authority';
import Header from './CanvasHeader';
import styles from './index.less';

const logo = window.logoHref.companyLogoIndex
export default class Index extends Component {
  state = {
    componentId: '',
  };

  componentDidMount() {
    window.gotoCanvasApp = (d) => {
      // console.log('processGroup', d)
      this.setState(
        {
          componentId: d.id,
        }
      )
      window.history.pushState({ title: d.status.name }, d.status.name, `https://${window.location.host}/canvas/${d.id}/0`);
    }
    // console.log('this.canvasIframe', this.canvasIframe.contentWindow, this.canvasIframe.contentWindow.iframeEnterGroup)
    window.gotoComponent = (processGroupId, componentIds) => {
      this.setState(
        {
          componentId: processGroupId,
        }
      )
      window.history.pushState({ title: processGroupId }, processGroupId, `https://${window.location.host}/canvas/${processGroupId}/${componentIds || 0}`);
    }
  }

  componentIdChange = (id) => {
    this.canvasIframe.contentWindow.iframeEnterGroup(id)
    this.setState(
      {
        componentId: id,
      }
    )
  }

  render() {
    const { componentId } = this.state
    const { match, menuData } = this.props
    const { params } = match
    const { processGroupId, componentIds } = params
    const compId = componentIds === '0' ? '' : componentIds;
    const src = `${getCanvasUrl()}?processGroupId=${processGroupId}&componentIds=${compId}`
    return (
      <div className={styles.canvasWrapper}>
        <Header
          componentIdChange={this.componentIdChange}
          componentId={componentId}
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
