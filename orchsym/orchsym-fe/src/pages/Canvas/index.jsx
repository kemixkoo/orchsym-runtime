import React, { Component } from 'react'
import { getCanvasUrl } from '@/utils/authority';
import Header from './CanvasHeader';
import styles from './index.less';

const logo = window.logoHref.companyLogoIndex
export default class Index extends Component {
  state = {
    componentId: '',
    closePop: false,
  };

  componentDidMount() {
    const { match } = this.props
    const { params } = match
    const { processGroupId } = params
    this.setState(
      {
        componentId: processGroupId,
      }
    )
    window.gotoCanvasApp = (d) => {
      // console.log('processGroup', d)
      this.setState(
        {
          componentId: d.id,
        }
      )
      window.history.pushState({ title: d.status.name }, d.status.name, `https://${window.location.host}/canvas/${d.id}`);
    }
    // console.log('this.canvasIframe', this.canvasIframe.contentWindow, this.canvasIframe.contentWindow.iframeEnterGroup)
    window.gotoComponent = (processGroupIds, componentIds) => {
      this.setState(
        {
          componentId: processGroupIds,
        }
      )
      window.history.pushState({ title: processGroupIds }, processGroupId, `https://${window.location.host}/canvas/${processGroupIds}/${componentIds || ''}`);
    }
  }

  changeState = (obj) => {
    this.setState(obj)
  }

  componentIdChange = (id) => {
    this.canvasIframe.contentWindow.iframeEnterGroup(id)
    this.setState(
      {
        componentId: id,
        closePop: true,
      }
    )
    window.history.pushState({ title: id }, id, `https://${window.location.host}/canvas/${id}`);
  }

  render() {
    const { componentId, closePop } = this.state
    const { match, menuData } = this.props
    const { params } = match
    const { processGroupId, componentIds } = params
    const compId = (componentIds === '0' || !componentIds) ? '' : componentIds;
    const src = `${getCanvasUrl()}?processGroupId=${processGroupId}&componentIds=${compId}`
    return (
      <div className={styles.canvasWrapper}>
        <Header
          changeState={this.changeState}
          closePop={closePop}
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
