import React, { PureComponent } from 'react';
import SaveTemp from './SaveTemp';
import CreateOrEditApp from './CreateOrEditApp';
import ApplicationHeader from './ApplicationHeader';
import AppList from './AppList';
// import IconFont from '@/components/IconFont';

export default class Application extends PureComponent {
  state = {
    title: null,
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
    const { title, createAppVisible, saveTempVisible } = this.state;

    return (
      <div>
        <ApplicationHeader />
        <AppList />
        <SaveTemp visible={saveTempVisible} handleSaveCancel={this.handleSaveCancel} />
        <CreateOrEditApp visible={createAppVisible} handleCreateCancel={this.handleCreateCancel} title={title} />
      </div>
    );
  }
}
