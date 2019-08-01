import React, { PureComponent } from 'react';
import SaveTemp from './SaveTemp';
import CreateApplication from './CreateApplication';
import ApplicationHeader from './ApplicationHeader';
import AppList from './AppList';
// import IconFont from '@/components/IconFont';

export default class Application extends PureComponent {
  state = {
    createAppVisible: null,
    saveTempVisible: null,
  };

  showCreateModal = () => {
    this.setState({
      createAppVisible: true,
    })
  }

  handleCreateCancel = () => {
    this.setState({
      createAppVisible: false,
    })
  }

  showEditModal = () => {
    this.setState({
      createAppVisible: true,
    })
  }

  handleEditCancel = () => {
    this.setState({
      createAppVisible: false,
    })
  }

  showSaveTemp = () => {
    this.setState({
      saveTempVisible: true,
    })
  }

  handleSaveCancel = () => {
    this.setState({
      saveTempVisible: false,
    })
  }

  render() {
    const { createAppVisible, saveTempVisible } = this.state;

    return (
      <div>
        <ApplicationHeader />
        <AppList />
        <SaveTemp visible={saveTempVisible} handleSaveCancel={this.handleSaveCancel} />
        <CreateApplication visible={createAppVisible} handleCreateCancel={this.handleCreateCancel} />
      </div>
    );
  }
}
