import React, {Component} from 'react';
import './App.css';

import axios from 'axios';

class CallDisplay extends React.Component {
  state = {
    msg: "UNINITIALIZED"
  }

  componentDidMount() {
    this.refreshCall()
  }

  render() {
    return (
      <div>
        <p>{this.state.msg}</p><br/>
        <button onClick={this.refreshCall}>Refresh</button><br/>
        <button onClick={this.refreshEtag}>ETag</button>
        <button onClick={this.refreshLastModified}>Last Modified</button><br/>
        <button onClick={this.refreshEtagLastModified}>ETag and Last Modified</button><br/>
        <button onClick={this.refreshEtag1m}>ETag max-age 1m</button>
        <button onClick={this.refreshLastModified1m}>Last Modified max-age 1m</button><br/>
      </div>
    )
  }

  refreshCall = () => {
    axios.get('http://localhost:8080/').then(this.refreshResp)
  }

  refreshEtag = () => {
    axios.get('http://localhost:8080/etag').then(this.refreshResp)
  }

  refreshLastModified = () => {
    axios.get('http://localhost:8080/last-modified').then(this.refreshResp)
  }

  refreshEtagLastModified = () => {
    axios.get('http://localhost:8080/etag-last-modified').then(this.refreshResp)
  }

  refreshEtag1m = () => {
    axios.get('http://localhost:8080/etag-1m').then(this.refreshResp)
  }

  refreshLastModified1m = () => {
    axios.get('http://localhost:8080/last-modified-1m').then(this.refreshResp)
  }

  refreshResp = (res) => {
      this.setState({msg: res.data});
  }
}

class App extends Component {
  render() {
    return (
      <div className="App">
        <div className="App-intro">
          <CallDisplay/>
        </div>
      </div>
    );
  }
}

export default App;
