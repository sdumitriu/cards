//
//  Licensed to the Apache Software Foundation (ASF) under one
//  or more contributor license agreements.  See the NOTICE file
//  distributed with this work for additional information
//  regarding copyright ownership.  The ASF licenses this file
//  to you under the Apache License, Version 2.0 (the
//  "License"); you may not use this file except in compliance
//  with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing,
//  software distributed under the License is distributed on an
//  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//  KIND, either express or implied.  See the License for the
//  specific language governing permissions and limitations
//  under the License.
//
import React from 'react';
import ReactDOM from 'react-dom';
import { AppBar, Grid, Box, ButtonGroup, Button, Toolbar, Typography, Link, IconButton, Icon, withStyles } from '@material-ui/core';

const styles = theme => ({
  title: {
    flexGrow: 1,
  },
});

class GlobalHeader extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      "loggedIn": window.Sling.getSessionInfo() !== null && window.Sling.getSessionInfo().userID !== 'anonymous'
    };
  }

  render() {
    return (
      <AppBar position="fixed">
        <Toolbar>
          <Typography variant="h6" color="inherit" noWrap className={this.props.classes.title}>
            <Link href="/" variant="inherit" color="inherit" underline="none">LFS Repository</Link>
          </Typography>
          {this.state.loggedIn ?

          <Typography variant="h6" align="right" color="inherit">{window.Sling.getSessionInfo().userID} <Link href="/system/sling/logout" color="inherit"><Button variant="contained">Sign Out</Button></Link></Typography>

          :

          <ButtonGroup variant="contained" size="small">
            <Button color="primary" id="login-homepage-button">
              Login
            </Button>
            <Button id="signup-homepage-button">
              Sign Up
            </Button>
          </ButtonGroup>

        }
        </Toolbar>
      </AppBar>
    );
  }
}

const GlobalHeaderComponent = withStyles(styles)(GlobalHeader);

ReactDOM.render(<GlobalHeaderComponent />, document.querySelector('#header-container'));
