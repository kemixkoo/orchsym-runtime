import React, { Component } from 'react'
import { getToken, getCanvasUrl } from '@/utils/authority';

export default class Index extends Component {
  render() {
    const { match } = this.props
    const { params } = match
    const { processGroupId } = params
    const src = `${getCanvasUrl()}?processGroupId=${processGroupId}&componentIds=&token=${getToken()}`
    return (
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
    );
  }
}
